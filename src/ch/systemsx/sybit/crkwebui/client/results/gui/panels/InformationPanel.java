package ch.systemsx.sybit.crkwebui.client.results.gui.panels;

import ch.systemsx.sybit.crkwebui.shared.model.PDBScoreItem;

import com.sencha.gxt.widget.core.client.container.HorizontalLayoutContainer;
import com.sencha.gxt.core.client.util.Margins;

/**
 * Panel containing information about the sequence and the experiment
 * @author biyani_n
 *
 */
public class InformationPanel extends HorizontalLayoutContainer {
	
	private GeneralInfoPanel generalInfoPanel;
	private SequenceInfoPanel sequenceInfoPanel;
	
	public InformationPanel(PDBScoreItem pdbScoreItem, int width){
		this.setWidth(width);
		
		generalInfoPanel = new GeneralInfoPanel(pdbScoreItem);
		sequenceInfoPanel = new SequenceInfoPanel(pdbScoreItem);
		
		this.add(generalInfoPanel, new HorizontalLayoutData(-1, 115,  new Margins(0, 5, 0, 0)));
		this.add(sequenceInfoPanel, new HorizontalLayoutData(1, 115, new Margins(0, 5, 0, 5)));

	}

	public void fillInfoPanel(PDBScoreItem pdbScoreItem) {
		generalInfoPanel.fillGeneralInfoPanel(pdbScoreItem.getSpaceGroup(), 
									pdbScoreItem.getExpMethod(), 
									pdbScoreItem.getResolution(), 
									pdbScoreItem.getRfreeValue());
		
		sequenceInfoPanel.generateSequenceInfoPanel(pdbScoreItem);
		
		
	}
	
	/**
	 * Resize the information panel
	 */
	public void resizePanel(int width) {
		//generalInfoPanel.resizePanel();
		this.setWidth(width);
	}

}
