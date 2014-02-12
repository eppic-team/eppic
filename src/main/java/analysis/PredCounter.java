package analysis;

import java.util.TreeMap;


public class PredCounter {
		
	private int total;
	
	private TreeMap<String,Integer> countBios;
	private TreeMap<String,Integer> countXtals;
	private TreeMap<String,Integer> subTotals;
	
	public PredCounter(String[] taxons) {
		
		countBios = new TreeMap<String, Integer>();
		countXtals = new TreeMap<String, Integer>();
		subTotals = new TreeMap<String, Integer>();
		
		for (String taxon:taxons) {
			countBios.put(taxon,0);
			countXtals.put(taxon,0);
			subTotals.put(taxon,0);
		}
		
	}
	
	public void count(String taxon) {
		subTotals.put(taxon,subTotals.get(taxon)+1);
	}
	
	public void countBio(String taxon) {
		countBios.put(taxon,countBios.get(taxon)+1);
	}
	
	public void countXtal(String taxon) {
		countXtals.put(taxon,countXtals.get(taxon)+1);
	}

	public int getCountBio(String taxon) {
		return countBios.get(taxon);
	}
	
	public int getCountXtal(String taxon) {
		return countXtals.get(taxon);
	}
	
	public int getTotalCountBio() {
		int sum = 0;
		for (int count:countBios.values()) {
			sum+=count;
		}
		return sum;
	}
	
	public int getTotalCountXtal() {
		int sum = 0;
		for (int count:countXtals.values()) {
			sum+=count;
		}
		return sum;		
	}
	
	public int getSubTotal(String taxon) {
		return subTotals.get(taxon);
	}
	
	public int getTotal() {
		return total;
	}
	
	public void setTotal(int total) {
		this.total = total;
	}
	
	public void setCountBio(String taxon, int count) {
		countBios.put(taxon,count);
	}

	public void setCountXtal(String taxon, int count) {
		countXtals.put(taxon,count);
	}

	
}
