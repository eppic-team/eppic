package ch.systemsx.sybit.crkwebui.client.gui;

import ch.systemsx.sybit.crkwebui.client.controllers.EventBusManager;
import ch.systemsx.sybit.crkwebui.client.events.WindowHideEvent;
import ch.systemsx.sybit.crkwebui.shared.model.HomologsInfoItem;
import ch.systemsx.sybit.crkwebui.shared.model.WindowData;

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

/**
 * Window containing sequence alignments.
 * @author AS
 */
public class AlignmentsWindow extends ResizableWindow 
{
	private static int ALIGNMENT_WINDOW_DEFAULT_WIDTH = 600;
	private static int ALIGNMENT_WINDOW_DEFAULT_HEIGHT = 400;
	
	private HomologsInfoItem homologsInfoItem;
	private String pdbName;
	
	private int nrOfCharacterInFirstSequence;
	private int nrOfCharacterInSecondSequence;
	private int maxLengthOfLeftAnnotations;
	private int maxLengthOfRightAnnotations;
	private int maxLengthOfAnnotations;
	
	private int MIN_DISTANCE_BETWEEN_UNIPROTID_AND_POSITION = 6;
	private int DISTANCE_BETWEEN_CURRENT_NR_AND_SEQUENCE = 2;
	
	public AlignmentsWindow(WindowData windowData,
							HomologsInfoItem homologsInfoItem,
							String pdbName) 
	{
		super(ALIGNMENT_WINDOW_DEFAULT_WIDTH,
			  ALIGNMENT_WINDOW_DEFAULT_HEIGHT,
			  windowData);
		
		this.homologsInfoItem = homologsInfoItem;
		this.pdbName = pdbName;
		
		this.setSize(windowWidth, windowHeight);
		
		this.setPlain(true);
		this.setLayout(new FitLayout());
		this.setHideOnButtonClick(true);
		this.getButtonBar().setVisible(false);
		
		this.addListener(Events.Resize, new Listener<WindowEvent>() {

			@Override
			public void handleEvent(WindowEvent be) 
			{
				windowHeight = be.getHeight();
				windowWidth = be.getWidth();
				updateWindowContent();
			}
		});
		
		this.addWindowListener(new WindowListener()
		{
			@Override
			public void windowHide(WindowEvent we)
			{
				EventBusManager.EVENT_BUS.fireEvent(new WindowHideEvent());
			}
		});
	}
	
