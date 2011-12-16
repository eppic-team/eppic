package ch.systemsx.sybit.crkwebui.client.gui;

import ch.systemsx.sybit.crkwebui.client.controllers.MainController;
import ch.systemsx.sybit.crkwebui.shared.model.InputType;
import ch.systemsx.sybit.crkwebui.shared.model.ProcessingInProgressData;
import ch.systemsx.sybit.crkwebui.shared.model.StatusOfJob;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Status;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.CenterLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;

/**
 * This panel is used to display status of the submitted job
 * @author srebniak_a
 *
 */
public class StatusPanel extends DisplayPanel 
{
	private FormPanel formPanel;
	
	private PDBIdentifierPanel pdbIdentifierPanel;
	private TextField<String> jobId;
	private TextField<String> status;
	private TextArea log;
//	private ProgressBar progressBar;
//	
//	private String currentStep = "";
	
	private Button killJob;
	
	private ToolBar statusBar;
	private Status statusProgress;
	private Status statusStepsFinished;  
	
	public StatusPanel(MainController mainController) 
	{
		super(mainController);
		init();
	}

	public void init() 
	{
		this.setBorders(true);
		this.setStyleAttribute("padding-top", "10px");
		this.setLayout(new RowLayout());
		
		formPanel = new FormPanel();
		formPanel.getHeader().setVisible(false);
		formPanel.setBodyBorder(false);
		formPanel.setButtonAlign(HorizontalAlignment.CENTER);
		formPanel.setScrollMode(Scroll.AUTOY);
		formPanel.setHeight(mainController.getWindowHeight() - 100);
		
		pdbIdentifierPanel = new PDBIdentifierPanel(mainController);
		pdbIdentifierPanel.setStyleAttribute("padding-left", "10px");
		this.add(pdbIdentifierPanel, new FormData("95%"));

		jobId = new TextField<String>();
		jobId.setFieldLabel(MainController.CONSTANTS.status_panel_jobId());
		jobId.setReadOnly(true);
		formPanel.add(jobId, new FormData("95%"));

		status = new TextField<String>();
		status.setFieldLabel(MainController.CONSTANTS.status_panel_status());
		status.setReadOnly(true);
		formPanel.add(status, new FormData("95%"));

		log = new TextArea();
		log.setFieldLabel(MainController.CONSTANTS.status_panel_log());
		log.setReadOnly(true);

		formPanel.add(log, new FormData("95% -110"));

//		LayoutContainer progressBarContainer = new LayoutContainer();
//		progressBarContainer.setLayout(new CenterLayout());
//		progressBarContainer.setHeight(20);
//		progressBar = new ProgressBar();
//		progressBar.setBounds(0, 0, 400, 20);
//		progressBarContainer.add(progressBar);
//		
//		formPanel.add(progressBarContainer, new FormData("100%"));
//		
//		Timer autoRefreshMyJobs = new Timer() 
//		{
//			private float counter = 0;
//			
//			public void run() 
//			{
//				progressBar.updateProgress(counter / 100, currentStep);
//				counter += 10;
//				
//				if(counter > 100)
//				{
//					counter = 0;
//				}
//			}
//		};
//
//		autoRefreshMyJobs.scheduleRepeating(500);
		
		killJob = new Button(MainController.CONSTANTS.status_panel_stop(), new SelectionListener<ButtonEvent>() {

			public void componentSelected(ButtonEvent ce) 
			{
				mainController.stopJob(jobId.getValue());
			}
		});
		
		killJob.setWidth(80);
		LayoutContainer killButtonContainer = new LayoutContainer();
		killButtonContainer.setLayout(new CenterLayout());
		killButtonContainer.add(killJob);
		killButtonContainer.setHeight(30);
		formPanel.add(killButtonContainer, new FormData("95%"));
		
		statusBar = new ToolBar();  
		  
		statusProgress = new Status();  
		statusProgress.setText("");  
		statusProgress.setWidth(150);  
		statusBar.add(statusProgress);  
		statusBar.add(new FillToolItem());  
	  
	    statusStepsFinished = new Status();  
	    statusStepsFinished.setWidth(100);  
	    statusStepsFinished.setText("");  
	    statusStepsFinished.setBox(true);
	    statusBar.add(statusStepsFinished);  
	    
	    formPanel.setBottomComponent(statusBar);
		this.add(formPanel, new RowData(1,1));

	}

	public void fillData(ProcessingInProgressData statusData) 
	{
		int scrollBefore = log.getElement().getFirstChildElement().getScrollTop();
		log.setValue(statusData.getLog());
		log.getElement().getFirstChildElement().setScrollTop(scrollBefore);
		
		status.setValue(String.valueOf(statusData.getStatus()));
		jobId.setValue(statusData.getJobId());
		pdbIdentifierPanel.setPDBText(statusData.getInput(), null, statusData.getInputType());
		
		if((status.getValue() != null) && (status.getValue().equals(StatusOfJob.RUNNING.getName())))
		{
			killJob.setVisible(true);
			statusProgress.setBusy(statusData.getStep().getCurrentStep());
			
			if(statusData.getStep().getTotalNumberOfSteps() != 0)
			{
				statusStepsFinished.setText(MainController.CONSTANTS.status_panel_step_counter() +
											": " +
											statusData.getStep().getCurrentStepNumber() +
											"/" +
											statusData.getStep().getTotalNumberOfSteps()
											);
			}
			else
			{
				statusStepsFinished.setText("");
			}
			
			statusStepsFinished.setVisible(true);
//			progressBar.setVisible(true);
		}
		else
		{
			killJob.setVisible(false);
			statusProgress.clearStatus("");
			statusStepsFinished.clearStatus("");
			statusStepsFinished.setVisible(false);
//			progressBar.setVisible(false);
		}
	}

	public void cleanData() 
	{
		log.setValue("");
		status.setValue("");
		jobId.setValue("");
		pdbIdentifierPanel.setPDBText("", null, InputType.NONE.getIndex());
	}
}
