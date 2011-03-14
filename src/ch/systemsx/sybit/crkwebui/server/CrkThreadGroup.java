package ch.systemsx.sybit.crkwebui.server;


public class CrkThreadGroup extends ThreadGroup 
{
//	private int nrOfAllowedThreadsInParallel = 2;
//	private int nrOfRunningThreads = 0;

	public CrkThreadGroup(String name) 
	{
		super(name);
	}

	public void uncaughtException(Thread t, Throwable e) 
	{
		
	}
	
//	public synchronized void runNextInQueue()
//	{
//		nrOfRunningThreads--;
//		
//		int estimatedNrOfCurrentThreads = this.activeCount();
//		Thread[] activeInstances = new Thread[estimatedNrOfCurrentThreads];
//
//		int nrOfCurrentThreads = this.enumerate(activeInstances);
//		
//		while(nrOfCurrentThreads > estimatedNrOfCurrentThreads)
//		{
//			estimatedNrOfCurrentThreads = nrOfCurrentThreads;
//			activeInstances = new Thread[nrOfCurrentThreads];
//			nrOfCurrentThreads = this.enumerate(activeInstances);
//		}
//		
//		boolean wasStarted = false;
//		int i = 0;
//		
//		while((!wasStarted) &&
//			  (i < activeInstances.length))
//		{
//			CrkRunner activeThread = (CrkRunner)activeInstances[i];
//			
//			if((activeThread != null) && 
//			   (!activeThread.isInterrupted()) &&
//			   (activeThread.isWaiting()))
//			{
//				if(nrOfRunningThreads < nrOfAllowedThreadsInParallel)
//				{
//					nrOfRunningThreads++;
//					activeThread.setIsWaiting(false);
//					activeThread.notifyAll();
//					wasStarted = true;
//				}
//			}
//			
//			i++;
//		}
//	}
//	
//	public synchronized boolean checkIfCanBeRun()
//	{
//		if(nrOfRunningThreads >= nrOfAllowedThreadsInParallel)
//		{
//			return false;
//		}
//		else
//		{
//			nrOfRunningThreads++;
//			return true;
//		}
//	}
}
