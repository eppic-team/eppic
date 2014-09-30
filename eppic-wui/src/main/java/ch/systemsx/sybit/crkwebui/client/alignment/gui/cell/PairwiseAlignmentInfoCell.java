package ch.systemsx.sybit.crkwebui.client.alignment.gui.cell;

import ch.systemsx.sybit.crkwebui.shared.model.PairwiseAlignmentInfo;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

public class PairwiseAlignmentInfoCell extends AbstractCell<PairwiseAlignmentInfo> {

	@Override
	public void render(Context context, PairwiseAlignmentInfo value, SafeHtmlBuilder sb) {
		
		sb.appendHtmlConstant(createHtmlString(value));
		
	}

	private String createHtmlString(PairwiseAlignmentInfo value){		
		String draw = "<pre class='eppic-alignment-container'>"
				+ value.getFirstValue()+ "\n"
				+  "\n"
				+ value.getSecondValue()
				+ "</pre>";
		
		return draw;
	}
}
