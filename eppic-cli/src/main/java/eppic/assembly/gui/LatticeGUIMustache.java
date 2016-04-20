package eppic.assembly.gui;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.StructureException;
import org.biojava.nbio.structure.StructureTools;
import org.biojava.nbio.structure.contact.StructureInterface;
import org.biojava.nbio.structure.io.util.FileDownloadUtils;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.Pseudograph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

import eppic.assembly.ChainVertex3D;
import eppic.assembly.InterfaceEdge3D;
import eppic.assembly.LatticeGraph3D;
import eppic.assembly.OrientedCircle;
import eppic.assembly.layout.StereographicLayout;

/**
 * Create viewers for LatticeGraph based on the Mustache template system. This
 * includes javascript-based 2D and 3D views, such as 3Dmol, vis.js, etc.
 * 
 * @author blivens
 *
 */
public class LatticeGUIMustache {
	private static final Logger logger = LoggerFactory.getLogger(LatticeGUIMustache.class);

	private final LatticeGraph3D latticeGraph;
	private final String template;

	private String pdbId; // Defaults to the structure's PDB ID, if available
	private String title; // Title for HTML page
	private String size; // Target size for content
	
	// cache for getGraph2D
	private UndirectedGraph<ChainVertex3D, InterfaceEdge3DSourced<ChainVertex3D>> graph2d = null;

	private StereographicLayout<ChainVertex3D,InterfaceEdge3D> layout2d = null;

	/**
	 * Factory method for known templates. Most templates use this class directly.
	 * A few known methods will use special subclasses (eg 3Dmol)
	 * 
	 * The template file can be given as a path to the mustache template, which
	 * can be either a full path or a short name within the eppic.assembly.gui
	 * resource directory. For instance, '3Dmol', 'LatticeGUI3Dmol.mustache.html'
	 * and 'eppic-cli/src/main/resources/mustache/eppic/assembly/gui/LatticeGUI3Dmol.mustache.html'
	 * should all locate the correct template.
	 * @param template String giving the path to the template.
	 * @return
	 * @throws StructureException 
	 */
	public static LatticeGUIMustache createLatticeGUIMustache(String template,Structure struc,Collection<Integer> interfaceIds) throws StructureException {
		template = expandTemplatePath(template);
		
		File templateFile = new File(template);
		if( templateFile.getName().equalsIgnoreCase("LatticeGUI3Dmol") ) {
			LatticeGUI3Dmol gui = new LatticeGUI3Dmol(struc, null, interfaceIds);
			//TODO work out how to set this
			gui.setStrucURI(struc.getIdentifier()+".cif");
			return gui;
		}
		return new LatticeGUIMustache(template, struc, interfaceIds);
	}
	/**
	 * 
	 * @param template
	 * @return
	 * @throws IllegalArgumentException if the template couldn't be found or was ambiguous
	 */
	private static String expandTemplatePath(String template) {
		// Try loading template directly
		URL url = LatticeGUIMustache.class.getResource(template);
		if( url != null) {
			return template;
		}
		// Assume that it refers to something in our template directory
		URL knownTemplate = LatticeGUIMustache.class.getResource(LatticeGUI3Dmol.MUSTACHE_TEMPLATE_3DMOL);
		if( knownTemplate == null ) {
			throw new IllegalStateException("Unable to find template resource. Error building jar file?");
		}
		File mustacheDir = new File(knownTemplate.getPath()).getParentFile();
		
		// Try template as file's basename
		File attempt = new File(mustacheDir,template);
		if( attempt.exists())
			return attempt.getAbsolutePath();
		
		// See if we have a single unique shortname
		String glob = String.format("{LatticeGUI[%s%s]%s,%s}.mustache.*",
				template.substring(0, 1).toLowerCase(),
				template.substring(0, 1).toUpperCase(),
				template.substring(1),
				template );
		try( DirectoryStream<Path> mustacheDirStream = Files.newDirectoryStream(mustacheDir.toPath(), glob) ) {
			Iterator<Path> it = mustacheDirStream.iterator();
			if(it.hasNext()) {
				Path first = it.next();
				if(it.hasNext()) {
					// not unique
					throw new IllegalArgumentException("Multiple templates match "+template+", starting with "+first+" and "+it.next());
				}
				return first.toAbsolutePath().toString();
			}
			// no matches
		} catch (IOException e) {}
		throw new IllegalArgumentException("No matching template found for "+template);
	}
	
	
	/**
	 * @param template Path to the template file. Short names are supported for templates in the MUSTACHE_TEMPLATE_DIR
	 * @param struc Structure used to create the graph (must have cell info)
	 * @param interfaceIds List of interface numbers, or null for all interfaces
	 * @throws StructureException For errors parsing the structure
	 */
	public LatticeGUIMustache(String template, Structure struc,Collection<Integer> interfaceIds) throws StructureException {
		this(template,struc,interfaceIds,null);
	}
	/**
	 * 
	 * @param template
	 * @param struc
	 * @param interfaceIds
	 * @param allInterfaces (Optional) List of interfaces for this structure.
	 *  If not null it avoids recalculating the full list.
	 * @throws StructureException
	 */
	public LatticeGUIMustache(String template, Structure struc,Collection<Integer> interfaceIds,List<StructureInterface> allInterfaces) throws StructureException {
		this.latticeGraph = new LatticeGraph3D(struc,allInterfaces);
		this.template = template;
		
		UndirectedGraph<ChainVertex3D, InterfaceEdge3D> graph = latticeGraph.getGraph();
		if( interfaceIds != null ) {
			logger.info("Filtering LatticeGraph3D to edges {}",interfaceIds);
			latticeGraph.filterEngagedInterfaces(interfaceIds);
		}
		logger.info("Using LatticeGraph3D with {} vertices and {} edges",graph.vertexSet().size(),graph.edgeSet().size());

		// Compute names and colors
		for(ChainVertex3D v : graph.vertexSet()) {
			v.setColorStr(toHTMLColor(v.getColor()));
		}

		for(InterfaceEdge3D e : graph.edgeSet()) {
			String colorStr = toHTMLColor(e.getColor());
			e.setColorStr(colorStr);
			
			if(e.getCircles() != null) {
				for(OrientedCircle circ: e.getCircles()) {
					//rescale perpendicular vector
					final double thickness = .5;
					Vector3d ab = new Vector3d();
					ab.sub(circ.getPerpendicular(),circ.getCenter());
					double len = ab.length();
					Point3d newPerp = new Point3d(ab);
					newPerp.scaleAdd(thickness/len, circ.getCenter());
					circ.setPerpendicular(newPerp);
				}
			}

		}

		// Default parameters
		pdbId = struc.getStructureIdentifier().toCanonical().getPdbId();
		if(pdbId == null || pdbId.length() != 4) {
			pdbId = struc.getName();
		}
		if(pdbId == null || pdbId.length() != 4) {
			pdbId = null;
			logger.error("Unable to get PDB ID.");
		}

		this.title = String.format("Lattice for %s",getPdbId());
		this.size = "800";
	}

