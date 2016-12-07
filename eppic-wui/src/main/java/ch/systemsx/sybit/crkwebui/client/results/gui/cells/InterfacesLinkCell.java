/**
 * 
 */
package ch.systemsx.sybit.crkwebui.client.results.gui.cells;

import ch.systemsx.sybit.crkwebui.client.results.data.AssemblyItemModel;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.sencha.gxt.data.shared.ListStore;


/**
 * Cell used to used to render the interfaces links
 * @author nikhil
 *
 */
public class InterfacesLinkCell extends AbstractCell<String> {

	private ListStore<AssemblyItemModel> itemsStore;
	private String type;

	public InterfacesLinkCell(ListStore<AssemblyItemModel> itemsStore, String type){
		this.itemsStore = itemsStore;
		this.type = type;
	}
	
	public InterfacesLinkCell(){}

	@Override
	public void render(Context context, String value, SafeHtmlBuilder sb) {
		sb.appendHtmlConstant(value);
	}


}
