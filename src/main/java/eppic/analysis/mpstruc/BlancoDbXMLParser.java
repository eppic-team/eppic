package eppic.analysis.mpstruc;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;


/**
 * A parser for Blanco DB of membrane proteins in XML format
 * 
 * @author duarte
 *
 */
public class BlancoDbXMLParser implements ContentHandler {

	// xml tags
	private static final String MPSTRUC_TAG = "mpstruc";
	private static final String TIMESTAMP_ATTRIBUTE = "timeStamp";
	private static final String GROUP_TAG = "group";
	private static final String SUBGROUP_TAG = "subgroup";
	private static final String NAME_TAG = "name";
	private static final String PROTEIN_TAG = "protein";
	private static final String PDBCODE_TAG = "pdbCode";
	private static final String RESOLUTION_TAG = "resolution";
	private static final String MEMBERPROTEINS_TAG = "memberProteins";
	
	private File blancoDbXMLFile;
	private InputSource input;
	
	private BlancoDb db;
	
	private BlancoCluster currentCluster;
	private BlancoEntry currentRepresentative;
	private BlancoEntry currentEntry;
	
	private String currentGroup;
	private String currentSubgroup;
	
	
	private StringBuffer buffer;
	private boolean inValue;
	
	private boolean inGroup;
	private boolean inSubgroup;
	private boolean inProtein;
	private boolean inMembers;
	
	
	/**
	 * Constructs new BlastXMLParser parsing the given Blast XML file
	 * Get the hits calling {@link #getHits()}
	 * @param blancoDbXMLFile
	 * @throws SAXException
	 * @throws IOException
	 */
	public BlancoDbXMLParser(File blancoDbXMLFile) throws SAXException, IOException{
		this.blancoDbXMLFile = blancoDbXMLFile;
		
		InputStream is = new FileInputStream(this.blancoDbXMLFile);
		this.input = new InputSource(is); 
			
		XMLReader parser = XMLReaderFactory.createXMLReader();
	 		
		parser.setContentHandler(this);
		
		parser.parse(input);

	}
	
	public BlancoDb getBlancoDb() {
		return db;
	}
	
	/**
	 * Initialises the buffer used for value reading.
	 * Subsequently {@link #characters(char[], int, int)} will write to the buffer
	 */
	private void initValueReading() {
		inValue = true;
		buffer = new StringBuffer();
	}
	
	/**
	 * Flushes the buffer returning the String accumulated.
	 * @return
	 */
	private String flushValue() {
		String readValue = buffer.toString();
		inValue = false;
		return readValue;
	}
	
	private String fixString(String str) {
		String[] toremove = {"<sub>","</sub>","<sup>","</sup>","<em>","</em>"};
		for (String remove:toremove) {
			str = str.replaceAll(remove, "");
		}
		
		str = str.replaceAll("&[Aa]lpha;", "alpha" );
		str = str.replaceAll("&[Bb]eta;" , "beta" );
		str = str.replaceAll("&[Gg]amma;", "gamma" );
		str = str.replaceAll("&[Dd]elta;", "delta" );
		
		return str;
	}
	
	public void startDocument() throws SAXException {
		db = new BlancoDb();
		inValue = false;
		inGroup = false;
		inSubgroup = false;
		inProtein = false;
		inMembers = false;
		buffer = null;
	}

	public void endDocument() throws SAXException {

	}

	public void startElement(String uri, String localName, String name,
			Attributes atts) throws SAXException {

		if (name.equals(MPSTRUC_TAG)) {
			String timestamp = atts.getValue(TIMESTAMP_ATTRIBUTE);

			DateFormat df = new SimpleDateFormat();
			this.db.setTimeStamp(df.parse(timestamp, new ParsePosition(3)));  
			if (db.getTimeStamp()==null) System.err.println("Warning! could not recognize timestamp format");

		}
		else if (name.equals(GROUP_TAG)) {
			this.inGroup = true;
		}
		else if (name.equals(SUBGROUP_TAG)) {
			this.inSubgroup = true;
		}
		else if (name.equals(NAME_TAG)) {
			initValueReading();
		}
		else if (name.equals(PROTEIN_TAG)) {
			this.inProtein = true;
			if (inProtein && !inMembers){ 
				currentCluster = new BlancoCluster();
				currentRepresentative = new BlancoEntry();
				currentCluster.addMember(currentRepresentative);
				currentCluster.setGroup(currentGroup);
				currentCluster.setSubgroup(currentSubgroup);
			}
				
			if (inProtein && inMembers)
				currentEntry = new BlancoEntry();
		}
		else if (name.equals(MEMBERPROTEINS_TAG)) {
			this.inMembers = true;
		}
		else if (name.equals(PDBCODE_TAG)) {
			initValueReading();
		}
		else if (name.equals(RESOLUTION_TAG)) {
			initValueReading();
		}
		
		
	}

