package ch.systemsx.sybit.crkwebui.client.alignment.gui.panels;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.sybit.crkwebui.client.alignment.data.AlignmentDataModel;
import ch.systemsx.sybit.crkwebui.client.alignment.data.AlignmentDataModelProperties;
import ch.systemsx.sybit.crkwebui.client.alignment.gui.cell.AlignmentCell;
import ch.systemsx.sybit.crkwebui.client.alignment.gui.cell.PairwiseAlignmentInfoCell;
import eppic.dtomodel.ChainCluster;
import eppic.dtomodel.PairwiseAlignmentData;
import eppic.dtomodel.PairwiseAlignmentInfo;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.sencha.gxt.core.client.Style.SelectionMode;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;

public class AlignmentsGridPanel extends VerticalLayoutContainer{
	
	//fixed widths for the columns
	private static final int HEADER_COL_WIDTH = 85;
	private static final int START_INDEX_COL_WIDTH = 65;
	private static final int END_INDEX_COL_WIDTH = 65;

	private static final int CHARACTER_WIDTH = 8;
	
	
	private ChainCluster chainCluster;

	private static final AlignmentDataModelProperties props = GWT.create(AlignmentDataModelProperties.class);

	private List<ColumnConfig<AlignmentDataModel, ?>> configs;
	private ListStore<AlignmentDataModel> store;
	private ColumnModel<AlignmentDataModel> columnModel;
	private Grid<AlignmentDataModel> grid;


	public AlignmentsGridPanel(ChainCluster chainCluster, int gridWidth, int gridHeight) {
		
		this.chainCluster = chainCluster;
		
		store = new ListStore<AlignmentDataModel>(props.key());
    	configs = createColumnConfig();
    	columnModel = new ColumnModel<AlignmentDataModel>(configs);
    	grid = createHomologsGrid();
    	
    	this.setPixelSize(gridWidth, gridHeight);
    	this.add(grid, new VerticalLayoutData(1, 1));
    	
    	this.addResizeHandler(new ResizeHandler()
		{
			@Override
			public void onResize(ResizeEvent event) {
				updatePanelContent();

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
    	
    	ColumnConfig<AlignmentDataModel, PairwiseAlignmentInfo> headerCol = 
    			new ColumnConfig<AlignmentDataModel, PairwiseAlignmentInfo>(props.rowHeader(), HEADER_COL_WIDTH);
    	headerCol.setCell(new PairwiseAlignmentInfoCell());
    	headerCol.setFixed(true);
    	
    	ColumnConfig<AlignmentDataModel, PairwiseAlignmentInfo> startIndexCol = 
    			new ColumnConfig<AlignmentDataModel, PairwiseAlignmentInfo>(props.startIndex(), START_INDEX_COL_WIDTH);
    	startIndexCol.setCell(new PairwiseAlignmentInfoCell());
    	startIndexCol.setFixed(true);
    	startIndexCol.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
    			
    	ColumnConfig<AlignmentDataModel, PairwiseAlignmentData> alignmentCol = 
    			new ColumnConfig<AlignmentDataModel, PairwiseAlignmentData>(props.alignment());
    	alignmentCol.setCell(new AlignmentCell());
    	
    	ColumnConfig<AlignmentDataModel, PairwiseAlignmentInfo> endIndexCol = 
    			new ColumnConfig<AlignmentDataModel, PairwiseAlignmentInfo>(props.endIndex(), END_INDEX_COL_WIDTH);
    	endIndexCol.setCell(new PairwiseAlignmentInfoCell());
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
	public void updatePanelContent()
	{
		store.clear();
		store.addAll(createAlignmentData());
	}

	public ChainCluster getHomologsInfoItem()
	{
		return chainCluster;
	}

	public void setHomologsInfoItem(ChainCluster chainCluster)
	{
		this.chainCluster = chainCluster;
	}

	/**
	 * Calculates the number of characters to be fit in a row of alignments
	 */
	private int calculateNrOfCharactersPerLine()
	{
		int availableWidth = this.getOffsetWidth(true) -
				HEADER_COL_WIDTH - START_INDEX_COL_WIDTH - END_INDEX_COL_WIDTH - 50;
		int nrOfChars = (availableWidth/CHARACTER_WIDTH);
		
		if(nrOfChars <=0 ) nrOfChars = 1;
		
		return nrOfChars;
	}
	
	/**
	 * Creates a list of Alignment Data to be placed in grid from homologs Item
	 * @return
	 */
	public List<AlignmentDataModel> createAlignmentData(){

		List<AlignmentDataModel> dataList =  new ArrayList<AlignmentDataModel>();

		String pdbId = "Chain" + chainCluster.getRepChain();

		String uniprotId = chainCluster.getRefUniProtId();

		String markupLine = getMarkupLine(chainCluster.getPdbAlignedSeq(), chainCluster.getRefAlignedSeq());
		
		int nrOfCharactersPerLine = calculateNrOfCharactersPerLine(); 
		int totalNumberOfCharacters = chainCluster.getPdbAlignedSeq().length();

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

			StringBuffer firstSequenceLine = new StringBuffer(chainCluster.getPdbAlignedSeq().substring(beginIndex, endIndex));
			StringBuffer secondSequenceLine = new StringBuffer(chainCluster.getRefAlignedSeq().substring(beginIndex, endIndex));
			StringBuffer markup = new StringBuffer(markupLine.substring(beginIndex, endIndex));

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

	private static String getMarkupLine(String seq1, String seq2) {
		
		// TODO we could do all this much better with colors, dropping the markup line altogether
		
		// we can't do much in the client if there's an issue at this point, let's return an empty string... 
		// TODO is there a better solution??
		if (seq1.length()!=seq2.length()) return ""; 
			
		StringBuffer sb = new StringBuffer();
		
		for (int i=0;i<seq1.length();i++) {
			
			char seq1Char = seq1.charAt(i);
			char seq2Char = seq2.charAt(i);
			
			if (seq1Char == '-' || seq2Char == '-') sb.append(' ');
			
			else if (seq1Char == seq2Char) sb.append('|');
			
			// we used to have 2 kind of characters here ':' or '.', with this implementation we 
			// are thus dropping the distinction of positive match and normal match, but I think we can live with that

			else sb.append('.'); 
			
		}
		
		return sb.toString();
	}
}
