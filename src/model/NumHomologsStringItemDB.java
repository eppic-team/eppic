package model;

import java.io.Serializable;

public class NumHomologsStringItemDB implements Serializable 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private int uid;
	
	private String text;
	
	private PDBScoreItemDB pdbScoreItem;
	
	public NumHomologsStringItemDB() 
	{
		
	}
	
	public NumHomologsStringItemDB(int uid,
								 String text) 
	{
		this.uid = uid;
		this.text = text;
	}
	
	public void setPdbScoreItem(PDBScoreItemDB pdbScoreItem) {
		this.pdbScoreItem = pdbScoreItem;
	}

	public PDBScoreItemDB getPdbScoreItem() {
		return pdbScoreItem;
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
