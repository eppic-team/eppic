package ch.systemsx.sybit.crkwebui.client.gui;

import java.util.List;

import com.extjs.gxt.ui.client.widget.grid.Grid;

public class GridUtil {

	public static int calculateWidthOfVisibleColumns(Grid grid,
													 List<Integer> initialColumnWidth)
	{
		int scoresGridWidthOfAllVisibleColumns = 0;
		
		for(int i=0; i<grid.getColumnModel().getColumnCount(); i++)
		{
			if(!grid.getColumnModel().getColumn(i).isHidden())
			{
				scoresGridWidthOfAllVisibleColumns += initialColumnWidth.get(i);
			}
		}
		
		return scoresGridWidthOfAllVisibleColumns;
	}
	
	public static boolean checkIfForceFit(int gridWidthOfAllVisibleColumns,
										  int width)
	{
		if(gridWidthOfAllVisibleColumns < width)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
}
