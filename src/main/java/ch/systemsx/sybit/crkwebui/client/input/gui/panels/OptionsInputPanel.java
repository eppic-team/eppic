package ch.systemsx.sybit.crkwebui.client.input.gui.panels;

import java.util.List;

import ch.systemsx.sybit.crkwebui.client.commons.appdata.AppPropertiesManager;
import ch.systemsx.sybit.crkwebui.client.commons.appdata.ApplicationContext;
import ch.systemsx.sybit.crkwebui.client.commons.util.StyleGenerator;
import ch.systemsx.sybit.crkwebui.client.input.data.SearchModeComboModel;
import ch.systemsx.sybit.crkwebui.client.input.listeners.SubmitKeyListener;
import ch.systemsx.sybit.crkwebui.shared.model.ApplicationSettings;
import ch.systemsx.sybit.crkwebui.shared.model.InputParameters;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor.Path;
import com.google.gwt.i18n.client.NumberFormat;
import com.sencha.gxt.cell.core.client.form.ComboBoxCell.TriggerAction;
import com.sencha.gxt.core.client.Style.Side;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer.VerticalLayoutData;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.form.FieldLabel;
import com.sencha.gxt.widget.core.client.form.NumberField;
import com.sencha.gxt.widget.core.client.form.NumberPropertyEditor.IntegerPropertyEditor;
import com.sencha.gxt.widget.core.client.form.validator.MaxNumberValidator;
import com.sencha.gxt.widget.core.client.form.validator.MinNumberValidator;
import com.sencha.gxt.widget.core.client.form.FieldSet;
import com.sencha.gxt.widget.core.client.form.ComboBox;
import com.sencha.gxt.widget.core.client.tips.ToolTipConfig;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;

/**
 * Panel used to display advanced parameters which can be selected by the user.
 * @author srebniak_a
 *
 */
public class OptionsInputPanel extends FieldSet 
{
	private NumberField<Integer> softIdentityCutOff;
	private NumberField<Integer> hardIdentityCutOff;
	private ListStore<SearchModeComboModel> searchModeValues;
	private ComboBox<SearchModeComboModel> searchModeCombo;
	private NumberField<Integer> maxNrOfSequences;
		
	private int LABEL_WIDTH = 150;
	private int FIELD_WIDTH = 200;

	interface SearchModeProperties extends PropertyAccess<SearchModeComboModel> {
		@Path("id")
	    ModelKeyProvider<SearchModeComboModel> id();
		@Path("searchMode")
	    LabelProvider<SearchModeComboModel> name();
	}
	
	public OptionsInputPanel(ApplicationSettings applicationSettings) 
	{
		List<String> searchModeDefaultList = applicationSettings.getSearchModeList();
		
		this.setHeadingHtml(StyleGenerator.defaultFontStyleString(AppPropertiesManager.CONSTANTS.input_advanced()));
		this.setCollapsible(true);
		this.setBorders(false);
		
		int height = 400;
		
		if(height > ApplicationContext.getAdjustedWindowData().getWindowHeight() * 0.4)
		{
			height = (int) (ApplicationContext.getAdjustedWindowData().getWindowHeight() * 0.4);
		}		

		createAlignmentsParametersPanel(searchModeDefaultList);
	}
	
	/**
	 * Creates the tool tip over the textboxes with text
	 * @param text
	 * @return
	 */
	private ToolTipConfig createToolTipConfig(String text){
		ToolTipConfig config = new ToolTipConfig();
	    config.setBodyHtml(StyleGenerator.defaultFontStyle(text));
	    config.setMouseOffset(new int[] {0, 0});
	    config.setAnchor(Side.LEFT);	    
	    return config;
	}
	

