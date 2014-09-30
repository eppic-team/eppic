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
     * @param version 
     * @return html page with jmol aplet
     */
    public static String generatePage(String title, String size, String serverUrl, String resultsLocation,
	    String fileName, String jmolScript, String version)  {
	jmolScript = jmolScript.replaceAll("\n", "");

	StringBuffer jmolPage = new StringBuffer();

	jmolPage.append("<html>" + "\n");
	jmolPage.append("<head>" + "\n");
	jmolPage.append("<title>" + "\n");
	jmolPage.append(title);
	jmolPage.append("</title>" + "\n");
	jmolPage.append("<script type=\"text/javascript\" language=\"javascript\" src=\"" + 
		serverUrl + "/resources/jmol/JSmol.min.js\"></script>" + "\n");
	if(version != null && version.equals("js"))
	    jmolPage.append("<script type=\"text/javascript\" language=\"javascript\" src=\"" + 
		serverUrl + "/resources/jmol/Jmol2js.js\"></script>" + "\n");
	else {
	    jmolPage.append("<script type=\"text/javascript\" language=\"javascript\" src=\"https://www.java.com/js/deployJava.js\"></script>" + "\n");
	    jmolPage.append("<script type=\"text/javascript\" language=\"javascript\">"
	    	+ " function checkJava() {"
	    	+ "	if(deployJava.getJREs().length == 0) { window.location = document.URL + '&version=js'; } }"
	    	+ "</script>");
	    jmolPage.append("<script type=\"text/javascript\" language=\"javascript\" src=\"" + 
		serverUrl + "/resources/jmol/Jmol2.js\"></script>" + "\n");
	}
	jmolPage.append("</head>" + "\n");
	jmolPage.append("<body onload=\"checkJava();\">" + "\n");
	jmolPage.append("<script>" + "\n");
	jmolPage.append("jmolInitialize(\"" + serverUrl + "/resources/jmol\");" + "\n");
	jmolPage.append("jmolSetCallback(\"language\", \"en\");" + "\n");
	if(version != null && version.equals("js"))
	    jmolPage.append("jmolApplet(" + size + ", 'load " + serverUrl + "/" + resultsLocation + "/" + fileName + "; "+ jmolScript + "');" + "\n");
	else
	    jmolPage.append("jmolApplet(" + size + ", 'load " + serverUrl + "/" + resultsLocation + "/" + fileName + ".gz; "+ jmolScript + "');" + "\n");
	jmolPage.append("</script>" + "\n");
	jmolPage.append("</body>" + "\n");
	jmolPage.append("</html>" + "\n");

	return jmolPage.toString();
    }
}
