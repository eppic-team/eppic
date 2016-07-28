package eppic.assembly.gui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.StructureException;
import org.biojava.nbio.structure.align.util.AtomCache;
import org.biojava.nbio.structure.contact.StructureInterface;
import org.biojava.nbio.structure.io.util.FileDownloadUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eppic.assembly.LatticeGraph3D;
import eppic.commons.util.IntervalSet;

/**
 * 3D molecular viewer for LatticeGraph.
 * 
 * 3Dmol requires a html file with the commands and an mmcif file with the full
 * unit cell. These are written with the {@link #write3DmolCommands(PrintWriter)}
 * and {@link #writeCIFfile(PrintWriter)} functions.
 * @author blivens
 *
 */
public class LatticeGUIMustache3D extends LatticeGUIMustache {
	private static final Logger logger = LoggerFactory.getLogger(LatticeGUIMustache3D.class);

	public static final String MUSTACHE_TEMPLATE_3DMOL = "mustache/eppic/assembly/gui/LatticeGUI3Dmol.html.mustache";
	public static final String MUSTACHE_TEMPLATE_NGL = "mustache/eppic/assembly/gui/LatticeGUINgl.html.mustache";
	public static final String DEFAULT_URL_3DMOL = "http://3Dmol.csb.pitt.edu/build/3Dmol-min.js";
	public static final String DEFAULT_URL_NGL = "https://cdn.rawgit.com/arose/ngl/v0.7.1a/js/build/ngl.embedded.min.js";

	private String strucURL;
	private String dataURL; // path to the json (or other) data file

	private String libURL = DEFAULT_URL_NGL;
	
	/**
	 * 
	 * @param struc Structure used to create the graph (must have cell info)
	 * @param strucFile Location on the filesystem for the unit cell mmcif file
	 * @param strucURL URI to access strucFile through the browser
	 * @param interfaceIds List of interface numbers, or null for all interfaces
	 * @throws StructureException For errors parsing the structure
	 */
	public LatticeGUIMustache3D(Structure struc,String strucURI,Collection<Integer> interfaceIds) throws StructureException {
		this(MUSTACHE_TEMPLATE_3DMOL,struc,strucURI,interfaceIds);
	}
	public LatticeGUIMustache3D(Structure struc,String strucURI,Collection<Integer> interfaceIds,List<StructureInterface> allInterfaces) throws StructureException {
		this(MUSTACHE_TEMPLATE_3DMOL,struc,strucURI,interfaceIds,allInterfaces);
	}
	public LatticeGUIMustache3D(String template, Structure struc,String strucURI,Collection<Integer> interfaceIds) throws StructureException {
		this(template,struc,strucURI,interfaceIds,null);
	}
	public LatticeGUIMustache3D(String template, LatticeGraph3D graph, String strucURL) throws StructureException {
		super(template, graph);
		this.strucURL = strucURL;
	}
	public LatticeGUIMustache3D(String template, Structure struc,String strucURL,Collection<Integer> interfaceIds,List<StructureInterface> allInterfaces) throws StructureException {
		super(template, struc,interfaceIds, allInterfaces);

		this.strucURL = strucURL;
	}

	/**
	 * get the URL for the CIF structure to write. Defaults to "[PDBID].cif"
	 * @return
	 */
	public String getStrucURL() {
		if(strucURL == null) {
			strucURL = String.format("%s.cif",getPdbId());
		}
		return strucURL;
	}
	public void setStrucURL(String strucURL) {
		this.strucURL = strucURL;
	}
	public String getLibURL() {
		return libURL;
	}
	public void setLibURL(String libURL) {
		this.libURL = libURL;
	}
	public String getDataURL() {
		return dataURL;
	}
	public void setDataURL(String dataURL) {
		this.dataURL = dataURL;
	}
	public static void main(String[] args) throws IOException, StructureException {
		final String usage = String.format(
				"Usage: %s [template] input output [interfaces [cell cellURL]]%n"
				+ "%n"
				+ "Arguments:%n"
				+ "  template    3Dmol, ngl, or the path to a custom template (default ngl)%n"
				+ "  input       PDB code or file%n"
				+ "  output      Path for output HTML file%n"
				+ "  interfaces  comma-separated list of interfaces, or * for all (default *)%n"
				+ "  cell        place to put the unit cell cif.gz file, if required for this%n"
				+ "              template (default don't generate)%n"
				+ "  cellURL     Path to reach cell (uncompressed) in the browser%n"
				+ "",
				LatticeGUIMustache3D.class.getSimpleName());
		if (args.length<1) {
			logger.error("Expected at least 2 arguments");
			logger.error(usage);
			System.exit(1);return;
		}

		// Parse arguments
		int arg = 0;
		
		// Try to match to known template
		String template;
		try {
			template = LatticeGUIMustache.expandTemplatePath(args[arg]);
			arg++;
		} catch(IllegalArgumentException e) {
			template = MUSTACHE_TEMPLATE_3DMOL;
		}
		
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
				interfaceIds = new IntervalSet(interfaceIdsCommaSep).getIntegerSet();
			} catch( NumberFormatException e) {
				logger.error("Invalid interface IDs. Expected comma-separated list, got {}",interfaceIdsCommaSep);
				System.exit(1);return;
			}
		}

		PrintWriter cifOut = null;
		String uri = null;
		String cifFilename = null;
		if(args.length>arg) {
			cifFilename = args[arg++];
			// need both filename and url
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
		} else {
			uri = String.format("rcsb://%s",input);
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

		LatticeGUIMustache3D gui = new LatticeGUIMustache3D(template, struc, uri, interfaceIds);

		if(template.toLowerCase().contains("ngl")) {
			gui.setLibURL(DEFAULT_URL_NGL);
		} else if(template.toLowerCase().contains("3dmol")) {
			gui.setLibURL(DEFAULT_URL_3DMOL);
		} else {
			logger.warn("Unrecognized template, so not setting libURL");
		}
		if(cifOut != null) {
			
			//cifOut.println(struc.toMMCIF());
			gui.writeCIFfile(cifOut);
		}

		gui.execute(htmlOut);

		if( !output.equals("-")) {
			htmlOut.close();
		}
		if( cifOut != null && !cifFilename.equals("-")) {
			cifOut.close();
		}
	}

}
