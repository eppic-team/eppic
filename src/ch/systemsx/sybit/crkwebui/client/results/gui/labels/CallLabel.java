package ch.systemsx.sybit.crkwebui.client.results.gui.labels;

import ch.systemsx.sybit.crkwebui.client.commons.gui.labels.LabelWithTooltip;
import ch.systemsx.sybit.crkwebui.client.commons.util.EscapedStringGenerator;
import ch.systemsx.sybit.crkwebui.shared.model.WindowData;

public class CallLabel extends LabelWithTooltip
{
	public CallLabel(String labelText,
					 String tooltipText,
					 WindowData windowData)
	{
		super(labelText, 
			  EscapedStringGenerator.generateSanitizedString(tooltipText), 
			  windowData, 
			  100);

		String color = "black";
		
		if (labelText.equals("bio")) 
		{
			color = "green";
		}
		else if (labelText.equals("xtal")) 
		{
			color = "red";
		}
		
		this.setStyleAttribute("color", color);
	}
}
