package eppic.commons.pisa;

import java.util.ArrayList;
import java.util.List;

/**
 * Our interpretation of PISA assembly predictions: represents a single oligomeric prediction
 * extracted from the PISA assemblies output.
 * 
 * Note that each OligomericPrediction can be composed of one or more 
 * equivalent PisaAssemblies (because of equivalent NCS related components) 
 *  
 * @author duarte_j
 *
 */
public class OligomericPrediction {

	private int mmSize;
	private List<PisaAssembly> assemblies;
	
	/**
	 * Creates a new OligomericPrediction object passing the macromolecular size:
	 * 1 for monomer, 2 for dimer, etc
	 * If PISA prediction is gray then we use mmSize -1
	 * @param mmSize
	 */
	public OligomericPrediction(int mmSize) {
		this.mmSize = mmSize;
		assemblies = new ArrayList<PisaAssembly>();
	}
	
	/**
	 * Returns the macromolecular size of this OligomericPrediction:
	 * 1 for monomer, 2 for dimer etc.
	 * If value returned is -1, it means that PISA prediction was in the GRAY area.
	 * @return
	 */
	public int getMmSize() {
		return mmSize;
	}
	
	public void setMmSize(int mmSize) {
		this.mmSize = mmSize;
	}
	
	public void addAssembly(PisaAssembly assembly) {
		
		this.assemblies.add(assembly);
	}
	
	public List<PisaAssembly> getAssemblies() {
		return assemblies;
	}
	
	/**
	 * Returns the PISA interface ids of all engaged protein-protein interfaces of this 
	 * PISA oligomeric prediction.
	 * @return
	 */
	public List<Integer> getProtInterfacesIds(PisaInterfaceList pil) {
		List<Integer> ids = new ArrayList<Integer>();
		for (PisaAssembly ass:assemblies) {
			for (int id:ass.getInterfaceIds()) {
				if (pil.getById(id).isProtein()) ids.add(id);
			}
		}
		return ids;
	}
	
	/**
	 * Returns true if given PISA interface id is one of the interfaces engaged in the 
	 * assemblies represented by this prediction
	 * @param id
	 * @return
	 */
	public boolean containProtInterface(int id) {
		for (PisaAssembly ass:assemblies) {
			for (int assid:ass.getInterfaceIds()) {
				if (assid==id) return true;
			}
		}
		return false;
		
	}
	
}
