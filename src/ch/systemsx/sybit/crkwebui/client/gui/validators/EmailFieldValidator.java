package ch.systemsx.sybit.crkwebui.client.gui.validators;

import ch.systemsx.sybit.crkwebui.shared.EmailFieldVerifier;

import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.Validator;

/**
 * Email address validator.
 * 
 * @author srebniak_a
 * 
 */
public class EmailFieldValidator implements Validator {
	@Override
	public String validate(Field<?> field, String value) {
		String validationResult = null;

		if (!EmailFieldVerifier.isValid(value)) {
			validationResult = value + " is not correct email address";
		}

		return validationResult;
	}

}
