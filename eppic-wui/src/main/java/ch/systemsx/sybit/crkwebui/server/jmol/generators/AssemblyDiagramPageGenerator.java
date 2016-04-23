package ch.systemsx.sybit.crkwebui.server.jmol.generators;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.biojava.nbio.structure.Atom;
import org.biojava.nbio.structure.PDBCrystallographicInfo;
import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.StructureException;
import org.biojava.nbio.structure.StructureIO;
import org.biojava.nbio.structure.align.util.AtomCache;
import org.biojava.nbio.structure.contact.AtomContactSet;
import org.biojava.nbio.structure.contact.StructureInterface;
import org.biojava.nbio.structure.contact.StructureInterfaceCluster;
import org.biojava.nbio.structure.io.LocalPDBDirectory.FetchBehavior;
import org.biojava.nbio.structure.io.PDBFileParser;
import org.biojava.nbio.structure.io.mmcif.MMcifParser;
import org.biojava.nbio.structure.io.mmcif.SimpleMMcifConsumer;
import org.biojava.nbio.structure.io.mmcif.SimpleMMcifParser;
import org.biojava.nbio.structure.xtal.CrystalTransform;
import org.biojava.nbio.structure.xtal.SpaceGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.systemsx.sybit.crkwebui.shared.model.Interface;
import eppic.assembly.ChainVertex3D;
import eppic.assembly.InterfaceEdge3D;
import eppic.assembly.gui.LatticeGUIMustache;
import eppic.assembly.layout.ComboLayout;
import eppic.assembly.layout.ConnectedComponentLayout;
import eppic.assembly.layout.GraphLayout;
import eppic.assembly.layout.QuaternaryOrientationLayout;
import eppic.assembly.layout.VertexPositioner;

/**
 * Helper class to generate the LatticeGraph HTML
 * @author Spencer Bliven
 *
 */
public class AssemblyDiagramPageGenerator {
	private static final Logger logger = LoggerFactory.getLogger(AssemblyDiagramPageGenerator.class);
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
	public static void generatePage(File directory, String inputName, String atomCachePath,
			String title, String size, List<Interface> interfaces,
			Collection<Integer> requestedIfaces, PrintWriter out) throws IOException, StructureException {

		// Read input structure
		Structure auStruct;
		File structFile = new File(directory,inputName);
		if(structFile.exists()) {
			// Match file type
			if( structFile.getName().endsWith(".cif")) {
				MMcifParser parser = new SimpleMMcifParser();

				SimpleMMcifConsumer consumer = new SimpleMMcifConsumer();

				// The Consumer builds up the BioJava - structure object.
				// you could also hook in your own and build up you own data model.
				parser.addMMcifConsumer(consumer);

				InputStream inStream = new FileInputStream(structFile);
				parser.parse(inStream);

				// now get the protein structure.
				auStruct = consumer.getStructure();
			} else {
				PDBFileParser parser = new PDBFileParser();

				InputStream inStream = new FileInputStream(structFile);
				auStruct = parser.parsePDBFile(inStream);
			}
		} else if (!inputName.matches("^\\d\\w\\w\\w$")) {
				logger.error("Could not find file {} and the inputName '{}' does not look like a PDB id. Can't produce the assembly diagram page!", structFile.toString(), inputName);
				return;
		} else {
			// it is like a PDB id, leave it to AtomCache
			AtomCache atomCache = null;
			if (atomCachePath ==null) 
				atomCache = new AtomCache();
			else 
				atomCache = new AtomCache(atomCachePath);
		
			// we set it to FETCH_FILES to avoid going to the PDB ftp server, because we trust in principle what we have in our cache dir (which is rsynced externally or by the eppic cli run)
			atomCache.setFetchBehavior(FetchBehavior.FETCH_FILES);
			
			StructureIO.setAtomCache(atomCache);
		
			
			// leave it to atomcache
			auStruct = StructureIO.getStructure(inputName);
		}

		// Read spacegroup
		PDBCrystallographicInfo crystInfo = auStruct
				.getCrystallographicInfo();
		SpaceGroup sg = crystInfo.getSpaceGroup();

		// Convert `Interface` beans to full StructureInterface objects
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

		LatticeGUIMustache gui = LatticeGUIMustache.createLatticeGUIMustache(LatticeGUIMustache.TEMPLATE_ASSEMBLY_DIAGRAM_FULL, auStruct, requestedIfaces, siList);


		// Override some properties if needed
		if(title != null)
			gui.setTitle(title);
		if(size != null) 
			gui.setSize(size);

		VertexPositioner<ChainVertex3D> vertexPositioner = ChainVertex3D.getVertexPositioner();
		List<GraphLayout<ChainVertex3D,InterfaceEdge3D>> layouts = new ArrayList<>();

		QuaternaryOrientationLayout<ChainVertex3D,InterfaceEdge3D> stereo = new QuaternaryOrientationLayout<>(vertexPositioner);
		
//		Point3d center = new Point3d();
//		Point3d zenith = new Point3d(0,0,1);
//		StereographicLayout<ChainVertex3D,InterfaceEdge3D> stereo = new StereographicLayout<>(vertexPositioner , center , zenith));
		layouts.add(stereo);
		
		ConnectedComponentLayout<ChainVertex3D, InterfaceEdge3D> packer = new ConnectedComponentLayout<>(vertexPositioner);
		packer.setPadding(100);
		layouts.add(packer);
		
		gui.setLayout2D(new ComboLayout<>(layouts) );

		
		// Construct page
		gui.execute(out);
		out.flush();
	}

}
