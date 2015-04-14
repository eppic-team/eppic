package ch.systemsx.sybit.crkwebui.shared.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import eppic.model.AssemblyDB;
import eppic.model.AssemblyScoreDB;
import eppic.model.PdbInfoDB;


public class Assembly implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private int uid;
	
	private boolean topologicallyValid;
	
	private String composition;

	private int mmSize;
	private String symmetry;
	private String stoichiometry;
	private String pseudoSymmetry;
	private String pseudoStoichiometry;
	
	private PdbInfoDB pdbInfo;
	
	private Set<InterfaceCluster> interfaceClusters;
	
	private List<AssemblyScore> assemblyScores;

	public Assembly() {
		this.interfaceClusters = new HashSet<InterfaceCluster>();
		this.assemblyScores = new ArrayList<AssemblyScore>();
	}

	public int getUid() {
		return uid;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

	public boolean isTopologicallyValid() {
		return topologicallyValid;
	}

	public void setTopologicallyValid(boolean topologicallyValid) {
		this.topologicallyValid = topologicallyValid;
	}

	public String getComposition() {
		return composition;
	}

	public void setComposition(String composition) {
		this.composition = composition;
	}

	public int getMmSize() {
		return mmSize;
	}

	public void setMmSize(int mMSize) {
		this.mmSize = mMSize;
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

	public PdbInfoDB getPdbInfo() {
		return pdbInfo;
	}

	public void setPdbInfo(PdbInfoDB pdbInfo) {
		this.pdbInfo = pdbInfo;
	}

	public Set<InterfaceCluster> getInterfaceClusters() {
		return interfaceClusters;
	}

	public void setInterfaceClusters(Set<InterfaceCluster> interfaceClusters) {
		this.interfaceClusters = interfaceClusters;
	}

	public List<AssemblyScore> getAssemblyScores() {
		return assemblyScores;
	}

	public void setAssemblyScores(List<AssemblyScore> assemblyScores) {
		this.assemblyScores = assemblyScores;
	}

	/**
	 * Converts DB model item into DTO one.
	 * @param assemblyDB model item to convert
	 * @return DTO representation of model item
	 */
	public static Assembly create(AssemblyDB assemblyDB) {
		Assembly assembly = new Assembly();
		
		assembly.setUid(assemblyDB.getUid());
		
		assembly.setMmSize(assemblyDB.getMmSize());
		assembly.setSymmetry(assemblyDB.getSymmetry());
		assembly.setStoichiometry(assemblyDB.getStoichiometry());		
		assembly.setPseudoSymmetry(assemblyDB.getPseudoSymmetry());
		assembly.setPseudoStoichiometry(assemblyDB.getPseudoStoichiometry());
		assembly.setTopologicallyValid(assemblyDB.isTopologicallyValid());
		assembly.setComposition(assemblyDB.getComposition());
		assembly.setPdbInfo(assemblyDB.getPdbInfo()); 
		
		if(assemblyDB.getAssemblyScores() != null) {
			
			List<AssemblyScoreDB> assemblyScoreDBs = assemblyDB.getAssemblyScores();
			
			List<AssemblyScore> assemblyScores = new ArrayList<AssemblyScore>();
			
			for(AssemblyScoreDB assemblyScoreDB : assemblyScoreDBs) {
				assemblyScores.add(AssemblyScore.create(assemblyScoreDB));
			}
			
			assembly.setAssemblyScores(assemblyScores);
		}
		
		// TODO initialise many-to-many relation to interface clusters
		
		return assembly;
	}
	
	
}
