package eppic.db;

import java.util.HashSet;
import java.util.Set;

public class InterfaceCluster implements Comparable<InterfaceCluster> {

	private int id;
	
	private HashSet<Interface> members;
	
	public InterfaceCluster(int id) {
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
		if (!(o instanceof InterfaceCluster)) return false;
		
		InterfaceCluster other = (InterfaceCluster)o;
		
		return this.id==other.id;
	}
	
	public int hashCode() {
		return new Integer(id).hashCode();
	}

	@Override
	public int compareTo(InterfaceCluster o) {
		
		return new Integer(id).compareTo(o.id); 
	}
}
