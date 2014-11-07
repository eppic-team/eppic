package eppic;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.math.random.RandomDataImpl;

import eppic.predictors.EvolCoreRimClusterPredictor;
import eppic.predictors.EvolCoreSurfaceClusterPredictor;
import eppic.predictors.EvolCoreSurfacePredictor;
import eppic.predictors.EvolCoreRimPredictor;
import owl.core.structure.ChainInterface;
import owl.core.structure.ChainInterfaceList;
import owl.core.structure.InterfaceCluster;
import owl.core.structure.Residue;

public class InterfaceEvolContextList implements Iterable<InterfaceEvolContext>, Serializable {

	private static final long serialVersionUID = 1L;
	
	private static final Log LOGGER = LogFactory.getLog(InterfaceEvolContextList.class);
	
	private List<InterfaceEvolContext> list;
	
	private ChainInterfaceList chainInterfList; 
	private ChainEvolContextList cecs;
		
	private int minNumSeqs;
	
	private boolean usePdbResSer;
	
	private Map<Integer,EvolCoreRimClusterPredictor> ecrcPredictors;
	private Map<Integer,EvolCoreSurfaceClusterPredictor> ecscPredictors;
	
	/**
	 * Constructs a InterfaceEvolContextList given a ChainInterfaceList with all 
	 * interfaces of a given PDB and a ChainEvolContextList with all evolutionary 
	 * contexts of chains of that same PDB. Adds an InterfaceEvolContext (containing a pair
	 * of ChainEvolContext and a ChainInterface) to this list for each protein-protein interface. 
	 * @param interfaces
	 * @param cecs
	 */
	public InterfaceEvolContextList(ChainInterfaceList interfaces, ChainEvolContextList cecs) {
		
		this.minNumSeqs = cecs.getMinNumSeqs();
		
		this.list = new ArrayList<InterfaceEvolContext>();
	
		this.chainInterfList = interfaces;
		this.cecs = cecs;

		this.ecrcPredictors = new TreeMap<Integer, EvolCoreRimClusterPredictor>();
		this.ecscPredictors = new TreeMap<Integer, EvolCoreSurfaceClusterPredictor>();
		
		
		for (ChainInterface pi:interfaces) {
			InterfaceEvolContext iec = new InterfaceEvolContext(pi, this);
			iec.setEvolCoreRimPredictor(new EvolCoreRimPredictor(iec));
			iec.setEvolCoreSurfacePredictor(new EvolCoreSurfacePredictor(iec));
			this.add(iec);
		}
		
		for (InterfaceCluster ic:interfaces.getClusters()) {
			List<EvolCoreRimPredictor> ecrMembers = new ArrayList<EvolCoreRimPredictor>();
			List<EvolCoreSurfacePredictor> ecsMembers = new ArrayList<EvolCoreSurfacePredictor>();
			for (int i=0;i<interfaces.size();i++) {
				if ( interfaces.getCluster(i+1).getId()==ic.getId()) {
					ecrMembers.add(list.get(i).getEvolCoreRimPredictor());
					ecsMembers.add(list.get(i).getEvolCoreSurfacePredictor());
				}
			}
			ecrcPredictors.put(ic.getId(), new EvolCoreRimClusterPredictor(ecrMembers));
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
	
	public void scoreCoreRim() {
		for (int i=0;i<list.size();i++) {
			list.get(i).getEvolCoreRimPredictor().computeScores();
		}
		for (EvolCoreRimClusterPredictor ecrcp:this.ecrcPredictors.values()) {
			ecrcp.computeScores();; 
		}
	}
	
	public void scoreCoreSurface() {
		for (int i=0;i<list.size();i++) {
			list.get(i).getEvolCoreSurfacePredictor().computeScores();
		}
		for (EvolCoreSurfaceClusterPredictor ecscp:this.ecscPredictors.values()) {
			ecscp.computeScores(); 
		}
	}
	
	public void setCoreRimScoreCutoff(double coreRimScoreCutoff) {
		for (int i=0;i<list.size();i++) {
			list.get(i).getEvolCoreRimPredictor().setCallCutoff(coreRimScoreCutoff);	
		}
		for (EvolCoreRimClusterPredictor ecrcp:this.ecrcPredictors.values()) {
			ecrcp.setCallCutoff(coreRimScoreCutoff); 
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

	public void setCoreRimPredBsaToAsaCutoff(double bsaToAsaCutoff, double minAsaForSurface) {
		chainInterfList.calcRimAndCores(bsaToAsaCutoff, minAsaForSurface);
		
		for (int i=0;i<list.size();i++) {
			list.get(i).getEvolCoreRimPredictor().setBsaToAsaCutoff(bsaToAsaCutoff, minAsaForSurface);
		}		
	}
	
	public void setCoreSurfacePredBsaToAsaCutoff(double bsaToAsaCutoff, double minAsaForSurface) {
		chainInterfList.calcRimAndCores(bsaToAsaCutoff, minAsaForSurface);
		
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
	
	public EvolCoreRimClusterPredictor getEvolCoreRimClusterPredictor(int clusterId) {
		return this.ecrcPredictors.get(clusterId);
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
	 * @param usePdbResSer if true PDB residue serials are used, if false CIF
	 * residue serials are used
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
	 * Given a PDB chain code returns the Set of residues that are in the surface but belong to NO
	 * interface (above given minInterfArea) 
	 * @param pdbChainCode
	 * @param minInterfArea
	 * @return
	 */
	public List<Residue> getResiduesNotInInterfaces(String pdbChainCode, double minInterfArea, double minAsaForSurface) {
		return this.chainInterfList.getResiduesNotInInterfaces(pdbChainCode, minInterfArea, minAsaForSurface);
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
	
	/**
	 * Returns the distribution of evolutionary scores of random subsets of residues in the surface (not belonging 
	 * to any interface above minInterfArea) for given pdbChainCode and scoType.
	 * @param pdbChainCode
	 * @param minInterfArea the residues considered will be those that are not in interfaces above this area value
	 * @param numSamples number of samples of size sampleSize to be taken from the surface
	 * @param sampleSize number of residues in each sample
	 * @param minAsaForSurface the minimum ASA for a residue to be considered surface
	 * @return
	 */
	public double[] getSurfaceScoreDist(String pdbChainCode, double minInterfArea, int numSamples, int sampleSize, double minAsaForSurface) {
		if (sampleSize==0) return new double[0];
		
		double[] dist = new double[numSamples];

		RandomDataImpl rd = new RandomDataImpl();
		for (int i=0;i<numSamples;i++) {
			Object[] sample = rd.nextSample(getResiduesNotInInterfaces(pdbChainCode, minInterfArea, minAsaForSurface), sampleSize);
			List<Residue> residues = new ArrayList<Residue>(sample.length);
			for (int j=0;j<sample.length;j++){
				residues.add((Residue)sample[j]);
			}
			// note that we must pass weighted=false as the weighting is done on bsas which doesn't make sense at all here 
			dist[i] = getChainEvolContext(pdbChainCode).calcScoreForResidueSet(residues, false);
			
			//Collections.sort(residues, new Comparator<Residue>() {
			//	public int compare(Residue o1, Residue o2) {
			//		if (o1.getSerial()<o2.getSerial()) return -1;
			//		if (o1.getSerial()>o2.getSerial()) return 1;
			//		return 0;
			//	}
			//});
			//System.out.print("draw "+i+": [");
			//for (Residue res:residues) {
			//	System.out.print(res.getSerial()+" ");
			//}
			//System.out.println("]");
		}		
		
		return dist;
	}
}
