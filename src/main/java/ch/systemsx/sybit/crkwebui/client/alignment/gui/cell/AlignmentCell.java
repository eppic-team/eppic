package ch.systemsx.sybit.crkwebui.client.alignment.gui.cell;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

public class AlignmentCell extends AbstractCell<String[]> {

	@Override
	public void render(com.google.gwt.cell.client.Cell.Context context,
			String[] value, SafeHtmlBuilder sb) {
		
		sb.appendHtmlConstant(createHtmlString(value));
		
	}
	
	private String createHtmlString(String[] value){
		StringBuffer firstSequenceLine = new StringBuffer(value[0]);
		StringBuffer secondSequenceLine = new StringBuffer(value[2]);
		StringBuffer markupLine = new StringBuffer(value[1]);
		String markup = value[1];
		
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
