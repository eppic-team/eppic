package martin;

import gnu.getopt.Getopt;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import owl.core.connections.JPredConnection;
import owl.core.sequence.Sequence;
import owl.core.structure.features.SecondaryStructure;
import owl.core.util.Goodies;

import crk.ChainEvolContext;
import crk.ChainEvolContextList;
import crk.ScoringType;




public class boundaryparser {

	/**
	 * @param args
	 */
	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		String help = 
				"Usage: \n" +
				"martin.boundaryparser \n" +
				" -c <File>      : PDB.chainevolcontext.dat\n" +
				" -u <string>    : UniProt code\n" +
				" -o <string>    : type of organism (gram-|gram+|euk)\n" +
				" [-p <string>]  : Psipred file\n" +
				" [-s <string>]  : full path to SignalP executable\n" +
				" [-h]           : print usage and exit\n\n";
		
		    
        File chainevolc = null;
        String uniprotCode = null;
        String psipfile = null;
        String organism = null;
        File signalpexe = new File("/afs/psi.ch/project/bioinfo/software/signalp-3.0/signalp");

		Getopt g = new Getopt("boundaryparser", args, "c:u:p:s:o:h?");
		int j;
		while ((j = g.getopt()) != -1) {
			switch(j){
			case 'c':
				chainevolc = new File(g.getOptarg());
				break;
			case 'u':
				uniprotCode = g.getOptarg();
				break;
			case 'p':
				psipfile = g.getOptarg();
				break;
			case 's':
				signalpexe = new File(g.getOptarg());
				break;		
			case 'o':
				organism = g.getOptarg();
				break;
			case 'h':
				System.out.println(help);
				System.exit(0);
				break;
			case '?':
				System.err.println(help);
				System.exit(1);
				break;
			}
		}       
        
		if (chainevolc == null){
			System.err.println("Missing input PDB.chainevolcontext.dat (-c)");
			System.err.println(help);
			System.exit(1);
		}
		
		if (uniprotCode == null){
			System.err.println("Missing input UniprotCode (-u)");
			System.err.println(help);
			System.exit(1);
		}
		
		if (organism == null){
			System.err.println("Missing input type of organism (-o)");
			System.err.println(help);
			System.exit(1);
		}
				
        ChainEvolContextList cecs = (ChainEvolContextList)Goodies.readFromFile(chainevolc);
        
        for (ChainEvolContext cec:cecs.getAllChainEvolContext()){
        	SecondaryStructure ss = null;
        	
        	System.out.println(cec.getQuery().getUniprotId());
        	
			if (! (cec.getQuery().getUniprotId()).equalsIgnoreCase(uniprotCode)){
				continue;
			}
			
			List<Double> listEnt = cec.getConservationScores(ScoringType.ENTROPY);
			List<Double> listKaK = cec.getConservationScores(ScoringType.KAKS);
				
			Sequence seq = cec.getQuery().getSeq();
			String seqStr = seq.getSeq();
			
			File fastaFile = new File("/tmp/tmp.fasta");
			seq.writeToFastaFile(fastaFile);
			
			SignalPRunner signalp = new SignalPRunner(signalpexe);
			signalp.runSignalP(fastaFile, organism,seq.getLength());
									
			if (cec.getQuery().getLength() <= 800) {
				System.out.println(seqStr);
			
				JPredConnection jpredcon = new JPredConnection();
				jpredcon.setDebugMode(true);
				jpredcon.setTimeout(1000);
				jpredcon.submitQuery(seqStr);
			
				System.out.println(seqStr);
				System.out.println(jpredcon.getSecondaryStructurePrediction());
				ss = jpredcon.getSecondaryStructurePredictionObject();
			} 
			else {
				/* 
				 * either split sequence in two parts, truncate or use different method
				 */		
				if (args.length < 3) {
					System.out.println("\n############################################");
					System.out.println("SEQUENCE too long: "+seq.getLength());
					System.out.println("Please provide a file <psipred.txt> and rerun!");
					System.exit(1);
				}
				else {
					File psipredfile = new File(psipfile);
					ss = new SecondaryStructure(psipredfile);
				}				
			}
			
			List<Integer> sssymb = SecStruToInt(listEnt,ss);
			
			
			
			
			System.out.println("#"+cec.getQuery().getUniprotId());
			System.out.printf("%s %3s %9s %8s %10s %3s %3s%n", "# Nr", "Aa","Entropy","KaKs","SecStr", "SSconfidence", "SignalP");
			for (int i = 0; i < listEnt.size(); i++) {
				if (i < signalp.getStop()){
					System.out.printf("%4d %2s %9.3f %10.3f %5s %2d %9.2f %8s%n", i+1, seqStr.charAt(i),listEnt.get(i),listKaK.get(i),ss.getSecStrucElement(i+1).getType(),sssymb.get(i),ss.getConfidence(i+1),"1");
				}
				else {
					System.out.printf("%4d %2s %9.3f %10.3f %5s %2d %9.2f %8s%n", i+1, seqStr.charAt(i),listEnt.get(i),listKaK.get(i),ss.getSecStrucElement(i+1).getType(),sssymb.get(i),ss.getConfidence(i+1),"0");
				}
			}
			
			//		String outfile = "/tmp/boundary_"+cec.getQuery().getUniId()+".txt";
			String outfile = "values_"+cec.getQuery().getUniprotId()+".txt";
			
			FileWriter fstream = new FileWriter(outfile);
			PrintWriter output = new PrintWriter(fstream);
			output.println("#"+cec.getQuery().getUniprotId());
			output.printf("%s %3s %9s %8s %10s %3s %3s%n", "# Nr", "Aa","Entropy","KaKs","SecStr", "SSconfidence", "SignalP");

			for (int i = 0; i < listEnt.size(); i++) {
				if (i+1 < signalp.getStop()){
					output.printf("%4d %2s %9.3f %10.3f %5s %2d %9.2f %8s%n", i+1, seqStr.charAt(i),listEnt.get(i),listKaK.get(i),ss.getSecStrucElement(i+1).getType(),sssymb.get(i),ss.getConfidence(i+1),"1");
				}
				else {
					output.printf("%4d %2s %9.3f %10.3f %5s %2d %9.2f %8s%n", i+1, seqStr.charAt(i),listEnt.get(i),listKaK.get(i),ss.getSecStrucElement(i+1).getType(),sssymb.get(i),ss.getConfidence(i+1),"0");
				}
			}
			output.close();

			System.out.println("done");
			break;
        }

	}
	
	
	static List<Integer> SecStruToInt (List<Double> listEntropy, SecondaryStructure ssprediction){
		List<Integer> toint = new ArrayList<Integer>();
		for (int i = 0; i < listEntropy.size(); i++){			
			switch (ssprediction.getSecStrucElement(i+1).getType()){
				case 'H': toint.add(1); break;
				case 'E': toint.add(2); break;
				default:  toint.add(3); break;
			}
		}
		return toint;
	}
	

}
