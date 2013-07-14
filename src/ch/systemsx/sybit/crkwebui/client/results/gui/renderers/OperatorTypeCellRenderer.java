package ch.systemsx.sybit.crkwebui.client.results.gui.renderers;

import ch.systemsx.sybit.crkwebui.client.commons.appdata.ApplicationContext;
import ch.systemsx.sybit.crkwebui.client.commons.gui.data.TooltipXPositionType;
import ch.systemsx.sybit.crkwebui.client.commons.gui.data.TooltipYPositionType;
import ch.systemsx.sybit.crkwebui.client.commons.gui.images.ImageWithTooltip;
//import ch.systemsx.sybit.crkwebui.client.commons.gui.labels.LabelWithTooltip;
import ch.systemsx.sybit.crkwebui.client.commons.util.EscapedStringGenerator;
//import ch.systemsx.sybit.crkwebui.client.results.gui.labels.CallLabel;
import ch.systemsx.sybit.crkwebui.shared.model.InterfaceItem;

import com.extjs.gxt.ui.client.data.BaseModel;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;

/**
 * Renderer used to style operator type.
 * @author srebniak_a
 *
 */
public class OperatorTypeCellRenderer implements GridCellRenderer<BaseModel> 
{
	@Override
	public Object render(final BaseModel model, final String property,
			ColumnData config, final int rowIndex, final int colIndex,
			ListStore<BaseModel> store, final Grid<BaseModel> grid) {
		
		String iconsDir = "resources/icons/"; 
		
		String value = EscapedStringGenerator.generateEscapedString((String) model.get(property));

		if (value != null) 
		{
			String tooltipText = null;

			String icon = "optype_" + value ;
			
			int interfaceId = (Integer)model.get("id");
			
			InterfaceItem interfaceItem = ApplicationContext.getPdbScoreItem().getInterfaceItem(interfaceId - 1);
			
			if(interfaceItem != null)
			{
				tooltipText = interfaceItem.getOperator();
				
				if (interfaceItem.getIsInfinite()) {
					icon += "_inf";					
				}					
								
			}
			
			icon+=".png"; // either optype_2S.png or optype_2S_red.png
			
			String source = iconsDir+icon;
			
			ImageWithTooltip imageWithTooltip = new ImageWithTooltip(source, 
					null,
					tooltipText,
					-1, 
					true, 
					1000, 
					0, 
					TooltipXPositionType.RIGHT, 
					TooltipYPositionType.TOP);
			
			//LabelWithTooltip operatorLabel = new CallLabel(value, 
			//												 tooltipText,
			//												 ApplicationContext.getWindowData());
			
			return imageWithTooltip;
		
		// to make it compatible with older versions without operatorType in the model, 
		// we return the operator string if no operator type is present			
		} else { 
			int interfaceId = (Integer)model.get("id");
			
			InterfaceItem interfaceItem = ApplicationContext.getPdbScoreItem().getInterfaceItem(interfaceId - 1);
			
			if(interfaceItem != null)
			{
				String operator = interfaceItem.getOperator();
				return operator;
			}
		}

		// if interfaceItem is null we have messed up somewhere
		return "?";
	}
}

