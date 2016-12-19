package eppic.db.tools;

import eppic.commons.sequence.AAAlphabet;
import eppic.commons.sequence.MultipleSequenceAlignment;
import eppic.model.ChainClusterDB;
import eppic.model.HomologDB;
import eppic.model.InterfaceClusterDB;
import eppic.model.InterfaceDB;
import eppic.model.PdbInfoDB;
import eppic.model.ResidueBurialDB;
import gnu.getopt.Getopt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.math.random.RandomDataImpl;
import org.apache.commons.math.stat.descriptive.UnivariateStatistic;
import org.apache.commons.math.stat.descriptive.moment.Mean;
import org.apache.commons.math.stat.descriptive.moment.StandardDeviation;

/**
 * A script to calculate eppic's performance on a given dataset and a set of parameters
 * by extracting the precalculated data from the eppic database.
 * 
 * 
 * @author Joseph Somody
 */
public class EvaluateParamset {
	
	// CONSTANTS
	public static final double SCORE_INFINITY_VALUE = 1000.00;
	
	// PARAMETERS (DEFAULTS SET HERE, CAN BE CHANGED BY PROVIDING PARAMSET FILE)
	public static double geomCutoff = 0.90;
	public static double evolCutoff = 0.80;
	public static double callThreshold = -0.90;
	public static double minSurfaceASA = 5.00;
	public static double numSurfResTol = 2.00;
	public static int numSurfSamp = 10000;
	public static int minHomologues = 10;
	public static double minHomology = 0.50;
	public static AAAlphabet aaAlphabet = new AAAlphabet(AAAlphabet.MIRNY_6);
	
	// DATA FROM DATABASE (RETRIEVED AND CALCULATED ONCE, HELD IN MEMORY FOR ALL PARAMSET EVALUATIONS)
	public static ArrayList<Boolean> truths; // LIST OF BOOLEANS (TRUE IFF BIO), ONE PER DATAPOINT
	public static ArrayList<ArrayList<MultipleSequenceAlignment>> MSAs; // LIST OF PAIRS OF MSAS, ONE PAIR PER DATAPOINT, PAIR = (LEFT, RIGHT)
	public static ArrayList<ArrayList<ArrayList<ArrayList<Double>>>> asaBsas; // LIST OF PAIRS OF LISTS OF PAIRS OF (ASA, BSA), ONE PER DATAPOINT, PAIR = (LEFT, RIGHT)
	public static ArrayList<ArrayList<HashMap<Integer, Integer>>> mappings; // LIST OF PAIRS OF MAPPINGS FROM RES_ID TO MSA_COL, ONE PER DATAPOINT, PAIR = (LEFT MAPPING, RIGHT)
	public static ArrayList<ArrayList<ArrayList<Integer>>> resids; // LIST OF PAIRS OF LISTS OF RESIDUE NUMBERS, ONE PER DATAPOINT, PAIR = (LEFT CHAIN RESID#S, RIGHT)
	public static ArrayList<ArrayList<Integer>> numHomologues; // LIST OF NUMBERS OF HOMOLOGUES, ONE PER DATAPOINT, PAIR = (LEFT, RIGHT)
	public static ArrayList<ArrayList<Double>> homologies; // LIST OF HOMOLOGY CUTOFFS, ONE PER DATAPOINT, PAIR = (LEFT, RIGHT)
	
	// INPUTS FROM COMMAND LINE
	public static String dbName;
	public static File datasetFile;
	public static File paramsetFile;
	
	private static File configFile;
	
	public static void main(String[] args) throws Exception {
		parseCommandLine(args);
		System.out.println("Collecting data from database...");
		collectData();
		System.out.println("Done collecting data from database.");
		System.out.println("Testing parameter sets...");
		testParamsets();
		System.out.println("Done testing parameter sets.");
	}
	
