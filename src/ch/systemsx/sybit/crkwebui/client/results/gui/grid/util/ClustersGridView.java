package ch.systemsx.sybit.crkwebui.client.results.gui.grid.util;

import ch.systemsx.sybit.crkwebui.client.results.data.InterfaceItemModel;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.sencha.gxt.widget.core.client.grid.GroupSummaryView;

public class ClustersGridView extends GroupSummaryView<InterfaceItemModel> {
	
	public ClustersGridView(){
		setShowGroupedColumn(false);
		setShowDirtyCells(false);
		setStartCollapsed(true);
		setStripeRows(true);
		setColumnLines(false);
		setForceFit(true);
		setEnableGroupingMenu(false);
		setEnableNoGroups(false);
	}
	
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
	
}
