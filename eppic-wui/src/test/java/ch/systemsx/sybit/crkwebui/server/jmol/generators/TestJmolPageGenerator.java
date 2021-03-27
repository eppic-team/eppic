package ch.systemsx.sybit.crkwebui.server.jmol.generators;

import static org.junit.Assert.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eppic.model.db.AssemblyContentDB;
import eppic.model.db.AssemblyDB;
import eppic.model.db.InterfaceDB;
import eppic.model.db.ResidueInfoDB;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eppic.model.db.ResidueBurialDB;

public class TestJmolPageGenerator {
	private static final Logger logger = LoggerFactory.getLogger(TestJmolPageGenerator.class);

	@Test
	public void testJmolPageGenerator() {
		
		List<ResidueBurialDB> residues = new ArrayList<>();
		
		ResidueBurialDB res1 = new ResidueBurialDB();
		ResidueBurialDB res2 = new ResidueBurialDB();
		ResidueBurialDB res3 = new ResidueBurialDB();
		ResidueBurialDB res4 = new ResidueBurialDB();
		ResidueBurialDB res7 = new ResidueBurialDB();

		// FIXME after rewrite
		//res1.setPdbResidueNumber("1");
		res1.setSide(false);
		//res2.setPdbResidueNumber("2A"); // an insertion code (should be represented as 2^A)
		res2.setSide(false);
		//res3.setPdbResidueNumber("3");
		res3.setSide(false);
		//res4.setPdbResidueNumber("4");
		res4.setSide(false);
		//res7.setPdbResidueNumber(null);
		res7.setSide(false);
		res1.setRegion(ResidueBurialDB.CORE_GEOMETRY);
		res2.setRegion(ResidueBurialDB.CORE_GEOMETRY); 
		res3.setRegion(ResidueBurialDB.RIM_EVOLUTIONARY);
		res4.setRegion(ResidueBurialDB.RIM_EVOLUTIONARY);
		res4.setRegion(ResidueBurialDB.RIM_EVOLUTIONARY);
		res7.setRegion(ResidueBurialDB.RIM_EVOLUTIONARY);

		// FIXME after rewrite
		ResidueBurialDB res5 = new ResidueBurialDB();
		ResidueBurialDB res6 = new ResidueBurialDB();
		//res5.setPdbResidueNumber("5");
		res5.setSide(true);
		//res6.setPdbResidueNumber("6");
		res6.setSide(true);
		res5.setRegion(ResidueBurialDB.CORE_GEOMETRY);
		res6.setRegion(ResidueBurialDB.CORE_GEOMETRY);
		
		residues.add(res1);
		residues.add(res2);
		residues.add(res3);
		residues.add(res4);
		residues.add(res5);
		residues.add(res6);
		residues.add(res7);
		
		InterfaceDB interfData = new InterfaceDB();
		interfData.setChain1("A"); 
		interfData.setChain2("B");		
		interfData.setResidueBurials(residues);
		
		
		AssemblyContentDB ac = new AssemblyContentDB();
		ac.setChainIds("A_0,B_0,A_1,B_1");
		AssemblyDB assemblyData = new AssemblyDB();
		List<AssemblyContentDB> acs = new ArrayList<>();
		acs.add(ac);
		assemblyData.setAssemblyContents(acs); 
		
		StringWriter out = new StringWriter();
		try( PrintWriter pw = new PrintWriter(out) ) {
			JmolPageGenerator.generatePage("test title", "200", "1smt", "http://localhost:8080/", "1smt.cif", interfData, assemblyData,
					"/ngl.embedded.min.js",pw, "/");
		}
		out.flush();
		String thepage = out.toString();
		
		
		logger.debug("Generated JmolPage HTML:\n{}",thepage);
		
		// checking that nothing is null
		assertFalse(thepage.contains("null"));

		assertFalse(thepage.contains("&amp;"));
		
		

		// checking that we have no empty residue selections
		Pattern p = Pattern.compile(".*\":\\w.*", Pattern.DOTALL);
		Matcher m = p.matcher(thepage);
		
		assertFalse(m.matches());
		
		//thepage = "some \ngarbage \":A some more\n gargabe";
		
		// checking that the color strings are really 6 chars + the hash
		p = Pattern.compile("\"(#\\w+)\"", Pattern.DOTALL);
		m = p.matcher(thepage);
		while (m.find()) {
			String colorStr = m.group(1);
			//System.out.println(colorStr);
			assertEquals(7, colorStr.length());
			assertEquals('#', colorStr.charAt(0));
		}
		
		
		// checking that no string has 0x prefixing it (to make sure that colors are prefixed with # and not with 0x)
		assertFalse(thepage.contains("0x"));
		
		// checking that the insertion code is represented like "2^A"		
		p = Pattern.compile(".*or 2\\^A.*", Pattern.DOTALL);
		m = p.matcher(thepage);
		assertTrue(m.matches());
		
	}

}