	private static void parseCommandLine(String[] args) throws Exception {
		String help = 
				"Usage: EvaluateParamset \n" +
						" -D <string> : the database name to use (example: \"eppic_2014_10\")\n" +
						" -i <file>   : the dataset used for scoring (example line: \"Many\\t1v6d\\tA\\tA\\t1\\txtal\")\n" +
						" -P <file>   : the list of parameter sets to test (line: \"GeomBurialCutoff\\t" +
						"EvolBurialCutoff\\tCoreSurfaceCallThreshold\\tSurfMinASA\\tNumSurfResTolerance\\t" +
						"NumSurfSamples\\tMinHomologues\\tMinHomology\\tAAAlphabet\")\n" +
						"[-g <file>]  : a configuration file containing the database access parameters, if not provided\n" +
						"               the config will be read from file "+DBHandler.DEFAULT_CONFIG_FILE_NAME+" in home dir\n";
						
		
		String localDbName = null;
		String listFileName = null;
		String paramsetFileName = null;
		Getopt g = new Getopt("EvaluateParamset", args, "D:i:P:g:h?");
		
		
		int c;
		while ((c = g.getopt()) != -1) {
			switch (c) {
			case 'D':
				localDbName = g.getOptarg();
				break;
			case 'i':
				listFileName = g.getOptarg();
				break;
			case 'P':
				paramsetFileName = g.getOptarg();
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
				break;
			}
		}
		
		dbName = localDbName;
		if (localDbName==null) {
			System.err.println("A database name must be provided with -D");
			System.exit(1);
		}
		
		if (listFileName == null) {
			System.err.println("A file with a list of PDB codes has to be provided with the -i flag.");
			System.exit(1);
		}
		datasetFile = new File(listFileName);
		if (!datasetFile.exists()) {
			System.err.println("Input string " + listFileName + " does not seem to be a file.");
			System.exit(1);
		}
		
		if (paramsetFileName == null) {
			System.out.println("No file with a list of parameter sets to be tested was provided with the -P flag. The default parameter set will be used.");
			paramsetFile = null;
		} else {
			paramsetFile = new File(paramsetFileName);
			if (!paramsetFile.exists()) {
				System.err.println("Input string " + paramsetFileName + " does not seem to be a file");
				System.exit(1);
			}
		}
	}
	
