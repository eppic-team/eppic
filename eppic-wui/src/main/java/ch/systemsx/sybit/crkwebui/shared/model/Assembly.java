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
	
	private PdbInfoDB pdbInfo;
	
	private Set<InterfaceCluster> interfaceClusters;
	
	private List<AssemblyScore> assemblyScores;
	
	private List<AssemblyContent> assemblyContents;

	private String interfaceClusterIds;
	
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

	public String getInterfaceClusterIds() {
		return interfaceClusterIds;
	}
	
	public void setInterfaceClusterIds(String interfaceClusterIds) {
		this.interfaceClusterIds = interfaceClusterIds;
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
		assembly.setInterfaceClusterIds(assemblyDB.getInterfaceClusterIds());
		
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
	
	public String getSymmetryString() {
		StringBuilder sb = new StringBuilder();
		for (int i=0;i<getAssemblyContents().size();i++	) {
			sb.append(getAssemblyContents().get(i).getSymmetry());
			if (i!=getAssemblyContents().size()-1) sb.append(",");
		}
		return sb.toString();
	}
	
	public String getStoichiometryString() {
		StringBuilder sb = new StringBuilder();
		for (int i=0;i<getAssemblyContents().size();i++	) {
			sb.append(getAssemblyContents().get(i).getStoichiometry());
			if (i!=getAssemblyContents().size()-1) sb.append(",");
		}
		return sb.toString();
	}
	
	public String getMmSizeString() {
		StringBuilder sb = new StringBuilder();
		for (int i=0;i<getAssemblyContents().size();i++	) {
			sb.append(getAssemblyContents().get(i).getMmSize());
			if (i!=getAssemblyContents().size()-1) sb.append(",");
		}
		return sb.toString();
	}
	
}
