package model;

import java.io.Serializable;

public class InterfaceResidueMethodItem implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private float score;
	
	public InterfaceResidueMethodItem()
	{
		
	}
	
	public InterfaceResidueMethodItem(float score) {
		this.score = score;
	}

	public void setScore(float score) {
		this.score = score;
	}

	public float getScore() {
		return score;
	}
}
