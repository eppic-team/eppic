package eppic.model.dto;

import java.io.Serializable;

import eppic.model.db.RunParametersDB;

/**
 * DTO class for run parameters entry.
 */
public class RunParameters implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private int uid;
	
	private double homSoftIdCutoff;
	private double homHardIdCutoff;
	private double queryCovCutoff;
	
	private int minNumSeqsCutoff;
	private int maxNumSeqsCutoff;
	
	private String alphabet;
	
	private double caCutoffForGeom;
	private double caCutoffForCoreRim;
	private double caCutoffForCoreSurface;
	
	private int geomCallCutoff;
	private double crCallCutoff;
	private double csCallCutoff;
	
	private String searchMode;
	
	private String uniprotVersion;
	private String eppicVersion;
	private String eppicBuild;

	public RunParameters() 
	{
		
	}
	
	public String getAlphabet() {
		return alphabet;
	}
	
	public void setAlphabet(String alphabet) {
		this.alphabet = alphabet;
	}
	
	public int getMinNumSeqsCutOff() {
		return minNumSeqsCutoff;
	}

	public void setMinNumSeqsCutOff(int minNumSeqsCutOff) {
		this.minNumSeqsCutoff = minNumSeqsCutOff;
	}

	public int getGeomCallCutOff() {
		return geomCallCutoff;
	}

	public void setGeomCallCutOff(int geomCallCutOff) {
		this.geomCallCutoff = geomCallCutOff;
	}

	public double getCaCutoffForGeom() {
		return caCutoffForGeom;
	}

	public void setCaCutoffForGeom(double caCutoffForGeom) {
		this.caCutoffForGeom = caCutoffForGeom;
	}

	public double getCaCutoffForCoreRim() {
		return caCutoffForCoreRim;
	}

	public void setCaCutoffForCoreRim(double caCutoffForCoreRim) {
		this.caCutoffForCoreRim = caCutoffForCoreRim;
	}

	public double getCaCutoffForCoreSurface() {
		return caCutoffForCoreSurface;
	}

	public void setCaCutoffForCoreSurface(double caCutoffForCoreSurface) {
		this.caCutoffForCoreSurface = caCutoffForCoreSurface;
	}

	public double getCrCallCutoff() {
		return crCallCutoff;
	}

	public void setCrCallCutoff(double csCallCutoff) {
		this.crCallCutoff = csCallCutoff;
	}

	public double getCsCallCutoff() {
		return csCallCutoff;
	}

	public void setCsCallCutoff(double csCallCutoff) {
		this.csCallCutoff = csCallCutoff;
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
	
	public String getUniprotVersion() {
		return uniprotVersion;
	}

	public void setUniprotVersion(String uniprotVersion) {
		this.uniprotVersion = uniprotVersion;
	}

	public String getEppicVersion() {
		return eppicVersion;
	}

	public void setEppicVersion(String eppicVersion) {
		this.eppicVersion = eppicVersion;
	}

	public void setHomSoftIdCutoff(double homSoftIdCutoff) {
		this.homSoftIdCutoff = homSoftIdCutoff;
	}

	public double getHomSoftIdCutoff() {
		return homSoftIdCutoff;
	}

	public void setHomHardIdCutoff(double homHardIdCutoff) {
		this.homHardIdCutoff = homHardIdCutoff;
	}

	public double getHomHardIdCutoff() {
		return homHardIdCutoff;
	}
	
	public String getSearchMode() {
		return searchMode;
	}

	public void setSearchMode(String searchMode) {
		this.searchMode = searchMode;
	}

	public int getMinNumSeqsCutoff() {
		return minNumSeqsCutoff;
	}

	public void setMinNumSeqsCutoff(int minNumSeqsCutoff) {
		this.minNumSeqsCutoff = minNumSeqsCutoff;
	}

	public int getGeomCallCutoff() {
		return geomCallCutoff;
	}

	public void setGeomCallCutoff(int geomCallCutoff) {
		this.geomCallCutoff = geomCallCutoff;
	}

	public String getEppicBuild() {
		return eppicBuild;
	}

	public void setEppicBuild(String eppicBuild) {
		this.eppicBuild = eppicBuild;
	}

	/**
	 * Converts DB model item into DTO one.
	 * @param runParametersDB model item to convert
	 * @return DTO representation of model item
	 */
	public static RunParameters create(RunParametersDB runParametersDB)
	{
		RunParameters runParameters = new RunParameters();
		runParameters.setCaCutoffForGeom(runParametersDB.getCaCutoffForGeom());
		runParameters.setCaCutoffForCoreRim(runParametersDB.getCaCutoffForCoreRim());
		runParameters.setCaCutoffForCoreSurface(runParametersDB.getCaCutoffForCoreSurface());
		runParameters.setCrCallCutoff(runParametersDB.getCrCallCutoff());
		runParameters.setMinNumSeqsCutOff(runParametersDB.getMinNumSeqsCutoff());
		runParameters.setHomHardIdCutoff(runParametersDB.getHomHardIdCutoff());
		runParameters.setHomSoftIdCutoff(runParametersDB.getHomSoftIdCutoff());
		runParameters.setMaxNumSeqsCutoff(runParametersDB.getMaxNumSeqsCutoff());
		runParameters.setGeomCallCutOff(runParametersDB.getGeomCallCutoff());
		runParameters.setQueryCovCutoff(runParametersDB.getQueryCovCutoff());
		runParameters.setAlphabet(runParametersDB.getAlphabet());
		runParameters.setUid(runParametersDB.getUid());
		runParameters.setCsCallCutoff(runParametersDB.getCsCallCutoff());
		runParameters.setUniprotVersion(runParametersDB.getUniProtVersion());
		runParameters.setEppicVersion(runParametersDB.getEppicVersion());
		runParameters.setSearchMode(runParametersDB.getSearchMode());
		runParameters.setEppicBuild(runParametersDB.getEppicBuild());
		return runParameters;
	}

}
