/**
 * 
 */
package ch.systemsx.sybit.crkwebui.client.homologs.gui.cells;

import ch.systemsx.sybit.crkwebui.client.commons.appdata.ApplicationContext;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

/**
 * Cell used to render the last taxon field in the homologs grid
 * @author duarte_j
 *
 */
public class LastTaxonCell extends AbstractCell<String> {

	@Override
	public void render(com.google.gwt.cell.client.Cell.Context context,
			String value, SafeHtmlBuilder sb) {
		
		String baseUrl = ApplicationContext.getSettings().getWikipediaLinkUrl();
		
		sb.appendHtmlConstant("<a href='"+
			baseUrl + value
			+"' target='_blank'>"+ value 
			+ "</a>");
		
	}
	
}
