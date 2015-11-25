package ch.systemsx.sybit.crkwebui.client.results.gui.panels;

import ch.systemsx.sybit.crkwebui.shared.model.PdbInfo;

import com.sencha.gxt.widget.core.client.container.HorizontalLayoutContainer;
import com.sencha.gxt.core.client.util.Margins;

/**
 * Panel containing information about the sequence and the experiment
 * @author biyani_n
 *
 */
public class InformationPanel extends HorizontalLayoutContainer {
	
	private SequenceInfoPanel sequenceInfoPanel;
	private TopologyInfoPanel topologyInfoPanel;
	public static AssemblyInfoPanel assemblyInfoPanel;
	
	public InformationPanel(PdbInfo pdbScoreItem, int width){
		this.setWidth(width);		
		topologyInfoPanel = new TopologyInfoPanel(pdbScoreItem);
		sequenceInfoPanel = new SequenceInfoPanel(pdbScoreItem);
		assemblyInfoPanel = new AssemblyInfoPanel(pdbScoreItem);
		this.add(topologyInfoPanel, new HorizontalLayoutData(195, 123,  new Margins(0, 10, 0, 0)));
		this.add(assemblyInfoPanel, new HorizontalLayoutData(250, 123,  new Margins(0, 5, 0, 0)));
		this.add(sequenceInfoPanel, new HorizontalLayoutData(1, 123, new Margins(0, 5, 0, 5)));
	}

	public void fillInfoPanel(PdbInfo pdbScoreItem) {
		sequenceInfoPanel.generateSequenceInfoPanel(pdbScoreItem);	
	}
	
	/**
	 * Resize the information panel
	 */
	public void resizePanel(int width) {
		this.setWidth(width);
	}

}
