package ch.systemsx.sybit.crkwebui.shared.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

/**
 * Interface residues items list representation.
 * @author AS
 *
 */
public class InterfaceResiduesItemsList extends HashMap<Integer, HashMap<Integer, List<InterfaceResidueItem>>> implements Serializable 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public InterfaceResiduesItemsList()
	{
		
	}
}
