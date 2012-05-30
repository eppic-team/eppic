package ch.systemsx.sybit.crkwebui.client.gui;

import ch.systemsx.sybit.crkwebui.client.controllers.AppPropertiesManager;
import ch.systemsx.sybit.crkwebui.client.controllers.MainController;
import ch.systemsx.sybit.crkwebui.shared.model.HomologsInfoItem;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.event.BorderLayoutEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Viewport;
import com.extjs.gxt.ui.client.widget.custom.ThemeSelector;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;

/**
 * Main view of the application.
 * @author srebniak_a
 *
 */
public class MainViewPort extends Viewport
{
	private MyJobsPanel myJobsPanel;

	private CenterPanel centerPanel;

//	private TopPanel topPanel;
	private BottomPanel bottomPanel;

	private InterfacesResiduesWindow interfacesResiduesWindow;
	private AlignmentsWindow alignmentsWindow;
	private AboutWindow aboutWindow;

	private MessageBox waitingMessageBox;
	private MessageBox errorMessageBox;

	private MainController mainController;

	public MainViewPort(final MainController mainController)
	{
		this.mainController = mainController;

		BorderLayout layout = new BorderLayout();
		this.setLayout(layout);
		this.setStyleAttribute("padding", "10px");

		layout.addListener(Events.Collapse, new Listener<BorderLayoutEvent>()
		{
			public void handleEvent(BorderLayoutEvent be)
			{
				if(be.getPanel() instanceof MyJobsPanel)
				{
					mainController.resizeResultsGrid();
				}
			}
		});

		layout.addListener(Events.Expand, new Listener<BorderLayoutEvent>()
		{
			public void handleEvent(BorderLayoutEvent be)
			{
				if(be.getPanel() instanceof MyJobsPanel)
				{
					mainController.resizeResultsGrid();
				}
			}
		});

		BorderLayoutData westData = new BorderLayoutData(LayoutRegion.WEST, 220);
		westData.setCollapsible(true);
		westData.setFloatable(true);
		westData.setSplit(true);
		westData.setMargins(new Margins(0, 5, 0, 0));

		myJobsPanel = new MyJobsPanel(mainController);
		this.add(myJobsPanel, westData);

		BorderLayoutData centerData = new BorderLayoutData(LayoutRegion.CENTER,
				200);
		centerData.setCollapsible(true);
		centerData.setFloatable(true);
		centerData.setSplit(true);
		centerData.setMargins(new Margins(0));

		centerPanel = new CenterPanel(mainController);
		this.add(centerPanel, centerData);

		BorderLayoutData northData = new BorderLayoutData(LayoutRegion.NORTH,
				10);
		northData.setMargins(new Margins(0, 0, 10, 0));

//		topPanel = new TopPanel(mainController);
//		this.add(topPanel, northData);

		BorderLayoutData southData = new BorderLayoutData(LayoutRegion.SOUTH,
				20);
		southData.setMargins(new Margins(5, 0, 0, 0));

		bottomPanel = new BottomPanel(mainController);
		bottomPanel.add(new ThemeSelector());
		this.add(bottomPanel, southData);
	}

	/**
	 * Retrieves panel containing list of jobs for the session.
	 * @return panel containing list of jobs for the session
	 */
	public MyJobsPanel getMyJobsPanel() {
		return myJobsPanel;
	}

	/**
	 * Retrieves main central panel.
	 * @return main central panel
	 */
	public CenterPanel getCenterPanel() {
		return centerPanel;
	}

	/**
	 * Retrieves bottom panel.
	 * @return bottom panel
	 */
	public BottomPanel getBottomPanel()
	{
		return bottomPanel;
	}

	/**
	 * Retrieves window containing interface residues.
	 * @return window containing interface residues
	 */
	public InterfacesResiduesWindow getInterfacesResiduesWindow() {
		return interfacesResiduesWindow;
	}

	/**
	 * Sets window containing interface residues.
	 * @param interfacesResiduesWindow window containing interface residues
	 */
	public void setInterfacesResiduesWindow(
			InterfacesResiduesWindow interfacesResiduesWindow) {
		this.interfacesResiduesWindow = interfacesResiduesWindow;
	}

