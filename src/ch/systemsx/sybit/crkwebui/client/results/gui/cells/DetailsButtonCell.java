package ch.systemsx.sybit.crkwebui.client.results.gui.cells;

import ch.systemsx.sybit.crkwebui.client.commons.events.SelectResultsRowEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.ShowDetailsEvent;
import ch.systemsx.sybit.crkwebui.client.commons.managers.EventBusManager;

import com.sencha.gxt.cell.core.client.TextButtonCell;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;


/**
 * Cell used to display details button which shows the residues.
 * @author nikhil
 *
 */
public class DetailsButtonCell extends TextButtonCell
{
	public DetailsButtonCell(){
		super();
		this.addSelectHandler(new SelectHandler() {

			@Override
			public void onSelect(SelectEvent event) {
				Context c = event.getContext();
				int row = c.getIndex();
				EventBusManager.EVENT_BUS.fireEvent(new SelectResultsRowEvent(row));
				EventBusManager.EVENT_BUS.fireEvent(new ShowDetailsEvent());
			}

		});
	}

}
