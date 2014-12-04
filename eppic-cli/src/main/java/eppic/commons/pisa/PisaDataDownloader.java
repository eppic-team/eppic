package eppic.commons.pisa;

import gnu.getopt.Getopt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PisaDataDownloader {
	/**
	 * A class to download a the PISA data for a list of pdb entries
	 * 
	 * @author biyani_n
	 */
	
	private static final String DEFAULT_DOWNLOAD_LOCATION="/tmp/";
	
	private String downloadLoc;      //Path of a directory where the files are to be downloaded
	private PisaConnection pc;
	
	private List<String> pdbCodes; //List of pdb codes for which the pisa files are to be downloaded.
	
	public PisaDataDownloader() throws IOException{
		this(DEFAULT_DOWNLOAD_LOCATION);
	}

	public PisaDataDownloader(String downloadLoc) throws IOException{
		
		this.downloadLoc = downloadLoc;
		this.pc = new PisaConnection();
		this.pdbCodes = new ArrayList<String>();
		checkDownloadLoc();
	}
	
	private void checkDownloadLoc() throws IOException{
		File loc = new File(this.downloadLoc);
		if(!loc.isDirectory()) throw new IOException("No directory present in the location: "+this.downloadLoc);
	}
	
	public void downloadInterfaces() throws IOException{
		this.pc.saveInterfacesDescription(this.pdbCodes, new File(this.downloadLoc));
	}
	
	public void downloadAssemblies() throws IOException{
		this.pc.saveAssembliesDescription(this.pdbCodes, new File(this.downloadLoc));
	}
	
	public void setPdbListFromFile(File listFile) throws IOException{
		setPdbListFromFile(listFile, 1);
	}
	
	public void setPdbListFromFile(File listFile, int startLine) throws IOException{
		if(!listFile.isFile()){
			throw new FileNotFoundException("No list file found: "+listFile.getName());
		}
		BufferedReader br = new BufferedReader(new FileReader(listFile));
		String line;
		ArrayList<String> pdbCodes = new ArrayList<String>();
		int lineCount = 0;
		while ((line=br.readLine())!=null){
			lineCount++;
			if (line.startsWith("#"))  continue; 
			if (lineCount < startLine) continue;
			for(String code:line.split(" ")) pdbCodes.add(code);
		}
		
		br.close();
		
		this.pdbCodes = pdbCodes;
	}

	public static void main(String[] args) throws IOException{
		
		String help = 
				"Usage: PisaDataDownloader\n" +
				"Downloads interfaces and assemblies XML file from PISA server for a given list of pdb entries\n" +
				"  -f <file>  	: File containing a list of pdb codes \n" +
				"  -o <dir> 	: Directory specifying where the output files are to be saved \n" +
				" DOWNLOAD OPTIONS:\n"+
				" [-a]          : Download only assemblies files\n" +
				" [-i]          : Download only interfaces files\n" +
				" [-b <int>]    : Line number in the list file from where the reading of pdb codes is to be started\n";
		
		File listFile = null;
		String downloadDir = null;
		boolean downloadInterfaces = true;
		boolean downloadAssemblies = true;
		int startLine = 1;
		
		Getopt g = new Getopt("UploadToDB", args, "f:o:aib:h?");
		int c;
		while ((c = g.getopt()) != -1) {
			switch(c){
			case 'f':
				listFile = new File(g.getOptarg());
				break;
			case 'o':
				downloadDir = g.getOptarg();
				break;
			case 'a':
				downloadAssemblies = true;
				downloadInterfaces = false;
				break;
			case 'i':
				downloadInterfaces = true;
				downloadAssemblies = false;
				break;
			case 'b':
				try{
					startLine = Integer.parseInt(g.getOptarg());
					break;
				}catch(NumberFormatException e){
					System.err.println("Provide an integer value with option -b");
					System.exit(1);
				}
			case 'h':
				System.out.println(help);
				System.exit(0);
				break;
			case '?':
				System.err.println(help);
				System.exit(1);
				break; // getopt() already printed an error
			}
		}
		
		if(listFile == null || downloadDir == null){
			System.err.println("Some options not specified correctly:");
			System.err.println(help);
			System.exit(1);
		}
		
		PisaDataDownloader downloader = new PisaDataDownloader(downloadDir);	
				
		downloader.setPdbListFromFile(listFile, startLine);
		
		if(downloadInterfaces) downloader.downloadInterfaces();
		if(downloadAssemblies) downloader.downloadAssemblies();
	}

}
