package ch.systemsx.sybit.crkwebui.server.db.model;

import model.PDBScoreItemDB;
import ch.systemsx.sybit.crkwebui.shared.CrkWebException;
import ch.systemsx.sybit.crkwebui.shared.model.PDBScoreItem;

public interface PDBScoreDAO 
{
	public PDBScoreItem getPDBScore(String jobId) throws CrkWebException;
	
	public void insertPDBScore(PDBScoreItemDB pdbScoreItem) throws CrkWebException;
}
