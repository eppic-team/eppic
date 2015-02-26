package eppic.db.tools;

import eppic.model.ChainClusterDB;
import eppic.model.HomologDB;
import eppic.model.InterfaceClusterDB;
import eppic.model.InterfaceDB;
import eppic.model.PdbInfoDB;
import eppic.model.ResidueDB;
import gnu.getopt.Getopt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import owl.core.sequence.alignment.MultipleSequenceAlignment;
import owl.core.structure.AAAlphabet;
import owl.core.structure.AminoAcid;

public class AlphabetOptimisation {
	
	public static final double EVOL_CUTOFF = 0.80;
	public static final double GEOM_CUTOFF = 0.90;
	public static final double MIN_ASA_FOR_SURFACE = 5;
	public static final String[] AMINO_ACIDS = {"A", "C", "D", "E", "F", "G", "H", "I", "K", "L", "M", "N", "P", "Q", "R", "S", "T", "V", "W", "Y"};
	public static final int CARRYING_CAPACITY = 10;
	public static final int INITIAL_POPULATION_SIZE = 10;
	public static final int ROUNDS_OF_EVOLUTION = 50;
	
	public static ArrayList<Boolean> truths; // THIS IS A LIST OF BOOLEANS (TRUE IFF BIO)
	public static ArrayList<ArrayList<MultipleSequenceAlignment>> MSAs; // THIS IS A LIST OF PAIRS OF MSAS
	public static ArrayList<ArrayList<ArrayList<Integer>>> regions; // THIS IS A LIST OF PAIRS OF LISTS OF REGIO_INTS
	public static ArrayList<ArrayList<HashMap<Integer, Integer>>> mappings; // THIS IS A LIST OF PAIRS OF MAPPINGS FROM RES_ID TO MSA_COL
	public static ArrayList<ArrayList<Integer>> resids;

