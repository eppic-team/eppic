package eppic.commons.pisa;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;



/**
 * PISA XML output parser using SAX
 * @author duarte
 *
 */
public class PisaAssembliesXMLParser implements ContentHandler {

	// xml tags
	private static final String PDB_ENTRY_TAG_V1 = "pdb_entry";
	private static final String PDB_ENTRY_TAG_V2 = "pisa_results";
	private static final String ASM_SET_TAG = "asm_set";
	private static final String ASSEMBLY_TAG = "assembly";
	private static final String INTERFACES_TAG = "interfaces";

	private static final String PDB_CODE_TAG_V1 = "pdb_code";
	private static final String PDB_CODE_TAG_V2 = "name";
	private static final String STATUS_TAG = "status";
	
	private static final String ID_TAG = "id";
	private static final String MMSIZE_TAG = "mmsize";
	private static final String SCORE_TAG = "score";
	private static final String	DISS_ENERGY_TAG = "diss_energy";
	private static final String	FORMULA_TAG = "formula";
	private static final String	COMPOSITION_TAG = "composition";

	private static final String NOCC_TAG = "nocc";
	
	/**
	 * PISA version in EBI web server
	 */
	public static final int VERSION1 = 1;
	
	/**
	 * PISA version in ccp4 command line program
	 */
	public static final int VERSION2 = 2;
		
	// members
	private InputSource input;
	
	private Map<String,PisaAsmSetList> allAssemblies;
	
	private PisaAsmSetList currentAsmSetList;
	private String currentPdbCode;
	private PisaAsmSet currentAsmSet;
	private PisaAssembly currentPisaAssembly;
	
	private StringBuffer buffer;
	private boolean inValue;
	
	private boolean inEntry;
	private boolean inAsmSet;
	private boolean inAssembly;
	private boolean inInterfaces;
	
	private int currentInterfaceId;

	private int pisaVersion; // either 1 or 2
	
	private String pdbEntryTag;
	private String pdbCodeTag;
	
	/**
	 * Constructs new PisaAssembliesXMLParser and parses the given XML stream
	 * Get the list calling {@link #getAllAssemblies()}
	 * Two XML formats are supported depending whether the version is {@link #VERSION1} for XML
	 * files downloaded from the PISA server or {@link #VERSION2} for XML files obtained from the
	 * CCP4 command-line PISA.
	 * @param is the stream with XML data for assemblies description
	 * @param pisaVersion the PISA version: either {@link #VERSION1} or {@link #VERSION2}
	 */
	public PisaAssembliesXMLParser(InputStream is, int pisaVersion) throws SAXException, IOException {
		this.input = new InputSource(is);
		this.pisaVersion = pisaVersion;
		
		if (pisaVersion==VERSION1) {
			pdbEntryTag = PDB_ENTRY_TAG_V1;
			pdbCodeTag = PDB_CODE_TAG_V1;
		} else if (pisaVersion==VERSION2) {
			pdbEntryTag = PDB_ENTRY_TAG_V2;
			pdbCodeTag = PDB_CODE_TAG_V2;			
		} else {
			throw new IllegalArgumentException("PISA versions supported are either "+VERSION1+" or "+VERSION2);
		}
		
		XMLReader parser = XMLReaderFactory.createXMLReader();
 		
		parser.setContentHandler(this);
		
		parser.parse(input);
	}
	
	/**
	 * Returns the parsed assemblies data
	 * @return a map of pdb codes to lists of assemblies
	 */
	public Map<String,PisaAsmSetList> getAllAssemblies() {
		return allAssemblies;
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
		allAssemblies = new HashMap<String,PisaAsmSetList>();
		inEntry = false;
		inValue = false;
		inAsmSet = false;
		inAssembly = false;
		inInterfaces = false;
		buffer = null;
	}

	@Override
	public void endDocument() throws SAXException {

	}

