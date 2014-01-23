package ch.systemsx.sybit.crkwebui.client.jobs.gui.panels;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.sybit.crkwebui.client.commons.appdata.AppPropertiesManager;
import ch.systemsx.sybit.crkwebui.client.commons.appdata.ApplicationContext;
import ch.systemsx.sybit.crkwebui.client.commons.events.BeforeJobDeletedEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.GetFocusOnJobsListEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.HideJobsPanelEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.JobListRetrievedEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.ShowJobsPanelEvent;
import ch.systemsx.sybit.crkwebui.client.commons.handlers.BeforeJobRemovedHandler;
import ch.systemsx.sybit.crkwebui.client.commons.handlers.GetFocusOnJobsListHandler;
import ch.systemsx.sybit.crkwebui.client.commons.handlers.JobListRetrievedHandler;
import ch.systemsx.sybit.crkwebui.client.commons.managers.EventBusManager;
import ch.systemsx.sybit.crkwebui.client.commons.services.eppic.CrkWebServiceProvider;
import ch.systemsx.sybit.crkwebui.client.commons.util.StyleGenerator;
import ch.systemsx.sybit.crkwebui.client.jobs.data.MyJobsModel;
import ch.systemsx.sybit.crkwebui.client.jobs.data.MyJobsModelProperties;
import ch.systemsx.sybit.crkwebui.client.jobs.gui.cells.InputCell;
import ch.systemsx.sybit.crkwebui.client.jobs.gui.cells.JobStatusCell;
import ch.systemsx.sybit.crkwebui.client.jobs.gui.grids.contextmenus.JobsPanelContextMenu;
import ch.systemsx.sybit.crkwebui.shared.model.ProcessingInProgressData;
import ch.systemsx.sybit.crkwebui.shared.model.StatusOfJob;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.safehtml.shared.UriUtils;
import com.google.gwt.user.client.History;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.button.ToolButton;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer.VerticalLayoutData;
import com.sencha.gxt.widget.core.client.event.CellClickEvent;
import com.sencha.gxt.widget.core.client.event.CellClickEvent.CellClickHandler;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.tips.QuickTip;
import com.sencha.gxt.widget.core.client.tips.ToolTipConfig;
import com.sencha.gxt.widget.core.client.toolbar.FillToolItem;
import com.sencha.gxt.widget.core.client.toolbar.ToolBar;
import com.sencha.gxt.core.client.Style.SelectionMode;
import com.sencha.gxt.core.client.util.IconHelper;
import com.sencha.gxt.core.client.util.KeyNav;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.data.shared.ListStore;

/**
 * Panel used to display the list of all jobs connected to the current session.
 * @author srebniak_a; nikhil
 *
 */
public class MyJobsPanel extends ContentPanel
{	
	private Grid<MyJobsModel> myJobsGrid;
	private ListStore<MyJobsModel> myJobsStore;
	
	private static final MyJobsModelProperties props = GWT.create(MyJobsModelProperties.class);

	private boolean isJobsListFirstTimeLoaded = true;

	public MyJobsPanel()
	{
		this.setHeadingHtml(StyleGenerator.defaultFontStyle(AppPropertiesManager.CONSTANTS.myjobs_panel_head()));
		
		VerticalLayoutContainer mainContainer = new VerticalLayoutContainer();

		List<ColumnConfig<MyJobsModel, ?>> myJobsConfigs = createColumnConfig();
		ColumnModel<MyJobsModel> myJobsColumnModel = new ColumnModel<MyJobsModel>(myJobsConfigs);
		myJobsStore = new ListStore<MyJobsModel>(props.key());

		ToolBar myJobsToolBar = createToolBar();
		mainContainer.add(myJobsToolBar, new VerticalLayoutData(1, -1, new Margins(0)));
		
		myJobsGrid = createJobsGrid(myJobsColumnModel);
		mainContainer.add(myJobsGrid, new VerticalLayoutData(1, 1, new Margins(0)));
		
		this.setWidget(mainContainer);
		
		initializeEventsListeners();
	}

	/**
	 * Creates columns configurations for jobs grid.
	 * @return columns configurations for jobs grid
	 */
	private List<ColumnConfig<MyJobsModel,?>> createColumnConfig()
	{		
		List<ColumnConfig<MyJobsModel,?>> configs = new ArrayList<ColumnConfig<MyJobsModel,?>>();

		ColumnConfig<MyJobsModel, String> inputColumn = new ColumnConfig<MyJobsModel, String>(props.input(),
						Integer.parseInt(ApplicationContext.getSettings().getGridProperties().get("jobs_input_width")));
		inputColumn.setHeader(ApplicationContext.getSettings().getGridProperties().get("jobs_input_header"));
		inputColumn.setCell(new InputCell());
		inputColumn.setResizable(true);
		inputColumn.setColumnTextClassName("eppic-my-jobs-list-input");
		inputColumn.setColumnHeaderClassName("eppic-default-font");
		
		ColumnConfig<MyJobsModel, String> statusColumn = new ColumnConfig<MyJobsModel, String>(props.status(),
				Integer.parseInt(ApplicationContext.getSettings().getGridProperties().get("jobs_status_width")));
		statusColumn.setHeader(ApplicationContext.getSettings().getGridProperties().get("jobs_status_header"));
		statusColumn.setCell(new JobStatusCell());
		statusColumn.setResizable(false);
		statusColumn.setColumnHeaderClassName("eppic-default-font");
		
		configs.add(inputColumn);
		configs.add(statusColumn);

		return configs;
	}
	
