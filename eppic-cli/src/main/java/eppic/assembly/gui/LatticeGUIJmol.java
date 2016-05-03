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
import org.jgrapht.UndirectedGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

import eppic.assembly.ChainVertex3D;
import eppic.assembly.InterfaceEdge3D;
import eppic.assembly.LatticeGraph3D;
import eppic.assembly.OrientedCircle;
import eppic.assembly.ParametricCircularArc;
import eppic.commons.util.StructureUtils;

/**
 * Jmol viewer for LatticeGraph.
 * 
 * @author blivens
 *
 */
public class LatticeGUIJmol {
	private static final String MUSTACHE_TEMPLATE_JMOL = "mustache/eppic/assembly/gui/LatticeGUIJmol.mustache.jmol";

	private final double defaultArrowOffset = 6;

	private static Logger logger = LoggerFactory.getLogger(LatticeGUIJmol.class);

	private LatticeGraph3D latticeGraph;
	private File strucFile;

	/**
	 * Due to limitations in the way structures are passed to Jmol,
	 * Jmol needs to load the structure directly from strucFile.
	 * @param struc Structure to build the graph
	 * @param strucFile Path to the structure of the asymmetric unit.
	 * @param interfaceIds List of interfaces to show, or null for all
	 * @throws StructureException
	 */
	public LatticeGUIJmol(Structure struc, File strucFile) throws StructureException {
		this.latticeGraph = new LatticeGraph3D(struc);
		UndirectedGraph<ChainVertex3D, InterfaceEdge3D> graph = latticeGraph.getGraph();

		// Compute Jmol names and colors
		for(ChainVertex3D v : graph.vertexSet()) {
			v.setUniqueName(toUniqueJmolID("chain"+v.toString()));
			v.setColorStr(toJmolColor(v.getColor()));
		}

		for(InterfaceEdge3D e : graph.edgeSet()) {
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
					seg.shrinkAbsolute(defaultArrowOffset);
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

	// Set of previously used jmol ids
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
	 * Convert a color to a Jmol tuple, e.g. "[.5,.5,.5]" (grey)
	 * @param color
	 * @return
	 */
	private static String toJmolColor(Color color) {
		if(color == null) return null;
		return String.format("[%f,%f,%f]", color.getRed()/256f,color.getGreen()/256f,color.getBlue()/256f);
	}

	public static void main(String[] args) throws IOException, StructureException {

		if (args.length<1) {
			logger.error("Expected at least 1 argument.");
			logger.error("Usage: {} <PDB code or file> [comma separated list of interfaces to display]",LatticeGUIJmol.class.getSimpleName());
			System.exit(1);
		}

		// Parse arguments
		int arg = 0;
		String input = args[arg++];
		List<Integer> interfaceIds = null;

		if (args.length>=2) {
			String interfaceIdsCommaSep = args[arg++];
			// '*' for all interfaces
			if(!interfaceIdsCommaSep.equals("*")) {
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
		}

		if (args.length>2) {
			logger.error("Expected at most 3 arguments.");
			logger.error("Usage: {} <PDB code or file> <output.js> [comma separated list of interfaces to display]",LatticeGUIJmol.class.getSimpleName());
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
			cache.getFileParsingParams().setAlignSeqRes(true);
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
		
		StructureUtils.expandNcsOps(struc);

		LatticeGUIJmol gui = new LatticeGUIJmol(struc, file);
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

	/**
	 * Show Jmol window
	 * @return
	 */
	public BiojavaJmol display() {
		BiojavaJmol jmol = new BiojavaJmol();
		jmol.setTitle(strucFile.getName());
		//jmol.setStructure(struc);

		jmol.evalString(getJmolCommands());

		jmol.getFrame().setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		return jmol;

	}

	/**
	 * Construct a jmol script to load the structure and display the graph
	 * 
	 * The script is generated from a template file.
	 * @return
	 */
	public String getJmolCommands() {
		MustacheFactory mf = new DefaultMustacheFactory();
		String template = MUSTACHE_TEMPLATE_JMOL;
		Mustache mustache = mf.compile(template);
		StringWriter out = new StringWriter();
		try {
			mustache.execute(new PrintWriter(out), this).flush();
		} catch (IOException e) {
			logger.error("Error generating jmol commands from template "+template,e);
		}
		return out.toString();
	}

	/**
	 * Get the underlying lattice graph datastructure
	 * @return
	 */
	public LatticeGraph3D getGraph() {
		return latticeGraph;
	}

	/**
	 * Used by Jmol to load the structure directly from the filesystem
	 * @return
	 */
	public File getStrucFile() {
		return strucFile;
	}

}
