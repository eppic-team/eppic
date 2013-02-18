/**
 * 
 */
package tools;

import gnu.getopt.Getopt;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import owl.core.connections.NoMatchFoundException;
import owl.core.connections.SiftsConnection;
import owl.core.connections.UniprotLocalConnection;
import owl.core.sequence.UnirefEntry;
import owl.core.util.Interval;

/**
 * @author biyani_n
 *
 */
public class WriteUniqueUniprots {
	
	public SiftsConnection sc;
	public UniprotLocalConnection uniLC;
	
	public WriteUniqueUniprots(String scPath, String uniprotdb) throws IOException, SQLException{
			sc = new SiftsConnection(scPath);
			uniLC = new UniprotLocalConnection(uniprotdb, null);
	}
	
	/**
	 * Gets argument from command line which gives a path of where the fasta files are to be written
	 * 
	 * @param args
	 * @throws IOException 
	 * @throws NoMatchFoundException 
	 * @throws SQLException 
	 */
	public static void main(String[] args) throws IOException, SQLException, NoMatchFoundException {
		
		String help = 
				"Usage: WriteUniqueUnirots\n" +
				"Creates fasta (.fa) files of unique mapings of PDB to Uniprot\n" +
				" -s <dir>  	: Sifts Connection file path \n" +
				" -u <dbName>    : Uniprot Database Name \n" +
				" [-o <dir>] 	: Directory where output files are to be written\n";
		
		Getopt g = new Getopt("UploadToDB", args, "s:u:o:h?");
		
		String scFilePath = null;
		String uniprotdbName = null;
		String outdirPath = "";
		boolean ifOutdir = false;
		
		int c;
		while ((c = g.getopt()) != -1) {
			switch(c){
			case 's':
				scFilePath = g.getOptarg();
				break;
			case 'u':
				uniprotdbName = g.getOptarg();
				break;
			case 'o':
				ifOutdir = true;
				outdirPath = g.getOptarg();
				break;
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
		
		//Check if the file for sifts connection exits!
		if( scFilePath == null || !(new File(scFilePath)).isFile()) {
			System.err.println("Hey Jim, Can you check if the SIFTS Connection file really exists! " +
					"I am having trouble in reading it.");
			System.err.println(help);
			System.exit(1);
		}
		
		//Check if the uniprotdbName is provided by the user
		if( uniprotdbName == null){
			System.err.println("Hey Jim, Could you please provide me with a unipotDB Name with option -u");
			System.err.println(help);
			System.exit(1);
		}
		
		//Check if outdir if provided really exists
		if(ifOutdir && !(new File(outdirPath)).isDirectory() ){
			System.err.println("Hey Jim, Can you check the Output Directory path: " + outdirPath +
					" I am having trouble in recognizing it!!");
			System.err.println(help);
			System.exit(1);
		}
		
		
		//Initialize the variable
		try{
			WriteUniqueUniprots wuni = new WriteUniqueUniprots(scFilePath, uniprotdbName);
			HashMap<String, ArrayList<Interval>> uniqueMap = wuni.sc.getUniqueMappings();
			PrintWriter outList = new PrintWriter(new File (outdirPath + "queries.list"));
		
			for(String uniprotid:uniqueMap.keySet()) {
				try {
					UnirefEntry uniEntry = wuni.uniLC.getUnirefEntry(uniprotid);
					String uniSeq = uniEntry.getSequence();
					for(Interval interv:uniqueMap.get(uniprotid)){
						//Create fasta files
						String fileName = outdirPath + uniprotid + "." + interv.beg + "-" + interv.end + ".fa";
						int maxLen = 60;
				
						if(uniSeq.length() >= interv.end){
							File outFile = new File (fileName); 
							PrintWriter out = new PrintWriter(outFile);
							out.println(">"+uniprotid+"_"+interv.beg+"-"+interv.end);
							String uniSubSeq = uniSeq.substring(interv.beg-1, interv.end);
							for(int i=0; i<uniSubSeq.length(); i+=maxLen) {
								out.println(uniSubSeq.substring(i, Math.min(i+maxLen, uniSubSeq.length())));
							}
							outList.println(uniprotid+"."+interv.beg+"-"+interv.end);
							out.close();
						}
						else{
							System.err.println("Warning: Length of query seq longer than uniprot seq for "+uniprotid+"_"+interv.beg+"-"+interv.end);
						}
					
					}
				}
				catch(NoMatchFoundException er){
					System.err.println("Warning: FileNotFoundException: " + er.getMessage());
				}
			}
			outList.close();
		}
		catch(Exception ex){
			System.err.println("Error in inputs: " + ex.getClass() + " " + ex.getMessage());
			System.exit(1);
		}
	}
}
