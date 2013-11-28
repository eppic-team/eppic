package ch.systemsx.sybit.crkwebui.client.input.validators;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.sybit.crkwebui.shared.validators.EmailFieldVerifier;

import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.EditorError;
import com.sencha.gxt.widget.core.client.form.Validator;
import com.sencha.gxt.widget.core.client.form.error.DefaultEditorError;

/**
 * Email address validator.
 * 
 * @author srebniak_a
 * 
 */
public class EmailFieldValidator implements Validator<String>
{
	@Override
	public List<EditorError> validate(Editor<String> editor, String value) {
		List<EditorError> validationResult = null;

		if (!EmailFieldVerifier.isValid(value))
		{
			validationResult = new ArrayList<EditorError>();
			validationResult.add(new DefaultEditorError(editor, value + " is not a correct email address", value));
		}
		
		return validationResult;
	}

}