	private static void collectData() throws Exception {
		// START DATABASE HANDLER
		DBHandler dbh = new DBHandler(dbName, configFile);
		
		// EXTRACT INFORMATION FROM DATASET
		List<ArrayList<String>> listList = readList(datasetFile);
		List<String> pdbCodes = listList.get(0);
		List<String> firstChains = listList.get(1);
		List<String> secondChains = listList.get(2);
		List<String> interfaceNumbers = listList.get(3);
		List<String> trueCalls = listList.get(4);
		List<PdbInfoDB> pdbInfos = dbh.deserializePdbList(pdbCodes);
		assert pdbCodes.size() == pdbInfos.size();
		
		// INITIALISE STATIC CLASS VARIABLES
		truths = new ArrayList<Boolean>();
		MSAs = new ArrayList<ArrayList<MultipleSequenceAlignment>>();
		asaBsas = new ArrayList<ArrayList<ArrayList<ArrayList<Double>>>>();
		mappings = new ArrayList<ArrayList<HashMap<Integer, Integer>>>();
		resids = new ArrayList<ArrayList<ArrayList<Integer>>>();
		numHomologues = new ArrayList<ArrayList<Integer>>();
		homologies = new ArrayList<ArrayList<Double>>();
		
		for (int i = 0; i < pdbInfos.size(); i++) {
			PdbInfoDB pdbInfo = pdbInfos.get(i);
			String firstChain = firstChains.get(i);
			String secondChain = secondChains.get(i);
			int interfaceNumber = Integer.parseInt(interfaceNumbers.get(i));
			
			// GET LEFT AND RIGHT CHAINCLUSTERS FOR EACH PDBID
			List<ChainClusterDB> chainClusters = pdbInfo.getChainClusters();
			ChainClusterDB firstChainCluster = getChainClusterFromChainClustersByChain(chainClusters, firstChain);
			ChainClusterDB secondChainCluster = getChainClusterFromChainClustersByChain(chainClusters, secondChain);
			
			if (!firstChainCluster.isHasUniProtRef() || !secondChainCluster.isHasUniProtRef()) {
				System.out.println(i + "\t" + pdbInfo.getPdbCode() + "\t" + "HAS NO HOMOLOGUES");
				continue;
			}
			
			// ADD TRUE CALL TO LIST OF TRUE CALLS
			truths.add(trueCalls.get(i).equals("bio"));
			
			// LOG HOMOLOGUES/HOMOLOGY FOR LATER FILTERING
			ArrayList<Integer> thisNumHomologuesPair = new ArrayList<Integer>();
			ArrayList<Double> thisHomologyPair = new ArrayList<Double>();
			thisNumHomologuesPair.add(firstChainCluster.getNumHomologs());
			thisNumHomologuesPair.add(secondChainCluster.getNumHomologs());
			thisHomologyPair.add(firstChainCluster.getSeqIdCutoff());
			thisHomologyPair.add(secondChainCluster.getSeqIdCutoff());
			numHomologues.add(thisNumHomologuesPair);
			homologies.add(thisHomologyPair);
			
			// GET LEFT AND RIGHT HOMOLOGUES
			List<HomologDB> firstHomologs = firstChainCluster.getHomologs();
			List<HomologDB> secondHomologs = secondChainCluster.getHomologs();
			
			// CREATE ARRAYS FOR CREATING LEFT AND RIGHT MULTIPLESEQUENCEALIGNMENTS
			String[] firstHomologTags = new String[firstHomologs.size() + 1];
			String[] firstHomologSeqs = new String[firstHomologs.size() + 1];
			String[] secondHomologTags = new String[secondHomologs.size() + 1];
			String[] secondHomologSeqs = new String[secondHomologs.size() + 1];
			
			// CREATE TAGS AND SEQUENCES FOR LEFT AND RIGHT QUERIES AND ADD TO ARRAY
			firstHomologTags[0] = firstChainCluster.getRefUniProtId() + "_" + firstChainCluster.getRefUniProtStart() + "-" + firstChainCluster.getRefUniProtEnd();
			firstHomologSeqs[0] = firstChainCluster.getMsaAlignedSeq();
			secondHomologTags[0] = secondChainCluster.getRefUniProtId() + "_" + secondChainCluster.getRefUniProtStart() + "-" + secondChainCluster.getRefUniProtEnd();
			secondHomologSeqs[0] = secondChainCluster.getMsaAlignedSeq();
			
			// ADD LEFT AND RIGHT HOMOLOGUE TAGS AND SEQUENCES TO ARRAY
			for (int j = 0; j < firstHomologs.size(); j++) {
				HomologDB homolog = firstHomologs.get(j);
				firstHomologTags[j + 1] = homolog.getUniProtId() + "_" + homolog.getSubjectStart() + "-" + homolog.getSubjectEnd();
				firstHomologSeqs[j + 1] = homolog.getAlignedSeq();
			}
			for (int j = 0; j < secondHomologs.size(); j++) {
				HomologDB homolog = secondHomologs.get(j);
				secondHomologTags[j + 1] = homolog.getUniProtId() + "_" + homolog.getSubjectStart() + "-" + homolog.getSubjectEnd();
				secondHomologSeqs[j + 1] = homolog.getAlignedSeq();
			}
			
			// CREATE PAIR (LEFT, RIGHT) OF MULTIPLESEQUENCEALIGNMENTS
			ArrayList<MultipleSequenceAlignment> MSAPair = new ArrayList<MultipleSequenceAlignment>();
			MSAPair.add(new MultipleSequenceAlignment(firstHomologTags, firstHomologSeqs));
			MSAPair.add(new MultipleSequenceAlignment(secondHomologTags, secondHomologSeqs));
			
			// ADD MULTIPLESEQUENCEALIGNMENT PAIR TO LIST OF MULTIPLESEQUENCEALIGNMENTS
			MSAs.add(MSAPair);
			
			// GET LEFT AND RIGHT LISTS OF (ASA,BSA) PAIRS FOR RESIDUES
			List<InterfaceClusterDB> interfaceClusters = pdbInfo.getInterfaceClusters();
			InterfaceDB theInterface = getInterfaceFromInterfaceClustersByInterfaceId(interfaceClusters, interfaceNumber);
			List<ResidueBurialDB> residues = theInterface.getResidueBurials();
			ArrayList<ArrayList<Double>> firstAsaBsas = new ArrayList<ArrayList<Double>>();
			ArrayList<ArrayList<Double>> secondAsaBsas = new ArrayList<ArrayList<Double>>();
			
			// CREATE LEFT AND RIGHT MAPPINGS FROM INTERFACE RESIDUE NUMBERS TO CHAINCLUSTER MSA COLUMN NUMBERS
			ArrayList<Integer> firstResids = new ArrayList<Integer>();
			ArrayList<Integer> secondResids = new ArrayList<Integer>();
			HashMap<Integer, Integer> firstMapping = new HashMap<Integer, Integer>();
			HashMap<Integer, Integer> secondMapping = new HashMap<Integer, Integer>();
			String firstPDB = firstChainCluster.getPdbAlignedSeq();
			String firstREF = firstChainCluster.getRefAlignedSeq();
			String secondPDB = secondChainCluster.getPdbAlignedSeq();
			String secondREF = secondChainCluster.getRefAlignedSeq();
			MultipleSequenceAlignment firstPairwise = new MultipleSequenceAlignment(new String[] {"firstPDB", "firstREF"}, new String[] {firstPDB, firstREF});
			MultipleSequenceAlignment secondPairwise = new MultipleSequenceAlignment(new String[] {"secondPDB", "secondREF"}, new String[] {secondPDB, secondREF});
			for (ResidueBurialDB aResidue : residues) {
				if (aResidue.getSide() == false) {
					int mapFrom = aResidue.getResidueInfo().getResidueNumber();
					if (mapFrom >= firstChainCluster.getPdbStart() && mapFrom <= firstChainCluster.getPdbEnd()) {
						int correspondingRefIndex = firstPairwise.al2seq("firstREF", firstPairwise.seq2al("firstPDB", mapFrom)) - 1;
						if (correspondingRefIndex > 0) {
							int withOffsets = correspondingRefIndex - firstChainCluster.getRefUniProtStart() + 1;
							int mapTo = MSAPair.get(0).seq2al(firstHomologTags[0], withOffsets + 1);
							ArrayList<Double> pairAsaBsa = new ArrayList<Double>();
							pairAsaBsa.add(aResidue.getAsa());
							pairAsaBsa.add(aResidue.getBsa());
							firstAsaBsas.add(pairAsaBsa);
							firstResids.add(aResidue.getResidueInfo().getResidueNumber());
							firstMapping.put(mapFrom, mapTo);
						}
					}
				} else if (aResidue.getSide() == true) {
					int mapFrom = aResidue.getResidueInfo().getResidueNumber();
					if (mapFrom >= secondChainCluster.getPdbStart() && mapFrom <= secondChainCluster.getPdbEnd()) {
						int correspondingRefIndex = secondPairwise.al2seq("secondREF", secondPairwise.seq2al("secondPDB", mapFrom)) - 1;
						if (correspondingRefIndex > 0) {
							int withOffsets = correspondingRefIndex - secondChainCluster.getRefUniProtStart() + 1;
							int mapTo = MSAPair.get(1).seq2al(secondHomologTags[0], withOffsets + 1);
							ArrayList<Double> pairAsaBsa = new ArrayList<Double>();
							pairAsaBsa.add(aResidue.getAsa());
							pairAsaBsa.add(aResidue.getBsa());
							secondAsaBsas.add(pairAsaBsa);
							secondResids.add(aResidue.getResidueInfo().getResidueNumber());
							secondMapping.put(mapFrom, mapTo);
						}
					}
				}
			}
			
			// CREATE PAIR (LEFT, RIGHT) OF MAPPINGS
			ArrayList<ArrayList<ArrayList<Double>>> asaBsaPair = new ArrayList<ArrayList<ArrayList<Double>>>();
			ArrayList<ArrayList<Integer>> residPair = new ArrayList<ArrayList<Integer>>();
			ArrayList<HashMap<Integer, Integer>> mappingPair = new ArrayList<HashMap<Integer, Integer>>();
			asaBsaPair.add(firstAsaBsas);
			residPair.add(firstResids);
			mappingPair.add(firstMapping);
			asaBsaPair.add(secondAsaBsas);
			residPair.add(secondResids);
			mappingPair.add(secondMapping);
			
			// ADD PAIR OF LISTS OF REGIONS TO LIST OF PAIRS OF LISTS OF REGIONS (AND RESIDS AND MAPPINGS)
			asaBsas.add(asaBsaPair);
			resids.add(residPair);
			mappings.add(mappingPair);
		}
	}
	
