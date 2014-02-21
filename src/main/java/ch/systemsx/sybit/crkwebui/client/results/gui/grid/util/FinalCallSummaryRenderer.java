package ch.systemsx.sybit.crkwebui.client.results.gui.grid.util;

public class FinalCallSummaryRenderer extends MethodsSummaryRenderer
{
	@Override
	protected String convertIntToCall(int callInt)
	{
		return super.convertIntToCall(callInt).toUpperCase();
	}
}
