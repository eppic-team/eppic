package eppic.db.tools;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.biojava.nbio.structure.contact.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eppic.db.ChainCluster;
import eppic.db.Interface;
import eppic.db.InterfaceCluster;
import eppic.db.PdbInfo;
import eppic.db.SeqClusterLevel;
import eppic.model.InterfaceClusterDB;
import eppic.model.InterfaceDB;
import eppic.model.PdbInfoDB;
import gnu.getopt.Getopt;

public class EstimateInterfaceComparisons {
	private static final SeqClusterLevel DEFAULT_SEQ_CLUSTER_LEVEL = SeqClusterLevel.C30;
	private static final Logger logger = LoggerFactory.getLogger(EstimateInterfaceComparisons.class);

	public static void main(String[] args) {
		String help = 
				"Usage: FindBiomimics \n" +		
				"  -D         : the database name to use\n"+
				"  -l         : sequence cluster level to be used (default "+DEFAULT_SEQ_CLUSTER_LEVEL.getLevel()+"\n"+
				"  -o         : Output file, in tsv format giving the sequence cluster IDs and number of interfaces.\n"+
				"The database access parameters must be set in file "+DBHandler.CONFIG_FILE_NAME+" in home dir\n";
		
		
		String dbName = null;
		SeqClusterLevel seqClusterLevel = DEFAULT_SEQ_CLUSTER_LEVEL;
		String outputFilename = null;
		
		Getopt g = new Getopt("FindBiomimics", args, "D:l:o:h?");
		int c;
		while ((c = g.getopt()) != -1) {
			switch(c){
			case 'D':
				dbName = g.getOptarg();
				break;
			case 'l':
				seqClusterLevel = SeqClusterLevel.getByLevel(Integer.parseInt(g.getOptarg())); 
				break;
			case 'o':
				outputFilename = g.getOptarg();
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
		
		DBHandler dbh = new DBHandler(dbName);
		
		
		// Represent interfaces by two (sorted) SeqCluster uid's
		// Store the number of interfaces in each
		Map<Pair<Integer>,Integer> interfaceCounts = new HashMap<>();
		
		
		// Count interfaces between each clusterpair
		List<String> allPDBs = dbh.getAllPdbCodes();
		logger.info("Analyzing {} structures",allPDBs.size());
		int pdbNum = 0;
		//List<PdbInfoDB> deserializePdbList = dbh.deserializePdbList(allPDBs);
		//logger.info("Deserialized all pdbs");
		for(String pdb : allPDBs) {
			PdbInfoDB pdbInfoDB = dbh.deserializePdb(pdb);
		//for(PdbInfoDB pdbInfoDB : deserializePdbList) {
			PdbInfo pdbInfo = new PdbInfo(pdbInfoDB);
			
			for( InterfaceClusterDB iclusterdb : pdbInfoDB.getInterfaceClusters()) {
				InterfaceCluster icluster = new InterfaceCluster(iclusterdb, pdbInfo);
				for( InterfaceDB ifacedb : iclusterdb.getInterfaces()) {
					Interface iface = new Interface(ifacedb,icluster);
					ChainCluster chainA = iface.getChainCluster(Interface.FIRST);
					ChainCluster chainB = iface.getChainCluster(Interface.SECOND);
					int seqA = chainA.getSeqClusterId(seqClusterLevel);
					int seqB = chainB.getSeqClusterId(seqClusterLevel);
					
					// skip chains that didn't cluster, e.g. DNA
					if(seqA < 0 || seqB < 0) {
						continue;
					}
					
					// create sorted pair
					Pair<Integer> seqPair;
					if( seqA <= seqB) {
						seqPair = new Pair<>(seqA,seqB);
					} else {
						seqPair = new Pair<>(seqB,seqA);
					}
					
					// increment count
					int count = 0;
					if(interfaceCounts.containsKey(seqPair)) {
						count = interfaceCounts.get(seqPair);
					}
					interfaceCounts.put(seqPair, count+1);
				}
			}
			
			if(pdbNum % Math.max(1,allPDBs.size()/10000) == 0) {
				updateProgress(pdbNum,allPDBs.size());
			}
			pdbNum++;
//			if(pdbNum > 100) {
//				break;
//			}
		}
		updateProgress(pdbNum,allPDBs.size());


		// Output
		System.out.format("Found %d non-redundant interfaces in cluster %d%%%n",
				interfaceCounts.size(),seqClusterLevel.getLevel());

		if(outputFilename != null) {
			try( PrintWriter out = new PrintWriter(outputFilename) ) {
				out.println("SeqCluster1\tSeqCluster2\tMembers");
				for(Entry<Pair<Integer>, Integer> entry : interfaceCounts.entrySet()) {
					int a = entry.getKey().getFirst();
					int b = entry.getKey().getSecond();
					int count = entry.getValue();
					out.format("%d\t%d\t%d%n", a,b,count);
				}
			} catch( IOException e) {
				logger.error("Error writing {}",outputFilename,e);
			}
		}


		outputHistogram(interfaceCounts.values(),60);

		BigInteger comparisons = BigInteger.valueOf(0);
		for(int x: interfaceCounts.values()) {
			comparisons = comparisons.add(BigInteger.valueOf(x*(x-1)/2));
		}
		System.out.format("Requires %d (%.3g) comparisons%n",comparisons,comparisons.doubleValue());
	}

	private static void outputHistogram(Collection<Integer> values, int bins) {
		int n = values.size();
		
		Iterator<Integer> it = values.iterator();
		int first = it.next();
		
		// Calculate range & sd
		int min = first;
		int max = first;
		int total = first;
		// for computing variance by shifted data method
		int totalSq = first*first;
		
		while(it.hasNext()) {
			int x = it.next();
			if( x < min) {
				min = x;
			}
			if( x > max) {
				max = x;
			}
			total += x;
			totalSq += (x-first)*(x-first);
		}
		it = null;
		
		double mean = (double)total/n;
		double var = ( totalSq - ((double)total*total)/n )/(n-1);
		
		// two bins for outliers, then (bins-2) for the central +-2*sigma
		//int binwidth = Math.max(1, Math.round((float)(Math.sqrt(var)*4./(bins-2))));
		//int firstBin = (int)(mean-binwidth*(bins/2.-1));
		int binwidth=1;
		int firstBin=2;
		int[] binvals = new int[bins];
		for( int x : values) {
			if(x<firstBin) {
				// first bin
				binvals[0]++;
			} else {
				int bin = (x-firstBin)/binwidth;
				if(bin >= bins) {
					// last bin
					binvals[bins-1] ++;
				} else {
					binvals[bin]++;
				}
			}
		}
		
		// normalize
		int maxCount = 0;
		for(int count : binvals) {
			if(count>maxCount)
				maxCount = count;
		}
		double countsPerChar = maxCount/70.;
		// print bins
		printBin(Math.min(min,firstBin),firstBin-1,binvals[0],countsPerChar);
		for(int i=1;i<bins-1;i++) {
			printBin(firstBin+binwidth*i, firstBin+binwidth*(i+1)-1, binvals[i], countsPerChar);
		}
		printBin(firstBin+binwidth*(bins-1),Math.max(max,firstBin+binwidth*bins-1),binvals[bins-1],countsPerChar);
		System.out.format("Mean=%f%n",mean);
		System.out.format("StdDev=%f%n",Math.sqrt(var));
	}
	private static void printBin(int min, int max, int counts, double countsPerChar) {
		System.out.format("%4d - %4d:%5d [",min,max,counts);
		for(int i=0;i<counts/countsPerChar;i++) {
			System.out.print('=');
		}
		System.out.println();
	}

	private static void updateProgress(int x, int size) {
		final int width=80;
		
		double percent = (double)x/size;
		StringBuffer line = new StringBuffer();
		line.append('|');
		for(int i=0;i<width-7;i++) {
			if( i <= percent*(width-7) ) {
				line.append('=');
			} else {
				line.append(' ');
			}
		}
		line.append(String.format("| %3.2f%%",percent*100));
		if(percent < 1 ) {
			line.append('\r');
		} else {
			line.append(String.format("%n"));
		}
		System.out.print(line);
	}
	
}