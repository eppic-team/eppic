package ch.systemsx.sybit.crkwebui.client.viewer.gui.windows;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasValue;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.core.client.util.ToggleGroup;
import com.sencha.gxt.widget.core.client.container.SimpleContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer.VerticalLayoutData;
import com.sencha.gxt.widget.core.client.event.BeforeShowEvent;
import com.sencha.gxt.widget.core.client.event.BeforeShowEvent.BeforeShowHandler;
import com.sencha.gxt.widget.core.client.form.CheckBox;

import ch.systemsx.sybit.crkwebui.client.commons.appdata.AppPropertiesManager;
import ch.systemsx.sybit.crkwebui.client.commons.appdata.ApplicationContext;
import ch.systemsx.sybit.crkwebui.client.commons.gui.windows.ResizableWindow;
import ch.systemsx.sybit.crkwebui.shared.model.WindowData;

public class ViewerSelectorWindow extends ResizableWindow{

	private static int WINDOW_DEFAULT_WIDTH = 380;
	private static int WINDOW_DEFAULT_HEIGHT = 220;
	
	private CheckBox local;
	private CheckBox jmol;
	private CheckBox pse;
	private ToggleGroup viewerGroup;
	
	public ViewerSelectorWindow(WindowData windowData) {
		super(WINDOW_DEFAULT_WIDTH, WINDOW_DEFAULT_HEIGHT, windowData);
		
		this.setHeadingHtml(AppPropertiesManager.CONSTANTS.viewer_window_title());
		this.setHideOnButtonClick(true);
		this.setModal(true);
		this.setBlinkModal(true);
		
		VerticalLayoutContainer mainContainer = new VerticalLayoutContainer();
		mainContainer.addStyleName("eppic-default-padding");
		mainContainer.addStyleName("eppic-default-font");
		this.setWidget(mainContainer);
		
		mainContainer.add(new SimpleContainer(), new VerticalLayoutData(0.5,-1));
		mainContainer.add(
				new HTMLPanel(AppPropertiesManager.CONSTANTS.viewer_window_box_title()),
				new VerticalLayoutData(1,-1, new Margins(0,0,20,0)));
		mainContainer.add(createCheckBoxContainer(), new VerticalLayoutData(-1,-1));
		
		this.addBeforeShowHandler(new BeforeShowHandler() {
			
			@Override
			public void onBeforeShow(BeforeShowEvent event) {
				String viewerCookie = Cookies.getCookie("crkviewer");
				if (viewerCookie != null) {
					if(viewerCookie.equals(AppPropertiesManager.CONSTANTS.viewer_local())) local.setValue(true);
					else if(viewerCookie.equals(AppPropertiesManager.CONSTANTS.viewer_jmol())) jmol.setValue(true);
					else if(viewerCookie.equals(AppPropertiesManager.CONSTANTS.viewer_pse())) pse.setValue(true);
				} else
					jmol.setValue(true);
				
			}
		});

	}
	
	/**
	 * Creates a check box group to select molecular viewer
	 * @return the container
	 */
	private VerticalLayoutContainer createCheckBoxContainer(){
		VerticalLayoutContainer con = new VerticalLayoutContainer();
		
		local =  new CheckBox();
		local.setName(AppPropertiesManager.CONSTANTS.viewer_local());
		local.setBoxLabel(AppPropertiesManager.CONSTANTS.viewer_local_label());
		
		jmol =  new CheckBox();
		jmol.setName(AppPropertiesManager.CONSTANTS.viewer_jmol());
		jmol.setBoxLabel(AppPropertiesManager.CONSTANTS.viewer_jmol_label());
		
		pse =  new CheckBox();
		pse.setName(AppPropertiesManager.CONSTANTS.viewer_pse());
		pse.setBoxLabel(AppPropertiesManager.CONSTANTS.viewer_pse_label());
		
		con.add(jmol);
		con.add(local);
		con.add(pse);
		
		viewerGroup = new ToggleGroup();
		viewerGroup.add(local);
		viewerGroup.add(jmol);
		viewerGroup.add(pse);
		viewerGroup.addValueChangeHandler(new ValueChangeHandler<HasValue<Boolean>>() {
			
			@Override
			public void onValueChange(ValueChangeEvent<HasValue<Boolean>> event) {
				ToggleGroup group = (ToggleGroup)event.getSource();
		        CheckBox box = (CheckBox)group.getValue();
		        Cookies.setCookie("crkviewer", box.getName());
				ApplicationContext.setSelectedViewer(box.getName());
				
			}
		});
		
		return con;
	}

}
