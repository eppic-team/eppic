/**
 * 
 */
package analysis.pisa;

import gnu.getopt.Getopt;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.xml.sax.SAXException;

import owl.core.connections.pisa.PisaAsmSetList;
import owl.core.connections.pisa.PisaAssembliesXMLParser;
import owl.core.connections.pisa.PisaInterfaceList;
import owl.core.connections.pisa.PisaInterfaceXMLParser;
import owl.core.structure.PdbAsymUnit;
import owl.core.structure.PdbLoadException;
import owl.core.util.FileFormatException;


/**
 * Class to read in the data from .xml.gz PISA files for interfaces and assemblies and parse them.
 * Finally predict "xtal" or "bio" for each interface corresponding to eppic.
 * @author biyani_n
 *
 */
public class PredictPisaInterfaceCalls {
	
	private static final String PROGRAM_NAME = "PredictPisaInterfaceCalls";
	private static final String DEFAULT_CIF_DIR = "/nfs/data/dbs/pdb/data/structures/all/mmCIF";
	
	private String cifDir;
	
	private File interfaceFile;
	private File assemblyFile;
	
	private List<PisaPdbData> pisaDatas;
	
	public PredictPisaInterfaceCalls(File interfaceFile, File assemblyFile, String cifDirPath) throws FileNotFoundException, SAXException, IOException, FileFormatException, PdbLoadException{
		this.interfaceFile = interfaceFile;
		this.assemblyFile = assemblyFile;
		this.cifDir = cifDirPath;
		this.pisaDatas = createPisaDatafromFiles(assemblyFile, interfaceFile);
	}
	
	public PredictPisaInterfaceCalls(File interfaceFile, File assemblyFile) throws FileNotFoundException, SAXException, IOException, FileFormatException, PdbLoadException{
		this(interfaceFile, assemblyFile, DEFAULT_CIF_DIR);
	}
	
	public String getCifDir() {
		return cifDir;
	}

	public void setCifDir(String cifDir) {
		this.cifDir = cifDir;
	}

	public File getInterfaceFile() {
		return interfaceFile;
	}

	public void setInterfaceFile(File interfaceFile) {
		this.interfaceFile = interfaceFile;
	}

	public File getAssemblyFile() {
		return assemblyFile;
	}

	public void setAssemblyFile(File assemblyFile) {
		this.assemblyFile = assemblyFile;
	}

	public List<PisaPdbData> getPisaDatas() {
		return pisaDatas;
	}

	public void setPisaDatas(List<PisaPdbData> pisaDatas) {
		this.pisaDatas = pisaDatas;
	}

	public List<PisaPdbData> createPisaDatafromFiles(File assemblyFile, File interfaceFile) throws FileNotFoundException, SAXException, IOException, PdbLoadException, FileFormatException{
		List<PisaPdbData> pisaDataList = new ArrayList<PisaPdbData>();
		
		//Parse Assemblies
		PisaAssembliesXMLParser assemblyParser = new PisaAssembliesXMLParser(new GZIPInputStream(new FileInputStream(assemblyFile)));
		Map<String,PisaAsmSetList> assemblySetListMap = assemblyParser.getAllAssemblies();		
		
		//Parse Interfaces
		PisaInterfaceXMLParser interfaceParser = new PisaInterfaceXMLParser(new GZIPInputStream(new FileInputStream(interfaceFile)));
		Map<String, PisaInterfaceList> interfaceListMap = interfaceParser.getAllInterfaces();
		
		for(String pdbCode:assemblySetListMap.keySet()){
			if(!interfaceListMap.keySet().contains(pdbCode))
				System.err.println("Warning: Assembly file different from interface file; Interface file does not contain data for pdb:"+pdbCode);
			else{
				try{
					File cifFile = File.createTempFile(pdbCode, "cif");
					PdbAsymUnit.grabCifFile(this.cifDir, null, pdbCode, cifFile, false);
					PisaPdbData local = new PisaPdbData(new PdbAsymUnit(cifFile),assemblySetListMap.get(pdbCode), interfaceListMap.get(pdbCode));
					cifFile.deleteOnExit();
					pisaDataList.add(local);
				}catch(PdbLoadException e){
					System.err.println("ERROR: Unable to load pdb file: "+pdbCode+"; "+e.getMessage());
				}catch(FileNotFoundException e){
					System.err.println("ERROR: Unable to find the file "+e.getMessage());
				}
			}
		}
		
		return pisaDataList;
		
	}
	
	public void printData(PrintStream out){
		//Print Header
		out.print("#Contains PDB's: ");
		for(PisaPdbData data:this.pisaDatas) out.print(data.getPdbCode()+" ");
		out.print("\n");
		if(!this.pisaDatas.isEmpty()) out.printf("#%4s %8s %8s %8s\n","PDB","EPPIC_ID","PISA_ID","PisaCall");
		for(PisaPdbData data:this.pisaDatas){
			for(int eppicI:data.getEppicToPisaInterfaceMap().keySet()){
				out.printf("%5s %8s %8s %8s\n",data.getPdbCode(),eppicI,data.getPisaIdForEppicInterface(eppicI),data.getPisaCallFromEppicInterface(eppicI).getName() );
			}
		}
	}

	/**
	 * Test method to take input assembly and interfaces gzipped xml files and produce the output
	 * @param args
	 * @throws PdbLoadException 
	 * @throws FileFormatException 
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException, SAXException, IOException, FileFormatException, PdbLoadException {
		
		File assemFile = null;
		File interfFile = null;
		String cifPath = DEFAULT_CIF_DIR;

		String help = "Usage: \n" +
				PROGRAM_NAME+"\n" +
				"   -a         :  Path to the assemblies gzipped xml file\n"+
				"   -i         :  Path to the interfaces gzipped xml file\n" +
				" [-c]         :  Path to cif files directory\n";

		Getopt g = new Getopt(PROGRAM_NAME, args, "a:i:c:h?");
		int c;
		while ((c = g.getopt()) != -1) {
			switch(c){
			case 'a':
				assemFile = new File(g.getOptarg());
				break;
			case 'i':
				interfFile = new File(g.getOptarg());
				break;
			case 'c':
				cifPath = g.getOptarg();
				break;
			case 'h':
			case '?':
				System.out.println(help);
				System.exit(0);
				break; // getopt() already printed an error
			}
		}
		
		if(assemFile==null || interfFile==null){
			System.err.println("Must specify both assembly and interface files");
			System.err.println(help);
			System.exit(1);
		}
		
		if(!assemFile.isFile() || !interfFile.isFile()){
			System.err.println("Either of assembly or interface file not present");
			System.err.println(help);
			System.exit(1);
		}
		

		PredictPisaInterfaceCalls predictor = new PredictPisaInterfaceCalls(interfFile,assemFile,cifPath);
		predictor.printData(System.out);
		

	}

}
