package ch.systemsx.sybit.crkwebui.client.controllers;

import java.util.List;

import model.PdbScore;
import ch.systemsx.sybit.crkwebui.client.data.StatusData;

import com.google.gwt.core.client.GWT;

public interface MainController 
{
	public static final AppProperties CONSTANTS = (AppProperties) GWT.create(AppProperties.class);
	
	public abstract void test(String testValue);
	
	public abstract void displayView(String token);
	
	public abstract void displayResults(String selectedId);
	
	public abstract void getStatusData(String selectedId);
	
	public abstract void getResultData(String selectedId);
	
	public abstract void displayInputView();
	
	public abstract void displayResultView(PdbScore resultData);
	
	public abstract void displayStatusView(StatusData statusData);
	
	public abstract void showError(String errorMessage);
	
	public abstract void killJob(String selectedId);
	
	public abstract void getJobsForCurrentSession();
	
	public abstract void setJobs(List<StatusData> statusData);
	
	public abstract void untieJobsFromSession();
}
