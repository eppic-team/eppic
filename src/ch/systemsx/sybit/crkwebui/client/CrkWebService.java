package ch.systemsx.sybit.crkwebui.client;

import java.util.List;

import ch.systemsx.sybit.crkwebui.client.data.ResultsData;
import ch.systemsx.sybit.crkwebui.client.data.StatusData;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("crk")
public interface CrkWebService extends RemoteService 
{
	public String greetServer(String name) throws IllegalArgumentException;
	
	public String test(String test); 
	
	public boolean checkIfDataProcessed(String id);
	
	public StatusData getStatusData(String id);
	
	public ResultsData getResultData(String id);

	public String killJob(String id);

	public List<StatusData> getJobsForCurrentSession();

	public String untieJobsFromSession();
	
}
