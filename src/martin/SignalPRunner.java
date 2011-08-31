package martin;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import owl.core.sequence.Sequence;

public class SignalPRunner {
	private int start;
	private int stop;
	private double probability;
	private File signalpbin;
	private String signalstring;

	
	public SignalPRunner (File signalpbin){
		this.signalpbin = signalpbin;
	}
	
	
	public void runSignalP (File fasta, String organism, int seqLength) throws IOException{
		String cmd = signalpbin+" -t "+organism+" -d /tmp/ "+fasta;
		String line;
		
		//System.out.println(cmd);
		
		Process signalp = Runtime.getRuntime().exec(cmd);
		BufferedReader signalpOut = new BufferedReader(new InputStreamReader(signalp.getInputStream()));
		BufferedReader signalpErr = new BufferedReader(new InputStreamReader(signalp.getErrorStream()));

		while((line = signalpOut.readLine()) != null) {
			//System.out.println(line);
			if (line.contains("cleavage")){
				
				Pattern p = Pattern.compile("^.*probability:\\s(\\d\\.\\d+)\\s.*pos\\.\\s+(-?\\d+)\\s+and\\s+(\\d+).*");
				Matcher m = p.matcher(line);
				if (m.matches()) {
					setProbability(Double.parseDouble(m.group(1)));
					setStart(Integer.parseInt(m.group(2)));
					setStop(Integer.parseInt(m.group(3)));
					setSignalstring(seqLength);
					break;
				}			
			}
		}
		signalpOut.close();
		signalpErr.close();
	}
	
	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public int getStop() {
		return stop;
	}

	public void setStop(int stop) {
		this.stop = stop;
	}

	public double getProbability() {
		return probability;
	}

	public void setProbability(double probability) {
		this.probability = probability;
	}

	public String getSignalstring() {
		return signalstring;
	}


	public void setSignalstring(int seqLength) {
		String signal = new String();
		for (int i = 0; i < seqLength; i++){
			if (i+1 < getStop()){
				signal += "1";
			}
			else {
				signal += "0";
			}
		}
		this.signalstring = signal;
	}
	
	
/*
	public static void main(String[] args) throws Exception{
		String filename = "/afs/psi.ch/project/bioinfo2/martin/1ozb/qxSFcZJvdpVT4iDQtKoAcMpbkkhyFL/1ozb.chainevolcontext.dat";
		
		File file = new File(filename);
		
		ChainEvolContextList cecs = (ChainEvolContextList)Goodies.readFromFile(file);
		for (ChainEvolContext cec:cecs.getAllChainEvolContext()){
			if (! (cec.getQuery().getUniId()).equalsIgnoreCase("P44853")){
				continue;
			}
			Sequence seq = cec.getQuery().getUniprotSeq();
			File fastaFile = new File("/tmp/tmp.fasta");
			seq.writeToFastaFile(fastaFile);
			
			File signalpexe = new File("/afs/psi.ch/project/bioinfo/software/signalp-3.0/signalp");
			
			SignalPRunner runner = new SignalPRunner(signalpexe);
			runner.runSignalP(fastaFile, "gram-");
		
			System.out.println(runner.getProbability());
			System.out.println(runner.getStart());
			System.out.println(runner.getStop());
			break;
		}
	}
	*/

	public static void main(String[] args) throws Exception{
		
			File fastaFile = new File("/tmp/P04128.fasta");
			Sequence seq = Sequence.readSeqs(fastaFile, Pattern.compile(">(.*)")).get(0);
			
			File signalpexe = new File("/afs/psi.ch/project/bioinfo/software/signalp-3.0/signalp");
			
			SignalPRunner runner = new SignalPRunner(signalpexe);
			runner.runSignalP(fastaFile, "gram-", seq.getLength());
		
			System.out.println(runner.getProbability());
			System.out.println(runner.getStart());
			System.out.println(runner.getStop());
			
			System.out.println(seq.getSeq());
			System.out.println(runner.getSignalstring());
	}

}