package ch.systemsx.sybit.crkwebui.client.results.data;

import java.util.List;

import ch.systemsx.sybit.crkwebui.shared.model.WarningItem;

import com.extjs.gxt.ui.client.data.BaseModel;

/**
 * Data model for interfaces grid.
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
		set("sizes", "");
		set("finalCallName", "");
		set("operator", "");
		set("operatorType", "");
		set("isInfinite", "");
		set("warnings", "");
		set("METHODS", "");
	}

	public InterfaceItemModel(int id,
							  double area,
							  String name,
							  String sizes,
							  String finalCallName,
							  String operator,
							  String operatorType,
							  boolean isInfinite,
							  List<WarningItem> warnings) 
	{
		set("id", id);
		set("area", area);
		set("name", name);
		set("sizes", sizes);
		set("finalCallName", finalCallName);
		set("operator", operator);
		set("operatorType", operatorType);
		set("isInfinite", isInfinite);
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
	
	public String getSizes() {
		return (String) get("sizes");
	}
	
	public void setSizes(String sizes) {
		set("sizes", sizes);
	}
	
	public String getFinalCallName() {
		return (String) get("finalCallName");
	}
	
	public void setFinalCallName(String finalCallName) {
		set("finalCallName", finalCallName);
	}
	
	public String getOperator() {
		return (String) get("operator");
	}
	
	public void setOperator(String operator) {
		set("operator", operator);
	}
	
	public String getOperatorType() {
		return (String) get("operatorType");
	}
	
	public void setOperatorType(String operatorType) {
		set("operatorType", operatorType);
	}
	
	public boolean getIsInfinite() {
		return (Boolean) get("isInfinite");
	}
	
	public void setIsInfinite(boolean isInfinite) {
		set("isInfinite", isInfinite);
	}
	
	public List<WarningItem> getWarnings() {
		return (List<WarningItem>) get("warnings");
	}
	
	public void setWarnings(List<WarningItem> warnings) {
		set("warnings", warnings);
	}
	
	public String getMETHODS() {
		return (String) get("METHODS");
	}
}
