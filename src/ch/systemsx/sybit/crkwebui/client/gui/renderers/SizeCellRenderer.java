package ch.systemsx.sybit.crkwebui.client.gui.renderers;

import ch.systemsx.sybit.crkwebui.client.controllers.MainController;

import com.extjs.gxt.ui.client.data.BaseModel;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;

/**
 * This class is used to style the size results
 * @author srebniak_a
 *
 */
public class SizeCellRenderer implements GridCellRenderer<BaseModel> 
{
	private MainController mainController;
	
	public SizeCellRenderer(MainController mainController) 
	{
		this.mainController = mainController;
	}

	@Override
	public Object render(final BaseModel model, String property,
			ColumnData config, final int rowIndex, final int colIndex,
			ListStore<BaseModel> store, Grid<BaseModel> grid) {
		
		int value = (Integer)model.get(property);
		
		int size1 = (Integer)model.get("size1");
		int size2 = (Integer)model.get("size2");
		
		int sizeSum = size1 + size2;
		
		Label sizeLabel = new Label(String.valueOf(value));

		if(sizeSum < mainController.getPdbScoreItem().getRunParameters().getMinCoreSizeForBio())
		{
			sizeLabel.setStyleAttribute("font-weight", "bold");
			sizeLabel.setStyleAttribute("color", "red");
		}

		return sizeLabel;
	}

}
