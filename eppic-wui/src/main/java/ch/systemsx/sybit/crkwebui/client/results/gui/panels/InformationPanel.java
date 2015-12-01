package ch.systemsx.sybit.crkwebui.client.results.gui.panels;

import ch.systemsx.sybit.crkwebui.shared.model.PdbInfo;

import com.google.gwt.user.client.Window;
import com.sencha.gxt.widget.core.client.container.HorizontalLayoutContainer;
import com.sencha.gxt.core.client.util.Margins;

/**
 * Panel containing information about the sequence and the experiment
 * @author biyani_n
 *
 */
public class InformationPanel extends HorizontalLayoutContainer {
	
	private SequenceInfoPanel sequenceInfoPanel;
	public static TopologyInfoPanel topologyInfoPanel = null;
	public static AssemblyInfoPanel assemblyInfoPanel;
	
	public InformationPanel(PdbInfo pdbScoreItem, int width){
		this.setWidth(width);		
		topologyInfoPanel = new TopologyInfoPanel(pdbScoreItem);
		sequenceInfoPanel = new SequenceInfoPanel(pdbScoreItem);
		assemblyInfoPanel = new AssemblyInfoPanel(pdbScoreItem);
		this.add(topologyInfoPanel, new HorizontalLayoutData(193, 122,  new Margins(0, 10, 0, 0)));
		this.add(assemblyInfoPanel, new HorizontalLayoutData(250, 122,  new Margins(0, 5, 0, 0)));
		this.add(sequenceInfoPanel, new HorizontalLayoutData(1, 122, new Margins(0, 5, 0, 5)));
	}
	
	public void removeTopologyPanel(PdbInfo pdbScoreItem){
		if(topologyInfoPanel != null){
			this.remove(topologyInfoPanel);
			topologyInfoPanel = null;
		}
	}
	
	public void addTopologyPanel(PdbInfo pdbScoreItem){
		if(topologyInfoPanel == null){
			topologyInfoPanel = new TopologyInfoPanel(pdbScoreItem);
			
			//SequenceInfoPanel sequenceInfoPanel2 = sequenceInfoPanel;
			//AssemblyInfoPanel assemblyInfoPanel2 = assemblyInfoPanel;
			this.remove(sequenceInfoPanel);
			this.remove(assemblyInfoPanel);
			
			this.add(topologyInfoPanel, new HorizontalLayoutData(190, 122,  new Margins(0, 10, 0, 0)));
			this.add(assemblyInfoPanel);
			this.add(sequenceInfoPanel);
			
			
		}
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
