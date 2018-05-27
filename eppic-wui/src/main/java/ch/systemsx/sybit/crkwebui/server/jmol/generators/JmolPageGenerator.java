package ch.systemsx.sybit.crkwebui.server.jmol.generators;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eppic.dtomodel.Assembly;
import eppic.dtomodel.Interface;
import eppic.dtomodel.Residue;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

import eppic.EppicParams;
import eppic.MolViewersHelper;
import eppic.assembly.gui.LatticeGUIMustache;
import eppic.model.ResidueBurialDB;

/**
 * Generate 3D molecular viewer pages. Note that the "Jmol" title is historical;
 * structures are currently displayed using NGL.
 *
 */
public class JmolPageGenerator 
{
	private static final Logger logger = LoggerFactory.getLogger(JmolPageGenerator.class);
	private static final String EPPIC_NGL_JS_FUNCTIONS = "resources/js/nglhelper.js";
	private static final String TEMPLATE_NGL = "ngl.html.mustache";
	private static final String CSSFILE = "eppic-static.css";
	
    /**
     * Generates html page containing the 3D viewer.
     * @param title title of the page
     * @param size 
     * @param serverUrl url to the server where jmol and results are stored
     * @param resultsLocation path to results on the server
     * @param fileName name of the cif file
     * @param interfData 
     * @param nglJsUrl
     * @param webappRoot
     * @return html page with jmol aplet
     */
	public static void generatePage(String title, String size, String serverUrl, String resultsLocation,
			String fileName, Interface interfData, Assembly assemblyData, String nglJsUrl, PrintWriter out, String webappRoot)  {
		
		
		boolean isCif = true;
		if (fileName.endsWith("cif")) {
			isCif = true;
		} else if (fileName.endsWith("pdb")) {
			isCif = false;
		}
		
		String jsVariables = generateSelectionVarsNgl(interfData, assemblyData, isCif);
		
		// we assume that the alphabet is the default (since in wui there's no way that user can change it)
		double maxEntropy = Math.log(EppicParams.DEF_ENTROPY_ALPHABET.getNumLetters()) / Math.log(2);

		if (!resultsLocation.startsWith("/"))
			resultsLocation = "/" + resultsLocation;
		String fileUrl = serverUrl + resultsLocation + "/" + fileName;

		Map<String,Object> page = new HashMap<>();
		page.put("title", title);
		page.put("libURL", nglJsUrl);
		if (!webappRoot.endsWith("/")) webappRoot = webappRoot + "/"; 
		page.put("jsURL", webappRoot + EPPIC_NGL_JS_FUNCTIONS);
		page.put("cssUrl", webappRoot + CSSFILE);
		page.put("fileURL", fileUrl);
		page.put("jsVariables", jsVariables);
		page.put("maxEntropy", String.format("%.4f",maxEntropy));
		page.put("size", size);

		MustacheFactory mf = new DefaultMustacheFactory();
		String template = LatticeGUIMustache.expandTemplatePath(TEMPLATE_NGL);
		Mustache mustache = mf.compile(template);

		try {
			mustache.execute(out, page)
				.flush();
		} catch (IOException e) {
			logger.error("Error generating output from template "+template,e);
		}
    }
	
	@SuppressWarnings("unused")
	private static String getCommaSeparatedList(List<Residue> residues) {
		StringBuilder sb = new StringBuilder();
		for (int i=0;i<residues.size();i++){
			sb.append(residues.get(i).getPdbResidueNumber());
			if (i!=residues.size()-1) sb.append(',');
		}
		return sb.toString();
	}
	
	private static String getOrSeparatedList(List<Residue> residues, String chain) {
		
		// TODO we should use range selections (with hyphens) to get better performance and shorter strings
		
		StringBuilder sb = new StringBuilder();

		for (int i=0;i<residues.size();i++){
			
			String pdbResNum = residues.get(i).getPdbResidueNumber();
			// in some cases (e.g. 1pmo chain B we have null PdbResidueNumber for some residues 
			// because of the way biojava is mapping the residues (in 1pmo residues 4-11 exist in chain B but are missing in chain A)
			// until we get a better solution in biojava, we need to skip that here so that we don't get a null pointer
			if (pdbResNum==null) {
				continue;
			}
			
			if (sb.length()!=0) sb.append(" or ");
			
			if (!Character.isDigit(pdbResNum.charAt(pdbResNum.length()-1)) ) {
				// in ngl insertion codes are specified with a "^", see https://github.com/arose/ngl/issues/19
				pdbResNum = pdbResNum.substring(0, pdbResNum.length()-1) + "^" + pdbResNum.charAt(pdbResNum.length()-1); 
			}
			sb.append(pdbResNum+":"+chain);
			
		}
		return sb.toString();
	}
	
