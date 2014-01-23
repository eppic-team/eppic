package ch.systemsx.sybit.crkwebui.server.jmol.generators;

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
	 * @return html page with jmol aplet
	 */
	public static String generatePage(String title,
									  String size,
									  String serverUrl,
									  String resultsLocation,
									  String fileName,
									  String jmolScript)
	{
		jmolScript = jmolScript.replaceAll("\n", "");
		
		StringBuffer jmolPage = new StringBuffer();
		
		jmolPage.append("<html>" + "\n");
		jmolPage.append("<head>" + "\n");
		jmolPage.append("<title>" + "\n");
		jmolPage.append(title);
		jmolPage.append("</title>" + "\n");
		jmolPage.append("<script type=\"text/javascript\" language=\"javascript\" src=\"" + 
			serverUrl + "/resources/jmol/Jmol.js\"></script>" + "\n");
		jmolPage.append("</head>" + "\n");
		jmolPage.append("<body>" + "\n");
		jmolPage.append("<script>" + "\n");
		jmolPage.append("jmolInitialize(\"" + serverUrl + "/resources/jmol\");" + "\n");
		jmolPage.append("jmolSetCallback(\"language\", \"en\");" + "\n");
		jmolPage.append("jmolApplet(" + size + ", 'load " + serverUrl + "/" + resultsLocation + "/" + fileName + "; "+ jmolScript + "');" + "\n");
		jmolPage.append("</script>" + "\n");
		jmolPage.append("</body>" + "\n");
		jmolPage.append("</html>" + "\n");
		
		return jmolPage.toString();
	}
}
