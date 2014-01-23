package ch.systemsx.sybit.crkwebui.client.residues.gui.grid.utils;

import java.util.Comparator;

import ch.systemsx.sybit.crkwebui.client.residues.data.InterfaceResidueItemModel;

import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import com.sencha.gxt.data.shared.SortDir;

public class InterfaceResidueItemModelComparator implements Comparator<InterfaceResidueItemModel> {

    private String fieldName;
    private SortDir dir;
    private RegExp regExp = RegExp.compile("\\d+");
    
    public InterfaceResidueItemModelComparator(String fieldName, SortDir dir) {
	this.fieldName = fieldName;
	this.dir = dir;
    }


    @Override
    public int compare(InterfaceResidueItemModel o1, InterfaceResidueItemModel o2) {
	if(SortDir.ASC == dir)
	    return compare(o1, o2, fieldName);
	else
	    return -1 * compare(o1, o2, fieldName);
    }

    private int compare(InterfaceResidueItemModel a, InterfaceResidueItemModel b, String propertieName) {
	if("structure".equals(propertieName))
	    return new Integer(a.getStructure()).compareTo(b.getStructure());
	if("residueNumber".equals(propertieName))
	    return new Integer(a.getResidueNumber()).compareTo(b.getResidueNumber());
	if("pdbResidueNumber".equals(propertieName))
	    return compareResidueNumber(a.getPdbResidueNumber(), b.getPdbResidueNumber());
	if("residueType".equals(propertieName)) 
	    return a.getResidueType().compareTo(b.getResidueType());
	if("asa".equals(propertieName))
	    return new Float(a.getAsa()).compareTo(b.getAsa());
	if("bsa".equals(propertieName))
	    return new Float(a.getBsa()).compareTo(b.getBsa());
	if("bsaPercentage".equals(propertieName))
	    return new Float(a.getBsaPercentage()).compareTo(b.getBsaPercentage());
	if("assignment".equals(propertieName))
	    return new Integer(a.getAssignment()).compareTo(b.getAssignment());
	if("entropyScore".equals(propertieName))
	    return new Float(a.getEntropyScore()).compareTo(b.getEntropyScore());
	return 0;
    }

    private int compareResidueNumber(String pdbResidueNum1, String pdbResidueNum2) {
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
}
