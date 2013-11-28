/**
 * 
 */
package ch.systemsx.sybit.crkwebui.client.residues.data;

import com.google.gwt.editor.client.Editor.Path;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;

/**
 * @author biyani_n
 *
 */
public interface InterfaceResidueItemModelProperties extends PropertyAccess<InterfaceResidueItemModel> {
	
	  @Path("residueNumber")
	  ModelKeyProvider<InterfaceResidueItemModel> key();
	 
	  ValueProvider<InterfaceResidueItemModel, Integer> residueNumber();
	  
	  ValueProvider<InterfaceResidueItemModel, String> pdbResidueNumber();
	  
	  ValueProvider<InterfaceResidueItemModel, String> residueType();
	  
	  ValueProvider<InterfaceResidueItemModel, Float> asa();
	  
	  ValueProvider<InterfaceResidueItemModel, Float> bsa();
	  
	  ValueProvider<InterfaceResidueItemModel, Float> bsaPercentage();
	  
	  ValueProvider<InterfaceResidueItemModel, Integer> assignment();
	  
	  ValueProvider<InterfaceResidueItemModel, Float> entropyScore();
	  
	  

}
