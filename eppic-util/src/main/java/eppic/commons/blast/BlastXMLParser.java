package eppic.commons.blast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * Blast XML output parser using SAX
 * @author duarte
 *
 */
public class BlastXMLParser implements ContentHandler {

	// xml tags
	private static final String DB_TAG = "BlastOutput_db";
	private static final String QUERY_LENGTH_TAG = "BlastOutput_query-len";
	private static final String QUERY_DEF_TAG = "BlastOutput_query-def";
	private static final String ITERATION_ITER_NUM_TAG = "Iteration_iter-num";
	private static final String ITERATIONS_HITS_TAG = "Iteration_hits";
	private static final String HIT_TAG = "Hit";
	private static final String HIT_ID_TAG = "Hit_id";
	private static final String HIT_DEF_TAG = "Hit_def";
	private static final String HIT_LEN_TAG = "Hit_len";
	private static final String HSP_TAG = "Hsp";
	private static final String HSP_BIT_SCORE_TAG = "Hsp_bit-score";
	private static final String HSP_EVALUE_TAG = "Hsp_evalue";
	private static final String HSP_QUERY_FROM_TAG = "Hsp_query-from";
	private static final String HSP_QUERY_TO_TAG = "Hsp_query-to";
	private static final String HSP_HIT_FROM_TAG = "Hsp_hit-from";
	private static final String HSP_HIT_TO_TAG = "Hsp_hit-to";
	private static final String HSP_IDENTITY = "Hsp_identity";
	private static final String HSP_ALIGN_LEN = "Hsp_align-len";
	private static final String HSP_QSEQ = "Hsp_qseq";
	private static final String HSP_HSEQ = "Hsp_hseq";
	
	private static final String ID_REGEX = "^\\s*(\\S+).*";

	private InputSource input;
	
	private BlastHitList hitList;
	
	private String queryId;
	private BlastHit currentHit;
	private String currentSubjectId;
	private String currentSubjectDef;
	private int currentSubjectLength;
	
	
	private BlastHsp currentHsp;
	private String currentQSeq;
	private String currentHSeq;
	private int currentIdentity;
	private int currentAliLength;
	
	private StringBuffer buffer;
	private boolean inValue;
	
	private boolean inIterationHits;
	private boolean inHit;
	private boolean inHsp;
	
	/**
	 * Constructs new BlastXMLParser parsing the given Blast XML file.
	 * The input blastXMLFile can be either raw XML or gzipped-compressed XML,
	 * one or the other are detected automatically by searching string "<?xml" in
	 * beginning of file, if no such string found then it will be considered a
	 * gzipped xml file.
	 * Get the hits calling {@link #getHits()}
	 * @param blastXMLFile
	 * @param ignoreDTDUrl if true the DTD URL won't be read (blast output files always define a DOCTYPE line 
	 * pointing to http://www.ncbi.nlm.nih.gov/dtd/NCBI_BlastOutput.dtd), if false it will be read and the
	 * document validated with it
	 * @throws SAXException
	 * @throws IOException
	 */
	public BlastXMLParser(File blastXMLFile, boolean ignoreDTDUrl) throws SAXException, IOException{
		
		InputStream is = getInputStream(blastXMLFile);
		this.input = new InputSource(is); 
			
		XMLReader parser = XMLReaderFactory.createXMLReader();
	 		
		//BlastXMLHandler handler = new BlastXMLHandler();
		parser.setContentHandler(this);
		
		if (ignoreDTDUrl) {
			parser.setEntityResolver(new BlankingResolver());
		}
		
		parser.parse(input);

	}
	
	/**
	 * Given a file guesses by reading it first few bytes and matching "<?xml"
	 * whether it is a blast xml file or a gzipped blast xml file, 
	 * returning the appropriate InputStream
	 * @param file
	 * @return a buffered FileInputStream if file starts with "<?xml", or 
	 * a GZIPInputStream otherwise
	 * @throws IOException
	 */
	private InputStream getInputStream (File file) throws IOException {
		
		boolean isZipped = false;
		
		// first we guess if the file is uncompressed xml or compressed

		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
		int bytesToRead = 5;
		byte[] buffer = new byte[bytesToRead];
		bis.mark(bytesToRead);
		bis.read(buffer);		
		
		String xmlStr = "<?xml";
		char[] xmlStrCA = xmlStr.toCharArray();
		for (int i=0;i<bytesToRead;i++) {  
			if (buffer[i]!=xmlStrCA[i]) {
				isZipped = true;
				break;
			}
		}
		bis.reset();
		
		// and we return the appropriate InputStream
		if (isZipped) {
			return new GZIPInputStream(bis);
		} else {
			return bis;
		}
	}
	
