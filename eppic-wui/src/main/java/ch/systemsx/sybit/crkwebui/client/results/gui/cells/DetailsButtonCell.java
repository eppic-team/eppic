package ch.systemsx.sybit.crkwebui.client.results.gui.cells;

import java.util.HashSet;
import java.util.Set;

import ch.systemsx.sybit.crkwebui.client.commons.events.SelectResultsRowEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.ShowDetailsEvent;
import ch.systemsx.sybit.crkwebui.client.commons.managers.EventBusManager;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;


/**
 * Cell used to display details button which shows the residues.
 * @author nikhil
 *
 */
public class DetailsButtonCell extends AbstractCell<String>
{
	@Override
	public void render(Context context,
			String value, SafeHtmlBuilder sb) {
		String imageHtml = "<img src=\"resources/icons/details_button.png\" "
				+ "onmouseover=\"this.src='resources/icons/details_button_over.png';\" "
				+ "onmouseout=\"this.src='resources/icons/details_button.png';\" />";
		sb.appendHtmlConstant(imageHtml);
		
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
			EventBusManager.EVENT_BUS.fireEvent(new ShowDetailsEvent());
        }
    }

}
