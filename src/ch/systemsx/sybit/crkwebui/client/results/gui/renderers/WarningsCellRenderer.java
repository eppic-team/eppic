package ch.systemsx.sybit.crkwebui.client.results.gui.renderers;

import java.util.List;

import ch.systemsx.sybit.crkwebui.client.commons.appdata.AppPropertiesManager;
import ch.systemsx.sybit.crkwebui.client.commons.gui.data.TooltipXPositionType;
import ch.systemsx.sybit.crkwebui.client.commons.gui.data.TooltipYPositionType;
import ch.systemsx.sybit.crkwebui.client.commons.gui.images.ImageWithTooltip;
import ch.systemsx.sybit.crkwebui.client.commons.util.EscapedStringGenerator;
import ch.systemsx.sybit.crkwebui.shared.model.WarningItem;

import com.extjs.gxt.ui.client.data.BaseModel;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;

/**
 * Renderer used to display warning icon.
 * @author srebniak_a
 *
 */
public class WarningsCellRenderer implements GridCellRenderer<BaseModel> 
{
	@Override
	public Object render(final BaseModel model, String property,
			ColumnData config, final int rowIndex, final int colIndex,
			ListStore<BaseModel> store, Grid<BaseModel> grid) 
	{
		final List<WarningItem> warnings = (List<WarningItem>)model.get("warnings");
		
		if((warnings != null) && (warnings.size() > 0))
		{
			ImageWithTooltip imageWithTooltip = new ImageWithTooltip("resources/icons/warning_icon.png",
																	 AppPropertiesManager.CONSTANTS.results_grid_warnings_tooltip_title(),
																	 generateWarningsTemplate(warnings),
																	 500,
																	 false,
																	 100,
																	 0,
																	 TooltipXPositionType.LEFT,
																	 TooltipYPositionType.BOTTOM);
			return imageWithTooltip;
		}
		else
		{
			return "";
		}
		
	}
	
	/**
	 * Creates list of interface warnings.
	 * @param warnings list of warnings to display
	 * @return template containing list of warnings
	 */
	private String generateWarningsTemplate(List<WarningItem> warnings)
	{
		String warningsList = "<div><ul class=\"eppic-tooltip-list\">";
		
		for(WarningItem warning : warnings)
		{
			if(!warning.getText().equals(""))
			{
				warningsList += "<li>" + EscapedStringGenerator.generateSanitizedString(warning.getText()) + "</li>";
			}
		}
			
		warningsList += "</ul></div>";
		
		return warningsList;
	}
}
