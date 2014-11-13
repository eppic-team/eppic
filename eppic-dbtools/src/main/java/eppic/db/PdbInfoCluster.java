package eppic.db;

import java.util.HashSet;
import java.util.Set;


public class PdbInfoCluster implements Comparable<PdbInfoCluster> {

	private int id;
	
	//private PdbInfo representative;
	
	private HashSet<PdbInfo> members;
	
	public PdbInfoCluster(int id) {
		this.id = id;
		members = new HashSet<PdbInfo>();
	}
	
	public boolean addMember(PdbInfo member) {
		return members.add(member);
	}

	public int getId() {
		return id;
	}
	
	public Set<PdbInfo> getMembers() {
		return members;
	}
	
	public boolean equals(Object o) {
		if (!(o instanceof PdbInfoCluster)) return false;
		
		PdbInfoCluster other = (PdbInfoCluster)o;
		
		return this.id==other.id;
	}
	
	public int hashCode() {
		return new Integer(id).hashCode();
	}

	@Override
	public int compareTo(PdbInfoCluster o) {
		
		return new Integer(id).compareTo(o.id); 
	}
}
