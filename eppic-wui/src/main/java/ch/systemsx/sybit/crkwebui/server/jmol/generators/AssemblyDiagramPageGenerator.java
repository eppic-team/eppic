package ch.systemsx.sybit.crkwebui.server.jmol.generators;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.List;

import org.biojava.nbio.structure.PDBCrystallographicInfo;
import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.StructureException;
import org.biojava.nbio.structure.contact.StructureInterface;
import org.biojava.nbio.structure.xtal.SpaceGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

import ch.systemsx.sybit.crkwebui.shared.model.Interface;
import eppic.assembly.ChainVertex3D;
import eppic.assembly.InterfaceEdge3D;
import eppic.assembly.gui.LatticeGUI3Dmol;
import eppic.assembly.gui.LatticeGUIMustache;
import eppic.assembly.layout.GraphLayout;

/**
 * Helper class to generate the LatticeGraph HTML
 * @author Spencer Bliven
 *
 */
public class AssemblyDiagramPageGenerator {
	private static final Logger logger = LoggerFactory.getLogger(AssemblyDiagramPageGenerator.class);
	
	public static final String TEMPLATE_ASSEMBLY_DIAGRAM_FULL_LAZY = "AssemblyDiagramFullLazy.html.mustache";
	public static final String TEMPLATE_ASSEMBLY_DIAGRAM_JSON = LatticeGUIMustache.expandTemplatePath("AssemblyDiagramFull.json.mustache");

	/**
	 * Generates html page containing the 3Dmol canvas.
	 * 
	 * @param directory path to the job directory
	 * @param inputName the input: either a PDB id or the file name as input by user
	 * @param atomCachePath the path for Biojava's AtomCache
	 * @param title Page title [default: structure name]
	 * @param size the canvas size 
	 * @param interfaces List of all interfaces to build the latticegraph
	 * @param requestedIfaces 
	 * @param out
	 * @return the HTML page
	 * @throws StructureException For errors parsing the input structure
	 * @throws IOException For errors reading or writing files
	 */
	public static void generateJSONPage(File directory, String inputName, String atomCachePath,
			List<Interface> interfaces,
			Collection<Integer> requestedIfaces, PrintWriter out) throws IOException, StructureException {

		Structure auStruct = LatticeGraphPageGenerator.readStructure(directory, inputName, atomCachePath);
		
		// Read spacegroup
		PDBCrystallographicInfo crystInfo = auStruct
				.getCrystallographicInfo();
		SpaceGroup sg = crystInfo.getSpaceGroup();

		// Convert `Interface` beans to full StructureInterface objects
		List<StructureInterface> siList = LatticeGraphPageGenerator.createStructureInterfaces(interfaces, sg);

		LatticeGUIMustache gui = LatticeGUIMustache.createLatticeGUIMustache(TEMPLATE_ASSEMBLY_DIAGRAM_JSON, auStruct, requestedIfaces, siList);

		GraphLayout<ChainVertex3D, InterfaceEdge3D> layout2D = LatticeGUIMustache.getDefaultLayout2D(auStruct);
		gui.setLayout2D( layout2D );


		// Hack to work around Mustache limitations which prevent generating valid JSON
		try(StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				) {

			// Construct page
			gui.execute(pw);

			pw.flush();
			sw.flush();
			String json = sw.toString();
			// Remove all trailing commas from lists (invalid JSON)
			json = json.replaceAll(",(?=\\s*[}\\]])","");
			out.write(json);
		}
		out.flush();
	}
	
	public static void generateHTMLPage(File directory, String inputName, String atomCachePath,
			String title, String size, String jsonURL, List<Interface> interfaces,
			Collection<Integer> requestedIfaces, PrintWriter out) throws IOException, StructureException {
		MustacheFactory mf = new DefaultMustacheFactory();
		String template = LatticeGUIMustache.expandTemplatePath(TEMPLATE_ASSEMBLY_DIAGRAM_FULL_LAZY);
		Mustache mustache = mf.compile(template);
		LazyLatticeGraph3D page = new LazyLatticeGraph3D();
		page.setSize(size);
		page.setTitle(title);
		page.setStrucURI(jsonURL);
		try {
			mustache.execute(out, page).flush();
		} catch (IOException e) {
			logger.error("Error generating output from template "+template,e);
		}
	}
	
	/**
	 * Small bean to mimic elements of {@link LatticeGUI3Dmol} without
	 * the overhead of calculating the graph
	 * @author blivens
	 *
	 */
	public static class LazyLatticeGraph3D {
		private String strucURI; //path to the structure
		private String libURL; // path to the library (if any)
		private String title; // Title for HTML page
		private String size; // Target size for content
		public LazyLatticeGraph3D() {}
		public LazyLatticeGraph3D(String strucURI, String libURL,
				String title, String size) {
			this.strucURI = strucURI;
			this.libURL = libURL;
			this.title = title;
			this.size = size;
		}
		public String getStrucURI() {
			return strucURI;
		}
		public void setStrucURI(String strucURI) {
			this.strucURI = strucURI;
		}
		public String getLibURL() {
			return libURL;
		}
		public void setLibURL(String libURL) {
			this.libURL = libURL;
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
	}

}
