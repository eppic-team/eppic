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

	static final String MUSTACHE_TEMPLATE_3DMOL = "mustache/eppic/assembly/gui/LatticeGUI3Dmol.html.mustache";
	static final String DEFAULT_URL_3DMOL = "https://cdn.rawgit.com/arose/ngl/v0.7.1a/js/build/ngl.embedded.min.js";
	
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
		this(MUSTACHE_TEMPLATE_3DMOL,struc,strucURI,interfaceIds);
	}
	public LatticeGUI3Dmol(Structure struc,String strucURI,Collection<Integer> interfaceIds,List<StructureInterface> allInterfaces) throws StructureException {
		this(MUSTACHE_TEMPLATE_3DMOL,struc,strucURI,interfaceIds,allInterfaces);
	}
	public LatticeGUI3Dmol(String template, Structure struc,String strucURI,Collection<Integer> interfaceIds) throws StructureException {
		this(template,struc,strucURI,interfaceIds,null);
	}
	public LatticeGUI3Dmol(String template, LatticeGraph3D graph, String strucURI) throws StructureException {
		super(template, graph);
		this.strucURI = strucURI;
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
		final String usage = String.format("Usage: %s [template] <PDB code or file> <output.html> [list,of,interfaces,or,* [<output.cif.gz> <http://path/to/cif>]]",LatticeGUI3Dmol.class.getSimpleName());
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
		AtomCache cache = new AtomCache();
		cache.getFileParsingParams().setAlignSeqRes(true);
		Structure struc = cache.getStructure(input);

		LatticeGUI3Dmol gui = new LatticeGUI3Dmol(template, struc, uri, interfaceIds);

		if(cifOut != null) {
			
			cifOut.println(struc.toMMCIF());
			//gui.writeCIFfile(cifOut);
			
		}

		gui.execute(htmlOut);

		if( !output.equals("-")) {
			htmlOut.close();
		}
	}

}
