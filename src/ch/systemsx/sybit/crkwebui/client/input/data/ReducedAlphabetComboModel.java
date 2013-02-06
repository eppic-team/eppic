package ch.systemsx.sybit.crkwebui.client.input.data;

import com.extjs.gxt.ui.client.data.BaseModel;

/**
 * Data model for reduced alphabet combobox.
 * @author srebniak_a
 *
 */
public class ReducedAlphabetComboModel extends BaseModel 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ReducedAlphabetComboModel(Integer reducedAlphabet) {
		set("reducedAlphabet", reducedAlphabet);
	}

	public Integer getReducedAlphabet() {
		return (Integer) get("reducedAlphabet");
	}
}
