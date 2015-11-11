package ch.systemsx.sybit.crkwebui.client.commons.managers;

import java.util.List;

import ch.systemsx.sybit.crkwebui.client.commons.appdata.ApplicationContext;
import ch.systemsx.sybit.crkwebui.server.files.downloader.servlets.FileDownloadServlet;
import ch.systemsx.sybit.crkwebui.server.jmol.servlets.LatticeGraphServlet;
import ch.systemsx.sybit.crkwebui.shared.model.Assembly;
import ch.systemsx.sybit.crkwebui.shared.model.Interface;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;

/**
 * 3D viewer runner.
 */
public class DiagramViewerRunner
{
	/**
	 * Shows the lattice graph viewer
	 * @param assemblyId assembly to consider
	 */
	public static void runViewerAssembly(String assemblyId)
	{
		String interfaceids = "";
		List<Assembly> assemblies = ApplicationContext.getPdbInfo().getAssemblies();
		for(Assembly a : assemblies){
			if((a.getId()+"").equals(assemblyId)){
				List<Interface> interfaces = a.getInterfaces();
				for(Interface i : interfaces){
					interfaceids+=i.getInterfaceId() + ",";
				}
			}
		}
		if(interfaceids.length()>0)
			interfaceids = interfaceids.substring(0,interfaceids.length()-1);

		String url = GWT.getModuleBaseURL() + LatticeGraphServlet.SERVLET_NAME;
		url +=  "?" + FileDownloadServlet.PARAM_ID + "=" + ApplicationContext.getPdbInfo().getJobId() +
				"&" + LatticeGraphServlet.PARAM_INTERFACES + "=" + interfaceids;

		if(!interfaceids.equals("")) {
			int size = Math.min( ApplicationContext.getWindowData().getWindowHeight() - 60,
					ApplicationContext.getWindowData().getWindowWidth() - 60);
			Window.open(url,"_blank","width="+size+",height="+size);
		}
	}

}
