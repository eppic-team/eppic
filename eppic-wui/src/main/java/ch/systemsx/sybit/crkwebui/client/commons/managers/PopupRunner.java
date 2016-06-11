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
import ch.systemsx.sybit.crkwebui.shared.model.Assembly;
import ch.systemsx.sybit.crkwebui.shared.model.Interface;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;

/**
 * Utility class to open various servlets as a popup or separate window
 */
public class PopupRunner
{
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
		// we use the same size approach as with the jmol viewer - JD 2016-01-06
		int size = Math.min( ApplicationContext.getWindowData().getWindowHeight() - 60,
				ApplicationContext.getWindowData().getWindowWidth() - 60);
		
		int canvasSize = size - 40;
		
		String interfaceids = null;
		List<Assembly> assemblies = ApplicationContext.getPdbInfo().getAssemblies();
		for(Assembly a : assemblies){
			if((a.getId()+"").equals(assemblyId)){
				interfaceids = joinInterfaceIds( a.getInterfaces() );
			}
		}
		//if(interfaceids == null)
			//Window.alert("No interfaces to show!");

		String url = GWT.getModuleBaseURL() + servlet;
		url +=  "?" + FileDownloadServlet.PARAM_ID + "=" + ApplicationContext.getPdbInfo().getJobId() +
				"&" + LatticeGraphServlet.PARAM_INTERFACES + "=" + interfaceids +
				"&" + JmolViewerServlet.PARAM_SIZE+"=" + canvasSize;

		//show only when interface count > 0
		//if(!interfaceids.equals("")) 	
			//Window.open(url,"_blank","width="+size+",height="+size);
		popup(url,"");
	}
	
	/**
	 * Consistent entry point to create new popup windows
	 * @param url
	 * @param features
	 */
	public static void popup(String url,String features) {
		//Window.open(url,"_blank","width="+size+",height="+size);
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

	public static String getLatticeGraphURL(){
		String url = GWT.getModuleBaseURL() + LatticeGraphServlet.SERVLET_NAME;
		url +=  "?" + FileDownloadServlet.PARAM_ID + "=" + ApplicationContext.getPdbInfo().getJobId() +
				"&" + LatticeGraphServlet.PARAM_INTERFACES + "=*";
		return url;
		
	}
	

}
