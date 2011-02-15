package ch.systemsx.sybit.crkwebui.server;

public class CrkThreadGroup extends ThreadGroup {

	public CrkThreadGroup(String name) {
		super(name);
	}

	public void uncaughtException(Thread t, Throwable e) 
	{
		
	}
}
