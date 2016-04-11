package ch.systemsx.sybit.crkwebui.server.jmol.generators;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

import ch.systemsx.sybit.crkwebui.shared.model.Assembly;
import ch.systemsx.sybit.crkwebui.shared.model.AssemblyContent;
import ch.systemsx.sybit.crkwebui.shared.model.Interface;
import ch.systemsx.sybit.crkwebui.shared.model.Residue;
import eppic.model.ResidueBurialDB;

public class TestJmolPageGenerator {

	@Test
	public void testJmolPageGenerator() {
		
		List<Residue> residues = new ArrayList<>();
		
		Residue res1 = new Residue();
		Residue res2 = new Residue();
		Residue res3 = new Residue();
		Residue res4 = new Residue();
		res1.setPdbResidueNumber("1");
		res1.setSide(false);
		res2.setPdbResidueNumber("2A"); // an insertion code (should be represented as 2^A)
		res2.setSide(false);
		res3.setPdbResidueNumber("3");
		res3.setSide(false);
		res4.setPdbResidueNumber("4");
		res4.setSide(false);
		res1.setRegion(ResidueBurialDB.CORE_GEOMETRY);
		res2.setRegion(ResidueBurialDB.CORE_GEOMETRY); 
		res3.setRegion(ResidueBurialDB.RIM_EVOLUTIONARY);
		res4.setRegion(ResidueBurialDB.RIM_EVOLUTIONARY);
		
		Residue res5 = new Residue();
		Residue res6 = new Residue();
		res5.setPdbResidueNumber("5");
		res5.setSide(true);
		res6.setPdbResidueNumber("6");
		res6.setSide(true);
		res5.setRegion(ResidueBurialDB.CORE_GEOMETRY);
		res6.setRegion(ResidueBurialDB.CORE_GEOMETRY);
		
		
		residues.add(res1);
		residues.add(res2);
		residues.add(res3);
		residues.add(res4);
		residues.add(res5);
		residues.add(res6);
		
		Interface interfData = new Interface();
		interfData.setChain1("A"); 
		interfData.setChain2("B");		
		interfData.setResidues(residues);
		
		
		AssemblyContent ac = new AssemblyContent();
		ac.setChainIds("A_0,B_0,A_1,B_1");
		Assembly assemblyData = new Assembly();
		List<AssemblyContent> acs = new ArrayList<>();
		acs.add(ac);
		assemblyData.setAssemblyContents(acs); 
		
		
		String thepage = JmolPageGenerator.generatePage("test title", "200", "http://myserver", "../files", "1smt.cif", interfData, assemblyData, "/ngl.embedded.min.js");
		
		
		System.out.println(thepage);
		
		// checking that nothing is null
		assertFalse(thepage.contains("null"));
		
		

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
		p = Pattern.compile(".*or 2\\^A\\).*", Pattern.DOTALL);
		m = p.matcher(thepage);
		assertTrue(m.matches());
		
	}

}
