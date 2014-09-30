package ch.systemsx.sybit.crkwebui.client.search.gui.cells;

import java.util.HashSet;
import java.util.Set;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.History;

public class PdbCodeCell extends AbstractCell<String> {

    @Override
    public void render(com.google.gwt.cell.client.Cell.Context context,
	    String value, SafeHtmlBuilder sb) {
	sb.appendHtmlConstant("<a style=\"cursor: pointer;\">" + value + "</a>");
    }

    @Override
    public Set<String> getConsumedEvents() {
	Set<String> events = new HashSet<String>();
	events.add("click");
	return events;
    }

    @Override
    public void onBrowserEvent(com.google.gwt.cell.client.Cell.Context context, Element parent, String value, NativeEvent event, ValueUpdater<String> valueUpdater) {
	super.onBrowserEvent(context, parent, value, event, valueUpdater);
	if ("click".equals(event.getType())) {
	    History.newItem("id/" + value);  
	}
    }
}