	@Override
	public void startElement(String uri, String localName, String name,
			Attributes atts) throws SAXException {
		if (name.equals(pdbEntryTag)){
			inEntry = true;
			currentAsmSetList = new PisaAsmSetList();
		}
		if (inEntry) {
			if (name.equals(pdbCodeTag)) {
				initValueReading();
			} else if (name.equals(STATUS_TAG)) {
				initValueReading();
			} else if (name.equals(ASM_SET_TAG)) {
				inAsmSet = true;
				currentAsmSet = new PisaAsmSet();
			}
			if (inAsmSet && !inAssembly) {
				if (name.equals(ASSEMBLY_TAG)) {
					inAssembly = true;
					currentPisaAssembly = new PisaAssembly();
				} 
			}
			
			if (inAsmSet && inAssembly && !inInterfaces) {
				if (name.equals(ID_TAG)) {
					initValueReading();
				} else if (name.equals(MMSIZE_TAG)){
					initValueReading();
				} else if (name.equals(SCORE_TAG)) {
					initValueReading();
				} else if (name.equals(DISS_ENERGY_TAG)) {
					initValueReading();
				} else if (name.equals(FORMULA_TAG)) {
					initValueReading();
				} else if (name.equals(COMPOSITION_TAG)) {
					initValueReading();
				} else if (name.equals(INTERFACES_TAG)) {
					inInterfaces = true;
				}
			}
			if (inAsmSet && inAssembly && inInterfaces) {
				if (name.equals(ID_TAG)) {
					initValueReading();
				} else if (name.equals(NOCC_TAG)) {
					initValueReading();
				}
			}


		} 
	}

	@Override
	public void endElement(String uri, String localName, String name)
			throws SAXException {

		if (name.equals(pdbEntryTag)) {
			inEntry = false;
			allAssemblies.put(currentPdbCode, currentAsmSetList);
		}
		if (inEntry) {
			if (name.equals(pdbCodeTag)) {
				currentPdbCode = flushValue().toLowerCase();
				currentAsmSetList.setPdbCode(currentPdbCode);
			} else if (name.equals(STATUS_TAG)) {
				currentAsmSetList.setStatus(flushValue());
			} else if (name.equals(ASM_SET_TAG)){
				inAsmSet = false;
				currentAsmSetList.add(currentAsmSet);
			}
			if (inAsmSet && inAssembly && !inInterfaces) {
				if (name.equals(ID_TAG)) {
					currentPisaAssembly.setId(Integer.parseInt(flushValue()));
				} else if (name.equals(MMSIZE_TAG)){
					currentPisaAssembly.setMmsize(Integer.parseInt(flushValue()));
				} else if (name.equals(SCORE_TAG)) {
					currentPisaAssembly.setScore(flushValue());
				} else if (name.equals(DISS_ENERGY_TAG)) {
					currentPisaAssembly.setDissEnergy(Double.parseDouble(flushValue()));
				} else if (name.equals(FORMULA_TAG)) {
					currentPisaAssembly.setFormula(flushValue());
				} else if (name.equals(COMPOSITION_TAG)) {
					currentPisaAssembly.setComposition(flushValue());
				} else if (name.equals(ASSEMBLY_TAG)) {
					inAssembly = false;
					currentAsmSet.add(currentPisaAssembly);
				}	
			}
			if (inAsmSet && inAssembly && inInterfaces) {
				if (name.equals(ID_TAG)) {
					currentInterfaceId = Integer.parseInt(flushValue());
					if (pisaVersion==VERSION2) {
						// version 1 lists all interfaces and assigns nocc>0 to all those composing this assembly
						// version 2 lists only the interfaces composing this assembly (nocc field is missing)
						currentPisaAssembly.addInterfaceId(currentInterfaceId);
					}
				} else if (name.equals(NOCC_TAG)) {
					int nocc = Integer.parseInt(flushValue());
					if (nocc>0) {
						currentPisaAssembly.addInterfaceId(currentInterfaceId);
					}
				} else if (name.equals(INTERFACES_TAG)) {
					inInterfaces = false;
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
	
	
}
