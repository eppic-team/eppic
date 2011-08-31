package martin;

import java.io.File;
import java.util.List;

import owl.core.sequence.Sequence;
import owl.core.util.Goodies;

import crk.ChainEvolContext;
import crk.ChainEvolContextList;
import crk.ScoringType;

public class ReadCRKdat {

	public static void main(String[] args) throws Exception {
		
		if(args.length < 2) {
			System.out.println("Usage: ReadCRKdat <PDB.chainevolcontext.dat> <UniprotCode>");
			System.exit(1);
		}
		
		File file = new File(args[0]);
		
		ChainEvolContextList cecs = (ChainEvolContextList)Goodies.readFromFile(file);
		
		
		for (ChainEvolContext cec:cecs.getAllChainEvolContext()){
			
			System.out.println(cec.getQuery().getUniId());
			
			if (! (cec.getQuery().getUniId()).equalsIgnoreCase(args[1])){
				continue;
			}
			
			
            List<Double> list = cec.getConservationScores(ScoringType.ENTROPY);
            
            System.out.println("\n### "+cec.getQuery().getUniId());
            Sequence seq = cec.getQuery().getUniprotSeq();
            
            System.out.println(seq);
            
            System.out.println("#######################"+cec.getRepresentativeChainCode());

            for (int i=0;i<list.size();i++){
                System.out.println(seq.getSeq().charAt(i)+" "+list.get(i));
            }
            
		}
		
		

	}

}
