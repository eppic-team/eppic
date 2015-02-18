package eppic.model;

import java.io.Serializable;
import java.util.List;

public class AssemblyDB implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private int uid;
	
	private String pdbCode;
	
	private String method;
	private int mmSize;
	private String symmetry;
	private String stoichiometry;
	private String pseudoSymmetry;
	private String pseudoStoichiometry;
	
	private double confidence;
	
	private PdbInfoDB pdbInfo;
	
	private List<InterfaceClusterScoreDB> interfaceClusterScores;

	public AssemblyDB() {
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

	public String getMethod() {
		return method;
	}

	public void setMethod(String type) {
		this.method = type;
	}

	public int getMmSize() {
		return mmSize;
	}

	public void setMmSize(int mmSize) {
		this.mmSize = mmSize;
	}

	public String getSymmetry() {
		return symmetry;
	}

	public void setSymmetry(String symmetry) {
		this.symmetry = symmetry;
	}

	public String getPseudoSymmetry() {
		return pseudoSymmetry;
	}

	public void setPseudoSymmetry(String pseudoSymmetry) {
		this.pseudoSymmetry = pseudoSymmetry;
	}

	public String getStoichiometry() {
		return stoichiometry;
	}

	public void setStoichiometry(String stoichiometry) {
		this.stoichiometry = stoichiometry;
	}

	public String getPseudoStoichiometry() {
		return pseudoStoichiometry;
	}

	public void setPseudoStoichiometry(String pseudoStoichiometry) {
		this.pseudoStoichiometry = pseudoStoichiometry;
	}

	public double getConfidence() {
		return confidence;
	}
	
	public void setConfidence(double confidence) {
		this.confidence = confidence;
	}
	
	public PdbInfoDB getPdbInfo() {
		return pdbInfo;
	}

	public void setPdbInfo(PdbInfoDB pdbInfo) {
		this.pdbInfo = pdbInfo;
	}

	public List<InterfaceClusterScoreDB> getInterfaceClusterScores() {
		return interfaceClusterScores;
	}

	public void setInterfaceClusterScores(List<InterfaceClusterScoreDB> interfaceClusterScores) {
		this.interfaceClusterScores = interfaceClusterScores;
	}
	
	

}
