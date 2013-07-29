package analysis;

import gnu.getopt.Getopt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import owl.core.structure.AaResidue;
import owl.core.structure.AminoAcid;
import owl.core.structure.Atom;
import owl.core.structure.AtomType;
//import owl.core.structure.ChainInterface;
import owl.core.structure.PdbChain;
import owl.core.structure.PdbAsymUnit;
import owl.core.structure.PdbLoadException;
import owl.core.structure.graphs.AICGEdge;
import owl.core.structure.graphs.AICGraph;
import owl.core.util.FileFormatException;
import owl.core.util.MySQLConnection;


public class InterfaceDistanceScorer {

	
	//private static final int TOO_FEW_COUNTS_THRESHOLD = 10;
	//private static final double TOO_FEW_COUNTS_SCORE = 0.0;
	
	private static final int NUM_ATOM_TYPES = 167;

	// our convention is for the index k to be for distances between DISTANCE_BINS[k] and DISTANCE_BINS[k-1]. k=0 will have all distances below DISTANCE_BINS[0]
	//private static final double[] DISTANCE_BINS = {2.0, 2.5, 3.0, 3.5, 4.0, 4.5, 5.0, 5.5, 6.0, 6.5, 7.0, 7.5, 8.0, 8.5, 9.0, 9.5, 10.0}; 
	private static final double[] DISTANCE_BINS = {2.0, 2.5, 3.0, 3.5, 4.0, 4.5, 5.0, 5.5, 6.0, 6.5, 7.0, 7.5};
	
	private static final String ATOM_DISTANCE_COUNTS_TABLE = "atom_distance_counts";
	
	//private static final int NSPHEREPOINTS = 9600;
	//private static final double BSATOASACUTOFF = 0.90;
	
	private double[] distanceBins;
	
	private int[][][] pairCounts;		// the counts of the pairs, indices: 1st i type, 2nd j type, 3rd distance bin (for types see types2indices and indices2types maps)
	private double[][][] scoringMat;	// the scoring matrix
	
	private int[][] sumOverDistanceBins;// counts of pairs of type a,b (irrespective of distance bin)
	private int[] sumOverPairTypes;
	private long totalPairsCount;
		
	private HashMap<String,Integer> types2indices;	// map of types to indices of the above arrays
	private HashMap<Integer,String> indices2types;	// map of indices of the above arrays to types

	
	
	
	private File listFile;							// the file containing the training set list (list of pdbCode+pdbChainCode)
	private ArrayList<String> structureIds;			// ids (pdbCode+pdbChainCode) of structures used for scoring matrix (only the valid ones from the listFile)
	private int totalStructures;						// the number of valid structures used for scoring matrix (i.e. size of structureIds above). 
														// We use a separate variable to store the value for the case we read scoring matrix from file
	
	private double cutoff;							// the distance cutoff
	
	
	
	public InterfaceDistanceScorer(File listFile, int minSeqSep) throws SQLException {
		
		this.types2indices = new HashMap<String, Integer>();
		this.indices2types = new HashMap<Integer, String>();
		
		this.distanceBins = DISTANCE_BINS;
		this.structureIds = new ArrayList<String>();
		this.listFile = listFile;
		this.cutoff = distanceBins[distanceBins.length-1];
		
		this.pairCounts = new int[NUM_ATOM_TYPES][NUM_ATOM_TYPES][distanceBins.length];
		
	}
	
	public InterfaceDistanceScorer(File countsFile) throws IOException, FileFormatException {

		this.types2indices = new HashMap<String, Integer>();
		this.indices2types = new HashMap<Integer, String>();
		
		readCountsFromFile(countsFile);
		calcScoringMat();
	}
	
