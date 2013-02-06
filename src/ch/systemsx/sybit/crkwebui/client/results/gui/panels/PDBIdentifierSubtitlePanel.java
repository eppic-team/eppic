package ch.systemsx.sybit.crkwebui.client.results.gui.panels;

import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.LayoutContainer;

/**
 * Panel used to store pdb identifier subtitle.
 * @author AS
 *
 */
public class PDBIdentifierSubtitlePanel extends LayoutContainer
{
	private Label pdbSubtitle;
	
	public PDBIdentifierSubtitlePanel()
	{
		this.addStyleName("eppic-pdb-title-label");
		
		pdbSubtitle = new Label();
		this.add(pdbSubtitle);
	}
	
	/**
	 * Sets value of pdb identifier subtitle.
	 * @param subtitle pdb identifier subtitle
	 */
	public void setPDBIdentifierSubtitle(String subtitle)
	{
		pdbSubtitle.setText(subtitle);
	}
}
