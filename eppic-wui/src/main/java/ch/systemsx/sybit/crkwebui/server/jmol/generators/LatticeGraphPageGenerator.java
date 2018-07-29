package ch.systemsx.sybit.crkwebui.server.jmol.generators;

import java.io.IOException;
import java.io.PrintWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.google.gson.Gson;

import eppic.assembly.LatticeGraph3D;
import eppic.assembly.gui.LatticeGUIMustache;

/**
 * Helper class to generate the LatticeGraph HTML
 * @author Spencer Bliven
 *
 */
public class LatticeGraphPageGenerator {
	private static final Logger logger = LoggerFactory.getLogger(LatticeGraphPageGenerator.class);

	public static final String TEMPLATE_LATTICE_GUI_NGL_LAZY = "LatticeGUINglLazy.html.mustache";

	/**
	 * Generates html page containing the NGL canvas.
	 * 
	 * @param strucURI URL to reach auCifFile within the browser
	 * @param title Page title [default: structure name]
	 * @param size the canvas size
	 * @param jsonURL path to the json dataURL
	 * @param out Stream to output the HTML page
	 * @param urlMolViewer path to the libURL
	 * @param webappRoot
	 * @throws IOException For errors reading or writing files
	 */
	public static void generateHTMLPage(String strucURI, String title, String size,
										String jsonURL, PrintWriter out, String urlMolViewer, String webappRoot) throws IOException {

		MustacheFactory mf = new DefaultMustacheFactory();
		String template = LatticeGUIMustache.expandTemplatePath(TEMPLATE_LATTICE_GUI_NGL_LAZY);
		Mustache mustache = mf.compile(template);
		LazyLatticeGUIMustache3D page = new LazyLatticeGUIMustache3D();
		page.setSize(size);
		page.setTitle(title);
		page.setDataURL(jsonURL);
		page.setLibURL(urlMolViewer);
		page.setStrucURL(strucURI);
		page.setWebappRoot(webappRoot);
		try {
			mustache.execute(out, page)
				.flush();
		} catch (IOException e) {
			logger.error("Error generating output from template "+template,e);
			throw e;
		}
	}

}
