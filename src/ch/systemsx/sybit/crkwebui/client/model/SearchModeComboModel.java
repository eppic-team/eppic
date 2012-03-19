package ch.systemsx.sybit.crkwebui.client.model;

import com.extjs.gxt.ui.client.data.BaseModel;

/**
 * Data model for search mode combobox.
 * @author srebniak_a
 *
 */
public class SearchModeComboModel extends BaseModel 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public SearchModeComboModel(String searchMode) {
		set("searchMode", searchMode);
	}

	public String getSearchMode() {
		return (String) get("searchMode");
	}
}