	private static ArrayList<ArrayList<String>> readList(File file) throws IOException {
		ArrayList<String> pdbs = new ArrayList<String>();
		ArrayList<String> ones = new ArrayList<String>();
		ArrayList<String> twos = new ArrayList<String>();
		ArrayList<String> ints = new ArrayList<String>();
		ArrayList<String> trueCalls = new ArrayList<String>();

		BufferedReader br = new BufferedReader(new FileReader(file));
		String line;
		while ((line = br.readLine()) != null) {
			if (line.startsWith("#")) continue;
			if (line.trim().isEmpty()) continue;
			String[] lineArray = line.trim().split("\\s+");
			if (lineArray[1].length() == 4) {
				pdbs.add(lineArray[1]);
				ones.add(lineArray[2]);
				twos.add(lineArray[3]);
				ints.add(lineArray[4]);
				trueCalls.add(lineArray[5]);
			}
		}
		br.close();

		ArrayList<ArrayList<String>> toReturn = new ArrayList<ArrayList<String>>();
		toReturn.add(pdbs);
		toReturn.add(ones);
		toReturn.add(twos);
		toReturn.add(ints);
		toReturn.add(trueCalls);
		assert pdbs.size() == ones.size();
		assert ones.size() == twos.size();
		assert twos.size() == ints.size();
		assert ints.size() == trueCalls.size();
		return toReturn;
	}
	
