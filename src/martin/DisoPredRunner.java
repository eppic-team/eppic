package martin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;


public class DisoPredRunner {

	private File disopredbin;
	private ArrayList<DisopredResidue> disopredout;

	
	public DisoPredRunner (File disopredbin){
		this.disopredbin = disopredbin;
	}
	
	public void runDisoPred (String fasta) throws IOException, InterruptedException {

		File fastafile = new File(fasta+".fasta");
		String cmd = disopredbin+" "+fastafile;
	
		Process disopred = Runtime.getRuntime().exec(cmd);
		BufferedReader disopredOut = new BufferedReader(new InputStreamReader(disopred.getInputStream()));

		String line;
		while((line = disopredOut.readLine()) != null) {
			System.out.println(line);
		}

		int exitValue = disopred.waitFor();
		
		if (exitValue>0) {
			throw new IOException("disopred exited with error value " + exitValue);
		}

		String horiz=fasta+".horiz_d";
		
		setDisopredout(readDisopredOutput(horiz));
	}

	
	
	public ArrayList<DisopredResidue> readDisopredOutput(String horiz) throws IOException{
		
		File disopredOutput = new File(horiz);
		BufferedReader br = new BufferedReader(new FileReader(disopredOutput));
		String line;
					
		String confidences= new String();
		String predictions = new String();
		String aachain = new String();
		
		ArrayList<DisopredResidue> disopredlist = new ArrayList<DisopredResidue>();
		
		while ((line=br.readLine())!=null) {
			if (line.length()==0) continue;
			
			if (line.startsWith("conf: ")) {
				confidences += line.substring(6);
			}
			else if (line.startsWith("pred: ")) {
				predictions += line.substring(6);
			}
			else if (line.startsWith("  AA")) {
				aachain += line.substring(6);
			}
		}
		
		if (confidences.length() != predictions.length() || confidences.length() != aachain.length()){
			throw new IOException("confidences/predictions/chain are not of same length");
		}

		System.out.println(aachain);
		System.out.println(predictions);
		System.out.println(confidences);
		
		for (int i = 0; i < aachain.length(); i++){
			boolean diso;
			
			if (predictions.charAt(i) == '.'){
				diso = false;
			}
			else if (predictions.charAt(i) == '*'){
				diso = true;
			}
			else{
				throw new IOException("error in parsing predictions: character not defined");
			}

			disopredlist.add(new DisopredResidue(aachain.charAt(i), diso, Double.parseDouble("0."+String.valueOf(confidences.charAt(i)))));
		}

		return disopredlist;
	}
	
	public ArrayList<DisopredResidue> getDisopredout() {
		return disopredout;
	}
	
	public DisopredResidue getDisopredout(int i) {
		return disopredout.get(i);
	}

	public void setDisopredout(ArrayList<DisopredResidue> disopredout) {
		this.disopredout = disopredout;
	}

	
	
	
	public static void main(String[] args) throws Exception {
		String disoexe = "/afs/psi.ch/project/bioinfo/software/disopred2.4.3/rundisopred";
		File disopredbin = new File(disoexe);
		String fastafile = "/tmp/sheep";
		
		DisoPredRunner disopred = new DisoPredRunner(disopredbin);
		disopred.runDisoPred(fastafile);
		
		for (int i = 0; i < disopred.getDisopredout().size(); i++) {
			System.out.println(disopred.getDisopredout(i).getResidue()+"\t"+disopred.getDisopredout(i).getConfidence()+"\t"+disopred.getDisopredout(i).isPrediction());
		}
	}

}
