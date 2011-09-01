package ch.systemsx.sybit.crkwebui.client.model;

import java.util.List;

import com.extjs.gxt.ui.client.data.BaseModel;

/**
 * 
 * @author srebniak_a
 *
 */
public class InterfaceItemModel extends BaseModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public InterfaceItemModel()
	{
		set("id", "");
		set("area", "");
		set("name", "");
		set("size1", "");
		set("size2", "");
		set("finalCall", "");
		set("operator", "");
		set("warnings", "");
		set("METHODS", "");
	}

	public InterfaceItemModel(int id,
							  double area,
							  String name,
							  int size1,
							  int size2,
							  String finalCall,
							  String operator,
							  List<String> warnings) 
	{
		set("id", id);
		set("area", area);
		set("name", name);
		set("size1", size1);
		set("size2", size2);
		set("finalCall", finalCall);
		set("operator", operator);
		set("warnings", warnings);
		set("METHODS", "");
	}

	public int getId() {
		return (Integer) get("id");
	}
	
	public void setId(int id) {
		set("id", id);
	}

	public double getArea() {
		return (Double) get("area");
	}
	
	public void setArea(double area) {
		set("area", area);
	}

	public String getName() {
		return (String) get("name");
	}
	
	public void setName(String name) {
		set("name", name);
	}
	
	public int getSize1() {
		return (Integer) get("size1");
	}
	
	public void setSize1(int size1) {
		set("size1", size1);
	}
	
	public int getSize2() {
		return (Integer) get("size2");
	}
	
	public void setSize2(int size2) {
		set("size2", size2);
	}
	
	public String getFinalCall() {
		return (String) get("finalCall");
	}
	
	public void setFinalCall(String finalCall) {
		set("finalCall", finalCall);
	}
	
	public String getOperator() {
		return (String) get("operator");
	}
	
	public void setOperator(String operator) {
		set("operator", operator);
	}
	
	public List<String> getWarnings() {
		return (List<String>) get("warnings");
	}
	
	public void setWarnings(List<String> warnings) {
		set("warnings", warnings);
	}
	
	public String getMETHODS() {
		return (String) get("METHODS");
	}
}
