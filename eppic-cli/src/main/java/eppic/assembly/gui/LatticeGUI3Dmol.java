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
import org.jgrapht.UndirectedGraph;
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
public class LatticeGUI3Dmol extends LatticeGUIMustache {
	private static final Logger logger = LoggerFactory.getLogger(LatticeGUI3Dmol.class);

	static final String MUSTACHE_TEMPLATE_3DMOL = "/mustache/eppic/assembly/gui/LatticeGUI3Dmol.mustache.html";
	static final String DEFAULT_URL_3DMOL = "http://3Dmol.csb.pitt.edu/build/3Dmol-min.js";
	
	private String strucURI;
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
		this(MUSTACHE_TEMPLATE_3DMOL,struc,strucURI,interfaceIds,null);
	}
	public LatticeGUI3Dmol(Structure struc,String strucURI,Collection<Integer> interfaceIds,List<StructureInterface> allInterfaces) throws StructureException {
		this(MUSTACHE_TEMPLATE_3DMOL,struc,strucURI,interfaceIds,allInterfaces);
	}
	public LatticeGUI3Dmol(String template, Structure struc,String strucURI,Collection<Integer> interfaceIds,List<StructureInterface> allInterfaces) throws StructureException {
		super(template, struc,interfaceIds, allInterfaces);

		this.strucURI = strucURI;
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
	public String getUrl3Dmol() {
		return url3Dmol;
	}
	public void setUrl3Dmol(String url3Dmol) {
		this.url3Dmol = url3Dmol;
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

		gui.execute(htmlOut);

		if( !output.equals("-")) {
			htmlOut.close();
		}
	}

}