	/**
	 * Creates alignments parameters fieldset.
	 * @param helpIconPlugin help icon plugin
	 * @param searchModeDefaultList search mode default list
	 * @return alignments parameters fieldset
	 */
	private void createAlignmentsParametersPanel(List<String> searchModeDefaultList)
	{
		FieldSet alignmentsParametersFieldSet = new FieldSet();
		alignmentsParametersFieldSet.setHeadingHtml(
				StyleGenerator.defaultFontStyleString(AppPropertiesManager.CONSTANTS.parameters_allignment()));
		alignmentsParametersFieldSet.addStyleName("eppic-rounded-border");
		
		this.add(alignmentsParametersFieldSet);
		
		VerticalLayoutContainer vlc = new VerticalLayoutContainer();
		
		alignmentsParametersFieldSet.add(vlc);
		
		softIdentityCutOff = createSoftIdentityCutoff();
		FieldLabel softIdentityCutOffLabel = new FieldLabel(softIdentityCutOff); 
		softIdentityCutOffLabel.setHTML(
				StyleGenerator.defaultFontStyle(AppPropertiesManager.CONSTANTS.parameters_soft_identity_cutoff()));
		softIdentityCutOffLabel.setLabelWidth(LABEL_WIDTH);
		vlc.add(softIdentityCutOffLabel, new VerticalLayoutData(1, -1, new Margins(5,5,5,20)));
		
		hardIdentityCutOff = createHardIdentityCutoff();
		FieldLabel hardIdentityCutOffLabel = new FieldLabel(hardIdentityCutOff);
		hardIdentityCutOffLabel.setHTML(
				StyleGenerator.defaultFontStyle(AppPropertiesManager.CONSTANTS.parameters_hard_identity_cutoff()));
		hardIdentityCutOffLabel.setLabelWidth(LABEL_WIDTH);
		vlc.add(hardIdentityCutOffLabel, new VerticalLayoutData(1, -1, new Margins(5,5,5,20)));

		maxNrOfSequences = createMaxNrOfSequences();
		FieldLabel maxNrOfSeqLabel = new FieldLabel(maxNrOfSequences);
		maxNrOfSeqLabel.setHTML(
				StyleGenerator.defaultFontStyle(AppPropertiesManager.CONSTANTS.parameters_max_num_sequences()));
		maxNrOfSeqLabel.setLabelWidth(LABEL_WIDTH);
		vlc.add(maxNrOfSeqLabel, new VerticalLayoutData(1, -1, new Margins(5,5,5,20)));
		
		searchModeValues = new ListStore<SearchModeComboModel>(new ModelKeyProvider<SearchModeComboModel>(){
		    @Override
		    public String getKey(SearchModeComboModel item) {
		      return item.getSearchMode();
		    }
		  });

		for (String value : searchModeDefaultList)
		{
			SearchModeComboModel model = new SearchModeComboModel(value);
			searchModeValues.add(model);
		}

		searchModeCombo = createSearchModeComboBox();
		FieldLabel searchModeComboLabel = new FieldLabel(searchModeCombo);
		searchModeComboLabel.setHTML(StyleGenerator.defaultFontStyle(AppPropertiesManager.CONSTANTS.parameters_search_mode()));
		searchModeComboLabel.setLabelWidth(LABEL_WIDTH);
		vlc.add(searchModeComboLabel, new VerticalLayoutData(1, -1, new Margins(5,5,5,20)));
		
	}
	
	private NumberField<Integer> createSoftIdentityCutoff()
	{
		NumberField<Integer> softIdentityCutOff = new NumberField<Integer>(new IntegerPropertyEditor());
		softIdentityCutOff.addStyleName("eppic-default-font");
		softIdentityCutOff.setWidth(FIELD_WIDTH);
		softIdentityCutOff.setAllowBlank(false);
		softIdentityCutOff.setFormat(NumberFormat.getDecimalFormat());
		softIdentityCutOff.addValidator(new MinNumberValidator<Integer>(0));
		softIdentityCutOff.addValidator(new MaxNumberValidator<Integer>(100));
		softIdentityCutOff.setToolTipConfig(
				createToolTipConfig(AppPropertiesManager.CONSTANTS.parameters_soft_identity_cutoff_hint()));
		softIdentityCutOff.addKeyDownHandler(new SubmitKeyListener());
		
		return softIdentityCutOff;
	}
	
