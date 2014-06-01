package ch.systemsx.sybit.crkwebui.server.db.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.persistence.EntityManager;

import eppic.model.ChainClusterDB;
import eppic.model.SeqClusterDB;
import eppic.tools.SeqClusterer;
import gnu.getopt.Getopt;

public class ClusterSequences {
	
	
	
	public static void main(String[] args) throws Exception {

		String help = 
				"Usage: ClusterSequences \n" +
				" [-l]: sequence cluster levels to do (comma separated) \n"+
				"  -o : write output to database (by default nothing is written) \n"+
				" [-a]: number of threads (default 1)\n"; 
		
		
		int numThreads = 1;
		boolean writeToDb = false;
		int[] clusteringIds = {100,95,90,80,70,60,50,40,30};

		Getopt g = new Getopt("ClusterSequences", args, "l:a:oh?");
		int c;
		while ((c = g.getopt()) != -1) {
			switch(c){
			case 'l':
				String[] levelsStr = g.getOptarg().split(",");
				clusteringIds = new int[levelsStr.length];
				for (int i=0;i<levelsStr.length;i++) {
					clusteringIds[i]=Integer.parseInt(levelsStr[i]);
				}
				break;
			case 'o':
				writeToDb = true;
				break;
			case 'a':
				numThreads = Integer.parseInt(g.getOptarg());
				break;
			case 'h':
				System.out.println(help);
				System.exit(0);
				break;
			case '?':
				System.err.println(help);
				System.exit(1);
				break; // getopt() already printed an error
			}
		}
		
		
		DBHandler dbh = new DBHandler();
	
		Map<Integer, ChainClusterDB> allChains = dbh.getAllChainsWithRef();
		
		
		
		SeqClusterer sc = new SeqClusterer(allChains, numThreads);
		
		Map<Integer,Map<Integer,Integer>> allMaps = new TreeMap<Integer,Map<Integer,Integer>>();
		
		for (int clusteringId:clusteringIds) {
			
			List<List<String>> clustersList = sc.clusterThem(clusteringId);
		
			allMaps.put(clusteringId, getMapFromList(clustersList));
			//sc.writeClustersToFile(clustersList, new File(outDir,"clusters"+clusteringId+".table")); 
		}
		
		
		if (!writeToDb) return;
		
		
		System.out.println("Writing to db...");
		
		EntityManager entityManager = dbh.getEntityManager();
		
		for (int chainClusterId:allChains.keySet()) {
			SeqClusterDB seqCluster = new SeqClusterDB(
					allMaps.get(100).get(chainClusterId),
					allMaps.get(95).get(chainClusterId),
					allMaps.get(90).get(chainClusterId),
					allMaps.get(80).get(chainClusterId),
					allMaps.get(70).get(chainClusterId),
					allMaps.get(60).get(chainClusterId),
					allMaps.get(50).get(chainClusterId),
					allMaps.get(40).get(chainClusterId),
					allMaps.get(30).get(chainClusterId));
			
			ChainClusterDB chainCluster = allChains.get(chainClusterId);
			seqCluster.setChainCluster(chainCluster);
			seqCluster.setPdbCode(chainCluster.getPdbCode());
			seqCluster.setRepChain(chainCluster.getRepChain());
			
			//System.out.println(seqCluster.getC100()+" "+seqCluster.getC95()+" "+seqCluster.getC90()+" "+seqCluster.getC80()
			//		+" "+seqCluster.getC70()+" "+seqCluster.getC60()+" "+seqCluster.getC50()+" "+seqCluster.getC40()
			//		+" "+seqCluster.getC30()+" "+seqCluster.getPdbCode()+" "+seqCluster.getRepChain()+" "+seqCluster.getChainCluster().getUid());
			//seqClusters.add(seqCluster);
			
			entityManager.getTransaction().begin();
			entityManager.persist(seqCluster);
			entityManager.getTransaction().commit();
			
		}
		
		entityManager.close();
		System.out.println("Done writing "+allChains.size()+" SeqCluster records");
	}

	private static Map<Integer, Integer> getMapFromList(List<List<String>> clustersList) {
		Map<Integer, Integer> map = new HashMap<Integer,Integer>();
		
		for (int i=0;i<clustersList.size();i++) {
			for (String member:clustersList.get(i)) {
				map.put(Integer.parseInt(member), i+1);
			}
		}
		
		return map;
	}
	
	
}
