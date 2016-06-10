package eppic.assembly.gui;

import java.awt.Color;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.StructureException;
import org.biojava.nbio.structure.align.util.AtomCache;
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
import eppic.assembly.LatticeGraph;
import eppic.assembly.LatticeGraph3D;
import eppic.assembly.OrientedCircle;
import eppic.assembly.layout.ComboLayout;
import eppic.assembly.layout.ConnectedComponentLayout;
import eppic.assembly.layout.GraphLayout;
import eppic.assembly.layout.QuaternaryOrientationLayout;
import eppic.assembly.layout.UnitCellLayout;
import eppic.assembly.layout.VertexPositioner;
import eppic.commons.util.IntervalSet;

/**
 * Create viewers for LatticeGraph based on the Mustache template system. This
 * includes javascript-based 2D and 3D views, such as 3Dmol, vis.js, etc.
 * 
 * @author blivens
 *
 */
public class LatticeGUIMustache {
	private static final Logger logger = LoggerFactory.getLogger(LatticeGUIMustache.class);
	
	// Some pre-defined templates for use with createLatticeGUIMustache
	private static final String TEMPLATE_DIR = "mustache/eppic/assembly/gui/";
	public static final String TEMPLATE_ASSEMBLY_DIAGRAM_FULL = TEMPLATE_DIR+"AssemblyDiagramFull.html.mustache";
	public static final String TEMPLATE_ASSEMBLY_DIAGRAM_THUMB = TEMPLATE_DIR+"AssemblyDiagramThumb.dot.mustache";
	public static final String TEMPLATE_3DMOL = LatticeGUI3Dmol.MUSTACHE_TEMPLATE_3DMOL;//"LatticeGUI3Dmol";
	public static final String MUSTACHE_TEMPLATE_NGL = "mustache/eppic/assembly/gui/LatticeGUINgl.html.mustache";



	private final LatticeGraph3D latticeGraph;
	private final String template;

	private String pdbId; // Defaults to the structure's PDB ID, if available
	private String title; // Title for HTML page
	private String size; // Target size for content
	
	// cache for getGraph2D
	private UndirectedGraph<ChainVertex3D, InterfaceEdge3DSourced<ChainVertex3D>> graph2d = null;

	private GraphLayout<ChainVertex3D,InterfaceEdge3D> layout2d = null;

	/**
	 * Factory method for known templates. Most templates use this class directly.
	 * A few known methods will use special subclasses (eg 3Dmol)
	 * 
	 * The template file can be given as a path to the mustache template, which
	 * can be either a full path or a short name within the eppic.assembly.gui
	 * resource directory. For instance, '3Dmol', 'LatticeGUI3Dmol.html.mustache'
	 * and 'eppic-cli/src/main/resources/mustache/eppic/assembly/gui/LatticeGUI3Dmol.html.mustache'
	 * should all locate the correct template.
	 * @param template String giving the path to the template.
	 * @return
	 * @throws StructureException 
	 * @throws IllegalArgumentException if the template couldn't be found or was ambiguous
	 */
	public static LatticeGUIMustache createLatticeGUIMustache(String template,Structure struc,Collection<Integer> interfaceIds,List<StructureInterface> allInterfaces) throws StructureException {
		String templatePath = expandTemplatePath(template);
		logger.info("Loading mustache template from {}",templatePath);

		if( templatePath.toLowerCase().contains(TEMPLATE_3DMOL.toLowerCase()) ) {
			LatticeGUI3Dmol gui = new LatticeGUI3Dmol(templatePath.toString(),struc, null, interfaceIds);
			//TODO work out how to set this
			gui.setStrucURI(struc.getIdentifier()+".cif");
			return gui;
		}
		return new LatticeGUIMustache(templatePath.toString(), struc, interfaceIds);
	}
	