	public void endElement(String uri, String localName, String name)
			throws SAXException {

		if (name.equals(GROUP_TAG)){
			this.inGroup = false;
		}
		else if (name.equals(SUBGROUP_TAG)) {
			this.inSubgroup = false;
		}
		else if (name.equals(NAME_TAG)) {
			if (inGroup && !inSubgroup && !inProtein && !inMembers) { 
				currentGroup = flushValue();
				currentGroup = fixString(currentGroup);
			}
				
			if (inGroup && inSubgroup && !inProtein && !inMembers) { 
				currentSubgroup = flushValue();
				currentSubgroup = fixString(currentSubgroup);
			}
			if (inGroup && inSubgroup && inProtein && !inMembers) {
				String clusterName = flushValue();
				clusterName = fixString(clusterName);
				currentCluster.setName(clusterName);
			}
		} 
		else if (name.equals(PROTEIN_TAG)) {
			this.inProtein = false;
			if (inMembers) 
				currentCluster.addMember(currentEntry);
			else 
				db.addCluster(currentCluster);
		}
		else if (name.equals(MEMBERPROTEINS_TAG)) {
			this.inMembers = false;
		}
		else if (name.equals(PDBCODE_TAG)) {
			if (inProtein && !inMembers) 
				currentRepresentative.setPdbCode(flushValue());
			if (inProtein && inMembers) {
				currentEntry.setPdbCode(flushValue());
			}
		}
		else if (name.equals(RESOLUTION_TAG)) {
			double resolution = -1;
			try {
				resolution = Double.parseDouble(flushValue());
			} catch (NumberFormatException e) {
				
			}

			if (inProtein && inMembers) {
				currentEntry.setResolution(resolution);
			}
			if (inProtein && !inMembers) {
				currentRepresentative.setResolution(resolution);
			}
		}
		
	}

	public void characters(char[] ch, int start, int length)
	throws SAXException {
		if (inValue) {
			buffer.append(ch, start, length);
		}
	}

	
	
	
	/*--------------------  empty methods --------------------------*/
	
	public void startPrefixMapping(String prefix, String uri)
	throws SAXException {
	}

	public void endPrefixMapping(String prefix) throws SAXException {
	}

	public void ignorableWhitespace(char[] ch, int start, int length)
			throws SAXException {
	}

	public void processingInstruction(String target, String data)
			throws SAXException {
	}

	public void setDocumentLocator(Locator locator) {
	}

	public void skippedEntity(String name) throws SAXException {
	}
	
	
	/*--------------------------  main  ---------------------------*/
	
	public static void main(String[] args) throws Exception {
		
		if (args.length<3) {
			System.err.println("Usage: BlancoDbXMLParser <input xml blanco db> <output tab file with resols/rfrees> <output tab file with clusters>");
			System.exit(1);
		}
		
		File inputXMLFile = new File(args[0]);
		File outputTabFile = new File(args[1]);
		File outputClustersFile = new File(args[2]);
		
		BlancoDbXMLParser parser = new BlancoDbXMLParser(inputXMLFile);
	
		File cifDir = new File("/home/duarte_j/cifrepo");

		// beware that there are many problems with the resolution field in the blanco db
		// that's why we take data from pdb
		
		BlancoDb db = parser.getBlancoDb();
		db.retrievePdbData(cifDir);
		db.removeEntriesWithNoPdbData();
		db.removeNonXrayEntries();
		
		//db.printTabular(System.out);
		
		// the file with all resols and rfree
		PrintStream ps = new PrintStream(outputTabFile);
		for (BlancoCluster cluster:db) {
			for (BlancoEntry entry:cluster) {
				ps.printf("%s\t%4.2f\t%4.2f\n",entry.getPdbCode(),entry.getResolution(),entry.getrFree());
			}
		}
		ps.close();
		
		// the file with clusters in tabular format (a cluster per line, like in blastclust)
		ps = new PrintStream(outputClustersFile);
		for (BlancoCluster cluster:db) {
			for (BlancoEntry entry:cluster) {
				ps.print(entry.getPdbCode()+" ");
			}
			ps.println();
		}
		ps.close();
		
		// stats output
		System.out.println();
		System.out.println();
		System.out.println("Number of clusters: "+db.getNumberClusters());
		System.out.println("Number of x-ray entries: "+db.size());
		System.out.println("Number of clusters with a member below:");
		
		double[] resols = {2.0,2.1,2.2,2.3,2.4,2.5,2.6,2.7,2.8,2.9,3.0,3.5};
		double rfree = 0.3;
		
		for (double resol:resols) {
			String resolstr = String.format("%4.2f",resol);
			System.out.println(resolstr+": "+
								db.getNumberClustersBelowResolution(resol)+" "+
								db.getNumberClustersBelowResolutionAndRfree(resol,rfree));
		}
		
		System.out.println("Representatives below 2.8");
		List<BlancoEntry>list = db.getBelowResolution(2.8);
		for (BlancoEntry entry:list) {
			System.out.printf("%s\t%4.2f\t%4.2f\n",entry.getPdbCode(),entry.getResolution(),entry.getrFree());
		}
		
		
		
		
	}
	

}
