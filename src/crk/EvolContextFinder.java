package crk;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import owl.core.runners.TcoffeeError;
import owl.core.runners.blast.BlastError;
import owl.core.sequence.UniprotVerMisMatchException;
import owl.core.structure.PdbAsymUnit;

public class EvolContextFinder {

	private static final Log LOGGER = LogFactory.getLog(EvolContextFinder.class);
	
	private static final Pattern  NONPROT_PATTERN = Pattern.compile("^X+$");
	
	private PdbAsymUnit pdb;
	private String pdbName;
	private String baseName;
	
	private TreeMap<String,ChainEvolContext> cecs; // map of representative chain codes to ChainEvolContexts
	
	public EvolContextFinder(PdbAsymUnit pdb, String pdbName, String baseName) {
		this.pdb = pdb;
		this.pdbName = pdbName;
		this.baseName = baseName;
		this.cecs = new TreeMap<String,ChainEvolContext>();
	}
	
	public void logUniqSequences() {
		String msg = "Unique sequences for "+pdbName+":";
		int i = 1;
		for (String representativeChain:pdb.getAllRepChains()) {
			List<String> entity = pdb.getSeqIdenticalGroup(representativeChain);
			msg+=" "+i+":";
			for (String chain:entity) {
				msg+=" "+chain;
			}
			i++;
		}
		LOGGER.info(msg);

	}

	public TreeMap<String,ChainEvolContext> getAllChainEvContext() {
		for (String representativeChain:pdb.getAllRepChains()) {
			
			Matcher nonprotMatcher = NONPROT_PATTERN.matcher(pdb.getChain(representativeChain).getSequence());
			if (nonprotMatcher.matches()) {
				LOGGER.warn("Representative chain "+representativeChain+" does not seem to be a protein chain. Won't analyse it.");
				continue;
			}

			cecs.put(representativeChain,new ChainEvolContext(pdb.getChain(representativeChain).getSequence(), representativeChain, pdb.getPdbCode(), pdbName));
		}
		return cecs;
	}
	
	public void retrieveQueryData(String emblCdsCacheDir, String siftsFile, String blastBinDir, String blastDbDir, String blastDb, int numThreads, boolean doScoreCRK) {
		// a) getting the uniprot ids corresponding to the query (the pdb sequence)
		for (ChainEvolContext chainEvCont:cecs.values()) {
			File emblQueryCacheFile = null;
			if (emblCdsCacheDir!=null) {
				emblQueryCacheFile = new File(emblCdsCacheDir,baseName+"."+pdbName+chainEvCont.getRepresentativeChainCode()+".query.emblcds.fa");
			}
			System.out.println("Finding query's uniprot mapping (through SIFTS or blasting)");
			try {
				chainEvCont.retrieveQueryData(siftsFile, emblQueryCacheFile, blastBinDir, blastDbDir, blastDb, numThreads,doScoreCRK);
			} catch (BlastError e) {
				LOGGER.error("Couldn't run blast to retrieve query's uniprot mapping");
				LOGGER.error(e.getMessage());
				System.err.println("Couldn't run blast to retrieve query's uniprot mapping");
				System.exit(1);
			}  catch (IOException e) {
				LOGGER.error("Problems while retrieving query data");
				LOGGER.error(e.getMessage());
				System.exit(1);
			}
			if (doScoreCRK && chainEvCont.getQueryRepCDS()==null) {
				// note calling chainEvCont.canDoCRK() will also check for this condition (here we only want to log it once)
				LOGGER.error("No CDS good match for query sequence! can't do CRK analysis on it.");
			}
		}
	}
	
	public void retrieveHomologues(String blastCacheDir, String blastBinDir, String blastDbDir, String blastDb, int numThreads, double idCutoff, double queryCoverageCutoff, String emblCdsCacheDir, boolean doScoreCRK, int maxNumSeqsSelecton) {
		// b) getting the homologs and sequence data 
		for (ChainEvolContext chainEvCont:cecs.values()) {

			System.out.println("Blasting for homologues...");
			File blastCacheFile = null;
			if (blastCacheDir!=null) {
				blastCacheFile = new File(blastCacheDir,baseName+"."+pdbName+chainEvCont.getRepresentativeChainCode()+".blast.xml"); 
			}
			try {
				chainEvCont.retrieveHomologs(blastBinDir, blastDbDir, blastDb, numThreads, idCutoff, queryCoverageCutoff, blastCacheFile);
				LOGGER.info("Uniprot version used: "+chainEvCont.getUniprotVer());
			} catch (UniprotVerMisMatchException e) {
				LOGGER.error(e.getMessage());
				System.err.println("Mismatch of Uniprot versions! Exiting.");
				System.err.println(e.getMessage());
				System.exit(1);					
			} catch (BlastError e) {
				LOGGER.error("Couldn't run blast to retrieve homologs.");
				LOGGER.error(e.getMessage());
				System.err.println("Couldn't run blast to retrieve homologs.");
				System.exit(1);
			} catch (IOException e) {
				LOGGER.error("Problem while blasting for sequence homologs");
				LOGGER.error(e.getMessage());
				System.exit(1);
			}

			System.out.println("Retrieving UniprotKB data and EMBL CDS sequences");
			File emblHomsCacheFile = null;
			if (emblCdsCacheDir!=null) {
				emblHomsCacheFile = new File(emblCdsCacheDir,baseName+"."+pdbName+chainEvCont.getRepresentativeChainCode()+".homologs.emblcds.fa");
			}
			try {
				chainEvCont.retrieveHomologsData(emblHomsCacheFile, doScoreCRK);
			} catch (UniprotVerMisMatchException e) {
				LOGGER.error(e.getMessage());
				System.err.println("Mismatch of Uniprot versions! Exiting.");
				System.err.println(e.getMessage());
				System.exit(1);
			} catch (IOException e) {
				LOGGER.error("Problem while fetching CDS data");
				LOGGER.error(e.getMessage());
				System.exit(1);
			}
			if (doScoreCRK && !chainEvCont.isConsistentGeneticCodeType()){
				// note calling chainEvCont.canDoCRK() will also check for this condition (here we only want to log it once)
				LOGGER.error("The list of homologs does not have a single genetic code type, can't do CRK analysis on it.");
			}
			
			// remove redundancy
			chainEvCont.removeRedundancy();

			// skimming so that there's not too many sequences for selecton
			chainEvCont.skimList(maxNumSeqsSelecton);
			
			// check the back-translation of CDS to uniprot
			// check whether we have a good enough CDS for the chain
			if (doScoreCRK && chainEvCont.canDoCRK()) {
				LOGGER.info("Number of homologs with at least one uniprot CDS mapping: "+chainEvCont.getNumHomologsWithCDS());
				LOGGER.info("Number of homologs with valid CDS: "+chainEvCont.getNumHomologsWithValidCDS());
			}

		}	
	}
	
