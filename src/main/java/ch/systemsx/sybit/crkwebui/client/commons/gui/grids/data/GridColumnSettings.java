package ch.systemsx.sybit.crkwebui.client.commons.gui.grids.data;

import java.io.Serializable;

/**
 * Settings used by the grid column.
 * @author AS
 */
public class GridColumnSettings implements Serializable 
{

	/**
	 * Flag specifying whether column should be displayed.
	 */
	private boolean displayColumn; 
	
	/**
	 * Width of the column.
	 */
	private int columnWidth;
	
	/**
	 * Used renderer.
	 */
	private String renderer;
	
	/**
	 * Title of the column.
	 */
	private String header;
	
	/**
	 * Flag specifying whether column can be resized.
	 */
	private boolean isResizable; 
	
	/**
	 * Flag specifying whether displaying context menu for specified column should not be allowed.
	 */
	private boolean disableColumnContextMenu;
	
	/**
	 * Tooltip for grid column.
	 */
	private String tooltip;
			
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public GridColumnSettings()
	{
		displayColumn = true; 
		columnWidth = 75;
		renderer = null;
		header = null;
		tooltip = null;
		isResizable = true; 
		disableColumnContextMenu = false; 
	}

	/**
	 * Retrieves information whether column should be displayed.
	 * @return flag specifying whether column should be displayed
	 */
	public boolean isDisplayColumn() {
		return displayColumn;
	}

	/**
	 * Sets whether column should be displayed.
	 * @param displayColumn flag specifying whether column should be displayed
	 */
	public void setDisplayColumn(boolean displayColumn) {
		this.displayColumn = displayColumn;
	}

	/**
	 * Retrieves width of the column.
	 * @return width of the column
	 */
	public int getColumnWidth() {
		return columnWidth;
	}

	/**
	 * Sets width of the column.
	 * @param columnWidth width of the column
	 */
	public void setColumnWidth(int columnWidth) {
		this.columnWidth = columnWidth;
	}

	/**
	 * Retrieves renderer used to display content of the column. If not set then null is returned(default).
	 * @return renderer used to display content of the column
	 */
	public String getRenderer() {
		return renderer;
	}

	/**
	 * Sets renderer used to properly display content of the column.
	 * @param renderer renderer used to properly display content of the column
	 */
	public void setRenderer(String renderer) {
		this.renderer = renderer;
	}

	/**
	 * Retrieves text of the header displayed in the grid. If no header is set then it returns null(default).
	 * @return text of the header
	 */
	public String getHeader() {
		return header;
	}

	/**
	 * Sets header of the column displayed in the grid.
	 * @param header text of the header
	 */
	public void setHeader(String header) {
		this.header = header;
	}

	/**
	 * Retrieves information whether width of the column can be changed(default true).
	 * @return flag specifying whether width of the column can be changed
	 */
	public boolean isResizable() {
		return isResizable;
	}

	/**
	 * Sets whether width of the column can be modified.
	 * @param isResizable flag specifying whether width of the column can be changed
	 */
	public void setResizable(boolean isResizable) {
		this.isResizable = isResizable;
	}

	/**
	 * Retrieves information whether context menu should not be displayed over the column(default false).
	 * @return flag specifying whether context menu should not be displayed over the column
	 */
	public boolean isDisableColumnContextMenu() {
		return disableColumnContextMenu;
	}

	/**
	 * Sets whether context menu should not be displayed for the column.
	 * @param disableColumnContextMenu flag specifying whether context menu should not be displayed
	 */
	public void setDisableColumnContextMenu(boolean disableColumnContextMenu) {
		this.disableColumnContextMenu = disableColumnContextMenu;
	}

	/**
	 * Retrieves text of the tooltip displayed over the column. If tooltip is not set(default) then null is returned.
	 * @return text of the tooltip displayed over the column
	 */
	public String getTooltip() {
		return tooltip;
	}

	/**
	 * Sets tooltip over the column.
	 * @param tooltip text of the tooltip to display over the column
	 */
	public void setTooltip(String tooltip) {
		this.tooltip = tooltip;
	}
}
