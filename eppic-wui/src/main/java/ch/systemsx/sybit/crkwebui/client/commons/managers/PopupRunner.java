package ch.systemsx.sybit.crkwebui.client.commons.managers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ch.systemsx.sybit.crkwebui.client.commons.appdata.ApplicationContext;
import ch.systemsx.sybit.crkwebui.server.files.downloader.servlets.FileDownloadServlet;
import ch.systemsx.sybit.crkwebui.server.jmol.servlets.AssemblyDiagramServlet;
import ch.systemsx.sybit.crkwebui.server.jmol.servlets.JmolViewerServlet;
import ch.systemsx.sybit.crkwebui.server.jmol.servlets.LatticeGraphServlet;
import eppic.model.dto.Assembly;
import eppic.model.dto.Interface;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;

/**
 * Utility class to open various servlets as a popup or separate window
 */
public class PopupRunner
{
	
	public static final int VIEWER_SIZE_OFFSET = 200;
	
	/**
	 * A constant to use for when all interfaces and not only those of a particular assembly should be shown.
	 */
	public static final String ALL_INTERFACES = "*";
	
	/**
	 * Open
	 * @param assemblyId
	 */
	public static void popupLatticeGraph(String assemblyId)
	{
		popupAssemblyView(LatticeGraphServlet.SERVLET_NAME,assemblyId);
	}
	
	public static void popupAssemblyDiagram(String assemblyId)
	{
		popupAssemblyView(AssemblyDiagramServlet.SERVLET_NAME,assemblyId);
	}
	
	private static void popupAssemblyView(String servlet, String assemblyId)
	{
		String url = getGraphViewerUrl(servlet, assemblyId);
		popup(url,"");
	}
	
	/**
	 * Consistent entry point to create new popup windows
	 * @param url
	 * @param features either empty string or something like "width=" + size + "," + "height=" + size
	 */
	public static void popup(String url,String features) {
		// this opens the viewer in a popup window of fixed dimensions, but it is not possible to modify the URL 
		//Window.open(url,"","width=" + size + "," + "height=" + size);
		
		// this opens the viewer in a new tab, the url is modifiable
		Window.open(url,"_blank",features);
	}
	
	/**
	 * Joins a list of interfaces into a comma-separated list of interface IDs.
	 * Sequential ranges are combined.
	 * @param interfaces
	 * @return
	 */
	public static String joinInterfaceIds(List<Interface> interfaces) {
		List<Integer> ids = new ArrayList<>(interfaces.size());
		for(Interface i : interfaces) {
			ids.add(i.getInterfaceId());
		}
		Collections.sort(ids,new Comparator<Integer>() {
			@Override
			public int compare(Integer x, Integer y) {
				return Integer.compare(x, y);
			}
		});
		return joinRanges( ids );
	}

	/**
	 * Collapses a sorted list of integers into a comma-separated list of ranges
	 * 
	 * Example: Arrays.asList(1,2,3,4,7,8,10) -> "1-4,7-8,10"
	 * @param elements pre-sorted list of element
	 * @return comma-separated list of elements, with sequential elements joined
	 */
	public static String joinRanges(List<Integer> elements) {
		if(elements.isEmpty()) {
			return "";
		}
		StringBuilder str = new StringBuilder();
		Integer startRange=null;
		Integer endRange=null;
		for(Integer i : elements) {
			if(startRange == null) {
				// first element
				startRange = i;
				endRange = i;
			} else {
				if( i == endRange+1 ) {
					// expand a range
					endRange = i;
				} else {
					// output old range
					if(str.length() > 0) {
						str.append(',');
					}
					str.append(startRange);
					if(endRange > startRange) {
						str.append('-');
						str.append(endRange);
					}
					// start a new range
					startRange = i;
					endRange = i;
				}
			}
		}
		if(str.length() > 0) {
			str.append(',');
		}
		str.append(startRange);
		if(endRange > startRange) {
			str.append('-');
			str.append(endRange);
		}
		return str.toString();
	}

	/**
	 * Builds the graph viewer URL (either 3D lattice graph or 2D assembly diagram).
	 * @param servlet the servlet name, e.g. {@link AssemblyDiagramServlet#SERVLET_NAME} or {@link LatticeGraphServlet#SERVLET_NAME}
	 * @param assemblyId the assembly identifier or {@link PopupRunner#ALL_INTERFACES} if all interfaces to be shown  
	 * @return
	 */
	public static String getGraphViewerUrl(String servlet, String assemblyId){

		// we use the same size approach as with the jmol viewer - JD 2016-01-06
		int size = Math.min( ApplicationContext.getWindowData().getWindowHeight(),
				ApplicationContext.getWindowData().getWindowWidth());

		int canvasSize = size - VIEWER_SIZE_OFFSET;

		
		String interfaceids = null;

		if (assemblyId == null || assemblyId.equals(ALL_INTERFACES)) {
			interfaceids = "*";
			
		} else {

			List<Assembly> assemblies = ApplicationContext.getPdbInfo().getAssemblies();
			for(Assembly a : assemblies){
				if((a.getId()+"").equals(assemblyId)){
					interfaceids = joinInterfaceIds( a.getInterfaces() );
				}
			}
		}

		String url = GWT.getModuleBaseURL() + servlet;
		url +=  "?" + FileDownloadServlet.PARAM_ID + "=" + ApplicationContext.getPdbInfo().getJobId() +
				"&" + LatticeGraphServlet.PARAM_INTERFACES + "=" + interfaceids +
				"&" + JmolViewerServlet.PARAM_SIZE+"=" + canvasSize;

		
		return url;
		
	}
	

}