	private NumberField<Integer> createHardIdentityCutoff()
	{
		NumberField<Integer> hardIdentityCutOff = new NumberField<Integer>(new IntegerPropertyEditor());
		hardIdentityCutOff.setWidth(FIELD_WIDTH);
		hardIdentityCutOff.setAllowBlank(false);
		hardIdentityCutOff.setFormat(NumberFormat.getDecimalFormat());
		hardIdentityCutOff.addValidator(new MinNumberValidator<Integer>(0));
		hardIdentityCutOff.addValidator(new MaxNumberValidator<Integer>(100));
		hardIdentityCutOff.setToolTipConfig(
				createToolTipConfig(AppPropertiesManager.CONSTANTS.parameters_hard_identity_cutoff_hint()));
		hardIdentityCutOff.addKeyDownHandler(new SubmitKeyListener());
		new FieldLabel(hardIdentityCutOff, AppPropertiesManager.CONSTANTS.parameters_hard_identity_cutoff());
		
		return hardIdentityCutOff;
	}
	
	private NumberField<Integer> createMaxNrOfSequences()
	{
		NumberField<Integer> maxNrOfSequences = new NumberField<Integer>(new IntegerPropertyEditor());
		maxNrOfSequences.setWidth(FIELD_WIDTH);
		maxNrOfSequences.setAllowBlank(false);
		maxNrOfSequences.setAllowNegative(false);
		maxNrOfSequences.setName("maxNrOfSequences");
		maxNrOfSequences.setToolTipConfig(
				createToolTipConfig(AppPropertiesManager.CONSTANTS.parameters_max_num_sequences_hint()));
		maxNrOfSequences.addKeyDownHandler(new SubmitKeyListener());
		new FieldLabel(maxNrOfSequences, AppPropertiesManager.CONSTANTS.parameters_max_num_sequences());
		
		return maxNrOfSequences;
	}
	
	private ComboBox<SearchModeComboModel> createSearchModeComboBox()
	{
		SearchModeProperties props = GWT.create(SearchModeProperties.class);
		
		ComboBox<SearchModeComboModel> searchModeCombo = 
				new ComboBox<SearchModeComboModel>(searchModeValues, props.name());
		searchModeCombo.setWidth(FIELD_WIDTH);
		searchModeCombo.setStore(searchModeValues);
		searchModeCombo.setTypeAhead(true);
		searchModeCombo.setTriggerAction(TriggerAction.ALL);
		//searchModeCombo.setDisplayField("searchMode");
		searchModeCombo.setEditable(false);
		searchModeCombo.setToolTipConfig(
				createToolTipConfig(AppPropertiesManager.CONSTANTS.parameters_search_mode_hint()));
		return searchModeCombo;
	}

	/**
	 * Sets default values of input parameters.
	 * @param defaultParameters default values of input parameters
	 */
	public void fillDefaultValues(InputParameters defaultParameters) 
	{
		softIdentityCutOff.setValue(Math.round(100*defaultParameters.getSoftIdentityCutoff()));
		hardIdentityCutOff.setValue(Math.round(100*defaultParameters.getHardIdentityCutoff()));
		
		maxNrOfSequences.setValue(defaultParameters.getMaxNrOfSequences());
		
		SearchModeComboModel defaultSearchModeValueModel = new SearchModeComboModel(
				defaultParameters.getSearchMode());
		searchModeCombo.setValue(defaultSearchModeValueModel);
		
	}

	/**
	 * Retrieves input parameters set by the user.
	 * @return input parameters set by the user
	 */
	public InputParameters getCurrentInputParameters() 
	{
		InputParameters currentInputParameters = new InputParameters();
		currentInputParameters.setMethods(ApplicationContext.getSettings().getDefaultParametersValues().getMethods());
		currentInputParameters.setReducedAlphabet(ApplicationContext.getSettings().getDefaultParametersValues().getReducedAlphabet());
		currentInputParameters.setSoftIdentityCutoff((float)(softIdentityCutOff.getValue()/100.0));
		currentInputParameters.setHardIdentityCutoff((float)(hardIdentityCutOff.getValue()/100.0));
		currentInputParameters.setMaxNrOfSequences(maxNrOfSequences.getValue()
				.intValue());
		
		currentInputParameters.setSearchMode(searchModeCombo
				.getValue().getSearchMode());

		return currentInputParameters;
	}

	/**
	 * Resets the values of the text boxes
	 */
	public void resetValues() {
		softIdentityCutOff.reset();
		hardIdentityCutOff.reset();
		searchModeCombo.reset();
		maxNrOfSequences.reset();
		fillDefaultValues(ApplicationContext.getSettings().getDefaultParametersValues());
		
	}
}
