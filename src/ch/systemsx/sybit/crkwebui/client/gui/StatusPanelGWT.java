package ch.systemsx.sybit.crkwebui.client.gui;

import ch.systemsx.sybit.crkwebui.client.controllers.MainController;
import ch.systemsx.sybit.crkwebui.client.data.StatusData;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;

public class StatusPanelGWT extends VerticalPanel 
{
	private MainController mainController;
	
	private Label jobId;
	private Label status;
	private TextArea log;
	private Button killJobButton;
	
	public StatusPanelGWT(final MainController mainController)
	{
		this.mainController = mainController;
		
		FlexTable layout = new FlexTable();
	    layout.setCellSpacing(6);
	    layout.setWidth("1000px");
	    FlexCellFormatter cellFormatter = layout.getFlexCellFormatter();

	    // Add a title to the form
	    layout.setHTML(0, 0, "CRK - status");
	    cellFormatter.setColSpan(0, 0, 2);
	    cellFormatter.setHorizontalAlignment(
	        0, 0, HasHorizontalAlignment.ALIGN_CENTER);

	    layout.setHTML(1, 0, "Job Id");
	    jobId = new Label();
	    layout.setWidget(1, 1, jobId);
	    
	    layout.setHTML(2, 0, "Status");
	    status = new Label();
	    layout.setWidget(2, 1, status);
	    
	    layout.setHTML(3, 0, "Log");
	    log = new TextArea();
	    log.setWidth("450");
	    log.setHeight("300");
	    layout.setWidget(3, 1, log);
	    
	    killJobButton = new Button("Kill", new ClickHandler() 
	    {
			@Override
			public void onClick(ClickEvent event) 
			{
				mainController.killJob(jobId.getText());
			}
		});
	    layout.setWidget(4, 0, killJobButton);
	    cellFormatter.setColSpan(4, 0, 2);
	    cellFormatter.setHorizontalAlignment(
	        4, 0, HasHorizontalAlignment.ALIGN_CENTER);

	    this.add(layout);
	}
	
	public void fillData(StatusData statusData)
	{
		log.setText(statusData.getLog());
		status.setText(String.valueOf(statusData.getStatus()));
		jobId.setText(statusData.getJobId());
	}
	
	public void cleanPanelData()
	{
		log.setText("");
		status.setText("");
		jobId.setText("");
	}
}