	/**
	 * Refreshes content of the alignments window.
	 */
	public void updateWindowContent()
	{
		ContentPanel homologsContentPanel = new ContentPanel();
		homologsContentPanel.setLayout(new RowLayout(Orientation.VERTICAL));
		homologsContentPanel.setBodyBorder(false);
		homologsContentPanel.setBorders(false);
		homologsContentPanel.getHeader().setVisible(false);
		homologsContentPanel.addStyleName("eppic-monospaced-font");
		homologsContentPanel.render(this.getElement());
		
		TextMetrics textMetrics = TextMetrics.get();
		textMetrics.bind(homologsContentPanel.el());
		
		this.removeAll();
		
		String pdbId = homologsInfoItem.getChains();
		
		if(pdbId.contains("("))
		{
			pdbId = pdbId.substring(0, pdbId.indexOf("("));
		}
		
		pdbId = pdbName + pdbId;
		
		String uniprotId = homologsInfoItem.getUniprotId();
		
		int nrOfCharactersPerLine = calculateNrOfCharactersPerLine(textMetrics, pdbId, uniprotId); 
		int totalNumberOfCharacters = homologsInfoItem.getAlignedSeq1().length();
		
		int firstSequenceIndex = 1;
		int secondSequenceIndex = 1;
		
		if(nrOfCharactersPerLine > 0)
		{
			for(int i=0; i<totalNumberOfCharacters; i+=nrOfCharactersPerLine)
			{
				int firstSequenceStartIndex = firstSequenceIndex;
				int secondSequenceStartIndex = secondSequenceIndex;
				
				int beginIndex = i;
				int endIndex = i + nrOfCharactersPerLine;
				if(endIndex > totalNumberOfCharacters)
				{
					endIndex = totalNumberOfCharacters;
				}
				
				StringBuffer firstSequenceLine = new StringBuffer(homologsInfoItem.getAlignedSeq1().substring(beginIndex, endIndex));
				StringBuffer secondSequenceLine = new StringBuffer(homologsInfoItem.getAlignedSeq2().substring(beginIndex, endIndex));
				StringBuffer markup = new StringBuffer(homologsInfoItem.getMarkupLine().substring(beginIndex, endIndex));
				
				int lengthOfFirstSequenceBeforeStyling = firstSequenceLine.length();
				int lengthOfSecondSequenceBeforeStyling = secondSequenceLine.length();
				
				for(int j=endIndex - beginIndex - 1; j>=0; j--)
				{
					if(firstSequenceLine.charAt(j) != '-')
					{
						firstSequenceIndex++;
					}
					
					if(secondSequenceLine.charAt(j) != '-')
					{
						secondSequenceIndex++;
					}
					
					if(markup.charAt(j) != '|')
					{
						firstSequenceLine.insert(j + 1, "</font>");
						firstSequenceLine.insert(j, "<font color=\"red\">");
						
						secondSequenceLine.insert(j + 1, "</font>");
						secondSequenceLine.insert(j, "<font color=\"red\">");
					}
				}
				
				int firstSequenceEndIndex = firstSequenceIndex;
				if(firstSequenceIndex != firstSequenceStartIndex)
				{
					firstSequenceEndIndex--;
				}
				
				int secondSequenceEndIndex = secondSequenceIndex;
				if(secondSequenceIndex != secondSequenceStartIndex)
				{
					secondSequenceEndIndex--;
				}
				
				String firstSequenceLineAnnotated = createAnnotatedSequenceLine(pdbId,
																				firstSequenceStartIndex,
																				firstSequenceEndIndex,
																				nrOfCharactersPerLine,
																				lengthOfFirstSequenceBeforeStyling,
																				firstSequenceLine.toString()); 
				
				String secondSequenceLineAnnotated = createAnnotatedSequenceLine(uniprotId,
																				 secondSequenceStartIndex,
																				 secondSequenceEndIndex,
																				 nrOfCharactersPerLine,
																				 lengthOfSecondSequenceBeforeStyling,
																				 secondSequenceLine.toString()); 
				
				for(int k=0; k<maxLengthOfLeftAnnotations + MIN_DISTANCE_BETWEEN_UNIPROTID_AND_POSITION + DISTANCE_BETWEEN_CURRENT_NR_AND_SEQUENCE; k++)
				{
					markup.insert(0, " ");
				}
				
				Text firstSequenceLabel = new Text(firstSequenceLineAnnotated.toString());
				Text markupLabel = new Text(markup.toString().replaceAll(" ", "&nbsp;"));
				Text secondSequenceLabel = new Text(secondSequenceLineAnnotated.toString());
				
				homologsContentPanel.add(firstSequenceLabel, new RowData(1, -1, new Margins(0)));  
				homologsContentPanel.add(markupLabel, new RowData(1, -1, new Margins(0)));
				homologsContentPanel.add(secondSequenceLabel, new RowData(1, -1, new Margins(0)));
				homologsContentPanel.add(new Text(), new RowData(1, -1, new Margins(0)));
				homologsContentPanel.add(new Text(), new RowData(1, -1, new Margins(0)));
				homologsContentPanel.setScrollMode(Scroll.AUTOY);
			}
			
			int minWindowWidth = 0;
			for(int i=0; i<maxLengthOfAnnotations + 40; i++)
			{
				minWindowWidth += textMetrics.getWidth("A");
			}
			this.setMinWidth(minWindowWidth);
		}
		
		this.add(homologsContentPanel);
		this.layout(true);
	}

	public HomologsInfoItem getHomologsInfoItem()
	{
		return homologsInfoItem;
	}
	
	public void setHomologsInfoItem(HomologsInfoItem homologsInfoItem)
	{
		this.homologsInfoItem = homologsInfoItem;
	}
	
	public String getPdbName() {
		return pdbName;
	}

	public void setPdbName(String pdbName) {
		this.pdbName = pdbName;
	}

