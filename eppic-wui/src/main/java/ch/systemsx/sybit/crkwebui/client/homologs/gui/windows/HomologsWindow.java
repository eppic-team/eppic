package ch.systemsx.sybit.crkwebui.client.homologs.gui.windows;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.ui.DockLayoutPanel;

import ch.systemsx.sybit.crkwebui.client.commons.gui.windows.ResizableWindow;
import ch.systemsx.sybit.crkwebui.client.homologs.gui.panels.HomologsGrid;
import ch.systemsx.sybit.crkwebui.client.homologs.gui.panels.HomologsHeaderPanel;
import ch.systemsx.sybit.crkwebui.shared.model.ChainCluster;
import ch.systemsx.sybit.crkwebui.shared.model.PdbInfo;
import ch.systemsx.sybit.crkwebui.shared.model.WindowData;

/**
 * Window to display homologs
 * @author nikhil
 *
 */
public class HomologsWindow extends ResizableWindow {

	private final static int ALIGNMENT_WINDOW_DEFAULT_WIDTH  = 600;
	private final static int ALIGNMENT_WINDOW_DEFAULT_HEIGHT = 400;
	
	private ChainCluster chainCluster;
	private String jobId;
	
	private HomologsHeaderPanel headerPanel;
	private HomologsGrid homologsGrid;
	
	public HomologsWindow(WindowData windowData, ChainCluster chainCluster, String jobId, PdbInfo pdbInfo){
		super(ALIGNMENT_WINDOW_DEFAULT_WIDTH, ALIGNMENT_WINDOW_DEFAULT_HEIGHT, windowData);
		this.chainCluster = chainCluster;
		this.jobId = jobId;
		
		this.setHideOnButtonClick(true);
		
		headerPanel = new HomologsHeaderPanel(chainCluster, jobId, pdbInfo);
		homologsGrid = new HomologsGrid();
		
		DockLayoutPanel dock = new DockLayoutPanel(Unit.PX);
		dock.addNorth(headerPanel, 55);
		
		dock.add(homologsGrid);
		
		this.setWidget(dock);
		
		this.addResizeHandler(new ResizeHandler()
		{
			@Override
			public void onResize(ResizeEvent event) {
				resizeWindow();

			}
		});
	}
	
	/**
	 * updates the content of the window
	 */
	public void updateWindowContent(){
		headerPanel.updateContent(chainCluster, jobId);
		homologsGrid.fillHomologsGrid(chainCluster.getHomologs(),
									chainCluster.getRefUniProtEnd()-chainCluster.getRefUniProtStart());
	}
	
	/**
	 * Resizes the window
	 */
	public void resizeWindow(){
		homologsGrid.resizePanel();
	}
}