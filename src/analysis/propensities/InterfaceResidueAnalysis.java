package analysis.propensities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.TreeMap;

import analysis.Utils;

import crk.CRKException;

import owl.core.structure.*;
import owl.core.util.FileFormatException;

/**
 * Identification of core residues at the interface of two chains
 * Loads structure from pdb file, finds core residues at the interface, write core residues to file 
 * @author biyani_n
 * @version 15.11.2012
 */

public class InterfaceResidueAnalysis {

	/**
	 * @param args
	 */
	
	private static ProteinFrequencies globalBio = new ProteinFrequencies();
	private static ProteinFrequencies globalXtal = new ProteinFrequencies();
	private static CoreEnrichments coreEnrich = new CoreEnrichments();
	
	private static double coreCutOff;            			//Core BSA/ASA ratio cutoff
	private static String dataSet;
	private static String outDir;
	private static String dataBioDir;
	
	//Methods
	public static PdbAsymUnit loadPdbFile(String pdbCode) throws CRKException, IOException, FileFormatException, PdbLoadException {
		System.out.println("Loading PDB data: " + pdbCode);
		String pdbFtpCifUrl = "";
		String localCifDir = "/nfs/data/dbs/pdb/data/structures/all/mmCIF";
		File cifFile = null;
		cifFile = new File(pdbCode + ".cif");
		cifFile.deleteOnExit();
		PdbAsymUnit.grabCifFile(localCifDir, pdbFtpCifUrl, pdbCode, cifFile, false);
		PdbAsymUnit fullpdb = new PdbAsymUnit(cifFile, PdbAsymUnit.DEFAULT_MODEL, false);
		
		// Strip the H atoms
		fullpdb.removeHatoms();
		
		return fullpdb;
	}
	
	private static void calFrequencies(ChainInterface interFace, int iChain, boolean ifBio){
		ProteinFrequencies local = new ProteinFrequencies();
		PdbChain chain = interFace.getFirstMolecule();
		if (iChain == 2) chain = interFace.getSecondMolecule();
		
		//For all residues in the chain calculate the frequencies
		for (Residue res:chain){
			if (res.getShortCode() != 'X'){
				AminoAcid aa = AminoAcid.getByOneLetterCode(res.getShortCode());
				//Calculate the frequency of all residues in chain
				local.full.addResidue(aa);
		
				//Calculate the frequency of surface residues
				if(res.getAsa() > 5) local.surface.addResidue(aa);
			
				//Calculate the frequency of interface core residues
				if(res.getBsaToAsaRatio() > coreCutOff && res.getAsa() >5) local.interfCore.addResidue(aa);
				
				//Calculate the frequency of protein core residues
				if(res.getAsa() <=5 ) local.chainCore.addResidue(aa);
			}
		}
		//Assign to global counters
		if(ifBio) {
			globalBio.addData(local);
			coreEnrich.addData(local);
		}
		else globalXtal.addData(local);
	}
	
