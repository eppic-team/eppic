package ch.systemsx.sybit.crkwebui.client.residues.util;

import java.util.Comparator;

import com.extjs.gxt.ui.client.util.DefaultComparator;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;

public class InterfaceResidueItemComparator implements Comparator<Object> {
   
    private RegExp regExp = RegExp.compile("\\d+");
    
    @Override
    public int compare(Object o1, Object o2) {
	if(o1 instanceof String && o2 instanceof String) {
	    String pdbResidueNum1 = ((String)o1);
	    String pdbResidueNum2 = ((String)o2);
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
	return DefaultComparator.INSTANCE.compare(o1, o2);
    }
}
