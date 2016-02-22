package eppic.db;

import java.util.HashSet;
import java.util.Set;

/**
 * A cluster of interfaces, clustering similar interfaces across different PDB ids in the whole database. 
 * 
 * @author Jose Duarte
 */
public class GlobalInterfaceCluster implements Comparable<GlobalInterfaceCluster> {

	private int id;
	
	private HashSet<Interface> members;
	
	public GlobalInterfaceCluster(int id) {
		this.id = id;
		members = new HashSet<Interface>();
	}
	
	public boolean addMember(Interface member) {
		return members.add(member);
	}

	public int getId() {
		return id;
	}
	
	public Set<Interface> getMembers() {
		return members;
	}
	
	public boolean equals(Object o) {
		if (!(o instanceof GlobalInterfaceCluster)) return false;
		
		GlobalInterfaceCluster other = (GlobalInterfaceCluster)o;
		
		return this.id==other.id;
	}
	
	public int hashCode() {
		return new Integer(id).hashCode();
	}

	@Override
	public int compareTo(GlobalInterfaceCluster o) {
		
		return new Integer(id).compareTo(o.id); 
	}
}
