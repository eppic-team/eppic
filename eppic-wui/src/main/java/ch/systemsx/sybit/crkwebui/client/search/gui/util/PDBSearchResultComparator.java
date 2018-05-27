package ch.systemsx.sybit.crkwebui.client.search.gui.util;

import java.util.Comparator;

import com.sencha.gxt.data.shared.SortDir;
import com.sencha.gxt.data.shared.SortInfo;

import eppic.dtomodel.PDBSearchResult;

public class PDBSearchResultComparator implements Comparator<PDBSearchResult> {

    private final SortInfo sortInfo;
    
    public PDBSearchResultComparator(SortInfo sortInfo) {
	this.sortInfo = sortInfo;
    }

    @Override
    public int compare(PDBSearchResult o1, PDBSearchResult o2) {
	int ascendingCompariosn = compareAscending(o1, o2); 
	return (sortInfo.getSortDir() == SortDir.ASC)?ascendingCompariosn: -1 * ascendingCompariosn;
    }

    int compareAscending(PDBSearchResult o1, PDBSearchResult o2) {
	switch (sortInfo.getSortField()) {
	case "sequenceClusterType":
	    return o1.getSequenceClusterType().compareTo(o2.getSequenceClusterType());
	case "pdbCode":
	    return o1.getPdbCode().compareTo(o2.getPdbCode());
	case "title":
	    return o1.getTitle().compareTo(o2.getTitle());
	case "spaceGroup":
	    return o1.getSpaceGroup().compareTo(o2.getSpaceGroup());
	case "cellA":
	    return Double.compare(o1.getCellA(),o2.getCellA());
	case "cellB":
	    return Double.compare(o1.getCellB(),o2.getCellB());
	case "cellC":
	    return Double.compare(o1.getCellC(),o2.getCellC());
	case "cellAlpha":
	    return Double.compare(o1.getCellAlpha(),o2.getCellAlpha());
	case "cellBeta":
	    return Double.compare(o1.getCellBeta(),o2.getCellBeta());
	case "cellGamma":
	    return Double.compare(o1.getCellGamma(),o2.getCellGamma());
	case "expMethod":
	    return o1.getExpMethod().compareTo(o2.getExpMethod());
	case "resolution":
	    return Double.compare(o1.getResolution(), o2.getResolution());
	case "rfreeValue":
	    return Double.compare(o1.getRfreeValue(), o2.getRfreeValue());
	case "crystalFormId":
	    return Double.compare(o1.getCrystalFormId(), o2.getCrystalFormId());
	default:
	    break;
	}
	return 0;
    }

}
