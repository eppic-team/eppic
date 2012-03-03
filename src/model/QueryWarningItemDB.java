package model;

import java.io.Serializable;

public class QueryWarningItemDB implements Serializable 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private int uid;
	
	private String text;
	
	private HomologsInfoItemDB homologsInfoItem;
	
	public QueryWarningItemDB() 
	{
		
	}
	
	public void setHomologsInfoItem(HomologsInfoItemDB homologsInfoItem) {
		this.homologsInfoItem = homologsInfoItem;
	}

	public HomologsInfoItemDB getHomologsInfoItem() {
		return homologsInfoItem;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

	public int getUid() {
		return uid;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getText() {
		return text;
	}

}
