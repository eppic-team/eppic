package eppic.db.tools.helpers;

import java.util.HashSet;
import java.util.Set;


public class GlobalPdbInfoCluster implements Comparable<GlobalPdbInfoCluster> {

	private int id;
	
	//private PdbInfo representative;
	
	private HashSet<PdbInfo> members;
	
	public GlobalPdbInfoCluster(int id) {
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
		if (!(o instanceof GlobalPdbInfoCluster)) return false;
		
		GlobalPdbInfoCluster other = (GlobalPdbInfoCluster)o;
		
		return this.id==other.id;
	}
	
	public int hashCode() {
		return new Integer(id).hashCode();
	}

	@Override
	public int compareTo(GlobalPdbInfoCluster o) {
		
		return new Integer(id).compareTo(o.id); 
	}
}