	/**
	 * hex verson of the color (e.g. '0xFF00CC')
	 * @param color
	 * @return
	 */
	private static String toHTMLColor(Color color) {
		if(color == null) return null;
		return String.format("#%2x%2x%2x", color.getRed(),color.getGreen(),color.getBlue());
	}


	/**
	 * Compile and execute the template
	 * 
	 * The page is constructed from a template file.
	 * @param out Output stream for the html commands
	 */
	public void execute(PrintWriter out) {
		MustacheFactory mf = new DefaultMustacheFactory();
		Mustache mustache = mf.compile(template);
		try {
			mustache.execute(out, this).flush();
		} catch (IOException e) {
			logger.error("Error generating output from template "+template,e);
		}
	}

	/**
	 * Get the LatticeGraph3D. This is primarily useful for accessing its
	 * getGraph method. For instance, to iterate over all vertices, use 
	 * <pre>{{#graph.graph.vertexSet}}ChainVertex3D vars, e.g.{{center.x}}{{/graph.graph.vertexSet}}</pre>
	 * @return
	 */
	public LatticeGraph3D getGraph() {
		return latticeGraph;
	}

	/**
	 * Get the PDB ID. By default, takes this from the structure identifier.
	 * @return
	 */
	public String getPdbId() {
		return pdbId;
	}
	public void setPdbId(String pdbId) {
		this.pdbId = pdbId;
	}

