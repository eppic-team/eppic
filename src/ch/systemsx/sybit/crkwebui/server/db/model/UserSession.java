package ch.systemsx.sybit.crkwebui.server.db.model;

import java.io.Serializable;

public class UserSession implements Serializable 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private int uid;
	private String sessionId;
	
	public UserSession()
	{
		
	}
	
	public int getUid() {
		return uid;
	}
	public void setUid(int uid) {
		this.uid = uid;
	}
	public String getSessionId() {
		return sessionId;
	}
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
	
}
