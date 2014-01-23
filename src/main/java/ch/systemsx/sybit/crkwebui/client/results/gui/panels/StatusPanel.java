package ch.systemsx.sybit.crkwebui.client.results.gui.panels;

import ch.systemsx.sybit.crkwebui.client.commons.appdata.AppPropertiesManager;
import ch.systemsx.sybit.crkwebui.client.commons.appdata.ApplicationContext;
import ch.systemsx.sybit.crkwebui.client.commons.gui.panels.DisplayPanel;
import ch.systemsx.sybit.crkwebui.client.commons.services.eppic.CrkWebServiceProvider;
import ch.systemsx.sybit.crkwebui.client.commons.util.EscapedStringGenerator;
import ch.systemsx.sybit.crkwebui.client.commons.util.StyleGenerator;
import ch.systemsx.sybit.crkwebui.shared.model.InputType;
import ch.systemsx.sybit.crkwebui.shared.model.ProcessingInProgressData;
import ch.systemsx.sybit.crkwebui.shared.model.StatusOfJob;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.HTML;
import com.sencha.gxt.core.client.util.Padding;
import com.sencha.gxt.widget.core.client.FramedPanel;
import com.sencha.gxt.widget.core.client.Status;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.CenterLayoutContainer;
import com.sencha.gxt.widget.core.client.container.HorizontalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.HorizontalLayoutContainer.HorizontalLayoutData;
import com.sencha.gxt.widget.core.client.container.SimpleContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.BoxLayoutContainer.BoxLayoutPack;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
import com.sencha.gxt.widget.core.client.form.FieldLabel;
import com.sencha.gxt.widget.core.client.form.FormPanel;
import com.sencha.gxt.widget.core.client.form.FormPanel.LabelAlign;
import com.sencha.gxt.widget.core.client.form.TextArea;
import com.sencha.gxt.widget.core.client.toolbar.FillToolItem;
import com.sencha.gxt.widget.core.client.toolbar.ToolBar;

/**
 * Panel used to display status of submitted job.
 * @author srebniak_a
 *
 */
public class StatusPanel extends DisplayPanel
{
	private static final int LABEL_WIDTH = 100;
	
	private FormPanel formPanel;

	private IdentifierHeaderPanel identifierHeaderPanel;
	
	private HTML jobId;
	private HTML status;
	private TextArea log;

	private TextButton killJob;

	private ToolBar statusBar;
	private Status statusProgress;
	private Status statusStepsFinished;

	public StatusPanel()
	{
		init();
	}

	/**
	 * Initializes content of the panel.
	 */
	private void init()
	{
		
		DockLayoutPanel dock = new DockLayoutPanel(Unit.PX);
		dock.addStyleName("eppic-default-font");
		
		identifierHeaderPanel = new IdentifierHeaderPanel(ApplicationContext.getWindowData().getWindowWidth() - 150);
		dock.addNorth(identifierHeaderPanel,60);
		
		HorizontalLayoutContainer statusContainer = new HorizontalLayoutContainer();
		statusContainer.getElement().setPadding(new Padding(0));
		//statusContainer.setScrollMode(ScrollMode.AUTOY);

		FramedPanel framedPanel = new FramedPanel();
		framedPanel.getHeader().setVisible(false);
		framedPanel.setButtonAlign(BoxLayoutPack.CENTER);
		
		formPanel = new FormPanel();
		framedPanel.setWidget(formPanel);
		
		VerticalLayoutContainer formContainer = new VerticalLayoutContainer();
		
		formContainer.add(new SimpleContainer(), new VerticalLayoutData(-1,20));
		
		formContainer.add(createJobIdField(), new VerticalLayoutData(-1, 20));
		
		formContainer.add(createStatusField(), new VerticalLayoutData(-1, 20));

		log = createLogTextarea();
		FieldLabel logLabel = new FieldLabel(log);
		logLabel.getElement().applyStyles("fontWeight:bold");
		logLabel.setHTML(StyleGenerator.defaultFontStyle(AppPropertiesManager.CONSTANTS.status_panel_log()));
		logLabel.setLabelAlign(LabelAlign.TOP);
		logLabel.addStyleName("eppic-status-label");
		formContainer.add(logLabel, new VerticalLayoutData(1,1));

		killJob = createStopJobButton();
		
		CenterLayoutContainer killButtonContainer = new CenterLayoutContainer();
		killButtonContainer.add(killJob);
		killButtonContainer.setHeight(30);
		formContainer.add(killButtonContainer, new VerticalLayoutData(1,60));

		statusBar = createStatusBar();
		formContainer.add(statusBar, new VerticalLayoutData(1,40));
		
		formPanel.setWidget(formContainer);
		
		statusContainer.add(new SimpleContainer(), new HorizontalLayoutData(0.05,1));
		statusContainer.add(formPanel, new HorizontalLayoutData(0.90,1));
		statusContainer.add(new SimpleContainer(), new HorizontalLayoutData(0.05,1));
		
		dock.add(statusContainer);
		
		this.setData(dock);

	}
	