	private static String generateSelectionVarsNgl(Interface interfData, Assembly assemblyData, boolean isCif) {
		
		List<String> chains = new ArrayList<>();
		String coreRimSelectionVarsStr = "";
		
		if (interfData != null) {
			String chain1 = interfData.getChain1();		
			String chain2 = interfData.getChain2(); 
			///boolean isSymRelated = false;
			

			if (chain1.equals(chain2)) {
				//isSymRelated = true;
				if (isCif) {
					// exactly as done in StructureInterface.toMMCIF()
					chain2 = chain2 +"_"+ interfData.getOperatorId();
				} else {
					// exactly as done in StructureInterface.toPDB()
					// NOTE this won't work with chain ids of more than 1 char
					chain2 = Character.toString(MolViewersHelper.getNextLetter(chain1.charAt(0)));
				}
			}
			
			chains.add(chain1);
			chains.add(chain2);

			String color1 = MolViewersHelper.getHexChainColor(chain1, true);
			String color2 = MolViewersHelper.getHexChainColor(chain2, true);

			String colorCore1 = MolViewersHelper.getHexInterf1Color(true);
			String colorCore2 = MolViewersHelper.getHexInterf2Color(true);

			List<Residue> coreResidues1 = new ArrayList<Residue>();
			List<Residue> rimResidues1 = new ArrayList<Residue>();
			List<Residue> coreResidues2 = new ArrayList<Residue>();
			List<Residue> rimResidues2 = new ArrayList<Residue>();

			for (Residue residue:interfData.getResidues() ) {

				if (residue.getRegion()==ResidueBurialDB.CORE_EVOLUTIONARY || residue.getRegion()==ResidueBurialDB.CORE_GEOMETRY) {
					if (residue.getSide()==false) {
						coreResidues1.add(residue);
					} else if (residue.getSide()==true) {
						coreResidues2.add(residue);
					}
				} else if (residue.getRegion()==ResidueBurialDB.RIM_EVOLUTIONARY) {
					if (residue.getSide()==false) {
						rimResidues1.add(residue);
					} else if (residue.getSide()==true) {
						rimResidues2.add(residue);
					}
				}
			}
			
			String seleCore1VarStr = getSeleVarStr("seleCore1", coreResidues1, chain1);
			String seleCore2VarStr = getSeleVarStr("seleCore2", coreResidues2, chain2);
			String seleRim1VarStr = getSeleVarStr("seleRim1", rimResidues1, chain1);
			String seleRim2VarStr = getSeleVarStr("seleRim2", rimResidues2, chain2);

			// note that variable names must match those in file EPPIC_NGL_JS_FUNCTIONS
			coreRimSelectionVarsStr = 				
					"var colorCore1 = \""+colorCore1+"\";\n" + 
					"var colorCore2 = \""+colorCore2+"\";\n" + 
					seleCore1VarStr + 
					seleCore2VarStr + 
					"var colorRim1  = \""+color1+"\";\n" + 
					"var colorRim2  = \""+color2+"\";\n" + 
					seleRim1VarStr + 
					seleRim2VarStr ; 

		}
		
		
		if (assemblyData!=null) {
			
			String chainIdsString = assemblyData.getChainIdsString();
			
			if (chainIdsString!=null) {				
				String[] chainIds = chainIdsString.split(",");
				chains = Arrays.asList(chainIds);
			}
		} 
		// else it stays empty and then the array var is empty
		
		// note that variable names must match those in file EPPIC_NGL_JS_FUNCTIONS
		return 
				"var chains     = " + toJsArray(chains) + ";\n" +
				"var colors     = " + toJsArray(getColorsForChains(chains)) + ";\n" +
				coreRimSelectionVarsStr;


	}
	
	private static List<String> getColorsForChains(List<String> chains) {
		List<String> colors = new ArrayList<>();
		for (String chain:chains) {
			colors.add(MolViewersHelper.getHexChainColor(chain, true));
		}
		
		return colors;
	}
	
	private static String toJsArray(List<String> list) {
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		for (int i = 0; i<list.size(); i++) {		
			if (i!=0) sb.append(", ");
			sb.append("\""+list.get(i)+"\"");
		}
		sb.append("]");
		return sb.toString();
		
	}
	
	private static String getSeleVarStr(String varName, List<Residue> residues, String chain) {
		if (residues==null || residues.isEmpty()) return "";
		
		//return "var "+varName+"  = \"("+getOrSeparatedList(residues, chain) + ") and (sidechain or .CA)\";\n";
		return "var "+varName+"  = \"("+getOrSeparatedList(residues, chain) + ") and (sidechainAttached)\";\n";
	}
	
	 
}
