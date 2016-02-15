/**
 * 
 */
package eppic.analysis.compare;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.StructureException;
import org.biojava.nbio.structure.StructureIO;
import org.biojava.nbio.structure.align.util.AtomCache;
import org.biojava.nbio.structure.contact.StructureInterfaceList;
import org.biojava.nbio.structure.xtal.CrystalBuilder;
import org.xml.sax.SAXException;

import eppic.DataModelAdaptor;
import eppic.EppicParams;
import eppic.commons.pisa.PisaAsmSetList;
import eppic.commons.pisa.PisaAssembliesXMLParser;
import eppic.commons.pisa.PisaInterfaceList;
import eppic.commons.pisa.PisaInterfaceXMLParser;
import eppic.commons.util.Goodies;
import eppic.model.PdbInfoDB;
import gnu.getopt.Getopt;



/**
 * Class to read in the data from .xml.gz PISA files for interfaces and assemblies and parse them.
 * Finally predict "xtal" or "bio" for each interface corresponding to eppic.
 * @author biyani_n
 *
 */
public class MatchPisaToEppic {

	private static final double MIN_AREA_PISA = 10;
	
	private static final String PROGRAM_NAME = "PredictPisaInterfaceCalls";
	
	private File serializedFilesDir;
	
	private List<PisaPdbData> pisaDatas;
	
	public MatchPisaToEppic(File interfaceFile, File assemblyFile, File serializedFilesDir, int pisaVersion) 
			throws SAXException, IOException {
		
		this.serializedFilesDir = serializedFilesDir;
		this.pisaDatas = createPisaDatafromFiles(assemblyFile, interfaceFile, pisaVersion);
	}
	

	public List<PisaPdbData> createPisaDatafromFiles(File assemblyFile, File interfaceFile, int pisaVersion) 
			throws SAXException, IOException {
		List<PisaPdbData> pisaDataList = new ArrayList<PisaPdbData>();
		
		//Parse Assemblies
		PisaAssembliesXMLParser assemblyParser = new PisaAssembliesXMLParser(new GZIPInputStream(new FileInputStream(assemblyFile)), pisaVersion);
		Map<String,PisaAsmSetList> assemblySetListMap = assemblyParser.getAllAssemblies();		
		
		//Parse Interfaces
		PisaInterfaceXMLParser interfaceParser = new PisaInterfaceXMLParser(new GZIPInputStream(new FileInputStream(interfaceFile)));
		Map<String, PisaInterfaceList> interfaceListMap = interfaceParser.getAllInterfaces();
		
		for(String pdbCode:assemblySetListMap.keySet()){
			if(!interfaceListMap.keySet().contains(pdbCode))
				System.err.println("Warning: Assembly file different from interface file; Interface file does not contain data for pdb:"+pdbCode);
			else{
				try{
					PdbInfoDB pdbInfo = null;
					if (serializedFilesDir!=null) {
						pdbInfo = getPdbInfoFromFile(pdbCode);
					} else {
						pdbInfo = getPdbInfo(getPdb(pdbCode));						
					}
					PisaPdbData local = new PisaPdbData(pdbInfo, assemblySetListMap.get(pdbCode), interfaceListMap.get(pdbCode), MIN_AREA_PISA);					
					pisaDataList.add(local);
				} catch(StructureException e){
					System.err.println("ERROR: Unable to load pdb file: "+pdbCode+", error: "+e.getMessage());
				} catch(IOException e) {
					System.err.println("ERROR: Unable to deserialize from file for pdb "+pdbCode+", error: "+e.getMessage());
				} catch (ClassNotFoundException e) {
					System.err.println("ERROR: Unable to deserialize from file for pdb "+pdbCode+", error: "+e.getMessage());
				} 
			}
		}
		
		return pisaDataList;
		
	}
	
	private Structure getPdb(String pdbCode) throws IOException, StructureException { 
		return StructureIO.getStructure(pdbCode);
	}
	
	private PdbInfoDB getPdbInfo(Structure pdb) {
		DataModelAdaptor dma = new DataModelAdaptor();
		
		CrystalBuilder cb = new CrystalBuilder(pdb);
		
		StructureInterfaceList eppicInterfaces = cb.getUniqueInterfaces(EppicParams.INTERFACE_DIST_CUTOFF);
		
		eppicInterfaces.calcAsas(EppicParams.DEF_NSPHEREPOINTS_ASA_CALC, 1, EppicParams.DEF_MIN_SIZE_COFACTOR_FOR_ASA);
		eppicInterfaces.removeInterfacesBelowArea(EppicParams.MIN_INTERFACE_AREA_TO_KEEP);
		 
		eppicInterfaces.getClusters(EppicParams.CLUSTERING_CONTACT_OVERLAP_SCORE_CUTOFF);
		dma.setInterfaces(eppicInterfaces);
		PdbInfoDB pdbInfo = dma.getPdbInfo();
		pdbInfo.setPdbCode(pdb.getPDBCode());
		return pdbInfo;
	}
	
