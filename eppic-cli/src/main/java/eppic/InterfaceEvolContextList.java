package eppic;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.biojava.nbio.structure.contact.StructureInterface;
import org.biojava.nbio.structure.contact.StructureInterfaceCluster;
import org.biojava.nbio.structure.contact.StructureInterfaceList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eppic.predictors.CombinedClusterPredictor;
import eppic.predictors.EvolCoreSurfaceClusterPredictor;
import eppic.predictors.EvolCoreSurfacePredictor;

public class InterfaceEvolContextList implements Iterable<InterfaceEvolContext>, Serializable {

	private static final long serialVersionUID = 1L;

	private static final Logger LOGGER = LoggerFactory.getLogger(InterfaceEvolContextList.class);
	
	private final List<InterfaceEvolContext> list;
	
	//private StructureInterfaceList chainInterfList; 
	private final ChainEvolContextList cecs;
		
	private int minNumSeqs;
	
	private boolean usePdbResSer;
	
	private Map<Integer,EvolCoreSurfaceClusterPredictor> ecscPredictors;
	private Map<Integer,CombinedClusterPredictor> ccPredictors;
	
	/**
	 * Constructs a InterfaceEvolContextList given a ChainInterfaceList with all 
	 * interfaces of a given PDB and a ChainEvolContextList with all evolutionary 
	 * contexts of chains of that same PDB. Adds an InterfaceEvolContext (containing a pair
	 * of ChainEvolContext and a ChainInterface) to this list for each protein-protein interface. 
	 * @param interfaces
	 * @param cecs
	 */
	public InterfaceEvolContextList(StructureInterfaceList interfaces, ChainEvolContextList cecs) {
		
		this.minNumSeqs = cecs.getMinNumSeqs();
		
		this.list = new ArrayList<>();
	
		//this.chainInterfList = interfaces;
		this.cecs = cecs;

		this.ecscPredictors = new TreeMap<>();
		this.ccPredictors = new TreeMap<>();
		
		
		for (StructureInterface pi:interfaces) {
			InterfaceEvolContext iec = new InterfaceEvolContext(pi, this);
			iec.setEvolCoreSurfacePredictor(new EvolCoreSurfacePredictor(iec));
			this.add(iec);
		}
		
		for (StructureInterfaceCluster ic:interfaces.getClusters(EppicParams.CLUSTERING_CONTACT_OVERLAP_SCORE_CUTOFF)) {
			List<EvolCoreSurfacePredictor> ecsMembers = new ArrayList<>();
			for (int i=0;i<interfaces.size();i++) {
				if ( interfaces.get(i+1).getCluster().getId()==ic.getId()) {
					ecsMembers.add(list.get(i).getEvolCoreSurfacePredictor());
				}
			}
			ecscPredictors.put(ic.getId(), new EvolCoreSurfaceClusterPredictor(ecsMembers));
		}
		
	}
	
	public int size() {
		return list.size();
	}
	
	public InterfaceEvolContext get(int i){
		return this.list.get(i);
	}
	
	private void add(InterfaceEvolContext iec) {
		list.add(iec);
	}
	
	@Override
	public Iterator<InterfaceEvolContext> iterator() {
		return list.iterator();
	}
	
	public void scoreCoreSurface() {
		for (int i=0;i<list.size();i++) {
			list.get(i).getEvolCoreSurfacePredictor().computeScores();
		}
		for (EvolCoreSurfaceClusterPredictor ecscp:this.ecscPredictors.values()) {
			ecscp.computeScores(); 
		}
	}
	
	public void setCoreSurfScoreCutoff(double coreSurfScoreCutoff) {
		for (int i=0;i<list.size();i++) {
			list.get(i).getEvolCoreSurfacePredictor().setCallCutoff(coreSurfScoreCutoff);
		}
		for (EvolCoreSurfaceClusterPredictor ecscp:this.ecscPredictors.values()) {
			ecscp.setCallCutoff(coreSurfScoreCutoff); 
		}
	}

	public void setCoreSurfacePredBsaToAsaCutoff(double bsaToAsaCutoff, double minAsaForSurface) {
		
		for (int i=0;i<list.size();i++) {
			list.get(i).getEvolCoreSurfacePredictor().setBsaToAsaCutoff(bsaToAsaCutoff, minAsaForSurface);
		}		
	}
	
	public void setCoreSurfaceScoreStrategy(int coreSurfaceScoreStrategy) {
		LOGGER.info("Using core surface score strategy: "+coreSurfaceScoreStrategy);
		for (int i=0;i<list.size();i++) {
			list.get(i).getEvolCoreSurfacePredictor().setCoreSurfaceScoreStrategy(coreSurfaceScoreStrategy);
		}				
	}
	
	public EvolCoreSurfaceClusterPredictor getEvolCoreSurfaceClusterPredictor(int clusterId) {
		return this.ecscPredictors.get(clusterId);
	}
	
	public int getMinNumSeqs() {
		return this.minNumSeqs;
	}
	
	public void setMinNumSeqs(int minNumSeqs) {
		this.minNumSeqs = minNumSeqs;
	}

	/**
	 * Whether the output warnings and PDB files are to be written with
	 * PDB residue serials or CIF (SEQRES) residue serials
	 */
	public boolean isUsePdbResSer() {
		return usePdbResSer;
	}
	
	/**
	 * Sets whether the output warnings and PDB files are to be written with
	 * PDB residue serials or CIF (SEQRES) residue serials
	 * @param usePdbResSer if true PDB residue serials are used, if false CIF
	 * residue serials are used
	 */
	public void setUsePdbResSer(boolean usePdbResSer) {
		this.usePdbResSer = usePdbResSer;
	}
	
	public int getHomologsCutoff() {
		return minNumSeqs;
	}

	public ChainEvolContextList getChainEvolContextList() {
		return cecs;	
	}
	
	/**
	 * Gets the ChainEvolContext corresponding to the given PDB chain code (can be 
	 * any PDB chain code, representative or not)
	 * 
	 * @param pdbChainCode
	 * @return
	 */
	public ChainEvolContext getChainEvolContext(String pdbChainCode) {
		return getChainEvolContextList().getChainEvolContext(pdbChainCode);
	}
	
	public void setCombinedClusterPredictor(int clusterId, CombinedClusterPredictor ccp) {
		ccPredictors.put(clusterId, ccp);
	}
	
	public CombinedClusterPredictor getCombinedClusterPredictor(int clusterId) {
		return ccPredictors.get(clusterId);
	}

}
