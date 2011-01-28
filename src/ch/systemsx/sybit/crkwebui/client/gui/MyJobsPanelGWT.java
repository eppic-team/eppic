package ch.systemsx.sybit.crkwebui.client.gui;

import java.util.List;

import ch.systemsx.sybit.crkwebui.client.controllers.MainController;
import ch.systemsx.sybit.crkwebui.client.data.StatusData;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

public class MyJobsPanelGWT extends VerticalPanel
{
	private MainController mainController;
	
	private VerticalPanel linksPanel;
	
	public MyJobsPanelGWT(final MainController mainController)
	{
		this.mainController = mainController;
		this.setSpacing(5);
		this.add(new Button("test", new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				mainController.test("testString");
			}
		}));
		
		Label jobsLabel = new Label("My Jobs:");
		this.add(jobsLabel);
		
		linksPanel = new VerticalPanel();
		this.add(linksPanel);
	}
	
	public void setJobs(List<StatusData> jobs)
	{
		this.remove(linksPanel);
		
		linksPanel = new VerticalPanel();
		
		if(jobs != null)
		{
			for(StatusData job : jobs)
			{
				Hyperlink link = new Hyperlink(job.getJobId(), "id/" + job.getJobId());
				linksPanel.add(link);
//				link.set
//				    link.ensureDebugId("cwHyperlink-" + cwClass.getName());
//				    return link;
			}
		}
		
		this.add(linksPanel);
	}
}
