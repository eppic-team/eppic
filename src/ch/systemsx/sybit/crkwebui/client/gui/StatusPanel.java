package ch.systemsx.sybit.crkwebui.client.gui;

import ch.systemsx.sybit.crkwebui.client.controllers.MainController;
import ch.systemsx.sybit.crkwebui.shared.model.StatusData;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FormData;

public class StatusPanel extends FormPanel 
{
	private MainController mainController;
	
	private TextField<String> jobId;
	private TextField<String> status;
	private TextArea log;
	
	public StatusPanel(MainController mainController)
	{
		this.mainController = mainController;
		
		init();
	}
	
	public void init()
	{
	    this.getHeader().setVisible(false);
	    this.setBorders(false);
	    this.setBodyBorder(false);
	    this.setButtonAlign(HorizontalAlignment.CENTER); 
	    this.setWidth(700);  
	  
	    jobId = new TextField<String>();  
	    jobId.setFieldLabel("Job Id");
	    jobId.setReadOnly(true);
	    this.add(jobId, new FormData("100%"));  
	    
	    status = new TextField<String>();  
	    status.setFieldLabel("Status");
	    status.setReadOnly(true);
	    this.add(status, new FormData("100%")); 
	    
	    log = new TextArea();
	    log.setFieldLabel("Log");
	    log.setHeight(400);
	    
	    this.add(log, new FormData("100%"));
	}
	
	public void fillData(StatusData statusData)
	{
		log.setValue(statusData.getLog());
		status.setValue(String.valueOf(statusData.getStatus()));
		jobId.setValue(statusData.getJobId());
	}
	
	public void cleanData()
	{
		log.setValue("");
		status.setValue("");
		jobId.setValue("");
	}
	
//	private Label jobId;
//	private Label status;
//	private TextArea log;
//	private Button killJobButton;
//	
//	public StatusPanel(final MainController mainController)
//	{
//		this.mainController = mainController;
//		
//		FlexTable layout = new FlexTable();
//	    layout.setCellSpacing(6);
//	    layout.setWidth("1000px");
//	    FlexCellFormatter cellFormatter = layout.getFlexCellFormatter();
//
//	    // Add a title to the form
//	    layout.setHTML(0, 0, "CRK - status");
//	    cellFormatter.setColSpan(0, 0, 2);
//	    cellFormatter.setHorizontalAlignment(
//	        0, 0, HasHorizontalAlignment.ALIGN_CENTER);
//
//	    layout.setHTML(1, 0, "Job Id");
//	    jobId = new Label();
//	    layout.setWidget(1, 1, jobId);
//	    
//	    layout.setHTML(2, 0, "Status");
//	    status = new Label();
//	    layout.setWidget(2, 1, status);
//	    
//	    layout.setHTML(3, 0, "Log");
//	    log = new TextArea();
//	    log.setWidth("450");
//	    log.setHeight("300");
//	    layout.setWidget(3, 1, log);
//	    
//	    killJobButton = new Button("Kill", new ClickHandler() 
//	    {
//			@Override
//			public void onClick(ClickEvent event) 
//			{
//				mainController.killJob(jobId.getText());
//			}
//		});
//	    layout.setWidget(4, 0, killJobButton);
//	    cellFormatter.setColSpan(4, 0, 2);
//	    cellFormatter.setHorizontalAlignment(
//	        4, 0, HasHorizontalAlignment.ALIGN_CENTER);
//
//	    this.add(layout);
//	}
//	
//	public void fillData(StatusData statusData)
//	{
//		log.setText(statusData.getLog());
//		status.setText(String.valueOf(statusData.getStatus()));
//		jobId.setText(statusData.getJobId());
//	}
//	
//	public void cleanPanelData()
//	{
//		log.setText("");
//		status.setText("");
//		jobId.setText("");
//	}
}
