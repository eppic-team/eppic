package eppic.assembly.gui;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JFrame;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import org.biojava.nbio.structure.Atom;
import org.biojava.nbio.structure.Calc;
import org.biojava.nbio.structure.Chain;
import org.biojava.nbio.structure.PDBCrystallographicInfo;
import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.StructureException;
import org.biojava.nbio.structure.StructureTools;
import org.biojava.nbio.structure.align.util.AtomCache;
import org.biojava.nbio.structure.contact.Pair;
import org.biojava.nbio.structure.contact.StructureInterfaceList;
import org.biojava.nbio.structure.gui.BiojavaJmol;
import org.biojava.nbio.structure.io.MMCIFFileReader;
import org.biojava.nbio.structure.io.PDBFileReader;
import org.biojava.nbio.structure.io.util.FileDownloadUtils;
import org.biojava.nbio.structure.xtal.CrystalBuilder;
import org.biojava.nbio.structure.xtal.CrystalCell;
import org.biojava.nbio.structure.xtal.CrystalTransform;
import org.jcolorbrewer.ColorBrewer;
import org.jgrapht.UndirectedGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

import eppic.assembly.ChainVertex;
import eppic.assembly.ChainVertex3D;
import eppic.assembly.InterfaceEdge;
import eppic.assembly.InterfaceEdge3D;
import eppic.assembly.LatticeGraph;
import eppic.assembly.LatticeGraph3D;
import eppic.assembly.OrientedCircle;
import eppic.assembly.ParametricCircularArc;

public class LatticeGUI3Dmol {
	private static Logger logger = LoggerFactory.getLogger(LatticeGUI3Dmol.class);
	private LatticeGraph3D graph;
	private File strucFile;
	private String pdbId; //TODO use file instead
	
	public LatticeGUI3Dmol(Structure struc, File strucFile,List<Integer> interfaceIds) throws StructureException {
		this.graph = new LatticeGraph3D(struc);
		if( interfaceIds != null ) {
			//TODO
			//gui = rawGui.restrict(interfaceIds);
		}
		
		// Compute Jmol names and colors
		for(ChainVertex3D v : graph.getVertices()) {
			v.setUniqueName(toUniqueJmolID("chain"+v.toString()));
			v.setColorStr(toHTMLColor(v.getColor()));
		}
		
		for(InterfaceEdge3D e : graph.getEdges()) {
			ChainVertex3D source = graph.getEdgeSource(e);
			ChainVertex3D target = graph.getEdgeTarget(e);
			
			String edgeName = String.format("edge_%s_%s",source,target);
			e.setUniqueName(toUniqueJmolID(edgeName));
			
			String colorStr = toHTMLColor(e.getColor());
			e.setColorStr(colorStr);

			if(e.getSegments() != null) {
				int i=1;
				for(ParametricCircularArc seg : e.getSegments()) {
					seg.setUniqueName(toUniqueJmolID(edgeName+"_seg"+i));
					i++;
				}
			}
			if(e.getCircles() != null) {
				int i=1;
				for(OrientedCircle circ: e.getCircles()) {
					//rescale perpendicular vector
					final double thickness = .5;
					Vector3d ab = new Vector3d();
					ab.sub(circ.getPerpendicular(),circ.getCenter());
					double len = ab.length();
					Point3d newPerp = new Point3d(ab);
					newPerp.scaleAdd(thickness/len, circ.getCenter());
					circ.setPerpendicular(newPerp);
					
					circ.setUniqueName(toUniqueJmolID(edgeName+"_circle"+i));
					i++;
				}
			}
		}
		this.strucFile = strucFile;
		//TODO support custom files
		pdbId = struc.getPdbId();
		if(pdbId == null || pdbId.length() != 4) {
			pdbId = struc.getName();
		}
		if(pdbId == null || pdbId.length() != 4) {
			logger.error("Unable to get PDB ID.");
		}
	}
	
	private Set<String> jmolIDs = new HashSet<String>();
	/**
	 * Insures that a Jmol identifier is unique. If an identifier has been used
	 * previously, appends an integer sequentially until it is unique
	 * @param name Seed identifier
	 * @return a unique string starting with name
	 */
	private String toUniqueJmolID(String name) {
		if(name == null) return null;
		String uid = name;
		int i = 0;
		synchronized(jmolIDs) {
			while(jmolIDs.contains(uid)) {
				uid = String.format("%s #%d",name,i);
				i++;
			}
			jmolIDs.add(uid);
		}
		return uid;
	}
	
