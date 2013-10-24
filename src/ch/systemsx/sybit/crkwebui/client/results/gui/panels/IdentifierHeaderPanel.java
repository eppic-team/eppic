package ch.systemsx.sybit.crkwebui.client.results.gui.panels;

import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.ColumnData;
import com.extjs.gxt.ui.client.widget.layout.ColumnLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.google.gwt.user.client.ui.Image;

public class IdentifierHeaderPanel extends LayoutContainer
{
    private PDBIdentifierPanel pdbIdentifierPanel;
    private PDBIdentifierSubtitlePanel pdbIdentifierSubtitlePanel;
    private LayoutContainer eppicLogoPanel;

    public IdentifierHeaderPanel(){
	this.setLayout(new ColumnLayout());

	LayoutContainer  pdbInfo = new LayoutContainer();
	pdbIdentifierPanel = new PDBIdentifierPanel();
	pdbInfo.add(pdbIdentifierPanel, new RowData(-1, -1, new Margins(0, 0, 1, 0)));

	pdbIdentifierSubtitlePanel = new PDBIdentifierSubtitlePanel();
	pdbInfo.add(pdbIdentifierSubtitlePanel, new RowData(-1, -1, new Margins(0, 0, 10, 0)));

	this.add(pdbInfo,  new ColumnData(.99));
	
	eppicLogoPanel = new LayoutContainer();
	
	Image logo = getLogo();
	eppicLogoPanel.add(logo, new RowData(-1, -1, new Margins(0, 0, 0, 0)));
	
	this.add(eppicLogoPanel, new ColumnData(100));
    }

    private Image getLogo() {
	String logoIconSource = "resources/images/eppic-logo.png";
	Image logo = new Image(logoIconSource);
	logo.setWidth("100px");
	logo.setHeight("40px");
	return logo;
    }
    
    public void setEppicLogoPanel(String eppicVersion){
    	eppicLogoPanel.removeAll();
    	
    	Image logo = getLogo();
    	eppicLogoPanel.add(logo, new RowData(-1, -1, new Margins(0, 0, 0, 0)));
    	
    	Label eppicVersionLabel = new Label(eppicVersion);
    	eppicVersionLabel.addStyleName("eppic-version");
    	eppicVersionLabel.setStyleAttribute("padding-left","70px");
    	eppicLogoPanel.add(eppicVersionLabel, new RowData(-1, -1, new Margins(0)));
    	
    	eppicLogoPanel.layout(true);
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
    }
}