	/**
	 * Performs the counts of the type pairs and stores them in the internal arrays.
	 * Use subsequently {@link #calcScoringMat()} to compute the scoring matrix from counts arrays.
	 * @throws SQLException if database server can't be accessed to get PDB data
	 * @throws IOException if list file can't be read  
	 */
	public void countPairs() throws SQLException, IOException {
		initAtomMap(types2indices, indices2types);

		BufferedReader br = new BufferedReader(new FileReader(listFile));
		String line;
		while ((line=br.readLine())!=null) {
			if (line.startsWith("#")) continue;
			if (line.trim().isEmpty()) continue;
			
			File pdbFile = new File(line.trim());
			if (!pdbFile.exists()) {
				System.err.println("Warning, couldn't find file "+pdbFile);
				continue;
			}
			
			PdbAsymUnit fullpdb = null;
			try {
				fullpdb = new PdbAsymUnit(pdbFile);
				
				System.out.println(line);
				this.structureIds.add(line);
				
			} catch (PdbLoadException e) {
				System.err.println("Couldn't load file "+line+": "+e.getMessage());
				continue;
			} catch (FileFormatException e) {
				System.err.println("Couldn't load file "+line+": "+e.getMessage());
				continue;
			}

			PdbChain chain1 = null;
			PdbChain chain2 = null;
			int i = 0;
			for (PdbChain chain:fullpdb.getAllChains()) {
				if (i==0) chain1 = chain;
				if (i==1) chain2 = chain;
				i++;
			}
			AICGraph graph = chain1.getAICGraph(chain2, cutoff);
			//ChainInterface interf = new ChainInterface(chain1, chain2, graph, null, null);
			//interf.calcSurfAccess(NSPHEREPOINTS, 1, true);
			//interf.calcRimAndCore(BSATOASACUTOFF);
			
			for (AICGEdge edge:graph.getEdges()) {
				Atom inode = graph.getEndpoints(edge).getFirst();
				Atom jnode = graph.getEndpoints(edge).getSecond();
				int distBin = getDistanceBin(edge.getDistance());
				// we have to avoid OXT atoms, they are not in the map
				if (inode.getCode().equals("OXT") || jnode.getCode().equals("OXT")) continue;
				if (inode.getType()==AtomType.H || jnode.getType()==AtomType.H) continue;
				if (!(inode.getParentResidue() instanceof AaResidue) || 
					!(jnode.getParentResidue() instanceof AaResidue)) continue;
				countPair(types2indices.get(inode.getParentResidue().getLongCode()+inode.getCode()),
						types2indices.get(jnode.getParentResidue().getLongCode()+jnode.getCode()),
						distBin);
			}
		}
		br.close();
		this.totalStructures = structureIds.size();

	}
	
	
	private void countPair(int i, int j, int k) {
		if (j>i){
			pairCounts[i][j][k]++;
		} else {
			pairCounts[j][i][k]++;
		}
	}
	
	/**
	 * Computes the scoring matrix from the counts arrays. The score is log2(p_obs/p_exp)
	 * Whenever the pair count of a certain pair is below the threshold ({@value #TOO_FEW_COUNTS_THRESHOLD}
	 * the score assigned for that pair is {@value #TOO_FEW_COUNTS_SCORE}
	 */
//	public void calcScoringMat() {
//		countTotals();
//		this.scoringMat = new double[NUM_ATOM_TYPES][NUM_ATOM_TYPES][distanceBins.length];
//		double logof2 = Math.log(2);
//		for(int i=0;i<scoringMat.length;i++) {
//			for(int j=i;j<scoringMat[i].length;j++) {
//				for (int k=0;k<distanceBins.length;k++) {
//					if (pairCounts[i][j][k]<TOO_FEW_COUNTS_THRESHOLD) { // When counts are too small, we can't get significant statistics for them
//						scoringMat[i][j][k] = TOO_FEW_COUNTS_SCORE;     // We use a score of 0 in this case, i.e. no information
//					} else {
//						scoringMat[i][j][k] = 
//							Math.log(
//									((double)pairCounts[i][j][k]/(double)sumOverDistanceBins[i][j])/
//									((double)sumOverPairTypes[k]/(double)totalPairsCount)
//							)/logof2; // we explicitely cast every int/long to double to avoid overflows, java doesn't check for them!
//					}
//				}
//			}
//		}
//	}	
	
