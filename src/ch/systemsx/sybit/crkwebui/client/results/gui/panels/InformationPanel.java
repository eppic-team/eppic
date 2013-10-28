package ch.systemsx.sybit.crkwebui.client.results.gui.panels;

import ch.systemsx.sybit.crkwebui.shared.model.PDBScoreItem;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;

/**
 * Panel containing information about the sequence and the experiment
 * @author biyani_n
 *
 */
public class InformationPanel extends LayoutContainer {
	
	private GeneralInfoPanel generalInfoPanel;
	private SequenceInfoPanel sequenceInfoPanel;
	
	public InformationPanel(PDBScoreItem pdbScoreItem){
		this.setBorders(false);
		this.setLayout(new RowLayout(Orientation.HORIZONTAL));
		
		generalInfoPanel = new GeneralInfoPanel(pdbScoreItem);
		sequenceInfoPanel = new SequenceInfoPanel(pdbScoreItem);
		
		this.add(generalInfoPanel, new RowData(-1, 90,  new Margins(0, 5, 0, 0)));
		this.add(sequenceInfoPanel, new RowData(1, 90, new Margins(0, 0, 0, 5)));

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
	public void resize() {
		generalInfoPanel.resize();
	}

}
