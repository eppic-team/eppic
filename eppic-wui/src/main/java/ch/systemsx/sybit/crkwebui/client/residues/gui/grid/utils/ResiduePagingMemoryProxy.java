package ch.systemsx.sybit.crkwebui.client.residues.gui.grid.utils;

import java.util.List;

import eppic.model.dto.Residue;

import com.google.gwt.core.client.Callback;
import com.sencha.gxt.data.shared.SortDir;
import com.sencha.gxt.data.shared.SortInfo;
import com.sencha.gxt.data.shared.loader.MemoryProxy;
import com.sencha.gxt.data.shared.loader.PagingLoadConfig;
import com.sencha.gxt.data.shared.loader.PagingLoadResult;
import com.sencha.gxt.data.shared.loader.PagingLoadResultBean;

/**
 * Class used to create paging in the residues grid
 * @author nikhil
 *
 */
public class ResiduePagingMemoryProxy extends MemoryProxy<PagingLoadConfig, PagingLoadResult<Residue>> {

    private List<Residue> totalList;

    public ResiduePagingMemoryProxy(List<Residue> totalList) {
	super(null);
	this.totalList = totalList;
    }

    @Override
    public void load(PagingLoadConfig config, Callback<PagingLoadResult<Residue>, Throwable> callback) {
	List<Residue> list = totalList;

	if(!list.isEmpty()){
	    if (config.getSortInfo() != null && config.getSortInfo().size() > 0) {
		SortInfo sortinfo = config.getSortInfo().get(0);
		final String field = sortinfo.getSortField();
		final SortDir sortDir = sortinfo.getSortDir();
		java.util.Collections.sort(list, new ResidueComparator(field, sortDir));
	    }

	    int listStartIdx = config.getOffset();
	    int listEndIdx = config.getOffset()+ config.getLimit();
	    if(listEndIdx > list.size()-1) listEndIdx = list.size();
	    List<Residue>results = list.subList(listStartIdx, listEndIdx);
	    callback.onSuccess(new PagingLoadResultBean<Residue>(results, list.size(), config.getOffset()));
	}

    }

    public void setList(List<Residue> residueList){
	this.totalList = residueList;
    }
}