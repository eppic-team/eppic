package ch.systemsx.sybit.crkwebui.client.top.gui.panels;

import ch.systemsx.sybit.crkwebui.client.commons.appdata.AppPropertiesManager;
import ch.systemsx.sybit.crkwebui.client.commons.appdata.ApplicationContext;
import ch.systemsx.sybit.crkwebui.client.commons.events.HideTopPanelSearchBoxEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.ShowTopPanelSearchBoxEvent;
import ch.systemsx.sybit.crkwebui.client.commons.gui.info.PopUpInfo;
import ch.systemsx.sybit.crkwebui.client.commons.handlers.HideTopPanelSearchBoxHandler;
import ch.systemsx.sybit.crkwebui.client.commons.handlers.ShowTopPanelSearchBoxHandler;
import ch.systemsx.sybit.crkwebui.client.commons.managers.EventBusManager;
import ch.systemsx.sybit.crkwebui.shared.validators.PdbCodeVerifier;

import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.FlexTable;
import com.sencha.gxt.widget.core.client.button.IconButton;
import com.sencha.gxt.widget.core.client.button.IconButton.IconConfig;
import com.sencha.gxt.widget.core.client.container.HorizontalLayoutContainer;
import com.sencha.gxt.widget.core.client.form.TextField;

public class TopPanelSearchBox extends HorizontalLayoutContainer {
	
	private static final int FIELD_WIDTH = 200;
	
	private FlexTable table;
	private TextField searchField;
	private IconButton searchButton;
	
	private boolean visibility;
	
	public TopPanelSearchBox(){
		this.setBorders(false);
		this.setWidth(300);
		this.addStyleName("eppic-default-font");
		
		table = new FlexTable();
		table.setCellSpacing(0);
	    table.setCellPadding(0);
		
		searchField = createSearchField();
		table.setWidget(0, 0, searchField);
		
		searchButton = createSearchButton();
		table.setWidget(0, 1, searchButton);
		
		this.add(table);
		
		initializeEventsListeners();
	}

	private IconButton createSearchButton() {
		IconConfig iconCnfg = new IconConfig("eppic-top-panel-search-button", "eppic-top-panel-search-button-over");
		
		IconButton iconButton = new IconButton(iconCnfg){
			@Override
			protected void onClick(Event event) {
				super.onClick(event);
				loadPdbData();
			}
		};
		iconButton.setPixelSize(22, 22);
		iconButton.setBorders(true);		
		
		return iconButton;
	}

	protected void loadPdbData() {
		String pdbCode = searchField.getCurrentValue();
		if( pdbCode != null){
			pdbCode = pdbCode.toLowerCase().trim();
			if(PdbCodeVerifier.isTrimmedValid(pdbCode)){
				ApplicationContext.setSelectedJobId(pdbCode);
				History.newItem("id/" + pdbCode);
				searchField.finishEditing();
				searchField.reset();
			}
			else{
				PopUpInfo.show(AppPropertiesManager.CONSTANTS.pdb_code_box_wrong_code_header(),
						 AppPropertiesManager.CONSTANTS.pdb_code_box_wrong_code_supporting_text()+ " " + pdbCode);
				searchField.finishEditing();
				searchField.reset();
				searchField.focus();
			}
		}
		
	}

	private TextField createSearchField() {
		final TextField pdbCodeField = new TextField();
		pdbCodeField.setWidth(FIELD_WIDTH);
		pdbCodeField.addStyleName("eppic-default-font");
		pdbCodeField.setAllowBlank(true);
		pdbCodeField.setEmptyText(AppPropertiesManager.CONSTANTS.top_search_panel_box_empty_text());
		pdbCodeField.addKeyDownHandler(new KeyDownHandler() {
			
			@Override
			public void onKeyDown(KeyDownEvent event) {
				if(event.getNativeKeyCode() == KeyCodes.KEY_ENTER)
					loadPdbData();
			}
		});
		
		return pdbCodeField;
	}
	
	/**
	 * Shows/Hides the panel according to the variable visibility
	 */
	private void setPanelVisibility(){
		this.setVisible(visibility);
	}
	
	/**
	 * Events listeners initialization.
	 */
	private void initializeEventsListeners()
	{
		EventBusManager.EVENT_BUS.addHandler(HideTopPanelSearchBoxEvent.TYPE, new HideTopPanelSearchBoxHandler() {
			
			@Override
			public void onHideSearchBox(HideTopPanelSearchBoxEvent event) {
				visibility = false;
				setPanelVisibility();
			}
		});
		
		EventBusManager.EVENT_BUS.addHandler(ShowTopPanelSearchBoxEvent.TYPE, new ShowTopPanelSearchBoxHandler() {
			
			@Override
			public void onShowSearchBox(ShowTopPanelSearchBoxEvent event) {
				visibility = true;
				setPanelVisibility();
			}
		});
	}

}
