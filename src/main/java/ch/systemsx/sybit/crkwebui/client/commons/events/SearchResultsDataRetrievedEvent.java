package ch.systemsx.sybit.crkwebui.client.commons.events;

import java.util.List;

import ch.systemsx.sybit.crkwebui.client.commons.handlers.SearchResultsDataRetrievedHandler;
import ch.systemsx.sybit.crkwebui.shared.model.PDBSearchResult;

import com.google.gwt.event.shared.GwtEvent;

public class SearchResultsDataRetrievedEvent extends GwtEvent<SearchResultsDataRetrievedHandler> 
{
	public static Type<SearchResultsDataRetrievedHandler> TYPE = new Type<SearchResultsDataRetrievedHandler>();
	
	/**
	 * Retrieved results data.
	 */
	private final List<PDBSearchResult> results;
	private final String uniProtId;
	
	public SearchResultsDataRetrievedEvent(String uniProtId, List<PDBSearchResult> results)
	{
		this.uniProtId = uniProtId;
		this.results = results;
	}
	
	@Override
	public com.google.gwt.event.shared.GwtEvent.Type<SearchResultsDataRetrievedHandler> getAssociatedType() 
	{
		return TYPE;
	}

	@Override
	protected void dispatch(SearchResultsDataRetrievedHandler handler) 
	{
		handler.onSearchResultsDataRetrieved(this);
	}

	/**
	 * Gets retrieved results data.
	 * @return retrieved results data
	 */
	public List<PDBSearchResult> getResults() {
		return results;
	}
	
	public String getUniProtId(){
		return uniProtId;
	}

}