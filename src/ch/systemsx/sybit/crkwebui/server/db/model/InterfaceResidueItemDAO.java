package ch.systemsx.sybit.crkwebui.server.db.model;

import java.util.List;

import ch.systemsx.sybit.crkwebui.shared.CrkWebException;
import ch.systemsx.sybit.crkwebui.shared.model.InterfaceResidueItem;
import ch.systemsx.sybit.crkwebui.shared.model.InterfaceResiduesItemsList;

public interface InterfaceResidueItemDAO {

	public List<InterfaceResidueItem> getResiduesForInterface(int interfaceUid) throws CrkWebException;
	
	public InterfaceResiduesItemsList getResiduesForAllInterfaces(int pdbScoreUid) throws CrkWebException;
}
