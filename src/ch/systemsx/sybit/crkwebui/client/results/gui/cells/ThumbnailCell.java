package ch.systemsx.sybit.crkwebui.client.results.gui.cells;

import java.util.HashSet;
import java.util.Set;

import ch.systemsx.sybit.crkwebui.client.commons.appdata.AppPropertiesManager;
import ch.systemsx.sybit.crkwebui.client.commons.appdata.ApplicationContext;
import ch.systemsx.sybit.crkwebui.client.commons.events.SelectResultsRowEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.ShowViewerEvent;
import ch.systemsx.sybit.crkwebui.client.commons.managers.EventBusManager;
import ch.systemsx.sybit.crkwebui.client.results.data.InterfaceItemModel;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.sencha.gxt.data.shared.ListStore;

/**
 * Cell used to display interfaces thumbnails.
 * @author nikhil
 *
 */
public class ThumbnailCell extends AbstractCell<String>{

private ListStore<InterfaceItemModel> itemsStore;
	
	public ThumbnailCell(ListStore<InterfaceItemModel> itemsStore){
		this.itemsStore = itemsStore;
	}
	
	@Override
	public void render(Context context,	String value, SafeHtmlBuilder sb) {
		int row = context.getIndex();
        InterfaceItemModel item = itemsStore.get(row);
		value = ApplicationContext.getSettings().getResultsLocation();
		String source = value +
				ApplicationContext.getPdbScoreItem().getJobId() + 
				"/" + ApplicationContext.getPdbScoreItem().getPdbName() +
				"." + item.getId() + ".75x75.png";
		
		sb.appendHtmlConstant("<img src='"+ source + 
				"' qtip='" + AppPropertiesManager.CONSTANTS.results_grid_thumbnail_tooltip_text() + "'/>");
	}
	
	@Override
    public Set<String> getConsumedEvents() {
        Set<String> events = new HashSet<String>();
        events.add("click");
        return events;
    }

    @Override
    public void onBrowserEvent(Context context, Element parent, String value, NativeEvent event, ValueUpdater<String> valueUpdater) {
        super.onBrowserEvent(context, parent, value, event, valueUpdater);
        if (parent.getFirstChildElement().isOrHasChild(Element.as(event.getEventTarget()))) {
	        int row = context.getIndex();
	        EventBusManager.EVENT_BUS.fireEvent(new SelectResultsRowEvent(row));
			EventBusManager.EVENT_BUS.fireEvent(new ShowViewerEvent());
        }
    }
	
}