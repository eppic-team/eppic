package ch.systemsx.sybit.crkwebui.client.search.gui.cells;

import ch.systemsx.sybit.crkwebui.client.homologs.util.IdentityColorPicker;
import ch.systemsx.sybit.crkwebui.shared.model.SequenceClusterType;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

/**
 * Cell used to display SequenceCLusterType values
 *
 */
public class SequenceClusterTypeCell extends AbstractCell<SequenceClusterType>  
{

    public static final String[] SEQUENCE_CLUSTER_COLORS = new String[10];

    static {
	for(int i=0; i<10; i++)
	    SEQUENCE_CLUSTER_COLORS[i] = IdentityColorPicker.getColor((i+1)/10.0); 
    }

    @Override
    public void render(com.google.gwt.cell.client.Cell.Context context,
	    SequenceClusterType value, SafeHtmlBuilder sb) {

	if (value == null) {
	    return;
	}
	String v = value.toString().replace("C", "") + "%";
	String color = getColorForSequenceCluster(value);
	sb.appendHtmlConstant("<span style=\"font-weight:bold;\"><div class=\"clusterCircle\" style=\"background-color: " + color + ";\"></div>"+ v +"</span>");
    }

    private String getColorForSequenceCluster(SequenceClusterType value) {
	int ind = Integer.parseInt(value.toString().replace("C", ""));
	return SEQUENCE_CLUSTER_COLORS[ind / 10 - 1];
    }  

}
