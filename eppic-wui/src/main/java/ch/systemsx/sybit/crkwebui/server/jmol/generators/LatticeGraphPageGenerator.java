package ch.systemsx.sybit.crkwebui.server.jmol.generators;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.zip.GZIPInputStream;

import org.biojava.nbio.structure.Atom;
import org.biojava.nbio.structure.PDBCrystallographicInfo;
import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.StructureException;
import org.biojava.nbio.structure.contact.AtomContactSet;
import org.biojava.nbio.structure.contact.StructureInterface;
import org.biojava.nbio.structure.contact.StructureInterfaceCluster;
import org.biojava.nbio.structure.io.PDBFileParser;
import org.biojava.nbio.structure.io.mmcif.MMcifParser;
import org.biojava.nbio.structure.io.mmcif.SimpleMMcifConsumer;
import org.biojava.nbio.structure.io.mmcif.SimpleMMcifParser;
import org.biojava.nbio.structure.xtal.CrystalTransform;
import org.biojava.nbio.structure.xtal.SpaceGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.systemsx.sybit.crkwebui.server.commons.util.io.FileCache;
import ch.systemsx.sybit.crkwebui.server.jmol.generators.json.ChainVertex3DJsonAdapter;
import ch.systemsx.sybit.crkwebui.server.jmol.generators.json.InterfaceEdge3DJsonAdapter;
import ch.systemsx.sybit.crkwebui.server.jmol.generators.json.InterfaceEdge3DSourcedJsonAdapter;
import ch.systemsx.sybit.crkwebui.server.jmol.generators.json.LatticeGraph3DJson;
import ch.systemsx.sybit.crkwebui.server.jmol.generators.json.ParametricCircularArcJsonAdapter;
import ch.systemsx.sybit.crkwebui.shared.model.Interface;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import eppic.assembly.ChainVertex3D;
import eppic.assembly.InterfaceEdge3D;
import eppic.assembly.LatticeGraph3D;
import eppic.assembly.ParametricCircularArc;
import eppic.assembly.gui.InterfaceEdge3DSourced;
import eppic.assembly.gui.LatticeGUIMustache;
import eppic.commons.util.IntervalSet;

/**
 * Helper class to generate the LatticeGraph HTML
 * @author Spencer Bliven
 *
 */
public class LatticeGraphPageGenerator {
	private static final Logger logger = LoggerFactory.getLogger(LatticeGraphPageGenerator.class);
	
	private static final Gson gson = createGson();
	
	public static final String TEMPLATE_LATTICE_GUI_NGL_LAZY = "LatticeGUINglLazy.html.mustache";

