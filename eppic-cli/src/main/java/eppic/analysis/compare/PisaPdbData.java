/**
 * 
 */
package eppic.analysis.compare;

import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import eppic.commons.pisa.OligomericPrediction;
import eppic.commons.pisa.PisaAsmSetList;
import eppic.commons.pisa.PisaInterface;
import eppic.commons.pisa.PisaInterfaceList;
import eppic.commons.util.CallType;
import eppic.model.db.InterfaceDB;
import eppic.model.db.PdbInfoDB;

/**
 * Class to store the pisa data of a pdb entry
 * @author biyani_n
 *
 */
public class PisaPdbData {
	
	public static final int MIN_CRYSTAL_TRANSLATION_FOR_WARNING = 4;	
	public static final double SMALL_AREA_WARNING = 100;
	
	private PdbInfoDB pdbInfo;
	
	private PisaAsmSetList assemblySetList;
	private PisaInterfaceList pisaInterfaces;

	private Map<Integer, CallType> pisaCalls;						//Map of Pisa Interface Id's to pisa Calls
	
	private InterfaceMatcher matcher;
	
	public PisaPdbData(PdbInfoDB pdbInfo, PisaAsmSetList assemblySetList, PisaInterfaceList pisaInterfaceList, double minArea) {
		
		
		this.pdbInfo = pdbInfo;
		this.assemblySetList = assemblySetList;
		this.pisaInterfaces = pisaInterfaceList;
		
		matcher = new InterfaceMatcher(
					pdbInfo.getInterfaceClusters(),
					SimpleInterface.createSimpleInterfaceListFromPisaInterfaceList(pisaInterfaceList, minArea)); 

		this.pisaCalls = setPisaCalls();
	}
	
	public Map<Integer, CallType> getPisaCalls() {
		return pisaCalls;
	}
		
	private Map<Integer, CallType> setPisaCalls(){
		Map<Integer, CallType> map = new TreeMap<Integer, CallType>();

		OligomericPrediction op = this.assemblySetList.getOligomericPred(this.pisaInterfaces );
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
	
	public static void printHeaders(PrintStream out) {
		//Print Header
		out.printf("#%4s %8s %8s %8s\n","PDB","EPPIC_ID","PISA_ID","PisaCall");

	}
	
	public void printTabular (PrintStream out, PrintStream err) {
		
		if (!matcher.checkOneToOneMapping()) {
			
			err.printf("Failed to match pisa:eppic interfaces (not a 1:1 mapping): %5s "
					+ "(totals: %d our interfaces, %d their interfaces)\n", 
					pdbInfo.getPdbCode(), matcher.getNumOurInterfaces(), matcher.getNumTheirInterfaces());
			return;
		}


		for(int eppicInterfId:matcher.getOurIds()) {

			if (matcher.oursMatch(eppicInterfId)) {
				int pisaId = matcher.getTheirs(eppicInterfId).getId();
				CallType pisaCall = pisaCalls.get(pisaId);
				
				out.printf("%5s %8s %8s %8s\n", pdbInfo.getPdbCode(), eppicInterfId, pisaId, pisaCall.getName() );
				
			} else {
				printWarning(err, pdbInfo.getInterface(eppicInterfId));
			}
			
		}
		
		List<SimpleInterface> theirsNM = matcher.getTheirsNotMatching();
		for (SimpleInterface theirI:theirsNM) {
			String msgPrefix = "Failed to match pisa interface";
			
			if (theirI.getArea()<SMALL_AREA_WARNING) {
				msgPrefix += " (small area "+String.format("%5.2f",theirI.getArea())+")";
			}
			err.printf(msgPrefix + " : %5s %8s\n", pdbInfo.getPdbCode(), theirI.getId());
		}
	}
	
	private void printWarning(PrintStream err, InterfaceDB ourI) {
		
		// if not we check if the contacts are too far away and warn that it's impossible
		// to match their interfaces in these cases (PISA only calculates up to 3 neighbors)
		int xTrans = ourI.getXtalTrans_x();
		int yTrans = ourI.getXtalTrans_y();
		int zTrans = ourI.getXtalTrans_z();
		int maxTrans = Math.max(Math.max(xTrans, yTrans), zTrans);

		String msgPrefix = "Failed to match eppic interface";
		
		if(maxTrans >= MIN_CRYSTAL_TRANSLATION_FOR_WARNING){

			msgPrefix += " (max translation "+maxTrans+")";
			
		}
		
		if (ourI.getArea()<SMALL_AREA_WARNING) {
			msgPrefix += " (small area "+String.format("%5.2f",ourI.getArea())+")";
		}
		
		err.printf(msgPrefix + " : %5s %8s \n", pdbInfo.getPdbCode(), ourI.getInterfaceId());

	}

}
