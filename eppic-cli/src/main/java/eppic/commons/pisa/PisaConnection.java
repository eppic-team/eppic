package eppic.commons.pisa;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import org.xml.sax.SAXException;


/**
 * Connection class to get interface data from the PISA server.
 * 
 * See http://www.ebi.ac.uk/msd-srv/prot_int/pi_download.html
 *  
 * @author duarte_j
 *
 */
public class PisaConnection {

	public static final String PISA_INTERFACES_URL = "http://www.ebi.ac.uk/msd-srv/pisa/cgi-bin/interfaces.pisa?"; //pdbcodelist
	public static final String PISA_ASSEMBLIES_URL = "http://www.ebi.ac.uk/msd-srv/pisa/cgi-bin/multimers.pisa?";  //pdbcodelist
	public static final String PISA_PDB_ASSEMBLIES_URL = "http://www.ebi.ac.uk/msd-srv/pisa/cgi-bin/multimer.pdb?";//pdbcode:n,m
		
	private static final int MAX_ENTRIES_PER_REQUEST = 10; // pisa doesn't specify a limit but recommends 20-50 per request
														   // first we used 50 and seemed to work, lately (14Oct 2011) we had to change to 10 
														   // because the server would stall at downloading
	
	private String interfacesUrl;
	private String assembliesUrl;
	//private String pdbAssembliesUrl;
	
	public PisaConnection()	{
		this.interfacesUrl = PISA_INTERFACES_URL;
		this.assembliesUrl = PISA_ASSEMBLIES_URL;
		//this.pdbAssembliesUrl = PISA_PDB_ASSEMBLIES_URL;
	}
	
	/**
	 * Retrieves the XML PISA interface description from the PISA web server dividing the 
	 * query into chunks of {@value #MAX_ENTRIES_PER_REQUEST}, parses it and 
	 * returns the result as a map of pdb codes to lists of PISA interfaces 
	 * @param pdbCodesList pdb codes list for which we want to retrieve pisa interfaces
	 * @return
	 * @throws IOException
	 * @throws SAXException
	 */
	public Map<String,PisaInterfaceList> getInterfacesDescription(List<String> pdbCodesList) throws IOException, SAXException {
		Map<String,PisaInterfaceList> allPisaInterfaces = new HashMap<String,PisaInterfaceList>();
		// we do batches of MAX_ENTRIES_PER_REQUEST
		for (int i=0;i<pdbCodesList.size();i+=MAX_ENTRIES_PER_REQUEST) {
			String commaSepList = "";
			for (int c=i;c<i+MAX_ENTRIES_PER_REQUEST && c<pdbCodesList.size();c++) {
				if (c!=i) commaSepList+=",";
				commaSepList+=pdbCodesList.get(c);
			}
			allPisaInterfaces.putAll(getInterfacesDescription(commaSepList));
		}
		return allPisaInterfaces;
	}
	
	/**
	 * Retrieves the XML PISA interface description from the PISA web server, parses it and 
	 * returns the result as a map of pdb codes to lists of PISA interfaces 
	 * @param commaSepList
	 * @return
	 * @throws IOException
	 * @throws SAXException
	 */
	private Map<String,PisaInterfaceList> getInterfacesDescription(String commaSepList) throws IOException, SAXException {
		URL interfacesURL = new URL(interfacesUrl+commaSepList);
		URLConnection conn = interfacesURL.openConnection();
		
		PisaInterfaceXMLParser pxmlParser = new PisaInterfaceXMLParser(conn.getInputStream());
		Map<String,PisaInterfaceList> map = pxmlParser.getAllInterfaces();
		if (map.isEmpty()) throw new IOException("The PISA server returned no interface data for URL "+interfacesURL.toString());
		return map;
	}
	
	/**
	 * Retrieves the XML PISA interface description from the PISA web server dividing the 
	 * query into chunks of {@value #MAX_ENTRIES_PER_REQUEST} and saves them to disk.
	 * Output files are named as <<int>.interfaces.xml.gz>. 
	 * The corresponding pdb entries are written in file InterfacesNameToPdb.dat 
	 * @param pdbCodesList pdb codes list for which we want to retrieve pisa assemblies
	 * @return
	 * @throws IOException
	 */
	public void saveInterfacesDescription(List<String> pdbCodesList, File saveDir) throws IOException{
		//Check for directory
		if(!saveDir.isDirectory()){
			throw new IOException("No directory present: "+saveDir.getName());
		}
		
		//Create the NameToPdb file
		PrintWriter out = new PrintWriter(new File(saveDir,"InterfacesNameToPdb.dat"));

		// we do batches of MAX_ENTRIES_PER_REQUEST
		for (int i=0;i<pdbCodesList.size();i+=MAX_ENTRIES_PER_REQUEST) {
			String commaSepList = "";
			for (int c=i;c<i+MAX_ENTRIES_PER_REQUEST && c<pdbCodesList.size();c++) {
				if (c!=i) commaSepList+=",";
				commaSepList+=pdbCodesList.get(c);
			}
			File filePath = new File(saveDir,(i+1)+"-"+(i+MAX_ENTRIES_PER_REQUEST)+".interfaces.xml.gz");
			try{
				saveInterfacesDescription(commaSepList, filePath);
				out.printf("%18s\t%60s\n",(i+1)+"-"+(i+MAX_ENTRIES_PER_REQUEST),commaSepList);
			}catch(IOException e){
				System.err.println("Error while downloading interfaces xml file from PISA for pdb files:\n"+commaSepList);
				System.err.println(e.getMessage());
			}
		}
		
		out.close();

	}
	
