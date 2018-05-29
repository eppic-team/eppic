package ch.systemsx.sybit.crkwebui.client.commons.services.eppic;

import java.util.HashMap;
import java.util.List;

import eppic.model.dto.ApplicationSettings;
import eppic.model.dto.Residue;
import eppic.model.dto.ResiduesList;
import eppic.model.dto.JobsForSession;
import eppic.model.dto.PDBSearchResult;
import eppic.model.dto.ProcessingData;
import eppic.model.dto.RunJobData;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The async counterpart of <code>CrkWebService</code>.
 * 
 * @author srebniak_a
 */
public interface CrkWebServiceAsync 
{
	public void loadSettings(AsyncCallback<ApplicationSettings> callback);

	public void runJob(RunJobData runJobData, AsyncCallback<String> callback);
	
	public void getResultsOfProcessing(String jobId, AsyncCallback<ProcessingData> callback);
	
	public void getJobsForCurrentSession(AsyncCallback<JobsForSession> callback);
	
	public void getInterfaceResidues(int interfaceUid,
									 AsyncCallback<HashMap<Integer, List<Residue>>> callback);
	
	public void stopJob(String jobToStop,
			AsyncCallback<String> stopJobsCallback);

	public void deleteJob(String jobToDelete,
						   AsyncCallback<String> deleteJobsCallback);

	public void untieJobsFromSession(AsyncCallback<Void> callback);

	public void getAllResidues(String jobId,
			AsyncCallback<ResiduesList> getAllResiduesCallback);
	
	public void getListOfPDBsHavingAUniProt(String uniProtId, AsyncCallback<List<PDBSearchResult>> callback);

	public void getListOfPDBs(String pdbCode, String chain, AsyncCallback<List<PDBSearchResult>> callback);
}
