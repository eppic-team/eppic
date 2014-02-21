package ch.systemsx.sybit.crkwebui.client.search.gui.cells;

import ch.systemsx.sybit.crkwebui.client.commons.appdata.ApplicationContext;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

public class PdbCodeCell extends AbstractCell<String> {

	@Override
	public void render(com.google.gwt.cell.client.Cell.Context context,
			String value, SafeHtmlBuilder sb) {
		
		String baseUrl = ApplicationContext.getSettings().getPdbLinkUrl();
		
		sb.appendHtmlConstant("<div class='eppic-search-grid-pdb-code'>"
				+ "<a href='"+
								baseUrl + value
								+"' target='_blank'>"+ value 
				+ "</a></div>");
		
	}

}