	private static ChainClusterDB getChainClusterFromChainClustersByChain(List<ChainClusterDB> clusters, String chain) {
		ChainClusterDB toReturn = null;
		for (ChainClusterDB cluster : clusters) {
			for (String member : cluster.getMemberChains().split(",")) {
				if (member.equals(chain)) {
					toReturn = cluster;
				}
			}
		}
		return toReturn;
	}
	
	private static InterfaceDB getInterfaceFromInterfaceClustersByInterfaceId(List<InterfaceClusterDB> clusters, int interfaceId) {
		InterfaceDB toReturn = null;
		for (InterfaceClusterDB cluster : clusters) {
			for (InterfaceDB anInterface : cluster.getInterfaces()) {
				if (anInterface.getInterfaceId() == interfaceId) {
					toReturn = anInterface;
				}
			}
		}
		return toReturn;
	}
	
	private static int getRegion(double asa, double bsa) {
		int assignment = ResidueBurialDB.OTHER;
		double ratio = bsa / asa;
		if (asa > minSurfaceASA && bsa > 0) {
			if (ratio < evolCutoff) {
				assignment = ResidueBurialDB.RIM_EVOLUTIONARY;
			} else if (ratio < geomCutoff) {
				assignment = ResidueBurialDB.CORE_EVOLUTIONARY; 
			} else {
				assignment = ResidueBurialDB.CORE_GEOMETRY;
			}
		} else if (asa > minSurfaceASA) {
			assignment = ResidueBurialDB.SURFACE;
		}		
		return assignment;
	}
	
