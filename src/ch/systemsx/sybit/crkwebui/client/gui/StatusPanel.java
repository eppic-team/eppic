package ch.systemsx.sybit.crkwebui.client.gui;

import ch.systemsx.sybit.crkwebui.client.controllers.AppPropertiesManager;
import ch.systemsx.sybit.crkwebui.client.controllers.CrkWebServiceProvider;
import ch.systemsx.sybit.crkwebui.client.gui.util.EscapedStringGenerator;
import ch.systemsx.sybit.crkwebui.shared.model.InputType;
import ch.systemsx.sybit.crkwebui.shared.model.ProcessingInProgressData;
import ch.systemsx.sybit.crkwebui.shared.model.StatusOfJob;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.Margins;
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
import com.google.gwt.core.client.GWT;

/**
 * Panel used to display status of submitted job.
 * @author srebniak_a
 *
 */
public class StatusPanel extends DisplayPanel
{
	private FormPanel formPanel;

	private PDBIdentifierPanel pdbIdentifierPanel;
	private PDBIdentifierSubtitlePanel pdbIdentifierSubtitlePanel;
	
	private TextField<String> jobId;
	private TextField<String> status;
	private TextArea log;

	private Button killJob;

	private ToolBar statusBar;
	private Status statusProgress;
	private Status statusStepsFinished;

	public StatusPanel(int windowHeight)
	{
		init(windowHeight);
	}

	/**
	 * Initializes content of the panel.
	 */
	private void init(int windowHeight)
	{
		this.setBorders(true);
		this.addStyleName("eppic-default-top-padding");
		this.setLayout(new RowLayout());

		formPanel = new FormPanel();
		formPanel.getHeader().setVisible(false);
		formPanel.setBodyBorder(false);
		formPanel.setButtonAlign(HorizontalAlignment.CENTER);
		formPanel.setScrollMode(Scroll.AUTOY);

		pdbIdentifierPanel = new PDBIdentifierPanel();
		pdbIdentifierPanel.addStyleName("eppic-default-left-padding");
		this.add(pdbIdentifierPanel, new FormData("95%"));
		
		FormPanel breakPanel = createBreakPanel();
		this.add(breakPanel, new RowData(1, 1.1, new Margins(0)));
		
		pdbIdentifierSubtitlePanel = new PDBIdentifierSubtitlePanel();
		pdbIdentifierSubtitlePanel.addStyleName("eppic-default-left-padding");
		this.add(pdbIdentifierSubtitlePanel, new FormData("95%"));
		
		breakPanel = createBreakPanel();
		this.add(breakPanel, new RowData(1, 1.1, new Margins(0)));
		
		jobId = createJobIdField();
		formPanel.add(jobId, new FormData("95%"));

		status = createStatusField();
		formPanel.add(status, new FormData("95%"));

		log = createLogTextarea();
		formPanel.add(log, new FormData("95% -110"));

		killJob = createStopJobButton();
		
		LayoutContainer killButtonContainer = new LayoutContainer();
		killButtonContainer.setLayout(new CenterLayout());
		killButtonContainer.add(killJob);
		killButtonContainer.setHeight(30);
		formPanel.add(killButtonContainer, new FormData("95%"));

		statusBar = createStatusBar();
		formPanel.setBottomComponent(statusBar);
		
		this.add(formPanel, new RowData(1,1));

	}
	
	/**
	 * Creates panel used to separate rows.
	 * @return break panel
	 */
	private FormPanel createBreakPanel()
	{
		FormPanel breakPanel = new FormPanel();
		breakPanel.setBorders(false);
		breakPanel.setBodyBorder(false);
		breakPanel.setPadding(0);
		breakPanel.getHeader().setVisible(false);
		return breakPanel;
	}
	
	/**
	 * Creates field used to store identifier of the job
	 * @return jobid field
	 */
	private TextField<String> createJobIdField()
	{
		TextField<String> jobId = new TextField<String>();
		jobId.setFieldLabel(AppPropertiesManager.CONSTANTS.status_panel_jobId());
		jobId.setReadOnly(true);
		return jobId;
	}
	
	/**
	 * Creates field used to store status of processing (running, waiting, error, etc.)
	 * @return status field
	 */
	private TextField<String> createStatusField()
	{
		TextField<String> status = new TextField<String>();
		status.setFieldLabel(AppPropertiesManager.CONSTANTS.status_panel_status());
		status.setReadOnly(true);
		return status;
	}
	
