package eppic.model.db;

import com.fasterxml.jackson.annotation.JsonBackReference;
import java.io.Serializable;

public class RunParametersDB implements Serializable {

	private static final long serialVersionUID = 1L;

	private double homSoftIdCutoff;
	private double homHardIdCutoff;
	private double queryCovCutoff;
	
	private int minNumSeqsCutoff;
	private int maxNumSeqsCutoff;
	
	private String alphabet;
	
	// core assignments cutoffs
	private double caCutoffForGeom;
	private double caCutoffForCoreRim;
	private double caCutoffForCoreSurface;
	
	// call cutoffs
	private int geomCallCutoff;
	private double crCallCutoff;
	private double csCallCutoff;
	
	private String searchMode;

	private String uniProtVersion;
	private String eppicVersion;
	private String eppicBuild;

	@JsonBackReference(value = "runParameters-ref")
	private PdbInfoDB pdbInfo;
	
	public RunParametersDB() {
		
	}
	
	public String getAlphabet() {
		return alphabet;
	}
	
	public void setAlphabet(String alphabet) {
		this.alphabet = alphabet;
	}
	
	public int getMinNumSeqsCutoff() {
		return minNumSeqsCutoff;
	}

	public void setMinNumSeqsCutoff(int homologsCutoff) {
		this.minNumSeqsCutoff = homologsCutoff;
	}

	public int getGeomCallCutoff() {
		return geomCallCutoff;
	}

	public void setGeomCallCutoff(int geomCallCutoff) {
		this.geomCallCutoff = geomCallCutoff;
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

	public void setCrCallCutoff(double crCallCutoff) {
		this.crCallCutoff = crCallCutoff;
	}

	public double getCsCallCutoff() {
		return csCallCutoff;
	}

	public void setCsCallCutoff(double csCallCutoff) {
		this.csCallCutoff = csCallCutoff;
	}

	public double getHomSoftIdCutoff() {
		return homSoftIdCutoff;
	}

	public void setHomSoftIdCutoff(double homSoftIdCutoff) {
		this.homSoftIdCutoff = homSoftIdCutoff;
	}
	
	public double getHomHardIdCutoff() {
		return homHardIdCutoff;
	}
	
	public void setHomHardIdCutoff(double homHardIdCutoff) {
		this.homHardIdCutoff = homHardIdCutoff;
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

	public String getSearchMode() {
		return searchMode;
	}

	public void setSearchMode(String searchMode) {
		this.searchMode = searchMode;
	}

	public void setPdbInfo(PdbInfoDB pdbInfo) {
		this.pdbInfo = pdbInfo;
	}

	public PdbInfoDB getPdbInfo() {
		return pdbInfo;
	}

	public String getUniProtVersion() {
		return uniProtVersion;
	}
	
	public void setUniProtVersion(String uniProtVersion) {
		this.uniProtVersion = uniProtVersion;
	}

	public String getEppicVersion() {
		return eppicVersion;
	}

	public void setEppicVersion(String eppicVersion) {
		this.eppicVersion = eppicVersion;
	}

	public String getEppicBuild() {
		return eppicBuild;
	}

	public void setEppicBuild(String eppicBuild) {
		this.eppicBuild = eppicBuild;
	}
}