	/**
	 * Attempts to expand a short template name into a full path. For instance, '3Dmol', 'LatticeGUI3Dmol.html.mustache'
	 * and 'eppic-cli/src/main/resources/mustache/eppic/assembly/gui/LatticeGUI3Dmol.html.mustache' should all locate
	 * the correct template.
	 * 
	 * @param template Short template name
	 * @return Path to a matching template which can be located using the current classloader
	 * @throws IllegalArgumentException
	 *             if the template couldn't be found or was ambiguous
	 */
	public static String expandTemplatePath(String template) {
		// Mustache loads templates through the classloader, so we want a path it understands
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		// Try loading template directly
		URL url = cl.getResource(template);
		if( url != null) {
			logger.debug("Matched template {} directly.",template);
			return template;
		}
		
		//Try in template directory
		String attempt = TEMPLATE_DIR+template;
		url = cl.getResource(attempt);
		if( url != null) {
			logger.debug("Matched template {}",attempt);
			return attempt;
		}

		// See if any of the known templates match as a short name
		Pattern longNameRE = Pattern.compile(".*/(LatticeGUI)?"+template+"(\\..*mustache)?$", Pattern.CASE_INSENSITIVE);
		try {
			List<String> knownTemplates = getKnownTemplates()
					.filter(known ->longNameRE.matcher(known).matches())
					.collect(Collectors.toList());
			if(knownTemplates.size() > 1) {
				// js and json tend to be partials, so de-prioritize them
				List<String> notJSTemplates = knownTemplates.stream()
						.filter( known -> ! known.toLowerCase().contains(".js"))
						.collect(Collectors.toList());
				if( notJSTemplates.size() == 1) {
					knownTemplates = notJSTemplates;
				}
			}
			if(knownTemplates.size() > 1) {
				throw new IllegalArgumentException("Multiple templates match "+template+": "+String.join(",", knownTemplates));
			} else if(knownTemplates.size() == 1) {
				return knownTemplates.get(0);
			}
		} catch (IOException e) {
			// error below
		}
		// Give up
		throw new IllegalArgumentException("No matching template found for "+template);
	}
	
