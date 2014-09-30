package ch.systemsx.sybit.crkwebui.client.input.data;

import java.io.Serializable;

/**
 * Data model for search mode combobox.
 * @author nikhil
 *
 */
public class SearchModeComboModel implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private int id;
	private String searchMode;
	
	private static int COUNTER = 0;

	public SearchModeComboModel(String searchMode) {
		this.id = COUNTER++;
		this.searchMode = searchMode;
	}
	
	public int getId(){
		return id;
	}

	public String getSearchMode() {
		return searchMode;
	}
}