	/**
	 * Displays window containing interface residues.
	 * @param selectedInterface selected interface identifier
	 */
	public void displayInterfacesWindow(int selectedInterface)
	{
		if((interfacesResiduesWindow == null) ||
		   (interfacesResiduesWindow.isResizeWindow()))
		{
			interfacesResiduesWindow = new InterfacesResiduesWindow(mainController, selectedInterface);
//			interfacesResiduesWindow.setResizable(false);
			interfacesResiduesWindow.setVisible(true);
			Scheduler.get().scheduleDeferred(new ScheduledCommand() {
				@Override
				public void execute() {
					interfacesResiduesWindow.getInterfacesResiduesPanel().resizeResiduesPanels(interfacesResiduesWindow.getWindowWidth(),
																							   interfacesResiduesWindow.getWindowHeight());
				}
			});
		}
		else
		{
			interfacesResiduesWindow.setSelectedInterface(selectedInterface);
			interfacesResiduesWindow.setWindowHeader();
			interfacesResiduesWindow.getInterfacesResiduesPanel().cleanData();
			interfacesResiduesWindow.getInterfacesResiduesPanel().getFirstStructurePanel().cleanResiduesGrid();
			interfacesResiduesWindow.getInterfacesResiduesPanel().getSecondStructurePanel().cleanResiduesGrid();
			interfacesResiduesWindow.getInterfacesResiduesPanel().getFirstStructurePanelSummary().cleanResiduesGrid();
			interfacesResiduesWindow.getInterfacesResiduesPanel().getSecondStructurePanelSummary().cleanResiduesGrid();
			interfacesResiduesWindow.setVisible(true);
		}

		//called beacuse of the bug in GXT 2.2.3
		// http://www.sencha.com/forum/showthread.php?126888-Problems-with-RowLayout
		interfacesResiduesWindow.layout(true);
	}

	/**
	 * Hides window containing interface residues.
	 */
	public void hideInterfacesWindow()
	{
		if(interfacesResiduesWindow != null)
		{
			interfacesResiduesWindow.setVisible(false);
		}
	}

	/**
	 * Shows waiting messagebox with provided text.
	 * @param text information displayed in messagebox
	 */
	public void showWaiting(final String text)
	{
//		Scheduler.get().scheduleDeferred(new ScheduledCommand() {
//			@Override
//			public void execute() {
				waitingMessageBox = MessageBox.wait(AppPropertiesManager.CONSTANTS.waiting_message_box_header(),
						text + ", " + AppPropertiesManager.CONSTANTS.waiting_message_box_info() + "...",
						text + "...");
				waitingMessageBox.show();
//			}
//		});
	}

	/**
	 * Hides waiting messagebox.
	 */
	public void hideWaiting()
	{
		if(waitingMessageBox != null)
		{
			waitingMessageBox.close();
		}
	}

	/**
	 * Shows error messagebox with provided error message.
	 * @param message error message to display
	 */
	public void showError(String message)
	{
		if((errorMessageBox == null) ||
		   (!errorMessageBox.isVisible()))
		{
			errorMessageBox = MessageBox.alert(AppPropertiesManager.CONSTANTS.error_message_box_header(), message, null);
			errorMessageBox.setMinWidth(300);
			errorMessageBox.setMaxWidth(mainController.getWindowData().getWindowWidth());
			errorMessageBox.show();
		}
	}

	/**
	 * Displays alignments window.
	 * @param homologsInfoItem homologs info item
	 * @param xPosition left corner
	 * @param yPosition top corner
	 */
	public void displayAlignmentsWindow(HomologsInfoItem homologsInfoItem,
										int xPosition,
										int yPosition)
	{
		if((alignmentsWindow == null) ||
		   (alignmentsWindow.isResizeWindow()))
		{
			alignmentsWindow = new AlignmentsWindow(mainController, homologsInfoItem);
			alignmentsWindow.setResizeWindow(false);
			alignmentsWindow.updateWindowContent();
			alignmentsWindow.setPagePosition(xPosition, yPosition);
		}
		else if(alignmentsWindow.getHomologsInfoItem() != homologsInfoItem)
		{
			alignmentsWindow.setHomologsInfoItem(homologsInfoItem);
			alignmentsWindow.updateWindowContent();
		}

		String alignmentWindowTitle = AppPropertiesManager.CONSTANTS.alignment_window_title();
		alignmentWindowTitle = alignmentWindowTitle.replaceFirst("%s", homologsInfoItem.getChains().substring(0, 1));
		alignmentWindowTitle = alignmentWindowTitle.replaceFirst("%s", homologsInfoItem.getUniprotId());
		alignmentsWindow.setHeading(alignmentWindowTitle);
		alignmentsWindow.setVisible(true);

		//called beacuse of the bug in GXT 2.2.3
		// http://www.sencha.com/forum/showthread.php?126888-Problems-with-RowLayout
		alignmentsWindow.layout(true);
	}

	/**
	 * Retrieves window containing alignments.
	 * @return window containing alignments
	 */
	public AlignmentsWindow getAlignmentsWindow()
	{
		return alignmentsWindow;
	}

	/**
	 * Shows window containing general information about the application.
	 */
	public void displayAboutWindow()
	{
		if((aboutWindow == null) ||
		   (aboutWindow.isResizeWindow()))
		{
			aboutWindow = new AboutWindow(mainController);
			aboutWindow.setResizeWindow(false);
		}

		aboutWindow.setVisible(true);
	}

	/**
	 * Retrieves window containing general information about application.
	 * @return window containing general information about application
	 */
	public AboutWindow getAboutWindow()
	{
		return aboutWindow;
	}
}
