package ch.systemsx.sybit.crkwebui.client.results.data;

import java.io.Serializable;

import ch.systemsx.sybit.crkwebui.client.commons.appdata.AppPropertiesManager;

/**
 * Data model for assemblies grid.
 * @author Althea Parker
 *
 */
public class AssemblyItemModel implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private int assemblyId; //assemblyId and identifier have the same value (different type)
	//private String identifier; 
	private int identifier;
	private String composition;
	private String mmSize;
	private String symmetry;
	private String stoichiometry;
	private String detailsButtonText = AppPropertiesManager.CONSTANTS.results_grid_interfaces_button();
	private String prediction;	
	private String thumbnailUrl = "";
	private String diagramUrl = "";
	private String pdbCode;
	private String numInterfaces;
	private boolean pdb1Assembly;
	//allie added on feb 28 2017
	private String callReason = "";
	private double confidence;
	private double assemblyScore;
	//private String assemblyScore;
	
	public double getAssemblyScore() {
		return assemblyScore;
	}
	public void setAssemblyScore(double assemblyScore) {
		this.assemblyScore = assemblyScore;
	}
	
	/*public String getAssemblyScore() {
		return assemblyScore;
	}
	public void setAssemblyScore(String assemblyScore) {
		this.assemblyScore = assemblyScore;
	}*/
	
	public double getConfidence() {
		return confidence;
	}
	public void setConfidence(double confidence) {
		this.confidence = confidence;
	}
	public String getPdbCode() {
		return pdbCode;
	}
	public void setPdbCode(String pdbCode) {
		this.pdbCode = pdbCode;
	}
	public String getPrediction() {
		return prediction;
	}
	public void setPrediction(String prediction) {
		this.prediction = prediction;
	}
	public int getAssemblyId() {
		return assemblyId;
	}
	public void setAssemblyId(int assemblyId) { 
		this.assemblyId = assemblyId;
	}
	public String getComposition() {
		return composition;
	}
	public void setComposition(String composition) {
		this.composition = composition;
	}
	public String getMmSize() {
		return mmSize;
	}
	public void setMmSize(String mmSize) {
		this.mmSize = mmSize;
	}
	public String getSymmetry() {
		return symmetry;
	}
	public void setSymmetry(String symmetry) {
		this.symmetry = symmetry;
	}
	public String getStoichiometry() {
		return stoichiometry;
	}
	public void setStoichiometry(String stoichiometry) {
		this.stoichiometry = stoichiometry;
	}
	/*public String getIdentifier() {
		return identifier;
	}
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}*/
	
	public int getIdentifier() {
		return identifier;
	}
	public void setIdentifier(int identifier) {
		this.identifier = identifier;
	}
	
	public String getDetailsButtonText() {
		return detailsButtonText;
	}

	public void setDetailsButtonText(String detailsButtonText) {
		this.detailsButtonText = detailsButtonText;
	}
	public String getThumbnailUrl() {
		return thumbnailUrl;
	}

	public void setThumbnailUrl(String thumbnailUrl) {
		this.thumbnailUrl = thumbnailUrl;
	}
	
	public String getDiagramUrl() {
		return diagramUrl;
	}

	public void setDiagramUrl(String diagramUrl) {
		this.diagramUrl = diagramUrl;
	}
		
	public String getNumInterfaces() {
		return numInterfaces;
	}
	public void setNumInterfaces(String numInterfaces) { 
		this.numInterfaces = numInterfaces;
	}
	
	public boolean isPdb1Assembly() {
		return pdb1Assembly;
	}
	
	public void setPdb1Assembly(boolean pdb1Assembly) {
		this.pdb1Assembly = pdb1Assembly;
	}
	
	public String getCallReason(){
		return callReason;
	}
	
	public void setCallReason(String callReason){
		this.callReason = callReason;
	}
	
}
