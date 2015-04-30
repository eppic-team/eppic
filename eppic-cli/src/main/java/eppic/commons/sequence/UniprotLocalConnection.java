package eppic.commons.sequence;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eppic.commons.util.MySQLConnection;


/**
 * Class to get UniProt Knowledge Base info from a local MySQL database. 
 * 
 * Two sources of data are needed: 
 * 
 * 1) a UniRef database downloaded from 
 * ftp://ftp.uniprot.org/pub/databases/uniprot/uniref/uniref100/ 
 * or for archived ones from
 * ftp://ftp.uniprot.org/pub/databases/uniprot/previous_releases/
 * and then parsed with {@link UnirefXMLParser}, containing a minimal subset of the 
 * UniProt Knowledge Base.
 * 
 * 2) a NEWT UniProt taxonomy database as downloaded from http://www.uniprot.org/taxonomy
 * 
 * The local database must be named like uniprot_yyyy_mm or for the older style versions (e.g. 15.3)
 * the name convention must be uniprot_xx_y. The tables of the database must be named "uniprot", 
 * "uniprot_clusters" and "taxonomy"
 * 
 * @see UniProtConnection
 * @see UnirefXMLParser
 * @author duarte_j
 *
 */
public class UniprotLocalConnection {

	private static final String DATA_TABLE = "uniprot"; 
	private static final String CLUSTERS_TABLE = "uniprot_clusters";
	private static final String TAX_TABLE = "taxonomy";
	
	private static final Logger LOGGER = LoggerFactory.getLogger(UniprotLocalConnection.class);
	
	private class TaxonomyRecord {
		
		@SuppressWarnings("unused")
		public String scientificName;
		public List<String> taxons;
		public TaxonomyRecord(String scientificName, List<String> taxons) {
			this.scientificName = scientificName;
			this.taxons = taxons;
		}
	}
	
	
	
	private MySQLConnection conn;
	private String dbName;
	private String uniprotVer;
	
	private HashSet<String> nonReturnedIdsLastMultipleRequest;
	
	public UniprotLocalConnection(String dbName) throws SQLException {
		
		conn = new MySQLConnection();
		
		this.dbName = dbName;
		
		if (!dbName.startsWith("uniprot_")) {
			throw new IllegalArgumentException("The local UniProt db name must start with 'uniprot_'");
		}
		
		this.uniprotVer = dbName.substring(dbName.indexOf('_')+1, dbName.length());
		if (this.uniprotVer.length()<7) { // for old style (pre 2010) version numbers
			// version numbers are 7 characters (2010_01) but older style uniprot version numbers (15.3) are only 3 to 4 chars
			this.uniprotVer = uniprotVer.replace("_", "."); 
		}
		
	}
	
	public String getVersion() {
		return uniprotVer;
	}
	
	public void close() {
		this.conn.close();
	}
	
	/**
	 * Given a uniprot or uniparc id returns the corresponding UnirefEntry record
	 * @param uniId
	 * @return
	 * @throws SQLException if something goes wrong while querying or if multiple matches are returned
	 * @throws NoMatchFoundException if no match found
	 */
	public UnirefEntry getUnirefEntry(String uniId) throws SQLException, NoMatchFoundException {
		String repId = null;
		String idColumn = null;
		int taxId = -1;
		String sequence = null;
		String uniprotId = null;
		String uniparcId = null;
		
		if (uniId.startsWith("UPI")) {
			// uniparc id
			repId = uniId;
			idColumn = "uniparc_id";
			uniparcId = uniId; 
		} else {
			// uniprot id
			UnirefEntryClusterMember repId_taxId = getRepresentative(uniId);
			repId = repId_taxId.getUniprotId();
			taxId = repId_taxId.getNcbiTaxId();
			idColumn = "uniprot_id";
			uniprotId = uniId;
		}
		
		Statement st = conn.createStatement();
		String sql = "SELECT tax_id, sequence FROM "+dbName+"."+DATA_TABLE+" WHERE "+idColumn+"='"+repId+"'";
		ResultSet rs = st.executeQuery(sql);
		
		int count = 0;
		while (rs.next()) {
			if (uniprotId==null) {
				// we take the tax_id from this table only when input was a uniparc
				taxId = rs.getInt(1);
			}
			sequence = rs.getString(2);
			count++;
		}
		rs.close();
		st.close();
		if (sequence==null) 
			throw new NoMatchFoundException("No match in table "+dbName+"."+DATA_TABLE+" for id "+repId);
		if (count>1) 
			throw new SQLException("Multiple matches in table "+dbName+"."+DATA_TABLE+" for id "+repId);
		
		UnirefEntry uniref = new UnirefEntry();
		uniref.setUniprotId(uniprotId);
		uniref.setUniparcId(uniparcId);
		uniref.setNcbiTaxId(taxId);
		uniref.setSequence(sequence);
		
		TaxonomyRecord tax = getTaxonomy(uniref.getNcbiTaxId());
		if (tax!=null) {
			uniref.setTaxons(tax.taxons);
		} else {
			LOGGER.info("No taxonomy information could be found for uniprot/uniparc id "+uniref.getUniId()+" (tax_id="+uniref.getNcbiTaxId()+")");
		}
		
		return uniref;
	}
	
