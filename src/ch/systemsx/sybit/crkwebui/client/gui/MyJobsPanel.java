package ch.systemsx.sybit.crkwebui.client.gui;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.sybit.crkwebui.client.controllers.MainController;
import ch.systemsx.sybit.crkwebui.client.model.MyJobsModel;
import ch.systemsx.sybit.crkwebui.shared.model.ProcessingInProgressData;
import ch.systemsx.sybit.crkwebui.shared.model.StatusOfJob;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.Hyperlink;

/**
 * This panel is used to display the list of all jobs connected to the current session
 * @author srebniak_a
 *
 */
public class MyJobsPanel extends ContentPanel 
{
	private MainController mainController;

	private Grid<MyJobsModel> myJobsGrid;
	private ListStore<MyJobsModel> myJobsStore;
	private ColumnModel myJobsColumnModel;

	private Button addNew;

	public MyJobsPanel(final MainController mainController) 
	{
		this.mainController = mainController;
//		this.setLayout(new RowLayout(Orientation.VERTICAL));
		this.setLayout(new FitLayout());
		this.setScrollMode(Scroll.NONE);
		this.setHeading(MainController.CONSTANTS.myjobs_panel_head());

		ToolBar toolBar = new ToolBar();

		addNew = new Button(MainController.CONSTANTS.myjobs_panel_new_button(), new SelectionListener<ButtonEvent>() {

			public void componentSelected(ButtonEvent ce) 
			{
				History.newItem("");
				mainController.displayInputView();
			}
		});

		toolBar.add(addNew);

//		Button test = new Button("Test", new SelectionListener<ButtonEvent>() {
//
//			public void componentSelected(ButtonEvent ce) 
//			{
//			}
//		});
//		ToolTipConfig toolTipConfig = new ToolTipConfig();
//		toolTipConfig.setShowDelay(0);
//		toolTipConfig.setText("This is tooltip");
//		test.setToolTip(toolTipConfig);
//
//		toolBar.add(test);

		this.setTopComponent(toolBar);

//		this.getHeader().addTool(
//				new ToolButton("x-tool-gear",
//						new SelectionListener<IconButtonEvent>() {
//
//							public void componentSelected(IconButtonEvent ce)
//							{
//								mainController.getJobsForCurrentSession();
//							}
//
//						}));

		GridCellRenderer<MyJobsModel> jobRenderer = new GridCellRenderer<MyJobsModel>() 
		{
			@Override
			public Object render(MyJobsModel model, String property,
					ColumnData config, int rowIndex, int colIndex,
					ListStore<MyJobsModel> store, Grid<MyJobsModel> grid) {
				String input = (String) model.get("input");
				if(input.contains("."))
				{
					input = input.substring(0, input.indexOf("."));
				}
				
				Hyperlink link = new Hyperlink(input, "id/" + myJobsStore.getAt(rowIndex).getJobid());
				return link;
			}
		};

		GridCellRenderer<MyJobsModel> statusRenderer = new GridCellRenderer<MyJobsModel>()
		{

			@Override
			public Object render(MyJobsModel model, String property,
					ColumnData config, int rowIndex, int colIndex,
					ListStore<MyJobsModel> store, Grid<MyJobsModel> grid) {
				String value = (String) model.get(property);
				String color = "black";

				if (value == null) {
					return value;
				} else if (value.equals(StatusOfJob.ERROR)) {
					color = "red";
				} else if (value.equals(StatusOfJob.FINISHED)) {
					color = "green";
				} else {
					return value;
				}

				return "<span qtitle='"
						+ myJobsColumnModel.getColumnById(property).getHeader()
						+ "' qtip='" + value
						+ "' style='font-weight: bold;color:" + color + "'>"
						+ value + "</span>";
			}
		};

		List<ColumnConfig> configs = new ArrayList<ColumnConfig>();

		ColumnConfig column = new ColumnConfig();
		column.setId("input");
		column.setHeader(MainController.CONSTANTS.myjobs_grid_input());
		column.setDataIndex("input");
		column.setWidth(90);
		column.setRowHeader(true);
		column.setRenderer(jobRenderer);
		configs.add(column);

		column = new ColumnConfig();
		column.setId("status");
		column.setHeader(MainController.CONSTANTS.myjobs_grid_status());
		column.setDataIndex("status");
		column.setWidth(90);
		column.setRowHeader(true);
		column.setRenderer(statusRenderer);
		configs.add(column);

		myJobsStore = new ListStore<MyJobsModel>();
		myJobsColumnModel = new ColumnModel(configs);

		myJobsGrid = new Grid<MyJobsModel>(myJobsStore, myJobsColumnModel);
		myJobsGrid.setStyleAttribute("borderTop", "none");
		myJobsGrid.setAutoExpandColumn("input");
		myJobsGrid.setBorders(false);
		myJobsGrid.setStripeRows(true);
		myJobsGrid.setColumnLines(true);
		myJobsGrid.setColumnReordering(true);
//		myJobsGrid.setAutoHeight(true);
//		myJobsGrid.setAutoWidth(true);
		
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
		
//		this.add(myJobsGrid, new RowData(1, 1, new Margins(0)));
		this.add(myJobsGrid);
	}

	public void setJobs(List<ProcessingInProgressData> jobs) 
	{
		MyJobsModel itemToSelect = null;
		int itemToSelectIndex = 0;
		
		List<MyJobsModel> data = new ArrayList<MyJobsModel>();

		if(jobs != null)
		{
			int i = 0;
			
			for (ProcessingInProgressData statusData : jobs)
			{
				MyJobsModel myJobsModel = new MyJobsModel(statusData.getJobId(),
														  statusData.getStatus(),
														  statusData.getInput());
				
				if(statusData.getJobId().equals(mainController.getSelectedJobId()))
				{
					itemToSelect = myJobsModel;
					itemToSelectIndex = i; 
				}
				
//				data.add(myJobsModel);
				
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
		
//		myJobsStore.removeAll();
//		myJobsStore.add(data);
//		myJobsGrid.reconfigure(myJobsStore, myJobsColumnModel);
		myJobsGrid.getView().refresh(false);
		
		if((mainController.getSelectedJobId() != null) &&
			(myJobsGrid.getStore().getCount() > 0))
		{
			myJobsGrid.getSelectionModel().select(itemToSelect, false);
			
			if(mainController.isJobsListFirstTimeLoaded())
			{
				myJobsGrid.getView().focusRow(itemToSelectIndex);
				mainController.setJobsListFirstTimeLoaded(false);
			}
		}
	}
}
