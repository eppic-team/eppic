package ch.systemsx.sybit.crkwebui.client.results.gui.panels;

import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.ColumnData;
import com.extjs.gxt.ui.client.widget.layout.ColumnLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.google.gwt.user.client.ui.Image;

public class IdentifierHeaderPanel extends LayoutContainer
{
    private PDBIdentifierPanel pdbIdentifierPanel;
    private PDBIdentifierSubtitlePanel pdbIdentifierSubtitlePanel;

    public IdentifierHeaderPanel(){
	this.setLayout(new ColumnLayout());

	LayoutContainer  pdbInfo = new LayoutContainer();
	pdbIdentifierPanel = new PDBIdentifierPanel();
	pdbInfo.add(pdbIdentifierPanel, new RowData(-1, -1, new Margins(0, 0, 1, 0)));

	pdbIdentifierSubtitlePanel = new PDBIdentifierSubtitlePanel();
	pdbInfo.add(pdbIdentifierSubtitlePanel, new RowData(-1, -1, new Margins(0, 0, 10, 0)));

	this.add(pdbInfo,  new ColumnData(.99));
	
	Image logo = getLogo();
	this.add(logo, new ColumnData(100));
    }

    private Image getLogo() {
	String logoIconSource = "resources/images/eppic-logo.png";
	Image logo = new Image(logoIconSource);
	logo.setWidth("100px");
	logo.setHeight("40px");
	return logo;
    }

    public void setPDBText(String pdbName, String spaceGroup, String expMethod, double resolution, int inputType)
    {
	pdbIdentifierPanel.setPDBText(pdbName, spaceGroup, expMethod, resolution, inputType);
    }

    public void setPDBIdentifierSubtitle(String subtitle)
    {
	pdbIdentifierSubtitlePanel.setPDBIdentifierSubtitle(subtitle);
    }

    public void resize() {
	this.layout(true);
    }
}
