package eppic.model.dto;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

/**
 * Interface residues items list representation.
 * @author AS
 *
 */
public class ResiduesList extends HashMap<Integer, HashMap<Integer, List<Residue>>> implements Serializable 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ResiduesList()
	{
		
	}
}