	/**
	 * Retrieves the XML PISA interface description from the PISA web server and saves it to local disk in a zipped file.
	 * @param commaSepList
	 * @param saveDir
	 * @return
	 * @throws IOException
	 */
	private void saveInterfacesDescription(String commaSepList, File filePath) throws IOException{
		
		URL interfaceURL = new URL(interfacesUrl+commaSepList);
		
		InputStream is = interfaceURL.openStream();
		GZIPOutputStream zos = new GZIPOutputStream(new FileOutputStream(filePath));
		
		int isRead;
		while((isRead=is.read()) != -1){
			zos.write(isRead);
		}
		
		zos.close();
	}
	
	/**
	 * Retrieves the XML PISA assembly description from the PISA web server dividing the 
	 * query into chunks of {@value #MAX_ENTRIES_PER_REQUEST}, parses it and 
	 * returns the result as a map of pdb codes to lists of PISA assemblies 
	 * @param pdbCodesList pdb codes list for which we want to retrieve pisa assemblies
	 * @param pisaVersion
	 * @return
	 * @throws IOException
	 * @throws SAXException
	 */
	public Map<String,PisaAsmSetList> getAssembliesDescription(List<String> pdbCodesList, int pisaVersion) throws IOException, SAXException {
		Map<String,PisaAsmSetList> allPisaAsmSets = new HashMap<String,PisaAsmSetList>();
		// we do batches of MAX_ENTRIES_PER_REQUEST
		for (int i=0;i<pdbCodesList.size();i+=MAX_ENTRIES_PER_REQUEST) {
			String commaSepList = "";
			for (int c=i;c<i+MAX_ENTRIES_PER_REQUEST && c<pdbCodesList.size();c++) {
				if (c!=i) commaSepList+=",";
				commaSepList+=pdbCodesList.get(c);
			}
			allPisaAsmSets.putAll(getAssembliesDescription(commaSepList, pisaVersion));
		}
		return allPisaAsmSets;
	}
	
	/**
	 * Retrieves the XML PISA assembly description from the PISA web server, parses it and 
	 * returns the result as a map of pdb codes to lists of PISA assemblies 
	 * @param commaSepList
	 * @param pisaVersion
	 * @return
	 * @throws IOException
	 * @throws SAXException
	 */
	private Map<String,PisaAsmSetList> getAssembliesDescription(String commaSepList, int pisaVersion) throws IOException, SAXException {
		URL assembliesURL = new URL(assembliesUrl+commaSepList);
		URLConnection conn = assembliesURL.openConnection();
		
		PisaAssembliesXMLParser pxmlParser = new PisaAssembliesXMLParser(conn.getInputStream(), pisaVersion);
		Map<String,PisaAsmSetList> map = pxmlParser.getAllAssemblies();
		if (map.isEmpty()) throw new IOException("The PISA server returned no assembly data for URL "+assembliesURL.toString());
		return map;
	}
	
	/**
	 * Retrieves the XML PISA assembly description from the PISA web server dividing the 
	 * query into chunks of {@value #MAX_ENTRIES_PER_REQUEST} and saves them to disk.
	 * Output files are named as <<int>.assemblies.xml.gz>. 
	 * The corresponding pdb entries are written in file AssembliesNameToPdb.dat 
	 * @param pdbCodesList pdb codes list for which we want to retrieve pisa assemblies
	 * @return
	 * @throws IOException
	 */
	public void saveAssembliesDescription(List<String> pdbCodesList, File saveDir) throws IOException{
		//Check for directory
		if(!saveDir.isDirectory()){
			throw new IOException("No directory present: "+saveDir.getName());
		}
		
		//Create the NameToPdb file
		PrintWriter out = new PrintWriter(new File(saveDir,"AssembliesNameToPdb.dat"));

		// we do batches of MAX_ENTRIES_PER_REQUEST
		for (int i=0;i<pdbCodesList.size();i+=MAX_ENTRIES_PER_REQUEST) {
			String commaSepList = "";
			for (int c=i;c<i+MAX_ENTRIES_PER_REQUEST && c<pdbCodesList.size();c++) {
				if (c!=i) commaSepList+=",";
				commaSepList+=pdbCodesList.get(c);
			}
			File filePath = new File(saveDir,(i+1)+"-"+(i+MAX_ENTRIES_PER_REQUEST)+".assemblies.xml.gz");
			try{
				saveAssembliesDescription(commaSepList, filePath);
				out.printf("%18s\t%60s\n",(i+1)+"-"+(i+MAX_ENTRIES_PER_REQUEST),commaSepList);
			}catch(IOException e){
				System.err.println("Error while downloading assemblies xml file from PISA for pdb files:\n"+commaSepList);
				System.err.println(e.getMessage());
			}
		}
		
		out.close();

	}
	
	/**
	 * Retrieves the XML PISA assembly description from the PISA web server and saves it to local disk in a zipped file.
	 * @param commaSepList
	 * @param saveDir
	 * @return
	 * @throws IOException
	 */
	private void saveAssembliesDescription(String commaSepList, File filePath) throws IOException{
		
		URL assembliesURL = new URL(assembliesUrl+commaSepList);
		
		InputStream is = assembliesURL.openStream();
		GZIPOutputStream zos = new GZIPOutputStream(new FileOutputStream(filePath));
		
		int isRead;
		while((isRead=is.read()) != -1){
			zos.write(isRead);
		}
		
		zos.close();
	}
	

}
