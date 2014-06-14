package ch.systemsx.sybit.crkwebui.client.alignment.gui.cell;

import ch.systemsx.sybit.crkwebui.shared.model.PairwiseAlignmentData;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

public class AlignmentCell extends AbstractCell<PairwiseAlignmentData> {

	@Override
	public void render(Context context, PairwiseAlignmentData value, SafeHtmlBuilder sb) {
		
		sb.appendHtmlConstant(createHtmlString(value));
		
	}
	
	private String createHtmlString(PairwiseAlignmentData value){
		StringBuffer firstSequenceLine = new StringBuffer(value.getFirstSequence());
		StringBuffer secondSequenceLine = new StringBuffer(value.getSecondSequence());
		StringBuffer markupLine = new StringBuffer(value.getMarkupLine());
		String markup = value.getMarkupLine();
		
		int beginIndex = 0;
		int endIndex = firstSequenceLine.length() -1;
		
		for(int j=endIndex - beginIndex; j>=0; j--)
		{
			if(markup.charAt(j) == ' ')
			{
				firstSequenceLine.insert(j + 1, "</div>");
				firstSequenceLine.insert(j, "<div class=\"eppic-alignment-blank\">");

				secondSequenceLine.insert(j + 1, "</div>");
				secondSequenceLine.insert(j, "<div class=\"eppic-alignment-blank\">");
				
				markupLine.insert(j + 1, "</div>");
				markupLine.insert(j, "<div class=\"eppic-alignment-blank\">");
			}
			else if(markup.charAt(j) == '|')
			{
				firstSequenceLine.insert(j + 1, "</div>");
				firstSequenceLine.insert(j, "<div class=\"eppic-alignment-match\">");

				secondSequenceLine.insert(j + 1, "</div>");
				secondSequenceLine.insert(j, "<div class=\"eppic-alignment-match\">");
				
				markupLine.insert(j + 1, "</div>");
				markupLine.insert(j, "<div class=\"eppic-alignment-match\">");
			}
			else
			{
				firstSequenceLine.insert(j + 1, "</div>");
				firstSequenceLine.insert(j, "<div class=\"eppic-alignment-mismatch\">");

				secondSequenceLine.insert(j + 1, "</div>");
				secondSequenceLine.insert(j, "<div class=\"eppic-alignment-mismatch\">");
				
				markupLine.insert(j + 1, "</div>");
				markupLine.insert(j, "<div class=\"eppic-alignment-mismatch\">");
			}
		}
		
		String draw = "<pre class='eppic-alignment-container'>"
				+ firstSequenceLine.toString()+ "\n"
				+ markupLine.toString() + "\n"
				+ secondSequenceLine.toString()
				+ "</pre>";
		
		return draw;
	}

}
