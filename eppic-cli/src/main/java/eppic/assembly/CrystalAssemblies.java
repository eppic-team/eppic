package eppic.assembly;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CrystalAssemblies implements Iterable<Assembly> {
	
	
	private static final Logger logger = LoggerFactory.getLogger(CrystalAssemblies.class);
	

	/**
	 * The set of all valid assemblies in the crystal.
	 */
	private Set<Assembly> all;
	
	/**
	 * The reduced list of assemblies where equivalent assemblies are represented by the
	 * maximal one (the one with most engaged interface clusters)
	 */
	private List<Assembly> clusteredList;
	
	private Map<Integer,AssemblyGroup> groups;
	
	public CrystalAssemblies(Set<Assembly> validAssemblies) {
		this.all = validAssemblies;
		
		initGroups();
		
		initClusters();
		
		// not clustered would be like this: (commenting out initGroups and initClusters)
		//clusteredList = new ArrayList<Assembly>();
		//clusteredList.addAll(all);
		
	}
	
	public int size() {
		return clusteredList.size();
	}
	
	private void initGroups() {
		this.groups = new TreeMap<Integer, AssemblyGroup>();

		for (Assembly assembly:all) {

			StoichiometrySet stoSet = assembly.getStoichiometrySet();

			// we classify into groups those that are fully covering

			if (stoSet.isFullyCovering()) {
				// we assume they are valid, which implies even stoichiometry (thus the size of first will give the size for all)			
				int size = stoSet.getFirst().getCountForIndex(0);

				if (!groups.containsKey(size)) {
					AssemblyGroup group = new AssemblyGroup();
					groups.put(size, group);
					group.add(assembly);
				} else {
					groups.get(size).add(assembly);
				}
			} else {
				logger.info("Assembly {} will not be clustered since it doesn't cover all entities",assembly.toString());
				if (!groups.containsKey(-1)) {
					AssemblyGroup group = new AssemblyGroup();
					group.add(assembly);
					groups.put(-1, group);
				} else {
					groups.get(-1).add(assembly);
				}
			}
		}
	}
	
	private void initClusters() {
		
		clusteredList = new ArrayList<Assembly>();
		
		for (int size:groups.keySet()) {
			AssemblyGroup ag = groups.get(size);

			if (size>0) {
				List<AssemblyGroup> clusters = ag.sortIntoClusters();

				logger.debug("{} assemblies with size {} group into {} clusters",ag.size(),size,clusters.size());


				for (int i=0;i<clusters.size();i++) {

					if (clusters.get(i).size()>1) 
						logger.info("Using assembly {} as representative for assembly cluster {}",clusters.get(i).get(0),clusters.get(i));

					// we use the first member of each cluster (which is the maximal group, see 
					// AssemblyGroup.sortIntoClusters() ) as the representative
					clusteredList.add(clusters.get(i).get(0)); 
				}
			} else {
				// for those in the "-1" group we just add each assembly as a single-member cluster
				for (Assembly assembly:ag) {
					clusteredList.add(assembly);
				}
			}
		}
	}

	@Override
	public Iterator<Assembly> iterator() {
		
		return clusteredList.iterator();
	}
	
	public Set<Assembly> getAllAssemblies() {
		return all;
	}
	
	public List<Assembly> getClusteredAssemblies() {
		return clusteredList;
	}
	
	
	
}
