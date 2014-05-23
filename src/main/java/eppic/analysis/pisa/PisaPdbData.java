/**
 * 
 */
package eppic.analysis.pisa;

import java.util.Map;
import java.util.TreeMap;

import javax.vecmath.Matrix4d;

import eppic.CallType;
import eppic.model.InterfaceClusterDB;
import eppic.model.InterfaceDB;
import eppic.model.PdbInfoDB;
import owl.core.connections.pisa.OligomericPrediction;
import owl.core.connections.pisa.PisaAsmSetList;
import owl.core.connections.pisa.PisaInterface;
import owl.core.connections.pisa.PisaInterfaceList;
import owl.core.structure.SpaceGroup;

/**
 * Class to store the pisa data of a pdb entry
 * @author biyani_n
 *
 */
public class PisaPdbData {
	
	private String pdbCode;
	private PdbInfoDB pdbInfo;
	private PisaAsmSetList assemblySetList;
	private PisaInterfaceList pisaInterfaces;
	private Map<Integer, PisaInterface> eppicToPisaInterfaceMap;   //Map of eppic Id to Pisa Interface
	private Map<Integer, CallType> pisaCalls;						//Map of Pisa Interface Id's to pisa Calls
	
	public PisaPdbData(PdbInfoDB pdbInfo, PisaAsmSetList assemblySetList, PisaInterfaceList pisaInterfaceList){
		
		this.pdbInfo = pdbInfo;
		this.pdbCode = pdbInfo.getPdbCode();
		this.assemblySetList = assemblySetList;
		this.pisaInterfaces = pisaInterfaceList;
		this.eppicToPisaInterfaceMap = createEppicToPisaMap();
		this.pisaCalls = setPisaCalls();
	}
	
	public String getPdbCode() {
		return pdbCode;
	}
	
	public Map<Integer, PisaInterface> getEppicToPisaInterfaceMap() {
		return eppicToPisaInterfaceMap;
	}

	public Map<Integer, CallType> getPisaCalls() {
		return pisaCalls;
	}

	/**
	 * Method to get the PISA interface corresponding to EPPIC interface.
	 * Returns a map with EPPIC Interface Id's as the keys and PIsa interfaces as the values
	 * If no Pisa interface is found, sets the value of that key to null
	 * @return Map<Integer, PisaInterface> eppicIdToPisaInterfaceMap
	 */
	public Map<Integer, PisaInterface> createEppicToPisaMap(){
		
		Map<Integer, PisaInterface> map = new TreeMap<Integer, PisaInterface>();
				
		for (InterfaceClusterDB ic:pdbInfo.getInterfaceClusters()) {
			for (InterfaceDB eppicI:ic.getInterfaces()) {
				PisaInterface pisaI = getMatchingInterface(eppicI);
				
				if (pisaI!=null) {
				
					map.put(eppicI.getInterfaceId(), pisaI);

				} else {
					// if not we check if the contacts are too far away and warn that it's impossible
					// to match PISA interfaces in these cases (PISA only calculates up to 3 neighbors)
					int xTrans = eppicI.getXtalTrans_x();
					int yTrans = eppicI.getXtalTrans_y();
					int zTrans = eppicI.getXtalTrans_z();
					int maxTrans = Math.max(Math.max(xTrans, yTrans), zTrans);
					if(maxTrans > 3){
						System.err.println("Warning: EPPIC interface id "+eppicI.getInterfaceId()+" (pdb: "+pdbCode+") has a maximum translation of "+maxTrans+" cells. No matching PISA interface should be expected");
					}
					else {
						System.err.println("Warning: no matching PISA interface found for EPPIC interface id "+eppicI.getInterfaceId()+" (pdb: "+pdbCode+")");
					}
					map.put(eppicI.getInterfaceId(), null);
				}
			}
		}
		
		// last sanity check: are all PISA interfaces matched?		
		for (PisaInterface pi: pisaInterfaces) {
			if (pi.getInterfaceArea()>50 && pi.isProtein() && !map.values().contains(pi)) {
				System.err.println("Warning: PISA interface id "+pi.getId()+" was not mapped to any EPPIC interface (pdb "+pdbCode+")");	
			}
		}
		
		
		return map;
	}
	
	/**
	 * Returns the corresponding PisaInterface to the given EPPIC interface, or null if no match found
	 * @param eppicI
	 * @return
	 */
	private PisaInterface getMatchingInterface(InterfaceDB eppicI) {

		PisaInterface match = null;
		
		for(PisaInterface pisaI:pisaInterfaces){
			
			if (areMatching(pisaI,eppicI)) {
				if (match!=null) {
					System.err.println("Warning: more than one matching PISA interface (id "+pisaI.getId()+
							") found for EPPIC interface id "+eppicI.getInterfaceId()+" (pdb "+pdbCode+"). "
									+ "Will only use first PISA interface found (id "+match.getId()+")");
				} else {
					match = pisaI;
				}
			}
			
		}

		return match;
	}
	
	private boolean areMatching(PisaInterface pisaI, InterfaceDB eppicI) {
		String eppicChain1 = eppicI.getChain1();
		String eppicChain2 = eppicI.getChain2();
		
		String pisaChain1 = pisaI.getFirstMolecule().getChainId();
		String pisaChain2 = pisaI.getSecondMolecule().getChainId();
		
		boolean invertedChains = false;
		if (pisaChain1.equals(eppicChain1) && pisaChain2.equals(eppicChain2)) {
			invertedChains = false;
		}
		else if (pisaChain1.equals(eppicChain2) && pisaChain2.equals(eppicChain1)) {
			invertedChains = true;
		}
		else {
			// chains don't match: this can't be the same interface
			return false;
		}
		
		// eppic always has 1st chain with identity, thus the transf12 coincides with transf2
		Matrix4d eppicTransf12 = SpaceGroup.getMatrixFromAlgebraic(eppicI.getOperator());

		Matrix4d pisaTransf1 = pisaI.getFirstMolecule().getTransf().getMatTransform();
		Matrix4d pisaTransf2 = pisaI.getSecondMolecule().getTransf().getMatTransform();
		// in case pisa does not have the first chain on identity, we first find the transf12
		// T12 = T02 * T01_inv
		Matrix4d pisaTransf1inv = new Matrix4d();
		pisaTransf1inv.invert(pisaTransf1);
		Matrix4d pisaTransf12 = new Matrix4d();
		pisaTransf12.mul(pisaTransf2, pisaTransf1inv);

		if (invertedChains) {
			pisaTransf12.invert();
		}

		
		// now we can compare the two transf12
		// a) first direct
		if (eppicTransf12.epsilonEquals(pisaTransf12, 0.00001)) {
			return true;
		}
		// b) then we need to check the inverse if the two chains are the same
		if (eppicChain1.equals(eppicChain2)) {
			pisaTransf12.invert();
			if (eppicTransf12.epsilonEquals(pisaTransf12, 0.00001)) {
				//System.err.println("Warning: inverse transform matches (EPPIC interface id "+eppicI.getInterfaceId()+", PISA interface id "+pisaI.getId()+", pdb "+pdbCode+")");
				return true;
			}
		}
		return false;
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
