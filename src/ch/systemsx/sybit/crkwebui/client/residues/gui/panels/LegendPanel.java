package ch.systemsx.sybit.crkwebui.client.residues.gui.panels;

import ch.systemsx.sybit.crkwebui.client.commons.util.EscapedStringGenerator;
import ch.systemsx.sybit.crkwebui.client.residues.data.LegendItem;

import com.google.gwt.user.client.ui.HTML;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.widget.core.client.container.BoxLayoutContainer.BoxLayoutData;
import com.sencha.gxt.widget.core.client.container.HBoxLayoutContainer;
import com.sencha.gxt.widget.core.client.container.HBoxLayoutContainer.HBoxLayoutAlign;
import com.sencha.gxt.widget.core.client.container.HorizontalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.HorizontalLayoutContainer.HorizontalLayoutData;
import com.sencha.gxt.widget.core.client.container.SimpleContainer;
import com.sencha.gxt.widget.core.client.container.VBoxLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VBoxLayoutContainer.VBoxLayoutAlign;

/**
 * Panel containing the description of the residues values.
 * @author srebniak_a
 *
 */
public class LegendPanel extends SimpleContainer 
{
	public LegendPanel()
	{
		VBoxLayoutContainer mainContainer = new VBoxLayoutContainer(VBoxLayoutAlign.CENTER);
		this.setWidget(mainContainer);
		
		this.setHeight(30);
		this.addStyleName("eppic-no-padding");  
		
		HorizontalLayoutContainer legendContainer = new HorizontalLayoutContainer();
		legendContainer.setHeight(30);
		legendContainer.setWidth(650);

		for(LegendItem item : LegendItem.values())
		{
			HBoxLayoutContainer itemContainer = createLegendItemContainer(item);
			double space = 1.0 / LegendItem.values().length;
			legendContainer.add(itemContainer, new HorizontalLayoutData(space,1));
		}
		
		mainContainer.add(legendContainer);
	}

	/**
	 * Creates item container.
	 * @param item item to display
	 * @return panel for legend item
	 */
	private HBoxLayoutContainer createLegendItemContainer(LegendItem item)
	{
		HBoxLayoutContainer itemContainer = new HBoxLayoutContainer(HBoxLayoutAlign.MIDDLE);
		
	    HBoxLayoutContainer itemTypePanel = new HBoxLayoutContainer();
	    itemTypePanel.setBorders(true);
	    itemTypePanel.addStyleName(item.getStyleName());
	    itemTypePanel.setWidth(20);
	    itemTypePanel.setHeight(20);
		
		HTML itemTypeLabel = new HTML(EscapedStringGenerator.generateEscapedString(item.getName()));
		
		itemContainer.add(itemTypePanel, new BoxLayoutData(new Margins(0, 10, 0, 0)));
		itemContainer.add(itemTypeLabel);
		
		return itemContainer;
	}
}
