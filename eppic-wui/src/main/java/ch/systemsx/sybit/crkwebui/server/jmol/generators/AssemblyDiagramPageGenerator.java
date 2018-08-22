package ch.systemsx.sybit.crkwebui.server.jmol.generators;

import java.io.IOException;
import java.io.PrintWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

import eppic.assembly.gui.LatticeGUIMustache;


/**
 * Helper class to generate the LatticeGraph HTML
 * @author Spencer Bliven
 *
 */
public class AssemblyDiagramPageGenerator {
	private static final Logger logger = LoggerFactory.getLogger(AssemblyDiagramPageGenerator.class);
	
	// note that this template is in eppic-wui/src/main/resources
	public static final String TEMPLATE_ASSEMBLY_DIAGRAM_FULL_LAZY = "AssemblyDiagramFullLazy.html.mustache";
	
	public static void generateHTMLPage(  
			String title, String size, String jsonURL, PrintWriter out, String webappRoot) throws IOException {

		MustacheFactory mf = new DefaultMustacheFactory();
		String template = LatticeGUIMustache.expandTemplatePath(TEMPLATE_ASSEMBLY_DIAGRAM_FULL_LAZY);
		Mustache mustache = mf.compile(template);
		LazyLatticeGUIMustache3D page = new LazyLatticeGUIMustache3D();
		page.setSize(size);
		page.setTitle(title);
		page.setDataURL(jsonURL);
		page.setWebappRoot(webappRoot);
		try {
			mustache.execute(out, page).flush();
		} catch (IOException e) {
			logger.error("Error generating output from template "+template,e);
			throw e;
		}
	}

}
