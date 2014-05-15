package eppic.db;

import java.util.ArrayList;
import java.util.List;

import edu.uci.ics.jung.graph.util.Pair;

public class LatticeMatchMatrix {

	private double[][] coMatrix;
	
	private int numInterfacesFirst;
	private int numInterfacesSecond;
	
	public LatticeMatchMatrix(double[][] coMatrix) {
		this.coMatrix = coMatrix;
		
		this.numInterfacesFirst = this.coMatrix.length;
		this.numInterfacesSecond = this.coMatrix[0].length;
	}
	
	public void setCoMatrix(double[][] coMatrix) {
		this.coMatrix = coMatrix;
		
		this.numInterfacesFirst = this.coMatrix.length;
		this.numInterfacesSecond = this.coMatrix[0].length;
	}
	
	public double[][] getCoMatrix() {
		return this.coMatrix;
	}
	
	public List<Pair<Integer>> getMatchingPairs(double coCutoff) {
		List<Pair<Integer>> list = new ArrayList<Pair<Integer>>();
		for (int i=0;i<coMatrix.length;i++) {
			for (int j=0;j<coMatrix[i].length;j++) {
				if (coMatrix[i][j]>coCutoff) {
					list.add(new Pair<Integer>(i+1,j+1));
				}
			}
		}
		return list;
	}
	
	private List<Integer> getFirstMatches(List<Pair<Integer>> matchingPairs) {
		List<Integer> matches = new ArrayList<Integer>();
		for (Pair<Integer> pair:matchingPairs) {
			matches.add(pair.getFirst());
		}		
		return matches;
	}
	
	private List<Integer> getSecondMatches(List<Pair<Integer>> matchingPairs) {
		List<Integer> matches = new ArrayList<Integer>();
		for (Pair<Integer> pair:matchingPairs) {
			matches.add(pair.getSecond());
		}		
		return matches;
	}
	
	public LatticeOverlapScore getLatticeOverlapScore(double coCutoff) {
		return new LatticeOverlapScore(getTotalOverlapFirstSecond(coCutoff),getTotalOverlapSecondFirst(coCutoff));
	}
	
	private double getTotalOverlapFirstSecond(double coCutoff) {
		int matchCount = 0;
		List<Integer> firstMatches = getFirstMatches(getMatchingPairs(coCutoff));
		for (int i=0;i<numInterfacesFirst;i++) {
			if (firstMatches.contains(i+1)) matchCount++;
		}
		
		return (double)matchCount/(double)numInterfacesFirst;
	}
	
	private double getTotalOverlapSecondFirst(double coCutoff) {
		int matchCount = 0;
		List<Integer> secondMatches = getSecondMatches(getMatchingPairs(coCutoff));
		for (int i=0;i<numInterfacesSecond;i++) {
			if (secondMatches.contains(i+1)) matchCount++;
		}
		
		return (double)matchCount/(double)numInterfacesSecond;

	}

}
