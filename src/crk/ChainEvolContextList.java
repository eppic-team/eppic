package crk;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import owl.core.sequence.UniprotEntry;
import owl.core.structure.PdbAsymUnit;
import owl.core.structure.PdbChain;


public class ChainEvolContextList implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final Log LOGGER = LogFactory.getLog(ChainEvolContextList.class);
	
	private TreeMap<String, ChainEvolContext> cecs; // one per representative chain
	private HashMap<String,String> chain2repChain; // pdb chain codes to pdb chain codes of representative chain
	
	private String pdbName;
	
	public ChainEvolContextList(PdbAsymUnit pdb, String pdbName) {
		this.cecs = new TreeMap<String, ChainEvolContext>();
		this.chain2repChain = pdb.getChain2repChainMap();
		this.pdbName = pdbName;
		
		for (String representativeChain:pdb.getAllRepChains()) {
						
			PdbChain chain = pdb.getChain(representativeChain);
			
			if (!chain.getSequence().isProtein()) {
				LOGGER.warn("Representative chain "+representativeChain+" does not seem to be a protein chain. Won't analyse it.");
				continue;
			}

			ChainEvolContext cec = new ChainEvolContext(chain.getSequenceMSEtoMET(), representativeChain, pdb.getPdbCode(), pdbName);
			cec.setSeqIdenticalChainsStr(pdb.getSeqIdenticalGroupString(representativeChain));
			
			cecs.put(representativeChain, cec);
		}
	}
	
	public void addChainEvolContext(String representativeChain, ChainEvolContext cec) {
		this.cecs.put(representativeChain, cec);
	}
	
	/**
	 * Gets the ChainEvolContext corresponding to the given PDB chain code (can be 
	 * any chain code, representative or not)
	 * @param pdbChainCode
	 * @return
	 */
	public ChainEvolContext getChainEvolContext(String pdbChainCode) {
		return cecs.get(chain2repChain.get(pdbChainCode));
	}
	
	/**
	 * Gets the Collection of all ChainEvolContext (sorted alphabetically by representative pdb chain code) 
	 * @return
	 */
	public Collection<ChainEvolContext> getAllChainEvolContext() {
		return cecs.values();
	}
	
	public String getPdbName() {
		return pdbName;
	}
	
	public void setPdbName(String pdbName) {
		this.pdbName = pdbName;
	}
	
	public List<String> getNumHomologsStrings(ScoringType scoType) {
		List<String> list = new ArrayList<String>(); 
		for (String repChain:cecs.keySet()) {
			ChainEvolContext cec = cecs.get(repChain);
			UniprotEntry uni = cec.getQuery();
			int numHomologs = -1;
			if (scoType==ScoringType.ENTROPY || scoType==ScoringType.ZSCORE) {
				numHomologs = cec.getNumHomologs();
			} 
//			else if (scoType==ScoringType.KAKS) {
//				numHomologs = cec.getNumHomologsWithValidCDS();
//			}
			String urlStr = "";
			if (uni!=null) urlStr = " (<a href=\""+uni.getUniprotUrl()+"\" target=\"_blank\">"+uni.getUniId()+"</a>)";
			list.add(cec.getSeqIndenticalChainStr()+urlStr+": "+numHomologs+" homologs");
		}
		return list;
	}
	
}
