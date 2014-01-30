package ch.systemsx.sybit.crkwebui.client.alignment.gui.windows;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.sybit.crkwebui.client.alignment.data.AlignmentDataModel;
import ch.systemsx.sybit.crkwebui.client.alignment.data.AlignmentDataModelProperties;
import ch.systemsx.sybit.crkwebui.client.alignment.gui.cell.AlignmentCell;
import ch.systemsx.sybit.crkwebui.client.alignment.gui.cell.ChainHeaderCell;
import ch.systemsx.sybit.crkwebui.client.alignment.gui.cell.IndexCell;
import ch.systemsx.sybit.crkwebui.client.commons.gui.windows.ResizableWindow;
import ch.systemsx.sybit.crkwebui.shared.model.HomologsInfoItem;
import ch.systemsx.sybit.crkwebui.shared.model.WindowData;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer.VerticalLayoutData;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.core.client.Style.SelectionMode;
import com.sencha.gxt.data.shared.ListStore;

/**
 * Window containing sequence alignments.
 * @author AS, nikhil
 */
public class AlignmentsWindow extends ResizableWindow 
{	
	private static int ALIGNMENT_WINDOW_DEFAULT_WIDTH = 600;
	private static int ALIGNMENT_WINDOW_DEFAULT_HEIGHT = 400;

	VerticalLayoutContainer gridContainer;

	private HomologsInfoItem homologsInfoItem;
	private String pdbName;

	private static final AlignmentDataModelProperties props = GWT.create(AlignmentDataModelProperties.class);

	private List<ColumnConfig<AlignmentDataModel, ?>> configs;
	private ListStore<AlignmentDataModel> store;
	private ColumnModel<AlignmentDataModel> columnModel;
	private Grid<AlignmentDataModel> grid;

	//fixed widths for the columns
	private static int headerColWidth = 80;
	private static int startIndexWidth = 65;
	private static int endIndexWidth = 65;

	private static int characterWidth = 8;

	public AlignmentsWindow(WindowData windowData,
			HomologsInfoItem homologsInfoItem,
			String pdbName) 
	{
		super(ALIGNMENT_WINDOW_DEFAULT_WIDTH,
				ALIGNMENT_WINDOW_DEFAULT_HEIGHT,
				windowData);

		this.homologsInfoItem = homologsInfoItem;
		this.pdbName = pdbName;
		this.setHideOnButtonClick(true);
		
		store = new ListStore<AlignmentDataModel>(props.key());
    	configs = createColumnConfig();
    	columnModel = new ColumnModel<AlignmentDataModel>(configs);
    	grid = createHomologsGrid();
    	
    	gridContainer = new VerticalLayoutContainer();
    	gridContainer.setPixelSize(ALIGNMENT_WINDOW_DEFAULT_WIDTH, ALIGNMENT_WINDOW_DEFAULT_HEIGHT);
    	gridContainer.add(grid, new VerticalLayoutData(1, 1));
    	
    	this.setWidget(gridContainer);

		this.addResizeHandler(new ResizeHandler()
		{
			@Override
			public void onResize(ResizeEvent event) {
				updateWindowContent();

			}
		});

	}
	
	/**
     * Creates the alignments grid
     * @return
     */
    private Grid<AlignmentDataModel> createHomologsGrid() {
    	Grid<AlignmentDataModel> grid = new Grid<AlignmentDataModel>(store, columnModel);
    	
    	grid.setBorders(false);
    	grid.getView().setStripeRows(true);
    	grid.getView().setColumnLines(false);
    	grid.setLoadMask(true);
    	grid.setHideHeaders(true);
    	grid.getView().setForceFit(true);
    	grid.getView().setTrackMouseOver(false);
    	grid.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    	
    	return grid;
	}
    
    private List<ColumnConfig<AlignmentDataModel, ?>> createColumnConfig(){
    	List<ColumnConfig<AlignmentDataModel, ?>> columns = new ArrayList<ColumnConfig<AlignmentDataModel,?>>();
    	
    	ColumnConfig<AlignmentDataModel, String[]> headerCol = 
    			new ColumnConfig<AlignmentDataModel, String[]>(props.rowHeader(), headerColWidth);
    	headerCol.setCell(new ChainHeaderCell());
    	headerCol.setFixed(true);
    	
    	ColumnConfig<AlignmentDataModel, Integer[]> startIndexCol = 
    			new ColumnConfig<AlignmentDataModel, Integer[]>(props.startIndex(), startIndexWidth);
    	startIndexCol.setCell(new IndexCell());
    	startIndexCol.setFixed(true);
    	startIndexCol.setAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
    			
    	ColumnConfig<AlignmentDataModel, String[]> alignmentCol = 
    			new ColumnConfig<AlignmentDataModel, String[]>(props.alignment());
    	alignmentCol.setCell(new AlignmentCell());
    	
    	ColumnConfig<AlignmentDataModel, Integer[]> endIndexCol = 
    			new ColumnConfig<AlignmentDataModel, Integer[]>(props.endIndex(), endIndexWidth);
    	endIndexCol.setCell(new IndexCell());
    	endIndexCol.setFixed(true);
    	
    	columns.add(headerCol);
    	columns.add(startIndexCol);
    	columns.add(alignmentCol);
    	columns.add(endIndexCol);
    	
    	return columns;
    }

	/**
	 * Refreshes content of the alignments window.
	 */
	public void updateWindowContent()
	{
		store.clear();
		store.addAll(createAlignmentData());
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
	 * Calculates the number of characters to be fit in a row of alignments
	 */
	private int calculateNrOfCharactersPerLine()
	{
		int availableWidth = this.getOffsetWidth(true) -
				headerColWidth - startIndexWidth - endIndexWidth - 50;
		int nrOfChars = (availableWidth/characterWidth);
		
		if(nrOfChars <=0 ) nrOfChars = 1;
		
		return nrOfChars;
	}
	
	/**
	 * Creates a list of Alignment Data to be placed in grid from homologs Item
	 * @return
	 */
	public List<AlignmentDataModel> createAlignmentData(){

		List<AlignmentDataModel> dataList =  new ArrayList<AlignmentDataModel>();

		String pdbId = homologsInfoItem.getChains();

		if(pdbId.contains("("))
		{
			pdbId = pdbId.substring(0, pdbId.indexOf("("));
		}

		pdbId = pdbName + pdbId;

		String uniprotId = homologsInfoItem.getUniprotId();

		int nrOfCharactersPerLine = calculateNrOfCharactersPerLine(); 
		int totalNumberOfCharacters = homologsInfoItem.getAlignedSeq1().length();

		int firstSequenceIndex = 1;
		int secondSequenceIndex = 1;

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
			
			AlignmentDataModel data = new AlignmentDataModel(pdbId, uniprotId,
											firstSequenceStartIndex,
											secondSequenceStartIndex,
											firstSequenceEndIndex,
											secondSequenceEndIndex,
											firstSequenceLine.toString(),
											markup.toString(),
											secondSequenceLine.toString()
										  );
			dataList.add(data);
		}
		
		return dataList;
	}

}
