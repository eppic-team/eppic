package eppic;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.List;

import owl.core.structure.ChainInterface;
import owl.core.structure.Residue;

public class MolViewersAdaptor {

	public static void writeJmolScriptFile(ChainInterface interf, double caCutoff, double minAsaForSurface, PymolRunner pr, File dir, String prefix, boolean usePdbResSer) 
			throws FileNotFoundException {

		File file = new File(dir,prefix+"."+interf.getId()+".jmol");
		PrintStream ps = new PrintStream(file);
		ps.print(createJmolScript(interf, caCutoff, minAsaForSurface, pr, usePdbResSer));
		ps.close();

	}

	private static String createJmolScript(ChainInterface interf, double caCutoff, double minAsaForSurface, PymolRunner pr, boolean usePdbResSer) {
		char chain1 = interf.getFirstMolecule().getPdbChainCode().charAt(0);
		char chain2 = interf.getSecondPdbChainCodeForOutput().charAt(0);
		
		String color1 = pr.getHexColorCode(pr.getChainColor(chain1, 0, interf.isSymRelated()));
		String color2 = pr.getHexColorCode(pr.getChainColor(chain2, 1, interf.isSymRelated()));
		color1 = "[x"+color1.substring(1, color1.length())+"]"; // converting to jmol format
		color2 = "[x"+color2.substring(1, color2.length())+"]";
		String colorInterf1 = pr.getHexColorCode(pr.getInterf1Color());
		String colorInterf2 = pr.getHexColorCode(pr.getInterf2Color());
		colorInterf1 = "[x"+colorInterf1.substring(1, colorInterf1.length())+"]";
		colorInterf2 = "[x"+colorInterf2.substring(1, colorInterf2.length())+"]";
		
		StringBuffer sb = new StringBuffer();
		sb.append("cartoon on; wireframe off; spacefill off; set solvent off;\n");
		sb.append("select :"+chain1+"; color "+color1+";\n");
		sb.append("select :"+chain2+"; color "+color2+";\n");
		interf.calcRimAndCore(caCutoff, minAsaForSurface);
		sb.append(getJmolSelString("core", chain1, interf.getFirstRimCore().getCoreResidues(), usePdbResSer)+";\n");
		sb.append(getJmolSelString("core", chain2, interf.getSecondRimCore().getCoreResidues(), usePdbResSer)+";\n");
		sb.append(getJmolSelString("rim", chain1, interf.getFirstRimCore().getRimResidues(), usePdbResSer)+";\n");
		sb.append(getJmolSelString("rim", chain2, interf.getSecondRimCore().getRimResidues(), usePdbResSer)+";\n");
		sb.append("define interface"+chain1+" core"+chain1+" or rim"+chain1+";\n");
		sb.append("define interface"+chain2+" core"+chain2+" or rim"+chain2+";\n");
		sb.append("define bothinterf interface"+chain1+" or interface"+chain2+";\n");
		// surfaces are cool but in jmol they don't display as good as in pymol, especially the transparency effect is quite bad
		//sb.append("select :"+chain1+"; isosurface surf"+chain1+" solvent;color isosurface gray;color isosurface translucent;\n");
		//sb.append("select :"+chain2+"; isosurface surf"+chain2+" solvent;color isosurface gray;color isosurface translucent;\n");
		sb.append("select interface"+chain1+";wireframe 0.3;\n");
		sb.append("select interface"+chain2+";wireframe 0.3;\n");
		sb.append("select core"+chain1+";"+"color "+colorInterf1+";wireframe 0.3;\n");
		sb.append("select core"+chain2+";"+"color "+colorInterf2+";wireframe 0.3;\n");
		if (interf.hasCofactors()) {
			sb.append("select ligand;wireframe 0.3;\n");
		}
		return sb.toString();
	}
	
	private static String getJmolResiSelString(List<Residue> list, char chainName, boolean usePdbResSer) {
		// residue 0 or negatives can exist (e.g. 1epr). In order to have an empty selection we 
		// simply use a very low negative number which is unlikely to exist in PDB
		if (list.isEmpty()) return "-10000:"+chainName;
		StringBuffer sb = new StringBuffer();
		for (int i=0;i<list.size();i++) {
			if (usePdbResSer) {
				// jmol uses a special syntax for residue serials with insertion 
				// codes, e.g. 23A from chain A would be "23^A:A" and not "23A:A"
				// A PDB with this problem is 1yg9, it would show a blank jmol screen before this fix
				String pdbSerial = list.get(i).getPdbSerial();
				char lastChar = pdbSerial.charAt(pdbSerial.length()-1);
				if (!Character.isDigit(lastChar)) {
					pdbSerial = pdbSerial.replace(Character.toString(lastChar), "^"+lastChar);
				}
				sb.append(pdbSerial+":"+chainName);
			} else {
				sb.append(list.get(i).getSerial()+":"+chainName);
			}
			if (i!=list.size()-1) sb.append(",");
		}
		return sb.toString();
	}

	private static String getJmolSelString(String namePrefix, char chainName, List<Residue> list, boolean usePdbResSer) {
		return "define "+namePrefix+chainName+" "+getJmolResiSelString(list,chainName, usePdbResSer);
	}
	

}
