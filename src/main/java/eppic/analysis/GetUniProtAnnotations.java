package eppic.analysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import owl.core.connections.NoMatchFoundException;
import owl.core.connections.UniProtConnection;
//import uk.ac.ebi.kraken.interfaces.uniprot.Keyword;
import uk.ac.ebi.kraken.interfaces.uniprot.UniProtEntry;
//import uk.ac.ebi.kraken.interfaces.uniprot.dbx.go.Go;
import uk.ac.ebi.kraken.interfaces.uniprot.description.Field;
import uk.ac.ebi.kraken.interfaces.uniprot.description.FieldType;
import uk.ac.ebi.kraken.interfaces.uniprot.description.Name;

/**
 * A script to get UniProt annotations (all names) from UniProt JAPI
 * for uniprot ids read from a file containing 3 columns: pdb_code uniprotId1 uniprotId2
 * It will count also all cases where any part of the name matches an antibody or MHC term. 
 * This is useful to check whether in a set of interfaces there is any with 
 * MHCs or antibody sequences, which at the moment EPPIC can't treat correctly.
 * 
 * @author duarte_j
 *
 */
public class GetUniProtAnnotations {

	public static void main(String[] args) throws Exception {

		File file = new File(args[0]);
		
		List<String[]> list = parse(file);
		
		UniProtConnection upc = new UniProtConnection();
		
		int countTotal = 0;
		int countMatching = 0;
		
		for (String[] entry:list) {
			
			String pdbCode = entry[0];
			String uniProtId1 = entry[1];
			String uniProtId2 = entry[2];
			
			System.out.println(pdbCode);
			
			try {
				boolean firstMatch = checkAnnotation(upc, uniProtId1);
				boolean secondMatch = checkAnnotation(upc, uniProtId2);
				countTotal++;
				
				if (firstMatch || secondMatch) {
					countMatching++;
				}
				
			} catch (NoMatchFoundException e) {
				System.out.println("No match found "+e.getMessage());
				continue;
			}
			

		}
		
		double ratio = (double) countMatching/(double)countTotal;
		System.out.println("Total interfaces: "+countTotal);
		System.out.println("Total matches to antibodies/MHC (at least 1 chain): "+countMatching+
				" ("+String.format("%4.2f", ratio)+")");
		
	}
	
	private static List<String[]> parse(File file) throws IOException {
		
		List<String[]> list = new ArrayList<String[]>();
		BufferedReader br = new BufferedReader(new FileReader(file));
		
		String line;
		
		while ((line=br.readLine())!=null) {
			list.add(line.split("\\s+"));
		}
		
		br.close();
		
		
		return list;
	}

	private static boolean checkAnnotation(UniProtConnection upc, String uniProtId) throws NoMatchFoundException {
		
		System.out.println("### "+uniProtId);
		
		UniProtEntry up = upc.getEntry(uniProtId);

		Name name = up.getProteinDescription().getRecommendedName();
		List<Name> altNames = up.getProteinDescription().getAlternativeNames();
		List<Name> subNames = up.getProteinDescription().getSubNames();
		
		
		List<Name> allNames = new ArrayList<Name>();
		allNames.add(name);
		allNames.addAll(altNames);
		allNames.addAll(subNames);
		
		boolean matches = false;
		
		System.out.print("Names: ");
		for (Name n:allNames) {
			List<Field> fields = n.getFieldsByType(FieldType.FULL);
			for (Field field:fields) {
				System.out.print(field.getValue()+" ");
				if (field.getValue().matches(".*\\bIg\\b.*")) return true;
				if (field.getValue().matches(".*MHC.*")) return true;
			}
		}
		System.out.println();
		
		return matches;
		
//		System.out.print("Keywords: ");
//		List<Keyword> keywords = up.getKeywords();
//		for (Keyword k:keywords) {
//			System.out.print(k.getValue()+" ");
//		}
//		System.out.println();
		
		
//		System.out.print("GO terms: ");
//		List<Go> terms = up.getGoTerms();
//		
//		for (Go term:terms) {
//			System.out.print(term.getGoTerm().getValue()+" ");
//		}
//		System.out.println();
	}
}
