package tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.Collection;

import org.xml.sax.SAXException;

import owl.core.connections.NoMatchFoundException;
import owl.core.connections.SiftsConnection;
import owl.core.features.SiftsFeature;
import owl.core.runners.blast.BlastHit;
import owl.core.runners.blast.BlastHitList;
import owl.core.runners.blast.BlastRunner;
import owl.core.runners.blast.BlastXMLParser;
import owl.core.sequence.alignment.PairwiseSequenceAlignment;
import owl.core.structure.PdbChain;
import owl.core.structure.PdbAsymUnit;
import owl.core.structure.PdbCodeNotFoundException;
import owl.core.structure.PdbLoadException;
import owl.core.util.MySQLConnection;

public class Pdb2UniprotCheck {
	
	private static final String PDBASE_DB ="pdbase";
	private static final String SIFTS_FILE = "/nfs/data/dbs/uniprot/current/pdb_chain_uniprot.lst";
	private static final String LISTFILE = "/home/duarte_j/cullpdb/cullpdb_pc90_res3.0_R1.0_d100426_chains18209";//cullpdb_pc20_res1.6_R0.25_d100426_chains1673
	
	private static final String BLASTOUT_SUFFIX = "blast.out.xml";
	private static final String FASTA_SUFFIX = ".fa";
	private static final String BLAST_BASENAME = "homSearch";
	
	private static final int 	BLAST_OUTPUT_TYPE = 7;  // xml output
	private static final boolean BLAST_NO_FILTERING = true;

	private static final String BLAST_BIN_DIR = "/usr/bin";
	private static final String BLAST_DB_DIR = "/nfs/data/dbs/uniprot/current";
	private static final String BLAST_DB = "uniprot_all.fasta";//"uniprot_all.fasta";
	private static final int DEFAULT_BLAST_NUMTHREADS = 4;
	
	private static final double ID_CUTOFF = 95;
	private static final double COV_CUTOFF = 0.95;
	
	private static final int SHORT_SEQ_CUTOFF = 35;

	
	public static void main (String[] args) throws Exception {
		
		System.setProperty("java.util.logging.config.file","/dev/null"); //get rid of logging from jaligner
		
		MySQLConnection conn = new MySQLConnection();
		SiftsConnection siftsConn = new SiftsConnection(SIFTS_FILE);
		System.out.println("Total SIFTS mappings for whole PDB: "+siftsConn.getMappingsCount());
		
		BufferedReader flist = new BufferedReader(new FileReader(LISTFILE));
		String line;
		
		File outFile = new File("/home/duarte_j/pdb2uniprot.blast");
		PrintStream ps = new PrintStream(outFile);
		
		int totalChains = 0;
		int missMappings = 0;
		int foundWithBlast = 0;
		int noHitsCount = 0;
		while ((line = flist.readLine() ) != null ) {
			if (line.startsWith("IDs")) continue;
			String pdbCode = line.split("\\s+")[0].substring(0,4).toLowerCase();
			String pdbChainCode = line.split("\\s+")[0].substring(4,5);
			
			PdbChain pdb = null;
			try {
				PdbAsymUnit fullpdb = new PdbAsymUnit(pdbCode,conn,PDBASE_DB);
				pdb = fullpdb.getChain(pdbChainCode);
				if (pdb.getFullLength()<SHORT_SEQ_CUTOFF) continue; //we don't even consider very short chains, they are usually pathological cases
				totalChains++;
				Collection<SiftsFeature> mappings = siftsConn.getMappings(pdbCode, pdbChainCode);
				//System.out.println(pdbCode+pdbChainCode+" "+mappings.size());
				for (@SuppressWarnings("unused") SiftsFeature mapping:mappings) {
					// this will throw exceptions InvalidFeatureCoordinates or OverlappingFeature
					// it just should not happen. If it does it's a test failure (that's why we throw the exceptions)
					// TODO At the moment I disabled this since we have dropped the feature implementation in PdbChain class (it wasn't working anyway)
					// TODO Needs to rewrite this whole program if I ever need it again!!!
					//pdb.addFeature(mapping); 
				}

//				Collection<Feature> fs = pdb.getFeaturesOfType(FeatureType.SIFTS);
//				if (fs.size()>1) {
//					System.out.print(pdbCode+pdbChainCode+"\t");
//					for (Feature f:fs) {
//						SiftsFeature sf = (SiftsFeature) f;
//						System.out.print(sf.getUniprotId() +"\t"+ sf.getIntervalSet()+"\t");
//					}
//
//					System.out.print("("+pdb.getFullLength()+")\t("+pdb.getMinObsResSerial()+"-"+pdb.getMaxObsResSerial()+")");
//					System.out.println();
//				}

			} catch (PdbCodeNotFoundException e) {
				System.err.println(e.getMessage());
				continue;
			} catch (NoMatchFoundException e) {
				System.err.println(e.getMessage());
				missMappings++;
				BlastHitList blastList = blastIt(pdb);
				BlastHit bestHit = blastList.getBestHit();
				if (bestHit!=null) {
					double identity = bestHit.getTotalPercentIdentity();
					double coverage = bestHit.getQueryCoverage();
					ps.printf("%s\t%s\t%4.1f\t%4.2f\n",pdbCode+pdbChainCode,bestHit.getSubjectId(),identity,coverage);
					PairwiseSequenceAlignment pwAl = new PairwiseSequenceAlignment(
							bestHit.getMaxScoringHsp().getAlignment().getSequenceNoGaps(bestHit.getQueryId()), 
							bestHit.getMaxScoringHsp().getAlignment().getSequenceNoGaps(bestHit.getSubjectId()), 
							bestHit.getQueryId(), 
							bestHit.getSubjectId());
					pwAl.writeAlignment(ps);
					//bestHit.getAlignment().writeFasta(ps, 80, true);
					if (identity>ID_CUTOFF && coverage>COV_CUTOFF) {
						foundWithBlast++;
					}
				} else {
					System.out.println("No hits");
					noHitsCount++;
				}
				continue;
			} catch (PdbLoadException e) {
				System.err.println(e.getMessage());
			}
		}
		flist.close();
		ps.close();
		System.out.println("Total chains: "+totalChains);
		System.out.println("Missing mappings: "+missMappings);
		System.out.println("Found with blast: "+foundWithBlast);
		System.out.println("No blast hits: "+noHitsCount);
		
		
	}
	
	private static BlastHitList blastIt(PdbChain pdb) throws Exception {
		System.out.println("Blasting...");
		File outBlast = File.createTempFile(BLAST_BASENAME,BLASTOUT_SUFFIX);
		File inputSeqFile = File.createTempFile(BLAST_BASENAME,FASTA_SUFFIX);
		outBlast.deleteOnExit();
		inputSeqFile.deleteOnExit();
		pdb.getSequence().writeToFastaFile(inputSeqFile);
		BlastRunner blastRunner = new BlastRunner(BLAST_BIN_DIR, BLAST_DB_DIR);
		blastRunner.runBlastp(inputSeqFile, BLAST_DB, outBlast, BLAST_OUTPUT_TYPE, BLAST_NO_FILTERING, DEFAULT_BLAST_NUMTHREADS);
		
		BlastHitList blastList = null;
		try {
			BlastXMLParser blastParser = new BlastXMLParser(outBlast);
			blastList = blastParser.getHits();
		} catch (SAXException e1) {
			// if this happens it means that blast doesn't format correctly its XML, i.e. has a bug
			System.err.println("Unexpected error: "+e1.getMessage());
			System.exit(1);
		}
		return blastList;
	}
	
}
