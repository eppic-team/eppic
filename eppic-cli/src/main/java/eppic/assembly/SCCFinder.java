package eppic.assembly;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedOrderedSparseMultigraph;

/**
 * A Strong Connected Component finder from: http://sourceforge.net/p/jung/discussion/252062/thread/b33ed334
 * 
 * Thanks anonymous Daniel
 */
public class SCCFinder<V,E> {
	

    private int index;
    private ArrayList<V> stack;
    
    private HashMap<V, Integer> indexHash;
    private HashMap<V, Integer> lowlinkHash;

    public SCCFinder() {
    	index = 0;
    	stack = new ArrayList<V>();
    	indexHash = new HashMap<V, Integer>();
    	lowlinkHash = new HashMap<V, Integer>();
    }
    
    public List<List<V>> tarjan(V v, DirectedGraph<V, E> source) {
        for (V node : source.getVertices()) {
            indexHash.put(node, -1);
            lowlinkHash.put(node, -1);
        }
        return tarjan_internal(v, source);
    }
    
    private List<List<V>> tarjan_internal(V v, DirectedGraph<V, E> source) {
    	
    	List<List<V>> SCC = new ArrayList<List<V>>();
        
    	indexHash.put(v, index);
        lowlinkHash.put(v, index);
        index++;
        stack.add(0, v);
        for (V n : source.getSuccessors(v)) {
            if (indexHash.get(n) == -1) {
                tarjan_internal(n, source);
                lowlinkHash.put(v, Math.min(lowlinkHash.get(v), lowlinkHash.get(n)));
            } else if (stack.contains(n)) {
                lowlinkHash.put(v, Math.min(lowlinkHash.get(v), indexHash.get(n)));
            }
        }
        if (lowlinkHash.get(v).equals(indexHash.get(v))) {
            V n;
            List<V> component = new ArrayList<V>();
            do {
                n = stack.remove(0);
                component.add(n);
            } while (n != v);
            SCC.add(component);
        }
        return SCC;
    }
    
    private static void printSCC(String node, DirectedGraph<String,Integer> graph) {
    	
    	SCCFinder<String,Integer> f = new SCCFinder<String,Integer>();
    	
    	List<List<String>> sccs = f.tarjan(node, graph);
    	
    	System.out.println("Size of SCC list for node "+node+": "+sccs.size());
    	
    	for (List<String> scc:sccs) {
    		System.out.println("Connected component: ");
    		for (String v:scc) {
    			System.out.print(v+" ");
    		}
    		System.out.println();
    	}
    }
    
    public static void main (String[] args) {
    	
    	DirectedGraph<String, Integer> graph = new DirectedOrderedSparseMultigraph<String, Integer>();
    	
    	graph.addEdge(1, "A0", "A1");
    	graph.addEdge(2, "A1", "A0");
    	graph.addEdge(3, "A1", "A2");
    	graph.addEdge(7, "A2", "A0");
    	
    	graph.addEdge(4, "B0", "B1");
    	graph.addEdge(5, "B1", "B2");
    	graph.addEdge(6, "B2", "B0");
    	
    	printSCC("A0", graph);
    	printSCC("A1", graph);
    	printSCC("A2", graph);
    	
    	printSCC("B0", graph);

    }
}