	/**
	 * Creates text area used to store log of processing.
	 * @return log textarea
	 */
	private TextArea createLogTextarea()
	{
		TextArea log = new TextArea();
		log.setFieldLabel(AppPropertiesManager.CONSTANTS.status_panel_log());
		log.setReadOnly(true);
		log.addInputStyleName("eppic-status-log");
		return log;
	}
	
	/**
	 * Creates button used to stop execution of the job
	 * @return stop job button
	 */
	private Button createStopJobButton()
	{
		Button killJob = new Button(AppPropertiesManager.CONSTANTS.status_panel_stop(), new SelectionListener<ButtonEvent>() {

			public void componentSelected(ButtonEvent ce)
			{
				CrkWebServiceProvider.getServiceController().stopJob(jobId.getValue());
			}
		});

		killJob.setWidth(80);
		
		return killJob;
	}

	/**
	 * Creates status toolbar.
	 * @return status toolbar
	 */
	private ToolBar createStatusBar()
	{
		ToolBar statusBar = new ToolBar();

		statusProgress = createStatusProgress();
		statusBar.add(statusProgress);
		statusBar.add(new FillToolItem());

	    statusStepsFinished = createStatusStepsFinished();
	    statusBar.add(statusStepsFinished);
	    
	    return statusBar;
	}
	
	/**
	 * Creates status item used to display busy icon.
	 * @return status item
	 */
	private Status createStatusProgress()
	{
		Status statusProgress = new Status();
		statusProgress.setText("");
		statusProgress.setWidth(150);
		return statusProgress;
	}
	
	/**
	 * Creates status item specifying current step and nr of total steps.
	 * @return step item
	 */
	private Status createStatusStepsFinished()
	{
		Status statusStepsFinished = new Status();
	    statusStepsFinished.setWidth(100);
	    statusStepsFinished.setText("");
	    statusStepsFinished.setBox(true);
	    return statusStepsFinished;
	}

	/**
	 * Sets content of the status panel.
	 * @param statusData status data of selected job
	 */
	public void fillData(ProcessingInProgressData statusData)
	{
		int scrollBefore = log.getElement().getFirstChildElement().getScrollTop();
		log.setValue(statusData.getLog());
		log.getElement().getFirstChildElement().setScrollTop(scrollBefore);

		status.setValue(String.valueOf(statusData.getStatus()));
		jobId.setValue(statusData.getJobId());
		pdbIdentifierPanel.setPDBText(statusData.getInput(), null, null, 0, statusData.getInputType());

		if((status.getValue() != null) &&
		   ((status.getValue().equals(StatusOfJob.RUNNING.getName())) ||
			(status.getValue().equals(StatusOfJob.WAITING.getName())) ||
			(status.getValue().equals(StatusOfJob.QUEUING.getName()))))
		{
			String subTitle = AppPropertiesManager.CONSTANTS.status_panel_subtitle();
			subTitle = subTitle.replaceFirst("%s", GWT.getHostPageBaseURL() + 
												   "Crkwebui.html?#id/" + 
												   EscapedStringGenerator.generateEscapedString(statusData.getJobId()));
			pdbIdentifierSubtitlePanel.setPDBIdentifierSubtitle(subTitle);
			killJob.setVisible(true);
			statusProgress.setBusy(statusData.getStep().getCurrentStep());

			if(statusData.getStep().getTotalNumberOfSteps() != 0)
			{
				statusStepsFinished.setText(AppPropertiesManager.CONSTANTS.status_panel_step_counter() +
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
		}
		else
		{
			pdbIdentifierSubtitlePanel.setPDBIdentifierSubtitle("");
			killJob.setVisible(false);
			statusProgress.clearStatus("");
			statusStepsFinished.clearStatus("");
			statusStepsFinished.setVisible(false);
		}
		
		this.layout(true);
	}

	/**
	 * Cleans content of status panel.
	 */
	public void cleanData()
	{
		log.setValue("");
		status.setValue("");
		jobId.setValue("");
		pdbIdentifierPanel.setPDBText("", null, null, 0, InputType.NONE.getIndex());
		pdbIdentifierSubtitlePanel.setPDBIdentifierSubtitle("");
		
		this.layout(true);
	}
}
