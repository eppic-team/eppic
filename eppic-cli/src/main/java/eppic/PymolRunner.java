package eppic;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import eppic.commons.util.StreamGobbler;
import org.biojava.nbio.structure.Chain;
import org.biojava.nbio.structure.Group;
import org.biojava.nbio.structure.contact.StructureInterface;
import org.biojava.nbio.structure.contact.StructureInterfaceList;
import org.jgrapht.UndirectedGraph;

import eppic.assembly.Assembly;
import eppic.assembly.ChainVertex;
import eppic.assembly.InterfaceEdge;
import eppic.assembly.SubAssembly;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class PymolRunner {

	private static final Logger logger = LoggerFactory.getLogger(PymolRunner.class);
	
	private static final String DEF_TN_STYLE = "ribbon";
	private static final String DEF_TN_BG_COLOR = "white";
	private static final int[] DEF_TN_HEIGHTS = {EppicParams.THUMBNAILS_SIZE};
	private static final int[] DEF_TN_WIDTHS = {EppicParams.THUMBNAILS_SIZE};

	private static final double MIN_INTERF_AREA_TO_DISPLAY = 400;
	
	private static final String[] DEF_CHAIN_COLORS_ASU_ALLINTERFACES = 
		{"green", "tv_green", "chartreuse", "splitpea", "smudge", "palegreen", "limegreen", "lime", "limon", "forest"};
	
	/**
	 * Pymol can't handle very long scripts, it breaks at about 32KB, see https://github.com/eppic-team/eppic/issues/210
	 */
	private static final int MAX_LENGTH_PYMOL_SCRIPT = 32600;

	
	private File pymolExec;
	
	public PymolRunner(File pymolExec) {
		this.pymolExec = pymolExec;
	}
	
	/**
	 * Generates png file, pymol pse file and pml script for given interface producing a 
	 * mixed cartoon/surface representation of interface with selections 
	 * coloring each chain with a color as set in {@link #setColors(String[], String)} and 
	 * through {@link #readColorsFromPropertiesFile(InputStream)}
	 * NOTE that multi-chain letters only work from PyMOL 1.7.4+
	 * @param interf the interface
	 * @param mmcifFile the input cif or cif.gz file
	 * @param outDir the output dir for png files
	 * @param base the basename of the output files
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
	public void generateInterfacePng(StructureInterface interf, File mmcifFile, File outDir, String base)
	throws IOException, InterruptedException {
		
		String molecName = getPymolMolecName(mmcifFile);

		File[] pngFiles = new File[DEF_TN_HEIGHTS.length];
		for (int i=0;i<DEF_TN_HEIGHTS.length;i++) {
			pngFiles[i] = new File(outDir,base+"."+DEF_TN_WIDTHS[i]+"x"+DEF_TN_HEIGHTS[i]+".png");
		}
		
		String chain1 = interf.getMoleculeIds().getFirst();
		String chain2 = interf.getMoleculeIds().getSecond();
		
		if (chain1.equals(chain2)) {
			// this is as done in StructureInterface.toMMCIF()
			chain2 = chain2+"_"+interf.getTransforms().getSecond().getTransformId();
		}
		
		String color1 = MolViewersHelper.getHexChainColor(chain1, false);
		String color2 = MolViewersHelper.getHexChainColor(chain2, false);
		
		List<String> command = new ArrayList<String>();
		command.add(pymolExec.getAbsolutePath());
		command.add("-q");
		command.add("-c");

		StringBuilder pymolScriptBuilder = new StringBuilder();
		
		pymolScriptBuilder.append("load "+mmcifFile.getAbsolutePath()+";");
				
		pymolScriptBuilder.append("orient;");
		
		pymolScriptBuilder.append("remove solvent;");
		
		// and now creating the png thumbnail
		pymolScriptBuilder.append("bg "+DEF_TN_BG_COLOR+";");
		
		pymolScriptBuilder.append("as "+DEF_TN_STYLE+";");
		
		if(DEF_TN_STYLE.equalsIgnoreCase("ribbon")) {
			pymolScriptBuilder.append("set ribbon_radius, 2;");
			pymolScriptBuilder.append("set dash_length, 100;");
			pymolScriptBuilder.append("set dash_radius, 2;");
			pymolScriptBuilder.append("set ray_trace_gain, 0;");
			pymolScriptBuilder.append("set ray_trace_disco_factor, 1;");
		}
		
		// TODO do we need to check something before issuing the cofactors command???
		//if (interf.hasCofactors()) {
		pymolScriptBuilder.append("show sticks, org;");
		//}
		
		pymolScriptBuilder.append("color "+color1+", "+molecName+" and chain "+chain1+";");
		pymolScriptBuilder.append("color "+color2+", "+molecName+" and chain "+chain2+";");
		
		pymolScriptBuilder.append("set ray_opaque_background, off;");
		
		for (int i=0;i<DEF_TN_HEIGHTS.length;i++) {
			if(DEF_TN_HEIGHTS[i]>=200 && DEF_TN_WIDTHS[i]>=200) {
				pymolScriptBuilder.append("set ray_trace_mode, 3;");
			} else {
				pymolScriptBuilder.append("set ray_trace_mode, 0;");
			}

			pymolScriptBuilder.append("viewport "+DEF_TN_HEIGHTS[i]+","+DEF_TN_WIDTHS[i] + ";");

			pymolScriptBuilder.append("ray;");
			
			pymolScriptBuilder.append("png "+pngFiles[i].getAbsolutePath() + ";");
		}

		pymolScriptBuilder.append("quit;");
		
		command.add("-d");

		command.add(pymolScriptBuilder.toString());

		// pymol breaks at about 32KB, see https://github.com/eppic-team/eppic/issues/210
		// It is unlikely that for the interface case, this will happen, but keeping it here in case.
		// For assemblies case it does happen a lot, there we switched to using a pml file.
		if (pymolScriptBuilder.length() > MAX_LENGTH_PYMOL_SCRIPT) {
			throw new IOException("Can't create "+base+" png file. Script is longer than "+MAX_LENGTH_PYMOL_SCRIPT+" bytes, PyMOL can't handle that. Script length is " + pymolScriptBuilder.length() + " bytes.");
		}

		logger.info("Running Pymol command: {}", command);
		
		Process pymolProcess = new ProcessBuilder(command).start();
		StringWriter pymolStderr = new StringWriter();
		StringWriter pymolStdout = new StringWriter();
		BufferedReader pymolStderrReader = new BufferedReader(new InputStreamReader(pymolProcess.getErrorStream()));
		BufferedReader pymolStdoutReader = new BufferedReader(new InputStreamReader(pymolProcess.getInputStream()));
		String line;
		while ((line = pymolStdoutReader.readLine()) != null) pymolStdout.write(line+"\n");
		while ((line = pymolStderrReader.readLine()) != null) pymolStderr.write(line+"\n");

		int exit = pymolProcess.waitFor();
		if (exit!=0) {
			throw new IOException("Pymol exited with error status "+exit +". Stdout is:\n"+pymolStdout.toString()+"\nStderr is:\n"+pymolStderr.toString());
		}
	}

	public void generateAssemblyPng(Assembly a, File mmcifFile, File outDir, String base) throws IOException, InterruptedException {

		String molecName = getPymolMolecName(mmcifFile);

		File[] pngFiles = new File[DEF_TN_HEIGHTS.length];
		for (int i=0;i<DEF_TN_HEIGHTS.length;i++) {
			pngFiles[i] = new File(outDir,base+"."+DEF_TN_WIDTHS[i]+"x"+DEF_TN_HEIGHTS[i]+".png");
		}

		List<String> chains = new ArrayList<String>();
		List<String> colors = new ArrayList<String>();

		for (SubAssembly sub : a.getAssemblyGraph().getSubAssemblies()) {
			//TODO we might need getFirstRelevantConnectedComponent(sto) instead, but we need the stoichiometry for that
			UndirectedGraph<ChainVertex, InterfaceEdge> g = sub.getConnectedGraph();

			// the same identifiers given in Assembly.writeToMmCifFile()
			for (ChainVertex v:g.vertexSet()) {

				String chain = v.getChainId()+"_"+v.getOpId();
				chains.add(chain);
				colors.add(MolViewersHelper.getHexChainColor(chain,false));
			}
		}

		List<String> command = new ArrayList<String>();
		command.add(pymolExec.getAbsolutePath());
		command.add("-q");
		command.add("-c");

		StringBuilder pymolScriptBuilder = new StringBuilder();

		pymolScriptBuilder.append("load "+mmcifFile.getAbsolutePath()+"\n");

		//pymolScriptBuilder.append("orient;");
		// Orient to Z axis, since coordinates are pre-transformed
		pymolScriptBuilder.append("set_view (1,0,0, 0,1,0, 0,0,1, 0,0,-1000, 0,0,0, 500,1500, -20)\n");
		pymolScriptBuilder.append("zoom all, complete=1, buffer=5\n");

		pymolScriptBuilder.append("remove solvent;");

		pymolScriptBuilder.append("bg "+DEF_TN_BG_COLOR+";");

		pymolScriptBuilder.append("as "+DEF_TN_STYLE+";");

		if(DEF_TN_STYLE.equalsIgnoreCase("ribbon")) {
			pymolScriptBuilder.append("set ribbon_radius, 2\n");
			pymolScriptBuilder.append("set dash_length, 100\n");
			pymolScriptBuilder.append("set dash_radius, 2\n");
			pymolScriptBuilder.append("set ray_trace_gain, 0\n");
			pymolScriptBuilder.append("set ray_trace_disco_factor, 1\n");
		}


		for (int i=0;i<chains.size();i++) {
			// note that pymol script lines can be separated by ';' or '\n'. But for large scripts ';' separation breaks pymol, so we really need '\n' here
			pymolScriptBuilder.append("color ").append(colors.get(i)).append(", ").append(molecName).append(" and chain ").append(chains.get(i)).append("\n");
		}

		pymolScriptBuilder.append("set ray_opaque_background, off\n");

		for (int j=0;j<DEF_TN_HEIGHTS.length;j++) {
			if(DEF_TN_HEIGHTS[j]>=200 && DEF_TN_WIDTHS[j]>=200) {
				pymolScriptBuilder.append("set ray_trace_mode, 3\n");
			} else {
				pymolScriptBuilder.append("set ray_trace_mode, 0\n");
			}
			pymolScriptBuilder.append("viewport "+DEF_TN_HEIGHTS[j]+","+DEF_TN_WIDTHS[j] + "\n");

			pymolScriptBuilder.append("ray\n");

			pymolScriptBuilder.append("png "+pngFiles[j].getAbsolutePath() + "\n");
		}

		File pmlFile = File.createTempFile("eppicAssemblyPng", ".pml");
		pmlFile.deleteOnExit();

		// NOTE we used to pass all commands in one string after -d (with the pymolScriptBuilder StringBuilder.
		//      but that can cause problems for very long scripts (e.g. viral capsid).
		//     Because of that we write to pml file and then load the pmls with pymol "@" command
		// See issue https://github.com/eppic-team/eppic/issues/210

		PrintStream pml = new PrintStream(pmlFile);
		pml.println(pymolScriptBuilder.toString());

		command.add("-d");

		command.add("@" + pmlFile.toString() + "; quit;");

		logger.info("Running Pymol command: {}", command);

		Process pymolProcess = new ProcessBuilder(command).start();
		// important: for large scripts with a lot of output the Process stderr/out streams don't play well with the
		// pymol executable. This makes sure that the stdout/err is gobbled up and that Process doesn't hang forever
		StreamGobbler s1 = new StreamGobbler ("stdout", pymolProcess.getInputStream ());
		StreamGobbler s2 = new StreamGobbler ("stderr", pymolProcess.getErrorStream ());
		s1.start();
		s2.start();
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

		StringBuilder pymolScriptBuilder = new StringBuilder();
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
			String color = MolViewersHelper.getChainColor(i);
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
			
			char chain1 = interf.getMoleculeIds().getFirst().charAt(0); // TODO this won't work for new mega files, we need to write mmCIF!
			char chain2 = interf.getMoleculeIds().getSecond().charAt(0);
			
			// this relies on pdb files having been produced with the same chain-renaming scheme
			if (chain1==chain2) {
				chain2 = MolViewersHelper.getNextLetter(chain1);
			}
			
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
		
		pymolScriptBuilder.append("set pse_export_version, 1.5");
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
	 * @param mmcifFile
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
			File mmcifFile, File pseFile, File pmlFile,
			File iconPngFile, int iconWidth, int iconHeight,
			double minScore, double maxScore) 
	throws IOException, InterruptedException {
		
		String molecName = getPymolMolecName(mmcifFile);

		//char chain1 = chain.getPdbChainCode().charAt(0);
		
		List<String> command = new ArrayList<String>();
		command.add(pymolExec.getAbsolutePath());
		command.add("-q");
		command.add("-c");


		// NOTE we used to pass all commands in one string after -d (with the pymolScriptBuilder StringBuilder.
		//      But pymol 1.3 and 1.4 seem to have problems with very long strings (causing segfaults)
		//      Because of that now we write most commands to pml file (which we were doing anyway so that users can 
		//      use the pml scripts if they want) and then load the pmls with pymol "@" command
		
		
		StringBuilder pymolScriptBuilder = new StringBuilder();
		PrintStream pml = new PrintStream(pmlFile);
		
		pymolScriptBuilder.append("load "+mmcifFile.getAbsolutePath()+";");
				
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
			if (interf.getMoleculeIds().getFirst().equals(chain.getName())) {
				cores = interf.getCoreResidues(caCutoffGeom, minAsaForSurface).getFirst();
				rims = interf.getRimResidues(caCutoffGeom, minAsaForSurface).getFirst();
				
			} else if (interf.getMoleculeIds().getSecond().equals(chain.getName())) {
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


			if (interf.getMoleculeIds().getFirst().equals(chain.getName())) {
				cores = interf.getCoreResidues(caCutoffCoreSurf, minAsaForSurface).getFirst();
				rims = interf.getRimResidues(caCutoffCoreSurf, minAsaForSurface).getFirst();

				
			} else if (interf.getMoleculeIds().getSecond().equals(chain.getName())) {
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
		
		// set pse_export_version is only supported from pymol 1.7.6 
		// in older pymols it produces an error and makes the rest of the script fail
		// Commenting it out for now, ideally we should detect the version and then
		// use it if version is good - JD 2015-12-22
		//pymolScriptBuilder.append("set pse_export_version, 1.5;");
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
		StringBuilder sb = new StringBuilder();
		int lastSerial = -9999;
		// we write selection ranges in the style: 5-14+18+23-34 (to make shorter strings, pymol has issues with very long strings)
		StringBuilder cs = new StringBuilder();		
		for (int i=0;i<list.size();i++) {
		
			Chain c = list.get(i).getChain();
			
			String pdbSerial = list.get(i).getResidueNumber().toString();
			// we need to escape the negative residues in pymol with a backslash
			if (pdbSerial.startsWith("-")) pdbSerial = "\\"+pdbSerial;
			
			int currentSerial = c.getEntityInfo().getAlignedResIndex(list.get(i), c);
			int prevSerial = -1;
			if (i>0) prevSerial = c.getEntityInfo().getAlignedResIndex(list.get(i-1),c);
			
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

	@SuppressWarnings("unused")
	private String getSelString(String namePrefix, String chainName, List<Group> list) {
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
	
}