	private static ArrayList<ArrayList<Double>> getEntropies(int whichDatapoint, int whichSide) {
		ArrayList<Double> coreEntropies = new ArrayList<Double>(); // empty list for core entropies
		ArrayList<Double> surfaceEntropies = new ArrayList<Double>(); // empty list for surface entropies
		ArrayList<ArrayList<Double>> thisAsaBsas = asaBsas.get(whichDatapoint).get(whichSide); // get appropriate list of ASA/BSA pairs
		ArrayList<Integer> thisRegions = new ArrayList<Integer>();
		for (int i = 0; i < thisAsaBsas.size(); i++) {
			thisRegions.add(getRegion(thisAsaBsas.get(i).get(0), thisAsaBsas.get(i).get(1)));
		}
		ArrayList<Integer> thisResidues = resids.get(whichDatapoint).get(whichSide); // get appropriate list of residue nums
		HashMap<Integer, Integer> thisMapping = mappings.get(whichDatapoint).get(whichSide); // get appropriate mapping from residnum to msacol
		MultipleSequenceAlignment thisMSA = MSAs.get(whichDatapoint).get(whichSide); // get appropriate msa
		for (int i = 0; i < thisResidues.size(); i++) { // loop through indices of regions/residues
			int thisRegion = thisRegions.get(i); // get the region at this index
			int mapFrom = thisResidues.get(i); // get the residue number at this index
			int mapped = thisMapping.get(mapFrom); // use mapping to get column number from residue number
			if (thisRegion == ResidueBurialDB.CORE_EVOLUTIONARY || thisRegion == ResidueBurialDB.CORE_GEOMETRY) {
				coreEntropies.add(thisMSA.getColumnEntropy(mapped, aaAlphabet)); // add column entropy to core list iff core
			}
			if (thisRegion != ResidueBurialDB.OTHER) { // not other -> one of surface/rimevol/coreevol/coregeom = the real surface
				surfaceEntropies.add(thisMSA.getColumnEntropy(mapped, aaAlphabet)); // add column entropy to surface list iff surface
			}
		}
		ArrayList<ArrayList<Double>> entropiesPair = new ArrayList<ArrayList<Double>>();
		entropiesPair.add(coreEntropies);
		entropiesPair.add(surfaceEntropies);
		return entropiesPair;
	}
	
	private static void testParamsets() throws IOException {
		System.out.println("GeomBurialCutoff\tEvolBurialCutoff\tCoreSurfaceCallThreshold\t" +
				"SurfMinASA\tNumSurfResTolerance\tNumSurfSamples\tMinHomologues\tMinHomology\tAAAlphabet\tBalancedAccuracy");
		if (paramsetFile == null) {
			System.out.println(getCurrentParamset() + "\t" + testFitness());
		} else {
			BufferedReader br = new BufferedReader(new FileReader(paramsetFile));
			String line;
			while ((line = br.readLine()) != null) {
				if (line.startsWith("#")) continue;
				if (line.trim().isEmpty()) continue;
				String[] lineArray = line.trim().split("\\s+");
				geomCutoff = Double.parseDouble(lineArray[0]);
				evolCutoff = Double.parseDouble(lineArray[1]);
				callThreshold = Double.parseDouble(lineArray[2]);
				minSurfaceASA = Double.parseDouble(lineArray[3]);
				numSurfResTol = Double.parseDouble(lineArray[4]);
				numSurfSamp = Integer.parseInt(lineArray[5]);
				minHomologues = Integer.parseInt(lineArray[6]);
				minHomology = Double.parseDouble(lineArray[7]);
				aaAlphabet = new AAAlphabet(lineArray[8]);
				System.out.println(getCurrentParamset() + "\t" + testFitness());
			}
			br.close();
		}
	}
	
	private static String getCurrentParamset() {
		StringBuilder sb = new StringBuilder();
		sb.append(geomCutoff);
		sb.append("\t");
		sb.append(evolCutoff);
		sb.append("\t");
		sb.append(callThreshold);
		sb.append("\t");
		sb.append(minSurfaceASA);
		sb.append("\t");
		sb.append(numSurfResTol);
		sb.append("\t");
		sb.append(numSurfSamp);
		sb.append("\t");
		sb.append(minHomologues);
		sb.append("\t");
		sb.append(minHomology);
		sb.append("\t");
		sb.append(aaAlphabet);
		return sb.toString();
	}
	
