package eppic.db.tools;

import java.io.File;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eppic.db.tools.helpers.GlobalInterfaceCluster;
import eppic.db.tools.helpers.GlobalPdbInfoCluster;
import eppic.db.tools.helpers.InterfaceCluster;
import eppic.db.tools.helpers.LatticeComparisonGroup;
import eppic.db.tools.helpers.PdbInfo;
import eppic.db.tools.helpers.PdbInfoList;
import eppic.db.tools.helpers.SeqClusterLevel;
import eppic.model.ChainClusterDB;
import eppic.model.InterfaceDB;
import eppic.model.PdbInfoDB;
import gnu.getopt.Getopt;

public class ClusterCrystalForms {

	private static final SeqClusterLevel DEFAULT_SEQ_CLUSTER_LEVEL = SeqClusterLevel.C50;
	private static final double DEFAULT_MIN_AREA = 10;
	private static final double DEFAULT_CO_CUTOFF = 0.2;
	private static final double DEFAULT_LOS_CLUSTER_CUTOFF = 0.8;
	
	private static int crystalFormId = 1;
	private static int interfClusterGlobalId = 1;

	private static boolean debug = false;

	public static void main(String[] args) throws Exception {

		String help = 
				"Usage: ClusterCrystalForms \n" +
				" -D <string> : the database name to use\n"+
				" -i <string> : PDB code and chain e.g. 1abcA, all members in its same sequence cluster will be clustered\n"+
				" [-l <int>]  : sequence cluster level (default "+DEFAULT_SEQ_CLUSTER_LEVEL+")\n"+
				" [-c <float>]: contact overlap score cutoff (default "+String.format("%3.1f",DEFAULT_CO_CUTOFF)+")\n"+
				" [-a <float>]: minimum area cutoff (default "+String.format("%3.0f",DEFAULT_MIN_AREA)+")\n"+
				" [-s <float>]: lattice overlap score cutoff for crystal-form clustering (default "+String.format("%3.1f",DEFAULT_LOS_CLUSTER_CUTOFF)+")\n"+
				" [-A]        : ignore -i and calculate crystal-form clusters for all sequence clusters in DB\n"+
				" [-f <file>] : file to write the crystal form cluster identifiers (only used in -A)\n"+
				" [-F <file>] : file to write the global interface cluster identifiers (only used in -A)\n"+
				" [-d]        : print some debug output (full lattice comparison and interfaces comparison matrices)\n"+
				" [-g <file>] : a configuration file containing the database access parameters, if not provided\n" +
				"               the config will be read from file "+DBHandler.DEFAULT_CONFIG_FILE_NAME+" in home dir\n";
		
		String pdbString = null;
		
		double coCutoff = DEFAULT_CO_CUTOFF;
		double minArea = DEFAULT_MIN_AREA;
		SeqClusterLevel seqClusterLevel = DEFAULT_SEQ_CLUSTER_LEVEL;
		double losClusterCutoff = DEFAULT_LOS_CLUSTER_CUTOFF;

		boolean calcAllClusters = false;
		File cfClustersFile = null;
		File interfClustersFile = null;
		String dbName = null;
		File configFile = null;
		
		
		Getopt g = new Getopt("ClusterCrystalForms", args, "D:i:c:a:l:s:Af:F:dg:h?");
		int c;
		while ((c = g.getopt()) != -1) {
			switch(c){
			case 'D':
				dbName = g.getOptarg();
				break;
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
			case 's':
				losClusterCutoff = Double.parseDouble(g.getOptarg());
				break;
			case 'A':
				calcAllClusters = true;
				break;
			case 'f':
				cfClustersFile = new File(g.getOptarg());
				break;
			case 'F':
				interfClustersFile = new File(g.getOptarg());
				break;
			case 'd':
				debug = true;
				break;
			case 'g':
				configFile = new File(g.getOptarg());
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
		
		if (dbName == null) {
			System.err.println("A database name must be provided with -D");
			System.exit(1);
		}
		
		if (calcAllClusters == false && pdbString == null) {
			System.err.println("A PDB code + chain id have to be provided with -i, e.g. 1b8gA");
			System.exit(1);
		}
		
		if (calcAllClusters == true && (cfClustersFile == null || interfClustersFile == null)) {
			System.err.println("If -A specified, then -f and -F needed");
			System.exit(1);
		}
		
		if (!calcAllClusters) {
			cfClustersFile = null;
			interfClustersFile = null;
			if (pdbString.length()<4) {
				System.err.println("A PDB code + chain id have to be provided with -i, e.g. 1b8gA");
				System.exit(1);
			}
		}

		// note if configFile is null, DBHandler will use a default location in user's home dir
		DBHandler dbh = new DBHandler(dbName, configFile);	

		PrintWriter cfcPw = null;
		PrintWriter icPw = null;
		if (cfClustersFile!=null) {
			cfcPw = new PrintWriter(cfClustersFile);
			cfcPw.println("# pdbInfo_uid\tpdb\tC"+seqClusterLevel.getLevel()+"\tcfCluster\tcrystalFormId");
		}
		if (interfClustersFile!=null) {
			icPw = new PrintWriter(interfClustersFile);
			icPw.println("# interfaceItem_uid\tpdb\tinterfaceId\tC"+seqClusterLevel.getLevel()+"\tcfCluster\tglobalInterfClusterId");
		}
		
		if (!calcAllClusters) {
			Matcher match = Pattern.compile("^(....)\\.?(.*)$").matcher(pdbString);
			if( !match.matches() ) {
				System.err.println("Invalid PDB code: "+pdbString);
				System.exit(1); return;
			}
			String pdbCode = match.group(1);
			String repChain = match.group(2);
			
			// try to guess chain
			if(repChain == null || repChain.isEmpty()) {
				PdbInfoDB pdbInfo = dbh.deserializePdb(pdbCode);
				List<ChainClusterDB> clusters = pdbInfo.getChainClusters();
				// Handle single chain PDBs
				repChain = clusters.get(0).getRepChain();
				if(clusters.size() > 1) {
					// Ambiguous chain
					System.err.format("PDB %s contains %d unique chains. Please specify one, e.g. %s%s",pdbCode,clusters.size(),pdbCode,repChain);
					System.exit(1); return;
				}
			}

			int seqClusterId = dbh.getClusterIdForPdbCodeAndChain(pdbCode, repChain, seqClusterLevel.getLevel());
			
			if (seqClusterId==-1) {
				System.err.println("Could not find sequence cluster (at "+seqClusterLevel.getLevel()+"%) for "+pdbCode+" and representative chain "+repChain);
				System.err.println("Make sure that the PDB code is in the database and that the representative chain id is valid");
			} else {
			
				calcCluster(seqClusterId, dbh, seqClusterLevel, coCutoff, minArea, losClusterCutoff, null, null);
			}
			
		} else {
			Set<Integer> clusterIds = dbh.getAllClusterIds(seqClusterLevel.getLevel());

			System.out.println("Total number of clusters (at "+seqClusterLevel.getLevel()+"% seq id): "+clusterIds.size());

			for (int clusterId:clusterIds) {
				calcCluster(clusterId, dbh, seqClusterLevel, coCutoff, minArea, losClusterCutoff, cfcPw, icPw);
			}
		}
		
		if (cfClustersFile!=null) {
			cfcPw.close();
		}
		if (interfClustersFile!=null) {
			icPw.close();
		}
	}
	
	private static void calcCluster(int seqClusterId, DBHandler dbh, SeqClusterLevel seqClusterLevel, double coCutoff, double minArea, double losClusterCutoff,
			PrintWriter cfcPw, PrintWriter icPw) 
			{
		
		long start = System.currentTimeMillis();
		List<PdbInfoDB> pdbInfoList = dbh.deserializeSeqCluster(seqClusterId, seqClusterLevel.getLevel());
		long end = System.currentTimeMillis();		

		PdbInfoList pdbList = new PdbInfoList(pdbInfoList);
		pdbList.setMinArea(minArea);
		
		System.out.println ("### Sequence cluster id ("+seqClusterLevel.getLevel()+"%): "+seqClusterId+" ("+pdbList.size()+" members)");
		System.out.println("Done deserialization in "+((end - start)/1000)+" s");
		
		start = System.currentTimeMillis();
		LatticeComparisonGroup cfMatrix = pdbList.calcLatticeOverlapMatrix(seqClusterLevel, coCutoff);
		end = System.currentTimeMillis();

		
		// the lattice comparison matrix
		if (debug) {
			System.out.printf("%4s","");
			for (int i=0;i<pdbList.size();i++) {
				System.out.printf("%11s\t",pdbList.get(i).getPdbInfo().getPdbCode());
			}
			System.out.println();

			for (int i=0;i<cfMatrix.getLatticeComparisonMatrix().length;i++) {
				System.out.print(pdbList.get(i).getPdbInfo().getPdbCode());
				for (int j=0;j<cfMatrix.getLatticeComparisonMatrix()[i].length;j++) {
					if (cfMatrix.getLatticeComparisonMatrix()[i][j]==null) 
						System.out.printf("%11s\t","NA");
					else
						System.out.printf("%5.3f:%5.3f\t",cfMatrix.getLatticeComparisonMatrix()[i][j].getfAB(),cfMatrix.getLatticeComparisonMatrix()[i][j].getfBA());
				}
				System.out.println();
			}

			System.out.println();
		}
		System.out.println("Done comparison in "+((end - start)/1000)+" s");

		
		
		// crystal form clusters
		System.out.println("Calculating crystal form clusters...");
		start = System.currentTimeMillis();
		Collection<GlobalPdbInfoCluster> cfClusters = cfMatrix.getCFClusters(losClusterCutoff);
		end = System.currentTimeMillis();
		System.out.println("Crystal form clusters calculated in "+((end - start)/1000)+" s");
		System.out.println("Total number of crystal form clusters: "+cfClusters.size());
		for (GlobalPdbInfoCluster cfCluster:cfClusters) {
			System.out.print("Cluster "+cfCluster.getId()+": ");			
			
			for (PdbInfo member:cfCluster.getMembers()) {
				
				member.getPdbInfo().setCrystalFormId(crystalFormId);
				
				System.out.print(member.getPdbInfo().getPdbCode()+" ");
				
				if (cfcPw !=null ) cfcPw.println( member.getPdbInfo().getUid()+"\t"+
												member.getPdbInfo().getPdbCode()+"\t"+
												seqClusterId+"\t"+
												cfCluster.getId()+"\t"+
												crystalFormId);
			}
			crystalFormId++;
			System.out.println();
			if (cfcPw !=null ) cfcPw.flush();
		}
		
		
		// the interface comparison mega-matrix (only printed in debug mode)
		if (debug) {
			System.out.printf("%7s","");
			for (int i=0;i<pdbList.getNumInterfaceClusters();i++) {
				InterfaceCluster iInterf = pdbList.getInterfaceCluster(i);
				System.out.printf("%4s:%02d\t", iInterf.getRepresentative().getInterface().getPdbCode(),iInterf.getInterfaceClusterDB().getClusterId());
			}
			System.out.println();
			for (int i=0;i<cfMatrix.getInterfaceComparisonMatrix().length;i++) {
				InterfaceCluster iInterf = pdbList.getInterfaceCluster(i);
				System.out.printf("%4s:%02d",iInterf.getRepresentative().getInterface().getPdbCode(),iInterf.getInterfaceClusterDB().getClusterId() );
				for (int j=0;j<cfMatrix.getInterfaceComparisonMatrix()[i].length;j++) {
					System.out.printf("%7.3f\t",cfMatrix.getInterfaceComparisonMatrix()[i][j]);
				}
				System.out.println();
			}
		}
		
		// interface clusters
		System.out.println("Calculating interface clusters...");
		start = System.currentTimeMillis();
		Collection<GlobalInterfaceCluster> interfClusters = cfMatrix.getInterfClusters(coCutoff);
		end = System.currentTimeMillis();
		System.out.println("Interfaces clusters calculated in "+((end - start)/1000)+" s");
		System.out.println("Total number of interface clusters: "+interfClusters.size());
		for (GlobalInterfaceCluster cluster:interfClusters) {

			// first we find the number of distinct crystal forms in cluster to print it in the header of each cluster
			Set<Integer> distinctCFsInCluster = new HashSet<Integer>();
			for (InterfaceCluster member:cluster.getMembers()) {
				distinctCFsInCluster.add(member.getRepresentative().getInterface().getInterfaceCluster().getPdbInfo().getCrystalFormId());
			}
			
			System.out.printf("Cluster %3d, in %2d/%2d (%4.2f) of CFs: ",
					cluster.getId(), 
					distinctCFsInCluster.size(), cfClusters.size(), 
					(double)distinctCFsInCluster.size()/(double)cfClusters.size() );

			for (InterfaceCluster member:cluster.getMembers()) {
				distinctCFsInCluster.add(member.getRepresentative().getInterface().getInterfaceCluster().getPdbInfo().getCrystalFormId());
				
				// we identify the interface cluster by its id, with the member interfaces in square brackets 
				StringBuilder interfaceId = new StringBuilder(String.valueOf(member.getInterfaceClusterDB().getClusterId()) + "[");
				int i = 0;
				for (InterfaceDB idb: member.getInterfaceClusterDB().getInterfaces()) {
					if (i!=0) interfaceId.append(",");
					interfaceId.append(idb.getInterfaceId());
					i++;
				}
				interfaceId.append("]");
				
				System.out.print(member.getRepresentative().getInterface().getPdbCode()+"-"+interfaceId+" ");
				
				if (icPw !=null ) icPw.println( member.getInterfaceClusterDB().getUid()+"\t"+
												member.getInterfaceClusterDB().getPdbCode()+"\t"+
												member.getInterfaceClusterDB().getClusterId()+"\t"+
												seqClusterId+"\t"+
												cluster.getId()+"\t"+
												interfClusterGlobalId);
			}
			System.out.println();
			interfClusterGlobalId++;
			
			if (icPw !=null ) icPw.flush();
		}
	}
}
