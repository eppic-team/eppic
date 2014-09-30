package ch.systemsx.sybit.crkwebui.client.residues.data;

/**
 * Enum used in legend panel to properly style boxes.
 */
public enum LegendItem 
{
	TOTALLY_BURIED("Totally buried", "eppic-grid-row-buried"),
	SURFACE("Surface", "eppic-grid-row-surface"),
	RIM("Rim", "eppic-grid-row-rim"),
	CORE_GEOMETRY("Core geometry (95% BSA)", "eppic-grid-row-core-geometry"),
	CORE_EVOLUTIONARY("Core evolutionary (70% BSA)", "eppic-grid-row-core-evolutionary");
	
	
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
