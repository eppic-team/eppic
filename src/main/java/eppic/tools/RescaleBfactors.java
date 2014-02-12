package eppic.tools;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import owl.core.structure.Atom;
import owl.core.structure.PdbChain;
import owl.core.structure.PdbAsymUnit;
import owl.core.structure.Residue;


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
		
		PdbAsymUnit pdb = new PdbAsymUnit(file);
		
		List<Double> bfactors = new ArrayList<Double>();
		
		for (PdbChain chain:pdb.getAllChains()) {
			bfactors.addAll(getBfactors(chain));
		}
		
		double maxBfactor = Collections.max(bfactors);
		double minBfactor = Collections.min(bfactors);
		
		double scale = (MAX_BFACTOR-MIN_BFACTOR)/(maxBfactor-minBfactor);
		double offset = MIN_BFACTOR - minBfactor*scale;
		
		PrintStream ps = new PrintStream(outfile);
		for (PdbChain chain:pdb.getAllChains()) {
			scaleBfactors(chain, scale, offset);
			chain.writeAtomLines(ps);
		}
		ps.close();
	}
	
	private static List<Double> getBfactors(PdbChain pdb) {
		List<Double> bfactors = new ArrayList<Double>();
		for (Residue residue:pdb) {
			for (Atom atom:residue) {
				bfactors.add(atom.getBfactor());
			}
		}
		return bfactors;
	}

	private static void scaleBfactors(PdbChain pdb, double scale, double offset) {
		for (Residue residue:pdb) {
			for (Atom atom:residue) {
				atom.setBfactor(atom.getBfactor()*scale+offset);
			}
		}
	}
	
}
