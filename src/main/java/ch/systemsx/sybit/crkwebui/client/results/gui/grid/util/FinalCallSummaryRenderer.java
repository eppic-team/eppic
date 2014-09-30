package ch.systemsx.sybit.crkwebui.client.results.gui.grid.util;

public class FinalCallSummaryRenderer extends MethodsSummaryRenderer
{
    @Override
    protected String convertIntToCall(int callInt)
    {
    	
	//String icon = addIcon(callInt);
	callInt = removeIconMarker(callInt);

    // commented out while the confidence calculation implementation is improved - JD 08.07.2014 
	//return super.convertIntToCall(callInt).toUpperCase() + icon;
	return super.convertIntToCall(callInt).toUpperCase() ;
    }

	private int removeIconMarker(int i) {
	if(i > 90)
	    return i - 100;
	if(i > 9)
	    return i - 10;
	return i;
    }

    @SuppressWarnings("unused")
	private String addIcon(int i) {
	if(i > 90)
	    return "<img src=\"resources/icons/excellent.png\" width=\"16\">";
	if(i > 9)
	    return "</img><img src=\"resources/icons/good.png\" width=\"16\"></img>";
	return "";
    }
}
