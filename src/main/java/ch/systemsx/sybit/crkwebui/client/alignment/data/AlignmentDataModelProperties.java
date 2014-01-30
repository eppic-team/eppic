package ch.systemsx.sybit.crkwebui.client.alignment.data;

import com.google.gwt.editor.client.Editor.Path;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;

public interface AlignmentDataModelProperties extends PropertyAccess<AlignmentDataModel> {
	
	  @Path("uid")
	  ModelKeyProvider<AlignmentDataModel> key();
	  
	  ValueProvider<AlignmentDataModel, String[]> rowHeader();
	  
	  ValueProvider<AlignmentDataModel, Integer[]> startIndex();
	  
	  ValueProvider<AlignmentDataModel, Integer[]> endIndex();
	  
	  ValueProvider<AlignmentDataModel, String[]> alignment();
	  

}
