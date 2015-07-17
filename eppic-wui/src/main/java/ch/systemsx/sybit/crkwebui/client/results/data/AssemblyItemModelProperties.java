package ch.systemsx.sybit.crkwebui.client.results.data;

import java.util.List;

import ch.systemsx.sybit.crkwebui.shared.model.InterfaceWarning;

import com.google.gwt.editor.client.Editor.Path;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;

//copy of interfaces - will convert this to assembly data
public interface AssemblyItemModelProperties extends PropertyAccess<AssemblyItemModel>  {
	
	  @Path("assemblyId")
	  ModelKeyProvider<AssemblyItemModel> key();
	 
	  ValueProvider<AssemblyItemModel, Integer> assemblyId();
	  
	  ValueProvider<AssemblyItemModel, String> identifier();
	  
	  ValueProvider<AssemblyItemModel, String> composition();
	  
	  ValueProvider<AssemblyItemModel, String> mmSize();
	  
	  ValueProvider<AssemblyItemModel, String> symmetry();
	  
	  ValueProvider<AssemblyItemModel, String> stoichiometry();
	  
	  ValueProvider<AssemblyItemModel, String> prediction();
	  
	  ValueProvider<AssemblyItemModel, String> detailsButtonText();
	   
	  ValueProvider<AssemblyItemModel, String> thumbnailUrl();
}
