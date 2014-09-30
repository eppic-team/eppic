package ch.systemsx.sybit.crkwebui.client.alignment.data;

import ch.systemsx.sybit.crkwebui.shared.model.PairwiseAlignmentData;
import ch.systemsx.sybit.crkwebui.shared.model.PairwiseAlignmentInfo;

import com.google.gwt.editor.client.Editor.Path;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;

public interface AlignmentDataModelProperties extends PropertyAccess<AlignmentDataModel> {
	
	  @Path("uid")
	  ModelKeyProvider<AlignmentDataModel> key();
	  
	  ValueProvider<AlignmentDataModel, PairwiseAlignmentInfo> rowHeader();
	  
	  ValueProvider<AlignmentDataModel, PairwiseAlignmentInfo> startIndex();
	  
	  ValueProvider<AlignmentDataModel, PairwiseAlignmentInfo> endIndex();
	  
	  ValueProvider<AlignmentDataModel, PairwiseAlignmentData> alignment();
	  

}
