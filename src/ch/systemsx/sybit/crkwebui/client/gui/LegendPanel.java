package ch.systemsx.sybit.crkwebui.client.gui;

import ch.systemsx.sybit.crkwebui.client.data.LegendItem;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayout.HBoxLayoutAlign;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayoutData;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout.VBoxLayoutAlign;

/**
 * Panel containing the description of the residues values.
 * @author srebniak_a
 *
 */
public class LegendPanel extends LayoutContainer 
{
	public LegendPanel()
	{
		this.setHeight(30);
		this.setStyleAttribute("padding", "0px");
		
		VBoxLayout legendPanelLayout = new VBoxLayout();  
		legendPanelLayout.setVBoxLayoutAlign(VBoxLayoutAlign.CENTER);  
		this.setLayout(legendPanelLayout);
		
		LayoutContainer legendContainer = new LayoutContainer();
		legendContainer.setLayout(new RowLayout(Orientation.HORIZONTAL));
		legendContainer.setHeight(30);
		legendContainer.setWidth(500);

		for(LegendItem item : LegendItem.values())
		{
			LayoutContainer itemContainer = new LayoutContainer();
			HBoxLayout itemContainerLayout = new HBoxLayout();  
			itemContainerLayout.setHBoxLayoutAlign(HBoxLayoutAlign.MIDDLE);  
		    itemContainer.setLayout(itemContainerLayout);
			
		    LayoutContainer itemTypePanel = new LayoutContainer();
		    itemTypePanel.setBorders(true);
		    itemTypePanel.addStyleName(item.getStyleName());
		    itemTypePanel.setWidth(20);
		    itemTypePanel.setHeight(20);
			
			Label itemTypeLabel = new Label(item.getName());
			
			itemContainer.add(itemTypePanel, new HBoxLayoutData(new Margins(0, 10, 0, 0)));
			itemContainer.add(itemTypeLabel);
			
			legendContainer.add(itemContainer, new RowData(0.25, 1, new Margins(0)));
		}
		
		this.add(legendContainer);
	}
}
