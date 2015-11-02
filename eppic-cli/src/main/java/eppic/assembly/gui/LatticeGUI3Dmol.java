package eppic.assembly.gui;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.StructureException;
import org.biojava.nbio.structure.StructureTools;
import org.biojava.nbio.structure.contact.StructureInterface;
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
	private static final String DEFAULT_URL_3DMOL = "http://3Dmol.csb.pitt.edu/build/3Dmol-min.js";
	
	private final LatticeGraph3D graph;
	private String strucURI;
	private String pdbId; //TODO use file instead
	private String title; //Title for HTML page
	private String url3Dmol = DEFAULT_URL_3DMOL;
	
	/**
	 * 
	 * @param struc Structure used to create the graph (must have cell info)
	 * @param strucFile Location on the filesystem for the unit cell mmcif file
	 * @param strucURI URI to access strucFile through the browser
	 * @param interfaceIds List of interface numbers, or null for all interfaces
	 * @throws StructureException For errors parsing the structure
	 */
	public LatticeGUI3Dmol(Structure struc,String strucURI,Collection<Integer> interfaceIds) throws StructureException {
		this(struc,strucURI,interfaceIds,null);
	}
	public LatticeGUI3Dmol(Structure struc,String strucURI,Collection<Integer> interfaceIds,List<StructureInterface> allInterfaces) throws StructureException {
		this.graph = new LatticeGraph3D(struc,allInterfaces);
		if( interfaceIds != null ) {
			logger.info("Filtering LatticeGraph3D to edges {}",interfaceIds);
			graph.filterEngagedInterfaces(interfaceIds);
		}
		logger.info("Generated LatticeGraph3D with {} vertices and {} edges",graph.getVertices().size(),graph.getEdges().size());

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
		
		this.title = String.format("Lattice for %s",getPdbId());
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
		final String usage = String.format("Usage: %s <PDB code or file> <output.html> [list,of,interfaces,or,* [<output.cif.gz> <http://path/to/cif>]]",LatticeGUI3Dmol.class.getSimpleName());
		if (args.length<1) {
			logger.error("Expected at least 2 arguments");
			logger.error(usage);
			System.exit(1);return;
		}

		// Parse arguments
		int arg = 0;
		String input = args[arg++];
		Collection<Integer> interfaceIds = null;

		String output = args[arg++];
		PrintWriter htmlOut;
		if(output.equals("-")) {
			htmlOut = new PrintWriter(System.out);
		} else {
			htmlOut = new PrintWriter(FileDownloadUtils.expandUserHome(output));
		}

		if (args.length>arg) {
			String interfaceIdsCommaSep = args[arg++];
			try {
				interfaceIds = parseInterfaceList(interfaceIdsCommaSep);
			} catch( NumberFormatException e) {
				logger.error("Invalid interface IDs. Expected comma-separated list, got {}",interfaceIdsCommaSep);
				System.exit(1);return;
			}
		}

		PrintWriter cifOut = null;
		String uri = null;
		if(args.length>arg) {
			// need both filename and url
			String cifFilename = args[arg++];
			if (args.length<=arg) {
				logger.error("No URL specified.",arg);
				logger.error(usage);
				System.exit(1);
			}
			uri = args[arg++];

			if(cifFilename.equals("-")) {
				cifOut = new PrintWriter(System.out);
			} else {
				File cifFile = new File(FileDownloadUtils.expandUserHome(cifFilename));
				cifOut = new PrintWriter(new GZIPOutputStream( new FileOutputStream( cifFile )));
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
	public String getStrucURI() {
		if(strucURI == null) {
			strucURI = String.format("%s.cif",getPdbId());
		}
		return strucURI;
	}
	public void setStrucURI(String strucURI) {
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
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getUrl3Dmol() {
		return url3Dmol;
	}
	public void setUrl3Dmol(String url3Dmol) {
		this.url3Dmol = url3Dmol;
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

}
