package ch.systemsx.sybit.crkwebui.client.homologs.gui.panels;

import ch.systemsx.sybit.crkwebui.client.homologs.util.IdentityColorPicker;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.widget.core.client.container.HorizontalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.SimpleContainer;

/**
 * Class to put color palette in the header
 * @author biyani_n
 *
 */
public class ColorPalettePanel  extends HorizontalLayoutContainer{
	
	private HTML label;

	public ColorPalettePanel(String label){
		this.label = new HTML(label);	
		init();
	}
	
	private void init(){
		this.add(label, new HorizontalLayoutData(-1,-1, new Margins(0,10,0,0)));
		
		this.add(new HTML("0%"), new HorizontalLayoutData(-1,-1, new Margins(0,2,0,0)));
		
		FlexTable colorTable = new FlexTable();
		colorTable.setCellPadding(0);
		colorTable.setCellSpacing(0);
		for(int i=0; i<10; i++){
			SimpleContainer sc = new SimpleContainer();
			sc.setPixelSize(20, 12);
			String color = IdentityColorPicker.getColor((i+1)/10.0);
			sc.getElement().applyStyles("backgroundColor:"+color);
			colorTable.setWidget(0, i, sc);
		}
		
		this.add(colorTable);
		this.add(new HTML("100%"), new HorizontalLayoutData(-1,-1, new Margins(0,0,0,2)));
		
	}
}
