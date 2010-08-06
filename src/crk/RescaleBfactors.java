package crk;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import owl.core.structure.Atom;
import owl.core.structure.Pdb;
import owl.core.structure.PdbfilePdb;


public class RescaleBfactors {

	private static final double MIN_BFACTOR = 2.0;
	private static final double MAX_BFACTOR = 100.0;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		
		if (args.length<2) {
			System.err.println("Must give 2 arguments: <input PDB file> <output rescaled PDB file>");
			System.exit(1);
		}
		File file = new File(args[0]);
		File outfile = new File(args[1]);
		
		String[] chains = new PdbfilePdb(file.getAbsolutePath()).getChains();
		
		Pdb[] pdbs = new Pdb[chains.length];
		for (int i=0;i<chains.length;i++) {
			pdbs[i] = new PdbfilePdb(file.getAbsolutePath());
			pdbs[i].load(chains[i]);
		}
		
		List<Double> bfactors = new ArrayList<Double>();
		
		for (Pdb pdb:pdbs) {
			bfactors.addAll(getBfactors(pdb));
		}
		
		double maxBfactor = Collections.max(bfactors);
		double minBfactor = Collections.min(bfactors);
		
		double scale = (MAX_BFACTOR-MIN_BFACTOR)/(maxBfactor-minBfactor);
		double offset = MIN_BFACTOR - minBfactor*scale;
		
		PrintStream ps = new PrintStream(outfile);
		for (Pdb pdb:pdbs) {
			scaleBfactors(pdb, scale, offset);
			pdb.writeAtomLines(ps);
		}
		ps.close();
	}
	
	private static List<Double> getBfactors(Pdb pdb) {
		List<Double> bfactors = new ArrayList<Double>();
		for (int atomser:pdb.getAllAtomSerials()) {
			bfactors.add(pdb.getAtom(atomser).getBfactor());
		}
		return bfactors;
	}

	private static void scaleBfactors(Pdb pdb, double scale, double offset) {
		for (int atomser:pdb.getAllAtomSerials()) {
			Atom atom = pdb.getAtom(atomser);
			atom.setBfactor(atom.getBfactor()*scale+offset);
		}
	}
	
}
