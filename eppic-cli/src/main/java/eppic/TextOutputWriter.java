package eppic;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eppic.commons.sequence.AlignmentConstructionException;
import eppic.commons.sequence.MultipleSequenceAlignment;
import eppic.commons.sequence.Sequence;
import eppic.model.AssemblyDB;
import eppic.model.AssemblyScoreDB;
import eppic.model.ChainClusterDB;
import eppic.model.ContactDB;
import eppic.model.HomologDB;
import eppic.model.InterfaceClusterDB;
import eppic.model.InterfaceClusterScoreDB;
import eppic.model.InterfaceDB;
import eppic.model.InterfaceScoreDB;
import eppic.model.PdbInfoDB;
import eppic.model.ResidueDB;
import eppic.model.ScoringMethod;

public class TextOutputWriter {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TextOutputWriter.class);
	
	private PdbInfoDB pdbInfo;
	private EppicParams params;
	
	public TextOutputWriter (PdbInfoDB pdbInfo, EppicParams params) {
		this.pdbInfo = pdbInfo;
		this.params = params;
	}

	
	
	public void writeInterfacesInfoFile() throws IOException {
		PrintStream ps = new PrintStream(params.getOutputFile(EppicParams.INTERFACES_FILE_SUFFIX));
		ps.println("Interfaces for input structure "+(params.isInputAFile()?params.getInFile().getName():params.getPdbCode()));
		ps.println("ASAs values calculated with "+params.getnSpherePointsASAcalc()+" sphere sampling points");
		
		
		printInterfacesInfo(ps, params.isUsePdbResSer());
		ps.close();
	}
	
	private void printInterfacesInfo(PrintStream ps, boolean usePdbResSer) {
		
		for (InterfaceClusterDB interfaceCluster: pdbInfo.getInterfaceClusters()) {
			for (InterfaceDB interfaceItem:interfaceCluster.getInterfaces()) {

				ps.print("# ");
				ps.printf("%d\t%9.2f\t%s\t%s\n",interfaceItem.getInterfaceId(),interfaceItem.getArea(), 
						interfaceItem.getChain1()+"+"+interfaceItem.getChain2(),interfaceItem.getOperator());
				
				ps.print("## ");
				this.printInterfacesMolInfo(ps, interfaceItem, 1, usePdbResSer);				
				
				ps.print("## ");
				this.printInterfacesMolInfo(ps, interfaceItem, 2, usePdbResSer);
				
			}
		}
	}
	
	private void printInterfacesMolInfo(PrintStream ps, InterfaceDB interfaceItem, int side, boolean usePdbResSer) {
		
		List<ResidueDB> cores = new ArrayList<ResidueDB>();
		
		for (ResidueDB residue:interfaceItem.getResidues()) {
			if (residue.getSide()==side) {
				
				if (residue.getRegion()==ResidueDB.CORE_GEOMETRY) cores.add(residue);
			}
		}
		
		String pdbChainCode = null;
		if (side==1) pdbChainCode = interfaceItem.getChain1();
		if (side==2) pdbChainCode = interfaceItem.getChain2();
		
		ps.println(side+"\t"+pdbChainCode+"\tprotein");

		if (cores.size()>0) {
			ps.printf("## core (%4.2f): %s\n", params.getCAcutoffForGeom(), getResString(cores, usePdbResSer));
		}
		ps.println("## seqres pdb res asa bsa burial(percent)");

		for (ResidueDB residue:interfaceItem.getResidues()) {	
			if (residue.getSide()==side) {
				ps.printf("%d\t%s\t%s\t%6.2f\t%6.2f",residue.getResidueNumber(),residue.getPdbResidueNumber(),residue.getResidueType(),residue.getAsa(),residue.getBsa());
				double percentBurial = 100.0*residue.getBsa()/residue.getAsa();
				if (percentBurial>0.1) {
					ps.printf("\t%5.1f\n",percentBurial);
				} else {
					ps.println();
				}
			}
		}
	}
	
	private String getResString(List<ResidueDB> residues, boolean usePdbResSer) {
		String str = "";
		for (int i=0;i<residues.size();i++) {
			String serial = null;
			if (usePdbResSer) {
				serial = residues.get(i).getPdbResidueNumber();
			} else {
				serial = ""+residues.get(i).getResidueNumber();
			}
			if (i!=residues.size()-1)
				str+=serial+",";
			else
				str+=serial;
		}
		return str;
	}
	
	public void writeScoresFile() throws IOException {
		PrintStream ps = new PrintStream(params.getOutputFile(EppicParams.SCORES_FILE_SUFFIX));
		printScoresHeaders(ps);
		for (InterfaceClusterDB interfaceCluster: pdbInfo.getInterfaceClusters()) {			
			for (InterfaceDB interfaceItem:interfaceCluster.getInterfaces()) {
				printInterfaceScores(ps, interfaceItem);
				ps.println();
			}
			if (interfaceCluster.size()>1) {
				printInterfaceClusterScores(ps, interfaceCluster);
				ps.println();
			}
		}
		ps.close();
	}
	
	private static void printScoresHeaders(PrintStream ps) {
		ps.printf("%7s\t%7s\t%7s\t%7s\t%7s\t%9s\t","cluster","id","chains","optype","topo","area");
		
		ps.printf("\t%7s\t%7s\t%7s\t%7s\t", "sc1-gm","sc2-gm","sc-gm","call-gm");
		ps.printf("\t%7s\t%7s\t%7s\t%7s\t", "sc1-cr","sc2-cr","sc-cr","call-cr");
		ps.printf("\t%7s\t%7s\t%7s\t%7s\t", "sc1-cs","sc2-cs","sc-cs","call-cs");
		ps.printf("\t%7s\t%7s\t%7s",        "sc","conf","call");
		
		ps.println();
	}
	
	private void printInterfaceScores(PrintStream ps, InterfaceDB interfaceItem) {

		String topology = "";
		// the 2 conditions can't be true at the same time
		if (interfaceItem.isInfinite()) topology = "inf";
		if (interfaceItem.isIsologous()) topology ="iso";
		
		// common info
		ps.printf("%7d\t%7d\t%7s\t%7s\t%7s\t%9.2f\t",
				interfaceItem.getClusterId(),
				interfaceItem.getInterfaceId(), 				 
				interfaceItem.getChain1()+"+"+interfaceItem.getChain2(),
				interfaceItem.getOperatorType(),
				topology,
				interfaceItem.getArea());
		
		InterfaceScoreDB interfaceScoreGm = interfaceItem.getInterfaceScore(ScoringMethod.EPPIC_GEOMETRY);
		InterfaceScoreDB interfaceScoreCr = interfaceItem.getInterfaceScore(ScoringMethod.EPPIC_CORERIM);
		InterfaceScoreDB interfaceScoreCs = interfaceItem.getInterfaceScore(ScoringMethod.EPPIC_CORESURFACE);
		InterfaceScoreDB interfaceScoreFn = interfaceItem.getInterfaceScore(ScoringMethod.EPPIC_FINAL);
		
		// geometry
		// there should always be geometry scores available, no check for null
		ps.printf("\t%7.0f\t%7.0f\t%7.0f\t%7s\t",
				interfaceScoreGm.getScore1(),
				interfaceScoreGm.getScore2(),
				interfaceScoreGm.getScore(),
				interfaceScoreGm.getCallName());
		
		// core-rim
		if (interfaceScoreCr==null) {
			ps.printf("\t%7s\t%7s\t%7s\t%7s\t","","","","");
		} else {
			ps.printf("\t%7.2f\t%7.2f\t%7.2f\t%7s\t",
					interfaceScoreCr.getScore1(),
					interfaceScoreCr.getScore2(),
					interfaceScoreCr.getScore(),
					interfaceScoreCr.getCallName());			
		}
		
		// core-surface
		if (interfaceScoreCs==null) {
			ps.printf("\t%7s\t%7s\t%7s\t%7s\t","","","","");
		} else {
			ps.printf("\t%7.2f\t%7.2f\t%7.2f\t%7s\t",
					interfaceScoreCs.getScore1(),
					interfaceScoreCs.getScore2(),
					interfaceScoreCs.getScore(),
					interfaceScoreCs.getCallName());			
		}
		
		
		// final
		if (interfaceScoreFn==null) {
			ps.printf("\t%7s\t%7s\t%7s","","","");
		} else {
			ps.printf("\t%7.0f\t%7.2f\t%7s",
					interfaceScoreFn.getScore(),
					interfaceScoreFn.getConfidence(),
					interfaceScoreFn.getCallName());			
		}
		
		
		// TODO should we display reasons? if the call does not come from score (hard areas, disulfides, etc) the call reason is useful
		// TODO should we display warnings? they are per InterfaceDB and not per InterfaceScoreDB
	}

	private void printInterfaceClusterScores(PrintStream ps, InterfaceClusterDB interfaceCluster) {
		// common info
		ps.printf("%7d\t%7s\t%7s\t%7s\t%7s\t%9.2f\t",
				interfaceCluster.getClusterId(),
				"-------", 				 
				"-------",
				"-------",
				"------>",
				interfaceCluster.getAvgArea());
		
		InterfaceClusterScoreDB interfaceScoreGm = interfaceCluster.getInterfaceClusterScore(ScoringMethod.EPPIC_GEOMETRY);
		InterfaceClusterScoreDB interfaceScoreCr = interfaceCluster.getInterfaceClusterScore(ScoringMethod.EPPIC_CORERIM);
		InterfaceClusterScoreDB interfaceScoreCs = interfaceCluster.getInterfaceClusterScore(ScoringMethod.EPPIC_CORESURFACE);
		InterfaceClusterScoreDB interfaceScoreFn = interfaceCluster.getInterfaceClusterScore(ScoringMethod.EPPIC_FINAL);

		// geometry
		// there should always be geometry scores available, anyway we check for nulls in case
		if (interfaceScoreGm==null) {
			ps.printf("\t%7s\t%7s\t%7s\t%7s\t","","","","");
		} else {
			ps.printf("\t%7.0f\t%7.0f\t%7.0f\t%7s\t",
				interfaceScoreGm.getScore1(),
				interfaceScoreGm.getScore2(),
				interfaceScoreGm.getScore(),
				interfaceScoreGm.getCallName());
		}
		
		// core-rim
		if (interfaceScoreCr==null) {
			ps.printf("\t%7s\t%7s\t%7s\t%7s\t","","","","");
		} else {
			ps.printf("\t%7.2f\t%7.2f\t%7.2f\t%7s\t",
					interfaceScoreCr.getScore1(),
					interfaceScoreCr.getScore2(),
					interfaceScoreCr.getScore(),
					interfaceScoreCr.getCallName());			
		}
		
		// core-surface
		if (interfaceScoreCs==null) {
			ps.printf("\t%7s\t%7s\t%7s\t%7s\t","","","","");
		} else {
			ps.printf("\t%7.2f\t%7.2f\t%7.2f\t%7s\t",
					interfaceScoreCs.getScore1(),
					interfaceScoreCs.getScore2(),
					interfaceScoreCs.getScore(),
					interfaceScoreCs.getCallName());			
		}
		
		
		// final
		if (interfaceScoreFn==null) {
			ps.printf("\t%7s\t%7s\t%7s","","","");
		} else {
			ps.printf("\t%7.0f\t%7.2f\t%7s",
					interfaceScoreFn.getScore(),
					interfaceScoreFn.getConfidence(),
					interfaceScoreFn.getCallName());			
		}
		
		
		// TODO should we display reasons? if the call does not come from score (hard areas, disulfides, etc) the call reason is useful
		// TODO should we display warnings? they are per InterfaceDB and not per InterfaceScoreDB
	}
	
	public void writePdbAssignments() throws IOException{
		
		PrintStream ps = new PrintStream(params.getOutputFile(EppicParams.PDB_BIOUNIT_ASSIGN_FILE_SUFFIX));
		
		AssemblyDB eppicAssembly = null;
		AssemblyDB pdb1Assembly = null;
		for (AssemblyDB assembly: pdbInfo.getAssemblies()) {
			for (AssemblyScoreDB as:assembly.getAssemblyScores()) {
				if (as.getMethod().equals(ScoringMethod.EPPIC_FINAL)) {
					if (eppicAssembly==null) eppicAssembly = assembly;
					else LOGGER.warn("More than 1 EPPIC assembly assignment!");
				}
				if (as.getMethod().equals(DataModelAdaptor.PDB_BIOUNIT_METHOD)) {
					if (pdb1Assembly==null) pdb1Assembly = assembly;
					else LOGGER.warn("More than 1 PDB1 assembly assignment!");					
				}

			}
		}
		
		List<InterfaceClusterDB> interfaceClusters = pdbInfo.getInterfaceClusters();		
		
		int eppicAssemblySize = 0;
		int pdb1AssemblySize = 0;
		String pdb1SymmetryStr = "";
		String eppicSymmetryStr = "";
		
		Set<InterfaceClusterDB> pdb1InterfaceClusters = null;
		
		if (eppicAssembly!=null) {
			eppicAssemblySize = eppicAssembly.getMmSize();
			if (eppicAssembly.getSymmetry()!=null)
				eppicSymmetryStr = ","+eppicAssembly.getSymmetry();
		}
		if (pdb1Assembly!=null) {
			pdb1InterfaceClusters = pdb1Assembly.getInterfaceClusters();
			pdb1AssemblySize = pdb1Assembly.getMmSize();
			if (pdb1Assembly.getSymmetry()!=null)
				pdb1SymmetryStr = ","+pdb1Assembly.getSymmetry();
		}
		
		
		
		ps.printf("%7s\t%20s\t%10s\t%10s\n",
				"clustId","members",
				ScoringMethod.EPPIC_FINAL + "("+eppicAssemblySize+eppicSymmetryStr+")",
				DataModelAdaptor.PDB_BIOUNIT_METHOD         + "("+pdb1AssemblySize+pdb1SymmetryStr+")"); 
		
		for (InterfaceClusterDB interfaceCluster:interfaceClusters) {
			String membersStr = "";
			for (InterfaceDB interfaceItem:interfaceCluster.getInterfaces()) {
				membersStr += interfaceItem.getInterfaceId()+" ";
			}
			ps.printf("%7d\t%20s\t",interfaceCluster.getClusterId(),membersStr);

			// TODO we should also take the assembly for eppic method from the assembly mapping but for the moment we don't have assembly predictions yet and we take the raw pairwise calls
			InterfaceClusterScoreDB icsEppic = interfaceCluster.getInterfaceClusterScore(ScoringMethod.EPPIC_FINAL);			
			
			if (icsEppic==null) {
				ps.printf("%10s\t","");
			} else {
				ps.printf("%10s\t",icsEppic.getCallName());
			}
			if (pdb1InterfaceClusters==null) {
				ps.printf("%10s","");
			} else {
				String call = null;
				if (containsCluster(pdb1InterfaceClusters, interfaceCluster)) call = CallType.BIO.getName();
				else call = CallType.CRYSTAL.getName();
				ps.printf("%10s",call);
			}
			
			
			ps.println();
			
		}
		
		ps.close();
	}
	
	private static boolean containsCluster(Set<InterfaceClusterDB> interfClusterSet, InterfaceClusterDB interfCluster) {
		for (InterfaceClusterDB ic:interfClusterSet) {
			if (ic.getClusterId()==interfCluster.getClusterId()) return true;
		}
		return false;
	}
	
	/**
	 * Writes to files (one per ChainCluster) a summary of the query and uniprot/cds identifiers and homologs with 
	 * their uniprot/cds identifiers
	 */
	public void writeHomologsSummaries() throws IOException {
		List<ChainClusterDB> chainClusters = pdbInfo.getChainClusters();
		
		for (ChainClusterDB cc:chainClusters) {
			if (cc.isHasUniProtRef()) {
				writeHomologsSummaryPerChain(cc);
			}
		}
	}
	
	private void writeHomologsSummaryPerChain(ChainClusterDB chainCluster) throws IOException {
		PrintStream ps = new PrintStream(params.getOutputFile("."+chainCluster.getRepChain()+".log"));
		ps.println("Query: chain "+chainCluster.getRepChain());
		ps.println("UniProt id for query:");
		ps.print(chainCluster.getRefUniProtId());
		
		if (chainCluster.getFirstTaxon()!=null && chainCluster.getLastTaxon()!=null) 
			ps.println("\t"+chainCluster.getFirstTaxon()+"\t"+chainCluster.getLastTaxon());
		else ps.println("\tunknown taxonomy");
		ps.println();
		
		ps.println("UniProt version: "+pdbInfo.getRunParameters().getUniProtVersion());
		ps.println("Homologs: "+chainCluster.getNumHomologs()+" with minimum "+
				String.format("%4.2f",chainCluster.getSeqIdCutoff())+" identity and "+
				String.format("%4.2f",pdbInfo.getRunParameters().getQueryCovCutoff())+" query coverage");

		double clusteringPercentId = chainCluster.getClusteringSeqId();		
		if (clusteringPercentId>0) 
			ps.println("List is redundancy reduced through clustering on "+clusteringPercentId+" sequence identity");
		
		for (HomologDB hom:chainCluster.getHomologs()) {
			ps.printf("%-13s",hom.getUniProtId());
			ps.printf("\t%5.1f",hom.getSeqId()*100.0);
			ps.printf("\t%5.1f",hom.getQueryCoverage()*100.0);
			if (hom.getFirstTaxon()!=null && hom.getLastTaxon()!=null) 
				ps.print("\t"+hom.getFirstTaxon()+"\t"+hom.getLastTaxon());
			ps.println();
		}
		ps.close();
	}
	
	public void writeEntropyFiles() throws IOException {
		
		List<ChainClusterDB> chainClusters = pdbInfo.getChainClusters();
		
		for (ChainClusterDB cc:chainClusters) {
			if (cc.isHasUniProtRef()) {
				writeEntropyFile(cc);
			}
		}

	}
	
	private void writeEntropyFile(ChainClusterDB chainCluster) throws IOException {
		
		
		PrintStream ps = new PrintStream(params.getOutputFile("."+chainCluster.getRepChain()+EppicParams.ENTROPIES_FILE_SUFFIX));

		String alphabet = pdbInfo.getRunParameters().getAlphabet();
		int numGroupsAlphabet = alphabet.split(":").length;
		
		ps.println("# Entropies for all observed residues of query sequence (reference UniProt: " +
				chainCluster.getRefUniProtId()+") based on a(n) " + numGroupsAlphabet + "-letter alphabet.");
		ps.println("# The maximum entropy value is " + Math.log(numGroupsAlphabet) / Math.log(2) + ".");
		ps.println("# seqres\tpdb\tuniprot\tpdb_res\tentropy");
 
		List<ResidueDB> list = getResidueListForChain(chainCluster);

				
		List<Sequence> sequences = new ArrayList<Sequence>(2);
		sequences.add(new Sequence("pdb",chainCluster.getPdbAlignedSeq()));
		sequences.add(new Sequence("uniprot",chainCluster.getRefAlignedSeq()));
		
		MultipleSequenceAlignment aln = null;
		try {
			aln = new MultipleSequenceAlignment(sequences);
		} catch (AlignmentConstructionException e) {
			LOGGER.warn("Could not create alignment from PDB aligned sequence and UniProt reference aligned sequence. Entropies file will contain no UniProt numbering, error: "+e.getMessage());			
		}
		
		for (int i=0;i<list.size();i++) {
			
			int uniprotSerial = 0;
			if (aln!=null) {
				int resser = list.get(i).getResidueNumber();
				if (resser==-1) continue; // most likely a het atom, we skip or otherwise al2seq below would fail as the hetatm would not be in alignment
				uniprotSerial = aln.al2seq("uniprot", aln.seq2al("pdb", resser));
			}
			
			ps.printf("%4d\t%4s\t%4d\t%3s\t%5.2f\n",
					list.get(i).getResidueNumber(), 
					list.get(i).getPdbResidueNumber(),  
					uniprotSerial, 
					list.get(i).getResidueType(), 
					list.get(i).getEntropyScore());
		}
		
		
		ps.close();
	}
	
	private List<ResidueDB> getResidueListForChain (ChainClusterDB chainCluster) {
		List<ResidueDB> list = new ArrayList<ResidueDB>();
		
		String repChain = chainCluster.getRepChain();
		
		cluster:
		for (InterfaceClusterDB interfaceCluster: pdbInfo.getInterfaceClusters()) {
			
			for (InterfaceDB interfaceItem:interfaceCluster.getInterfaces()) {
				int side = 0;
				if (interfaceItem.getChain1().equals(repChain)) {
					side = 1;
				} else if (interfaceItem.getChain2().equals(repChain)) {
					side = 2;
				} else {
					continue;
				}
				
				List<ResidueDB> residues = interfaceItem.getResidues();
				for (ResidueDB residue:residues) {
					if (residue.getSide()==side) list.add(residue);
				}
				break cluster;

			}
		}
		
		return list;
	}
	
	public void writeAlnFiles() throws IOException {
		
		List<ChainClusterDB> chainClusters = pdbInfo.getChainClusters();
		
		for (ChainClusterDB cc:chainClusters) {
			if (cc.isHasUniProtRef()) {
				writeAlnFile(cc);
			}
		}
		
	}
	
	private void writeAlnFile(ChainClusterDB chainCluster) throws IOException {
		PrintStream ps = new PrintStream(params.getOutputFile("."+chainCluster.getRepChain()+".aln"));
		
		
		int len = 80;

		// query sequence
		String seq = chainCluster.getMsaAlignedSeq();
		String name = chainCluster.getRefUniProtId()+"_"+chainCluster.getRefUniProtStart()+"-"+chainCluster.getRefUniProtEnd();
		ps.print('>' + name+"\n");
		for(int i=0; i<seq.length(); i+=len) {
			ps.print(seq.substring(i, Math.min(i+len,seq.length()))+"\n");
		}
		
		// homologs sequences
		for ( HomologDB hom:chainCluster.getHomologs() ) {
			
			seq = hom.getAlignedSeq();
			
			ps.print ('>' + hom.getUniProtId()+"_"+hom.getSubjectStart()+"-"+hom.getSubjectEnd() +"\n");
			
			for(int i=0; i<seq.length(); i+=len) {
				ps.print(seq.substring(i, Math.min(i+len,seq.length()))+"\n");
			}
		}
		

		ps.close();
	}
	
	public void writeContactsInfoFile() throws IOException {
		PrintStream ps = new PrintStream(params.getOutputFile(EppicParams.CONTACTS_FILE_SUFFIX));
		ps.println("Contacts per interface for input structure "+(params.isInputAFile()?params.getInFile().getName():params.getPdbCode()));
		ps.printf("Distance cut-off %5.2f\n",EppicParams.INTERFACE_DIST_CUTOFF);
		
		// NOTE the residue numbers for contacts are written ALWAYS with SEQRES residue serials 
		printContactsInfo(ps);
		ps.close();
	}
	
	private void printContactsInfo(PrintStream ps) {
		
		ps.println("# iRes\tiType\tiBurial\tjRes\tjType\tjBurial\tminDist\tnAtoms\tnHBonds\tdisulf\tclash");
		
		for (InterfaceClusterDB interfaceCluster: pdbInfo.getInterfaceClusters()) {
			for (InterfaceDB interfaceItem:interfaceCluster.getInterfaces()) {

				ps.print("# ");
				ps.printf("%d\t%9.2f\t%s\t%s\n",interfaceItem.getInterfaceId(),interfaceItem.getArea(), 
						interfaceItem.getChain1()+"+"+interfaceItem.getChain2(),interfaceItem.getOperator());
				
				
				for (ContactDB contact: interfaceItem.getContacts()) {
					ps.printf("%d\t%s\t%4.2f\t%d\t%s\t%4.2f\t%5.2f\t%d\t%d\t%3s\t%1s\n",
							contact.getFirstResNumber(),contact.getFirstResType(),contact.getFirstBurial(),
							contact.getSecondResNumber(),contact.getSecondResType(),contact.getSecondBurial(),
							contact.getMinDistance(),
							contact.getNumAtoms(),
							contact.getNumHBonds(),
							(contact.isDisulfide()?"S-S":""),
							(contact.isClash()?"x":""));			
				}
				
			}
		}
	}

	
}
