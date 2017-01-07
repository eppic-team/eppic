package eppic;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eppic.model.AssemblyContentDB;
import eppic.model.AssemblyDB;
import eppic.model.ChainClusterDB;
import eppic.model.ContactDB;
import eppic.model.HomologDB;
import eppic.model.InterfaceClusterDB;
import eppic.model.InterfaceClusterScoreDB;
import eppic.model.InterfaceDB;
import eppic.model.InterfaceScoreDB;
import eppic.model.PdbInfoDB;
import eppic.model.ResidueBurialDB;
import eppic.model.ResidueInfoDB;
import eppic.model.ScoringMethod;

public class TextOutputWriter {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TextOutputWriter.class);
	
	private PdbInfoDB pdbInfo;
	private EppicParams params;
	
	public TextOutputWriter (PdbInfoDB pdbInfo, EppicParams params) {
		this.pdbInfo = pdbInfo;
		this.params = params;
		
		LOGGER.debug("Is non-standard SG: {}", pdbInfo.isNonStandardSg());
		LOGGER.debug("Is non-standard coord frame convention: {}", pdbInfo.isNonStandardCoordFrameConvention());
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
				this.printInterfacesMolInfo(ps, interfaceItem, false, usePdbResSer);				
				
				ps.print("## ");
				this.printInterfacesMolInfo(ps, interfaceItem, true, usePdbResSer);
				
			}
		}
	}
	
	private void printInterfacesMolInfo(PrintStream ps, InterfaceDB interfaceItem, boolean side, boolean usePdbResSer) {
		
		List<ResidueBurialDB> cores = new ArrayList<ResidueBurialDB>();
		
		for (ResidueBurialDB residue:interfaceItem.getResidueBurials()) {
			if (residue.getSide()==side) {
				
				if (residue.getRegion()==ResidueBurialDB.CORE_GEOMETRY) cores.add(residue);
			}
		}
		
		String pdbChainCode = null;
		if (side==false) pdbChainCode = interfaceItem.getChain1();
		if (side==true)  pdbChainCode = interfaceItem.getChain2();
		
		ps.println(side+"\t"+pdbChainCode+"\tprotein");

		if (cores.size()>0) {
			ps.printf("## core (%4.2f): %s\n", params.getCAcutoffForGeom(), getResString(cores, usePdbResSer));
		}
		ps.println("## seqres pdb res asa bsa burial(percent)");

		for (ResidueBurialDB residue:interfaceItem.getResidueBurials()) {	
			if (residue.getSide()==side) {
				ResidueInfoDB residueInfo = residue.getResidueInfo();
				int resNum = 0;
				String pdbResNum = "0";
				String resType = "XXX";
				if (residueInfo!=null) {
					resNum = residueInfo.getResidueNumber();
					pdbResNum = residueInfo.getPdbResidueNumber();
					resType = residueInfo.getResidueType();
				}
				ps.printf("%d\t%s\t%s\t%6.2f\t%6.2f",
						resNum,
						pdbResNum,
						resType,
						residue.getAsa(),residue.getBsa());
				double percentBurial = 100.0*residue.getBsa()/residue.getAsa();
				if (percentBurial>0.1) {
					ps.printf("\t%5.1f\n",percentBurial);
				} else {
					ps.println();
				}
			}
		}
	}
	
	private String getResString(List<ResidueBurialDB> residues, boolean usePdbResSer) {
		String str = "";
		for (int i=0;i<residues.size();i++) {
			String serial = "0";
			if (residues.get(i).getResidueInfo()!=null) {
				if (usePdbResSer) {
					serial = residues.get(i).getResidueInfo().getPdbResidueNumber();
				} else {
					serial = ""+residues.get(i).getResidueInfo().getResidueNumber();
				}
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
			ps.printf("\t%7.2f\t%7.2f\t%7s",
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
			ps.printf("\t%7.2f\t%7.2f\t%7s",
					interfaceScoreFn.getScore(),
					interfaceScoreFn.getConfidence(),
					interfaceScoreFn.getCallName());			
		}
		
		
		// TODO should we display reasons? if the call does not come from score (hard areas, disulfides, etc) the call reason is useful
		// TODO should we display warnings? they are per InterfaceDB and not per InterfaceScoreDB
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
 

		List<ResidueInfoDB> residueInfos = chainCluster.getResidueInfos();
				
		for (int i=0;i<residueInfos.size();i++) {
			
			ResidueInfoDB residueInfo = residueInfos.get(i);
			
			if (residueInfo.getResidueNumber()<=0) continue;
			
			int resNum = 0;
			String pdbResNum = "0";
			int uniprotNum = 0;
			String resType = "XXX";
			double entropy = -1;
			if (residueInfo!=null) {
				resNum = residueInfo.getResidueNumber();
				pdbResNum = residueInfo.getPdbResidueNumber();
				uniprotNum = residueInfo.getUniProtNumber();
				resType = residueInfo.getResidueType();
				entropy = residueInfo.getEntropyScore();
			}
			
			
			ps.printf("%4d\t%4s\t%4d\t%3s\t%5.2f\n",
					resNum, 
					pdbResNum,  
					uniprotNum, 
					resType, 
					entropy);
		}
		
		
		ps.close();
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

	
	public void writeAssembliesFile() throws IOException {
		PrintStream ps = new PrintStream(params.getOutputFile(EppicParams.ASSEMBLIES_FILE_SUFFIX));
		ps.println("# Topologically valid assemblies in "+(params.isInputAFile()?params.getInFile().getName():params.getPdbCode()));		
		
		ps.printf("%3s %20s %10s %15s %15s %15s\n",
				"id",
				"Interf cluster ids",
				"Size",
				"Stoichiometry",
				"Symmetry",
				"Predicted by");
		
		boolean hasTopInvalidAssemblies = false;
		for (AssemblyDB assembly:pdbInfo.getAssemblies()) {
			if (assembly.isTopologicallyValid()) {
				printAssemblyInfo(ps, assembly);
			} else {
				hasTopInvalidAssemblies = true;
			}
		}
		
		if (hasTopInvalidAssemblies) {
			//ps.println("# ---------------------------------");
			ps.println("# Topologically invalid assemblies:");


			for (AssemblyDB assembly:pdbInfo.getAssemblies()) {
				if (!assembly.isTopologicallyValid()) {
					printAssemblyInfo(ps, assembly);
				}
			}
		}
		
		ps.close();
	}
	
	private void printAssemblyInfo(PrintStream ps, AssemblyDB assembly) {
		// first we gather the assembly predictions
		StringBuilder sb = new StringBuilder();
		for (int i=0;i<assembly.getAssemblyScores().size();i++) {
			
			if (assembly.getAssemblyScores().get(i).getCallName()!=null && 
				assembly.getAssemblyScores().get(i).getCallName().equals(CallType.BIO.getName())) {
				
				if (sb.length()>0) sb.append(','); // only add comma if there's already another method before
				
				sb.append(assembly.getAssemblyScores().get(i).getMethod());
			}
			
		}
		// now we print everything
		List<AssemblyContentDB> contents = assembly.getAssemblyContents();
		String mmSizeString = "";
		String stoString = "";
		String symString = "";
		if (contents!=null) {
			mmSizeString = DataModelAdaptor.getMmSizeString(contents);
			stoString = DataModelAdaptor.getStoichiometryString(contents);
			symString = DataModelAdaptor.getSymmetryString(contents);
		}
		ps.printf("%3d %20s %10s %15s %15s %15s\n",
				assembly.getId(),
				assembly.getInterfaceClusterIds(),
				mmSizeString,
				stoString,
				symString,
				sb.toString());
	}
		
}