	/**
	 * Returns the parsed BlastHitList
	 * @return
	 */
	public BlastHitList getHits() {
		return hitList;
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
	
	@Override
	public void startDocument() throws SAXException {
		hitList = new BlastHitList();
		inValue = false;
		inIterationHits = false;
		inHit = false;
		inHsp = false;
		buffer = null;
	}

	@Override
	public void endDocument() throws SAXException {

	}

	@Override
	public void startElement(String uri, String localName, String name,
			Attributes atts) throws SAXException {

		if (name.equals(DB_TAG)) {
			initValueReading();
		}
		else if (name.equals(QUERY_LENGTH_TAG)) {
			initValueReading();
		}
		else if (name.equals(QUERY_DEF_TAG)) {
			initValueReading();
		}
		else if (name.equals(ITERATION_ITER_NUM_TAG)) {
			initValueReading();
		}
		else if (name.equals(ITERATIONS_HITS_TAG)) {
			inIterationHits = true;
		}
		else if (name.equals(HIT_TAG)) {
			inHit = true;
			this.currentHit = new BlastHit();
		}
		if (inIterationHits && inHit) {
			if (name.equals(HIT_ID_TAG)){
				initValueReading();
			} 
			else if (name.equals(HIT_DEF_TAG)){
				initValueReading();
			}
			else if (name.equals(HIT_LEN_TAG)) {
				initValueReading();
			}
			else if (name.equals(HSP_TAG)) {
				inHsp = true;
				this.currentHsp = new BlastHsp(currentHit);
				this.currentHit.setQueryId(queryId);
				this.currentHit.setQueryLength(hitList.getQueryLength());
				this.currentHit.setSubjectId(currentSubjectId);
				this.currentHit.setSubjectDef(currentSubjectDef);
				this.currentHit.setSubjectLength(currentSubjectLength);
				this.currentHit.addHsp(currentHsp);
			}
			if (inHsp) {
				if (name.equals(HSP_BIT_SCORE_TAG)) {
					initValueReading();
				}
				else if (name.equals(HSP_EVALUE_TAG)) {
					initValueReading();
				}
				else if (name.equals(HSP_QUERY_FROM_TAG)) {
					initValueReading();
				}
				else if (name.equals(HSP_QUERY_TO_TAG)) {
					initValueReading();
				}
				else if (name.equals(HSP_HIT_FROM_TAG)) {
					initValueReading();
				}
				else if (name.equals(HSP_HIT_TO_TAG)) {
					initValueReading();
				}
				else if (name.equals(HSP_IDENTITY)) {
					initValueReading();
				}
				else if (name.equals(HSP_ALIGN_LEN)) {
					initValueReading();
				}
				else if (name.equals(HSP_QSEQ)) {
					initValueReading();
				}
				else if (name.equals(HSP_HSEQ)) {
					initValueReading();
				}
			}
		}
		
		
	}

	@Override
	public void endElement(String uri, String localName, String name)
			throws SAXException {

		if (name.equals(DB_TAG)){
			this.hitList.setDb(flushValue());
		}
		if (name.equals(QUERY_LENGTH_TAG)){
			this.hitList.setQueryLength(Integer.parseInt(flushValue()));
		}
		else if (name.equals(QUERY_DEF_TAG)) {
			String val = flushValue();
			Pattern p = Pattern.compile(ID_REGEX);
			Matcher m = p.matcher(val);
			if (m.matches()) {
				this.queryId = m.group(1);
				this.hitList.setQueryId(queryId);
			}
		}
		else if (name.equals(ITERATION_ITER_NUM_TAG)) {
			if (Integer.parseInt(flushValue())>1) {
				System.err.println("WARNING: this BLAST XML file contains more than one iteration. Multiple iterations parsing not supported yet!");
			}
		}
		else if (name.equals(ITERATIONS_HITS_TAG)) {
			inIterationHits = false;
		}
		else if (name.equals(HIT_TAG)) {
			this.hitList.add(currentHit);
			inHit = false;
		}
		if (inIterationHits && inHit) {
			if (name.equals(HIT_ID_TAG)) {
				String val = flushValue();
				Pattern p = Pattern.compile(ID_REGEX);
				Matcher m = p.matcher(val);
				if (m.matches()) {
					this.currentSubjectId = m.group(1);
				}
			}
			else if (name.equals(HIT_DEF_TAG)) {
				this.currentSubjectDef = flushValue();
			}
			else if (name.equals(HIT_LEN_TAG)){				
				this.currentSubjectLength = Integer.parseInt(flushValue());
			}
			else if (name.equals(HSP_TAG)) {
				this.currentHsp.setAlignment(currentQSeq, currentHSeq);
				this.currentHsp.setIdentities(currentIdentity);
				inHsp = false;
			}
			if (inHsp) {
				if (name.equals(HSP_BIT_SCORE_TAG)) {
					this.currentHsp.setScore(Double.parseDouble(flushValue()));
				}
				else if (name.equals(HSP_EVALUE_TAG)) {
					this.currentHsp.setEValue(Double.parseDouble(flushValue()));
				}
				else if (name.equals(HSP_QUERY_FROM_TAG)) {
					this.currentHsp.setQueryStart(Integer.parseInt(flushValue()));
				}
				else if (name.equals(HSP_QUERY_TO_TAG)) {
					this.currentHsp.setQueryEnd(Integer.parseInt(flushValue()));
				}
				else if (name.equals(HSP_HIT_FROM_TAG)) {
					this.currentHsp.setSubjectStart(Integer.parseInt(flushValue()));
				}
				else if (name.equals(HSP_HIT_TO_TAG)) {
					this.currentHsp.setSubjectEnd(Integer.parseInt(flushValue()));
				}
				else if (name.equals(HSP_IDENTITY)) {
					this.currentIdentity = Integer.parseInt(flushValue());
				}
				else if (name.equals(HSP_ALIGN_LEN)) {
					this.currentAliLength = Integer.parseInt(flushValue());
					this.currentHsp.setAliLength(currentAliLength);
				}
				else if (name.equals(HSP_QSEQ)) {
					this.currentQSeq = flushValue();
				}
				else if (name.equals(HSP_HSEQ)) {
					this.currentHSeq = flushValue();
				}
			}
		}
		
	}

	@Override
	public void characters(char[] ch, int start, int length)
	throws SAXException {
		if (inValue) {
			buffer.append(ch, start, length);
		}
	}

	
	
	
	/*--------------------  empty methods --------------------------*/
	
	@Override
	public void startPrefixMapping(String prefix, String uri)
	throws SAXException {
	}

	@Override
	public void endPrefixMapping(String prefix) throws SAXException {
	}

	@Override
	public void ignorableWhitespace(char[] ch, int start, int length)
			throws SAXException {
	}

	@Override
	public void processingInstruction(String target, String data)
			throws SAXException {
	}

	@Override
	public void setDocumentLocator(Locator locator) {
	}

	@Override
	public void skippedEntity(String name) throws SAXException {
	}
	
	
	/*--------------------------  main ----------------------------*/
	
	/**
	 * to test the class. See class BlastParsersTest for a complete junit4 test of this class
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		//File xmlfile = new File("/scratch/local/temp/blast.out.xml");
		File xmlfile = new File(args[0]);
		
		BlastXMLParser bxp = new BlastXMLParser(xmlfile, false);
		
		BlastHitList hitListXML = bxp.getHits();
		System.out.println("query length= "+hitListXML.getQueryLength());
		
		hitListXML.printSome(20);
		System.out.println("Total number of hits: "+hitListXML.size());

		// printing alignments
		for (BlastHit hit: hitListXML) {
			for (BlastHsp hsp:hit) {
				hsp.getAlignment().printFasta();
				System.out.println();
			}
		}
		
		//hitListXML.printTabular();
		
		
	}
}
