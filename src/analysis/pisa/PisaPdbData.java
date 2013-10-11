/**
 * 
 */
package analysis.pisa;

import java.util.Map;
import java.util.TreeMap;

import crk.CRKParams;
import crk.CallType;

import owl.core.connections.pisa.PisaAsmSet;
import owl.core.connections.pisa.PisaAssembly;
import owl.core.connections.pisa.PisaInterface;
import owl.core.connections.pisa.PisaInterfaceList;
import owl.core.structure.ChainInterface;
import owl.core.structure.ChainInterfaceList;
import owl.core.structure.PdbAsymUnit;
import owl.core.structure.PdbBioUnit;

/**
 * Class to store the pisa data of a pdb entry
 * @author biyani_n
 *
 */
public class PisaPdbData {
	
	private PdbAsymUnit pdb;
	private PisaAsmSet assemblySet;
	private PisaInterfaceList pisaInterfaces;
	private Map<Integer, PisaInterface> eppicToPisaInterfaceMap;   //Map of eppic Id to Pisa Interface
	private Map<PisaInterface, CallType> pisaCalls;
	
	public PisaPdbData(PdbAsymUnit pdb){
		this.pdb = pdb;
		this.assemblySet = new PisaAsmSet();
		this.pisaInterfaces = new PisaInterfaceList();
		this.eppicToPisaInterfaceMap = new TreeMap<Integer, PisaInterface>();
		this.pisaCalls = new TreeMap<PisaInterface, CallType>();
	}
	
	public PisaPdbData(PdbAsymUnit pdb, PisaAsmSet assemblySet, PisaInterfaceList pisaInterfaceList){
		this.pdb = pdb;
		this.assemblySet = assemblySet;
		this.pisaInterfaces = pisaInterfaceList;
		setEppicToPisaMap();
		this.pisaCalls = setPisaCalls();
	}
	
	public String getPdbCode(){
		return this.pdb.getPdbCode();
	}
	
	public Map<Integer, PisaInterface> getEppicToPisaInterfaceMap() {
		return eppicToPisaInterfaceMap;
	}

	public void setEppicToPisaInterfaceMap(
			Map<Integer, PisaInterface> eppicToPisaInterface) {
		this.eppicToPisaInterfaceMap = eppicToPisaInterface;
	}

	public Map<PisaInterface, CallType> getPisaCalls() {
		return pisaCalls;
	}

	public void setPisaCalls(Map<PisaInterface, CallType> pisaCalls) {
		this.pisaCalls = pisaCalls;
	}

	private void setEppicToPisaMap(){
		pdb.removeHatoms();
		ChainInterfaceList eppicInterfaces = pdb.getAllInterfaces(CRKParams.INTERFACE_DIST_CUTOFF, 
				CRKParams.DEF_NSPHEREPOINTS_ASA_CALC, 1, true, false, 
				CRKParams.DEF_MIN_SIZE_COFACTOR_FOR_ASA,
				CRKParams.MIN_INTERFACE_AREA_TO_KEEP);
		
		this.eppicToPisaInterfaceMap = createEppicToPisaMap(eppicInterfaces, this.pisaInterfaces);
		
	}
	/**
	 * Method to get the PISA interface corresponding to EPPIC interface.
	 * Returns a map with EPPIC Interface Id's as the keys and PIsa interfaces as the values
	 * @param eppicInterfaces
	 * @param pisaInterfaces
	 * @return Map<Integer, PisaInterface> eppicIdToPisaInterfaceMap
	 */
	public static Map<Integer, PisaInterface> createEppicToPisaMap(ChainInterfaceList eppicInterfaces, PisaInterfaceList pisaInterfaces){
		//if(pisaInterfaces == null) return null;
		
		Map<Integer, PisaInterface> map = new TreeMap<Integer, PisaInterface>();
				
		for(ChainInterface eppicI:eppicInterfaces){
			for(PisaInterface pisaI:pisaInterfaces){
				PdbBioUnit pisaUnit = new PdbBioUnit();
				pisaUnit.addOperator(pisaI.getFirstMolecule().getChainId(), 
						pisaI.getFirstMolecule().getTransf().getMatTransform() );
				pisaUnit.addOperator(pisaI.getSecondMolecule().getChainId(), 
						pisaI.getSecondMolecule().getTransf().getMatTransform() );
				
				if(pisaUnit.matchesInterface(eppicI)) {
					map.put(eppicI.getId(), pisaI);
					break;
				}
			}
			if(!map.containsKey(eppicI)) System.err.println("No corresponding PISA interface found for Eppic Interface with id="+eppicI.getId());
		}
		
		return map;
	}
	
	private Map<PisaInterface, CallType> setPisaCalls(){
		Map<PisaInterface, CallType> map = new TreeMap<PisaInterface, CallType>();
		
		for(PisaInterface interf:this.pisaInterfaces){
			for(PisaAssembly assembly:this.assemblySet){
				if(assembly.getInterfaceIds().contains(interf.getId())){
					map.put(interf, CallType.BIO);
					break;
				}
			}
			if(!map.containsKey(interf)) map.put(interf, CallType.CRYSTAL);
		}
		
		return map;
	}
	
	public CallType getPisaCallFromEppicInterface(int eppicInterfaceId){
		
		if(!this.eppicToPisaInterfaceMap.containsKey(eppicInterfaceId)) return null;		
		else return(this.pisaCalls.get(this.eppicToPisaInterfaceMap.get(eppicInterfaceId)));
	}
	
	public int getEppicIdForPisaInterface(PisaInterface pisaInterface){
		int eppicId=-1;
		for(int eppicI:this.eppicToPisaInterfaceMap.keySet() ){
			if(this.eppicToPisaInterfaceMap.get(eppicI).getId() == pisaInterface.getId()){
				eppicId = eppicI;
				break;
			}
		}
		
		return eppicId;
	}
	
	public int getPisaIdForEppicInterface(int eppicInterfaceId){
		if(!this.eppicToPisaInterfaceMap.containsKey(eppicInterfaceId)) return -1;
		else return(this.eppicToPisaInterfaceMap.get(eppicInterfaceId).getId());
	}

}