	public static void main(String[] args) throws Exception {
		String help = 
				"Usage: AlphabetOptimisation\n" +
				" -D : the database name to use\n" +
				" -i : a list file of PDB codes\n" +
				"The database access must be set in file " + DBHandler.CONFIG_FILE_NAME + " in home dir\n";

		String dbName = null;
		String listFileName = null;
		
		Getopt g = new Getopt("AlphabetOptimisation", args, "D:i:h?");
		int c;
		while ((c = g.getopt()) != -1) {
			switch (c) {
			case 'D':
				dbName = g.getOptarg();
				break;
			case 'i':
				listFileName = g.getOptarg();
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
		
		if (listFileName == null) {
			System.err.println("A file with a list of PDB codes have to be provided with -i");
			System.exit(1);
		}
		
		File listFile = new File(listFileName);
		if (!listFile.exists()) {
			System.err.println("Input string " + listFileName + " does not seem to be a file");
			System.exit(1);
		}
		
		// START DATABASE HANDLER
		DBHandler dbh = new DBHandler(dbName);
		
		// EXTRACT INFORMATION FROM DATASET
		List<ArrayList<String>> listList = readList(listFile);
		List<String> pdbCodes = listList.get(0);
		List<String> firstChains = listList.get(1);
		List<String> secondChains = listList.get(2);
		List<String> interfaceNumbers = listList.get(3);
		List<String> trueCalls = listList.get(4);
		List<PdbInfoDB> pdbInfos = dbh.deserializePdbList(pdbCodes);
		
		// INITIALISE STATIC CLASS VARIABLES
		truths = new ArrayList<Boolean>();
		MSAs = new ArrayList<ArrayList<MultipleSequenceAlignment>>();
		regions = new ArrayList<ArrayList<ArrayList<Integer>>>();
		mappings = new ArrayList<ArrayList<HashMap<Integer, Integer>>>();
		resids = new ArrayList<ArrayList<Integer>>();
		
		for (int i = 0; i < pdbInfos.size(); i++) {
			System.out.println(i);
			PdbInfoDB pdbInfo = pdbInfos.get(i);
			String firstChain = firstChains.get(i);
			String secondChain = secondChains.get(i);
			int interfaceNumber = Integer.parseInt(interfaceNumbers.get(i));
			
			// ADD TRUE CALL TO LIST OF TRUE CALLS
			truths.add(trueCalls.get(i).equals("bio"));
			
			// GET LEFT AND RIGHT CHAINCLUSTERS FOR EACH PDBID
			List<ChainClusterDB> chainClusters = pdbInfo.getChainClusters();
			ChainClusterDB firstChainCluster = getChainClusterFromChainClustersByChain(chainClusters, firstChain);
			ChainClusterDB secondChainCluster = getChainClusterFromChainClustersByChain(chainClusters, secondChain);
			
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
//			System.out.println("query-first-seq " + firstHomologTags[0] + " " + firstChainCluster.getMsaAlignedSeq());
			secondHomologTags[0] = secondChainCluster.getRefUniProtId() + "_" + secondChainCluster.getRefUniProtStart() + "-" + secondChainCluster.getRefUniProtEnd();
			secondHomologSeqs[0] = secondChainCluster.getMsaAlignedSeq();
//			System.out.println("query-second-seq " + secondChainCluster.getMsaAlignedSeq());
			
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
			
			// GET LEFT AND RIGHT LISTS OF REGIONS OF RESIDUES
			List<InterfaceClusterDB> interfaceClusters = pdbInfo.getInterfaceClusters();
			InterfaceDB theInterface = getInterfaceFromInterfaceClustersByInterfaceId(interfaceClusters, interfaceNumber);
			List<ResidueDB> residues = theInterface.getResidues();
			ArrayList<Integer> firstRegions = new ArrayList<Integer>();
			ArrayList<Integer> secondRegions = new ArrayList<Integer>();
			for (ResidueDB aResidue : residues) {
				if (aResidue.getSide() == 1) {
					firstRegions.add(getRegion(aResidue.getAsa(), aResidue.getBsa()));
				}
				else if (aResidue.getSide() == 2) {
					secondRegions.add(getRegion(aResidue.getAsa(), aResidue.getBsa()));
				}
			}
			
			// CREATE PAIR (LEFT, RIGHT) OF LISTS OF REGIONS OF RESIDUES
			ArrayList<ArrayList<Integer>> regionPair = new ArrayList<ArrayList<Integer>>();
			regionPair.add(firstRegions);
			regionPair.add(secondRegions);
			
			// ADD PAIR OF LISTS OF REGIONS TO LIST OF PAIRS OF LISTS OF REGIONS
			regions.add(regionPair);
			
			// CREATE LEFT AND RIGHT MAPPINGS FROM INTERFACE RESIDUE NUMBERS TO CHAINCLUSTER MSA COLUMN NUMBERS
			HashMap<Integer, Integer> firstMapping = new HashMap<Integer, Integer>();
			HashMap<Integer, Integer> secondMapping = new HashMap<Integer, Integer>();
			String firstPDB = firstChainCluster.getPdbAlignedSeq();
			String firstREF = firstChainCluster.getRefAlignedSeq();
			String secondPDB = secondChainCluster.getPdbAlignedSeq();
			String secondREF = secondChainCluster.getRefAlignedSeq();
			MultipleSequenceAlignment firstPairwise = new MultipleSequenceAlignment(new String[] {"firstPDB", "firstREF"}, new String[] {firstPDB, firstREF});
			MultipleSequenceAlignment secondPairwise = new MultipleSequenceAlignment(new String[] {"secondPDB", "secondREF"}, new String[] {secondPDB, secondREF});
			for (ResidueDB aResidue : residues) {
				if (aResidue.getSide() == 1) {
//					if (firstChainCluster.getPdbAlignedSeq().charAt(aResidue.getResidueNumber() - 1) != AminoAcid.three2one(aResidue.getResidueType())) {
//						System.out.println(firstChainCluster.getPdbAlignedSeq().charAt(aResidue.getResidueNumber() - 1) + "\t!=\t" + aResidue.getResidueType() + aResidue.getResidueNumber());
//					}
					int mapFrom = aResidue.getResidueNumber();
					int correspondingRefIndex = firstPairwise.al2seq("firstREF", firstPairwise.seq2al("firstPDB", mapFrom)) - 1;
					if (correspondingRefIndex > 0) {
						int withOffsets = correspondingRefIndex - firstChainCluster.getRefUniProtStart() + 1;
						int mapTo = MSAPair.get(0).seq2al(firstHomologTags[0], withOffsets + 1);
//						System.out.println(aResidue.getResidueType() + mapFrom + "\t" + correspondingRefIndex + "\t" + withOffsets + "\t" + mapTo + "\t" + MSAPair.get(0).getColumn(mapTo).charAt(0));
//						if (MSAPair.get(0).getColumn(mapTo).charAt(0) != AminoAcid.three2one(aResidue.getResidueType())) {
//							System.out.println(MSAPair.get(0).getColumn(mapTo).charAt(0) + "\t!=\t" + aResidue.getResidueType() + aResidue.getResidueNumber());
//						}
						firstMapping.put(mapFrom, mapTo);
					}
				}
				else if (aResidue.getSide() == 2) {
//					if (secondChainCluster.getPdbAlignedSeq().charAt(aResidue.getResidueNumber() - 1) != AminoAcid.three2one(aResidue.getResidueType())) {
//						System.out.println(secondChainCluster.getPdbAlignedSeq().charAt(aResidue.getResidueNumber() - 1) + "\t!=\t" + aResidue.getResidueType() + aResidue.getResidueNumber());
//					}
					int mapFrom = aResidue.getResidueNumber();
					int correspondingRefIndex = secondPairwise.al2seq("secondREF", secondPairwise.seq2al("secondPDB", mapFrom)) - 1;
					if (correspondingRefIndex > 0) {
						int withOffsets = correspondingRefIndex - secondChainCluster.getRefUniProtStart() + 1;
						int mapTo = MSAPair.get(1).seq2al(secondHomologTags[0], withOffsets + 1);
//						System.out.println(aResidue.getResidueType() + mapFrom + "\t" + correspondingRefIndex + "\t" + withOffsets + "\t" + mapTo + "\t" + MSAPair.get(1).getColumn(mapTo).charAt(0));
//						if (MSAPair.get(1).getColumn(mapTo).charAt(0) != AminoAcid.three2one(aResidue.getResidueType())) {
//						System.out.println(MSAPair.get(1).getColumn(mapTo).charAt(0) + "\t!=\t" + aResidue.getResidueType() + aResidue.getResidueNumber());
//						}
						secondMapping.put(mapFrom, mapTo);
					}
				}
			}
			
			// CREATE PAIR (LEFT, RIGHT) OF MAPPINGS
			ArrayList<HashMap<Integer, Integer>> mappingPair = new ArrayList<HashMap<Integer, Integer>>();
			mappingPair.add(firstMapping);
			mappingPair.add(secondMapping);

			// ADD PAIR OF LISTS OF REGIONS TO LIST OF PAIRS OF LISTS OF REGIONS
			mappings.add(mappingPair);
		}
		
		// RUN ALPHABET OPTIMISATION
		optimiseAlphabet();
	}
	
	private static String removeGaps(String input) {
		String output = "";
		for (char c : input.toCharArray()) {
			if (c != MultipleSequenceAlignment.GAPCHARACTER) {
				output += c;
			}
		}
		return output;
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
		int assignment = ResidueDB.OTHER;
		double ratio = bsa / asa;
		if (asa > MIN_ASA_FOR_SURFACE && bsa > 0) {
			if (ratio < EVOL_CUTOFF) {
				assignment = ResidueDB.RIM_EVOLUTIONARY;
			}
			else if (ratio < GEOM_CUTOFF) {
				assignment = ResidueDB.CORE_EVOLUTIONARY; 
			}
			else {
				assignment = ResidueDB.CORE_GEOMETRY;
			}
		}
		else if (asa > MIN_ASA_FOR_SURFACE) {
			assignment = ResidueDB.SURFACE;
		}		
		return assignment;
	}
	
	private static ArrayList<ArrayList<Double>> getEntropies(int whichDatapoint, int whichSide, AAAlphabet whichAlphabet) {
		ArrayList<Double> coreEntropies = new ArrayList<Double>();
		ArrayList<Double> surfaceEntropies = new ArrayList<Double>();
		ArrayList<Integer> thisRegions = regions.get(whichDatapoint).get(whichSide);
		for (int i = 0; i < thisRegions.size(); i++) {
			int thisRegion = thisRegions.get(i);
			if (thisRegion == ResidueDB.CORE_EVOLUTIONARY || thisRegion == ResidueDB.CORE_GEOMETRY) {
				int mapped = mappings.get(whichDatapoint).get(whichSide).get(i);
				coreEntropies.add(MSAs.get(whichDatapoint).get(whichSide).getColumnEntropy(mapped, whichAlphabet));
			}
			else if (thisRegion == ResidueDB.SURFACE) {
				surfaceEntropies.add(null);
			}
		}
		ArrayList<ArrayList<Double>> entropiesPair = new ArrayList<ArrayList<Double>>();
		entropiesPair.add(coreEntropies);
		entropiesPair.add(surfaceEntropies);
		return entropiesPair;
	}
	
	private static void optimiseAlphabet() {
//		System.out.println(truths.size());
//		System.out.println(MSAs.size());
//		System.out.println(regions.size());
//		System.out.println(mappings.size());
		
		/*
		 * overall strategy here:
		 * this function should create alphabets and call another function to get their fitness (testFitness())
		 * testFitness() should simply be the sampling of appropriate entropy values
		 * testFitness() should call a third function (getEntropies()), which returns entropy values separated by region 
		 */
		
		
//		Set<AAAlphabet> alphabetPool = initialisePool();
//		for (AAAlphabet alphabet : alphabetPool) {
//			System.out.println(alphabet + "\t" + testFitness(alphabet));
//		}
//		for (int i = 1; i <= ROUNDS_OF_EVOLUTION; i++) {
//			alphabetPool = evolve(alphabetPool);
//			System.out.println("\nAfter " + i + " rounds of evolution...");
//			for (AAAlphabet alphabet : alphabetPool) {
//				System.out.println(alphabet + "\t" + testFitness(alphabet));
//			}
//		}
	}
	
	private static Set<AAAlphabet> initialisePool() {
		Set<AAAlphabet> pool = new HashSet<AAAlphabet>();
		for (int i = 0; i < INITIAL_POPULATION_SIZE; i++) {
			pool.add(new AAAlphabet(AAAlphabet.STANDARD_20));
		}
		return pool;
	}
	
	private static int testFitness(AAAlphabet alphabet) {
//		this will eventually be replaced by something that evaluates the scores for each pdbid
//		going to have to take the alphabet as a parameter
//		will then use the alphabet to calculate entropies for the global static MSAs
//		will then need to sample them while referring to regions and calculate score
//		maybe that last part can be done using the code already in EPPIC
		
//		don't use the String pdbResidueNumber from the ResidueDB, it's not always an int...
//		use the int residueNumber field from ResidueDB instead
//		also, for regions, anything "2" is also "3", since the geom
//		cutoff is higher (more stringent) than the evol one...
//		so to look at core vs surface, need to look at 0 vs 2+3
		
		int fitness = 1;
		if (alphabet.getGroupByOneLetterCode('D') == alphabet.getGroupByOneLetterCode('E')) fitness++;
		if (alphabet.getGroupByOneLetterCode('H') == alphabet.getGroupByOneLetterCode('K')) fitness++;
		if (alphabet.getGroupByOneLetterCode('H') == alphabet.getGroupByOneLetterCode('R')) fitness++;
		if (alphabet.getGroupByOneLetterCode('K') == alphabet.getGroupByOneLetterCode('R')) fitness++;
		if (alphabet.getGroupByOneLetterCode('N') == alphabet.getGroupByOneLetterCode('Q')) fitness++;
		if (alphabet.getGroupByOneLetterCode('L') == alphabet.getGroupByOneLetterCode('I')) fitness++;
		return fitness;
	}
	
	private static Set<AAAlphabet> evolve(Set<AAAlphabet> oldPool) {
		Set<AAAlphabet> newPool = new HashSet<AAAlphabet>();
		for (AAAlphabet alphabet : oldPool) {
			int fitness = testFitness(alphabet);
			for (int i = 0; i < fitness; i++) {
				newPool.add(mutateAlphabet(alphabet));
			}
		}
		while (newPool.size() > CARRYING_CAPACITY) {
			AAAlphabet worstAlphabet = null;
			int worstFitness = Integer.MAX_VALUE;
			for (AAAlphabet anAlphabet : newPool) {
				if (testFitness(anAlphabet) < worstFitness) {
					worstAlphabet = anAlphabet;
					worstFitness = testFitness(anAlphabet);
				}
			}
			newPool.remove(worstAlphabet);
		}
		return newPool;
	}
	
	private static AAAlphabet mutateAlphabet(AAAlphabet alphabet) {
		ArrayList<String> groups = new ArrayList<String>(Arrays.asList(alphabet.getGroups()));
		while (groups.size() < 20) {
			groups.add("");
		}
		Random rand = new Random();
		String targetAA = AMINO_ACIDS[rand.nextInt(AMINO_ACIDS.length)];
		int moveFrom = -1;
		int moveTo = rand.nextInt(AMINO_ACIDS.length);
		for (int i = 0; i < groups.size(); i++) {
			if (groups.get(i).contains(targetAA)) {
				moveFrom = i;
				break;
			}
		}
		groups.set(moveFrom, groups.get(moveFrom).replace(targetAA, ""));
		groups.set(moveTo, groups.get(moveTo).concat(targetAA));
		String newString = "";
		for (String group : groups) {
			if (!group.isEmpty()) {
				newString += group;
				newString += ":";
			}
		}
		return new AAAlphabet(newString.substring(0, newString.length() - 1));
	}
}