	/**
	 * Create status field
	 */
	private HorizontalLayoutContainer createStatusField(){
		HorizontalLayoutContainer con = new HorizontalLayoutContainer();
		status = new HTML();
		HTML statusLabel = new HTML(AppPropertiesManager.CONSTANTS.status_panel_status()+":");
		statusLabel.setWidth(LABEL_WIDTH+"px");
		statusLabel.addStyleName("eppic-status-label");
		
		con.add(statusLabel);
		con.add(status);
		
		return con;
	}
	
	/**
	 * Create job id field
	 */
	private HorizontalLayoutContainer createJobIdField(){
		HorizontalLayoutContainer con = new HorizontalLayoutContainer();
		jobId = new HTML();
		HTML jobIdLabel = new HTML(AppPropertiesManager.CONSTANTS.status_panel_jobId()+":");
		jobIdLabel.setWidth(LABEL_WIDTH+"px");
		jobIdLabel.addStyleName("eppic-status-label");
		
		con.add(jobIdLabel);
		con.add(jobId);
		
		return con;
	}
	
	/**
	 * Creates text area used to store log of processing.
	 * @return log textarea
	 */
	private TextArea createLogTextarea()
	{
		TextArea log = new TextArea();
		log.setReadOnly(true);
		log.addStyleName("eppic-status-log");
		return log;
	}
	
	/**
	 * Creates button used to stop execution of the job
	 * @return stop job button
	 */
	private TextButton createStopJobButton()
	{
		TextButton killJob = new TextButton(AppPropertiesManager.CONSTANTS.status_panel_stop());
		killJob.addSelectHandler(new SelectHandler() {
			
			@Override
			public void onSelect(SelectEvent event) {
				CrkWebServiceProvider.getServiceController().stopJob(jobId.getHTML());
				
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
		statusProgress.setWidth(300);
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
	    //statusStepsFinished.setBox(true);
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

		status.setHTML(String.valueOf(statusData.getStatus()));
		jobId.setHTML(statusData.getJobId());
		identifierHeaderPanel.setPDBText(statusData.getInput(), null, null, 0, 0, statusData.getInputType());
		identifierHeaderPanel.setEmptyDownloadResultsLink();

		if((status.getHTML() != null) &&
		   ((status.getHTML().equals(StatusOfJob.RUNNING.getName())) ||
			(status.getHTML().equals(StatusOfJob.WAITING.getName())) ||
			(status.getHTML().equals(StatusOfJob.QUEUING.getName()))))
		{
			String subTitle = AppPropertiesManager.CONSTANTS.status_panel_subtitle();
			subTitle = subTitle.replaceFirst("%s", GWT.getHostPageBaseURL() + 
												   "Crkwebui.html?#id/" + 
												   EscapedStringGenerator.generateEscapedString(statusData.getJobId()));
			identifierHeaderPanel.setPDBIdentifierSubtitle(subTitle);
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
		    identifierHeaderPanel.setPDBIdentifierSubtitle("");
			killJob.setVisible(false);
			statusProgress.clearStatus("");
			statusStepsFinished.clearStatus("");
			statusStepsFinished.setVisible(false);
		}
	}

	/**
	 * Cleans content of status panel.
	 */
	public void cleanData()
	{
		log.setValue("");
		status.setHTML("");
		jobId.setHTML("");
		identifierHeaderPanel.setPDBText("", null, null, 0, 0, InputType.NONE.getIndex());
		identifierHeaderPanel.setPDBIdentifierSubtitle("");
		identifierHeaderPanel.setEmptyDownloadResultsLink();
	}
}
