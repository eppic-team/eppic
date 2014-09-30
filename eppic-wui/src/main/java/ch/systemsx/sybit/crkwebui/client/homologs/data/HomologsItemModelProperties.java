package ch.systemsx.sybit.crkwebui.client.homologs.data;

import ch.systemsx.sybit.crkwebui.shared.model.HomologIdentityData;

import com.google.gwt.editor.client.Editor.Path;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;

/**
 * Interface used to map model class to grid
 * @author nikhil
 *
 */
public interface HomologsItemModelProperties extends PropertyAccess<HomologsItemModel> {
	
	  @Path("uid")
	  ModelKeyProvider<HomologsItemModel> key();
	  
	  ValueProvider<HomologsItemModel, String> uniId();
	  
	  ValueProvider<HomologsItemModel, HomologIdentityData> idData();
	  
	  ValueProvider<HomologsItemModel, Double> queryCov();
	  
	  ValueProvider<HomologsItemModel, String> firstTaxon();
	  
	  ValueProvider<HomologsItemModel, String> lastTaxon(); 
	  

}