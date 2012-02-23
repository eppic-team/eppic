package martin;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import owl.core.sequence.Sequence;
import owl.core.structure.PdbAsymUnit;
import owl.core.structure.features.SecondaryStructure;
import owl.core.util.Goodies;

import crk.ChainEvolContext;
import crk.ChainEvolContextList;
import crk.ScoringType;




public class ca {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

  
        File chainevolc = new File("/afs/psi.ch/project/fim/projects/ca/selection/rcsb/rcsb066520.chainevolcontext.dat");
        String uniprotCode = "P04128";
        
		
        ChainEvolContextList cecs = (ChainEvolContextList)Goodies.readFromFile(chainevolc);
        
        
        for (ChainEvolContext cec:cecs.getAllChainEvolContext()){
        	SecondaryStructure ss = null;
        	
			if (! (cec.getQuery().getUniprotId()).equalsIgnoreCase(uniprotCode)){
				continue;
			}
			
			List<Double> listEnt = cec.getConservationScores(ScoringType.ENTROPY);
			List<Double> listKaK = cec.getConservationScores(ScoringType.KAKS);
				
			Sequence seq = cec.getQuery().getSeq();
			String seqStr = seq.getSeq();
			File fastaFile = new File("/tmp/tmp.fasta");
			seq.writeToFastaFile(fastaFile);
			
			
			
			
			
			PdbAsymUnit pdb = new PdbAsymUnit(new File("/afs/psi.ch/project/fim/projects/ca/selection/rcsb/rcsb066520.pdb"));
			ss = pdb.getChain("D").getSecondaryStructure();
			
	
			List<Integer> sssymb = SecStruToInt(cec,listEnt,ss);

	
			String outfile = "/afs/psi.ch/project/fim/projects/ca/selection/rcsb/values_"+cec.getQuery().getUniprotId()+".txt";
			//String outfile = "values_"+cec.getQuery().getUniId()+".txt";
			
			FileWriter fstream = new FileWriter(outfile);
			PrintWriter output = new PrintWriter(fstream);
			output.println("#"+cec.getQuery().getUniprotId());
			output.printf("%s %3s %9s %8s %10s %n", "# Nr", "Aa","Entropy","KaKs","SecStr");

			for (int i = 0; i < listEnt.size(); i++) {
				output.printf("%4d %2s %9.3f %10.3f %2d%n", i+1, seqStr.charAt(i),listEnt.get(i),listKaK.get(i), sssymb.get(i));
			}
			output.close();

			System.out.println("done");
			break;
        }

	}
	
	
	static List<Integer> SecStruToInt (ChainEvolContext cec, List<Double>  listEntropy, SecondaryStructure ssprediction){
		List<Integer> toint = new ArrayList<Integer>();
				
		for (int i = 0; i < listEntropy.size(); i++){
			int serial = cec.getPDBPosForQueryUniprotPos(i);
			
			if (ssprediction.getSecStrucElement(serial) == null){
				toint.add(3);
			}
			else{
				switch (ssprediction.getSecStrucElement(serial).getType()){
					case 'H': toint.add(1); break;
					case 'S': toint.add(2); break;
					default:  toint.add(3); break;
				}
			}
		}
		return toint;
	}
	

}