	private static double testFitness() { // balanced accuracy
		double truePositives = 0.0;
		double falsePositives = 0.0;
		double trueNegatives = 0.0;
		double falseNegatives = 0.0;
		for (int i = 0; i < truths.size(); i++) { // loop through all datapoints
			if ((numHomologues.get(i).get(0) >= minHomologues) && (numHomologues.get(i).get(1) >= minHomologues)) {
				if ((homologies.get(i).get(0) >= minHomology) && (homologies.get(i).get(1) >= minHomology)) {
					ArrayList<ArrayList<Double>> leftEntropiesPair = getEntropies(i, 0); // get left entropies
					Double leftScore = doEntropySamplingAndCalculation(leftEntropiesPair.get(0), leftEntropiesPair.get(1)); // and calculate the score
					ArrayList<ArrayList<Double>> rightEntropiesPair = getEntropies(i, 1); // get right entropies
					Double rightScore = doEntropySamplingAndCalculation(rightEntropiesPair.get(0), rightEntropiesPair.get(1)); // and calculate the score
					double finalScore = Double.NaN; // default value for final score
					if (!Double.isNaN(leftScore) && !Double.isNaN(rightScore)) { // if neither side is NaN, average for final score
						finalScore = (leftScore + rightScore) / 2.0;
					} else if (Double.isNaN(leftScore)) { // if left is NaN, use right for final
						finalScore = rightScore;
					} else if (Double.isNaN(rightScore)) { // if right is NaN, use left for final
						finalScore = leftScore;
					} // otherwise, both are NaN, final stays NaN
					boolean finalCall = Boolean.FALSE;
					if (!Double.isNaN(finalScore)) {
						finalCall = finalScore < callThreshold;
					}
					boolean truth = truths.get(i);
					if (finalCall && truth) {
						truePositives++;
					} else if (!finalCall && !truth) {
						trueNegatives++;
					} else if (finalCall && !truth) {
						falsePositives++;
					} else if (!finalCall && truth) {
						falseNegatives++;
					}
				}
			}
		}
		if (truePositives + falseNegatives < 1) {
			return 0.0;
		}
		if (trueNegatives + falsePositives < 1) {
			return 0.0;
		}
		double sensitivity = truePositives / (truePositives + falseNegatives);
		double specificity = trueNegatives / (falsePositives + trueNegatives);
		return (sensitivity + specificity) / 2.0;
	}
	
	private static double doEntropySamplingAndCalculation(ArrayList<Double> coreEntropies, ArrayList<Double> surfaceEntropies) {
		if (coreEntropies.size() == 0) {
			return Double.NaN;
		}
		if (surfaceEntropies.size() < (coreEntropies.size() * numSurfResTol)) {
			return Double.NaN;
		}
		double coreTotal = 0.0;
		double coreCount = 0.0;
		for (double thisEntropy : coreEntropies) {
			coreTotal += thisEntropy;
			coreCount += 1.0;
		}
		double coreScore = coreTotal / coreCount;
		final int SAMPLE_SIZE = coreEntropies.size();
		double surfaceScoreMean = Double.NaN;
		double surfaceScoreSD = Double.NaN;
		double[] surfaceScoreDistribution = new double[numSurfSamp];
		RandomDataImpl rd = new RandomDataImpl();
		for (int i = 0; i < numSurfSamp; i++) {
			Object[] sample = rd.nextSample(surfaceEntropies, SAMPLE_SIZE);
			double total = 0.0;
			double count = 0.0;
			for (int j = 0; j < sample.length; j++) {
				total += (double)sample[j];
				count += 1.0;
			}
			surfaceScoreDistribution[i] = total / count;
		}
		UnivariateStatistic stat = new Mean();
		surfaceScoreMean = stat.evaluate(surfaceScoreDistribution);
		stat = new StandardDeviation();
		surfaceScoreSD = stat.evaluate(surfaceScoreDistribution);
		double zScore = Double.NaN;
		if (surfaceScoreSD != 0) {
			zScore = (coreScore - surfaceScoreMean) / surfaceScoreSD;
		} else {
			if ((coreScore - surfaceScoreMean) > 0) {
				zScore = SCORE_INFINITY_VALUE;
			} else if ((coreScore - surfaceScoreMean) < 0) {
				zScore = -SCORE_INFINITY_VALUE;
			} else {
				zScore = Double.NaN;
			}
		}
		return zScore;
	}
}