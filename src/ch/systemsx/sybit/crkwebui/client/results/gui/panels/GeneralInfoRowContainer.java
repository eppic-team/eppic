/**
 * 
 */
package ch.systemsx.sybit.crkwebui.client.results.gui.panels;

import ch.systemsx.sybit.crkwebui.client.commons.util.EscapedStringGenerator;

import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.ColumnData;
import com.extjs.gxt.ui.client.widget.layout.ColumnLayout;

/**
 * Container used to store fields of general information panel
 * @author biyani_n
 *
 */
public class GeneralInfoRowContainer extends LayoutContainer {
	private static final int FIELD_NAME_LENGTH = 100;
	private static final int VALUE_LENGTH = 200;
	
	private Label fieldNameLabel;
	private Label valueLabel;
	
	public GeneralInfoRowContainer(){
		this.setBorders(false);
		this.setWidth(getContainerLength());
		//this.setLayout(new RowLayout(Orientation.HORIZONTAL));
		//this.addStyleName("eppic-pdb-identifier-label");
		
	}

	public void fillContent(String fieldName, String value) {
		this.removeAll();
		
		this.setLayout(new ColumnLayout());
		
		EscapedStringGenerator.generateEscapedString(fieldName);
		EscapedStringGenerator.generateEscapedString(value);
		
		fieldNameLabel = new Label(fieldName);
		fieldNameLabel.addStyleName("eppic-general-info-label");
		this.add(fieldNameLabel, new ColumnData(FIELD_NAME_LENGTH));
		
		valueLabel = new Label(value);
		valueLabel.addStyleName("eppic-general-info-label-value");
		this.add(valueLabel, new ColumnData(VALUE_LENGTH));
		
		this.layout(true);		
	}
	
	/**
	 * Returns the length of the container (Max Field length + Max value length)
	 * @return length of the container (Max Field length + Max value length)
	 */
	public static int getContainerLength(){
		return FIELD_NAME_LENGTH + VALUE_LENGTH;
	}

}
