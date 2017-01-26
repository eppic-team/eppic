package ch.systemsx.sybit.crkwebui.client.residues.data;

import ch.systemsx.sybit.crkwebui.client.commons.appdata.AppPropertiesManager;

/**
 * Enum used in legend panel to properly style boxes.
 */
public enum LegendItem 
{
	TOTALLY_BURIED(AppPropertiesManager.CONSTANTS.interfaces_residues_legend_buried(), "eppic-grid-row-buried"),
	SURFACE(AppPropertiesManager.CONSTANTS.interfaces_residues_legend_surface(), "eppic-grid-row-surface"),
	RIM(AppPropertiesManager.CONSTANTS.interfaces_residues_legend_rim(), "eppic-grid-row-rim"),
	CORE_GEOMETRY(AppPropertiesManager.CONSTANTS.interfaces_residues_legend_core_geom(), "eppic-grid-row-core-geometry"),
	CORE_EVOLUTIONARY(AppPropertiesManager.CONSTANTS.interfaces_residues_legend_core_evol(), "eppic-grid-row-core-evolutionary");
	
	
	/**
	 * Type of the item.
	 */
	private String name;
	
	/**
	 * Name of the style.
	 */
	private String styleName;
	
	LegendItem(String name,
			   String styleName)
	{
		this.name = name;
		this.styleName = styleName;
	}
	
	/**
	 * Retrieves type of the item.
	 * @return type of the item
	 */
	public String getName() {
		return name;
	}

	/**
	 * Retrieves name of the style to use.
	 * @return name of the style to use
	 */
	public String getStyleName() {
		return styleName;
	}
}
