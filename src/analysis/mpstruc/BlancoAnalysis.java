package analysis.mpstruc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

public class BlancoAnalysis {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		if (args.length<2) {
			System.err.println("Usage: BlancoAnalysis <input xml blanco db> <input tab file with resols/rfrees>");
			System.exit(1);
		}
		
		File inputXMLFile = new File(args[0]);
		File inTabFile = new File(args[1]);
		
		double resolution = 2.8;
		double rFree = 0.3;
				
		String prefixWikiTableStr = " | | | | | | | |";
		String pdbUrlSuffix = "http://www.pdb.org/pdb/explore/explore.do?structureId=";
		
		HashMap<String,double[]> pdbcodes2resolrfree = parseTabFile(inTabFile);
		
		BlancoDbXMLParser parser = new BlancoDbXMLParser(inputXMLFile);		
		BlancoDb db = parser.getBlancoDb();

		// setting resols and rfrees from files and removing those not present in file
		for (BlancoCluster cluster:db) {
			Iterator<BlancoEntry> it = cluster.iterator();
			
			while (it.hasNext()) {
				BlancoEntry entry = it.next();
				double[] resolrfree = pdbcodes2resolrfree.get(entry.getPdbCode());
				if (resolrfree==null) {
					it.remove();
					continue;
				}
				entry.setExpMethod("X-RAY DIFFRACTION");
				entry.setResolution(resolrfree[0]);
				entry.setrFree(resolrfree[1]);
			}
		}
		
		
		String lastgroup = null;
		String lastsubgroup = null;
		for (BlancoCluster cluster:db) {
			String group = cluster.getGroup();
			String subgroup = cluster.getSubgroup();

			if (lastgroup==null || !lastgroup.equals(group)) {
				System.out.println("\n\nh3. "+group);
			}
			if (lastsubgroup==null || !lastsubgroup.equals(subgroup)) {
				System.out.println("| | *"+subgroup+"* |"+prefixWikiTableStr);
			}

			
			BlancoEntry best = cluster.getBestResolutionMember();
			
			if (	best==null || 
					best.getResolution()>=resolution ||
					best.getrFree()<0 ||
					best.getrFree()>=rFree) {

				// no good enough representative
				
				lastgroup = group;
				lastsubgroup = subgroup;
				
				continue;
			}
			
			
			
				
			System.out.println("| ["+best.getPdbCode()+"|"+pdbUrlSuffix+best.getPdbCode()+"] | "+cluster.getName()+" |"+prefixWikiTableStr);
			
			
			
			lastgroup = group;
			lastsubgroup = subgroup;
		}
		//db.printTabular(System.out);
		
	}
	
	private static HashMap<String,double[]> parseTabFile(File file) throws IOException {
		
		HashMap<String,double[]> pdbcodes2resolrfree = new HashMap<String, double[]>();
		
		BufferedReader br = new BufferedReader(new FileReader(file));
		
		String line;		
		while ((line=br.readLine())!=null) {
			if (line.startsWith("#")) continue;
			String[] tokens = line.split("\\s+");
			String pdbcode = tokens[0];
			double resol = Double.parseDouble(tokens[1]);
			double rfree = Double.parseDouble(tokens[2]);
			double[] resolrfree = {resol,rfree};
			pdbcodes2resolrfree.put(pdbcode,resolrfree);
		}
		
		br.close();
		
		return pdbcodes2resolrfree;
	}

}
