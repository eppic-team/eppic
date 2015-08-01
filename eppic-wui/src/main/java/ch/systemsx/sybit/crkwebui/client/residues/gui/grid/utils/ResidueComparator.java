package ch.systemsx.sybit.crkwebui.client.residues.gui.grid.utils;

import java.util.Comparator;

import ch.systemsx.sybit.crkwebui.shared.model.ResidueBurial;

import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import com.sencha.gxt.data.shared.SortDir;

public class ResidueComparator implements Comparator<ResidueBurial> {

	private String fieldName;
	private SortDir dir;
	private RegExp regExp = RegExp.compile("\\d+");

	public ResidueComparator(String fieldName, SortDir dir) {
		this.fieldName = fieldName;
		this.dir = dir;
	}


	@Override
	public int compare(ResidueBurial o1, ResidueBurial o2) {
		if(SortDir.ASC == dir)
			return compare(o1, o2, fieldName);
		else
			return -1 * compare(o1, o2, fieldName);
	}

	private int compare(ResidueBurial a, ResidueBurial b, String propertieName) {
		if("side".equals(propertieName))
			return new Boolean(a.getSide()).compareTo(b.getSide());
		if("residueNumber".equals(propertieName))
			return new Integer(a.getResidueInfo().getResidueNumber()).compareTo(b.getResidueInfo().getResidueNumber());
		if("pdbResidueNumber".equals(propertieName))
			return compareResidueNumber(a.getResidueInfo().getPdbResidueNumber(), b.getResidueInfo().getPdbResidueNumber());
		if("residueType".equals(propertieName)) 
			return a.getResidueInfo().getResidueType().compareTo(b.getResidueInfo().getResidueType());
		if("asa".equals(propertieName))
			return new Double(a.getAsa()).compareTo(b.getAsa());
		if("bsa".equals(propertieName))
			return new Double(a.getBsa()).compareTo(b.getBsa());
		if("bsaPercentage".equals(propertieName))
			return new Double(a.getBsaPercentage()).compareTo(b.getBsaPercentage());
		if("region".equals(propertieName))
			return new Integer(a.getRegion()).compareTo(b.getRegion());
		if("entropyScore".equals(propertieName))
			return new Double(a.getResidueInfo().getEntropyScore()).compareTo(b.getResidueInfo().getEntropyScore());
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
