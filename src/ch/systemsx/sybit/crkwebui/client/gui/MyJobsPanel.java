package ch.systemsx.sybit.crkwebui.client.gui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.systemsx.sybit.crkwebui.client.controllers.MainController;
import ch.systemsx.sybit.crkwebui.client.model.MyJobsModel;
import ch.systemsx.sybit.crkwebui.shared.model.ProcessingInProgressData;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.IconHelper;
import com.extjs.gxt.ui.client.util.KeyNav;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.user.client.History;

/**
 * Panel used to display the list of all jobs connected to the current session.
 * @author srebniak_a
 *
 */
public class MyJobsPanel extends ContentPanel 
{
	private MainController mainController;

	private ContentPanel myJobsGridContainer;
	private Grid<MyJobsModel> myJobsGrid;
	private List<ColumnConfig> myJobsConfigs;
	private ListStore<MyJobsModel> myJobsStore;
	private ColumnModel myJobsColumnModel;
	private Map<String, Integer> initialColumnWidth;

	private Button addNew;
	
	private boolean isJobsListFirstTimeLoaded = true;

	public MyJobsPanel(final MainController mainController) 
	{
		this.mainController = mainController;
		this.setLayout(new RowLayout(Orientation.VERTICAL));
		this.setHeading(MainController.CONSTANTS.myjobs_panel_head());

		ToolBar toolBar = new ToolBar();

		addNew = new Button(MainController.CONSTANTS.myjobs_panel_new_button(), new SelectionListener<ButtonEvent>() {

			public void componentSelected(ButtonEvent ce) 
			{
				History.newItem("");
				
				Scheduler.get().scheduleDeferred(new ScheduledCommand() {
					@Override
					public void execute() {
						if((mainController.getMainViewPort().getCenterPanel().getDisplayPanel() != null) &&
						   (mainController.getMainViewPort().getCenterPanel().getDisplayPanel() instanceof InputDataPanel))
						{
							((InputDataPanel)mainController.getMainViewPort().getCenterPanel().getDisplayPanel()).getPdbCodeField().focus();
						}
					}
			    });
				
			}
		});
		
		String addIconSource = "resources/icons/add_icon.png";
		addNew.setIcon(IconHelper.createPath(addIconSource));

		toolBar.add(addNew);

		this.setTopComponent(toolBar);
		
		myJobsConfigs = createColumnConfig();

		myJobsStore = new ListStore<MyJobsModel>();
		myJobsColumnModel = new ColumnModel(myJobsConfigs);

		myJobsGrid = new Grid<MyJobsModel>(myJobsStore, myJobsColumnModel);
		myJobsGrid.setStyleAttribute("borderTop", "none");
		myJobsGrid.setBorders(false);
		myJobsGrid.setStripeRows(true);
		myJobsGrid.setColumnLines(true);
		myJobsGrid.setAutoWidth(true);
		myJobsGrid.getView().setForceFit(true);
		
		myJobsGrid.addListener(Events.CellClick, new Listener<GridEvent>()
		{
			@Override
			public void handleEvent(GridEvent be) 
			{
				History.newItem("id/" + myJobsStore.getAt(be.getRowIndex()).getJobid());
			}
		});
		
		myJobsGrid.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		myJobsGrid.getSelectionModel().addSelectionChangedListener(new SelectionChangedListener<MyJobsModel>() 
		{
			@Override
			public void selectionChanged(SelectionChangedEvent<MyJobsModel> se) 
			{
				if(se.getSelectedItem() != null)
				{
					History.newItem("id/" + se.getSelectedItem().getJobid());
				}
			}
		});
		
		new KeyNav<ComponentEvent>(myJobsGrid)
		{
			@Override
            public void onDelete(ComponentEvent ce) 
			{
				MyJobsModel selectedItem = myJobsGrid.getSelectionModel().getSelectedItem();
				if(selectedItem != null)
				{
					mainController.deleteJob(selectedItem.getJobid());
				}
			}
		};
		
		this.add(myJobsGrid, new RowData(1, 1, new Margins(0)));
	}
	
	/**
	 * Creates columns configurations for residues summary grid.
	 * @return columns configurations for residues summary grid
	 */
	private List<ColumnConfig> createColumnConfig() 
	{
		List<ColumnConfig> configs = GridColumnConfigGenerator.createColumnConfigs(mainController,
																				   "jobs",
																				   new MyJobsModel());

		if(configs != null)
		{
			initialColumnWidth = new HashMap<String, Integer>();
			
			for(ColumnConfig columnConfig : configs)
			{
				initialColumnWidth.put(columnConfig.getId(), columnConfig.getWidth());
			}
		}

		return configs;
	}

	/**
	 * Adds jobs to grid.
	 * @param jobs jobs to display
	 */
	public void setJobs(List<ProcessingInProgressData> jobs) 
	{
		MyJobsModel itemToSelect = null;
		int itemToSelectIndex = 0;
		
		if(jobs != null)
		{
			int i = 0;
			
			List<MyJobsModel> currentModels = myJobsStore.getModels();
			for(MyJobsModel model : currentModels)
			{
				boolean found = false;
				int j=0;
				
				while((j < jobs.size()) && (!found))
				{
					if(jobs.get(j).getJobId().equals(model.get("jobid")))
					{
						found = true;
					}
					
					j++;
				}
				
				if(!found)
				{
					myJobsStore.remove(model);
				}
			}
			
			for (ProcessingInProgressData statusData : jobs)
			{
				MyJobsModel myJobsModel = new MyJobsModel(statusData.getJobId(),
														  statusData.getStatus(),
														  statusData.getInput());
				
				if((mainController.getSelectedJobId() != null) &&
				   (statusData.getJobId().equals(mainController.getSelectedJobId())))
				{
					itemToSelect = myJobsModel;
					itemToSelectIndex = i; 
				}
				
				MyJobsModel existingModel = myJobsStore.findModel("jobid", statusData.getJobId());
				
				if(existingModel != null)
				{
					existingModel.set("status", statusData.getStatus());
					existingModel.set("input", statusData.getInput());
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
		

		if((mainController.getSelectedJobId() != null) &&
			(myJobsStore.getCount() > 0))
		{
			myJobsGrid.getSelectionModel().select(itemToSelect, false);
			
			if(isJobsListFirstTimeLoaded)
			{
				myJobsGrid.getView().focusRow(itemToSelectIndex);
				isJobsListFirstTimeLoaded = false;
			}
		}
	}
	
	/**
	 * Retrieves grid containing jobs.
	 * @return grid containing jobs
	 */
	public Grid<MyJobsModel> getMyJobsGrid()
	{
		return myJobsGrid;
	}

	/**
	 * Selects correct job after removal.
	 * @param jobToStop identifier of the job which was stopped
	 */
	public void selectPrevious(String jobToStop) 
	{
		List<MyJobsModel> currentJobs = myJobsStore.getModels();
		
		boolean found = false;
		int jobNr = 0;
		
		while((jobNr < currentJobs.size()) && (!found))
		{
			if(currentJobs.get(jobNr).get("jobid").equals(jobToStop))
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
		else if(myJobsStore.getModels().size() > 1)
		{
			myJobsGrid.getSelectionModel().select(currentJobs.get(0), false);
		}
		else
		{
			History.newItem("");
		}
	}
}
