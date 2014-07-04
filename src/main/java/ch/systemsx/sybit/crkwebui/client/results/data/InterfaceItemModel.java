package ch.systemsx.sybit.crkwebui.client.results.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import ch.systemsx.sybit.crkwebui.client.commons.appdata.AppPropertiesManager;
import ch.systemsx.sybit.crkwebui.shared.model.InterfaceWarning;

/**
 * Data model for interfaces grid.
 * @author srebniak_a
 *
 */
public class InterfaceItemModel implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private int interfaceId;
	private int clusterId;
	
	private double area;
	private String name;
	private String sizes;
	
	private String operator;
	private String operatorType;
	
	private boolean isInfinite;
	
	//Interface Calls
	private String geometryCall;
	private String coreRimCall;
	private String coreSurfaceCall;
	private String finalCallName;
	
	//Cluster Calls
	private String clusterGeometryCall;
	private String clusterCoreRimCall;
	private String clusterCoreSurfaceCall;
	private String clusterFinalCall;

	private List<InterfaceWarning> warnings;
	private String thumbnailUrl = "";
	private String warningsImagePath = "resources/icons/warning_icon.png";
	private String detailsButtonText = AppPropertiesManager.CONSTANTS.results_grid_details_button();

	private double confidence;
	private double clusterConfidence;

	public InterfaceItemModel()
	{
		interfaceId = 0;
		clusterId = 0;
		area = 0;
		name = "";
		sizes = "";
		geometryCall = "";
		coreRimCall = "";
		coreSurfaceCall = "";
		finalCallName = "";
		clusterGeometryCall = "";
		clusterCoreRimCall = "";
		clusterCoreSurfaceCall = "";
		clusterFinalCall = "";
		operator = "";
		operatorType = "";
		isInfinite = false;
		warnings = new ArrayList<InterfaceWarning>();
	}

	public int getClusterId() {
		return clusterId;
	}

	public void setClusterId(int clusterId) {
		this.clusterId = clusterId;
	}

	public String getGeometryCall() {
		return geometryCall;
	}

	public void setGeometryCall(String geometryCall) {
		this.geometryCall = geometryCall;
	}

	public String getCoreRimCall() {
		return coreRimCall;
	}

	public void setCoreRimCall(String coreRimCall) {
		this.coreRimCall = coreRimCall;
	}

	public String getCoreSurfaceCall() {
		return coreSurfaceCall;
	}

	public void setCoreSurfaceCall(String coreSurfaceCall) {
		this.coreSurfaceCall = coreSurfaceCall;
	}

	public String getWarningsImagePath() {
		return warningsImagePath;
	}

	public void setWarningsImagePath(String warningsImagePath) {
		this.warningsImagePath = warningsImagePath;
	}

	public int getInterfaceId() {
		return interfaceId;
	}

	public void setInterfaceId(int interfaceId) {
		this.interfaceId = interfaceId;
	}

	public double getArea() {
		return area;
	}

	public void setArea(double area) {
		this.area = area;
	}

	public boolean isInfinite() {
		return isInfinite;
	}

	public void setInfinite(boolean isInfinite) {
		this.isInfinite = isInfinite;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSizes() {
		return sizes;
	}

	public void setSizes(String sizes) {
		this.sizes = sizes;
	}

	public String getFinalCallName() {
		return finalCallName;
	}

	public void setFinalCallName(String finalCallName) {
		this.finalCallName = finalCallName;
	}

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	public String getOperatorType() {
		return operatorType;
	}

	public void setOperatorType(String operatorType) {
		this.operatorType = operatorType;
	}

	public List<InterfaceWarning> getWarnings() {
		return warnings;
	}

	public void setWarnings(List<InterfaceWarning> warnings) {
		this.warnings = warnings;
	}

	public String getThumbnailUrl() {
		return thumbnailUrl;
	}

	public void setThumbnailUrl(String thumbnailUrl) {
		this.thumbnailUrl = thumbnailUrl;
	}
	
	public String getDetailsButtonText() {
		return detailsButtonText;
	}

	public void setDetailsButtonText(String detailsButtonText) {
		this.detailsButtonText = detailsButtonText;
	}

	public String getClusterGeometryCall() {
		return clusterGeometryCall;
	}

	public void setClusterGeometryCall(String clusterGeometryCall) {
		this.clusterGeometryCall = clusterGeometryCall;
	}

	public String getClusterCoreRimCall() {
		return clusterCoreRimCall;
	}

	public void setClusterCoreRimCall(String clusterCoreRimCall) {
		this.clusterCoreRimCall = clusterCoreRimCall;
	}

	public String getClusterCoreSurfaceCall() {
		return clusterCoreSurfaceCall;
	}

	public void setClusterCoreSurfaceCall(String clusterCoreSurfaceCall) {
		this.clusterCoreSurfaceCall = clusterCoreSurfaceCall;
	}

	public String getClusterFinalCall() {
		return clusterFinalCall;
	}

	public void setClusterFinalCall(String clusterFinalCall) {
		this.clusterFinalCall = clusterFinalCall;
	}

	public void setFinalConfidence(double confidence) {
	    this.confidence = confidence;
	}
	
	public double getConfidence() {
	    return confidence;
	}
	
	public void setClusterFinalConfidence(double confidence) {
	    this.clusterConfidence = confidence;
	}
	
	public double getClusterConfidence() {
	    return clusterConfidence;
	}
}
