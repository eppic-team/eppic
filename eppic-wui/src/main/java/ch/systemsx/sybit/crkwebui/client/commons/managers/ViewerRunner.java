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
	/**
	 * Starts selected viewer. Type of the viewer is determined based on the option selected in viewer selector.
	 * @param interfaceId identifier of the interface
	 */
	public static void runViewer(String interfaceId)
	{
		if(ApplicationContext.getSelectedViewer().equals(AppPropertiesManager.CONSTANTS.viewer_jmol()))
		{
			showJmolViewer(interfaceId);
		}
		else if(ApplicationContext.getSelectedViewer().equals(AppPropertiesManager.CONSTANTS.viewer_local()))
		{
			downloadFileFromServer(FileDownloadServlet.TYPE_VALUE_INTERFACE, interfaceId, FileDownloadServlet.COORDS_FORMAT_VALUE_PDB);
		}
		else if(ApplicationContext.getSelectedViewer().equals(AppPropertiesManager.CONSTANTS.viewer_pse()))
		{
			downloadFileFromServer(FileDownloadServlet.TYPE_VALUE_INTERFACE, interfaceId, FileDownloadServlet.COORDS_FORMAT_VALUE_PSE);
		}
		else
		{
			EventBusManager.EVENT_BUS.fireEvent(new ShowErrorEvent("No viewer selected"));
		}
	}

	/**
	 * Displays jmol viewer.
	 * @param interfaceNr interface identifier
	 */
	private static void showJmolViewer(String interfaceNr)
	{
		int size = ApplicationContext.getWindowData().getWindowHeight() - 20;
		if(size > ApplicationContext.getWindowData().getWindowWidth() - 20)
		{
			size = ApplicationContext.getWindowData().getWindowWidth() - 20;
		}
		
		int jmolAppletSize = size - 40;
		
		// NOTE we have set now CIF as the default format, JD 2015-06-13
		
		String jmolViewerUrl = GWT.getModuleBaseURL() + JmolViewerServlet.SERVLET_NAME;
		jmolViewerUrl += "?"+FileDownloadServlet.PARAM_ID+"=" + ApplicationContext.getPdbInfo().getJobId() + 
						 "&"+JmolViewerServlet.PARAM_INPUT+"=" + ApplicationContext.getPdbInfo().getTruncatedInputName() + 
						 "&"+FileDownloadServlet.PARAM_INTERFACE_ID+"=" + interfaceNr +
						 "&"+FileDownloadServlet.PARAM_COORDS_FORMAT+"=" + FileDownloadServlet.COORDS_FORMAT_VALUE_CIF+
						 "&"+JmolViewerServlet.PARAM_SIZE+"=" + jmolAppletSize;
		
		Window.open(jmolViewerUrl, "", "width=" + size + "," +
										"height=" + size);

	}

	/**
	 * Downloads file from the server.
	 * @param type type of the file to download
	 * @param interfaceId identifier of the interface
	 */
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
}