	public static Stream<String> getKnownTemplates() throws IOException {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		URL knownDirURL = cl.getResource(TEMPLATE_DIR);
		if( knownDirURL == null) {
			throw new IllegalStateException("Unable to find mustache templates. Error building jar?");
		}
		URI knownDirURI;
		try {
			knownDirURI = knownDirURL.toURI();
		} catch (URISyntaxException e) {
			throw new IllegalStateException("Unable to find mustache templates. Error building jar?");
		}
		Path knownDirPath;
		//need to strip leading / from jars
		
		Function<Path,String> pathToStr;
		if(knownDirURI.getScheme().equals("jar")) {
			FileSystem fs = FileSystems.newFileSystem(knownDirURI, Collections.<String,Object>emptyMap());
			knownDirPath = fs.getPath(TEMPLATE_DIR);
			pathToStr = path -> path.toString().charAt(0) == '/' ? path.toString().substring(1) : path.toString();
		} else {
			knownDirPath = Paths.get(knownDirURI);
			pathToStr = Path::toString;
		}
		return Files.walk(knownDirPath, 1)
				.map( pathToStr );
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
	public LatticeGUIMustache(String template, Structure struc,Collection<Integer> interfaceIds, List<StructureInterface> allInterfaces) throws StructureException {
		this(template,new LatticeGraph3D(struc,allInterfaces));

		if( interfaceIds != null ) {
			logger.info("Filtering LatticeGraph3D to edges {}",interfaceIds);
			latticeGraph.filterEngagedInterfaces(interfaceIds);
		}

		pdbId = struc.getStructureIdentifier().toCanonical().getPdbId();
		if(pdbId == null || pdbId.length() != 4) {
			pdbId = struc.getName();
		}
		if(pdbId == null || pdbId.length() != 4) {
			pdbId = null;
			logger.error("Unable to get PDB ID.");
		}

		this.title = String.format("Lattice for %s",getPdbId());
	}
	/**
	 * Constructor from a latticeGraph directly.
	 * The caller should pre-filter the engaged edges. The PdbId and Title
	 * properties should be set manually as well.
	 * @param template
	 * @param latticeGraph
	 * @throws StructureException
	 */
	public LatticeGUIMustache(String template, LatticeGraph3D latticeGraph) throws StructureException {
		this.latticeGraph = latticeGraph;
		this.template = template;

		UndirectedGraph<ChainVertex3D, InterfaceEdge3D> graph = latticeGraph.getGraph();
		logger.info("Using LatticeGraph3D with {} vertices and {} edges",graph.vertexSet().size(),graph.edgeSet().size());

		// Compute names and colors
		for(ChainVertex3D v : graph.vertexSet()) {
			v.setColorStr(toHexColor(v.getColor()));
		}

		for(InterfaceEdge3D e : graph.edgeSet()) {
			String colorStr = toHexColor(e.getColor());
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
		pdbId = null;
		title = null;
		this.size = "800";
	}

	/**
	 * hex version of the color (e.g. 'FF00CC')
	 * @param color
	 * @return
	 */
	private static String toHexColor(Color color) {
		if(color == null) return null;
		return String.format("%02x%02x%02x", color.getRed(),color.getGreen(),color.getBlue());
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
	public GraphLayout<ChainVertex3D,InterfaceEdge3D> getLayout2D() {
		return layout2d;
	}
	/**
	 * @param layout2d the 2D layout to set
	 */
	public void setLayout2D(GraphLayout<ChainVertex3D,InterfaceEdge3D> layout2d) {
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
			UndirectedGraph<ChainVertex3D, InterfaceEdge3D> graph3d = getGraph().getGraph();
			//clone
			UndirectedGraph<ChainVertex3D, InterfaceEdge3D> graph2dUnsorced = cloneGraph3D(graph3d);
			//Filter duplicate components
			graph2dUnsorced = LatticeGraph.filterUniqueStoichiometries(graph2dUnsorced);
			//Layout
			if(layout2d == null) {
				logger.warn("No 2D layout set for calculating the 2D graph. Defaulting to z-projection");
			} else {
				layout2d.projectLatticeGraph(graph2dUnsorced);
			}
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
				// list all interfaces, or null for "*"
				interfaceIds = new IntervalSet(interfaceIdsCommaSep).getIntegerSet();
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
		AtomCache cache = new AtomCache();
		cache.getFileParsingParams().setAlignSeqRes(true);
		Structure struc = cache.getStructure(input);

		LatticeGUIMustache gui;
		try {
			gui = createLatticeGUIMustache(template, struc, interfaceIds,null);
		} catch( IllegalArgumentException e) {
			System.err.println(e.getMessage());
			System.exit(1); return;
		}

		gui.setLayout2D( getDefaultLayout2D(struc) );
		gui.execute(mainOut);

		if( !output.equals("-")) {
			mainOut.close();
		}
	}

	public static GraphLayout<ChainVertex3D, InterfaceEdge3D> getDefaultLayout2D(
			Structure struc) {
		VertexPositioner<ChainVertex3D> vertexPositioner = ChainVertex3D.getVertexPositioner();
		List<GraphLayout<ChainVertex3D,InterfaceEdge3D>> layouts = new ArrayList<>();

		layouts.add( new UnitCellLayout<>(vertexPositioner, struc.getCrystallographicInfo().getCrystalCell()));
		QuaternaryOrientationLayout<ChainVertex3D,InterfaceEdge3D> stereo = new QuaternaryOrientationLayout<>(vertexPositioner);

//		Point3d center = new Point3d();
//		Point3d zenith = new Point3d(0,0,1);
//		StereographicLayout<ChainVertex3D,InterfaceEdge3D> stereo = new StereographicLayout<>(vertexPositioner , center , zenith));
		layouts.add(stereo);

		ConnectedComponentLayout<ChainVertex3D, InterfaceEdge3D> packer = new ConnectedComponentLayout<>(vertexPositioner);
		packer.setPadding(100);
		layouts.add(packer);
		return new ComboLayout<>(layouts);
	}
}
