package ch.systemsx.sybit.crkwebui.client.results.gui.panels;

import com.google.gwt.user.client.ui.HTML;
import com.sencha.gxt.widget.core.client.container.SimpleContainer;

/**
 * Panel used to store pdb identifier subtitle.
 * @author AS
 *
 */
public class PDBIdentifierSubtitlePanel extends SimpleContainer
{
	private HTML pdbSubtitle;
	
	public PDBIdentifierSubtitlePanel()
	{
		this.addStyleName("eppic-pdb-title-label");
		
		pdbSubtitle = new HTML();
		this.add(pdbSubtitle);
	}
	
	/**
	 * Sets value of pdb identifier subtitle.
	 * @param subtitle pdb identifier subtitle
	 */
	public void setPDBIdentifierSubtitle(String subtitle)
	{
		pdbSubtitle.setHTML(subtitle);
	}
}
