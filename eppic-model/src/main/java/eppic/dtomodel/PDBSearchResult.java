package eppic.dtomodel;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Date;

/**
 * A class used to display the Search Results in the interface
 * @author biyani_n
 *
 */
public class PDBSearchResult implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public class PDBSearchResultComparator implements Comparator<PDBSearchResult> {

	private String fieldName;


	@Override
	public int compare(PDBSearchResult o1, PDBSearchResult o2) {
		// TODO before refactoring into dbtools package this was here (depending on SortDir from sencha), it looks it was never used. Review
	    //if(SortDir.ASC == dir)
		//return compare(o1, o2, fieldName);
	    //else
		return -1 * compare(o1, o2, fieldName);
	}

	private int compare(PDBSearchResult a, PDBSearchResult b, String propertieName) {
	    if("pdbCode".equals(propertieName))
		return new String(a.getPdbCode()).compareTo(b.getPdbCode());
	    if("title".equals(propertieName))
		return new String(a.getTitle()).compareTo(b.getTitle());
	    if("releaseDate".equals(propertieName))
		return (a.getReleaseDate().compareTo(b.getReleaseDate()));
	    if("spaceGroup".equals(propertieName))
		return new String(a.getSpaceGroup()).compareTo(b.getSpaceGroup());
	    if("resolution".equals(propertieName))
		return new Double(a.getResolution()).compareTo(b.getResolution());
	    if("rfreeValue".equals(propertieName))
		return new Double(a.getRfreeValue()).compareTo(b.getRfreeValue());
	    if("expMethod".equals(propertieName))
		return new String(a.getExpMethod()).compareTo(b.getExpMethod());

	    return 0;
	}

    }

    private int uid;
    private String pdbCode;
    private String title;
    private Date releaseDate;
    private String spaceGroup;
    private double resolution;
    private double rfreeValue;
    private String expMethod;
    private SequenceClusterType sequenceClusterType;
    private boolean suspicious;
    private double cellA;
    private double cellB;
    private double cellC;
    private double cellAlpha;
    private double cellBeta;
    private double cellGamma;
    private int crystalFormId;

    public PDBSearchResult() {}
    
    public PDBSearchResult(int uid,
	    String pdbCode,
	    String title,
	    Date releaseDate,
	    String spaceGroup,
	    double cellA, double cellB, double cellC, 
	    double cellAlpha, double cellBeta, double cellGamma, 
	    double resolution,
	    double rfreeValue,
	    String expMethod,
	    int crystalFormId, 
	    SequenceClusterType sequenceClusterType){
	this(uid, pdbCode, title, releaseDate, spaceGroup, resolution, rfreeValue, expMethod);
	this.setCellA(cellA);
	this.setCellB(cellB);
	this.setCellC(cellC);
	this.setCellAlpha(cellAlpha);
	this.setCellBeta(cellBeta);
	this.setCellGamma(cellGamma);
	this.setCrystalFormId(crystalFormId);
	this.setSequenceClusterType(sequenceClusterType);
    }

    public PDBSearchResult(int uid,
	    String pdbCode,
	    String title,
	    Date releaseDate,
	    String spaceGroup,
	    double resolution,
	    double rfreeValue,
	    String expMethod){

	this.uid = uid;
	this.pdbCode = pdbCode;
	this.title = title;
	this.releaseDate = releaseDate;
	this.spaceGroup = spaceGroup;
	this.resolution = resolution;
	this.rfreeValue = rfreeValue;
	this.expMethod = expMethod;

    }

    public String getPdbCode() {
	return pdbCode;
    }

    public void setPdbCode(String pdbCode) {
	this.pdbCode = pdbCode;
    }

    public int getUid() {
	return uid;
    }

    public void setUid(int uid) {
	this.uid = uid;
    }

    public String getTitle() {
	return title;
    }

    public void setTitle(String title) {
	this.title = title;
    }

    public Date getReleaseDate() {
	return releaseDate;
    }

    public void setReleaseDate(Date releaseDate) {
	this.releaseDate = releaseDate;
    }

    public String getSpaceGroup() {
	return spaceGroup;
    }

    public void setSpaceGroup(String spaceGroup) {
	this.spaceGroup = spaceGroup;
    }

    public double getResolution() {
	return resolution;
    }

    public void setResolution(double resolution) {
	this.resolution = resolution;
    }

    public double getRfreeValue() {
	return rfreeValue;
    }

    public void setRfreeValue(double rfreeValue) {
	this.rfreeValue = rfreeValue;
    }

    public String getExpMethod() {
	return expMethod;
    }

    public void setExpMethod(String expMethod) {
	this.expMethod = expMethod;
    }

    public SequenceClusterType getSequenceClusterType() {
	return sequenceClusterType;
    }

    public void setSequenceClusterType(SequenceClusterType sequenceClusterType) {
	this.sequenceClusterType = sequenceClusterType;
    }

    public boolean isSuspicious() {
	return suspicious;
    }

    public void setSuspicious(boolean suspicious) {
	this.suspicious = suspicious;
    }

    public double getCellA() {
	return cellA;
    }

    public void setCellA(double cellA) {
	this.cellA = cellA;
    }

    public double getCellB() {
	return cellB;
    }

    public void setCellB(double cellB) {
	this.cellB = cellB;
    }

    public double getCellC() {
	return cellC;
    }

    public void setCellC(double cellC) {
	this.cellC = cellC;
    }

    public double getCellAlpha() {
	return cellAlpha;
    }

    public void setCellAlpha(double cellAlpha) {
	this.cellAlpha = cellAlpha;
    }

    public double getCellBeta() {
	return cellBeta;
    }

    public void setCellBeta(double cellBeta) {
	this.cellBeta = cellBeta;
    }

    public double getCellGamma() {
	return cellGamma;
    }

    public void setCellGamma(double cellGamma) {
	this.cellGamma = cellGamma;
    }

    public int getCrystalFormId() {
	return crystalFormId;
    }

    public void setCrystalFormId(int crystalFormId) {
	this.crystalFormId = crystalFormId;
    }
}
