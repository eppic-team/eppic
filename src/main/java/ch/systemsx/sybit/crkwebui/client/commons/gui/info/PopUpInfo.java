package ch.systemsx.sybit.crkwebui.client.commons.gui.info;

import com.sencha.gxt.core.client.util.Point;
import com.sencha.gxt.widget.core.client.info.DefaultInfoConfig;
import com.sencha.gxt.widget.core.client.info.Info;
import com.sencha.gxt.widget.core.client.info.InfoConfig;

public class PopUpInfo extends Info {

	@Override
	protected void onShowInfo() {
		super.onShowInfo();
	}

	/**
	 * @param title
	 * @param message
	 */
	public static void show(String title, String message) {
		show(title, message, 5000);
	}

	/**
	 * 
	 * @param title
	 * @param message
	 */
	public static void show(String title, String message, int miliseconds) {

		PopUpInfo info = new PopUpInfo();
		info.getElement().applyStyles("textAlign:center");
		
		InfoConfig config = new DefaultInfoConfig(title, message);
		config.setDisplay(miliseconds);

		info.show(config);

		info.setWidth(info.getOffsetWidth() + 30);
		Point p = info.position();
		p.setX(((p.getX() + info.getOffsetWidth()) / 2) - (info.getOffsetWidth() / 2));
		info.setPosition(p.getX(), 0);

	}
}
