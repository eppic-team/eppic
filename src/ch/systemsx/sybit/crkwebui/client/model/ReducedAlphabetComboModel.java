package ch.systemsx.sybit.crkwebui.client.model;

import com.extjs.gxt.ui.client.data.BaseModel;

public class ReducedAlphabetComboModel extends BaseModel {
	public ReducedAlphabetComboModel(Integer reducedAlphabet) {
		set("reducedAlphabet", reducedAlphabet);
	}

	public Integer getReducedAlphabet() {
		return (Integer) get("reducedAlphabet");
	}
}
