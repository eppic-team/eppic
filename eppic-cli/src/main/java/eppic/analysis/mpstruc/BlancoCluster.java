package eppic.analysis.mpstruc;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class BlancoCluster implements Iterable<BlancoEntry> {
	
	private String name;
	//private BlancoEntry representative;
	private List<BlancoEntry> members;
	private String group;
	private String subgroup;
	
	public BlancoCluster() {
		this.members = new ArrayList<BlancoEntry>();
	}
	
	public void printTabular(PrintStream ps) {
		ps.println();
		ps.println(group+"\t"+subgroup+"\t"+name);
		for (BlancoEntry member:this) {
			member.printTabular(ps);
		}
	}
	
	public void addMember(BlancoEntry entry) {
		members.add(entry);
	}
	
//	public BlancoEntry getRepresentative() {
//		return representative;
//	}
//	
//	public void setRepresentative(BlancoEntry representative) {
//		this.representative = representative;
//	}
	
	public String getGroup() {
		return group;
	}
	
	public void setGroup(String group) {
		this.group = group;
	}
	
	public String getSubgroup() {
		return subgroup;
	}
	
	public void setSubgroup(String subgroup) {
		this.subgroup = subgroup;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public Iterator<BlancoEntry> iterator() {
		return members.iterator();
	}
	
	public int size() {
		return members.size()+1;
	}
	
	public BlancoEntry getBestResolutionMember() {
		List<BlancoEntry> entries = new ArrayList<BlancoEntry>();
		entries.addAll(members);
		Collections.sort(entries);
		for (BlancoEntry entry:entries) {
			if (entry.getResolution()>0) return entry;
		}
		return null;
		
	}
	
	public void removeEntriesWithNoPdbData() {
		Iterator<BlancoEntry> it = members.iterator();
		while (it.hasNext()) {
			BlancoEntry next = it.next();
			if (!next.hasPdbData()) it.remove();
		}
	}
	
	public void removeNonXrayEntries() {
		Iterator<BlancoEntry> it = members.iterator();
		while (it.hasNext()) {
			BlancoEntry next = it.next();
			if (next.getExpMethod()==null) {
				System.err.println("Warning: experimental method for "+next.getPdbCode()+" is null. Removing entry");
				it.remove();
				continue;
			}
			if (!next.getExpMethod().equals("X-RAY DIFFRACTION")) it.remove();
		}
	}

}
