package ch.systemsx.sybit.crkwebui.shared.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import eppic.model.AssemblyContentDB;
import eppic.model.AssemblyDB;
import eppic.model.AssemblyScoreDB;
import eppic.model.PdbInfoDB;


public class Assembly implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private int uid;
	
	private int id;
	
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
	
	private List<AssemblyContent> assemblyContents;

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

	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
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

	public List<AssemblyContent> getAssemblyContents() {
		return assemblyContents;
	}

	public void setAssemblyContents(List<AssemblyContent> assemblyContents) {
		this.assemblyContents = assemblyContents;
	}

	/**
	 * Converts DB model item into DTO one.
	 * @param assemblyDB model item to convert
	 * @return DTO representation of model item
	 */
	public static Assembly create(AssemblyDB assemblyDB) {
		Assembly assembly = new Assembly();
		
		assembly.setUid(assemblyDB.getUid());
		assembly.setId(assemblyDB.getId());
		
		assembly.setTopologicallyValid(assemblyDB.isTopologicallyValid());
		assembly.setPdbInfo(assemblyDB.getPdbInfo()); 
		
		if(assemblyDB.getAssemblyScores() != null) {
			
			List<AssemblyScoreDB> assemblyScoreDBs = assemblyDB.getAssemblyScores();
			
			List<AssemblyScore> assemblyScores = new ArrayList<AssemblyScore>();
			
			for(AssemblyScoreDB assemblyScoreDB : assemblyScoreDBs) {
				AssemblyScore as = AssemblyScore.create(assemblyScoreDB);
				as.setAssembly(assembly);
				assemblyScores.add(as);
			}
			
			assembly.setAssemblyScores(assemblyScores);
		}
		
		if (assemblyDB.getAssemblyContents() != null) {
			List<AssemblyContentDB> assemblyContentDBs = assemblyDB.getAssemblyContents();
			
			List<AssemblyContent> assemblyContents = new ArrayList<AssemblyContent>();
			
			for (AssemblyContentDB assemblyContentDB : assemblyContentDBs) {
				AssemblyContent ac = AssemblyContent.create(assemblyContentDB);
				ac.setAssembly(assembly);
				assemblyContents.add(ac);
			}
			
			assembly.setAssemblyContents(assemblyContents);
		}
		
		// TODO initialise many-to-many relation to interface clusters
		
		return assembly;
	}
	
	
}
