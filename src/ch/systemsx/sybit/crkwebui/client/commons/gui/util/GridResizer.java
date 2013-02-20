package ch.systemsx.sybit.crkwebui.client.commons.gui.util;

import java.util.List;

import com.extjs.gxt.ui.client.widget.grid.Grid;

public class GridResizer 
{
	private Grid grid;
	private List<Integer> initialColumnsWidths;
	private boolean scrollable;
	private boolean useBufferedView;
	private float gridWidthMultiplier;
	
	public GridResizer(Grid grid,
					   List<Integer> initialColumnsWidths,
					   boolean useBufferedView,
					   boolean scrollable)
	{
		this.grid = grid;
		this.initialColumnsWidths = initialColumnsWidths;
		this.useBufferedView = useBufferedView;
		this.scrollable = scrollable;
	}

	/**
	 * Resizes grid with columns based on provided space.
	 * @param assignedWidth width assigned for the grid
	 */
	public void resize(int assignedWidth)
	{
		int widthOfVisibleColumns = GridUtil.calculateWidthOfVisibleColumns(grid,
																			initialColumnsWidths);
		
		int scrollSpace = calculateScrollSpace();
		assignedWidth -= scrollSpace;

		boolean checkIfExtendsColumns = checkIfExtendColumns(assignedWidth, widthOfVisibleColumns);

		int nrOfColumns = grid.getColumnModel().getColumnCount();
		int[] columnsWidths = calculateColumnsWidths(checkIfExtendsColumns, 
													 assignedWidth, 
													 widthOfVisibleColumns, 
													 nrOfColumns);

		if (!checkIfExtendsColumns)
		{
			assignedWidth = widthOfVisibleColumns;
		}
		
		int sumOfVisibleColumnsAssignedWidths = calculateWidthOfVisibleColumns(columnsWidths); 
		if(sumOfVisibleColumnsAssignedWidths != assignedWidth)
		{
			adjustColumnsWidths(columnsWidths, sumOfVisibleColumnsAssignedWidths, assignedWidth);
		}
		
		for(int i=0; i<columnsWidths.length; i++)
		{
			grid.getColumnModel().getColumn(i).setWidth(columnsWidths[i]);
		}
		
		grid.setWidth(assignedWidth + scrollSpace);
	}
	
	/**
	 * Calculates width of the column necessary for scroll. This is used only in a case grid supports scrolling
	 * (and not e.g. paging). It's calculated based on useBufferedView and scrollable properties.
	 * @return width of scroll column
	 */
	private int calculateScrollSpace()
	{
		int scrollSpace = 0;
		
		if(scrollable || useBufferedView)
		{
			scrollSpace = 20;
		}
		
		return scrollSpace;
	}
	
	/**
	 * Adjusts width of columns of the grid to fit to the value assigned for the grid. This is necessary due
	 * to the fact that calculated widths may not use all or may use too much space because of rounding of floats
	 * to ints.
	 * @param columnsWidths widths of the coulmns to adjust
	 * @param sumOfVisibleColumnsAssignedWidths sum of widths of all visible columns
	 * @param assignedWidth width provided for the grid
	 */
	private void adjustColumnsWidths(int[] columnsWidths,
									 int sumOfVisibleColumnsAssignedWidths,
									 int assignedWidth) 
	{
		if(sumOfVisibleColumnsAssignedWidths > assignedWidth)
		{
			int difference = sumOfVisibleColumnsAssignedWidths - assignedWidth;
			decreaseColumnsWidths(columnsWidths, difference);
		}
		else if(sumOfVisibleColumnsAssignedWidths < assignedWidth)
		{
			int difference = assignedWidth - sumOfVisibleColumnsAssignedWidths;
			increaseColumnsWidths(columnsWidths, difference);
		}
	}
	
	/**
	 * Decreases values of widths of columns with total number equal to amountToSubtract.
	 * @param columnsWidths widths of columns to modify
	 * @param amountToSubtract total nr to decrease width (it's summary number for all and not for each of them)
	 */
	private void decreaseColumnsWidths(int[] columnsWidths,
									   int amountToSubtract)
	{
		int[] widthsToSubtract = calculateValuesToModify(columnsWidths, amountToSubtract);
		
		for (int i = 0; i < widthsToSubtract.length; i++)
		{
			columnsWidths[i] -= widthsToSubtract[i];
		}
	}
	
