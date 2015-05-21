package eppic.assembly;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A representation of all valid assemblies in the crystal.
 * 
 * @author duarte_j
 *
 */
public class CrystalAssemblies implements Iterable<Assembly> {
	
	
	private static final Logger logger = LoggerFactory.getLogger(CrystalAssemblies.class);
	

	/**
	 * The set of all valid assemblies in the crystal.
	 */
	private Set<Assembly> all;	
	
	/**
	 * Each of the clusters of equivalent assemblies.
	 * The representative is the first member and the maximal Assembly in terms of number of engaged interface clusters.
	 */
	private List<AssemblyGroup> clusters;
	
	private Map<Integer,AssemblyGroup> groups;
	
	public CrystalAssemblies(Set<Assembly> validAssemblies) {
		this.all = validAssemblies;
		
		initGroups();
		
		initClusters();
		
		
	}
	
	public int size() {
		return clusters.size();
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
		
		clusters = new ArrayList<AssemblyGroup>();
		
		for (int size:groups.keySet()) {
			AssemblyGroup ag = groups.get(size);

			if (size>0) {
				List<AssemblyGroup> clustersForGroup = ag.sortIntoClusters();

				logger.debug("{} assemblies with size {} group into {} clusters",ag.size(),size,clustersForGroup.size());


				for (int i=0;i<clustersForGroup.size();i++) {

					if (clustersForGroup.get(i).size()>1) 
						logger.info("Using assembly {} as representative for assembly cluster {}",clustersForGroup.get(i).get(0),clustersForGroup.get(i));

					this.clusters.add(clustersForGroup.get(i)); 
				}
			} else {
				// for those in the "-1" group we just add each assembly as a single-member cluster
				for (Assembly assembly:ag) {
					AssemblyGroup g = new AssemblyGroup();
					g.add(assembly);
					clusters.add(g);
				}
			}
		}
	}

	@Override
	public Iterator<Assembly> iterator() {
		
		return getUniqueAssemblies().iterator();
	}
	
	/**
	 * Get all valid assemblies in the crystal (unclustered)
	 * @return
	 * @see #getUniqueAssemblies()
	 */
	public Set<Assembly> getAllAssemblies() {
		return all;
	}
	
	/**
	 * Returns the list of unique valid assemblies in the crystal, that is the representatives 
	 * of each of the assembly clusters. The representatives are chosen to be those assemblies that
	 * have maximal number of engaged interface clusters out of the group of equivalent assemblies 
	 * in the assembly cluster.
	 * @return
	 */
	public List<Assembly> getUniqueAssemblies() {
		List<Assembly> representatives = new ArrayList<Assembly>();
		for (AssemblyGroup cluster:clusters) {
			// we use the first member of each cluster (which is the maximal group, see 
			// AssemblyGroup.sortIntoClusters() ) as the representative
			representatives.add(cluster.get(0));
		}
		return representatives;
	}
	
	public List<AssemblyGroup> getClusters() {
		return clusters;
	}
	
	
}
