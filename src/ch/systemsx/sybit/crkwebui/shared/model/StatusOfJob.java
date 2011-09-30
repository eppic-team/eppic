package ch.systemsx.sybit.crkwebui.shared.model;

import java.io.Serializable;

public class StatusOfJob implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static String RUNNING = "Running";
	public static String STOPPED = "Stopped";
	public static String FINISHED = "Finished";
	public static String ERROR = "Error";
	public static String NONEXISTING = "nonexisting";
	public static String QUEUING = "queuing";
}
