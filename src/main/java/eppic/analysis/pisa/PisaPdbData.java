/**
 * 
 */
package eppic.analysis.pisa;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import eppic.CallType;
import eppic.model.PdbInfoDB;
import owl.core.connections.pisa.OligomericPrediction;
import owl.core.connections.pisa.PisaAsmSetList;
import owl.core.connections.pisa.PisaInterface;
import owl.core.connections.pisa.PisaInterfaceList;

/**
 * Class to store the pisa data of a pdb entry
 * @author biyani_n
 *
 */
public class PisaPdbData {
	
	private String pdbCode;
	
	private PisaAsmSetList assemblySetList;
	private PisaInterfaceList pisaInterfaces;

	private Map<Integer, CallType> pisaCalls;						//Map of Pisa Interface Id's to pisa Calls
	
	private InterfaceMatcher matcher;
	
	public PisaPdbData(PdbInfoDB pdbInfo, PisaAsmSetList assemblySetList, PisaInterfaceList pisaInterfaceList, double minArea) 
	 throws OneToManyMatchException {
		
		
		this.pdbCode = pdbInfo.getPdbCode();
		this.assemblySetList = assemblySetList;
		this.pisaInterfaces = pisaInterfaceList;
		
		matcher = new InterfaceMatcher(
					pdbInfo.getInterfaceClusters(),
					SimpleInterface.createSimpleInterfaceListFromPisaInterfaceList(pisaInterfaceList, minArea)); 
		matcher.checkAllMatch(true);

		this.pisaCalls = setPisaCalls();
	}
	
	public String getPdbCode() {
		return pdbCode;
	}
	
	public Map<Integer, CallType> getPisaCalls() {
		return pisaCalls;
	}

	public Collection<Integer> getEppicInterfaceIds() {
		return matcher.getOurIds();
	}

	
	private Map<Integer, CallType> setPisaCalls(){
		Map<Integer, CallType> map = new TreeMap<Integer, CallType>();

		OligomericPrediction op = this.assemblySetList.getOligomericPred();
		int assemSize = op.getMmSize();

		if(assemSize == -1){
			for(PisaInterface interf:this.pisaInterfaces){
				map.put(interf.getId(), CallType.NO_PREDICTION);
			}
		}
		else{
			for(PisaInterface interf:this.pisaInterfaces){
				if (op.containProtInterface(interf.getId())) {
					map.put(interf.getId(), CallType.BIO);
				} else {
					map.put(interf.getId(), CallType.CRYSTAL);
				}				
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
		if (!matcher.hasMatch(eppicInterfaceId)) return CallType.NO_PREDICTION;
		
		return (pisaCalls.get(matcher.getTheirs(eppicInterfaceId).getId()));
	}
	
	/**
	 * Returns the pisa id for an eppic interface, returns -1 if no pisa interface is found corresponding to eppic interface
	 * @param eppicInterfaceId
	 * @return
	 */
	public int getPisaIdForEppicInterface(int eppicInterfaceId){
		if (!matcher.hasMatch(eppicInterfaceId)) return -1;
		
		return (matcher.getTheirs(eppicInterfaceId).getId());
	}

}