	private PdbInfoDB getPdbInfoFromFile(String pdbCode) throws ClassNotFoundException, IOException {
		String midIndex = pdbCode.substring(1,3);
		File subdir = new File(serializedFilesDir,"divided"+File.separator+midIndex+File.separator+pdbCode);
		File webuidatFile = new File(subdir,pdbCode+".webui.dat");
		return (PdbInfoDB)Goodies.readFromFile(webuidatFile);
	}

	public void printData(PrintStream out, PrintStream err){
		for(PisaPdbData data:this.pisaDatas){
			data.printTabular(out, err);
		}
	}
	
	/**
	 * Main method to take input assembly and interfaces gzipped xml files and produce the output
	 * @param args
	 */
	public static void main(String[] args) {
		
		File assemFile = null;
		File interfFile = null;
		int pisaVersion = PisaAssembliesXMLParser.VERSION2;
		
		File listFile = null;
		File dir = null;
		File serializedFilesDir = null;
		
		String help = "Usage: \n" +
				PROGRAM_NAME+"\n" +
				"   -a         :  PISA assemblies gzipped xml file\n"+
				"   -i         :  PISA interfaces gzipped xml file\n" +
				"   -l         :  file with list of PDB codes, use in combination with -d (-a and -i"+
				"                 will be ignored)\n"+
				"   -d         :  dir containing PISA <pdb>.assemblies.xml.gz and <pdb>.interfaces.xml.gz\n"+
				"                 files. Use in combination with -l (-a and -i will be ignored)\n"+
				"  [-w]        :  dir containing webui.dat files for each PDB (in the usual divided layout).\n"+
				"                 If -w not specified, then interfaces will be calculated on the fly from mmCIF \n"+
				"                 files read from dir given in -c\n"+
				"  [-v]        :  PISA version, either "+PisaAssembliesXMLParser.VERSION1+
				" for web PISA or "+PisaAssembliesXMLParser.VERSION2+
				" for ccp4 package's command line PISA (default "+PisaAssembliesXMLParser.VERSION2+")\n";

		Getopt g = new Getopt(PROGRAM_NAME, args, "a:i:l:d:w:v:h?");
		int c;
		while ((c = g.getopt()) != -1) {
			switch(c){
			case 'a':
				assemFile = new File(g.getOptarg());
				break;
			case 'i':
				interfFile = new File(g.getOptarg());
				break;
			case 'l':
				listFile = new File(g.getOptarg());
				break;
			case 'd':
				dir = new File(g.getOptarg());
				break;
			case 'w':
				serializedFilesDir = new File(g.getOptarg());
				break;
			case 'v':
				pisaVersion = Integer.parseInt(g.getOptarg());
				break;
			case 'h':
			case '?':
				System.out.println(help);
				System.exit(0);
				break; // getopt() already printed an error
			}
		}
		
		if ((assemFile == null || interfFile == null) && (listFile == null || dir == null)){
			System.err.println("Must specify either: -a/-i (single files) or -l/-d (list file and dir)");
			System.err.println(help);
			System.exit(1);
		}
		
		AtomCache cache = new AtomCache();		
		cache.setUseMmCif(true);		
		StructureIO.setAtomCache(cache); 


		if (assemFile!=null) {
			// single files
			MatchPisaToEppic predictor;
			try {
				predictor = new MatchPisaToEppic(interfFile,assemFile,serializedFilesDir,pisaVersion);
				PisaPdbData.printHeaders(System.out); 
				predictor.printData(System.out, System.err);

			} catch (SAXException e) {
				System.err.println("Problem reading pisa files, error: "+e.getMessage());				
			} catch (IOException e) {
				System.err.println("Problem reading pisa files, error: "+e.getMessage());
			} 
			
		} else {
			PisaPdbData.printHeaders(System.out); 
			// list file and dir
			List<String> list = readListFile(listFile);
			for (String pdbCode:list) {
				
				interfFile = new File(dir,pdbCode+".interfaces.xml.gz");
				assemFile = new File(dir,pdbCode+".assemblies.xml.gz"); 

				try {

					MatchPisaToEppic predictor = 
						new MatchPisaToEppic(interfFile,assemFile,serializedFilesDir,pisaVersion);
					
					predictor.printData(System.out, System.err);
					
				} catch (IOException e) {
					System.err.println("Problem reading file for pdb "+pdbCode+", error: "+e.getMessage());
					continue;
				} catch (SAXException e) {
					System.err.println("Problem reading xml file for pdb "+pdbCode+", error: "+e.getMessage());
					continue;
				} 
			}
			
		}

	}
	
	private static List<String> readListFile(File listFile) {
		List<String> list = new ArrayList<String>();
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(listFile));

			String line;
			while((line = br.readLine())!=null) {
				if (line.startsWith("#")) continue;
				if (line.isEmpty()) continue;
				list.add(line);
			}
			br.close();
		} catch (IOException e) {
			System.err.println("Problem while reading list file "+listFile+", error: "+e.getMessage());
			System.exit(1);
		}
		return list;
	}

}
