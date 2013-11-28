/**
 * 
 */
package ch.systemsx.sybit.crkwebui.client.homologs.gui.cells;

import com.sencha.gxt.cell.core.client.ProgressBarCell;

/**
 * Cell to render bars in the homologs grid
 * @author biyani_n
 *
 */
public class PercentageBarCell extends ProgressBarCell {
	
	 @Override
     public boolean handlesSelection() {
       return true;
     }
	 
	public PercentageBarCell(String barText){
		super();
		this.setProgressText("{0}% "+ barText);
	    this.setWidth(120);
	}

}
