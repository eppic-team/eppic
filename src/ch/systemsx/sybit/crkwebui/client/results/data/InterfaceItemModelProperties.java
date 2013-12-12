package ch.systemsx.sybit.crkwebui.client.results.data;

import java.util.List;

import ch.systemsx.sybit.crkwebui.shared.model.WarningItem;

import com.google.gwt.editor.client.Editor.Path;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;

public interface InterfaceItemModelProperties extends PropertyAccess<InterfaceItemModel>  {
	
	  @Path("id")
	  ModelKeyProvider<InterfaceItemModel> key();
	 
	  ValueProvider<InterfaceItemModel, Integer> id();
	  
	  ValueProvider<InterfaceItemModel, Integer> clusterId();
	  
	  ValueProvider<InterfaceItemModel, Double> area();
	  
	  ValueProvider<InterfaceItemModel, String> name();
	   
	  ValueProvider<InterfaceItemModel, String> sizes();
	  
	  ValueProvider<InterfaceItemModel, String> geometryCall();
	  
	  ValueProvider<InterfaceItemModel, String> coreRimCall();
	  
	  ValueProvider<InterfaceItemModel, String> coreSurfaceCall();
	  
	  ValueProvider<InterfaceItemModel, String> finalCallName();
	  
	  ValueProvider<InterfaceItemModel, String> operator();
	  
	  ValueProvider<InterfaceItemModel, String> operatorType();
	  
	  ValueProvider<InterfaceItemModel, Boolean> isInfinite();
	  
	  ValueProvider<InterfaceItemModel, List<WarningItem>> warnings();
	  
	  ValueProvider<InterfaceItemModel, String> thumbnailUrl();
	  
	  ValueProvider<InterfaceItemModel, String> warningsImagePath();
	  
	  ValueProvider<InterfaceItemModel, String> detailsButtonText();

}
