package ch.systemsx.sybit.crkwebui.client.gui;

import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.layout.CenterLayout;
import com.google.gwt.user.client.ui.HTML;

public class RecaptchaPanel extends ContentPanel 
{
	private HTML captchaDiv;
	private String key;

	public RecaptchaPanel(String key) 
	{
		this.key = key;
		this.setLayout(new CenterLayout());
		captchaDiv = new HTML("<div id=\"recaptcha_div\"/>");
		this.add(captchaDiv);
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