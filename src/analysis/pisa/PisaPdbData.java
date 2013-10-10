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
	private Map<ChainInterface, PisaInterface> eppicToPisaInterfaceMap;
	private Map<PisaInterface, CallType> pisaCalls;
	
	public PisaPdbData(PdbAsymUnit pdb){
		this.pdb = pdb;
		this.assemblySet = new PisaAsmSet();
		this.pisaInterfaces = new PisaInterfaceList();
		this.eppicToPisaInterfaceMap = new TreeMap<ChainInterface, PisaInterface>();
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
	
	public Map<ChainInterface, PisaInterface> getEppicToPisaInterfaceMap() {
		return eppicToPisaInterfaceMap;
	}

	public void setEppicToPisaInterfaceMap(
			Map<ChainInterface, PisaInterface> eppicToPisaInterface) {
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
		
	public static Map<ChainInterface, PisaInterface> createEppicToPisaMap(ChainInterfaceList eppicInterfaces, PisaInterfaceList pisaInterfaces){
		//if(pisaInterfaces == null) return null;
		
		Map<ChainInterface, PisaInterface> map = new TreeMap<ChainInterface, PisaInterface>();
				
		for(ChainInterface eppicI:eppicInterfaces){
			for(PisaInterface pisaI:pisaInterfaces){
				PdbBioUnit pisaUnit = new PdbBioUnit();
				pisaUnit.addOperator(pisaI.getFirstMolecule().getChainId(), 
						pisaI.getFirstMolecule().getTransf().getMatTransform() );
				pisaUnit.addOperator(pisaI.getSecondMolecule().getChainId(), 
						pisaI.getSecondMolecule().getTransf().getMatTransform() );
				
				if(pisaUnit.matchesInterface(eppicI)) {
					map.put(eppicI, pisaI);
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
	
	public CallType getPisaCallFromEppicInterface(ChainInterface eppicInterface){
		
		if(!this.eppicToPisaInterfaceMap.containsKey(eppicInterface)) return null;		
		else return(this.pisaCalls.get(this.eppicToPisaInterfaceMap.get(eppicInterface)));
	}
	
	public int getEppicIdForPisaInterface(PisaInterface pisaInterface){
		int eppicId=-1;
		for(ChainInterface eppicI:this.eppicToPisaInterfaceMap.keySet() ){
			if(this.eppicToPisaInterfaceMap.get(eppicI).getId() == pisaInterface.getId()){
				eppicId = eppicI.getId();
				break;
			}
		}
		
		return eppicId;
	}
	
	public int getPisaIdForEppicInterface(ChainInterface eppicInterface){
		if(!this.eppicToPisaInterfaceMap.containsKey(eppicInterface)) return -1;
		else return(this.eppicToPisaInterfaceMap.get(eppicInterface).getId());
	}

}
