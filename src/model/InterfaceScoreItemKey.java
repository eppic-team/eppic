package model;

import java.io.Serializable;

public class InterfaceScoreItemKey implements Serializable
{
	private String method;
	private int interfaceId;
	
	public String getMethod() {
		return method;
	}
	public void setMethod(String method) {
		this.method = method;
	}
	public int getInterfaceId() {
		return interfaceId;
	}
	public void setInterfaceId(int interfaceId) {
		this.interfaceId = interfaceId;
	}
	
	public boolean equals(Object that)
	{
		if(this == that)
		{
			return true;
		}
		
		if((this == null) && (that == null))
		{
			return true;
		}
		
		if((this == null) && (that != null) ||
		   (this != null) && (that == null))
		{
			return false;
		}
		
		InterfaceScoreItemKey thatObject = (InterfaceScoreItemKey)that;
		
		if(this.getInterfaceId() == thatObject.getInterfaceId() &&
		   this.getMethod().equals(thatObject.getMethod()))
		{
			return true;
		}
		
		return false;
		
		
	}
	
}
