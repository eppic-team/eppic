package ch.systemsx.sybit.crkwebui.client.model;

import java.io.Serializable;

/**
 * This class is used to represent settings used by the grid column
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
	 * Flag specifying whether displaying context menu for specified column should be allowed.
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

	public boolean isDisplayColumn() {
		return displayColumn;
	}

	public void setDisplayColumn(boolean displayColumn) {
		this.displayColumn = displayColumn;
	}

	public int getColumnWidth() {
		return columnWidth;
	}

	public void setColumnWidth(int columnWidth) {
		this.columnWidth = columnWidth;
	}

	public String getRenderer() {
		return renderer;
	}

	public void setRenderer(String renderer) {
		this.renderer = renderer;
	}

	public String getHeader() {
		return header;
	}

	public void setHeader(String header) {
		this.header = header;
	}

	public boolean isResizable() {
		return isResizable;
	}

	public void setResizable(boolean isResizable) {
		this.isResizable = isResizable;
	}

	public boolean isDisableColumnContextMenu() {
		return disableColumnContextMenu;
	}

	public void setDisableColumnContextMenu(boolean disableColumnContextMenu) {
		this.disableColumnContextMenu = disableColumnContextMenu;
	}

	public String getTooltip() {
		return tooltip;
	}

	public void setTooltip(String tooltip) {
		this.tooltip = tooltip;
	}
}
