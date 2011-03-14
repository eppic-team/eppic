package ch.systemsx.sybit.crkwebui.client.gui;

import ch.systemsx.sybit.crkwebui.client.controllers.MainController;
import ch.systemsx.sybit.crkwebui.shared.model.ProcessingInProgressData;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;

public class StatusPanel extends DisplayPanel 
{
	private FormPanel formPanel;
	
	private TextField<String> jobId;
	private TextField<String> status;
	private TextArea log;
	
	private Button killJob;
	
	public StatusPanel(MainController mainController) 
	{
		super(mainController);
		init();
	}

	public void init() 
	{
//		VBoxLayout vBoxLayout = new VBoxLayout();
//		vBoxLayout.setVBoxLayoutAlign(VBoxLayoutAlign.CENTER);
//		this.setLayout(vBoxLayout);
//		this.setBorders(false);
		this.setLayout(new FitLayout());
		this.setBorders(true);
		
		formPanel = new FormPanel();
		formPanel.getHeader().setVisible(false);
		formPanel.setBodyBorder(false);
		formPanel.setButtonAlign(HorizontalAlignment.CENTER);
		formPanel.setScrollMode(Scroll.AUTOY);
		formPanel.setHeight(mainController.getWindowHeight() - 100);

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
//		log.setHeight(400);

		formPanel.add(log, new FormData("95% -60"));
		
		killJob = new Button(MainController.CONSTANTS.status_panel_stop(), new SelectionListener<ButtonEvent>() {

			public void componentSelected(ButtonEvent ce) 
			{
				mainController.killJob(jobId.getValue());
			}
		});
		
		formPanel.setButtonAlign(HorizontalAlignment.CENTER);
//		killJob.setVisible(false);
		formPanel.addButton(killJob);
		
		this.add(formPanel);

	}

	public void fillData(ProcessingInProgressData statusData) 
	{
		log.setValue(statusData.getLog());
		status.setValue(String.valueOf(statusData.getStatus()));
		jobId.setValue(statusData.getJobId());
		
		if((status.getValue() != null) && (status.getValue().equals("Running")))
		{
			killJob.setVisible(true);
		}
		else
		{
			killJob.setVisible(false);
		}
	}

	public void cleanData() 
	{
		log.setValue("");
		status.setValue("");
		jobId.setValue("");
	}
}
