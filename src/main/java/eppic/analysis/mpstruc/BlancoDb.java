package eppic.analysis.mpstruc;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class BlancoDb implements Iterable<BlancoCluster> {

	private Date timeStamp;
	
	private List<BlancoCluster> list;
	
	public BlancoDb() {
		this.list = new ArrayList<BlancoCluster>();
	}
	
	public void addCluster(BlancoCluster cluster) {
		this.list.add(cluster);
	}

	public Date getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(Date timeStamp) {
		this.timeStamp = timeStamp;
	}

	@Override
	public Iterator<BlancoCluster> iterator() {
		return list.iterator();
	}
	
	public void printTabular(PrintStream ps) {
		for (BlancoCluster cluster:this) {
			cluster.printTabular(ps);
		}
	}
	
	public int getNumberClusters() {
		return list.size();				
	}
	
	public void retrievePdbData(File cifDir) {
		for (BlancoCluster cluster:this) {
			for (BlancoEntry entry:cluster) {
				entry.retrievePdbData(cifDir);
			}
		}
	}
	
	public void removeEntriesWithNoPdbData() {
		for (BlancoCluster cluster:this) {			
			cluster.removeEntriesWithNoPdbData();
		}		
	}
	
	public void removeNonXrayEntries() {
		for (BlancoCluster cluster:this) {			
			cluster.removeNonXrayEntries();
		}		
	}
	
	
	public List<BlancoEntry> getBelowResolution(double resolution) {
		List<BlancoEntry> list = new ArrayList<BlancoEntry>();
		for (BlancoCluster cluster:this) {
			BlancoEntry best = cluster.getBestResolutionMember();
			if (best!=null && best.getResolution()<resolution) 
				list.add(best);
		}
		return list;
	}
	
	public int getNumberClustersBelowResolution(double resolution) {
		int total = 0;
		for (BlancoCluster cluster:this) {
			if (cluster.getBestResolutionMember()!=null && 
				cluster.getBestResolutionMember().getResolution()<resolution) 
				total++;
		}
		return total;
	}

	public int getNumberClustersBelowResolutionAndRfree(double resolution, double rFree) {
		int total = 0;
		for (BlancoCluster cluster:this) {
			if (cluster.getBestResolutionMember()!=null && 
				cluster.getBestResolutionMember().getResolution()<resolution &&
				cluster.getBestResolutionMember().getrFree()>0 && cluster.getBestResolutionMember().getrFree()<rFree) 
				total++;
		}
		return total;
	}

	public int size() {
		int total = 0;
		for (BlancoCluster cluster:this) {
			total+=cluster.size();
		}
		return total;
	}
}
