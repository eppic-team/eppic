package ch.systemsx.sybit.crkwebui.client.model;

import com.extjs.gxt.ui.client.data.BaseModel;

public class InterfaceResidueSummaryModel extends BaseModel 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public InterfaceResidueSummaryModel()
	{
		
	}

	public InterfaceResidueSummaryModel( String title,
										 double asa,
										 double bsa,
										 double bsaPercentage) 
	{
		set("title", title);
		set("asa", asa);
		set("bsa", bsa);
		set("bsaPercentage", bsaPercentage);
		set("METHODS", "");
	}

	public String getTitle() {
		return (String) get("title");
	}
	
	public void setTitle(String title) {
		set("title", title);
	}
	
	public double getAsa() {
		return (Double) get("asa");
	}
	
	public void setAsa(double asa) {
		set("asa", asa);
	}
	
	public double getBsa() {
		return (Double) get("bsa");
	}
	
	public void setBsa(double bsa) {
		set("bsa", bsa);
	}
	
	public double getBsaPercentage() {
		return (Double) get("bsaPercentage");
	}
	
	public void setBsaPercentage(double bsaPercentage) {
		set("bsaPercentage", bsaPercentage);
	}
}
