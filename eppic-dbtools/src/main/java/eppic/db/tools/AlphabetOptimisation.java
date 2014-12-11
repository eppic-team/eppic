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
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import owl.core.sequence.alignment.MultipleSequenceAlignment;
import owl.core.structure.AAAlphabet;

public class AlphabetOptimisation {
	
	//public static final double CORE_RIM_CUTOFF = 0.80;
	public static final double CORE_SURFACE_CUTOFF = 0.80;
	public static final double GEOM_CUTOFF = 0.90;
	public static final double MIN_ASA_FOR_SURFACE = 5;
	public static final String[] AMINO_ACIDS = {"A", "C", "D", "E", "F", "G", "H", "I", "K", "L", "M", "N", "P", "Q", "R", "S", "T", "V", "W", "Y"};
	public static final int CARRYING_CAPACITY = 10;
	public static final int INITIAL_POPULATION_SIZE = 10;
	public static final int ROUNDS_OF_EVOLUTION = 50;

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
				break; // getopt() already printed an error
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
		
		DBHandler dbh = new DBHandler(dbName);
		
		List<ArrayList<String>> listList = readList(listFile);
		List<String> pdbCodes = listList.get(0);
		List<String> firstChains = listList.get(1);
		List<String> secondChains = listList.get(2);
		List<String> interfaceNumbers = listList.get(3);
		List<String> trueCalls = listList.get(4);
		
		ArrayList<ArrayList<MultipleSequenceAlignment>> MSAs = new ArrayList<ArrayList<MultipleSequenceAlignment>>();
		
		ArrayList<ArrayList<Integer>> allResidueTypes = new ArrayList<ArrayList<Integer>>();

		List<PdbInfoDB> pdbInfos = dbh.deserializePdbList(pdbCodes);
		
		//for (int i = 0; i < pdbInfos.size(); i++) {
		for (int i = 0; i < 1; i++) {
			System.out.println(i);
			PdbInfoDB pdbInfo = pdbInfos.get(i);
			String firstChain = firstChains.get(i);
			String secondChain = secondChains.get(i);
			int interfaceNumber = Integer.parseInt(interfaceNumbers.get(i));
			boolean trueCall = trueCalls.get(i).equals("bio");
			
			List<ChainClusterDB> chainClusters = pdbInfo.getChainClusters();
			ChainClusterDB firstChainCluster = getChainClusterFromChainClustersByChain(chainClusters, firstChain);
			ChainClusterDB secondChainCluster = getChainClusterFromChainClustersByChain(chainClusters, secondChain);
			
			List<InterfaceClusterDB> interfaceClusters = pdbInfo.getInterfaceClusters();
			InterfaceDB theInterface = getInterfaceFromInterfaceClustersByChainsAndId(interfaceClusters, interfaceNumber);
			List<ResidueDB> residues = theInterface.getResidues();
			
			for (ResidueDB testRes : residues) {
				System.out.println(testRes.getPdbResidueNumber() + " " + testRes.getResidueNumber() + " " + testRes.getResidueType() + " " + testRes.getAsa() + " " + testRes.getBsa() + " " + getRegion(testRes.getAsa(), testRes.getBsa()) + " " + testRes.getSide());
			}
			
			ArrayList<Integer> residueTypes = new ArrayList<Integer>();
			for (int j = 0; j < residues.size(); j++) {
				ResidueDB residue = residues.get(j);
				residueTypes.add(getRegion(residue.getAsa(), residue.getBsa()));
			}
			System.out.println("residues.size() = " + residues.size());
			allResidueTypes.add(residueTypes);
			
			List<HomologDB> firstHomologs = firstChainCluster.getHomologs();
			List<HomologDB> secondHomologs = secondChainCluster.getHomologs();
			
			String[] firstHomologTags = new String[firstHomologs.size() + 1];
			String[] firstHomologSeqs = new String[firstHomologs.size() + 1];
			String[] secondHomologTags = new String[secondHomologs.size() + 1];
			String[] secondHomologSeqs = new String[secondHomologs.size() + 1];
			
			firstHomologTags[0] = firstChainCluster.getRefUniProtId() + "_" + firstChainCluster.getRefUniProtStart() + "-" + firstChainCluster.getRefUniProtEnd();
			firstHomologSeqs[0] = firstChainCluster.getMsaAlignedSeq();
			secondHomologTags[0] = secondChainCluster.getRefUniProtId() + "_" + secondChainCluster.getRefUniProtStart() + "-" + secondChainCluster.getRefUniProtEnd();
			secondHomologSeqs[0] = secondChainCluster.getMsaAlignedSeq();
			
			System.out.println("first PDB = " + firstChainCluster.getPdbStart() + "-" + firstChainCluster.getPdbEnd());
			System.out.println("first REF = " + firstChainCluster.getRefUniProtStart() + "-" + firstChainCluster.getRefUniProtEnd());
			
			System.out.println("msa length = " + firstChainCluster.getMsaAlignedSeq().length());
			
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
			
			ArrayList<MultipleSequenceAlignment> pair = new ArrayList<MultipleSequenceAlignment>();
			pair.add(new MultipleSequenceAlignment(firstHomologTags, firstHomologSeqs));
			pair.add(new MultipleSequenceAlignment(secondHomologTags, secondHomologSeqs));
			
			MSAs.add(pair);
			
			int firstLowestPdb = Integer.MAX_VALUE;
			for (ResidueDB aResidue : residues) {
				if (aResidue.getSide() == 1 && Integer.parseInt(aResidue.getPdbResidueNumber()) < firstLowestPdb) {
					firstLowestPdb = Integer.parseInt(aResidue.getPdbResidueNumber());
				}
			}
			int firstHighestPdb = Integer.MIN_VALUE;
			for (ResidueDB aResidue : residues) {
				if (aResidue.getSide() == 1 && Integer.parseInt(aResidue.getPdbResidueNumber()) > firstLowestPdb) {
					firstHighestPdb = Integer.parseInt(aResidue.getPdbResidueNumber());
				}
			}
			for (int j = firstLowestPdb; j <= firstHighestPdb; j++) {
				int msaResSeqIndex = j - (firstChainCluster.getRefUniProtStart() - firstChainCluster.getPdbStart());
				char msaChar = pair.get(0).getColumn(pair.get(0).seq2al(pair.get(0).getTagFromIndex(0), msaResSeqIndex)).charAt(0);
				ResidueDB jRes = null;
				for (ResidueDB aRes : residues) {
					if (Integer.parseInt(aRes.getPdbResidueNumber()) == j && aRes.getSide() == 1) {
						jRes = aRes;
					}
				}
				System.out.println(msaChar + "\t" + jRes.getResidueType());
			}
		}
		
