package ch.systemsx.sybit.crkwebui.client.search.gui.util;

import java.util.Collections;
import java.util.List;

import com.google.gwt.core.client.Callback;
import com.sencha.gxt.data.shared.SortInfo;
import com.sencha.gxt.data.shared.loader.DataProxy;
import com.sencha.gxt.data.shared.loader.PagingLoadConfig;
import com.sencha.gxt.data.shared.loader.PagingLoadResult;
import com.sencha.gxt.data.shared.loader.PagingLoadResultBean;

import eppic.model.dto.PDBSearchResult;

public class PagingMemoryProxy implements DataProxy<PagingLoadConfig, PagingLoadResult<PDBSearchResult>> {

    private final List<PDBSearchResult> totalList;

    public PagingMemoryProxy(List<PDBSearchResult> totalList) {
        this.totalList = totalList;
    }

    @Override
    public void load(PagingLoadConfig loadConfig, Callback<PagingLoadResult<PDBSearchResult>, Throwable> callback) {
        // Get results list based on the data the proxy was created with
        SortInfo sortInfo = null;
	if(loadConfig.getSortInfo().size() > 0)
	    sortInfo = loadConfig.getSortInfo().get(0);
	if(sortInfo != null)
	    Collections.sort(totalList, new PDBSearchResultComparator(sortInfo));
	int limit = loadConfig.getOffset() + loadConfig.getLimit();
        if (totalList.isEmpty()) {
            limit = 0;
        } else if (limit >= totalList.size()) {
            limit = totalList.size();
        }
        List<PDBSearchResult> results = totalList.subList(loadConfig.getOffset(), limit);

        PagingLoadResultBean<PDBSearchResult> bean = new PagingLoadResultBean<PDBSearchResult>(results, totalList.size(), loadConfig.getOffset());
        callback.onSuccess(bean);
    }

}