	/**
	 * Creates the tool bar of myjobs grid
	 * @return tool bar
	 */
	private ToolBar createToolBar(){
		ToolBar tb = new ToolBar();

		tb.add(createClearAllButton());
		tb.add(new FillToolItem());
		
		ToolButton info = new ToolButton(ToolButton.QUESTION);
		ToolTipConfig config = new ToolTipConfig(AppPropertiesManager.CONSTANTS.myjobs_grid_tooltip());
		config.setMaxWidth(225);
		info.setToolTipConfig(config);
		tb.add(info);

		return tb;
	}
	
	/**
	 * Creates button used to clear the list.
	 * @return button 
	 */
	private TextButton createClearAllButton()
	{
		TextButton clearAllButton = new TextButton(AppPropertiesManager.CONSTANTS.myjobs_clear_button());
		clearAllButton.addStyleName("eppic-default-font");
		clearAllButton.addSelectHandler(new SelectHandler() {
			@Override
			public void onSelect(SelectEvent event) {
				CrkWebServiceProvider.getServiceController().deleteAllJobs(getCurrentJobsList());
			}
		});


		String iconSource = "resources/icons/clear_icon.png";
		clearAllButton.setIcon(IconHelper.getImageResource(UriUtils.fromSafeConstant(iconSource), 12, 12));
		
		return clearAllButton;
	}
	
	/**
	 * Method to return a list of current jobs
	 * @return list of job id's
	 */
	private List<String> getCurrentJobsList() {
		List<String> jobsList = new ArrayList<String>();
		for(MyJobsModel m:myJobsStore.getAll()){
			jobsList.add(m.getJobid());
		}
		
		return jobsList;
	}
	
	/**
	 * Creates jobs grid.
	 * @param myJobsColumnModel column model used for jobs grid
	 * @return grid with jobs
	 */
	private Grid<MyJobsModel> createJobsGrid(ColumnModel<MyJobsModel> myJobsColumnModel)
	{
		final Grid<MyJobsModel> myJobsGrid = new Grid<MyJobsModel>(myJobsStore, myJobsColumnModel);
		myJobsGrid.setBorders(false);
		myJobsGrid.getView().setStripeRows(true);
		myJobsGrid.getView().setColumnLines(false);
		myJobsGrid.getView().setForceFit(true);
		myJobsGrid.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		
		myJobsGrid.addStyleName("eppic-default-font");
		
		myJobsGrid.setContextMenu(new JobsPanelContextMenu(myJobsGrid));
		
		myJobsGrid.addCellClickHandler(new CellClickHandler() {
			
			@Override
			public void onCellClick(CellClickEvent event) {
				History.newItem("id/" + myJobsStore.get(event.getRowIndex()).getJobid());
				
			}
		});

		myJobsGrid.getSelectionModel().addSelectionHandler(new SelectionHandler<MyJobsModel>() {

			@Override
			public void onSelection(SelectionEvent<MyJobsModel> event) {
				if(event.getSelectedItem() != null )
				{
						History.newItem("id/" + event.getSelectedItem().getJobid());
				}
				
			}
			
		});

		new KeyNav(myJobsGrid)
		{
			@Override
            public void onDelete(NativeEvent event)
			{
				MyJobsModel selectedItem = myJobsGrid.getSelectionModel().getSelectedItem();
				if(selectedItem != null)
				{
					CrkWebServiceProvider.getServiceController().deleteJob(selectedItem.getJobid());
				}
			}
		};
		
		QuickTip gridQT = new QuickTip(myJobsGrid);
		//Bug-Fix in GXt 3.0.1
		//To fix the issue of blank Tooltips we set the delay
		gridQT.setQuickShowInterval(0);
		gridQT.getToolTipConfig().setShowDelay(0);
		
		return myJobsGrid;
	}

