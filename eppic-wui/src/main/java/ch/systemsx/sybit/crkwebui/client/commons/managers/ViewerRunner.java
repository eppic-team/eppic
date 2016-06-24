package ch.systemsx.sybit.crkwebui.client.commons.managers;

import ch.systemsx.sybit.crkwebui.client.commons.appdata.AppPropertiesManager;
import ch.systemsx.sybit.crkwebui.client.commons.appdata.ApplicationContext;
import ch.systemsx.sybit.crkwebui.client.commons.events.ShowErrorEvent;
import ch.systemsx.sybit.crkwebui.server.files.downloader.servlets.FileDownloadServlet;
import ch.systemsx.sybit.crkwebui.server.jmol.servlets.JmolViewerServlet;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;

/**
 * 3D viewer runner.
 */
public class ViewerRunner 
{
	
	private static final int VIEWER_SIZE_OFFSET = 40;
	
	/**
	 * Triggers selected action (viewer) when in interface results panel. 
	 * The type of the action (viewer) is determined based on the option selected in viewer selector.
	 * @param interfaceId identifier of the interface
	 */
	public static void runViewer(String interfaceId)
	{
		if(ApplicationContext.getSelectedViewer().equals(AppPropertiesManager.CONSTANTS.viewer_jmol()))
		{
			showJmolViewer(interfaceId);
		}
		//case that PDB viewer selected 
		else if(ApplicationContext.getSelectedViewer().equals(AppPropertiesManager.CONSTANTS.viewer_local()))
		{
			downloadFileFromServer(FileDownloadServlet.TYPE_VALUE_INTERFACE, interfaceId, FileDownloadServlet.COORDS_FORMAT_VALUE_CIF);
		}
		else
		{
			EventBusManager.EVENT_BUS.fireEvent(new ShowErrorEvent("No viewer selected"));
		}
	}

	/**
	 * Displays 3D viewer for the given interface id.
	 * @param interfaceId interface identifier
	 */
	private static void showJmolViewer(String interfaceId)
	{
		int size = ApplicationContext.getWindowData().getWindowHeight() - 60;
		if(size > ApplicationContext.getWindowData().getWindowWidth() - 60)
		{
			size = ApplicationContext.getWindowData().getWindowWidth() - 60;
		}
		
		int jmolAppletSize = size - VIEWER_SIZE_OFFSET;
		
		// note we have set the default format to CIF - JD 2015-06-15
		
		String jmolViewerUrl = GWT.getModuleBaseURL() + JmolViewerServlet.SERVLET_NAME;
		jmolViewerUrl += "?"+FileDownloadServlet.PARAM_ID+"=" + ApplicationContext.getPdbInfo().getJobId() + 
						 "&"+FileDownloadServlet.PARAM_TYPE + "=" + FileDownloadServlet.TYPE_VALUE_INTERFACE + 
						 "&"+JmolViewerServlet.PARAM_INPUT+"=" + ApplicationContext.getPdbInfo().getTruncatedInputName() + 
						 "&"+FileDownloadServlet.PARAM_INTERFACE_ID+"=" + interfaceId +
						 "&"+FileDownloadServlet.PARAM_COORDS_FORMAT+"=" + FileDownloadServlet.COORDS_FORMAT_VALUE_CIF+
						 "&"+JmolViewerServlet.PARAM_SIZE+"=" + jmolAppletSize;
		
		//this opens the viewer in a popup window of fixed dimensions and it is not possible to modify the URL
		//Window.open(jmolViewerUrl, "", "width=" + size + "," + "height=" + size);
		
		//this opens the viewer in a new tab
		Window.open(jmolViewerUrl,"_blank","");

	}

	/**
	 * Displays 3D viewer for the given assembly id.
	 * @param assemblyId assembly identifier
	 */
	private static void showJmolViewerAssembly(String assemblyId)
	{
		int size = ApplicationContext.getWindowData().getWindowHeight() - 60;
		if(size > ApplicationContext.getWindowData().getWindowWidth() - 60)
		{
			size = ApplicationContext.getWindowData().getWindowWidth() - 60;
		}
		
		int jmolAppletSize = size - VIEWER_SIZE_OFFSET;
		
		// note we have set the default format to CIF - JD 2015-06-15
		
		String jmolViewerUrl = GWT.getModuleBaseURL() + JmolViewerServlet.SERVLET_NAME;
		jmolViewerUrl += "?"+FileDownloadServlet.PARAM_ID+"=" + ApplicationContext.getPdbInfo().getJobId() + 
						 "&"+FileDownloadServlet.PARAM_TYPE + "=" + FileDownloadServlet.TYPE_VALUE_ASSEMBLY + 
						 "&"+JmolViewerServlet.PARAM_INPUT+"=" + ApplicationContext.getPdbInfo().getTruncatedInputName() + 
						 "&"+FileDownloadServlet.PARAM_ASSEMBLY_ID+"=" + assemblyId +
						 "&"+FileDownloadServlet.PARAM_COORDS_FORMAT+"=" + FileDownloadServlet.COORDS_FORMAT_VALUE_CIF+
						 "&"+JmolViewerServlet.PARAM_SIZE+"=" + jmolAppletSize;
		
		//this opens the viewer in a popup window of fixed dimensions and it is not possible to modify the URL
		//Window.open(jmolViewerUrl, "", "width=" + size + "," + "height=" + size);

		//this opens the viewer in a new tab
		Window.open(jmolViewerUrl,"_blank","");

	}
	
