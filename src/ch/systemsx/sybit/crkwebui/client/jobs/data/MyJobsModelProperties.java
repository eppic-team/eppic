package ch.systemsx.sybit.crkwebui.client.jobs.data;

import com.google.gwt.editor.client.Editor.Path;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;

/**
 * A class to handle the properties of the MyJobsModel data
 * @author biyani_n
 *
 */
public interface MyJobsModelProperties extends PropertyAccess<MyJobsModel> {
	
	  @Path("jobid")
	  ModelKeyProvider<MyJobsModel> key();
	 
	  ValueProvider<MyJobsModel, String> jobid();
	   
	  ValueProvider<MyJobsModel, String> status();
	  
	  ValueProvider<MyJobsModel, String> input();

}
