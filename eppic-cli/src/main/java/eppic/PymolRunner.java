package eppic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.biojava.nbio.structure.Chain;
import org.biojava.nbio.structure.Group;
import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.contact.StructureInterface;
import org.biojava.nbio.structure.contact.StructureInterfaceList;
import org.biojava.nbio.structure.io.PDBFileParser;


public class PymolRunner {
	
	/**
	 * We use 26 colors corresponding to chain letters A to Z (second 13 are repeated from first 13)
	 */
	private static final String[] DEF_CHAIN_COLORS = 
	{"green","cyan","yellow","white","lightblue","magenta","red","orange","wheat","limon","salmon","palegreen","lightorange",
	 "green","cyan","yellow","white","lightblue","magenta","red","orange","wheat","limon","salmon","palegreen","lightorange",};
	
	private static final String DEF_SYM_RELATED_CHAIN_COLOR = "grey";
	
	private static final String[] DEF_CHAIN_COLORS_ASU_ALLINTERFACES = 
		{"green", "tv_green", "chartreuse", "splitpea", "smudge", "palegreen", "limegreen", "lime", "limon", "forest"};
	
	private static final String DEF_TN_STYLE = "cartoon";
	private static final String DEF_TN_BG_COLOR = "white";
	private static final int[] DEF_TN_HEIGHTS = {75};
	private static final int[] DEF_TN_WIDTHS = {75};
	
	private static final double MIN_INTERF_AREA_TO_DISPLAY = 400;
	
	private File pymolExec;
	private String[] chainColors;
	private String symRelatedColor;
	private String interf1color;
	private String interf2color;
	
	private HashMap<String, String> colorMappings;
	
	public PymolRunner(File pymolExec) {
		this.pymolExec = pymolExec;
		chainColors = DEF_CHAIN_COLORS;
		symRelatedColor = DEF_SYM_RELATED_CHAIN_COLOR;
	}
	
	public void setColors(String[] chainColors, String symRelatedColor) {
		this.chainColors = chainColors;
		this.symRelatedColor = symRelatedColor;
	}
	
	/**
	 * Generates png images of the desired heights and widths with the specified style and 
	 * coloring each chain with a color as set in {@link #setColors(String[], String)}
	 * @param pdbFile
	 * @param outPngFiles output png file names
	 * @param style can be cartoon, surface, spheres
	 * @param bgColor the background color for the image: black, white, gray
	 * @param heights
	 * @param widths 
	 * @throws IOException 
	 * @throws InterruptedException 
	 * @throws IllegalArgumentException if heights length differs from widths length
	 */
	public void generatePng(File pdbFile, File[] outPngFiles, String style, String bgColor, int[] heights, int[] widths) 
	throws IOException, InterruptedException {
		
		if (heights.length!=widths.length || heights.length!=outPngFiles.length) 
			throw new IllegalArgumentException("The number of heights is different from the number of widths or the number of output png files");
		String molecName = pdbFile.getName().substring(0, pdbFile.getName().lastIndexOf('.'));
		
		PDBFileParser pdbpars = new PDBFileParser();

		Structure s = pdbpars.parsePDBFile(new FileInputStream(pdbFile)) ;

		List<Chain> chains = s.getChains();
		
		List<String> command = new ArrayList<String>();
		command.add(pymolExec.getAbsolutePath());
		command.add("-q");
		command.add("-c");

		StringBuffer pymolScriptBuilder = new StringBuffer();
		
		pymolScriptBuilder.append("load "+pdbFile.getAbsolutePath() + ";");
		
		pymolScriptBuilder.append("bg "+bgColor + ";");
	
		pymolScriptBuilder.append("orient;");

		pymolScriptBuilder.append("remove solvent;");
		
		pymolScriptBuilder.append("as "+style + ";");
		for (int c=0;c<chains.size();c++) {
			char letter = chains.get(c).getChainID().charAt(0);
			String color = null;
			if (letter<'A' || letter>'Z') {
				// if out of the range A-Z then we assign simply a color based on the chain index
				color = chainColors[c%chainColors.length];
			} else {
				// A-Z correspond to ASCII codes 65 to 90. The letter ascii code modulo 65 gives an indexing of 0 (A) to 25 (Z)
				// a given letter will always get the same color assigned
				color = chainColors[letter%65];	
			}
			pymolScriptBuilder.append("color "+color+", "+molecName+" and chain "+letter + ";");
		}

		for (int i=0;i<heights.length;i++) {
			pymolScriptBuilder.append("viewport "+heights[i]+","+widths[i] + ";");
			
			pymolScriptBuilder.append("ray;");
			
			pymolScriptBuilder.append("png "+outPngFiles[i].getAbsolutePath() + ";");
		}
		
		pymolScriptBuilder.append("quit;");
		
		command.add("-d");
		command.add(pymolScriptBuilder.toString());
		
		Process pymolProcess = new ProcessBuilder(command).start();
		int exit = pymolProcess.waitFor();
		if (exit!=0) {
			throw new IOException("Pymol exited with error status "+exit);
		}
	}
	
