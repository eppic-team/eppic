package eppic.assembly.gui;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.StructureException;
import org.biojava.nbio.structure.StructureTools;
import org.biojava.nbio.structure.io.util.FileDownloadUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

import eppic.assembly.ChainVertex3D;
import eppic.assembly.InterfaceEdge3D;
import eppic.assembly.LatticeGraph3D;
import eppic.assembly.OrientedCircle;

/**
 * 3Dmol viewer for LatticeGraph.
 * 
 * 3Dmol requires a html file with the commands and an mmcif file with the full
 * unit cell. These are written with the {@link #write3DmolCommands(PrintWriter)}
 * and {@link #writeCIFfile(PrintWriter)} functions.
 * @author blivens
 *
 */
public class LatticeGUI3Dmol {
	private static final Logger logger = LoggerFactory.getLogger(LatticeGUI3Dmol.class);

	private static final String MUSTACHE_TEMPLATE_3DMOL = "mustache/eppic/assembly/gui/LatticeGUI3Dmol.mustache.html";

	private final LatticeGraph3D graph;
	private URI strucURI;
	private String pdbId; //TODO use file instead

	/**
	 * 
	 * @param struc Structure used to create the graph (must have cell info)
	 * @param strucFile Location on the filesystem for the unit cell mmcif file
	 * @param strucURI URI to access strucFile through the browser
	 * @param interfaceIds List of interface numbers, or null for all interfaces
	 * @throws StructureException For errors parsing the structure
	 */
	public LatticeGUI3Dmol(Structure struc,URI strucURI,List<Integer> interfaceIds) throws StructureException {
		this.graph = new LatticeGraph3D(struc);
		if( interfaceIds != null ) {
			graph.filterEngagedInterfaces(interfaceIds);
		}

		// Compute Jmol names and colors
		for(ChainVertex3D v : graph.getVertices()) {
			v.setColorStr(toHTMLColor(v.getColor()));
		}

		for(InterfaceEdge3D e : graph.getEdges()) {
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
		this.strucURI = strucURI;
		//TODO support custom files
		pdbId = struc.getPdbId();
		if(pdbId == null || pdbId.length() != 4) {
			pdbId = struc.getName();
		}
		if(pdbId == null || pdbId.length() != 4) {
			logger.error("Unable to get PDB ID.");
		}
	}

	/**
	 * hex verson of the color (e.g. '0xFF00CC')
	 * @param color
	 * @return
	 */
	private static String toHTMLColor(Color color) {
		if(color == null) return null;
		return String.format("0x%2x%2x%2x", color.getRed(),color.getGreen(),color.getBlue());
	}

	public static void main(String[] args) throws IOException, StructureException {
		final String usage = String.format("Usage: %s <PDB code or file> <output.html> [list,of,interfaces,or,* [<output.cif.gz> <http://path/to/cif>]]",LatticeGUIJmol.class.getSimpleName());
		if (args.length<1) {
			logger.error("Expected at least 2 arguments");
			logger.error(usage);
			System.exit(1);return;
		}

		// Parse arguments
		int arg = 0;
		String input = args[arg++];
		List<Integer> interfaceIds = null;

		String output = args[arg++];
		PrintWriter htmlOut;
		if(output.equals("-")) {
			htmlOut = new PrintWriter(System.out);
		} else {
			htmlOut = new PrintWriter(FileDownloadUtils.expandUserHome(output));
		}

		if (args.length>arg) {
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
						System.exit(1);return;
					}
				}
			}
		}

		PrintWriter cifOut = null;
		URI uri = null;
		if(args.length>arg) {
			// need both filename and url
			String cifFilename = args[arg++];
			if (args.length<=arg) {
				logger.error("No URL specified.",arg);
				logger.error(usage);
				System.exit(1);
			}
			String uriStr = args[arg++];

			if(cifFilename.equals("-")) {
				cifOut = new PrintWriter(System.out);
			} else {
				File cifFile = new File(FileDownloadUtils.expandUserHome(cifFilename));
				cifOut = new PrintWriter(new GZIPOutputStream( new FileOutputStream( cifFile )));
			}
			try {
				uri = new URI(uriStr);
			} catch (URISyntaxException e) {
				logger.error(e.getMessage());
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

		LatticeGUI3Dmol gui = new LatticeGUI3Dmol(struc, uri, interfaceIds);

		if(cifOut != null) {
			gui.writeCIFfile(cifOut);
		}

		gui.write3DmolCommands(htmlOut);

		if( !output.equals("-")) {
			htmlOut.close();
		}
	}

	/**
	 * Write a cif file containing the unit cell.
	 * @param out
	 * @throws IOException
	 * @throws StructureException
	 */
	public void writeCIFfile(PrintWriter out) throws IOException, StructureException {
		graph.writeCellToMmCifFile(out);
	}

	/**
	 * Write an HTML page containing a 3Dmol canvas with this LatticeGraph.
	 * 
	 * The page is constructed from a template file.
	 * @param out Output stream for the html commands
	 */
	public void write3DmolCommands(PrintWriter out) {
		MustacheFactory mf = new DefaultMustacheFactory();
		String template = MUSTACHE_TEMPLATE_3DMOL;
		Mustache mustache = mf.compile(template);
		try {
			mustache.execute(out, this).flush();
		} catch (IOException e) {
			logger.error("Error generating 3Dmol commands from template "+template,e);
		}
	}

	public LatticeGraph3D getGraph() {
		return graph;
	}

	/**
	 * get the URL for the CIF structure to write. Defaults to "[PDBID].cif"
	 * @return
	 */
	public URI getStrucURI() {
		if(strucURI == null) {
			try {
				strucURI = new URI(String.format("%s.cif",getPdbId()));
			} catch (URISyntaxException e) {
				// Give up, return null
			}
		}
		return strucURI;
	}
	public void setStrucURI(URI strucURI) {
		this.strucURI = strucURI;
	}
	/**
	 * Get the PDB ID. By default, takes this from the structure name.
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
		return getPdbId().substring(1, 3);
	}
}
