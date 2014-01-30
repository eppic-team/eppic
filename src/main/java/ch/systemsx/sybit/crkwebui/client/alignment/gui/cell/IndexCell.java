package ch.systemsx.sybit.crkwebui.client.alignment.gui.cell;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

public class IndexCell extends AbstractCell<Integer[]>{

	@Override
	public void render(com.google.gwt.cell.client.Cell.Context context,
			Integer[] value, SafeHtmlBuilder sb) {
		sb.appendHtmlConstant(createHtmlString(value));
		
	}

	private String createHtmlString(Integer[] value){		
		String draw = "<pre class='eppic-alignment-container'>"
				+ String.valueOf(value[0])+ "\n"
				+  "\n"
				+ String.valueOf(value[1])
				+ "</pre>";
		
		return draw;
	}

}
