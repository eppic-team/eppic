package ch.systemsx.sybit.crkwebui.client.input.gui.panels;

import com.google.gwt.user.client.ui.HTML;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.container.CenterLayoutContainer;

/**
 * This panel is used to display captcha - depending on the settings on the server side.
 *
 */
public class RecaptchaPanel extends ContentPanel 
{
	private HTML captchaDiv;
	private String key;

	public RecaptchaPanel(String key) 
	{
		this.key = key;
		CenterLayoutContainer mainContainer = new CenterLayoutContainer();
		this.setWidget(mainContainer);
		captchaDiv = new HTML("<div id=\"recaptcha_div\"/>");
		mainContainer.add(captchaDiv);
		this.getHeader().setVisible(false);
	}

	public static native void create(String key, String div,
    		String theme, String lang, int tabIndex) /*-{
        $wnd.Recaptcha.create(key, "recaptcha_div", {
        theme:theme,
        lang:lang,
        tabindex:tabIndex 
        });
    }-*/;

	@Override
	protected void onAttach() {
		super.onAttach();
		create(key, "recaptcha_div", "blue", "en", 0);
	}

	@Override
	protected void onDetach() {
		destroy();
		super.onDetach();
	}

	public static native void destroy() /*-{
    	$wnd.Recaptcha.destroy();
	}-*/;

	public static native void reload() /*-{
    	$wnd.Recaptcha.reload();
	}-*/;

    public static native String getChallenge() /*-{
        return $wnd.Recaptcha.get_challenge();
    }-*/;

    public static native String getResponse() /*-{
        return $wnd.Recaptcha.get_response();
    }-*/;

    public static native void focusResponseField() /*-{
        return $wnd.Recaptcha.focus_response_field();
    }-*/;

    public static native void showHelp() /*-{
        return $wnd.Recaptcha.showhelp();
    }-*/;

    public static native void switchType(String newType) /*-{
        return $wnd.Recaptcha.switch_type(newType);
    }-*/;
}