	/**
	 * Computes the scoring matrix from the counts arrays. The score is log2(p_obs/p_exp)
	 * Whenever the pair count of a certain pair is below the threshold ({@value #TOO_FEW_COUNTS_THRESHOLD}
	 * the score assigned for that pair is {@value #TOO_FEW_COUNTS_SCORE}
	 * Using Ponstingl 2000 (which is Sippl 1990) scoring
	 */
	public void calcScoringMat() {
		countTotals();
		this.scoringMat = new double[NUM_ATOM_TYPES][NUM_ATOM_TYPES][distanceBins.length];
		double sigma = 0.02;
		for(int i=0;i<scoringMat.length;i++) {
			for(int j=i;j<scoringMat[i].length;j++) {
				for (int k=0;k<distanceBins.length;k++) {
					// we explicitely cast every int/long to double to avoid overflows, java doesn't check for them!
					scoringMat[i][j][k] = 
							Math.log(1+sigma*(double)sumOverDistanceBins[i][j]) -
							Math.log(1+sigma*(double)sumOverDistanceBins[i][j]*((double)pairCounts[i][j][k]/(double)sumOverPairTypes[k]));
					// following Sippl 1990 and Ponstingl 2000
				}
			}
		}
	}
	
	public String getTypeFromIndex(int i) {
		return indices2types.get(i);
	}
	
	public int getIndexFromType(String type) {
		return types2indices.get(type);
	}
	
	private int getDistanceBin(double distance) {
		for (int k=0;k<distanceBins.length;k++) {
			int bin = k;
			if (distance<=distanceBins[k]) {
				return bin;
			}
		}
		return -1; //shouldn't even happen: the cutoff for the AIGraph limits this
	}
	
	private void countTotals() {
		sumOverDistanceBins = new int[NUM_ATOM_TYPES][NUM_ATOM_TYPES];
		sumOverPairTypes = new int[distanceBins.length];
		totalPairsCount=0;
		
		for (int i=0;i<NUM_ATOM_TYPES;i++) {
			for (int j=i;j<NUM_ATOM_TYPES;j++) {
				for (int k=0;k<distanceBins.length;k++) {
					totalPairsCount+=pairCounts[i][j][k];
					sumOverDistanceBins[i][j]+=pairCounts[i][j][k];
					sumOverPairTypes[k]+=pairCounts[i][j][k];
				}
			}
		}
	}
	
	public double scoreIt(PdbAsymUnit pdb) {
		
		PdbChain chain1 = null;
		PdbChain chain2 = null;
		int i = 0;
		for (PdbChain chain:pdb.getAllChains()) {
			if (i==0) chain1 = chain;
			if (i==1) chain2 = chain;
			i++;
		}
		AICGraph graph = chain1.getAICGraph(chain2, cutoff);
		return scoreIt(graph);
	}

	private double scoreIt(AICGraph graph) {
		double totalScore = 0;
		//int edgeCount = 0;
		for (AICGEdge edge:graph.getEdges()) {
			Atom inode = graph.getEndpoints(edge).getFirst();
			Atom jnode = graph.getEndpoints(edge).getSecond();
			if (inode.getCode().equals("OXT") || jnode.getCode().equals("OXT")) continue;
			if (inode.getType()==AtomType.H || jnode.getType()==AtomType.H) continue;
			if (!(inode.getParentResidue() instanceof AaResidue) || 
				!(jnode.getParentResidue() instanceof AaResidue)) continue;

			//edgeCount++;
			int i = types2indices.get(inode.getParentResidue().getLongCode()+inode.getCode());
			int j = types2indices.get(jnode.getParentResidue().getLongCode()+jnode.getCode());
			int k = getDistanceBin(edge.getDistance());
			if (j>=i) {
				totalScore+=scoringMat[i][j][k];
			} else {
				totalScore+=scoringMat[j][i][k];
			}
		}
		// we follow here Ponstingl 2000 where they seem not to normalise (and thus scores are VERY correlated to interface area, so quite useless)
		return totalScore; // /(double)edgeCount;
	}

	public void writeCountsToFile (File file) throws FileNotFoundException {
		PrintWriter pw = new PrintWriter(file);

		pw.println("# SCORE METHOD: atom distance dependent");
		pw.println("# cutoff: "+cutoff);
		pw.println("# structures: "+sizeOfTrainingSet());
		pw.println("# list: "+getListFile().toString());
		pw.println("# pairs: "+totalPairsCount);
		pw.print("# distance bins: ");
		for (int k=0;k<distanceBins.length;k++) {
			pw.printf("%3.1f ",distanceBins[k]);
		}
		pw.println();

		for (int i=0;i<NUM_ATOM_TYPES;i++) {
			for (int j=i;j<NUM_ATOM_TYPES;j++) {
				for (int k=0;k<distanceBins.length;k++) {
					pw.println(indices2types.get(i)+"\t"+indices2types.get(j)+"\t"+k+"\t"+pairCounts[i][j][k]);
				}
			}
		}
		pw.close();
	}
	
