package eppic.analysis;

import java.io.File;
import java.util.List;
import java.util.TreeMap;

import owl.core.connections.PiqsiAnnotation;
import owl.core.connections.PiqsiConnection;

public class CompareToPiqsi {

	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		if (args.length<3) {
			System.err.println("Usage: CompareToPiqsi <PiQSi file> <xtal dataset file> <bio dataset file>");
			System.exit(1);
		}
		
		PiqsiConnection pc = new PiqsiConnection(new File(args[0]));
		
		File xtaldatasetFile = new File(args[1]);
		File biodatasetFile = new File(args[2]);
		
		TreeMap<String,List<Integer>> xtaldataset = Utils.readListFile(xtaldatasetFile);
		TreeMap<String,List<Integer>> biodataset = Utils.readListFile(biodatasetFile);
		
		System.out.println(xtaldatasetFile);
		System.out.println("#pdb pdb_subunits piqsi_subunits");
		
		int countMissing = 0;
		int countAgree = 0;
		
		for (String pdbCode:xtaldataset.keySet()) {
			PiqsiAnnotation an = pc.getAnnotation(pdbCode);
			
			if (an==null) {
				countMissing++;
			} else {
				if (an.getPiqsiSubunits()==1) {
					countAgree++;
					System.out.println(pdbCode+" "+an.getPdbSubunits()+" "+an.getPiqsiSubunits());
				} 
			}
		}
		int countPresent = xtaldataset.size()-countMissing;
		
		System.out.println("Total in xtaldataset    : "+xtaldataset.size());
		System.out.println(" present in PiQSi       : "+countPresent);
		System.out.println(" agreeing with PiQSi    : "+countAgree);
		System.out.println(" disagreeing with PiQSi : "+(countPresent-countAgree));

		
		System.out.println(biodatasetFile);
		System.out.println("#pdb pdb_subunits piqsi_subunits");
		
		countMissing = 0;
		countAgree = 0;
		
		
		for (String pdbCode:biodataset.keySet()) {
			PiqsiAnnotation an = pc.getAnnotation(pdbCode);
			
			if (an==null) {
				countMissing++;
			} else {
				if (an.getPiqsiSubunits()>1) {
					countAgree++;
					System.out.println(pdbCode+" "+an.getPdbSubunits()+" "+an.getPiqsiSubunits());
				} 
			}
		}
		countPresent = biodataset.size()-countMissing;
				
		System.out.println("Total in biodataset     : "+biodataset.size());
		System.out.println(" present in PiQSi       : "+countPresent);
		System.out.println(" agreeing with PiQSi    : "+countAgree);
		System.out.println(" disagreeing with PiQSi : "+(countPresent-countAgree));
	}

}
