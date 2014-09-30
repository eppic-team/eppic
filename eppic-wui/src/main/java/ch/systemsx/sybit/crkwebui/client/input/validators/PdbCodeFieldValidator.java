package ch.systemsx.sybit.crkwebui.client.input.validators;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.sybit.crkwebui.shared.validators.PdbCodeVerifier;

import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.EditorError;
import com.sencha.gxt.widget.core.client.form.Validator;
import com.sencha.gxt.widget.core.client.form.error.DefaultEditorError;

/**
 * Pdb code validator.
 * @author AS
 *
 */
public class PdbCodeFieldValidator implements Validator<String> {

	@Override
	public List<EditorError> validate(Editor<String> editor, String value) {
		List<EditorError> validationResult = null;
		
		if (!PdbCodeVerifier.isTrimmedValid(value))
		{
			validationResult = new ArrayList<EditorError>();
			validationResult.add(new DefaultEditorError(editor, value + " is not a correct PDB code", value));
		}

		return validationResult;
	}

}
