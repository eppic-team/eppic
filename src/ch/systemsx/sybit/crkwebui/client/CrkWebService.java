package ch.systemsx.sybit.crkwebui.client;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import model.InterfaceResidueItem;
import model.PDBScoreItem;
import model.ProcessingData;
import ch.systemsx.sybit.crkwebui.shared.model.ApplicationSettings;
import ch.systemsx.sybit.crkwebui.shared.model.RunJobData;
import ch.systemsx.sybit.crkwebui.shared.model.ProcessingInProgressData;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("crk")
public interface CrkWebService extends RemoteService {
	public String greetServer(String name) throws IllegalArgumentException;

	public String test(String test);

	public ProcessingData getResultsOfProcessing(String id) throws Exception;

	public String killJob(String id);

	public List<ProcessingInProgressData> getJobsForCurrentSession();

	public String untieJobsFromSession();

	public ApplicationSettings getSettings() throws Exception;

	public void runJob(RunJobData runJobData);

	public HashMap<Integer, List<InterfaceResidueItem>> getInterfaceResidues(
			String jobId, int interfaceId);

}
