package eppic.db.tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import ch.systemsx.sybit.crkwebui.shared.model.PdbInfo;
import eppic.model.PdbInfoDB;

/**
 * Class to dump all PDB ids present in db to individual xml files
 * 
 * 
 * @author Jose Duarte
 */
public class DumpToXml {

	private static DBHandler dbh;
	private static File dir;
	
	public static void main(String[] args) {
		
		if (args.length<2) {
			System.err.println("Usage: DumpToXml <database name> <output directory (will be written in divided layout)> [<limit to first n PDB ids>]"); 
			System.exit(1);
		}
		
		dbh = new DBHandler(args[0]);
		dir = new File(args[1]);
		
		int limit = -1;
		
		if (args.length==3) {
			limit = Integer.parseInt(args[2]);
		}
		
		List<String> pdbCodes = dbh.getAllPdbCodes();

		int size = pdbCodes.size();
		if (limit>0) size = limit;
		
		for (int i=0; i<size; i++) {
			
			try {
				dumpToXml(pdbCodes.get(i)); 
			} catch (IOException|JAXBException e) {
				System.err.println("Failed creating xml file for "+pdbCodes.get(i)+". Error: "+e.getMessage());
			}
		}
	}
	
	private static void dumpToXml(String pdbCode) throws IOException, JAXBException {
		
		String idx = pdbCode.substring(1,3);
		File subdir = new File(dir, idx);
		
		subdir.mkdir();
		
		File file = new File(subdir, pdbCode+".xml.gz");
		
		PdbInfoDB pdbInfoDb = dbh.deserializePdb(pdbCode);
		
		PdbInfo pdbInfo = PdbInfo.create(pdbInfoDb);
		
		try (PrintStream ps = new PrintStream(new GZIPOutputStream(new FileOutputStream(file)))) {

			serializePdbInfoList(pdbInfo, ps);		

			ps.close();
		}
	}
	
	private static void serializePdbInfoList(PdbInfo pdbInfo, PrintStream writer) throws JAXBException {
	    // create JAXB context and initializing Marshaller
	    JAXBContext jaxbContext = JAXBContext.newInstance(PdbInfo.class);
	    Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

	    // for getting nice formatted output
	    jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
	    jaxbMarshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);

	    // Writing it
	    jaxbMarshaller.marshal(pdbInfo, writer);
	    
	    
	}

}