	private UnirefEntryClusterMember getRepresentative(String uniId) throws SQLException, NoMatchFoundException {
		Statement st = conn.createStatement();
		String sql = "SELECT representative, tax_id FROM "+dbName+"."+CLUSTERS_TABLE+" WHERE member='"+uniId+"'";
		ResultSet rs = st.executeQuery(sql);
		String repUniId = null;
		int memberTaxId = -1;
		int count = 0;
		while (rs.next()) {
			repUniId = rs.getString(1);
			memberTaxId = rs.getInt(2);
			count++;
		}
		rs.close();
		st.close();
		if (repUniId==null) 
			throw new NoMatchFoundException("No match in clusters table "+dbName+"."+CLUSTERS_TABLE+" for uniprot id "+uniId);
		if (count>1) 
			throw new SQLException("Multiple matches in clusters table "+dbName+"."+CLUSTERS_TABLE+" for uniprot id "+uniId);
			
		// we abuse here the UnirefEntryClusterMember class to return the result in one object,
		// but it is not a correctly constructed object! because the uniprot id is the one of 
		// the representative and the tax id the one of the member!
		return new UnirefEntryClusterMember(repUniId, memberTaxId);
	}
	
	/**
	 * Given a list of uniprot or uniparc ids returns the corresponding UnirefEntry records.
	 * If the query does not return all requested ids a warning is logged and the list of non-returned 
	 * ids can be retrieved through {@link #getNonReturnedIdsLastMultipleRequest()}  
	 * @param uniIds
	 * @return
	 * @throws SQLException
	 */
	public List<UnirefEntry> getMultipleUnirefEntries(List<String> uniIds) throws SQLException {
		nonReturnedIdsLastMultipleRequest = new HashSet<String>();
		
		List<UnirefEntry> entries = new ArrayList<UnirefEntry>();
		for (String uniId:uniIds) {			
			try {
				entries.add(getUnirefEntry(uniId));
			} catch (NoMatchFoundException e) {
				nonReturnedIdsLastMultipleRequest.add(uniId);
				LOGGER.warn("Information for uniprot/uniparc ID "+uniId+" could not be retrieved from local Uniprot.");
			}
			
		}
		return entries;
	}
	
	public HashSet<String> getNonReturnedIdsLastMultipleRequest() {
		return nonReturnedIdsLastMultipleRequest;
	}
	
	public TaxonomyRecord getTaxonomy(int taxId) throws SQLException {
		if (taxId==0) return null;
		
		Statement st = conn.createStatement();
		String sql = "SELECT scientific,lineage FROM "+dbName+"."+TAX_TABLE+" WHERE tax_id="+taxId; 
		ResultSet rs = st.executeQuery(sql);

		String scientific = null;
		String lineage = null;
		while (rs.next()) {
			 scientific = rs.getString(1);
			 lineage = rs.getString(2);
		}
		rs.close();
		st.close();
		
		if (scientific==null) 
			return null;

		String[] taxons = lineage.split("; ");
		List<String> taxonsAL = Arrays.asList(taxons);
		
		return new TaxonomyRecord(scientific, taxonsAL);
	}
	
	/**
	 * Dumps all sequences of the local uniprot database to a FASTA file with headers as the uniprot
	 * UniRef provided files, i.e. >UniRef100_UPI123456789
	 * @param file
	 * @throws SQLException
	 * @throws IOException
	 */
	public void dumpToFasta(File file) throws SQLException, IOException {
		
		PrintStream out = new PrintStream(file);		
		
		Statement st = conn.createStatement();
		// this is important: by default query results are stored completely in memory, but with this amount of data we can't do that
		// with this we read row by row instead of storing in memory first
		// see http://benjchristensen.com/2008/05/27/mysql-jdbc-memory-usage-on-large-resultset/
		st.setFetchSize(Integer.MIN_VALUE);  
		String sql = "SELECT id,uniprot_id,uniparc_id,sequence FROM "+dbName+"."+DATA_TABLE; 
		ResultSet rs = st.executeQuery(sql);
		String id = null;
		String uniprotId = null;
		String uniparcId = null;
		String sequence = null;

		while (rs.next()) {
			 id = rs.getString(1);
			 uniprotId = rs.getString(2);
			 uniparcId = rs.getString(3);
			 sequence = rs.getString(4);
			 printFastaEntry(out, id, uniprotId, uniparcId, sequence);
		}
		rs.close();
		st.close();
		
		out.close();
		
	}
	
	private void printFastaEntry(PrintStream out, String id, String uniprotId, String uniparcId, String sequence) {
		//>UniRef100_UPI00024B9D0B
		
		String header = null;
		if (id.startsWith("UniRef100_")) {
			header = ">"+id;	
		} else {
			String uniId = null;
			if (uniprotId==null) uniId = uniparcId;
			else uniId = uniprotId;
				
			header = ">UniRef100_"+uniId;
		}
		out.println(header);
		
		int len = 60; 
		for(int i=0; i<sequence.length(); i+=len) {
			out.println(sequence.substring(i, Math.min(i+len,sequence.length())));
		}		
		
	}
	
	/**
	 * Given a local uniprot database name and an output fasta file dumps all sequences 
	 * in fasta format with headers as the ones used by UniProt for its UniRef files 
	 * @param args
	 * @throws SQLException
	 * @throws IOException
	 */
	public static void main(String[] args) throws SQLException, IOException {
		if (args.length<2) {
			System.err.println("Usage: UniprotLocalConnection <database name> <file to dump fasta sequences>");
			System.exit(1);
		}
		UniprotLocalConnection upl = new UniprotLocalConnection(args[0]);
		upl.dumpToFasta(new File(args[1]));
	}
}
