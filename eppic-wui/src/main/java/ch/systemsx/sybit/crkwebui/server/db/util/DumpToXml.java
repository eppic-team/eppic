package ch.systemsx.sybit.crkwebui.server.db.util;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import ch.systemsx.sybit.crkwebui.server.files.downloader.servlets.DataDownloadServlet;
import ch.systemsx.sybit.crkwebui.shared.model.PdbInfo;
import eppic.model.PdbInfoDB;

/**
 * Class to dump all PDB ids present in db to individual xml files
 * 
 * 
 * @author Jose Duarte
 */
public class DumpToXml {

	// TODO move the share.model package to eppic-model module and then we can move all this code to eppic-dbtools where it belongs
	
	private static DBHandler dbh;
	private static File dir;
	
	public static void main(String[] args) {
		
		dbh = new DBHandler(args[0]);
		dir = new File(args[1]);
		
		List<String> pdbCodes = dbh.getAllPdbCodes();

		
		for (String pdbCode : pdbCodes) {
			
			try {
				dumpToXml(pdbCode);
			} catch (IOException|JAXBException e) {
				System.err.println("Failed creating xml file for "+pdbCode+". Error: "+e.getMessage());
			}
		}
	}
	
	private static void dumpToXml(String pdbCode) throws IOException, JAXBException {
		
		String idx = pdbCode.substring(1,3);
		File subdir = new File(dir, idx);
		
		subdir.mkdir();
		
		File file = new File(subdir, pdbCode+".xml");
		
		PdbInfoDB pdbInfoDb = dbh.deserializePdb(pdbCode);
		
		PdbInfo pdbInfo = PdbInfo.create(pdbInfoDb);
		
		List<PdbInfo> list = new ArrayList<PdbInfo>(1);
		list.add(pdbInfo);
		
		// TODO gzip the file as we write it
		PrintWriter pw = new PrintWriter(file);
		
		//DataDownloadServlet.serializePdbInfoList(list, pw);
	}

}
