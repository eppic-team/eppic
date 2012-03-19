package ch.systemsx.sybit.crkwebui.server;

/**
 * Crk runner thread.
 * @author AS
 *
 */
public class CrkThread extends Thread
{
	private CrkRunner crkRunner;

	public CrkThread(CrkThreadGroup runInstances, CrkRunner crkRunner,
			String jobId)
	{
		super(runInstances, crkRunner, jobId);
		this.crkRunner = crkRunner;
	}
	
	@Override
	public void interrupt()
	{
		super.interrupt();
		
		if(crkRunner != null)
		{
			crkRunner.stopJob();
		}
	}

}
