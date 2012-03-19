package ch.systemsx.sybit.crkwebui.server.db.model;

import java.util.List;

import ch.systemsx.sybit.crkwebui.shared.CrkWebException;
import ch.systemsx.sybit.crkwebui.shared.model.InterfaceResidueItem;
import ch.systemsx.sybit.crkwebui.shared.model.InterfaceResiduesItemsList;

/**
 * DAO for InterfaceResidue item.
 * @author AS
 *
 */
public interface InterfaceResidueItemDAO {

	/**
	 * Retrieves list of interface residue items for specified interface.
	 * @param interfaceUid uid of interface item
	 * @return list of interface residue items for specified interface
	 * @throws CrkWebException
	 */
	public List<InterfaceResidueItem> getResiduesForInterface(int interfaceUid) throws CrkWebException;
	
	/**
	 * Retrieves list of interface residue items for all interfaces.
	 * @param pdbScoreUid uid of pdb score item
	 * @return list of interface residue items for all interfaces
	 * @throws CrkWebException
	 */
	public InterfaceResiduesItemsList getResiduesForAllInterfaces(int pdbScoreUid) throws CrkWebException;
}
