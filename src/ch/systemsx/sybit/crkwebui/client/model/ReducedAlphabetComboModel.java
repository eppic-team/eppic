package ch.systemsx.sybit.crkwebui.client.model;

import com.extjs.gxt.ui.client.data.BaseModel;

/**
 * Data model for reduced alphabet combobox
 * @author srebniak_a
 *
 */
public class ReducedAlphabetComboModel extends BaseModel 
{
	public ReducedAlphabetComboModel(Integer reducedAlphabet) {
		set("reducedAlphabet", reducedAlphabet);
	}

	public Integer getReducedAlphabet() {
		return (Integer) get("reducedAlphabet");
	}
}