	/**
	 * Generates html page containing the NGL canvas.
	 * 
	 * @param directory path to the job directory
	 * @param inputName the input: either a PDB id or the file name as input by user
	 * @param strucFile the file with the AU structure (can be cif or pdb and gzipped or not)
	 * @param strucURI URL to reach auCifFile within the browser
	 * @param title Page title [default: structure name]
	 * @param size the canvas size
	 * @param jsonURI path to the json dataURL
	 * @param interfaces List of all interfaces to build the latticegraph
	 * @param requestedIfaces subset of the interfaces to display
	 * @param out Stream to output the HTML page
	 * @param urlMolViewer path to the libURL
	 * @throws StructureException For errors parsing the input structure
	 * @throws IOException For errors reading or writing files
	 */
	public static void generateHTMLPage(File directory, String inputName, File strucFile,
			String strucURI, String title, String size, String jsonURL, List<Interface> interfaces,
			Collection<Integer> requestedIfaces, PrintWriter out, String urlMolViewer) throws IOException, StructureException {
		logger.info("JSON URL for {}: {}",inputName,jsonURL);
		logger.info("Structure URL for {}: {}",inputName,strucURI);
		MustacheFactory mf = new DefaultMustacheFactory();
		String template = LatticeGUIMustache.expandTemplatePath(TEMPLATE_LATTICE_GUI_NGL_LAZY);
		Mustache mustache = mf.compile(template);
		LazyLatticeGUIMustache3D page = new LazyLatticeGUIMustache3D();
		page.setSize(size);
		page.setTitle(title);
		page.setDataURL(jsonURL);
		page.setLibURL(urlMolViewer);
		page.setStrucURL(strucURI);
		try {
			mustache.execute(out, page)
				.flush();
		} catch (IOException e) {
			logger.error("Error generating output from template "+template,e);
		}
	}
	/**
	 * Generates html page containing the NGL canvas.
	 * 
	 * @param directory path to the job directory
	 * @param inputName the input: either a PDB id or the file name as input by user
	 * @param strucFile the file with the AU structure (can be cif or pdb and gzipped or not)
	 * @param interfaces List of all interfaces to build the latticegraph
	 * @param requestedIfaces subset of the interfaces to display
	 * @param out Stream to output the HTML page
	 * @throws StructureException For errors parsing the input structure
	 * @throws IOException For errors reading or writing files
	 * @throws ExecutionException 
	 * @throws InterruptedException 
	 */
	public static void generateJSONPage(File directory, String inputName, File strucFile, List<Interface> interfaces,
			Collection<Integer> requestedIfaces, PrintWriter out) throws IOException, StructureException, InterruptedException, ExecutionException {
		String jsonFilename = getJsonFilename(directory, inputName, requestedIfaces);

		Callable<String> computeJson = () -> {
			if( !strucFile.exists() ) {
				// this shouldn't happen...
				throw new IOException("Could not find input AU file "+ strucFile.toString());

			}

			// Read input structure

			Structure struc = readStructure(strucFile);

			// Read spacegroup
			PDBCrystallographicInfo crystInfo = struc
					.getCrystallographicInfo();
			SpaceGroup sg = crystInfo.getSpaceGroup();

			List<StructureInterface> siList = createStructureInterfaces(interfaces, sg);

			LatticeGraph3D graph = new LatticeGraph3D(struc,siList);
			if( requestedIfaces != null ) {
				logger.info("Filtering LatticeGraph3D to edges {}",requestedIfaces);
				graph.filterEngagedInterfaces(requestedIfaces);
			}
			graph.setHexColors();

			String json = gson.toJson(graph);

			logger.info("Caching LatticeGraph JSON at {}",jsonFilename);
			return json;
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
		String jsonFilename = new File(directory,String.format("%s.latticeGraph.%s.json", inputName, interfaceIntervals)).toString();
		return jsonFilename;
	}

	/**
	 * Convert `Interface` beans to full StructureInterface objects
	 * @param interfaces
	 * @param sg
	 * @return
	 */
	public static List<StructureInterface> createStructureInterfaces(
			List<Interface> interfaces, SpaceGroup sg) {
		List<StructureInterface> siList = new ArrayList<StructureInterface>(
				interfaces.size());
		for (Interface iface : interfaces) {
			// TODO
			Atom[] firstMolecule = new Atom[0];
			Atom[] secondMolecule = new Atom[0];
			String firstMoleculeId = iface.getChain1();
			String secondMoleculeId = iface.getChain2();
			AtomContactSet contacts = null;
			int interfaceId = iface.getInterfaceId();
			int opId = iface.getOperatorId();
			if(opId < 0 || opId >= sg.getNumOperators() ) {
				logger.error("Found interface {} in the database, but only {} operators in spacegroup",opId, sg.getNumOperators());
				continue;
			}
			CrystalTransform firstTransf = new CrystalTransform(sg, 0);
			CrystalTransform secondTransf = new CrystalTransform(sg,
					opId);
			secondTransf.setMatTransform(SpaceGroup.getMatrixFromAlgebraic(iface.getOperator()));
			
			StructureInterface siface = new StructureInterface(
					firstMolecule, secondMolecule, firstMoleculeId,
					secondMoleculeId, contacts, firstTransf, secondTransf);
			siface.setId(interfaceId);
			// hack, new cluster for each interface but with duplicate IDs
			StructureInterfaceCluster cluster = new StructureInterfaceCluster();
			cluster.setId(iface.getClusterId());
			siface.setCluster(cluster );
			siList.add(siface);
		}
		return siList;
	}

	/**
	 * Loads a structure from given file path.
	 * @param auFile
	 * @return the parsed Structure
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws StructureException If inputName was neither a file nor a PDB code
	 */
	public static Structure readStructure(File auFile) throws FileNotFoundException, IOException,
			StructureException {
				
		// Read input structure
		Structure auStruct;

		// Match file type
		if( auFile.getName().endsWith(".cif") || auFile.getName().endsWith(".CIF")) { 
			MMcifParser parser = new SimpleMMcifParser();

			SimpleMMcifConsumer consumer = new SimpleMMcifConsumer();

			parser.addMMcifConsumer(consumer);

			InputStream inStream = new FileInputStream(auFile);
			parser.parse(inStream);

			auStruct = consumer.getStructure();
		} else if (auFile.getName().endsWith("cif.gz") || auFile.getName().endsWith("CIF.GZ") || auFile.getName().endsWith("CIF.gz") || auFile.getName().endsWith("cif.GZ")) {
			MMcifParser parser = new SimpleMMcifParser();

			SimpleMMcifConsumer consumer = new SimpleMMcifConsumer();

			parser.addMMcifConsumer(consumer);

			InputStream inStream = new GZIPInputStream(new FileInputStream(auFile));
			parser.parse(inStream);

			auStruct = consumer.getStructure();

			// assume it is a pdb file if extension different from cif, cif.gz				
		} else if (auFile.getName().endsWith(".gz") || auFile.getName().endsWith(".GZ")) {
			PDBFileParser parser = new PDBFileParser();

			InputStream inStream = new GZIPInputStream(new FileInputStream(auFile));
			auStruct = parser.parsePDBFile(inStream);

		} else {

			PDBFileParser parser = new PDBFileParser();

			InputStream inStream = new FileInputStream(auFile);
			auStruct = parser.parsePDBFile(inStream);
		}

		return auStruct;
	}

	/**
	 * Create a Gson object that can serialize LatticeGraph3D objects
	 * @return
	 */
	private static Gson createGson() {
		return new GsonBuilder()
			.registerTypeAdapter(LatticeGraph3D.class, new LatticeGraph3DJson())
			.registerTypeAdapter(ChainVertex3D.class, new ChainVertex3DJsonAdapter())
			.registerTypeAdapter(InterfaceEdge3DSourced.class, new InterfaceEdge3DSourcedJsonAdapter())
			.registerTypeAdapter(InterfaceEdge3D.class, new InterfaceEdge3DJsonAdapter())
			.registerTypeAdapter(ParametricCircularArc.class, new ParametricCircularArcJsonAdapter())
			.setPrettyPrinting()
			.create();
	}

}
