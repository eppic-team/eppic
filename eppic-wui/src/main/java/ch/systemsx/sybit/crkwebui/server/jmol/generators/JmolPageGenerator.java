package ch.systemsx.sybit.crkwebui.server.jmol.generators;

import ch.systemsx.sybit.crkwebui.shared.model.Interface;
import eppic.PymolRunner;

public class JmolPageGenerator 
{
    /**
     * Generates html page containing jmol aplet.
     * @param title title of the page
     * @param size size of aplet
     * @param serverUrl url to the server where jmol and results are stored
     * @param resultsLocation path to results on the server
     * @param fileName name of the pdb file
     * @param jmolScript script to execute
     * @param version 
     * @return html page with jmol aplet
     */
	public static String generatePage(String title, String size, String serverUrl, String resultsLocation,
			String fileName, PymolRunner pr, String version, Interface interfData)  {
		

		boolean isSymRelated = interfData.getChain1().equals(interfData.getChain2());		
		String color1 = pr.getChainColor(interfData.getChain1().charAt(0), 0, isSymRelated);
		String color2 = pr.getChainColor(interfData.getChain2().charAt(0), 1, isSymRelated);
		
		System.out.println("isSymRelated="+isSymRelated+", color1="+color1+" color2="+color2);
		
		//String color1 = "cyan";
		//String color2 = "green";
		
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
				"data-select1='chain:"+interfData.getChain1()+"' "+
				"data-style1='cartoon:color="+color1+"'"+

				// chain 2
				"data-select2='chain:"+interfData.getChain2()+"' "+
				"data-style2='cartoon:color="+color2+"'"+
						
				"</div>");
		jmolPage.append("</body>" + "\n");
		jmolPage.append("</html>" + "\n");

		return jmolPage.toString();
    }
}
