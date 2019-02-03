package eppic.commons.sequence;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eppic.commons.util.Interval;



/**
 * Connection class to get data from EBI's SIFTS resource mapping PDB chains to Uniprot entries.
 * This seems to be at the moment the gold standard for PDB to Uniprot mapping. The class loads
 * the results from a given URL or file pointer and caches the results so that subsequent queries
 * are done in O(1) time (for the price of memory consumption).
 * See http://www.ebi.ac.uk/msd/sifts
 * 
 * @author duarte, stehr
 */
public class SiftsConnection {
	
	public static final String PDB2UNIPROT_URL = "ftp://ftp.ebi.ac.uk/pub/databases/msd/sifts/text/pdb_chain_uniprot.lst";

	private static final Pattern URL_PATTERN = Pattern.compile("^\\w+://.*"); 
	
	private Map<String,SiftsFeature> chain2uniprot;

	/**
	 * Constructs a SiftsConnection. Use {@link #parsePdb2Uniprot(String)} afterwards
	 */
	public SiftsConnection() {
		chain2uniprot = new HashMap<>();
	}
	
	/**
	 * Constructs a SiftsConnection parsing the SIFTS data and storing it.
	 * To access the SIFTS data use {@link #getMappings(String, String)}
	 * @param pdb2uniprotURL a URL pointing to the SIFTS pdb to uniprot mapping file or simply 
	 * a path to a local file
	 * @throws IOException
	 */
	public SiftsConnection(String pdb2uniprotURL) throws IOException{
		chain2uniprot = new HashMap<>();
		parsePdb2Uniprot(pdb2uniprotURL);
	}

	/**
	 * Parses the SIFTS table and stores pdb2uniprot and uniprot2pdb maps.
	 * @param fileURL a URL pointing to the SIFTS pdb to UniProt mapping file or
	 * a path to a local file
	 * @throws IOException
	 */
	protected void parsePdb2Uniprot(String fileURL) throws IOException {
		Reader reader = null;

		Matcher m = URL_PATTERN.matcher(fileURL);
		if (m.matches()) {
			// it is a URL
			URL pdb2enzymeURL = new URL(fileURL);
			URLConnection urlConn = pdb2enzymeURL.openConnection();
			reader = new InputStreamReader(urlConn.getInputStream());
		} else {
			// it is a file
			reader = new FileReader(new File(fileURL));
		}

		parsePdb2Uniprot(reader);
	}

	/**
	 * Parses the SIFTS table and stores pdb2uniprot and uniprot2pdb maps.
	 * @param reader a URL pointing to the SIFTS pdb to UniProt mapping file or
	 * a path to a local file
	 * @throws IOException
	 */
	protected void parsePdb2Uniprot(Reader reader) throws IOException {

		BufferedReader br = new BufferedReader(reader);
		String line;
		int lineCount = 0;
		while ((line=br.readLine())!=null) {
			lineCount++;
			if (line.startsWith("#") || line.startsWith("PDB") || line.trim().isEmpty()) continue;
			String[] fields = line.split("\\s+");
			
			if (fields.length!=9) {
				throw new IOException("The SIFTS file does not seem to be in the right format. Line "+lineCount+" does not have exactly 9 fields");
			}
			
			String pdbCode = fields[0];
			String pdbChainCode = fields[1];
			String id = pdbCode+pdbChainCode;
			String uniprotId = fields[2];
			int cifBeg = Integer.parseInt(fields[3]);
			int cifEnd = Integer.parseInt(fields[4]);
			//String pdbBeg = fields[5];
			//String pdbEnd = fields[6];
			int uniBeg = Integer.parseInt(fields[7]);
			int uniEnd = Integer.parseInt(fields[8]);
			
			SiftsFeature feat;
			// store pdb2uniprot record
			if (chain2uniprot.containsKey(id)) {
				feat = chain2uniprot.get(id);
			} else {
				feat = new SiftsFeature(pdbCode, pdbChainCode);
				chain2uniprot.put(id, feat);
			}
			feat.addSegment(uniprotId, cifBeg, cifEnd, uniBeg, uniEnd);

		}
		br.close();
	}

	/**
	 * Gets a list of SiftsFeatures for the given PDB chain, sorted 
	 * according to cif intervals, i.e. in the order that they appear in the PDB chain  
	 * @param pdbCode
	 * @param pdbChainCode
	 * @return
	 * @throws NoMatchFoundException if no matching UniProt entry is found
	 */
	public SiftsFeature getMappings(String pdbCode, String pdbChainCode) throws NoMatchFoundException{
		if (!chain2uniprot.containsKey(pdbCode+pdbChainCode)) 
			throw new NoMatchFoundException("No SIFTS mapping for PDB "+pdbCode+", chain "+pdbChainCode);
		SiftsFeature list = chain2uniprot.get(pdbCode+pdbChainCode);

		// TODO do we need this now?
		// before returning the list we make sure it is sorted based on the order of cif intervals, i.e. as they happen in PDB chain
		//list.sort(Comparator.comparing(o -> o.getCifIntervalSet().iterator().next()));
		return chain2uniprot.get(pdbCode+pdbChainCode);
	}
	
	/**
	 * Gets the Collection of all mappings for all PDB chains found in the SIFTS repository
	 * @return
	 */
	public Collection<SiftsFeature> getAllMappings() {
		return chain2uniprot.values();
	}
	
	/**
	 * Gets a Map with unique mappings of UniProt ids for all PDB chains found in the SIFTS repository
	 * as the keys and all its unique segments as the array list
	 * @param pdbIds restrict output mappings to this pdb ids, if null all mappings are output
	 * @return a map with keys uniprot ids and value a list of unique uniprot intervals for that uniprot id
	 */
	public Map<String, List<Interval>> getUniqueMappings(Set<String> pdbIds) {
		Map<String, List<Interval>> uniqueMap = new HashMap<>();

		for (SiftsFeature feat : chain2uniprot.values()) {
			if (pdbIds!=null && !pdbIds.contains(feat.getPdbCode()))
				continue;

			for (int i = 0; i<feat.getUniprotIntervalSet().size(); i++) {
				String uniProtId = feat.getUniprotIds().get(i);
				Interval uniProtInterv = feat.getUniprotIntervalSet().get(i);

				List<Interval> intervalsPerUniProt;
				if (uniqueMap.containsKey(uniProtId)) {
					intervalsPerUniProt = uniqueMap.get(uniProtId);
				} else {
					intervalsPerUniProt = new ArrayList<>();
					uniqueMap.put(uniProtId, intervalsPerUniProt);
				}

				if(!intervalsPerUniProt.contains(uniProtInterv))
					intervalsPerUniProt.add(uniProtInterv);
			}

		}
		
		return uniqueMap;
	}

	/**
	 * Gets a Map with unique mappings of UniProt ids for all PDB chains found in the SIFTS repository
	 * as the keys and all its unique segments as the array list
	 * @return a map with keys uniprot ids and value a list of unique uniprot intervals for that uniprot id
	 */
	public Map<String, List<Interval>> getUniqueMappings() {
		return getUniqueMappings(null);
	}
	
	/**
	 * Gets the total number of available pdb to UniProt mappings available in the SIFTS 
	 * database. A mapping is one pdbId+chainId (one mapping per unique chain).
	 * @return the count
	 */
	public int getMappingsCount() {
		return chain2uniprot.size();
	}

}