	public static void printFiles() throws IOException{
		System.out.println("Writing Files in Directory: ");
		System.out.println(outDir);
		
		//Write Summary File
		File outFileSummary = new File (outDir+dataSet+"_Summary.dat");
		PrintWriter outSumm = new PrintWriter(outFileSummary);
		
		outSumm.println("-------------------------------------------------------");
		outSumm.println(" ENRICHMENTS ");
		outSumm.println("-------------------------------------------------------");
		globalBio.printEnrichments(outSumm);
		
		outSumm.println("-------------------------------------------------------");
		outSumm.println(" RELATIVE ABUNDANCE ");
		outSumm.println("-------------------------------------------------------");
		coreEnrich.printData(outSumm);
		
		outSumm.println("-------------------------------------------------------");
		outSumm.println(" PROPERTIES ");
		outSumm.println("-------------------------------------------------------");
		globalBio.printProperties(outSumm);
		
		outSumm.println("-------------------------------------------------------");
		outSumm.println(" PROPENSITIES ");
		outSumm.println("-------------------------------------------------------");
		globalBio.printPropensities(outSumm);
		
		//outSumm.println("-------------------------------------------------------");
		//outSumm.println("        CRYSTAL CONTACTS SUMMARY                       ");
		//outSumm.println("-------------------------------------------------------");
		//globalXtal.printData(outSumm);
		outSumm.close();
		
		File outFileEnrichments = new File (outDir+dataSet+"_Enrichments.dat");
		PrintWriter outEnrich = new PrintWriter(outFileEnrichments);
		globalBio.printEnrichmentsTable(outEnrich);
		outEnrich.close();
		
		File outFilePropensities = new File (outDir+dataSet+"_Propensities.dat");
		PrintWriter outProp = new PrintWriter(outFilePropensities);
		globalBio.printPropensitiesTable(outProp);
		outProp.close();
		
		File outFileProperties = new File (outDir+dataSet+"_Properties.dat");
		PrintWriter outProperty = new PrintWriter(outFileProperties);
		globalBio.printProperties(outProperty);
		outProperty.close();
		
		File outFileRelAbun = new File (outDir+dataSet+"_RelAbundance.dat");
		PrintWriter outAbun = new PrintWriter(outFileRelAbun);
		coreEnrich.printDataTable(outAbun);
		outAbun.close();
		
	}
	
	public static void readInput(File inputFile) throws IOException{
		
		BufferedReader br = new BufferedReader(new FileReader(inputFile));
		String line;
		String variable = " ";
		while ((line=br.readLine())!=null){
			if (line.startsWith("#")) { variable=line; continue; }
			if (line.trim().isEmpty()) continue;
			
			if(variable.equals("#DATASET NAME")) dataSet = line;
			if(variable.equals("#DATASET PATH")) dataBioDir = line;
			if(variable.equals("#OUTPUT PATH")) outDir = line;
			if(variable.equals("#CORE CUT OFF")) coreCutOff = Double.parseDouble(line);
			
		}
		br.close();
	}
	
	public static void main(String[] args) throws IOException, CRKException, FileFormatException, PdbLoadException {		
		System.out.println("Reading input from: "+args[0]);
		File inputFile = new File(args[0]);
		readInput(inputFile);
		
		System.out.println(" Dataset name: "+ dataSet);
		System.out.println(" Dataset File Path: "+ dataBioDir);
		System.out.println(" Output Directory: "+ outDir);
		System.out.println(" Core-Cut-Off used: "+ coreCutOff);
		System.out.print('\n');
		
		File dataBio=new File(dataBioDir);			
		TreeMap<String,List<Integer>> bioToAnalyse = Utils.readListFile(dataBio);
		System.out.println("Reading pdb codes from "+dataBioDir);
		System.out.println("Total number of pdb entries in file: "+ bioToAnalyse.size());
		
		for(String pdbCode:bioToAnalyse.keySet()) {
			//Load PDB File
			System.out.println("------------------------------------------------------------------------");
			System.out.println("Reading and processing " + pdbCode);
			PdbAsymUnit fullpdb = loadPdbFile(pdbCode);
			
			//Calculate all the interfaces for the pdb structure
			System.out.println("Calculating all Interfaces ....");
			ChainInterfaceList allInterFaces = fullpdb.getAllInterfaces(6, 3000, 1, true, false, -1);
			
			//Get number of Bio Interfaces
			int bioInterFaces = bioToAnalyse.get(pdbCode).size();
			System.out.println("Total number of relevent interfaces: " + bioInterFaces);
			
			//Calculate the frequencies for bio and xtal separately
			int countInterFace = 0;
			boolean isBio = false;
			for (ChainInterface interFace:allInterFaces) {
				countInterFace++;
				if(countInterFace <= bioInterFaces) isBio = true;
				else isBio = false;
				//Calculate the frequencies of residues in various regions for both chains
				for(int iChain=1; iChain<=2; iChain++) calFrequencies(interFace, iChain, isBio);
			}
		}//end of calculations
		
		printFiles();
		System.out.println("------------------------------------------------------------------------");
		System.out.println("DONE !!!!");
	
	}

}