	/**
	 * hex verson of the color (e.g. '0xFF00CC')
	 * @param color
	 * @return
	 */
	private static String toHTMLColor(Color color) {
		if(color == null) return null;
		return String.format("0x%2x%2x%2x", color.getRed(),color.getGreen(),color.getBlue());
	}

	public static void main(String[] args) throws IOException, StructureException {
		final String usage = String.format("Usage: %s <PDB code or file> <output file> [comma separated list of interfaces to display]",LatticeGUIJmol.class.getSimpleName());
		if (args.length<1) {
			logger.error("Expected at least 2 arguments");
			logger.error(usage);
			System.exit(1);
		}
		
		// Parse arguments
		int arg = 0;
		String input = args[arg++];
		List<Integer> interfaceIds = null;
		
		String output = args[arg++];
		PrintWriter out;
		if(output.equals("-")) {
			out = new PrintWriter(System.out);
		} else {
			out = new PrintWriter(FileDownloadUtils.expandUserHome(output));
		}
		
		if (args.length>arg) {
			String interfaceIdsCommaSep = args[arg++];
			String[] splitIds = interfaceIdsCommaSep.split("\\w*,\\w*");
			interfaceIds = new ArrayList<Integer>(splitIds.length);
			for(String idStr : splitIds) {
				try {
					interfaceIds.add(new Integer(idStr));
				} catch( NumberFormatException e) {
					logger.error("Invalid interface IDs. Expected comma-separated list, got {}",interfaceIdsCommaSep);
					System.exit(1);
				}
			}
		}

		if (args.length>arg) {
			logger.error("Expected at most {} arguments.",arg);
			logger.error(usage);
			System.exit(1);
		}
		// Done parsing

		// Load input structure
		Structure struc;
		
		File file = new File(FileDownloadUtils.expandUserHome(input)); // try as filename
		if (file.exists()) {
			struc = StructureTools.getStructure(file.getAbsolutePath());
		} else if (input.matches("\\d\\w\\w\\w")){ // try as PDB id
			AtomCache cache = new AtomCache();
			cache.setUseMmCif(true);
			struc = cache.getStructure(input);
			file = getFile(cache,input);
			if(!file.exists() ) {
				logger.error(String.format("Error loading {} from {}",input,file.getAbsolutePath()));
				System.exit(1);
			}
		} else { // give up
			file = null;
			struc = null;
			logger.error("Unable to read structure or file {}",input);
			System.exit(1);
		}
		
		LatticeGUI3Dmol gui = new LatticeGUI3Dmol(struc, file, interfaceIds);
		
		gui.get3DmolCommands(out);
		
		if( !output.equals("-")) {
			out.close();
		}
	}
	

	/**
	 * Tries to guess the file location from an AtomCache
	 * @param cache
	 * @param name
	 * @return
	 */
	private static File getFile(AtomCache cache, String name) {
		if(cache.isUseMmCif()) {
			MMCIFFileReader reader = new MMCIFFileReader(cache.getPath());
			reader.setFetchBehavior(cache.getFetchBehavior());
			reader.setObsoleteBehavior(cache.getObsoleteBehavior());

			File file = reader.getLocalFile(name);
			return file;
		} else {
			PDBFileReader reader = new PDBFileReader(cache.getPath());
			reader.setFetchBehavior(cache.getFetchBehavior());
			reader.setObsoleteBehavior(cache.getObsoleteBehavior());

			reader.setFileParsingParameters(cache.getFileParsingParams());

			File file = reader.getLocalFile(name); 

			return file;
		}
	}
	
	
	public void get3DmolCommands(PrintWriter out) {
		MustacheFactory mf = new DefaultMustacheFactory();
		String template = "mustache/eppic/assembly/gui/LatticeGUI3Dmol.mustache.html";
		Mustache mustache = mf.compile(template);
		try {
			mustache.execute(out, this).flush();
		} catch (IOException e) {
			logger.error("Error generating 3Dmol commands from template "+template,e);
		}
	}

	public LatticeGraph3D getGraph() {
		return graph;
	}

	public File getStrucFile() {
		return strucFile;
	}
	public String getPdbId() {
		return pdbId;
	}

}
