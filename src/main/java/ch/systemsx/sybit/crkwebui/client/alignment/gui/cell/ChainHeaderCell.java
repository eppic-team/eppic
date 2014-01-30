package ch.systemsx.sybit.crkwebui.client.alignment.gui.cell;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

public class ChainHeaderCell extends AbstractCell<String[]> {

	@Override
	public void render(com.google.gwt.cell.client.Cell.Context context,
			String[] value, SafeHtmlBuilder sb) {
		sb.appendHtmlConstant(createHtmlString(value));
		
	}

	private String createHtmlString(String[] value){		
		String draw = "<pre class='eppic-alignment-container'>"
				+ value[0]+ "\n"
				+  "\n"
				+ value[1]
				+ "</pre>";
		
		return draw;
	}
}
