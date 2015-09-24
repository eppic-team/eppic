package eppic.assembly.gui;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JFrame;

import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.StructureException;
import org.biojava.nbio.structure.StructureTools;
import org.biojava.nbio.structure.align.util.AtomCache;
import org.biojava.nbio.structure.gui.BiojavaJmol;
import org.biojava.nbio.structure.io.MMCIFFileReader;
import org.biojava.nbio.structure.io.PDBFileReader;
import org.biojava.nbio.structure.io.util.FileDownloadUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.github.mustachejava.TemplateFunction;
import com.google.common.base.Function;

import eppic.assembly.ChainVertex3D;
import eppic.assembly.InterfaceEdge3D;
import eppic.assembly.LatticeGraph3D;
import eppic.assembly.OrientedCircle;
import eppic.assembly.ParametricCircularArc;


public class LatticeGUIJmol {
	private static Logger logger = LoggerFactory.getLogger(LatticeGUIJmol.class);
	
	private LatticeGraph3D graph;
	private File strucFile;
	
	public LatticeGUIJmol(Structure struc, File strucFile,List<Integer> interfaceIds) throws StructureException {
		this.graph = new LatticeGraph3D(struc);
		if( interfaceIds != null ) {
			//TODO
			//gui = rawGui.restrict(interfaceIds);
		}
		
		// Compute Jmol names and colors
		for(ChainVertex3D v : graph.getVertices()) {
			v.setUniqueName(toUniqueJmolID("chain"+v.toString()));
			v.setColorStr(toJmolColor(v.getColor()));
		}
		
		for(InterfaceEdge3D e : graph.getEdges()) {
			ChainVertex3D source = graph.getEdgeSource(e);
			ChainVertex3D target = graph.getEdgeTarget(e);
			
			String edgeName = String.format("edge_%s_%s",source,target);
			e.setUniqueName(toUniqueJmolID(edgeName));
			
			String colorStr = toJmolColor(e.getColor());
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
					circ.setUniqueName(toUniqueJmolID(edgeName+"_circle"+i));
					i++;
				}
			}
		}
		this.strucFile = strucFile;
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
	
	private static String toJmolColor(Color color) {
		if(color == null) return null;
		return String.format("[%f,%f,%f]", color.getRed()/256f,color.getGreen()/256f,color.getBlue()/256f);
	}

	public static void main(String[] args) throws IOException, StructureException {
		
		if (args.length<1) {
			logger.error("Expected at least 1 argument.");
			logger.error("Usage: %s <PDB code or file> [comma separated list of interfaces to display]",LatticeGUIJmol.class.getSimpleName());
			System.exit(1);
		}
		
		// Parse arguments
		int arg = 0;
		String input = args[arg++];
		List<Integer> interfaceIds = null;
		
		if (args.length>=2) {
			String interfaceIdsCommaSep = args[arg++];
			String[] splitIds = interfaceIdsCommaSep.split("\\s*,\\s*");
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

		if (args.length>2) {
			logger.error("Expected at most 3 arguments.");
			logger.error("Usage: %s <PDB code or file> <output.js> [comma separated list of interfaces to display]",LatticeGUIJmol.class.getSimpleName());
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
		
		LatticeGUIJmol gui = new LatticeGUIJmol(struc, file, interfaceIds);
		if(interfaceIds != null) {
			gui.getGraph().filterEngagedInterfaces(interfaceIds);
		}

		System.out.println(gui.getJmolCommands());
		gui.display();
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
			reader.setFetchBehavior(cache	.getFetchBehavior());
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
	
	// Show Jmol window
	public BiojavaJmol display() {
		BiojavaJmol jmol = new BiojavaJmol();
		jmol.setTitle(strucFile.getName());
		//jmol.setStructure(struc);
		
		jmol.evalString(getJmolCommands());

		jmol.getFrame().setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		return jmol;

	}
	
	public String getJmolCommands() {
		MustacheFactory mf = new DefaultMustacheFactory();
		String template = "mustache/eppic/assembly/gui/LatticeGUIJmol.mustache.jmol";
		Mustache mustache = mf.compile(template);
		StringWriter out = new StringWriter();
		try {
			mustache.execute(new PrintWriter(out), this).flush();
		} catch (IOException e) {
			logger.error("Error generating jmol commands from template "+template,e);
		}
		return out.toString();
	}

	public LatticeGraph3D getGraph() {
		return graph;
	}

	public File getStrucFile() {
		return strucFile;
	}
	
}
