package ch.systemsx.sybit.crkwebui.shared;

import java.io.Serializable;

public class CrkWebException extends Exception implements Serializable
{
	public CrkWebException()
	{
		
	}

	public CrkWebException(Throwable e)
	{
		super(e);
	}
}