	/**
	 * Increases values of widths of columns with total number equal to amountToAdd.
	 * @param columnsWidths widths of columns to modify
	 * @param amountToAdd total nr to increase width (it's summary number for all and not for each of them)
	 */
	private void increaseColumnsWidths(int[] columnsWidths,
			   						   int amountToAdd)
	{
		int[] widthsToAdd = calculateValuesToModify(columnsWidths, amountToAdd);
		
		for (int i = 0; i < widthsToAdd.length; i++)
		{
			columnsWidths[i] += widthsToAdd[i];
		}
	}
	
	/**
	 * Calculates how much each of the columns's width needs to be changed.
	 * @param columnsWidths widths of columns to modify
	 * @param difference total nr to modify width (it's summary number for all and not for each of them)
	 * @return values to modify width of columns
	 */
	private int[] calculateValuesToModify(int[] columnsWidths,
										  int difference)
	{
		int[] widthsToModify = new int[columnsWidths.length];
		for(int i=0; i<widthsToModify.length; i++)
		{
			widthsToModify[i] = 0;
		}
		
		int valueToModifyForEachColumns = difference / columnsWidths.length;
		int valueToModifyForSpecifiedColumns = difference % columnsWidths.length;
		
		for(int i=0; i<widthsToModify.length; i++)
		{
			widthsToModify[i] += valueToModifyForEachColumns;
		}
		
		for(int i=0; i<valueToModifyForSpecifiedColumns; i++)
		{
			widthsToModify[i]++;
		}
		
		return widthsToModify;
	}

	private int calculateWidthOfVisibleColumns(int[] columnsWidths) 
	{
		int widthOfVisibleColumns = 0;
		
		for(int i=0; i<grid.getColumnModel().getColumnCount(); i++)
		{
			if(!grid.getColumnModel().getColumn(i).isHidden())
			{
				widthOfVisibleColumns += columnsWidths[i];
			}
		}
		
		return widthOfVisibleColumns;
	}
	
	/**
	 * Checks whether columns should be extended or initial widths of the columns should be used instead.
	 * @param assignedWidth width assigned for the grid
	 * @param widthOfVisibleColumns sum of widths of all visible columns
	 * @return whether resize columns
	 */
	private boolean checkIfExtendColumns(int assignedWidth,
			 							 int widthOfVisibleColumns)
	{
		if(GridUtil.checkIfForceFit(widthOfVisibleColumns,
				 					assignedWidth))
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	/**
	 * Prepares widths of all the columns of the grid.
	 * @param checkIfExtendsColumns flag pointing whether columns should be resized or initial values used instead
	 * @param assignedWidth width assigned for the grid
	 * @param widthOfVisibleColumns total width of all visible columns
	 * @param nrOfColumns nr of columns
	 * @return widths of columns
	 */
	private int[] calculateColumnsWidths(boolean checkIfExtendsColumns,
			 int assignedWidth,
			 int widthOfVisibleColumns,
			 int nrOfColumns) 
	{
		int[] columnsWidths = null;
		
		if (checkIfExtendsColumns)
		{
			columnsWidths = calculateExtendedColumnsWidths(assignedWidth, widthOfVisibleColumns, nrOfColumns);
		}
		else
		{
			columnsWidths = calculateOriginalColumnWidths(nrOfColumns);
		}
		
		return columnsWidths;
	}
	
	/**
	 * Prepares widths of the columns in a case original initial widths of the columns are to be used.
	 * @param nrOfColumns nr of columns
	 * @return width of columns
	 */
	private int[] calculateOriginalColumnWidths(int nrOfColumns) 
	{
		gridWidthMultiplier = 1;

		int[] columnsWidths = new int[nrOfColumns];
		
		for (int i = 0; i < nrOfColumns; i++)
		{
			columnsWidths[i] = initialColumnsWidths.get(i);
		}
		
		return columnsWidths;
	}

	/**
	 * Prepares width of the columns in a case column resizing is to be used.
	 * @param assignedWidth width assigned for the grid
	 * @param widthOfVisibleColumns total width of all visible columns
	 * @param nrOfColumns nr of columns
	 * @return widths of the columns
	 */
	private int[] calculateExtendedColumnsWidths(int assignedWidth,
									 			 int widthOfVisibleColumns,
									 			 int nrOfColumns)
	{
		gridWidthMultiplier = (float)assignedWidth / widthOfVisibleColumns;

		int[] columnsWidths = new int[nrOfColumns];
		
		for (int i = 0; i < nrOfColumns; i++)
		{
			columnsWidths[i] = Math.round(initialColumnsWidths.get(i) * gridWidthMultiplier);
		}
		
		return columnsWidths;
	}
	
	public float getGridWidthMultiplier() {
		return gridWidthMultiplier;
	}
}
