package ch.systemsx.sybit.crkwebui.client.residues.util;

import ch.systemsx.sybit.crkwebui.client.residues.data.InterfaceResidueItemModel;

import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.store.StoreSorter;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;

public class ResiduesPanelSorter extends StoreSorter<InterfaceResidueItemModel> {

    private RegExp regExp = RegExp.compile("\\d+");
    
    @Override
    public int compare(Store<InterfaceResidueItemModel> store, InterfaceResidueItemModel m1, InterfaceResidueItemModel m2, String property) {
	if(property.equals(InterfaceResidueItemModel.PDB_RESIDUE_NUMBER_PROPERTY_NAME)) {
	    String pdbResidueNum1 = m1.get(InterfaceResidueItemModel.PDB_RESIDUE_NUMBER_PROPERTY_NAME);
	    String pdbResidueNum2 = m2.get(InterfaceResidueItemModel.PDB_RESIDUE_NUMBER_PROPERTY_NAME);
	    MatchResult matcher1 = regExp.exec(pdbResidueNum1);
	    Integer pdbNum1 = 0;
	    if(matcher1 != null)
		pdbNum1 = Integer.parseInt(matcher1.getGroup(0));
	    MatchResult matcher2 = regExp.exec(pdbResidueNum2);
	    Integer pdbNum2 = 0;
	    if(matcher2 != null)
		pdbNum2 = Integer.parseInt(matcher2.getGroup(0));
	    if(pdbNum1.equals(pdbNum2))
		return pdbResidueNum1.compareTo(pdbResidueNum2);
	    return pdbNum1.compareTo(pdbNum2);
	}
	return super.compare(store, m1, m2, property);
    }

}
