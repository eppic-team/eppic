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

import ch.systemsx.sybit.crkwebui.shared.model.Interface;
import eppic.assembly.gui.LatticeGUI3Dmol;
import eppic.assembly.gui.LatticeGUIMustache;

/**
 * Helper class to generate the LatticeGraph HTML
 * @author Spencer Bliven
 *
 */
public class LatticeGraphPageGenerator {
	private static final Logger logger = LoggerFactory.getLogger(LatticeGraphPageGenerator.class);
	/**
	 * Generates html page containing the NGL canvas.
	 * 
	 * @param directory path to the job directory
	 * @param inputName the input: either a PDB id or the file name as input by user
	 * @param auFile the file with the AU structure (can be cif or pdb and gzipped or not)
	 * @param auURI URL to reach auCifFile within the browser
	 * @param title Page title [default: structure name]
	 * @param size the canvas size 
	 * @param interfaces List of all interfaces to build the latticegraph
	 * @param requestedIfaces 
	 * @param out
	 * @return the HTML page
	 * @throws StructureException For errors parsing the input structure
	 * @throws IOException For errors reading or writing files
	 */
	public static void generatePage(File directory, String inputName, File auFile,
			String auURI, String title, String size, List<Interface> interfaces,
			Collection<Integer> requestedIfaces, PrintWriter out, String urlMolViewer) throws IOException, StructureException {

		
		if( !auFile.exists() ) {
			// this shouldn't happen...
			throw new IOException("Could not find input AU file "+ auFile.toString());
		
		}
		
		// Read input structure
		
		Structure auStruct = readStructure(auFile);

		// Read spacegroup
		PDBCrystallographicInfo crystInfo = auStruct
				.getCrystallographicInfo();
		SpaceGroup sg = crystInfo.getSpaceGroup();

		List<StructureInterface> siList = createStructureInterfaces(interfaces, sg);

		LatticeGUI3Dmol gui = new LatticeGUI3Dmol(LatticeGUIMustache.MUSTACHE_TEMPLATE_NGL, auStruct, auURI,
				requestedIfaces, siList);

		// Override some properties if needed
		if(title != null)
			gui.setTitle(title);
		if(size != null) 
			gui.setSize(size);
		
		//"https://cdn.rawgit.com/arose/ngl/v0.7.1a/js/build/ngl.embedded.min.js"
		gui.setUrl3Dmol(urlMolViewer);
		

		// Construct page
		gui.execute(out);
		out.flush();
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

}
