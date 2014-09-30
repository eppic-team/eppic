package ch.systemsx.sybit.crkwebui.client.results.gui.grid.util;

import java.util.Map;

import ch.systemsx.sybit.crkwebui.client.commons.appdata.ApplicationContext;
import ch.systemsx.sybit.crkwebui.client.commons.util.EscapedStringGenerator;
import ch.systemsx.sybit.crkwebui.client.results.data.InterfaceItemModel;
import ch.systemsx.sybit.crkwebui.client.results.gui.cells.MethodCallCell;
import ch.systemsx.sybit.crkwebui.shared.model.InterfaceCluster;
import ch.systemsx.sybit.crkwebui.shared.model.InterfaceClusterScore;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.widget.core.client.grid.ColumnData;
import com.sencha.gxt.widget.core.client.grid.GroupSummaryView;
import com.sencha.gxt.widget.core.client.grid.SummaryColumnConfig;

public class ClustersGridView extends GroupSummaryView<InterfaceItemModel> {

    public ClustersGridView(){
	setShowGroupedColumn(false);
	setShowDirtyCells(false);
	setStartCollapsed(false);
	setStripeRows(true);
	setColumnLines(false);
	setForceFit(true);
	setEnableGroupingMenu(false);
	setEnableNoGroups(false);		
    }

    /**
     * Coded for GXT 3.0.1
     * This method would have to be changed if the gxt changes their implementation
     */
    @Override
    public SafeHtml renderGroupHeader(GroupingData<InterfaceItemModel> groupInfo) {
	String component = "<div>";
	for(InterfaceItemModel m: groupInfo.getItems()){
	    String thumbnailUrl = m.getThumbnailUrl();
	    component += "<img style='margin-left:5px; border:1px solid #F1F1F1;' src='"+thumbnailUrl+"' height='30' width='30'/>";
	}
	component += "</div>";
	return SafeHtmlUtils.fromTrustedString(component);
    }

    /**
     * Coded for GXT 3.0.1
     * TODO This method would have to be changed if the gxt changes their implementation
     */
    @Override
    protected SafeHtml renderSummary(GroupingData<InterfaceItemModel> groupInfo, Map<ValueProvider<? super InterfaceItemModel, ?>, Number> data) {
	int colCount = cm.getColumnCount();
	int last = colCount - 1;
	InterfaceCluster interfaceCluster = null; 
	for(InterfaceCluster ic : ApplicationContext.getPdbInfo().getInterfaceClusters())
	    if(ic.getClusterId() == groupInfo.getItems().get(0).getClusterId())
		interfaceCluster = ic;		
	String unselectableClass = " " + unselectable;
	
	String cellClass = styles.cell();
	String cellInner = styles.cellInner();
	String cellFirstClass = " x-grid-cell-first";
	String cellLastClass = "  x-grid-cell-last";

	SafeHtmlBuilder trBuilder = new SafeHtmlBuilder();

	for (int i = 0, len = colCount; i < len; i++) {  
	    SummaryColumnConfig<InterfaceItemModel,?> cf = (SummaryColumnConfig<InterfaceItemModel,?>) cm.getColumn(i);
	    ColumnData cd = getColumnData().get(i);

	    String cellClasses = cellClass;
	    cellClasses += (i == 0 ? cellFirstClass : (i == last ? cellLastClass : ""));

	    //String id = cf.getColumnClassSuffix();
	    //if (id != null && !id.equals("")) {
		//cellClasses += " x-grid-td-" + id;
	    //}

	    Number n = data.get(cm.getValueProvider(i));
	    String value = "";

	    if (cf.getSummaryFormat() != null) {
		value = n == null ? "" : cf.getSummaryFormat().format(n.doubleValue());
	    } else if (cf.getSummaryRenderer() != null) {
		value = cf.getSummaryRenderer().render(n, data).asString();
	    }
	    SafeHtml cellContentInHtml = null;
	    String cellInnerClasses = cellInner;
	    if(cf.getCell() instanceof MethodCallCell) {
		String tooltip = null;
		String methodType = MethodSummaryType.methodTypeStringFromSummaryType(cf.getSummaryType());
		for(InterfaceClusterScore iCS : interfaceCluster.getInterfaceClusterScores())
		    if(iCS.getMethod().equals(methodType))
			tooltip = iCS.getCallReason();
		if(tooltip != null)
		    tooltip = tooltip.replaceAll("\n", "<br/>");
		cellContentInHtml = prepareMethodCallCellContent(value, tooltip);
		cellInnerClasses += " eppic-results-final-call";
		if(value.toLowerCase().startsWith("bio")){
		    cellInnerClasses += " eppic-results-grid-cell-bio";
		} else if(value.toLowerCase().startsWith("xtal")){
		    cellInnerClasses += " eppic-results-grid-cell-xtal";
		}
	    } else {
		cellContentInHtml = SafeHtmlUtils.fromString(value);
	    }
	    SafeHtml tdContent = tpls.td(i, cellClasses, cd.getStyles(), cellInnerClasses, cf.getColumnTextStyle(), cellContentInHtml);
	    trBuilder.append(tdContent);
	}

	String rowClasses = "x-grid-row-summary";

	if (!selectable) {
	    rowClasses += unselectableClass;
	}

	SafeHtml cells = trBuilder.toSafeHtml();

	return tpls.tr(rowClasses, cells);
    }

    SafeHtml prepareMethodCallCellContent(String value, String tooltip) {
	SafeHtml cellContentInHtml;
	tooltip = EscapedStringGenerator.generateEscapedString(tooltip);
	tooltip = "<div class=\"eppic-default-font eppic-results-grid-tooltip\">" + tooltip + "</div>";
	String cellContent = "<span qtip='" + tooltip + "'>"+ value +"</span>";
	cellContentInHtml = SafeHtmlUtils.fromTrustedString(cellContent);
	return cellContentInHtml;
    }

}