	/**
	 * This method is not currently used - it is intended to be separate  
	 * for when ShowAssemblyViewerInNewTabEvent is triggered by pressing the shift key.
	 * @param assemblyId
	 */
	private static void showJmolViewerAssemblyInNewTab(String assemblyId)
	{
		int size = ApplicationContext.getWindowData().getWindowHeight() - 60;
		if(size > ApplicationContext.getWindowData().getWindowWidth() - 60)
		{
			size = ApplicationContext.getWindowData().getWindowWidth() - 60;
		}
		
		int jmolAppletSize = size - VIEWER_SIZE_OFFSET;
		
		// note we have set the default format to CIF - JD 2015-06-15
		
		String jmolViewerUrl = GWT.getModuleBaseURL() + JmolViewerServlet.SERVLET_NAME;
		jmolViewerUrl += "?"+FileDownloadServlet.PARAM_ID+"=" + ApplicationContext.getPdbInfo().getJobId() + 
						 "&"+FileDownloadServlet.PARAM_TYPE + "=" + FileDownloadServlet.TYPE_VALUE_ASSEMBLY + 
						 "&"+JmolViewerServlet.PARAM_INPUT+"=" + ApplicationContext.getPdbInfo().getTruncatedInputName() + 
						 "&"+FileDownloadServlet.PARAM_ASSEMBLY_ID+"=" + assemblyId +
						 "&"+FileDownloadServlet.PARAM_COORDS_FORMAT+"=" + FileDownloadServlet.COORDS_FORMAT_VALUE_CIF+
						 "&"+JmolViewerServlet.PARAM_SIZE+"=" + jmolAppletSize;

		Window.open(jmolViewerUrl,"_blank","");

	}
	
	
	/**
	 * Triggers selected action (viewer) when in assembly results panel. 
	 * The type of the action (viewer) is determined based on the option selected in viewer selector.
	 * @param assemblyId identifier of the assembly
	 */
	public static void runViewerAssembly(String assemblyId)
	{
		if(ApplicationContext.getSelectedViewer().equals(AppPropertiesManager.CONSTANTS.viewer_jmol()))
		{
			showJmolViewerAssembly(assemblyId);
		}
		//mmCIF CASE (was PDB)
		else if(ApplicationContext.getSelectedViewer().equals(AppPropertiesManager.CONSTANTS.viewer_local()))
		{
			downloadAssemblyFileFromServer(FileDownloadServlet.TYPE_VALUE_ASSEMBLY, assemblyId, FileDownloadServlet.COORDS_FORMAT_VALUE_CIF);
		}
		else
		{
			EventBusManager.EVENT_BUS.fireEvent(new ShowErrorEvent("No viewer selected"));
		}
	}
	
	public static void runViewerAssemblyInNewTab(String assemblyId)
	{
		if(ApplicationContext.getSelectedViewer().equals(AppPropertiesManager.CONSTANTS.viewer_jmol()))
		{
			showJmolViewerAssemblyInNewTab(assemblyId);
		}
	}
	
	/**
	 * Downloads file from the server.
	 * @param type type of the file to download
	 * @param interfaceId identifier of the interface
	 * @param format
	 */
	//an example file is
	//http://pc11467.psi.ch:8081/ewui/fileDownload?type=interface&id=1smt&interfaceId=1&type=interface&coordsFormat=cif
	private static void downloadFileFromServer(String type, String interfaceId, String format)
	{
		String fileDownloadServletUrl = GWT.getModuleBaseURL() + FileDownloadServlet.SERVLET_NAME;
		fileDownloadServletUrl += 
				"?"+FileDownloadServlet.PARAM_TYPE+"=" + type + 
				"&"+FileDownloadServlet.PARAM_ID+"=" + ApplicationContext.getPdbInfo().getJobId() + 
				"&"+FileDownloadServlet.PARAM_INTERFACE_ID+"=" + interfaceId + 
				"&"+FileDownloadServlet.PARAM_COORDS_FORMAT+"=" + format;
		
		Window.open(fileDownloadServletUrl, "", "");
	}
	
	/**
	 * Downloads file from the server.
	 * @param type type of the file to download
	 * @param assemblyId identifier of the interface
	 * @param format
	 */
	private static void downloadAssemblyFileFromServer(String type, String assemblyId, String format)
	{
		String fileDownloadServletUrl = GWT.getModuleBaseURL() + FileDownloadServlet.SERVLET_NAME;
		fileDownloadServletUrl += 
				"?"+FileDownloadServlet.PARAM_TYPE+"=" + type + 
				"&"+FileDownloadServlet.PARAM_ID+"=" + ApplicationContext.getPdbInfo().getJobId() + 
				"&"+FileDownloadServlet.PARAM_ASSEMBLY_ID+"=" + assemblyId + 
				"&"+FileDownloadServlet.PARAM_COORDS_FORMAT+"=" + format;
		
		Window.open(fileDownloadServletUrl, "", "");
	}	
	
}
