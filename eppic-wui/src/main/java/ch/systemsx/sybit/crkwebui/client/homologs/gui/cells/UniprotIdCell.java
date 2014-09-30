/**
 * 
 */
package ch.systemsx.sybit.crkwebui.client.homologs.gui.cells;

import ch.systemsx.sybit.crkwebui.client.commons.appdata.ApplicationContext;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

/**
 * Cell used to render uniprot ids in the homologs grid
 * @author biyani_n
 *
 */
public class UniprotIdCell extends AbstractCell<String> {

	@Override
	public void render(com.google.gwt.cell.client.Cell.Context context,
			String value, SafeHtmlBuilder sb) {
		
		String baseUrl = null;
		
		if(value.startsWith("UPI"))
			baseUrl = ApplicationContext.getSettings().getUniparcLinkUrl();
		else
			baseUrl = ApplicationContext.getSettings().getUniprotLinkUrl();
		
		sb.appendHtmlConstant("<a href='"+
			baseUrl + value
			+"' target='_blank'>"+ value 
			+ "</a>");
		
	}
	
}
