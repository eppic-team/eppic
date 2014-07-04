package ch.systemsx.sybit.crkwebui.client.results.gui.grid.util;

public class FinalCallSummaryRenderer extends MethodsSummaryRenderer
{
    @Override
    protected String convertIntToCall(int callInt)
    {
	String icon = addIcon(callInt);
	callInt = removeIconMarker(callInt);
	return super.convertIntToCall(callInt).toUpperCase() + icon;
    }

    private int removeIconMarker(int i) {
	if(i > 90)
	    return i - 100;
	if(i > 9)
	    return i - 10;
	return i;
    }

    private String addIcon(int i) {
	if(i > 90)
	    return "<img src=\"resources/icons/excellent.png\" width=\"16\">";
	if(i > 9)
	    return "</img><img src=\"resources/icons/good.png\" width=\"16\"></img>";
	return "";
    }
}