	public void readCountsFromFile(File file) throws IOException, FileFormatException {
		initAtomMap(types2indices, indices2types);
		
		BufferedReader br = new BufferedReader(new FileReader(file));
		int lineCount = 0;
		String line;
		while ((line=br.readLine())!=null) {
			lineCount++;
			Pattern p = Pattern.compile("^# cutoff: (.*)$");
			Matcher m = p.matcher(line);
			if (m.matches()) {
				this.cutoff = Double.parseDouble(m.group(1));
			}
			p = Pattern.compile("^# structures: (.*)$");
			m = p.matcher(line);
			if (m.matches()) {
				this.totalStructures = Integer.parseInt(m.group(1));
			}
			p = Pattern.compile("^# list: (.*)$");
			m = p.matcher(line);
			if (m.matches()) {
				this.listFile = new File(m.group(1));
			}			
			p = Pattern.compile("^# pairs: (.*)$");
			m = p.matcher(line);
			if (m.matches()) {
				this.totalPairsCount = Long.parseLong(m.group(1));
			}
			p = Pattern.compile("^# distance bins: (.*)$");
			m = p.matcher(line);
			if (m.matches()) {
				String[] tokens = m.group(1).split("\\s");
				distanceBins = new double[tokens.length];
				for (int k=0;k<distanceBins.length;k++) {
					distanceBins[k] = Double.parseDouble(tokens[k]);
				}
				this.pairCounts = new int[NUM_ATOM_TYPES][NUM_ATOM_TYPES][distanceBins.length];
			}
			if (!line.startsWith("#")) {
				String[] tokens = line.split("\\s");
				if (tokens.length!=4) {
					br.close();
					throw new FileFormatException("Counts file "+file+" has incorrect number of columns in line "+lineCount);
				}
				int i = types2indices.get(tokens[0]);
				int j = types2indices.get(tokens[1]);
				int k = Integer.parseInt(tokens[2]);
				int count = Integer.parseInt(tokens[3]);
				pairCounts[i][j][k] = count;
			}
		}
		br.close();
		
		if (cutoff!=distanceBins[distanceBins.length-1]) {
			throw new FileFormatException("Cutoff line doesn't coincide with last value in distance bins line in counts file "+file);
		}
		br.close();
	}
	
	public void writeCountsToDb(String database, MySQLConnection conn) throws SQLException {
		//TODO this is a (working) stub implementation, should write metadata with parameters like distanceBins and so on to separate table
		Statement stmt = conn.createStatement();
		for (int i=0;i<NUM_ATOM_TYPES;i++) {
			for (int j=i;j<NUM_ATOM_TYPES;j++) {
				for (int k=0;k<distanceBins.length;k++) {

					String sql = "INSERT INTO "+database+"."+ATOM_DISTANCE_COUNTS_TABLE+" (i_res, j_res, dist_bin, count) " +
							" VALUES ('"+indices2types.get(i)+"', '"+indices2types.get(j)+"', "+k+", "+pairCounts[i][j][k]+")";
					stmt.executeUpdate(sql);
				}
			}
		}
		stmt.close();
	}
	
	public void readCountsFromDb(String database, MySQLConnection conn) throws SQLException {
		//TODO this is a (working) stub implementation, missing many things: distanceBins and other parameters must be read from db (a metadata table)
		initAtomMap(types2indices, indices2types);
		pairCounts = new int[NUM_ATOM_TYPES][NUM_ATOM_TYPES][distanceBins.length];
		Statement stmt = conn.createStatement();
		String sql = "SELECT i_res, j_res, dist_bin, count FROM "+database+"."+ATOM_DISTANCE_COUNTS_TABLE;
		ResultSet rsst = stmt.executeQuery(sql);
		while (rsst.next()) {
			int i = types2indices.get(rsst.getString(1));
			int j = types2indices.get(rsst.getString(2));
			int k = rsst.getInt(3);
			int count = rsst.getInt(4);
			if (i>j) {
				System.err.println("Warning: indices for atom types in wrong order in database!");
			}
			pairCounts[i][j][k]=count;
		}
		rsst.close();
		stmt.close();
		this.listFile = new File("unknown"); 
	}
	
