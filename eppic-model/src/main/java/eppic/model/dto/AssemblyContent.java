package eppic.model.dto;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import eppic.model.db.AssemblyContentDB;

@XmlAccessorType(XmlAccessType.FIELD)
public class AssemblyContent implements Serializable {

	private static final long serialVersionUID = 1L;
	private int uid;	
	private int mmSize;
	private String symmetry;
	private String stoichiometry;
	private String composition;
	private String compositionRepChainIds;
	
	
	

	private String chainIds;
	
	
	public int getUid() {
		return uid;
	}
	
	public void setUid(int uid) {
		this.uid = uid;
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
	
	public String getStoichiometry() {
		return stoichiometry;
	}
	
	public void setStoichiometry(String stoichiometry) {
		this.stoichiometry = stoichiometry;
	}
	
	public String getComposition() {
		return composition;
	}
	
	public void setComposition(String composition) {
		this.composition = composition;
	}
	
	public String getCompositionRepChainIds() {
		return compositionRepChainIds;
	}

	public void setCompositionRepChainIds(String compositionRepChainIds) {
		this.compositionRepChainIds = compositionRepChainIds;
	}

	public String getChainIds() {
		return chainIds;
	}

	public void setChainIds(String chainIds) {
		this.chainIds = chainIds;
	}

	public static AssemblyContent create(AssemblyContentDB assemblyContentDB) {
		AssemblyContent ac = new AssemblyContent();
		ac.setMmSize(assemblyContentDB.getMmSize());
		ac.setSymmetry(assemblyContentDB.getSymmetry());
		ac.setStoichiometry(assemblyContentDB.getStoichiometry());
		ac.setComposition(assemblyContentDB.getComposition());
		ac.setCompositionRepChainIds(assemblyContentDB.getCompositionRepChainIds());
		ac.setChainIds(assemblyContentDB.getChainIds());
		return ac;
	}
	
}
