package ch.systemsx.sybit.crkwebui.client.commons.gui.renderers;

import com.extjs.gxt.ui.client.data.BaseModel;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;

public interface GridCellRendererFactory 
{
	public GridCellRenderer<BaseModel> createGridCellRenderer(String rendererName);
}
