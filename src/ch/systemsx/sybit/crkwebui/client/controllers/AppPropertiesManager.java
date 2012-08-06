package ch.systemsx.sybit.crkwebui.client.controllers;

import com.google.gwt.core.client.GWT;

/**
 * Application properties manager.
 *
 */
public class AppPropertiesManager 
{
	public static final AppProperties CONSTANTS = (AppProperties) GWT.create(AppProperties.class);
}
