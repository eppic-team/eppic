package ch.systemsx.sybit.crkwebui.client.results.gui.cells;

import java.util.HashSet;
import java.util.Set;

import ch.systemsx.sybit.crkwebui.client.commons.appdata.AppPropertiesManager;
import ch.systemsx.sybit.crkwebui.client.commons.events.SelectResultsRowEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.ShowViewerEvent;
import ch.systemsx.sybit.crkwebui.client.commons.managers.EventBusManager;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

/**
 * Cell used to display interfaces thumbnails.
 * @author nikhil
 *
 */
public class ThumbnailCell extends AbstractCell<String>{
	
	@Override
	public void render(Context context,	String value, SafeHtmlBuilder sb) {		
		sb.appendHtmlConstant("<img src='"+ value + 
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