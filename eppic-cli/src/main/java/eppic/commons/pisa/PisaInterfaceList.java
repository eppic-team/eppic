package eppic.commons.pisa;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.contact.StructureInterfaceList;

public class PisaInterfaceList implements Iterable<PisaInterface> {

	private String pdbCode;
	private List<PisaInterface> list;
	
	public PisaInterfaceList() {
		list = new ArrayList<PisaInterface>();
	}
	
	public boolean add(PisaInterface interf) {
		return list.add(interf);
	}

	public List<PisaInterface> getList() {
		return list;
	}

	@Override
	public Iterator<PisaInterface> iterator() {
		return list.iterator();
	}
	
	public String getPdbCode() {
		return pdbCode; 
	}
	
	public void setPdbCode(String pdbCode) {
		this.pdbCode = pdbCode;
	}
	
	/**
	 * Gets the PISA interface identified by the given PISA interface id
	 * If no such id exists returns null.
	 * @param id
	 * @return
	 */
	public PisaInterface getById(int id) {
		for (PisaInterface pi:list) {
			if (pi.getId()==id) return pi;
		}
		return null;
	}
	
	/**
	 * Gets the PISA interface identified by the given prot-prot interface id, 
	 * that is the interface serial if only protein-protein interfaces are considered.
	 * @param protprotId
	 * @return
	 */
	public PisaInterface getByProtProtId(int protprotId) {
		for (PisaInterface pi:list) {
			if (pi.getProtProtId()==protprotId) return pi;
		}
		return null;		
	}
	
	/**
	 * Sets the protprotId members of the PisaInterfaces in this list
	 * @return
	 */
	protected void setProtProtIds() {
		int i = 1;
		for (PisaInterface pi:list) {
			if (pi.isProtein()) {
				pi.setProtProtId(i);
				i++;
			} else {
				pi.setProtProtId(-1);
			}
		}
	}
	
	/**
	 * Converts this list of Pisa interfaces to one of our ChainInterfaceList containing interfaces
	 * including coordinates appropriately transformed as indicated by transformations read from Pisa.
	 * @param pdb the PDB entry data corresponding to this PisaInterfaceList
	 * @return
	 * @throws IllegalArgumentException if the pdb code of given pdb does not match this PisaInterface's code
	 */
	public StructureInterfaceList convertToChainInterfaceList(Structure pdb) {
		if (!pdb.getPdbId().equals(this.pdbCode)) {
			throw new IllegalArgumentException("Pdb code of given PdbAsymUnit does not match pdb code from PISA");
		}
		StructureInterfaceList interfList = new StructureInterfaceList();
		for (PisaInterface interf:this){
			interfList.add(interf.convertToChainInterface(pdb));
		}
		return interfList;
	}
}