	/**
	 * Returns the list file, i.e. the files with the list of pdbCode+pdbChainCode used for 
	 * compiling the scoring matrix
	 * @return
	 */
	public File getListFile() {
		return listFile;
	}
	
	/**
	 * Returns the number of structures used for compiling the scoring matrix (only the
	 * ones that were actually used, i.e. that passed the quality checks. See {@link #isValidPdb(PdbChain)})
	 * @return
	 */
	public int sizeOfTrainingSet() {
		return totalStructures;
	}
	
	/**
	 * Gets the cutoff used for defining contacts
	 * @return
	 */
	public double getCutoff() {
		return cutoff;
	}
	
	/**
	 * Given two maps populates them with atom types and indices.
	 * @param types2indices
	 * @param indices2types
	 */
	private static void initAtomMap(HashMap<String,Integer> types2indices, HashMap<Integer,String> indices2types) {
		int i=0;
		for (AminoAcid aa:AminoAcid.getAllStandardAAs()) {
			for (String atom:AminoAcid.getAtoms(aa.getThreeLetterCode())) {
				types2indices.put(aa.getThreeLetterCode()+atom,i);
				i++;				
			}
		}
		for (String resType:types2indices.keySet()) {
			indices2types.put(types2indices.get(resType), resType);
		}
	}
	

	
	public static void main(String[] args) throws Exception {
		String help = 
			"\n" +
			"Two usages:\n" +
			" a) with -o: compiles a scoring matrix for a distance based atom types potential from a given file \n" +
			"    with a list of pdb structures (-l).\n" +
			" b) with -c: reads counts from given file, calculate a scoring matrix from them and score given file\n" +
			"    list in -l.\n" +
			"Options:\n" +
			"  -o <file>     : file to write the counts matrix to\n" +
			"  -l <file>     : file with list of pdb files to score (a) or to generate counts from (b)\n" +
			"  -c <file>     : file to read counts from \n";
			File listFile = null;
			File outFile = null;
			File countsFile = null;

			Getopt g = new Getopt("InterfaceDistanceScorer", args, "o:l:c:h?");
			int c;
			while ((c = g.getopt()) != -1) {
				switch(c){
				case 'l':
					listFile = new File(g.getOptarg());
					break;
				case 'o':
					outFile = new File(g.getOptarg());
					break;
				case 'c':
					countsFile = new File(g.getOptarg());
					break;
				case 'h':
				case '?':
					System.out.println(help);
					System.exit(0);
					break; // getopt() already printed an error
				}
			}

			if (listFile==null) {
				System.err.println("Missing argument -l");
				System.exit(1);
			}

			if (outFile==null && countsFile==null) {
				System.err.println("Missing argument: either -o or -c must be given");
				System.exit(1);
			}
			
			if (outFile!=null) {

				InterfaceDistanceScorer sc = new InterfaceDistanceScorer(listFile, 0);
				sc.countPairs();
				sc.countTotals();
				//sc.calcScoringMat();
				sc.writeCountsToFile(outFile);
			}
			
			if (countsFile!=null) {
				InterfaceDistanceScorer sc = new InterfaceDistanceScorer(countsFile);
				sc.calcScoringMat();
				BufferedReader br = new BufferedReader(new FileReader(listFile));
				String line;
				while ((line=br.readLine())!=null) {
					if (line.trim().isEmpty() || line.startsWith("#")) continue;
					File pdbFile = new File(line.trim());
					if (!pdbFile.exists()) {
						System.err.println("Warning: can't read "+pdbFile);
						continue;
					}
					double score = sc.scoreIt(new PdbAsymUnit(pdbFile));
					System.out.printf("%s %7.3f\n",pdbFile,score);
				}
				br.close();
					
				
			}
			
			

	}
}
