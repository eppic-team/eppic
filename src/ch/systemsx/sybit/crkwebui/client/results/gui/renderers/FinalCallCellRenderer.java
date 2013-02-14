package ch.systemsx.sybit.crkwebui.client.results.gui.renderers;

import ch.systemsx.sybit.crkwebui.client.commons.appdata.ApplicationContext;
import ch.systemsx.sybit.crkwebui.client.commons.gui.labels.LabelWithTooltip;
import ch.systemsx.sybit.crkwebui.client.commons.util.EscapedStringGenerator;
import ch.systemsx.sybit.crkwebui.client.results.gui.labels.CallLabel;
import ch.systemsx.sybit.crkwebui.shared.model.InterfaceItem;

import com.extjs.gxt.ui.client.data.BaseModel;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;

/**
 * Renderer used to style final result.
 * @author srebniak_a
 *
 */
public class FinalCallCellRenderer implements GridCellRenderer<BaseModel> 
{
	@Override
	public Object render(final BaseModel model, final String property,
			ColumnData config, final int rowIndex, final int colIndex,
			ListStore<BaseModel> store, final Grid<BaseModel> grid) {
		
		String value = EscapedStringGenerator.generateEscapedString((String) model.get(property));

		if (value != null) 
		{
			String tooltipText = null;
			
			int interfaceId = (Integer)model.get("id");
			
			InterfaceItem interfaceItem = ApplicationContext.getPdbScoreItem().getInterfaceItem(interfaceId - 1);
			
			if(interfaceItem != null)
			{
				tooltipText = interfaceItem.getFinalCallReason();
				
				if(tooltipText != null)
				{
					tooltipText = tooltipText.replaceAll("\n", "<br/>");
				}
			}
			
			LabelWithTooltip callReasonLabel = new CallLabel(value, 
															 tooltipText,
															 ApplicationContext.getWindowData());
			callReasonLabel.addStyleName("eppic-results-final-call");
			
			return callReasonLabel;
		}

		return value;
	}
}