	/**
	 * Generates general sequence text settings.
	 * @param textMetrics used text metrics
	 * @param firstSequenceLeftAnnotation first sequence to annotate
	 * @param secondSequenceLeftAnnotation second sequence to annotate
	 * @return nr of characters of original sequences per line.
	 */
	private int calculateNrOfCharactersPerLine(TextMetrics textMetrics,
											   String firstSequenceLeftAnnotation,
											   String secondSequenceLeftAnnotation)
	{
		int widthOfCharacter = textMetrics.getWidth("A");
		
		nrOfCharacterInFirstSequence = 0;
		for(int i=0; i<homologsInfoItem.getAlignedSeq1().length(); i++)
		{
			if(homologsInfoItem.getAlignedSeq1().charAt(i) != '-')
			{
				nrOfCharacterInFirstSequence++;
			}
		}
		
		nrOfCharacterInSecondSequence = 0;
		for(int i=0; i<homologsInfoItem.getAlignedSeq2().length(); i++)
		{
			if(homologsInfoItem.getAlignedSeq2().charAt(i) != '-')
			{
				nrOfCharacterInSecondSequence++;
			}
		}
		
		int firstSequenceLeftAnnotationLength = firstSequenceLeftAnnotation.length() + String.valueOf(nrOfCharacterInFirstSequence).length();
		int secondSequenceLeftAnnotationLength = secondSequenceLeftAnnotation.length() + String.valueOf(nrOfCharacterInSecondSequence).length();
		
		maxLengthOfLeftAnnotations = firstSequenceLeftAnnotationLength;
		if(secondSequenceLeftAnnotationLength > maxLengthOfLeftAnnotations)
		{
			maxLengthOfLeftAnnotations = secondSequenceLeftAnnotationLength;
		}
		
		maxLengthOfRightAnnotations = String.valueOf(nrOfCharacterInFirstSequence).length();
		if(String.valueOf(nrOfCharacterInSecondSequence).length() > maxLengthOfRightAnnotations)
		{
			maxLengthOfRightAnnotations = String.valueOf(nrOfCharacterInSecondSequence).length();
		}
		
		//include empty spaces
		maxLengthOfAnnotations = maxLengthOfLeftAnnotations + 
									 maxLengthOfRightAnnotations + 
									 MIN_DISTANCE_BETWEEN_UNIPROTID_AND_POSITION +
									 DISTANCE_BETWEEN_CURRENT_NR_AND_SEQUENCE * 2;
		
		int nrOfCharactersPerLine = (windowWidth - 40) / widthOfCharacter;
		nrOfCharactersPerLine -= maxLengthOfAnnotations;
		return nrOfCharactersPerLine;
	}
	
	/**
	 * Generates annotated sequence line.
	 * @param leftAnnotation uniprot/pdb id.
	 * @param sequenceStartIndex index of the first character of the sequence part which is going to be annotated.
	 * @param sequenceEndIndex index of the last character of the sequence part which is going to be annotated.
	 * @param nrOfCharactersPerLine nr of characters per line for sequence.
	 * @param lengthOfSequenceBeforeStyling nr of characters in the sequence. 
	 * @param sequence sequence part to annotate.
	 * @return annotated sequence line.
	 */
	private String createAnnotatedSequenceLine(String leftAnnotation,
											   int sequenceStartIndex,
											   int sequenceEndIndex,
											   int nrOfCharactersPerLine,
											   int lengthOfSequenceBeforeStyling,
											   String sequence)
	{
		StringBuffer sequenceLineAnnotated = new StringBuffer(leftAnnotation);
		int lengthOfSequenceStartIndex = String.valueOf(sequenceStartIndex).length();
		int nrOfSpacesInSeq = maxLengthOfLeftAnnotations + MIN_DISTANCE_BETWEEN_UNIPROTID_AND_POSITION - sequenceLineAnnotated.length() - lengthOfSequenceStartIndex;
		
		for(int i=0; i<nrOfSpacesInSeq; i++)
		{
			sequenceLineAnnotated.append("&nbsp;");
		}
		
		sequenceLineAnnotated.append(sequenceStartIndex);
		
		for(int i=0; i<DISTANCE_BETWEEN_CURRENT_NR_AND_SEQUENCE; i++)
		{
			sequenceLineAnnotated.append("&nbsp;");
		}
		
		sequenceLineAnnotated.append(sequence);
		
		int nrOfSpacesInSeqRight = maxLengthOfRightAnnotations - String.valueOf(sequenceEndIndex).length();
		
		for(int i=0; i<nrOfCharactersPerLine - lengthOfSequenceBeforeStyling + DISTANCE_BETWEEN_CURRENT_NR_AND_SEQUENCE + nrOfSpacesInSeqRight; i++)
		{
			sequenceLineAnnotated.append("&nbsp;");
		}
		
		sequenceLineAnnotated.append(sequenceEndIndex);
		
		return sequenceLineAnnotated.toString();
	}
}
