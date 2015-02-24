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
	public static ArrayList<ArrayList<HashMap<Integer, Integer>>> mappings; // THIS IS A LIST OF PAIRS OF MAPPINGS FROM MSA_COL TO RES_ID

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
		
//		for (int i = 0; i < pdbInfos.size(); i++) {
		for (int i = 0; i < 1; i++) {
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
			System.out.println("query-first-seq " + firstChainCluster.getMsaAlignedSeq());
			secondHomologTags[0] = secondChainCluster.getRefUniProtId() + "_" + secondChainCluster.getRefUniProtStart() + "-" + secondChainCluster.getRefUniProtEnd();
			secondHomologSeqs[0] = secondChainCluster.getMsaAlignedSeq();
			//System.out.println("query-second-seq " + secondChainCluster.getMsaAlignedSeq());
			
			// CREATE LEFT AND RIGHT MAPPINGS FROM MULTIPLESEQUENCEALIGNMENT COLUMN NUMBERS TO INTERFACE RESIDUE NUMBERS
			HashMap<Integer, Integer> firstMapping = new HashMap<Integer, Integer>();
			HashMap<Integer, Integer> secondMapping = new HashMap<Integer, Integer>();
			// populate mappings here :P
			
			// CREATE PAIR (LEFT, RIGHT) OF MAPPINGS
			ArrayList<HashMap<Integer, Integer>> mappingPair = new ArrayList<HashMap<Integer, Integer>>();
			mappingPair.add(firstMapping);
			mappingPair.add(secondMapping);
			
			// ADD PAIR OF LISTS OF REGIONS TO LIST OF PAIRS OF LISTS OF REGIONS
			mappings.add(mappingPair);
			
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
					System.out.println(aResidue.getResidueType() + aResidue.getResidueNumber() + " ");
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
		}
		
		// RUN ALPHABET OPTIMISATION
		System.out.println(truths);
//		for (ArrayList<MultipleSequenceAlignment> msaPair : MSAs) {
//			for (MultipleSequenceAlignment msa : msaPair) {
//				msa.printSimple();
//			}
//		}
		System.out.println(regions);
		//optimiseAlphabet();
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
	
	private static void optimiseAlphabet() {
		Set<AAAlphabet> alphabetPool = initialisePool();
		for (AAAlphabet alphabet : alphabetPool) {
			System.out.println(alphabet + "\t" + testFitness(alphabet));
		}
		for (int i = 1; i <= ROUNDS_OF_EVOLUTION; i++) {
			alphabetPool = evolve(alphabetPool);
			System.out.println("\nAfter " + i + " rounds of evolution...");
			for (AAAlphabet alphabet : alphabetPool) {
				System.out.println(alphabet + "\t" + testFitness(alphabet));
			}
		}
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