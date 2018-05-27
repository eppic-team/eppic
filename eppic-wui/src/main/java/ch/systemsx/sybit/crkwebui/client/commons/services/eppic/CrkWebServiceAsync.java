package ch.systemsx.sybit.crkwebui.client.commons.services.eppic;

import java.util.HashMap;
import java.util.List;

import eppic.dtomodel.ApplicationSettings;
import eppic.dtomodel.Residue;
import eppic.dtomodel.ResiduesList;
import eppic.dtomodel.JobsForSession;
import eppic.dtomodel.PDBSearchResult;
import eppic.dtomodel.ProcessingData;
import eppic.dtomodel.RunJobData;

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
