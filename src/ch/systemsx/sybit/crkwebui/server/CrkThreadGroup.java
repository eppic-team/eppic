package ch.systemsx.sybit.crkwebui.server;

/**
 * This class is used to register each job as thread in the group.
 * @author srebniak_a
 *
 */
public class CrkThreadGroup extends ThreadGroup 
{
	public CrkThreadGroup(String name) 
	{
		super(name);
	}

	public void uncaughtException(Thread t, Throwable e) 
	{
		
	}
}
