package ch.systemsx.sybit.crkwebui.shared.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import eppic.model.AssemblyContentDB;
import eppic.model.AssemblyDB;
import eppic.model.AssemblyScoreDB;
import eppic.model.InterfaceClusterDB;


public class Assembly implements Serializable  {
	
	private static final long serialVersionUID = 1L;

	private int uid;
	
	private int id;
	
	private boolean topologicallyValid;
		
	private List<InterfaceCluster> interfaceClusters;
	
	private List<AssemblyScore> assemblyScores;
	
	private List<AssemblyContent> assemblyContents;

	private String interfaceClusterIdsString;
	
	public Assembly() {
		this.interfaceClusters = new ArrayList<InterfaceCluster>();
		this.assemblyScores = new ArrayList<AssemblyScore>();
	}

	public List<Interface> getInterfaces(){
		List<Interface> interfaces = new ArrayList<Interface>();
		for(InterfaceCluster cluster : interfaceClusters){
			interfaces.addAll(cluster.getInterfaces());
		}
		return interfaces;
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

	public List<InterfaceCluster> getInterfaceClusters() {
		return interfaceClusters;
	}

	public void setInterfaceClusters(List<InterfaceCluster> interfaceClusters) {
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

	public String getInterfaceClusterIdsString() {
		return interfaceClusterIdsString;
	}
	
	public void setInterfaceClusterIdsString(String interfaceClusterIdsString) {
		this.interfaceClusterIdsString = interfaceClusterIdsString;
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
		assembly.setInterfaceClusterIdsString(assemblyDB.getInterfaceClusterIds());
		
		assembly.setTopologicallyValid(assemblyDB.isTopologicallyValid());
		
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
		
		// NOTE the interface clusters here will be new objects, thus no direct link to the actual objects  
		// created in InterfaceCluster class. 
		// The connection between them has to go through the uids 
		if (assemblyDB.getInterfaceClusters()!=null) {
			List<InterfaceCluster> interfaceClusters = new ArrayList<InterfaceCluster>();
			
			for (InterfaceClusterDB icDB:assemblyDB.getInterfaceClusters()) {
				interfaceClusters.add(InterfaceCluster.create(icDB));
			}
			assembly.setInterfaceClusters(interfaceClusters);
		}
		
		// The opposite relation (interfaceCluster to assembly) is not added here. 
		// It could be added in the initialisation of InterfaceCluster if needed at some point
		
		return assembly;
	}
	
	/**
	 * Get the list of interface cluster ids of all interface clusters belonging to this Assembly
	 * @return
	 */
	public List<Integer> getInterfaceClusterIds() {
		List<Integer> ids = new ArrayList<Integer>();
		for (InterfaceCluster ic:getInterfaceClusters()) {
			ids.add(ic.getClusterId());
		}
		return ids;
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
	
	//Not needed yet
	public String getCompositionString() {
		return "TODO Composition";
	}

	/**
	 * Returns the call corresponding to the eppic method
	 * @return
	 */
	public String getPredictionString(){
		
		AssemblyScore score = null;
		for (AssemblyScore as:assemblyScores) {
			if (as.getMethod().equals("eppic")) { 
				score = as;
			}
		}
		
		if (score==null) {
			return "NOPRED";
		}
		
		return score.getCallName().toUpperCase();
		
	}
	
	public String getChainIdsString() {
		// TODO this returns the first only, we should consider what to do with the others
		
		if (assemblyContents.size()==0) return null;
		return assemblyContents.get(0).getChainIds();
	}
	
	public String getIdentifierString(){
		return this.id+"";
	}
	
}
