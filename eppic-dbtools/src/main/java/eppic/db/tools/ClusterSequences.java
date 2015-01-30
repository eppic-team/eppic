package eppic.db.tools;

import eppic.model.ChainClusterDB;
import eppic.model.SeqClusterDB;
import gnu.getopt.Getopt;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ClusterSequences {
	
	
	// blastclust complains about too short sequences, any sequence <= than this value won't be used
	private static final int MIN_LENGTH = 8;
	
	/**
	 * The clustering identity levels, they have to be the exact same levels as in SeqClusterDB constructor
	 */
	private static final int[] CLUSTERING_IDS = {100,95,90,80,70,60,50,40,30};
	

		
	public static void main(String[] args) throws Exception {

		String help = 
				"Usage: ClusterSequences \n" +		
				"  -D         : the database name to use\n"+
				" [-a]        : number of threads (default 1)\n"+
				" [-s <file>] : a blastclust save file with precomputed blast for all chains\n"+
				"The database access parameters must be set in file "+DBHandler.CONFIG_FILE_NAME+" in home dir\n";
		
		
		int numThreads = 1;
		File saveFile = null;
		String dbName = null;

		
		Getopt g = new Getopt("ClusterSequences", args, "D:a:s:h?");
		int c;
		while ((c = g.getopt()) != -1) {
			switch(c){
			case 'D':
				dbName = g.getOptarg();
				break;
			case 'a':
				numThreads = Integer.parseInt(g.getOptarg());
				break;
			case 'h':
				System.out.println(help);
				System.exit(0);
				break;
			case 's':
				saveFile = new File(g.getOptarg());
				break;
			case '?':
				System.err.println(help);
				System.exit(1);
				break; // getopt() already printed an error
			}
		}
		
		if (dbName == null) {
			System.err.println("A database name must be provided with -D");
			System.exit(1);
		}
		
		DBHandler dbh = new DBHandler(dbName);
		
		boolean canDoUpdate = true;
		
		if (!dbh.checkSeqClusterEmpty()) {
			System.err.println("Warning! SeqCluster table is not empty. Will continue calculating clusters but won't be able to update database");
			//System.exit(1);
			canDoUpdate = false;
		}
		
		Map<Integer, ChainClusterDB> allChains = dbh.getAllChainsWithRef();
		System.out.println("Read "+allChains.size()+" chains having pdbCode and UniProt reference");
		filterShortSeqs(allChains, MIN_LENGTH);
		System.out.println(allChains.size()+" chains with length greater than "+MIN_LENGTH);
		
		SeqClusterer sc = null;
		if (saveFile!=null) {
			sc =new SeqClusterer(allChains, saveFile);
		} else {
			sc =new SeqClusterer(allChains, numThreads);
			System.out.println("blastclust save file stored in "+sc.getBlastclustSaveFile());
		}
		
		Map<Integer,Map<Integer,Integer>> allMaps = new TreeMap<Integer,Map<Integer,Integer>>();
		
		for (int clusteringId:CLUSTERING_IDS) {
			
			List<List<String>> clustersList = sc.clusterThem(clusteringId);
		
			if (saveFile!=null) {
				// if a save file was provided we need to make sure that it contains only the ids that we have in the database
				// and not others
				if (!checkIdsInList(clustersList, allChains)) {
					System.err.println("Some chainCluster_uids found in blastclust output are not in the database! Exiting");
					System.exit(1);
				}
			}
			
			allMaps.put(clusteringId, getMapFromList(clustersList));
			 
		}
		
		if (!canDoUpdate) {			
			System.out.println("Can't do database update because SeqCluster table is not empty.");
			System.out.println("Remove all records in SeqCluster table and then run again using the computed blastclust save file: -s "+sc.getBlastclustSaveFile());
			System.out.println("Exiting");
			System.exit(1);
		}
		
		System.out.println("Writing to db...");
		
		Map<Integer,Integer> map100 = allMaps.get(100);
		Map<Integer,Integer> map95 = allMaps.get(95);
		Map<Integer,Integer> map90 = allMaps.get(90);
		Map<Integer,Integer> map80 = allMaps.get(80);
		Map<Integer,Integer> map70 = allMaps.get(70);
		Map<Integer,Integer> map60 = allMaps.get(60);
		Map<Integer,Integer> map50 = allMaps.get(50);
		Map<Integer,Integer> map40 = allMaps.get(40);
		Map<Integer,Integer> map30 = allMaps.get(30);
		
		
		for (int chainClusterId:allChains.keySet()) {
			
			SeqClusterDB seqCluster = new SeqClusterDB(
					map100.get(chainClusterId),
					map95.get(chainClusterId),
					map90.get(chainClusterId),
					map80.get(chainClusterId),
					map70.get(chainClusterId),
					map60.get(chainClusterId),
					map50.get(chainClusterId),
					map40.get(chainClusterId),
					map30.get(chainClusterId));
			
			ChainClusterDB chainCluster = allChains.get(chainClusterId);
			seqCluster.setChainCluster(chainCluster);
			seqCluster.setPdbCode(chainCluster.getPdbCode());
			seqCluster.setRepChain(chainCluster.getRepChain());
			
			//System.out.println(seqCluster.getC100()+" "+seqCluster.getC95()+" "+seqCluster.getC90()+" "+seqCluster.getC80()
			//		+" "+seqCluster.getC70()+" "+seqCluster.getC60()+" "+seqCluster.getC50()+" "+seqCluster.getC40()
			//		+" "+seqCluster.getC30()+" "+seqCluster.getPdbCode()+" "+seqCluster.getRepChain()+" "+seqCluster.getChainCluster().getUid());
			//seqClusters.add(seqCluster);
			
			dbh.persistSeqCluster(seqCluster);
			
		}
		
		
		System.out.println("Done writing "+allChains.size()+" SeqCluster records");
	}

	/**
	 * Converts a list of clusters into a Map of member ids (chainCluster_uid) to 
	 * cluster ids (1 to n, index of list + 1)  
	 */
	private static Map<Integer, Integer> getMapFromList(List<List<String>> clustersList) {
		Map<Integer, Integer> map = new HashMap<Integer,Integer>();
		
		for (int i=0;i<clustersList.size();i++) {
			for (String member:clustersList.get(i)) {
				map.put(Integer.parseInt(member), i+1);
			}
		}
		
		return map;
	}
	
	private static void filterShortSeqs(Map<Integer, ChainClusterDB> allChains, int minLength) {
		Iterator<Integer> it = allChains.keySet().iterator();
		while (it.hasNext()) {
			if (allChains.get(it.next()).getPdbAlignedSeq().replaceAll("-",	"").length()<=minLength) 
				it.remove();;
		}
	}
	
	private static boolean checkIdsInList(List<List<String>> clustersList, Map<Integer, ChainClusterDB> allChains) {
		for (int i=0;i<clustersList.size();i++) {
			for (String member:clustersList.get(i)) {
					if (!allChains.containsKey(Integer.parseInt(member))) return false;
				}
			}
		return true;
	}
}