		/*
		 * Note to self for after back from vacation
		 * 
		 * alphabet evolution thing looks okay (that's what's below here)
		 * for the mapping of the residue information (extracted from interfaces)
		 * onto the MSA (extracted from homologues from chaincluster)
		 * need to fix this...really weird now
		 * PDB vs REF seqences in chaincluster can have gaps in the alignment!!
		 * therefore, must first map the PDB to the REF properly (not JUST by difference in indices [shift])
		 * and THEN once that's done, can use MSAalignedseq indices to map onto MSA itself
		 * 
		 * entire idea here: have two things extracted in parallel and then mapped
		 * need to basically assign a label (region) to each column in MSA
		 * then calculated core--surface score by looking at difference of entropies between labels
		 * 
		 * note also: don't don't don't use the String pdbResidueNumber from the ResidueDB
		 * this can't be used for ANYTHING numeric
		 * use the int residueNumber field from ResidueDB instead, it's the only one that's reliably sequential
		 * that needs to be fixed in the above code...along with lots more 
		 * 
		 * okay also note that the region labels are a bit odd...anything "2" is also "3", since the geom
		 * cutoff is higher (more stringent) than the evol one...so if you want to look at core vs surface
		 * need to look at 0 vs 2+3
		 * 
		 */
		
		
		
		
		
		
		for (int i = 0; i < 1; i++) {
			System.out.println(pdbCodes.get(i) + " " + interfaceNumbers.get(i) + " left");
			MSAs.get(i).get(0).printSimple();
			System.out.println(pdbCodes.get(i) + " " + interfaceNumbers.get(i) + " right");
			MSAs.get(i).get(1).printSimple();
		}
		
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
			pool.add(new AAAlphabet(AAAlphabet.MURPHY_20));
		}
		return pool;
	}
	
	private static int testFitness(AAAlphabet alphabet) {
		// this will eventually be replaced by something that evaluates the scores for each pdbid
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
	
	private static InterfaceDB getInterfaceFromInterfaceClustersByChainsAndId(List<InterfaceClusterDB> clusters, int interfaceId) {
		InterfaceDB toReturn = null;
		for (InterfaceClusterDB interfaceCluster : clusters) {
			for (InterfaceDB anInterface : interfaceCluster.getInterfaces()) {
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
			if (ratio < CORE_SURFACE_CUTOFF) {
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
}
