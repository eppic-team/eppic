package eppic.db.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import javax.persistence.EntityManager;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import eppic.model.dto.PdbInfo;
import eppic.model.db.PdbInfoDB;

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
			System.err.println("Write out all pdb ids present in database to xml.gz files to a dir in divided layout. If a list file is passed only those pdb ids are written."); 
			System.err.println("Usage: DumpToXml <database name> <output directory> [<list file with pdb ids>]"); 
			System.exit(1);
		}
		
		dbh = new DBHandler(args[0], null);
		dir = new File(args[1]);
		
		File listFile = null;
		
		if (args.length==3) {
			listFile = new File(args[2]);
		}
		
		
		List<String> pdbCodes = null;
		if (listFile==null) {
			pdbCodes = dbh.getAllPdbCodes();
		} else {
			try {
				pdbCodes = readListFile(listFile);
			} catch (IOException e) {
				System.err.println("Couldn't read list file " + listFile.toString() + ". Error: "+e.getMessage());
				System.exit(1);
			}
		}

		int size = pdbCodes.size();

		System.out.println("About to dump "+size+" PDBs to xml.gz files");
		
		for (int i=0; i<size; i++) {
			
			try {
				dumpToXml(pdbCodes.get(i)); 
			} catch (Exception e) {
				System.err.println("Failed creating xml file for "+pdbCodes.get(i)+". Error: "+e.getMessage());
			}
			if (i%1000 == 0) {
				System.out.println("Done "+i+" PDB ids");
			}
		}
	}
	
	private static void dumpToXml(String pdbCode) throws IOException, JAXBException {
		
		String idx = pdbCode.substring(1,3);
		File subdir = new File(dir, idx);
		
		subdir.mkdir();
		
		File file = new File(subdir, pdbCode+".xml.gz");
		
		EntityManager em = dbh.getEntityManager();
		PdbInfoDB pdbInfoDb = dbh.deserializePdb(em, pdbCode);
		
		PdbInfo pdbInfo = PdbInfo.create(pdbInfoDb);
		
		try (PrintStream ps = new PrintStream(new GZIPOutputStream(new FileOutputStream(file)))) {

			serializePdbInfo(pdbInfo, ps);		

			ps.close();
		}
		em.close();
	}
	
	private static void serializePdbInfo(PdbInfo pdbInfo, PrintStream writer) throws JAXBException {
	    // create JAXB context and initializing Marshaller
	    JAXBContext jaxbContext = JAXBContext.newInstance(PdbInfo.class);
	    Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

	    // for getting nice formatted output
	    jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
	    jaxbMarshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);

	    // Writing it
	    jaxbMarshaller.marshal(pdbInfo, writer);
	    
	    
	}
	
	private static List<String> readListFile(File file) throws IOException {
		List<String> list = new ArrayList<>();
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line;
		while ( (line=br.readLine())!=null ) {
			if (line.startsWith("#")) continue;
			if (line.trim().isEmpty()) continue;
			list.add(line.trim().toLowerCase());
			
		}
		br.close();
		return list;
	}

}
