package ch.systemsx.sybit.crkwebui.client.residues.gui.grid.utils;

import java.util.Comparator;

import eppic.dtomodel.Residue;

import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import com.sencha.gxt.data.shared.SortDir;

public class ResidueComparator implements Comparator<Residue> {

	private String fieldName;
	private SortDir dir;
	private RegExp regExp = RegExp.compile("\\d+");

	public ResidueComparator(String fieldName, SortDir dir) {
		this.fieldName = fieldName;
		this.dir = dir;
	}


	@Override
	public int compare(Residue o1, Residue o2) {
		if(SortDir.ASC == dir)
			return compare(o1, o2, fieldName);
		else
			return -1 * compare(o1, o2, fieldName);
	}

	private int compare(Residue a, Residue b, String propertieName) {
		if("side".equals(propertieName))
			return new Boolean(a.getSide()).compareTo(b.getSide());
		if("residueNumber".equals(propertieName))
			return new Integer(a.getResidueNumber()).compareTo(b.getResidueNumber());
		if("pdbResidueNumber".equals(propertieName))
			return compareResidueNumber(a.getPdbResidueNumber(), b.getPdbResidueNumber());
		if("residueType".equals(propertieName)) 
			return a.getResidueType().compareTo(b.getResidueType());
		if("asa".equals(propertieName))
			return new Double(a.getAsa()).compareTo(b.getAsa());
		if("bsa".equals(propertieName))
			return new Double(a.getBsa()).compareTo(b.getBsa());
		if("bsaPercentage".equals(propertieName))
			return new Double(a.getBsaPercentage()).compareTo(b.getBsaPercentage());
		if("region".equals(propertieName))
			return new Short(a.getRegion()).compareTo(b.getRegion());
		if("entropyScore".equals(propertieName))
			return new Double(a.getEntropyScore()).compareTo(b.getEntropyScore());
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
