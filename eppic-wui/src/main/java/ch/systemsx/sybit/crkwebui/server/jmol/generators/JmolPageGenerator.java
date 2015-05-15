package ch.systemsx.sybit.crkwebui.server.jmol.generators;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.sybit.crkwebui.shared.model.Interface;
import ch.systemsx.sybit.crkwebui.shared.model.Residue;
import eppic.PymolRunner;
import eppic.model.ResidueDB;

public class JmolPageGenerator 
{
    /**
     * Generates html page containing jmol aplet.
     * @param title title of the page
     * @param size 
     * @param serverUrl url to the server where jmol and results are stored
     * @param resultsLocation path to results on the server
     * @param fileName name of the pdb file
     * @param pr
     * @param interfData 
     * @return html page with jmol aplet
     */
	public static String generatePage(String title, String size, String serverUrl, String resultsLocation,
			String fileName, PymolRunner pr, Interface interfData)  {
		
		char chain1 = interfData.getChain1().charAt(0);		
		char chain2 = interfData.getChain2().charAt(0); 
		boolean isSymRelated = false;
		
		if (chain1==chain2) {
			isSymRelated = true;
			chain2 = pr.getNextLetter(chain1);
		}
		
		String color1 = pr.getChainColor(chain1, 0, isSymRelated);
		String color2 = pr.getChainColor(chain2, 1, isSymRelated);
		
		//System.out.println("isSymRelated="+isSymRelated+", color1="+color1+" color2="+color2);
		
		List<Residue> coreResidues1 = new ArrayList<Residue>();
		List<Residue> coreResidues2 = new ArrayList<Residue>();
		for (Residue residue:interfData.getResidues() ) {
			
			if (residue.getRegion()==ResidueDB.CORE_EVOLUTIONARY) {
				if (residue.getSide()==1) {
					coreResidues1.add(residue);
				} else if (residue.getSide()==2) {
					coreResidues2.add(residue);
				}
			}
		}
		
		StringBuffer jmolPage = new StringBuffer();

		String fileUrl = serverUrl + "/" + resultsLocation + "/" + fileName;

		jmolPage.append("<html>" + "\n");
		jmolPage.append("<head>" + "\n");
		jmolPage.append("<title>" + "\n");
		jmolPage.append(title);
		jmolPage.append("</title>" + "\n");

		jmolPage.append("<script src=\"http://3Dmol.csb.pitt.edu/build/3Dmol-min.js\"></script> ");

		jmolPage.append("</head>" + "\n");
		jmolPage.append("<body>" + "\n");

		
		jmolPage.append(
				"<div style=\"height: "+size+"px; width: "+size+"px; position: relative;\" "+
				"class='viewer_3Dmoljs' data-href='"+fileUrl+"' data-backgroundcolor='0xffffff' "+
				
				// chain 1
				"data-select1='chain:"+chain1+"' "+
				"data-style1='cartoon:color="+color1+"'"+

				// chain 2
				"data-select2='chain:"+chain2+"' "+
				"data-style2='cartoon:color="+color2+"'"+
				
				// core residues 1
				"data-select3='resi:"+getCommaSeparatedList(coreResidues1)+";chain:"+chain1+"'"+
				"data-style3='cartoon:color="+color1+";stick:color=red'"+
				
				// core residues 1
				"data-select4='resi:"+getCommaSeparatedList(coreResidues2)+";chain:"+chain2+"'"+
				"data-style4='cartoon:color="+color2+";stick:color=raspberry'"+

				
				"</div>");
		
		jmolPage.append("</body>" + "\n");
		jmolPage.append("</html>" + "\n");

		return jmolPage.toString();
    }
	
	private static String getCommaSeparatedList(List<Residue> residues) {
		StringBuilder sb = new StringBuilder();
		for (int i=0;i<residues.size();i++){
			sb.append(residues.get(i).getPdbResidueNumber());
			if (i!=residues.size()-1) sb.append(',');
		}
		return sb.toString();
	}
}