	/**
	 * @return The second and third characters of {@link #getPdbId()}
	 */
	public String getPdbIdMiddle2() {
		if(getPdbId() == null) {
			return null;
		}
		return getPdbId().substring(1, 3);
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getSize() {
		return size;
	}
	public void setSize(String size) {
		this.size = size;
	}

	/**
	 * Parses a comma-separated list of digits.
	 * returns null for '*', indicating all interfaces.
	 * @param list Input string
	 * @return list of interface ids, or null for all interfaces
	 * @throws NumberFormatException for invalid input
	 */
	public static List<Integer> parseInterfaceList(String list) throws NumberFormatException{
		// '*' for all interfaces
		if(list == null || list.isEmpty() || list.equals("*") ) {
			return null;// all interfaces
		}
		String[] splitIds = list.split("\\s*,\\s*");
		List<Integer> interfaceIds = new ArrayList<Integer>(splitIds.length);
		for(String idStr : splitIds) {
			interfaceIds.add(new Integer(idStr));
		}
		return interfaceIds;
	}


	/**
	 * Write a cif file containing the unit cell.
	 * @param out
	 * @throws IOException
	 * @throws StructureException
	 */
	public void writeCIFfile(PrintWriter out) throws IOException, StructureException {
		latticeGraph.writeCellToMmCifFile(out);
	}
	
	/**
	 * @return the current 2D layout
	 */
	public StereographicLayout<ChainVertex3D,InterfaceEdge3D> getLayout2D() {
		return layout2d;
	}
	/**
	 * @param layout2d the 2D layout to set
	 */
	public void setLayout2D(StereographicLayout<ChainVertex3D,InterfaceEdge3D> layout2d) {
		if(this.layout2d != null && !this.layout2d.equals(layout2d)) {
			// graph2d depends on the layout
			this.graph2d = null;
		}
		this.layout2d = layout2d;
		
	}

	/**
	 * Get a version of the graph where all 3D coordinates have z=0.
	 * This is achieved by performing a stereographic projection of the 3D
	 * coordinates.
	 * @param layout
	 * @return
	 */
	public UndirectedGraph<ChainVertex3D, InterfaceEdge3DSourced<ChainVertex3D>> getGraph2D() {
		if( graph2d == null) {
			if(layout2d == null) {
				throw new IllegalStateException("No 2D layout set for calculating the 2D graph");
			}
			UndirectedGraph<ChainVertex3D, InterfaceEdge3D> graph3d = getGraph().getGraph();
			UndirectedGraph<ChainVertex3D, InterfaceEdge3D> graph2dUnsorced = cloneGraph3D(graph3d);
			layout2d.projectLatticeGraph(graph2dUnsorced);
			graph2d = InterfaceEdge3DSourced.addSources(graph2dUnsorced);
		}
		return graph2d;
	}

	private static UndirectedGraph<ChainVertex3D, InterfaceEdge3D> cloneGraph3D(
			UndirectedGraph<ChainVertex3D, InterfaceEdge3D> oldGraph) {
		// Mappings from old graph to new
		Map<ChainVertex3D,ChainVertex3D> newVertices = new HashMap<>(oldGraph.vertexSet().size());
		Map<InterfaceEdge3D,InterfaceEdge3D> newEdges = new HashMap<>(oldGraph.edgeSet().size());
		for(ChainVertex3D vert : oldGraph.vertexSet()) {
			ChainVertex3D newVert = new ChainVertex3D(vert);
			newVertices.put(vert,newVert);
		}
		for(InterfaceEdge3D edge :oldGraph.edgeSet()) {
			InterfaceEdge3D newEdge = new InterfaceEdge3D(edge);
			newEdges.put(edge, newEdge);
		}
		// convert old graph to new one
		UndirectedGraph<ChainVertex3D,InterfaceEdge3D> newGraph = new Pseudograph<>(InterfaceEdge3D.class);
		for(ChainVertex3D vert : newVertices.values()) {
			newGraph.addVertex(vert);
		}
		for(InterfaceEdge3D edge : oldGraph.edgeSet()) {
			ChainVertex3D source = newVertices.get( oldGraph.getEdgeSource(edge) );
			ChainVertex3D target = newVertices.get( oldGraph.getEdgeTarget(edge) );
			InterfaceEdge3D newEdge = newEdges.get( edge );
			newGraph.addEdge(source, target, newEdge);
		}
		return newGraph;
	}
	public static void main(String[] args) throws IOException, StructureException {
		final String usage = String.format("Usage: %s template structure output [interfacelist]",LatticeGUIMustache.class.getSimpleName());
		if (args.length<2) {
			logger.error("Expected at least 3 arguments");
			logger.error(usage);
			System.exit(1);return;
		}

		// Parse arguments
		int arg = 0;
		String template = args[arg++];

		String input = args[arg++];

		String output = args[arg++];
		PrintWriter mainOut;
		if(output.equals("-")) {
			mainOut = new PrintWriter(System.out);
		} else {
			mainOut = new PrintWriter(FileDownloadUtils.expandUserHome(output));
		}

		Collection<Integer> interfaceIds = null;
		if (args.length>arg) {
			String interfaceIdsCommaSep = args[arg++];
			try {
				interfaceIds = parseInterfaceList(interfaceIdsCommaSep);
			} catch( NumberFormatException e) {
				logger.error("Invalid interface IDs. Expected comma-separated list, got {}",interfaceIdsCommaSep);
				System.exit(1);return;
			}
		}

		if (args.length>arg) {
			logger.error("Expected at most {} arguments.",arg);
			logger.error(usage);
			System.exit(1);return;
		}
		// Done parsing

		// Load input structure
		Structure struc = StructureTools.getStructure(input);

		LatticeGUIMustache gui;
		try {
			gui = createLatticeGUIMustache(template, struc, interfaceIds);
		} catch( IllegalArgumentException e) {
			System.err.println(e.getMessage());
			System.exit(1); return;
		}
		Point3d center = new Point3d();
		Point3d zenith = new Point3d(0,0,1);
		StereographicLayout<ChainVertex3D,InterfaceEdge3D> layout2d = new StereographicLayout<>(ChainVertex3D.getVertexPositioner(), center , zenith);
		gui.setLayout2D(layout2d );
		gui.execute(mainOut);

		if( !output.equals("-")) {
			mainOut.close();
		}
	}
	
}
