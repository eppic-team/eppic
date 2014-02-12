/**
 * 
 */
package eppic.analysis.pisa;

import java.util.Map;
import java.util.TreeMap;

import eppic.EppicParams;
import eppic.CallType;
import owl.core.connections.pisa.PisaAsmSetList;
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
	private PisaAsmSetList assemblySetList;
	private PisaInterfaceList pisaInterfaces;
	private Map<Integer, PisaInterface> eppicToPisaInterfaceMap;   //Map of eppic Id to Pisa Interface
	private Map<Integer, CallType> pisaCalls;						//Map of Pisa Interface Id's to pisa Calls
	
	public PisaPdbData(PdbAsymUnit pdb, PisaAsmSetList assemblySetList, PisaInterfaceList pisaInterfaceList){
		this.pdb = pdb;
		this.assemblySetList = assemblySetList;
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

	public Map<Integer, CallType> getPisaCalls() {
		return pisaCalls;
	}

	public void setPisaCalls(Map<Integer, CallType> pisaCalls) {
		this.pisaCalls = pisaCalls;
	}

	private void setEppicToPisaMap(){
		pdb.removeHatoms();
		ChainInterfaceList eppicInterfaces = pdb.getAllInterfaces(EppicParams.INTERFACE_DIST_CUTOFF, 
				EppicParams.DEF_NSPHEREPOINTS_ASA_CALC, 1, true, false, 
				EppicParams.DEF_MIN_SIZE_COFACTOR_FOR_ASA,
				EppicParams.MIN_INTERFACE_AREA_TO_KEEP);
		
		this.eppicToPisaInterfaceMap = createEppicToPisaMap(eppicInterfaces, this.pisaInterfaces);
		
	}
	/**
	 * Method to get the PISA interface corresponding to EPPIC interface.
	 * Returns a map with EPPIC Interface Id's as the keys and PIsa interfaces as the values
	 * If no Pisa interface is found, sets the value of that key to null
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
			if(!map.containsKey(eppicI.getId())){
				int xTrans = Math.abs(eppicI.getSecondTransf().getCrystalTranslation().x);
				int yTrans = Math.abs(eppicI.getSecondTransf().getCrystalTranslation().y);
				int zTrans = Math.abs(eppicI.getSecondTransf().getCrystalTranslation().z);
				int maxTrans = Math.max(Math.max(xTrans, yTrans), zTrans);
				if(maxTrans > 3){
					System.err.println("Warning: EPPIC interface with Id="+eppicI.getId()+" for pdb: "+pisaInterfaces.getPdbCode()+" has a maximum translation of "+maxTrans+" cells; No PISA interface should be expected");
				}
				else System.err.println("Warning: No corresponding PISA interface found for EPPIC Interface with id="+eppicI.getId()+" for pdb: "+pisaInterfaces.getPdbCode());
				map.put(eppicI.getId(), null);
			}
		}
		return map;
	}
	
	private Map<Integer, CallType> setPisaCalls(){
		Map<Integer, CallType> map = new TreeMap<Integer, CallType>();

		int assemSize = this.assemblySetList.getOligomericPred().getMmSize();

		if(assemSize == -1){
			for(PisaInterface interf:this.pisaInterfaces){
				map.put(interf.getId(), CallType.NO_PREDICTION);
			}
		}
		else{
			for(PisaInterface interf:this.pisaInterfaces){
				for(PisaAssembly assembly:this.assemblySetList.getOligomericPred().getPisaAsmSet()){
					if(assembly.getInterfaceIds().contains(interf.getId())){
						map.put(interf.getId(), CallType.BIO);
						break;
					}
				}
				if(!map.containsKey(interf.getId())) map.put(interf.getId(), CallType.CRYSTAL);
			}
		}
		return map;
	}
	
	/**
	 * Returns the Call Type from the eppic Id. 
	 * If there is no pisa interface found for a eppic id "NO PREDICTION" is returned
	 * @param eppicInterfaceId
	 * @return
	 */
	public CallType getPisaCallFromEppicInterface(int eppicInterfaceId){
		
		if(!this.eppicToPisaInterfaceMap.containsKey(eppicInterfaceId)) return null;
		else if(this.eppicToPisaInterfaceMap.get(eppicInterfaceId)==null) return CallType.NO_PREDICTION;
		else return(this.pisaCalls.get(this.eppicToPisaInterfaceMap.get(eppicInterfaceId).getId()));
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
	
	/**
	 * Returns the pisa id for an eppic interface, returns 0 if no pisa interface is found corresponding to eepic interface
	 * @param eppicInterfaceId
	 * @return
	 */
	public int getPisaIdForEppicInterface(int eppicInterfaceId){
		if(!this.eppicToPisaInterfaceMap.containsKey(eppicInterfaceId)) return -1;
		else if(this.eppicToPisaInterfaceMap.get(eppicInterfaceId)==null) return 0;
		else return(this.eppicToPisaInterfaceMap.get(eppicInterfaceId).getId());
	}

}
