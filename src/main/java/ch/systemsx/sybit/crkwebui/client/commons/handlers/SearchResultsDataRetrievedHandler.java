package ch.systemsx.sybit.crkwebui.client.commons.handlers;

import ch.systemsx.sybit.crkwebui.client.commons.events.SearchResultsDataRetrievedEvent;

import com.google.gwt.event.shared.EventHandler;

public interface SearchResultsDataRetrievedHandler extends EventHandler 
{
	/**
	 * Method called when search data was retrieved.
	 * @param event
	 */
	 public void onSearchResultsDataRetrieved(SearchResultsDataRetrievedEvent event);
}

