/**
 * 
 */
package ch.systemsx.sybit.crkwebui.client.homologs.gui.cells;

import ch.systemsx.sybit.crkwebui.client.homologs.util.IdentityColorPicker;
import ch.systemsx.sybit.crkwebui.shared.model.HomologIdentityData;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.sencha.gxt.cell.core.client.ResizeCell;

/**
 * Cell to render bars in the homologs grid
 * @author biyani_n
 *
 */
public class IdentityBarCell extends ResizeCell<HomologIdentityData> {
	
	/**
	 * Class used to set the options of the Bar
	 * @author biyani_n
	 *
	 */
	public class IdentityBarCellOptions{
		
		private int barHeightInPixels;		//Height of the bar
		private int barWidthPercent;		//Percentage of width to be used by bar 0-100
		private String barUnfilledColor;	//The unfilled color of the bar
		private String barBorderColor;		//Border color of the bar
		
		public IdentityBarCellOptions(){
			barHeightInPixels = 15;
			barWidthPercent = 85;
			barUnfilledColor = "#F1F1F1";
			barBorderColor = "#E1E1E1";
		}
		
		public IdentityBarCellOptions(int barHeightInPixels,
									  int barWidthPercent,
									  String barUnfilledColor,
									  String barBorderColor){
			this.barHeightInPixels = barHeightInPixels;
			this.barWidthPercent = barWidthPercent;
			this.barUnfilledColor = barUnfilledColor;
			this.barBorderColor = barBorderColor;
		}

		public int getBarHeightInPixels() {
			return barHeightInPixels;
		}

		public void setBarHeightInPixels(int barHeightInPixels) {
			this.barHeightInPixels = barHeightInPixels;
		}

		public int getBarWidthPercent() {
			return barWidthPercent;
		}

		public void setBarWidthPercent(int barWidthPercent) {
			this.barWidthPercent = barWidthPercent;
		}

		public String getBarUnfilledColor() {
			return barUnfilledColor;
		}

		public void setBarUnfilledColor(String barUnfilledColor) {
			this.barUnfilledColor = barUnfilledColor;
		}

		public String getBarBorderColor() {
			return barBorderColor;
		}

		public void setBarBorderColor(String barBorderColor) {
			this.barBorderColor = barBorderColor;
		}
		
	}
	
	private IdentityBarCellOptions options;
	
	public IdentityBarCell(){
		options = new IdentityBarCellOptions();
	}
	
	public IdentityBarCell(IdentityBarCellOptions options){
		this.options = options;
	}
	
	@Override
	public void render(com.google.gwt.cell.client.Cell.Context context,
			HomologIdentityData value, SafeHtmlBuilder sb) {
		sb.appendHtmlConstant(generateComponent(value));
		
	}

	/**
	 * Creates a html component based on the identity data
	 * @param data
	 * @return the html component to be drawn
	 */
	private String generateComponent(HomologIdentityData data){		
		
		int matchLeftMargin = Math.round((100*(data.getQueryStart()-1))/data.getQueryLength());
		int matchWidth = Math.round((100*(data.getQueryEnd()-data.getQueryStart()))/data.getQueryLength());
		
		String color = IdentityColorPicker.getColor(data.getSeqIdToQuery()/100.0);

		String draw = 
						"<div style='width=100%; height=100%;'>"
						+ "<div style='display:inline-block; vertical-align:middle; "
						+ 			  "width:"+ options.getBarWidthPercent() +"%; "
						+ 			  "height:"+ options.getBarHeightInPixels() +"px; "
						+ 			  "border:1px solid "+ options.getBarBorderColor() +"; "
						+ 			  "background-color:"+ options.getBarUnfilledColor() +"'>"
						+ 	"<div style='width:"+ matchWidth +"%; "
						+   			"height:"+ options.getBarHeightInPixels() +"px; "
						+ 				"margin-left:"+ matchLeftMargin +"%; "
						+				"background-color:"+ color +";"
						+ 				"text-align:center;'>"
						+ 	"</div>"
						+ "</div>"
						+ "<div style='margin-left:2px; "
						+ 			  "display:inline-block; text-align:center; vertical-align:middle; "
						+ 			  "width="+ (100-options.getBarWidthPercent()) +"%; "
						+             "height=100%;'>"
						+ 		Math.round(data.getSeqIdToQuery()) + "% "
						+ "</div>"
						+"</div>";
		
		return draw;
	}

}