	/**
	 * Adds jobs to grid.
	 * @param jobs jobs to display
	 * @param selectedJobId current job
	 */
	private void setJobs(List<ProcessingInProgressData> jobs,
						String selectedJobId)
	{
		MyJobsModel itemToSelect = null;
		int itemToSelectIndex = 0;

		if(jobs != null)
		{
			int i = 0;

			for (int ii=0; ii < myJobsStore.size(); ii++)
			{
				MyJobsModel model = myJobsStore.get(ii);
				boolean found = false;
				int j=0;

				while((j < jobs.size()) && (!found))
				{
					if(jobs.get(j).getJobId().equals(model.getJobid()))
					{
						found = true;
						break;
					}

					j++;
				}

				if(!found)
				{
					myJobsStore.remove(ii);
				}
			}

			for (ProcessingInProgressData statusData : jobs)
			{
				MyJobsModel myJobsModel = new MyJobsModel(statusData.getJobId(),
														  statusData.getStatus(),
														  statusData.getInput());

				if((selectedJobId != null) &&
				   (statusData.getJobId().equals(selectedJobId)))
				{
					itemToSelect = myJobsModel;
					itemToSelectIndex = i;
				}

				MyJobsModel existingModel = myJobsStore.findModel(myJobsModel);

				if(existingModel != null)
				{
					existingModel.setStatus(statusData.getStatus());
					existingModel.setInput(statusData.getInput());
					myJobsStore.update(existingModel);
				}
				else
				{
					myJobsStore.add(myJobsModel);
				}

				i++;
			}
		}

		myJobsStore.commitChanges();

		if((selectedJobId != null) &&
			(myJobsStore.size() > 0))
		{
			myJobsGrid.getSelectionModel().select(itemToSelect, false);

			if(isJobsListFirstTimeLoaded)
			{
				myJobsGrid.getView().focusRow(itemToSelectIndex);
				isJobsListFirstTimeLoaded = false;
			}
			EventBusManager.EVENT_BUS.fireEvent(new ShowJobsPanelEvent());
		}else{
			EventBusManager.EVENT_BUS.fireEvent(new HideJobsPanelEvent());
			
		}
	}
	
	/**
	 * Selects correct job before removal.
	 * @param jobToStop identifier of the job which was removed
	 */
	private void selectPrevious(String jobToRemove)
	{
		List<MyJobsModel> currentJobs = myJobsStore.getAll();

		boolean found = false;
		int jobNr = 0;

		while((jobNr < currentJobs.size()) && (!found))
		{
			if(currentJobs.get(jobNr).getJobid().equals(jobToRemove))
			{
				found = true;
			}

			jobNr++;
		}

		jobNr -= 2;

		if(jobNr >= 0)
		{
			myJobsGrid.getSelectionModel().select(currentJobs.get(jobNr), false);
		}
		else if(myJobsStore.getAll().size() > 1)
		{
			myJobsGrid.getSelectionModel().select(currentJobs.get(1), false);
		}
		else
		{
			History.newItem("");
		}
	}
	
	/**
	 * Checks whether there is any job which was not finished.
	 * @param jobs jobs to validate
	 * @return information whether there is any job which was not finished.
	 */
	private boolean checkIfAnyJobRunning(List<ProcessingInProgressData> jobs)
	{
		boolean anyRunning = false;
				
		if(jobs != null)
		{
			int counter = 0;
			
			while((!anyRunning) &&
				  (counter < jobs.size()))
			{
				String status = jobs.get(counter).getStatus();
				
				if((status != null) &&
				   ((status.equals(StatusOfJob.QUEUING.getName())) ||
					(status.equals(StatusOfJob.RUNNING.getName())) ||
					(status.equals(StatusOfJob.WAITING.getName()))))
				{
					anyRunning = true;
				}
				
				counter++;
			}
		}
				
		return anyRunning;
	}
	
	/**
	 * Events listeners initialization.
	 */
	private void initializeEventsListeners()
	{
		EventBusManager.EVENT_BUS.addHandler(GetFocusOnJobsListEvent.TYPE, new GetFocusOnJobsListHandler() {
			
			@Override
			public void onGrabFocusOnJobsList(GetFocusOnJobsListEvent event) {
				myJobsGrid.focus();
			}
		});
		
		EventBusManager.EVENT_BUS.addHandler(JobListRetrievedEvent.TYPE, new JobListRetrievedHandler() {
			
			@Override
			public void onJobListRetrieved(JobListRetrievedEvent event) {
				setJobs(event.getJobs(), ApplicationContext.getSelectedJobId());
				ApplicationContext.setAnyJobRunning(checkIfAnyJobRunning(event.getJobs()));
			}
		});
		
		EventBusManager.EVENT_BUS.addHandler(BeforeJobDeletedEvent.TYPE, new BeforeJobRemovedHandler() {
			
			@Override
			public void onBeforeJobRemoved(BeforeJobDeletedEvent event) 
			{
				if(event.getJobToDelete().equals(ApplicationContext.getSelectedJobId()))
				{
					selectPrevious(event.getJobToDelete());
				}
			}
		});
	}
}
