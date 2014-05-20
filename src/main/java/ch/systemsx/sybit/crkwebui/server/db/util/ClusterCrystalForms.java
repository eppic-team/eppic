package ch.systemsx.sybit.crkwebui.server.db.util;

//import java.io.File;
//import java.io.PrintWriter;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import owl.core.sequence.alignment.PairwiseSequenceAlignment.PairwiseSequenceAlignmentException;
import eppic.db.CFCompareMatrix;
import eppic.db.PdbInfo;
import eppic.db.PdbInfoCluster;
import eppic.db.PdbInfoList;
import eppic.db.SeqClusterLevel;
import eppic.model.PdbInfoDB;
import gnu.getopt.Getopt;

public class ClusterCrystalForms {

	private static final SeqClusterLevel DEFAULT_SEQ_CLUSTER_LEVEL = SeqClusterLevel.C50;
	private static final double DEFAULT_MIN_AREA = 10;
	private static final double DEFAULT_CO_CUTOFF = 0.2;
	private static final double DEFAULT_LOS_CLUSTER_CUTOFF = 0.8;

	public static void main(String[] args) throws Exception {

		String help = 
				"Usage: ClusterCrystalForms \n" +
				" -i : PDB code and chain e.g. 1abcA, all members in its same sequence cluster will be clustered\n"+
				" -l : sequence cluster level (default "+DEFAULT_SEQ_CLUSTER_LEVEL+")\n"+
				" -c : contact overlap score cutoff (default "+String.format("%3.1f",DEFAULT_CO_CUTOFF)+")\n"+
				" -a : minimum area cutoff (default "+String.format("%3.0f",DEFAULT_MIN_AREA)+")\n"+
				" -o : lattice overlap score cutoff for crystal-form clustering (default "+String.format("%3.1f",DEFAULT_LOS_CLUSTER_CUTOFF)+")\n"+
				" -A : ignore -i and calculate crystal-form clusters for all sequence clusters in DB\n";
				//" -f : file to write the crystal form cluster identifiers (only used in -A)\n"+
				//" -F : file to write the global interface cluster identifiers (only used in -A)\n";
				//" -d : print some debug output\n"; 


		String pdbString = null;
		
		//boolean debug = false;
		
		double coCutoff = DEFAULT_CO_CUTOFF;
		double minArea = DEFAULT_MIN_AREA;
		SeqClusterLevel seqClusterLevel = DEFAULT_SEQ_CLUSTER_LEVEL;
		double losClusterCutoff = DEFAULT_LOS_CLUSTER_CUTOFF;

		boolean calcAllClusters = false;
		//File cfClustersFile = null;
		//File interfClustersFile = null;
		
		Getopt g = new Getopt("ClusterCrystalForms", args, "i:c:a:l:o:Af:F:h?");
		int c;
		while ((c = g.getopt()) != -1) {
			switch(c){
			case 'i':
				pdbString = g.getOptarg();
				break;
			case 'c':
				coCutoff = Double.parseDouble(g.getOptarg());
				break;
			case 'a':
				minArea = Double.parseDouble(g.getOptarg());
				break;
			case 'l':
				seqClusterLevel = SeqClusterLevel.getByLevel(Integer.parseInt(g.getOptarg())); 
				break;		
			case 'o':
				losClusterCutoff = Double.parseDouble(g.getOptarg());
				break;
			case 'A':
				calcAllClusters = true;
				break;
//			case 'f':
//				cfClustersFile = new File(g.getOptarg());
//				break;
//			case 'F':
//				interfClustersFile = new File(g.getOptarg());
//				break;
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
		
		
		if (calcAllClusters == false && pdbString == null) {
			System.err.println("A PDB code/chain code have to be provided with -i");
			System.exit(1);
		}
		
//		if (calcAllClusters == true && (cfClustersFile == null || interfClustersFile == null)) {
//			System.err.println("If -A specified, then -f and -F needed");
//			System.exit(1);
//		}

		
		DBHandler dbh = new DBHandler();


//		PrintWriter cfcPw = null;
//		PrintWriter icPw = null;
//		if (cfClustersFile!=null) {
//			cfcPw = new PrintWriter(cfClustersFile);
//		}
//		if (interfClustersFile!=null) {
//			icPw = new PrintWriter(interfClustersFile);
//		}
		
		if (!calcAllClusters) {
			String pdbCode = pdbString.substring(0, 4);		
			String repChain = pdbString.substring(4); 

			int clusterId = dbh.getClusteIdForPdbCode(pdbCode, repChain, seqClusterLevel.getLevel());
			calcCluster(clusterId, dbh, seqClusterLevel, coCutoff, minArea, losClusterCutoff);
		} else {
			Set<Integer> clusterIds = dbh.getAllClusterIds(seqClusterLevel.getLevel());

			System.out.println("Total number of clusters (at "+seqClusterLevel.getLevel()+"% seq id): "+clusterIds.size());

			for (int clusterId:clusterIds) {
				calcCluster(clusterId, dbh, seqClusterLevel, coCutoff, minArea, losClusterCutoff);
			}
		}
		
//		if (cfClustersFile!=null) {
//			cfcPw.close();
//		}
//		if (interfClustersFile!=null) {
//			icPw.close();
//		}
	}
	
	private static void calcCluster(int clusterId, DBHandler dbh, SeqClusterLevel seqClusterLevel, double coCutoff, double minArea, double losClusterCutoff) 
			throws PairwiseSequenceAlignmentException {
		
		List<PdbInfoDB> pdbInfoList = dbh.deserializeSeqCluster(clusterId, seqClusterLevel.getLevel());

		PdbInfoList pdbList = new PdbInfoList(pdbInfoList);
		System.out.println ("Sequence cluster id ("+seqClusterLevel.getLevel()+"%): "+clusterId+" ("+pdbList.size()+" members)");

		long start = System.currentTimeMillis();
		CFCompareMatrix cfMatrix = pdbList.calcLatticeOverlapMatrix(seqClusterLevel, coCutoff, minArea);
		long end = System.currentTimeMillis();

		System.out.printf("%4s","");
		for (int i=0;i<pdbList.size();i++) {
			System.out.printf("%11s\t",pdbList.get(i).getPdbInfo().getPdbCode());
		}
		System.out.println();

		for (int i=0;i<cfMatrix.getMatrix().length;i++) {
			System.out.print(pdbList.get(i).getPdbInfo().getPdbCode());
			for (int j=0;j<cfMatrix.getMatrix()[i].length;j++) {
				if (cfMatrix.getMatrix()[i][j]==null) 
					System.out.printf("%11s\t","NA");
				else
					System.out.printf("%5.3f:%5.3f\t",cfMatrix.getMatrix()[i][j].getfAB(),cfMatrix.getMatrix()[i][j].getfBA());
			}
			System.out.println();
		}

		System.out.println();
		System.out.println();
		System.out.println("Done in "+((end - start)/1000)+" s");


		Collection<PdbInfoCluster> clusters = cfMatrix.getClusters(losClusterCutoff);
		System.out.println("Total number of clusters: "+clusters.size());
		for (PdbInfoCluster cluster:clusters) {
			System.out.print("Cluster "+cluster.getId()+": ");
			for (PdbInfo member:cluster.getMembers()) {
				System.out.print(member.getPdbInfo().getPdbCode()+" ");
			}
			System.out.println();
		}
	}
}
