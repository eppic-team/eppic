package ch.systemsx.sybit.crkwebui.server.jmol.generators;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import org.biojava.nbio.structure.PDBCrystallographicInfo;
import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.StructureException;
import org.biojava.nbio.structure.contact.StructureInterface;
import org.biojava.nbio.structure.xtal.SpaceGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.systemsx.sybit.crkwebui.server.commons.util.io.FileCache;
import ch.systemsx.sybit.crkwebui.server.jmol.servlets.LatticeGraphServlet;
import ch.systemsx.sybit.crkwebui.shared.model.Interface;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

import eppic.EppicParams;
import eppic.assembly.ChainVertex3D;
import eppic.assembly.InterfaceEdge3D;
import eppic.assembly.gui.LatticeGUIMustache;
import eppic.assembly.layout.GraphLayout;
import eppic.commons.util.IntervalSet;

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
	 * @throws ExecutionException 
	 * @throws InterruptedException 
	 */
	public static void generateJSONPage(File directory, String inputName, String atomCachePath,
			List<Interface> interfaces,
			Collection<Integer> requestedIfaces, PrintWriter out) throws IOException, StructureException, InterruptedException, ExecutionException {
		String jsonFilename = getJsonFilename(directory, inputName, requestedIfaces);
		Callable<String> computeJson = () -> {

			File auFile = LatticeGraphServlet.getAuFileName(directory, inputName, atomCachePath);
			Structure auStruct = LatticeGraphPageGenerator.readStructure(auFile);

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
				logger.info("Caching Assembly JSON at {}",jsonFilename);
				return json.toString();
			}
		};
		FileCache cache = FileCache.getInstance();
		String json = cache.getString(jsonFilename, computeJson);
		out.println(json);

	}

	private static String getJsonFilename(File directory, String inputName, Collection<Integer> requestedIfaces) {
		String interfaceIntervals;
		if(requestedIfaces == null || requestedIfaces.isEmpty() ) {
			interfaceIntervals = "*";
		} else {
			interfaceIntervals = new IntervalSet(new TreeSet<>(requestedIfaces)).toSelectionString();
		}
		String jsonFilename = new File(directory,String.format("%s%s.%s.json", inputName, EppicParams.ASSEMBLIES_DIAGRAM_FILES_SUFFIX, interfaceIntervals)).toString();
		return jsonFilename;
	}
	
	public static void generateHTMLPage(File directory, String inputName, String atomCachePath,
			String title, String size, String jsonURL, List<Interface> interfaces,
			Collection<Integer> requestedIfaces, PrintWriter out) throws IOException, StructureException {
		MustacheFactory mf = new DefaultMustacheFactory();
		String template = LatticeGUIMustache.expandTemplatePath(TEMPLATE_ASSEMBLY_DIAGRAM_FULL_LAZY);
		Mustache mustache = mf.compile(template);
		LazyLatticeGUIMustache3D page = new LazyLatticeGUIMustache3D();
		page.setSize(size);
		page.setTitle(title);
		page.setDataURL(jsonURL);
		try {
			mustache.execute(out, page).flush();
		} catch (IOException e) {
			logger.error("Error generating output from template "+template,e);
		}
	}

}