	/**
	 * Generates png file, pymol pse file and pml script for given interface producing a 
	 * mixed cartoon/surface representation of interface with selections 
	 * coloring each chain with a color as set in {@link #setColors(String[], String)} and 
	 * through {@link #readColorsFromPropertiesFile(InputStream)}
	 * @param interf
	 * @param caCutoff
	 * @param minAsaForSurface
	 * @param pdbFile
	 * @param pseFile
	 * @param pmlFile
	 * @param base
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
	public void generateInterfPngPsePml(StructureInterface interf, double caCutoff, double minAsaForSurface, 
			File pdbFile, File pseFile, File pmlFile, String base) 
	throws IOException, InterruptedException {
		
		String molecName = getPymolMolecName(pdbFile);

		File[] pngFiles = new File[DEF_TN_HEIGHTS.length];
		for (int i=0;i<DEF_TN_HEIGHTS.length;i++) {
			pngFiles[i] = new File(pdbFile.getParent(),base+"."+DEF_TN_WIDTHS[i]+"x"+DEF_TN_HEIGHTS[i]+".png");
		}
		
		// TODO before Biojava move, for second chain we used to have getSecondPdbChainCodeForOutput (i.e. the next one from first if they were equal)
		char chain1 = interf.getMoleculeIds().getFirst().charAt(0);
		char chain2 = interf.getMoleculeIds().getSecond().charAt(0);
		
		String color1 = getChainColor(chain1, 0, interf.isSymRelated());
		String color2 = getChainColor(chain2, 1, interf.isSymRelated());
		
		List<String> command = new ArrayList<String>();
		command.add(pymolExec.getAbsolutePath());
		command.add("-q");
		command.add("-c");


		// NOTE we used to pass all commands in one string after -d (with the pymolScriptBuilder StringBuffer.
		//      But pymol 1.3 and 1.4 seem to have problems with very long strings (causing segfaults)
		//      Because of that now we write most commands to pml file (which we were doing anyway so that users can 
		//      use the pml scripts if they want) and then load the pmls with pymol "@" command
		
		
		StringBuffer pymolScriptBuilder = new StringBuffer();
		PrintStream pml = new PrintStream(pmlFile);
		
		pymolScriptBuilder.append("load "+pdbFile.getAbsolutePath()+";");
				
		String cmd;

		cmd = "orient";
		writeCommand(cmd, pml);
		
		cmd = "remove solvent";
		writeCommand(cmd, pml);
		
		cmd = "as cartoon";
		writeCommand(cmd, pml);
		
		cmd = "color "+color1+", "+molecName+" and chain "+chain1;
		writeCommand(cmd, pml);
		cmd = "color "+color2+", "+molecName+" and chain "+chain2;
		writeCommand(cmd, pml);
		
		cmd = "select chain"+chain1+", chain "+chain1;
		writeCommand(cmd, pml);
		cmd = "select chain"+chain2+", chain "+chain2;
		writeCommand(cmd, pml);
		
		cmd = getSelString("core", chain1, interf.getCoreResidues(caCutoff, minAsaForSurface).getFirst());
		writeCommand(cmd, pml);
		cmd = getSelString("core", chain2, interf.getCoreResidues(caCutoff, minAsaForSurface).getSecond());
		writeCommand(cmd, pml);
		cmd = getSelString("rim", chain1, interf.getRimResidues(caCutoff, minAsaForSurface).getFirst());
		writeCommand(cmd, pml);
		cmd = getSelString("rim", chain2, interf.getRimResidues(caCutoff, minAsaForSurface).getSecond());
		writeCommand(cmd, pml);
		
		cmd = "select interface"+chain1+", core"+chain1+" or rim"+chain1;
		writeCommand(cmd, pml);
		cmd = "select interface"+chain2+", core"+chain2+" or rim"+chain2;
		writeCommand(cmd, pml);
		cmd = "select bothinterf , interface"+chain1+" or interface"+chain2;
		writeCommand(cmd, pml);
		// not showing surface anymore, was not so useful 
		//cmd = "show surface, chain "+chain1;
		//writeCommand(cmd, pml);
		//cmd = "show surface, chain "+chain2;
		//writeCommand(cmd, pml);
		//pymolScriptBuilder.append("color blue, core"+chains[0]+";");
		//pymolScriptBuilder.append("color red, rim"+chains[0]+";");
		cmd = "color "+interf1color+", core"+chain1;
		writeCommand(cmd, pml);
		//pymolScriptBuilder.append("color slate, core"+chains[1]+";");
		//pymolScriptBuilder.append("color raspberry, rim"+chains[1]+";");
		cmd = "color "+interf2color+", core"+chain2;
		writeCommand(cmd, pml);
		cmd = "show sticks, bothinterf";
		writeCommand(cmd, pml);
		//cmd = "set transparency, 0.35";
		//writeCommand(cmd, pml);
		//pymolScriptBuilder.append("zoom bothinterf"+";");
		
		// TODO do we need to check something before issuing the cofactors command???
		//if (interf.hasCofactors()) {
		cmd = "select cofactors, org;";
		writeCommand(cmd, pml);
		cmd = "show sticks, cofactors;";
		writeCommand(cmd, pml);
		//}
		
		cmd = "select none";// so that the last selection is deactivated
		writeCommand(cmd, pml);
		
		pml.close();
		
		pymolScriptBuilder.append("@ "+pmlFile+";");
		
		pymolScriptBuilder.append("save "+pseFile+";");

		// and now creating the png thumbnail
		pymolScriptBuilder.append("bg "+DEF_TN_BG_COLOR+";");
		
		pymolScriptBuilder.append("as "+DEF_TN_STYLE+";");
		
		// TODO do we need to check something before issuing the cofactors command???
		//if (interf.hasCofactors()) {
		pymolScriptBuilder.append("show sticks, org;");
		//}
		
		pymolScriptBuilder.append("color "+color1+", "+molecName+" and chain "+chain1+";");
		pymolScriptBuilder.append("color "+color2+", "+molecName+" and chain "+chain2+";");
		
		pymolScriptBuilder.append("set ray_opaque_background, off;");
		
		for (int i=0;i<DEF_TN_HEIGHTS.length;i++) {
			pymolScriptBuilder.append("viewport "+DEF_TN_HEIGHTS[i]+","+DEF_TN_WIDTHS[i] + ";");

			pymolScriptBuilder.append("ray;");
			
			pymolScriptBuilder.append("png "+pngFiles[i].getAbsolutePath() + ";");
		}
		
		pymolScriptBuilder.append("quit;");
		
		command.add("-d");
		
		//System.out.println(pymolScriptBuilder.toString());
		
		command.add(pymolScriptBuilder.toString());

		
		Process pymolProcess = new ProcessBuilder(command).start();
		int exit = pymolProcess.waitFor();
		if (exit!=0) {
			throw new IOException("Pymol exited with error status "+exit);
		}
	}
	
	public void generateInterfacesPse(File asuPdbFile, Set<String> chains, 
			File pmlFile, File pseFile, File[] interfacePdbFiles, 
			StructureInterfaceList interfaces) 
		throws IOException, InterruptedException {
		
		String molecName = getPymolMolecName(asuPdbFile);
		
		List<String> command = new ArrayList<String>();
		command.add(pymolExec.getAbsolutePath());
		command.add("-q");
		command.add("-c");

		StringBuffer pymolScriptBuilder = new StringBuffer();
		PrintStream pml = new PrintStream(pmlFile);
		
		String cmd;

		
		// loading and coloring
		cmd = "load "+asuPdbFile.getAbsolutePath();
		writeCommand(cmd,pml);
		
		cmd = "show cell";
		writeCommand(cmd,pml);
		
		int i = 0;
		for (String chain:chains) {
			cmd = "color "+DEF_CHAIN_COLORS_ASU_ALLINTERFACES[i%DEF_CHAIN_COLORS_ASU_ALLINTERFACES.length]+
					", "+molecName+" and chain "+chain;
			writeCommand(cmd, pml);
			
			i++;			
		}
				
		i = 1;
		for (File interfPdbFile:interfacePdbFiles) {
			cmd = "load "+interfPdbFile.getAbsolutePath();
			writeCommand(cmd,pml);
			
			String symMolecName = getPymolMolecName(interfPdbFile);
			String color = chainColors[i%chainColors.length];
			cmd = "color "+color+", "+symMolecName;
			writeCommand(cmd, pml);
			i++;
		}
		
		// selections 
		
		i = 0;
		for (String chain:chains) {
			
			cmd = "select "+molecName+"."+chain+", "+molecName+" and chain " + chain;
			writeCommand(cmd,pml);

			i++;			
		}
		
		i = 1;
		for (File interfPdbFile:interfacePdbFiles) {
			StructureInterface interf = interfaces.get(i);
			
			// TODO before Biojava move, for second chain we used to have getSecondPdbChainCodeForOutput (i.e. the next one from first if they were equal)
			char chain1 = interf.getMoleculeIds().getFirst().charAt(0); // TODO this won't work for new mega files, we need to write mmCIF!
			char chain2 = interf.getMoleculeIds().getSecond().charAt(0);
			
			String symMolecName = getPymolMolecName(interfPdbFile);
			
			cmd = "select "+symMolecName+"."+chain1+", "+symMolecName+" and chain " + chain1;
			writeCommand(cmd,pml);
			
			cmd = "select "+symMolecName+"."+chain2+", "+symMolecName+" and chain " + chain2;
			writeCommand(cmd,pml);
			
			i++;
		}
		
		cmd = "remove solvent";
		writeCommand(cmd, pml);
		
		cmd = "as cartoon";
		writeCommand(cmd, pml);

		
		pml.close();
		
		pymolScriptBuilder.append("@ "+pmlFile+";");
		
		pymolScriptBuilder.append("save "+pseFile+";");

		
		
		
		pymolScriptBuilder.append("quit;");
		
		command.add("-d");

		command.add(pymolScriptBuilder.toString());

		
		Process pymolProcess = new ProcessBuilder(command).start();
		int exit = pymolProcess.waitFor();
		if (exit!=0) {
			throw new IOException("Pymol exited with error status "+exit);
		}

	}
	
	/**
	 * Generates a pymol pse file with surface representation and spectrum b-factor coloring
	 * with magenta dots overlaying marking the core residues of each of the interfaces given
	 * @param chain
	 * @param interfaces
	 * @param caCutoffGeom
	 * @param caCutoffCoreSurf
	 * @param minAsaForSurface
	 * @param pdbFile
	 * @param pseFile
	 * @param pmlFile 
	 * @param iconPngFile
	 * @param iconWidth
	 * @param iconHeight
	 * @param minScore the minimum possible score for b factor colors scaling (passed as minimum to PyMOL's spectrum command)
	 * if <0 then no minimum passed (PyMOL will calculate based on present b-factors)
	 * @param maxScore the maximum possible score for b factor colors scaling (passed as maximum to PyMOL's spectrum command)
	 * if <0 then no maximum passed (PyMOL will calculate based on present b-factors) 
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
	public void generateChainPse(Chain chain, StructureInterfaceList interfaces, 
			double caCutoffGeom, double caCutoffCoreSurf, double minAsaForSurface, 
			File pdbFile, File pseFile, File pmlFile,
			File iconPngFile, int iconWidth, int iconHeight,
			double minScore, double maxScore) 
	throws IOException, InterruptedException {
		
		String molecName = getPymolMolecName(pdbFile);

		//char chain1 = chain.getPdbChainCode().charAt(0);
		
		List<String> command = new ArrayList<String>();
		command.add(pymolExec.getAbsolutePath());
		command.add("-q");
		command.add("-c");


		// NOTE we used to pass all commands in one string after -d (with the pymolScriptBuilder StringBuffer.
		//      But pymol 1.3 and 1.4 seem to have problems with very long strings (causing segfaults)
		//      Because of that now we write most commands to pml file (which we were doing anyway so that users can 
		//      use the pml scripts if they want) and then load the pmls with pymol "@" command
		
		
		StringBuffer pymolScriptBuilder = new StringBuffer();
		PrintStream pml = new PrintStream(pmlFile);
		
		pymolScriptBuilder.append("load "+pdbFile.getAbsolutePath()+";");
				
		String cmd;

		cmd = "orient";
		writeCommand(cmd, pml);
		
		cmd = "remove solvent";
		writeCommand(cmd, pml);

		// we need to show sticks so that clicking selects residues (otherwise if only surface shown, clicking doesn't select anything)
		cmd = "as sticks"; 
		writeCommand(cmd, pml);

		cmd = "show surface";
		writeCommand(cmd, pml);
		
		String spectrumParams = "";
		if (minScore>=0) spectrumParams+=", minimum="+minScore;
		if (maxScore>=0) spectrumParams+=", maximum="+maxScore;
		cmd = "spectrum b"+spectrumParams;
		writeCommand(cmd, pml);

		String dotsLayerMolecName = molecName+"Dots";
		cmd = "copy "+dotsLayerMolecName+", "+molecName;
		writeCommand(cmd, pml);

		cmd = "copy "+dotsLayerMolecName+", "+molecName;
		writeCommand(cmd, pml);
		cmd = "h_add "+dotsLayerMolecName;
		writeCommand(cmd, pml);
		cmd = "color magenta, "+dotsLayerMolecName;
		writeCommand(cmd, pml);
		
		String caCutoffGeomStr = String.format("_%2.0f",caCutoffGeom*100.0);
		String caCutoffCoreSurfStr = String.format("_%2.0f",caCutoffCoreSurf*100.0);
		
		List<Integer> interfaceIds = new ArrayList<Integer>();
		
		for (StructureInterface interf:interfaces) {
			if (interf.getTotalArea()<MIN_INTERF_AREA_TO_DISPLAY) continue;
			
			List<Group> cores = null;
			List<Group> rims = null;
			if (interf.getMoleculeIds().getFirst().equals(chain.getChainID())) {
				cores = interf.getCoreResidues(caCutoffGeom, minAsaForSurface).getFirst();
				rims = interf.getRimResidues(caCutoffGeom, minAsaForSurface).getFirst();
				
			} else if (interf.getMoleculeIds().getSecond().equals(chain.getChainID())) {
				cores = interf.getCoreResidues(caCutoffGeom, minAsaForSurface).getSecond();
				rims = interf.getRimResidues(caCutoffGeom, minAsaForSurface).getSecond();

				
			} else {
				continue;
			}
			int id = interf.getId();
			interfaceIds.add(id);
			
			
			selectRimCore(pml, cores, rims, dotsLayerMolecName, id+"Dots"+caCutoffGeomStr);
			
			cmd = "select interface"+id+"Dots, core"+id+"Dots"+caCutoffGeomStr+" or rim"+id+"Dots"+caCutoffGeomStr;
			writeCommand(cmd, pml);
			
			selectRimCore(pml, cores, rims, molecName, id+caCutoffGeomStr);
			
			cmd = "select interface"+id+", core"+id+caCutoffGeomStr+" or rim"+id+caCutoffGeomStr;
			writeCommand(cmd, pml);


			if (interf.getMoleculeIds().getFirst().equals(chain.getChainID())) {
				cores = interf.getCoreResidues(caCutoffCoreSurf, minAsaForSurface).getFirst();
				rims = interf.getRimResidues(caCutoffCoreSurf, minAsaForSurface).getFirst();

				
			} else if (interf.getMoleculeIds().getSecond().equals(chain.getChainID())) {
				cores = interf.getCoreResidues(caCutoffCoreSurf, minAsaForSurface).getSecond();
				rims = interf.getRimResidues(caCutoffCoreSurf, minAsaForSurface).getSecond();

				
			} 
			selectRimCore(pml, cores, rims, dotsLayerMolecName, id+"Dots"+caCutoffCoreSurfStr);

			selectRimCore(pml, cores, rims, molecName, id+caCutoffCoreSurfStr);

		}
		
		// remove from the dots layer everything not in an interface
		cmd = "remove "+dotsLayerMolecName+" ";
		for (int id:interfaceIds) {
			cmd+=" and not interface"+id+"Dots";
		}
		writeCommand(cmd, pml);
		
		cmd = "hide everything, "+dotsLayerMolecName;
		writeCommand(cmd, pml);
		
		// show the dots for core+caCutoffCoreSurf only 
		for (int id:interfaceIds) {
			cmd = "as dots, core"+id+"Dots"+caCutoffCoreSurfStr;			
			writeCommand(cmd, pml);		
			break; // i.e. we only show cores for the first interface
		}

		cmd = "set dot_width=1, "+dotsLayerMolecName;
		writeCommand(cmd, pml);

		
		cmd = "select none";// so that the last selection is deactivated
		writeCommand(cmd, pml);
		
		pml.close();
		
		pymolScriptBuilder.append("@ "+pmlFile+";");
		
		pymolScriptBuilder.append("save "+pseFile+";");
		
		// writing finally the icon png (we don't need this in pml)
		pymolScriptBuilder.append("hide everything, "+dotsLayerMolecName+";"); // we don't want the pink dots for icons
		pymolScriptBuilder.append("viewport " + iconHeight + "," + iconWidth + ";");
		pymolScriptBuilder.append("set ray_opaque_background, off;");
		pymolScriptBuilder.append("ray;");
		pymolScriptBuilder.append("png "+iconPngFile+";");

		pymolScriptBuilder.append("quit;");
		
		command.add("-d");
		
		//System.out.println(pymolScriptBuilder.toString());
		
		command.add(pymolScriptBuilder.toString());

		
		Process pymolProcess = new ProcessBuilder(command).start();
		int exit = pymolProcess.waitFor();
		if (exit!=0) {
			throw new IOException("Pymol exited with error status "+exit);
		}
	}

	
	public String getChainColor(char letter, int index, boolean isSymRelated) {
		String color = null;
		if (isSymRelated && index!=0) {
			color = symRelatedColor;
		} else {
			if (letter<'A' || letter>'Z') {
				// if out of the range A-Z then we assign simply a color based on the chain index
				color = chainColors[index%chainColors.length];
			} else {
				// A-Z correspond to ASCII codes 65 to 90. The letter ascii code modulo 65 gives an indexing of 0 (A) to 25 (Z)
				// a given letter will always get the same color assigned
				color = chainColors[letter%65];	
			}
		}
		return color;
	}
	
	private void selectRimCore(PrintStream pml, List<Group> cores, List<Group> rims, String molecName, String suffix) {
		String cmd = "select core"+suffix+", "+molecName+" and resi "+getResiSelString(cores);
		writeCommand(cmd, pml);
		cmd = "select rim"+suffix+", "+molecName+" and resi "+getResiSelString(rims);
		writeCommand(cmd, pml);
	}
	
	private String getPymolMolecName(File pdbFile) {
		String fileName = pdbFile.getName();
		if (fileName.endsWith(".gz")) {
			fileName=fileName.substring(0,fileName.lastIndexOf('.'));
		}
		return fileName.substring(0, fileName.lastIndexOf('.'));
	}
	
	private String getResiSelString(List<Group> list) {
		if (list.isEmpty()) return "none"; // the keyword for an empty selection in pymol
		StringBuffer sb = new StringBuffer();
		int lastSerial = -9999;
		// we write selection ranges in the style: 5-14+18+23-34 (to make shorter strings, pymol has issues with very long strings)
		StringBuilder cs = new StringBuilder();		
		for (int i=0;i<list.size();i++) {
		
			Chain c = list.get(i).getChain();
			
			String pdbSerial = list.get(i).getResidueNumber().toString();
			// we need to escape the negative residues in pymol with a backslash
			if (pdbSerial.startsWith("-")) pdbSerial = "\\"+pdbSerial;
			
			int currentSerial = DataModelAdaptor.getSeqresSerial(list.get(i), c);
			int prevSerial = -1;
			if (i>0) prevSerial = DataModelAdaptor.getSeqresSerial(list.get(i-1),c);
			
			if (i==0) {
				lastSerial =  currentSerial;
				cs.append(pdbSerial);
			}
			if ((i > 0) && (currentSerial-prevSerial != 1)) {
				if (lastSerial == prevSerial){
					cs.append("+");
					cs.append(pdbSerial);
					lastSerial = currentSerial;
				} else {
					cs.append("-");
					String cs2e = list.get(i-1).getResidueNumber().toString();
					if (cs2e.startsWith("-")) cs2e = "\\"+cs2e;
					cs.append(cs2e);
					lastSerial = currentSerial;
					cs.append("+");
					cs.append(pdbSerial);
				}
			}
			
			sb.append(pdbSerial);

			if (i!=list.size()-1) sb.append("+");
			
		}

		int strlen=cs.length();
		int maxlen=800; //greater than maxlen will be split with "or resi" in the middle to avoid pymol buffer overflow
		if (strlen>maxlen){
			int m=strlen%maxlen;
			for (int i=0;i<m;i++){
				int startidx=cs.indexOf("+", maxlen*(i+1)); 
				if (startidx>0) cs.replace(startidx, startidx+1, " or resi " );//to fix pymol buffer overflow error
				
			}
		}
		return cs.toString(); // to write pymol selection 3-6+11+15-17 or resi 34-45,47,78
	}

	private String getSelString(String namePrefix, char chainName, List<Group> list) {
		return "select "+namePrefix+chainName+", chain "+chainName+" and ( resi "+getResiSelString(list)+")";
	}

	private void writeCommand(String cmd, PrintStream ps) {
		//if (sb!=null) {
		//	sb.append(cmd+";");
		//}
		if (ps!=null) {
			ps.println(cmd);
		}
		
	}
	
	/**
	 * Reads from properties file the chain colors: 26 colors, one per alphabet letter
	 * and a color for the sym related chain
	 * @param is
	 * @throws IOException
	 */
	public void readColorsFromPropertiesFile(InputStream is) throws IOException {
		
		Properties p = new Properties();
		p.load(is);

		chainColors = new String[26];
		char letter = 'A';
		for (int i=0;i<26;i++) {
			chainColors[i] = p.getProperty(Character.toString(letter)); 
			letter++;
		}
		symRelatedColor = p.getProperty("SYMCHAIN");
		interf1color = p.getProperty("INTERF1");
		interf2color = p.getProperty("INTERF2");
	}	
	
	/**
	 * Reads from resource file the color mappings of pymol color names to hex RGB color codes
	 * @param is
	 * @throws IOException
	 */
	public void readColorMappingsFromResourceFile(InputStream is) throws IOException {
		colorMappings = new HashMap<String, String>();
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String line;
		while ((line=br.readLine())!=null) {
			if (line.isEmpty()) continue;
			if (line.startsWith("#")) continue;
			String[] tokens = line.split(" ");
			colorMappings.put(tokens[0], tokens[1]);
		}
		br.close();
	}
	
	/**
	 * Given a pymol color name (e.g. "raspberry") returns the hex RGB color code, e.g. #b24c66
	 * or null if no such color name exists.
	 * @param pymolColor
	 * @return
	 */
	public String getHexColorCode(String pymolColor) {
		return colorMappings.get(pymolColor);
	}

	public String getInterf1Color() {
		return interf1color;
	}
	
	public String getInterf2Color() {
		return interf2color;
	}
}