package ch.systemsx.sybit.crkwebui.client.homologs.gui.windows;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.ui.DockLayoutPanel;

import ch.systemsx.sybit.crkwebui.client.commons.gui.windows.ResizableWindow;
import ch.systemsx.sybit.crkwebui.client.homologs.gui.panels.HomologsGrid;
import ch.systemsx.sybit.crkwebui.client.homologs.gui.panels.HomologsHeaderPanel;
import ch.systemsx.sybit.crkwebui.shared.model.HomologsInfoItem;
import ch.systemsx.sybit.crkwebui.shared.model.WindowData;

/**
 * Window to display homologs
 * @author nikhil
 *
 */
public class HomologsWindow extends ResizableWindow {

	private static int ALIGNMENT_WINDOW_DEFAULT_WIDTH = 600;
	private static int ALIGNMENT_WINDOW_DEFAULT_HEIGHT = 400;
	
	private HomologsInfoItem homologsInfoItem;
	private String jobId;
	
	private HomologsHeaderPanel headerPanel;
	private HomologsGrid homologsGrid;
	
	public HomologsWindow(WindowData windowData, HomologsInfoItem homologsInfoItem, String jobId){
		super(ALIGNMENT_WINDOW_DEFAULT_WIDTH, ALIGNMENT_WINDOW_DEFAULT_HEIGHT, windowData);
		this.homologsInfoItem = homologsInfoItem;
		this.jobId = jobId;
		
		this.setHideOnButtonClick(true);
		
		headerPanel = new HomologsHeaderPanel(homologsInfoItem, jobId);
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
		headerPanel.updateContent(homologsInfoItem, jobId);
		homologsGrid.fillHomologsGrid(homologsInfoItem.getHomologs(),
									homologsInfoItem.getRefUniProtEnd()-homologsInfoItem.getRefUniProtStart());
	}
	
	/**
	 * Resizes the window
	 */
	public void resizeWindow(){
		homologsGrid.resizePanel();
	}
}