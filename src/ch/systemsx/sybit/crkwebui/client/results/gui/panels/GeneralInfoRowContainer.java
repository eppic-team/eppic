/**
 * 
 */
package ch.systemsx.sybit.crkwebui.client.results.gui.panels;

import ch.systemsx.sybit.crkwebui.client.commons.util.EscapedStringGenerator;

import com.google.gwt.user.client.ui.HTML;
import com.sencha.gxt.widget.core.client.container.CssFloatLayoutContainer;

/**
 * Container used to store fields of general information panel
 * @author biyani_n
 *
 */
public class GeneralInfoRowContainer extends CssFloatLayoutContainer {
	private static final int FIELD_NAME_LENGTH = 100;
	private static final int VALUE_LENGTH = 230;
	
	private HTML fieldNameLabel;
	private HTML valueLabel;
	
	public GeneralInfoRowContainer(){
		super();
		this.setWidth(getContainerLength());	
	}

	public void fillContent(String fieldName, String value) {		
		EscapedStringGenerator.generateEscapedString(fieldName);
		EscapedStringGenerator.generateEscapedString(value);
		
		fieldNameLabel = new HTML(fieldName);
		fieldNameLabel.addStyleName("eppic-general-info-label");
		this.add(fieldNameLabel, new CssFloatData(FIELD_NAME_LENGTH));
		
		valueLabel = new HTML(value);
		valueLabel.addStyleName("eppic-general-info-label-value");
		this.add(valueLabel, new CssFloatData(VALUE_LENGTH));

	}
	
	/**
	 * Returns the length of the container (Max Field length + Max value length)
	 * @return length of the container (Max Field length + Max value length)
	 */
	public static int getContainerLength(){
		return FIELD_NAME_LENGTH + VALUE_LENGTH;
	}

}
