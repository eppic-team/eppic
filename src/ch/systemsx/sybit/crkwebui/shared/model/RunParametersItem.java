package ch.systemsx.sybit.crkwebui.shared.model;

import java.io.Serializable;

import model.RunParametersItemDB;

public class RunParametersItem implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private int uid;
	
	private int homologsCutoff;
	private double idCutoff;
	private double queryCovCutoff;
	private int maxNumSeqsCutoff;
	private int reducedAlphabet;
	private int minCoreSizeForBio;
	private double caCutoffForGeom;
	private double caCutoffForRimCore;
	private double caCutoffForZscore;
	private double entrCallCutoff;
	private double kaksCallCutoff;
	private double zScoreCutoff; 
	
	private String uniprotVer;
	private String crkVersion;

	public RunParametersItem() 
	{
		
	}
	
	public int getReducedAlphabet() {
		return reducedAlphabet;
	}
	
	public void setReducedAlphabet(int reducedAlphabet) {
		this.reducedAlphabet = reducedAlphabet;
	}
	
	public int getHomologsCutoff() {
		return homologsCutoff;
	}

	public void setHomologsCutoff(int homologsCutoff) {
		this.homologsCutoff = homologsCutoff;
	}

	public int getMinCoreSizeForBio() {
		return minCoreSizeForBio;
	}

	public void setMinCoreSizeForBio(int minCoreSizeForBio) {
		this.minCoreSizeForBio = minCoreSizeForBio;
	}

	public double getCaCutoffForGeom() {
		return caCutoffForGeom;
	}

	public void setCaCutoffForGeom(double caCutoffForGeom) {
		this.caCutoffForGeom = caCutoffForGeom;
	}

	public double getCaCutoffForRimCore() {
		return caCutoffForRimCore;
	}

	public void setCaCutoffForRimCore(double caCutoffForRimCore) {
		this.caCutoffForRimCore = caCutoffForRimCore;
	}

	public double getCaCutoffForZscore() {
		return caCutoffForZscore;
	}

	public void setCaCutoffForZscore(double caCutoffForZscore) {
		this.caCutoffForZscore = caCutoffForZscore;
	}

	public double getEntrCallCutoff() {
		return entrCallCutoff;
	}

	public void setEntrCallCutoff(double entrCallCutoff) {
		this.entrCallCutoff = entrCallCutoff;
	}

	public double getKaksCallCutoff() {
		return kaksCallCutoff;
	}

	public void setKaksCallCutoff(double kaksCallCutoff) {
		this.kaksCallCutoff = kaksCallCutoff;
	}

	public double getzScoreCutoff() {
		return zScoreCutoff;
	}

	public void setzScoreCutoff(double zScoreCutoff) {
		this.zScoreCutoff = zScoreCutoff;
	}

	public double getIdCutoff() {
		return idCutoff;
	}

	public void setIdCutoff(double idCutoff) {
		this.idCutoff = idCutoff;
	}

	public double getQueryCovCutoff() {
		return queryCovCutoff;
	}

	public void setQueryCovCutoff(double queryCovCutoff) {
		this.queryCovCutoff = queryCovCutoff;
	}

	public int getMaxNumSeqsCutoff() {
		return maxNumSeqsCutoff;
	}

	public void setMaxNumSeqsCutoff(int maxNumSeqsCutoff) {
		this.maxNumSeqsCutoff = maxNumSeqsCutoff;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

	public int getUid() {
		return uid;
	}
	
	public static RunParametersItem create(RunParametersItemDB runParametersItemDB)
	{
		RunParametersItem runParametersItem = new RunParametersItem();
		runParametersItem.setCaCutoffForGeom(runParametersItemDB.getCaCutoffForGeom());
		runParametersItem.setCaCutoffForRimCore(runParametersItemDB.getCaCutoffForRimCore());
		runParametersItem.setCaCutoffForZscore(runParametersItemDB.getCaCutoffForZscore());
		runParametersItem.setEntrCallCutoff(runParametersItemDB.getEntrCallCutoff());
		runParametersItem.setHomologsCutoff(runParametersItemDB.getHomologsCutoff());
		runParametersItem.setIdCutoff(runParametersItemDB.getIdCutoff());
		runParametersItem.setKaksCallCutoff(runParametersItemDB.getKaksCallCutoff());
		runParametersItem.setMaxNumSeqsCutoff(runParametersItemDB.getMaxNumSeqsCutoff());
		runParametersItem.setMinCoreSizeForBio(runParametersItemDB.getMinCoreSizeForBio());
		runParametersItem.setQueryCovCutoff(runParametersItemDB.getQueryCovCutoff());
		runParametersItem.setReducedAlphabet(runParametersItemDB.getReducedAlphabet());
		runParametersItem.setUid(runParametersItemDB.getUid());
		runParametersItem.setzScoreCutoff(runParametersItemDB.getzScoreCutoff());
		runParametersItem.setUniprotVer(runParametersItemDB.getUniprotVer());
		runParametersItem.setCrkVersion(runParametersItemDB.getCrkVersion());
		return runParametersItem;
	}

	public String getUniprotVer() {
		return uniprotVer;
	}

	public void setUniprotVer(String uniprotVer) {
		this.uniprotVer = uniprotVer;
	}

	public String getCrkVersion() {
		return crkVersion;
	}

	public void setCrkVersion(String crkVersion) {
		this.crkVersion = crkVersion;
	}
}
