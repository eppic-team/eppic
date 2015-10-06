package ch.systemsx.sybit.crkwebui.server.jmol.generators;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.biojava.nbio.structure.Atom;
import org.biojava.nbio.structure.PDBCrystallographicInfo;
import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.StructureException;
import org.biojava.nbio.structure.StructureTools;
import org.biojava.nbio.structure.contact.AtomContactSet;
import org.biojava.nbio.structure.contact.StructureInterface;
import org.biojava.nbio.structure.contact.StructureInterfaceCluster;
import org.biojava.nbio.structure.xtal.CrystalTransform;
import org.biojava.nbio.structure.xtal.SpaceGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.systemsx.sybit.crkwebui.shared.model.Interface;
import eppic.assembly.gui.LatticeGUI3Dmol;

/**
 * Helper class to generate the LatticeGraph HTML
 * @author Spencer Bliven
 *
 */
public class LatticeGraphPageGenerator {
	private static final Logger logger = LoggerFactory.getLogger(LatticeGraphPageGenerator.class);
	/**
	 * Generates html page containing the 3Dmol canvas.
	 * 
	 * @param structFilename Path to the input structure
	 * @param ucFilename Path to the unit cell structure. Will be generated if doesn't exist
	 * @param ucURI URL to reach ucFilename within the browser
	 * @param title Page title [default: structure name]
	 * @param interfaces List of all interfaces to build the latticegraph
	 * @param requestedIfaces 
	 * @param url3dmoljs 3Dmol script URL. (default: http://3Dmol.csb.pitt.edu/build/3Dmol-min.js)
	 * @return the HTML page
	 * @throws StructureException For errors parsing the input structure
	 * @throws IOException For errors reading or writing files
	 */
	public static void generatePage(String structFilename, String ucFilename,
			String ucURI, String title, List<Interface> interfaces,
			List<Integer> requestedIfaces, String url3dmoljs, PrintWriter out) throws IOException, StructureException {

		out.println("<!--");
		out.println("structFilename="+structFilename);
		out.println("exists="+(new File(structFilename).exists()?"true":"false"));
		out.println("ucFilename="+ucFilename);
		out.println("interfaces="+interfaces);
		out.println("ucURI="+ucURI);
		out.println("title="+title);
		out.println("PWD="+(new File(".").getAbsolutePath()));
		out.println("-->");
		//out.close();
		//if(true)return;
		
		// Read input structure
		Structure auStruct = StructureTools.getStructure(structFilename);

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
			CrystalTransform firstTransf = new CrystalTransform(sg, 0);
			CrystalTransform secondTransf = new CrystalTransform(sg,
					iface.getInterfaceId());
			StructureInterface siface = new StructureInterface(
					firstMolecule, secondMolecule, firstMoleculeId,
					secondMoleculeId, contacts, firstTransf, secondTransf);
			siface.setId(iface.getInterfaceId());
			// hack, new cluster for each interface but with duplicate IDs
			StructureInterfaceCluster cluster = new StructureInterfaceCluster();
			cluster.setId(iface.getClusterId());
			siface.setCluster(cluster );
			siList.add(siface);
		}


		LatticeGUI3Dmol gui = new LatticeGUI3Dmol(auStruct, ucURI,
				requestedIfaces, siList);

		// Override some properties if needed
		if(title != null)
			gui.setTitle(title);
		if(url3dmoljs != null)
			gui.setUrl3Dmol(url3dmoljs);

		// Write unit cell, if necessary
		File ucFile = new File(ucFilename);
		if( !ucFile.exists() ) {
			logger.info("Writing Unit Cell file to {}",ucFile.getAbsolutePath());
			PrintWriter cifOut = new PrintWriter(ucFile);
			gui.writeCIFfile(cifOut);
			cifOut.close();
		}

		// Construct page
		gui.write3DmolCommands(out);
		out.flush();
	}

	public static void main(String[] args) {
		// Parse arguments
		final String usage = "usage: structFilename unitcellFilename";
		if(args.length< 2) {
			System.err.println("Insufficient arguments");
			System.err.println(usage);
			System.exit(1);return;
		}
		int arg=0;
		String structFilename = args[arg++];
		String ucFilename = args[arg++];
		if(args.length>=arg) {
			System.err.println("Too many arguments");
			System.err.println(usage);
			System.exit(1);return;
		}
		// Done parsing arguments

		String ucURI = String.format("file://%s",new File(ucFilename).getAbsolutePath());

		List<Interface> interfaces = null;
		StringWriter html = null;
		PrintWriter html2 = null;
		try {
			html = new StringWriter();
			html2 = new PrintWriter(html);
			generatePage(structFilename, ucFilename, ucURI, null, interfaces ,null, null,html2);
			System.out.println(html.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (StructureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if(html != null) {
				try {
					html.close();
				} catch(Exception e) {}
			}
			if(html2 != null) {
				try {
					html2.close();
				} catch(Exception e) {}
			}
		}

	}
}
