package eppic.db.tools;

import java.io.File;

import org.biojava.nbio.structure.contact.Pair;

import eppic.db.LatticeMatchMatrix;
import eppic.db.LatticeOverlapScore;
import eppic.db.PdbInfo;
import eppic.db.SeqClusterLevel;
import gnu.getopt.Getopt;

public class CompareLattices {

	private static final SeqClusterLevel DEFAULT_SEQ_CLUSTER_LEVEL = SeqClusterLevel.C50;
	private static final double DEFAULT_MIN_AREA = 10;
	private static final double DEFAULT_CO_CUTOFF = 0.2;

	public static void main(String[] args) throws Exception {

		String help = 
				"Usage: CompareLattices \n" +
				" -D <string> : the database name to use\n"+
				" -f <string> : first PDB code\n" +
				" -s <string> : second PDB code\n"+
				" [-c <float>]: contact overlap score cutoff, default "+String.format("%.1f", DEFAULT_CO_CUTOFF)+"\n"+
				" [-a <float>]: minimum area cutoff, default "+String.format("%.0f", DEFAULT_MIN_AREA)+"\n"+
				" [-l <int>  ]: sequence cluster level to be used, default "+DEFAULT_SEQ_CLUSTER_LEVEL+"\n"+
				" [-d]        : print some debug output\n"+
				" [-g <file> ]: a configuration file containing the database access parameters, if not provided\n" +
				"               the config will be read from file "+DBHandler.DEFAULT_CONFIG_FILE_NAME+" in home dir\n";
		
		boolean debug = false;
		
		String pdbCode1 = null;
		String pdbCode2 = null;

		double coCutoff = DEFAULT_CO_CUTOFF;
		double minArea = DEFAULT_MIN_AREA;
		SeqClusterLevel seqClusterLevel = DEFAULT_SEQ_CLUSTER_LEVEL;
		
		String dbName = null;
		File configFile = null;
		
		
		Getopt g = new Getopt("CompareLattices", args, "D:f:s:c:a:l:dg:h?");
		int c;
		while ((c = g.getopt()) != -1) {
			switch(c){
			case 'D':
				dbName = g.getOptarg();
				break;
			case 'f':
				pdbCode1 = g.getOptarg();
				break;
			case 's':
				pdbCode2 = g.getOptarg();
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
		
		
		if (pdbCode1 == null || pdbCode2 == null) {
			System.err.println("At least options -f and -s are needed");
			System.exit(1);
		}
		
		
		DBHandler dbh = new DBHandler(dbName, configFile);	
		
		PdbInfo pdb1 = new PdbInfo(dbh.deserializePdb(pdbCode1));
		PdbInfo pdb2 = new PdbInfo(dbh.deserializePdb(pdbCode2));
		
		
		System.out.println (pdbCode1+" vs "+pdbCode2);
		
		if (pdb1.haveSameContent(pdb2, seqClusterLevel)) {
			System.out.println("Both PDBs have same content (at "+seqClusterLevel.getLevel()+"% seq id)");
		} else {
			System.out.println("Both PDBs haven't got the same content (at "+seqClusterLevel.getLevel()+"% seq id)");
			//System.exit(0);
		}
		
		LatticeMatchMatrix llm = pdb1.calcLatticeOverlapMatrix(pdb2, seqClusterLevel, minArea, debug);
		double[][] matrix = llm.getCoMatrix();
		for (int i=0;i<matrix.length;i++) {
			for (int j=0;j<matrix[i].length;j++) {
				System.out.printf("%2d-%2d: %5.3f\t",i+1,j+1,matrix[i][j]);
			}
			System.out.println();
		}
		
		System.out.println("Matching pairs:");
		for (Pair<Integer> pair:llm.getMatchingPairs(coCutoff)) {
			System.out.println(pair.getFirst()+"-"+pair.getSecond());
		}
	
		LatticeOverlapScore s = llm.getLatticeOverlapScore(coCutoff);
		System.out.printf("Total overlaps fAB: %5.3f, fBA: %5.3f\n", s.getfAB(), s.getfBA());
	}
}
