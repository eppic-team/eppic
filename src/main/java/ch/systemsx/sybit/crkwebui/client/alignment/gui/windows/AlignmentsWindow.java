package ch.systemsx.sybit.crkwebui.client.alignment.gui.windows;

import ch.systemsx.sybit.crkwebui.client.alignment.gui.panels.AlignmentHeaderPanel;
import ch.systemsx.sybit.crkwebui.client.alignment.gui.panels.AlignmentsGridPanel;
import ch.systemsx.sybit.crkwebui.client.commons.gui.windows.ResizableWindow;
import ch.systemsx.sybit.crkwebui.shared.model.ChainCluster;
import ch.systemsx.sybit.crkwebui.shared.model.WindowData;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.DockLayoutPanel;


/**
 * Window containing sequence alignments.
 * @author AS, nikhil
 */
public class AlignmentsWindow extends ResizableWindow 
{	
	private static int ALIGNMENT_WINDOW_DEFAULT_WIDTH = 600;
	private static int ALIGNMENT_WINDOW_DEFAULT_HEIGHT = 400;
	
	private static int HEADER_HEIGHT = 50;
	
	private ChainCluster chainCluster;

	private AlignmentHeaderPanel headerPanel;
	private AlignmentsGridPanel gridContainer;

	
	public AlignmentsWindow(WindowData windowData,
			ChainCluster chainCluster,
			String pdbName) 
	{
		super(ALIGNMENT_WINDOW_DEFAULT_WIDTH,
				ALIGNMENT_WINDOW_DEFAULT_HEIGHT,
				windowData);
		
		this.chainCluster = chainCluster;
		this.setHideOnButtonClick(true);
		
		headerPanel = new AlignmentHeaderPanel();		
		gridContainer = new AlignmentsGridPanel(chainCluster, pdbName, 
												ALIGNMENT_WINDOW_DEFAULT_WIDTH, ALIGNMENT_WINDOW_DEFAULT_HEIGHT - HEADER_HEIGHT);
		DockLayoutPanel dock = new DockLayoutPanel(Unit.PX);
		dock.addNorth(headerPanel, HEADER_HEIGHT);
		
		dock.add(gridContainer);
		
		this.setWidget(dock);

	}
	
	public void updateWindowContent(){
		headerPanel.updateContent(chainCluster.getRefUniProtId(), 
								  chainCluster.getFirstTaxon(),
								  chainCluster.getLastTaxon(),
								  chainCluster.getRepChain(), 
								  chainCluster.getMemberChains());
		gridContainer.updatePanelContent();
	}
	
	

}
