package ch.systemsx.sybit.crkwebui.client.gui;

import ch.systemsx.sybit.crkwebui.client.controllers.MainController;
import ch.systemsx.sybit.crkwebui.shared.model.HomologsInfoItem;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.WindowEvent;
import com.extjs.gxt.ui.client.event.WindowListener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.util.TextMetrics;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;

public class AlignmentsWindow extends ResizableWindow 
{
	private static int ALIGNMENT_WINDOW_DEFAULT_WIDTH = 400;
	private static int ALIGNMENT_WINDOW_DEFAULT_HEIGHT = 400;
	
	private HomologsInfoItem homologsInfoItem;
	
	public AlignmentsWindow(final MainController mainController,
							 HomologsInfoItem homologsInfoItem) 
	{
		super(mainController,
			  ALIGNMENT_WINDOW_DEFAULT_WIDTH,
			  ALIGNMENT_WINDOW_DEFAULT_HEIGHT);
		
		this.homologsInfoItem = homologsInfoItem;
		
		this.setSize(windowWidth, windowHeight);
		
		this.setPlain(true);
		this.setLayout(new FitLayout());
		this.setHideOnButtonClick(true);
		this.getButtonBar().setVisible(false);
		
		Listener<WindowEvent> resizeWindowListener = new Listener<WindowEvent>() {

			@Override
			public void handleEvent(WindowEvent be) 
			{
				windowHeight = be.getHeight();
				windowWidth = be.getWidth();
				updateWindowContent();
			}
		};
		
		this.addListener(Events.Resize, resizeWindowListener);
		
		this.addWindowListener(new WindowListener()
		{
			@Override
			public void windowHide(WindowEvent we)
			{
				MainViewPort mainViewPort = mainController.getMainViewPort();
				
				if((mainViewPort != null) &&
		           (mainViewPort.getCenterPanel() != null) &&
		           (mainViewPort.getCenterPanel().getDisplayPanel() != null) &&
		           (mainViewPort.getCenterPanel().getDisplayPanel() instanceof ResultsPanel))
		           {
				    	((ResultsPanel)mainViewPort.getCenterPanel().getDisplayPanel()).getResultsGrid().focus();
		           }
			}
		});
	}
	
	public void updateWindowContent()
	{
		ContentPanel homologsContentPanel = new ContentPanel();
		homologsContentPanel.setLayout(new RowLayout(Orientation.VERTICAL));
		homologsContentPanel.setBodyBorder(false);
		homologsContentPanel.setBorders(false);
		homologsContentPanel.getHeader().setVisible(false);
		homologsContentPanel.setStyleAttribute("font-family", "courier");
		
		TextMetrics textMetrics = TextMetrics.get();
		int widthOfCharacter = textMetrics.getWidth("A");
		
		this.removeAll();
		
		int nrOfCharactersPerLine = (windowWidth - 40) / widthOfCharacter;
		int totalNumberOfCharacters = homologsInfoItem.getAlignedSeq1().length();
		
		for(int i=0; i<totalNumberOfCharacters; i+=nrOfCharactersPerLine)
		{
			int beginIndex = i;
			int endIndex = i + nrOfCharactersPerLine;
			if(endIndex > totalNumberOfCharacters)
			{
				endIndex = totalNumberOfCharacters;
			}
			
			StringBuffer firstSequenceLine = new StringBuffer(homologsInfoItem.getAlignedSeq1().substring(beginIndex, endIndex));
			StringBuffer secondSequenceLine = new StringBuffer(homologsInfoItem.getAlignedSeq2().substring(beginIndex, endIndex));
			String markup = homologsInfoItem.getMarkupLine().substring(beginIndex, endIndex);
			
			for(int j=endIndex - beginIndex - 1; j>=0; j--)
			{
				if(markup.charAt(j) != '|')
				{
					firstSequenceLine.insert(j + 1, "</font>");
					firstSequenceLine.insert(j, "<font color=\"red\">");
					
					secondSequenceLine.insert(j + 1, "</font>");
					secondSequenceLine.insert(j, "<font color=\"red\">");
				}
			}
			
			Text firstSequenceLabel = new Text(firstSequenceLine.toString());
			Text markupLabel = new Text(markup.toString());
			Text secondSequenceLabel = new Text(secondSequenceLine.toString());
			
			homologsContentPanel.add(firstSequenceLabel, new RowData(1, -1, new Margins(0)));  
			homologsContentPanel.add(markupLabel, new RowData(1, -1, new Margins(0)));
			homologsContentPanel.add(secondSequenceLabel, new RowData(1, -1, new Margins(0)));
			homologsContentPanel.add(new Text(), new RowData(1, -1, new Margins(0)));
			homologsContentPanel.setScrollMode(Scroll.AUTOY);
//			this.add(new Label(), new RowData(1, 20));
		}
		
		this.add(homologsContentPanel);
		this.layout(true);
	}

	public HomologsInfoItem getHomologsInfoItem()
	{
		return homologsInfoItem;
	}
}
