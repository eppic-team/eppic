package ch.systemsx.sybit.crkwebui.client.controllers;

import java.util.List;

import ch.systemsx.sybit.crkwebui.client.data.ResultsData;
import ch.systemsx.sybit.crkwebui.client.data.StatusData;
import ch.systemsx.sybit.crkwebui.client.gui.InputDataPanel;
import ch.systemsx.sybit.crkwebui.client.gui.MainViewPort;
import ch.systemsx.sybit.crkwebui.client.gui.ResultsPanel;
import ch.systemsx.sybit.crkwebui.client.gui.StatusPanel;

import com.extjs.gxt.ui.client.widget.Viewport;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout.VBoxLayoutAlign;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;

public class MainControllerImpl implements MainController 
{
	private MainViewPort mainViewPort;
	
	private ServiceController serviceController;
	
//	private String selectedId;
	
	public MainControllerImpl(Viewport viewport)
	{
		mainViewPort = new MainViewPort(this);
		RootPanel.get().add(mainViewPort);
//		viewport.add(mainViewPort);
		this.serviceController = new ServiceControllerImpl(this);
	}
	
	public void test(String testValue)
	{
		serviceController.test(testValue);
	}
	
	public void displayView(String token)
	{
		if((token != null) &&
		   (token.length() > 3) &&
		   (token.startsWith("id")))
		{
			String selectedId = token.substring(3);
			displayResults(selectedId);
		}
		else
		{
			displayInputView();
		}
	}
	
	public void displayInputView()
	{
		mainViewPort.getDisplayPanel().removeAll();
		
		VBoxLayout vBoxLayout = new VBoxLayout();
		vBoxLayout.setVBoxLayoutAlign(VBoxLayoutAlign.CENTER);
		FormPanel inputDataPanelWrapper = new FormPanel();
		inputDataPanelWrapper.setLayout(vBoxLayout);
		inputDataPanelWrapper.setBorders(false);
		inputDataPanelWrapper.setBodyBorder(false);
		inputDataPanelWrapper.getHeader().setVisible(false);
		
		InputDataPanel inputDataPanel = new InputDataPanel(this);
		
		inputDataPanelWrapper.add(inputDataPanel);
		mainViewPort.getDisplayPanel().add(inputDataPanelWrapper);
		mainViewPort.getDisplayPanel().layout();
//		mainPanel.getDisplayPanel().setCellHorizontalAlignment(inputDataPanel, HasHorizontalAlignment.ALIGN_CENTER);
	}
	
	public void displayResults(String selectedId)
	{
		serviceController.checkIfDataProcessed(selectedId);
	}
	
	public void getStatusData(String selectedId)
	{
		serviceController.getStatusData(selectedId);
	}
	
	public void getResultData(String selectedId)
	{
		serviceController.getResultData(selectedId);
	}
	
	public void displayResultView(ResultsData resultData)
	{
		mainViewPort.getDisplayPanel().removeAll();
		
		ResultsPanel resultsPanel = new ResultsPanel(this, resultData);
		mainViewPort.getDisplayPanel().setLayout(new FitLayout());
		mainViewPort.getDisplayPanel().add(resultsPanel);
		mainViewPort.getDisplayPanel().layout();
	}
	
	public void displayStatusView(StatusData statusData)
	{
		mainViewPort.getDisplayPanel().removeAll();
		
		VBoxLayout vBoxLayout = new VBoxLayout();
		vBoxLayout.setVBoxLayoutAlign(VBoxLayoutAlign.CENTER);
		FormPanel statusPanelWrapper = new FormPanel();
		statusPanelWrapper.setLayout(vBoxLayout);
		statusPanelWrapper.setBorders(false);
		statusPanelWrapper.setBodyBorder(false);
		statusPanelWrapper.getHeader().setVisible(false);
		
		StatusPanel statusPanel = new StatusPanel(this);
		statusPanel.fillData(statusData);
		
		statusPanelWrapper.add(statusPanel);
		
		mainViewPort.getDisplayPanel().add(statusPanelWrapper);
		mainViewPort.getDisplayPanel().layout();
		
//		if(mainPanel.getDisplayPanel().getWidgetCount() > 0 &&
//		   mainPanel.getDisplayPanel().getWidget(0) instanceof StatusPanelGWT)
//		{
//			((StatusPanelGWT)(mainPanel.getDisplayPanel().getWidget(0))).fillData(statusData);
//		}
//		else
//		{
//			for(int i=0; i<mainPanel.getDisplayPanel().getWidgetCount(); i++)
//			{
//				mainPanel.getDisplayPanel().remove(i);
//			}
//			
//			StatusPanelGWT statusPanel = new StatusPanelGWT(this);
//			mainPanel.getDisplayPanel().add(statusPanel);
//			statusPanel.fillData(statusData);
//		}
	}
	
	public void showError(String errorMessage)
	{
		Window.alert(errorMessage);
	}
	
	public void killJob(String selectedId)
	{
		serviceController.killJob(selectedId);
	}
	
	public void getJobsForCurrentSession()
	{
		serviceController.getJobsForCurrentSession();
	}
	
	public void setJobs(List<StatusData> statusData)
	{
		mainViewPort.getMyJobsPanel().setJobs(statusData);
	}
	
	public void untieJobsFromSession()
	{
		serviceController.untieJobsFromSession();
	}
	
}
