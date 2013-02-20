package ch.systemsx.sybit.crkwebui.client.commons.gui.util;

import java.util.List;

import com.extjs.gxt.ui.client.widget.grid.Grid;

/**
 * This class contains util methods for grid.
 * @author AS
 */
public class GridUtil 
{
	/**
	 * Calculates sum of initial widths of all the visible columns of the grid.
	 * @param grid input grid
	 * @param initialColumnsWidths initial widths of the columns 
	 * @return sum of initial widths of all the visible columns of the grid 
	 */
	public static int calculateWidthOfVisibleColumns(Grid grid,
													 List<Integer> initialColumnsWidths)
	{
		int scoresGridWidthOfAllVisibleColumns = 0;
		
		for(int i=0; i<grid.getColumnModel().getColumnCount(); i++)
		{
			if(!grid.getColumnModel().getColumn(i).isHidden())
			{
				scoresGridWidthOfAllVisibleColumns += initialColumnsWidths.get(i);
			}
		}
		
		return scoresGridWidthOfAllVisibleColumns;
	}
	
	/**
	 * Checks whether sum of the widths of visible columns is smaller than width of the place to store grid.
	 * @param gridWidthOfAllVisibleColumns sum of the widths of visible columns
	 * @param gridWidth width of the place assigned for the grid
	 * @return flag pointing whether columns should be automatically extended
	 */
	public static boolean checkIfForceFit(int gridWidthOfAllVisibleColumns,
										  int gridWidth)
	{
		if(gridWidthOfAllVisibleColumns < gridWidth)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
}
