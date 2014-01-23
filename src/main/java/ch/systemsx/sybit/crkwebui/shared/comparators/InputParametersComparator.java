package ch.systemsx.sybit.crkwebui.shared.comparators;

import ch.systemsx.sybit.crkwebui.shared.model.InputParameters;

/**
 * Input parameters comparator.
 * @author AS
 */
public class InputParametersComparator 
{
	/**
	 * Checks if two instances of input parameters are equal.
	 * @param first first object to compare
	 * @param second second object to compare
	 * @return result of comparison
	 */
	public static boolean checkIfEquals(InputParameters first,
								 InputParameters second)
	{
		if(first.getMaxNrOfSequences() != second.getMaxNrOfSequences())
		{
			return false;
		}
		
		if(first.getReducedAlphabet() != second.getReducedAlphabet())
		{
			return false;
		}
		
		if(Math.abs(first.getHardIdentityCutoff() - second.getHardIdentityCutoff()) > 0.0001)
		{
			return false;
		}
		
		if(Math.abs(first.getSoftIdentityCutoff() - second.getSoftIdentityCutoff()) > 0.0001)
		{
			return false;
		}
		
		if((first.getSearchMode() == null) && (second.getSearchMode() != null))
		{
			return false;
		}
		
		if((first.getSearchMode() != null) && (second.getSearchMode() == null))
		{
			return false;
		}
		
		if((first.getSearchMode() != null) && (second.getSearchMode() != null))
		{
			if(!first.getSearchMode().equals(second.getSearchMode()))
			{
				return false;
			}
		}
		
		if((first.getMethods() == null) && (second.getMethods() != null))
		{
			return false;
		}
		
		if((first.getMethods() != null) && (second.getMethods() == null))
		{
			return false;
		}
		
		if((first.getMethods() != null) && (second.getMethods() != null))
		{
			if(!first.getMethods().equals(second.getMethods()))
			{
				return false;
			}
		}
		
		return true;
	}
}