	public void align(File tcoffeeBin, boolean useTcoffeeVeryFastMode) {
		// c) align
		for (ChainEvolContext chainEvCont:cecs.values()) {

			System.out.println("Aligning protein sequences with t_coffee...");
			try {
				chainEvCont.align(tcoffeeBin, useTcoffeeVeryFastMode);
			} catch (TcoffeeError e) {
				LOGGER.error("Couldn't run t_coffee to align protein sequences");
				LOGGER.error(e.getMessage());
				System.err.println("Couldn't run t_coffee to align protein sequences");
				System.exit(1);
			} catch (IOException e) {
				LOGGER.error("Problems while running t_coffee to align protein sequences");
				LOGGER.error(e.getMessage());
				System.exit(1);
			}
		}
	}
	
	public void writeSeqsAndAlnToFile(boolean doScoreCRK, File outDir){
		for (ChainEvolContext chainEvCont:cecs.values()) {

			File outFile = null;
			try {
				// writing homolog sequences to file
				outFile = new File(outDir,baseName+"."+pdbName+chainEvCont.getRepresentativeChainCode()+".fa");
				chainEvCont.writeHomologSeqsToFile(outFile);

				// printing summary to file
				outFile = new File(outDir,baseName+"."+pdbName+chainEvCont.getRepresentativeChainCode()+".log");
				PrintStream log = new PrintStream(outFile);
				chainEvCont.printSummary(log);
				log.close();
				// writing the alignment to file
				outFile = new File(outDir,baseName+"."+pdbName+chainEvCont.getRepresentativeChainCode()+".aln");
				chainEvCont.writeAlignmentToFile(outFile);
				// writing the nucleotides alignment to file
				if (doScoreCRK && chainEvCont.canDoCRK()) {
					outFile = new File(outDir,baseName+"."+pdbName+chainEvCont.getRepresentativeChainCode()+".cds.aln");
					chainEvCont.writeNucleotideAlignmentToFile(outFile);
				}
			} catch(FileNotFoundException e){
				LOGGER.error("Couldn't write file "+outFile);
				LOGGER.error(e.getMessage());
			}
		}
	}
	
	public void computeEvolutionaryScores(File outDir, int reducedAlphabet, boolean doScoreCRK, File selectonBin, double selectonEpsilon) {
		for (ChainEvolContext chainEvCont:cecs.values()) {

			// d) computing entropies
			chainEvCont.computeEntropies(reducedAlphabet);

			// e) compute ka/ks ratios
			if (doScoreCRK && chainEvCont.canDoCRK()) {
				System.out.println("Running selecton (this will take long)...");
				try {
				chainEvCont.computeKaKsRatiosSelecton(selectonBin, 
						new File(outDir,baseName+"."+pdbName+chainEvCont.getRepresentativeChainCode()+".selecton.res"),
						new File(outDir,baseName+"."+pdbName+chainEvCont.getRepresentativeChainCode()+".selecton.log"), 
						new File(outDir,baseName+"."+pdbName+chainEvCont.getRepresentativeChainCode()+".selecton.tree"),
						new File(outDir,baseName+"."+pdbName+chainEvCont.getRepresentativeChainCode()+".selecton.global"),
						selectonEpsilon);
				} catch (IOException e) {
					LOGGER.error("Problems while running selecton");
					LOGGER.error(e.getMessage());
					System.exit(1);
				}
			}
		}
	}
	
	public void writeEvolScoresLog(File outDir, boolean doScoreCRK, String entropiesFileSuffix, String kaksFileSuffix) {
		for (ChainEvolContext chainEvCont:cecs.values()) {

			File outFile = null;
			try {
				// writing the conservation scores (entropies/kaks) log file
				outFile = new File(outDir,baseName+"."+pdbName+chainEvCont.getRepresentativeChainCode()+entropiesFileSuffix);
				PrintStream conservScoLog = new PrintStream(outFile);
				chainEvCont.printConservationScores(conservScoLog, ScoringType.ENTROPY);
				conservScoLog.close();
				if (doScoreCRK && chainEvCont.canDoCRK()) {
					outFile = new File(outDir,baseName+"."+pdbName+chainEvCont.getRepresentativeChainCode()+kaksFileSuffix);
					conservScoLog = new PrintStream(outFile);
					chainEvCont.printConservationScores(conservScoLog, ScoringType.KAKS);
					conservScoLog.close();				
				}
			} catch (FileNotFoundException e) {
				LOGGER.error("Could not write the scores log file "+outFile);
				LOGGER.error(e.getMessage());
			}
		}
	}
}
