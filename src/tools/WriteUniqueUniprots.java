/**
 * 
 */
package tools;

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
	 * args[0] : SiftsConnectionFile
	 * args[1] : Path to print files
	 * @param args
	 * @throws IOException 
	 * @throws NoMatchFoundException 
	 * @throws SQLException 
	 */
	public static void main(String[] args) throws IOException, SQLException, NoMatchFoundException {
		WriteUniqueUniprots wuni = new WriteUniqueUniprots(args[0], args[2]);
		HashMap<String, ArrayList<Interval>> uniqueMap = wuni.sc.getUniqueMappings();
		PrintWriter outList = new PrintWriter(new File (args[1]+"queries.list"));
		
		for(String uniprotid:uniqueMap.keySet()) {
			try {
				UnirefEntry uniEntry = wuni.uniLC.getUnirefEntry(uniprotid);
				String uniSeq = uniEntry.getSequence();
				for(Interval interv:uniqueMap.get(uniprotid)){
					String fileName = args[1]+uniprotid+"."+interv.beg+"-"+interv.end+".fa";
					int maxLen = 60;
				
					if(uniSeq.length()>=interv.end){
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

}
