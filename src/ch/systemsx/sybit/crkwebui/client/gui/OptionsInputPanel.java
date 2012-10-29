package ch.systemsx.sybit.crkwebui.client.gui;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.sybit.crkwebui.client.controllers.AppPropertiesManager;
import ch.systemsx.sybit.crkwebui.client.controllers.ApplicationContext;
import ch.systemsx.sybit.crkwebui.client.listeners.SubmitKeyListener;
import ch.systemsx.sybit.crkwebui.client.model.ReducedAlphabetComboModel;
import ch.systemsx.sybit.crkwebui.client.model.SearchModeComboModel;
import ch.systemsx.sybit.crkwebui.shared.model.ApplicationSettings;
import ch.systemsx.sybit.crkwebui.shared.model.InputParameters;
import ch.systemsx.sybit.crkwebui.shared.model.SupportedMethod;

import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldSetEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ComponentHelper;
import com.extjs.gxt.ui.client.widget.ComponentPlugin;
import com.extjs.gxt.ui.client.widget.WidgetComponent;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.form.NumberField;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window;

/**
 * Panel used to display advanced parameters which can be selected by the user.
 * @author srebniak_a
 *
 */
public class OptionsInputPanel extends FieldSet 
{
	private FormData formData;
	private ListStore<ReducedAlphabetComboModel> reducedAlphabetValues;
	private ComboBox<ReducedAlphabetComboModel> reducedAlphabetCombo;
	private NumberField softIdentityCutOff;
	private NumberField hardIdentityCutOff;
	private ListStore<SearchModeComboModel> searchModeValues;
	private ComboBox<SearchModeComboModel> searchModeCombo;
	private NumberField maxNrOfSequences;
	private FieldSet[] methodsFieldsets;
	
	private int LABEL_WIDTH = 200;

	public OptionsInputPanel(ApplicationSettings applicationSettings) 
	{
		InputParameters defaultInputParameters = applicationSettings.getDefaultParametersValues();
		List<Integer> reducedAlphabetDefaultList = applicationSettings.getReducedAlphabetList();
		List<String> searchModeDefaultList = applicationSettings.getSearchModeList();
		List<SupportedMethod> supportedMethods = applicationSettings.getScoresTypes();
		
		this.setHeading(AppPropertiesManager.CONSTANTS.input_advanced());
		this.setCollapsible(true);
		this.setBorders(false);

		formData = new FormData("-20");

		final FormLayout layout = new FormLayout();
		
		int defaultFieldLengthForHelp = layout.getDefaultWidth() - layout.getLabelWidth() + LABEL_WIDTH;
		
		layout.setLabelWidth(LABEL_WIDTH);
		this.setLayout(layout);
		
		int height = 400;
		
		if(height > ApplicationContext.getAdjustedWindowData().getWindowHeight() * 0.4)
		{
			height = (int) (ApplicationContext.getAdjustedWindowData().getWindowHeight() * 0.4);
		}
		
		ComponentPlugin helpIconPlugin = createHelpIconPlugin(defaultFieldLengthForHelp);

		methodsFieldsets = new FieldSet[supportedMethods.size()];
		
		for(int i=0; i<supportedMethods.size(); i++)
		{
			methodsFieldsets[i] = createMethodFieldSet(supportedMethods.get(i), helpIconPlugin, reducedAlphabetDefaultList);
			
			if(supportedMethods.get(i).isHasFieldSet())
			{
				this.add(methodsFieldsets[i]);
			}
		}
		

		FieldSet alignmentsParametersFieldSet = createAlignmentsParametersFieldSet(helpIconPlugin, searchModeDefaultList);
		this.add(alignmentsParametersFieldSet);

		fillDefaultValues(defaultInputParameters);
	}
	
	/**
	 * Creates help icon plugin.
	 * @param defaultFieldLengthForHelp position for help icon
	 * @return help icon plugin
	 */
	private ComponentPlugin createHelpIconPlugin(final int defaultFieldLengthForHelp)
	{
		ComponentPlugin plugin = new ComponentPlugin()
		{
			public void init(Component component) 
			{
				component.addListener(Events.Render, new Listener<ComponentEvent>() 
				{
					public void handleEvent(ComponentEvent be) 
					{
						HelpIconPanel helpIconPanel = new HelpIconPanel((String)be.getComponent().getData("hint"));
						final WidgetComponent helpImage = helpIconPanel.getImageComponent();
						
						if(helpImage != null)
						{
							Element parent = be.getComponent().el().findParent(".x-form-element", 3).dom;
						    helpImage.render(parent);
						    helpImage.el().setVisibility(true);
						    helpImage.el().makePositionable(true);
						        
						    if (!helpImage.isAttached()) 
						    {
						    	ComponentHelper.doAttach(helpImage);
						    }
						
						    Scheduler.get().scheduleDeferred(new ScheduledCommand() {
								@Override
								public void execute() {
									helpImage.setPosition(defaultFieldLengthForHelp + 80, 3);
								    helpImage.el().setVisible(true);
								}
						    });
						}
					    
					}
				});
			}
		};
		
		return plugin;
	}
	
	/**
	 * Creates alignments parameters fieldset.
	 * @param helpIconPlugin help icon plugin
	 * @param searchModeDefaultList search mode default list
	 * @return alignments parameters fieldset
	 */
	private FieldSet createAlignmentsParametersFieldSet(ComponentPlugin helpIconPlugin,
													List<String> searchModeDefaultList)
	{
		FormLayout alignmentsParametersFieldSetLayout = new FormLayout();
		alignmentsParametersFieldSetLayout.setLabelWidth(LABEL_WIDTH);

		FieldSet alignmentsParametersFieldSet = new FieldSet();
		alignmentsParametersFieldSet.setHeading(AppPropertiesManager.CONSTANTS
				.parameters_allignment());
		alignmentsParametersFieldSet
				.setLayout(alignmentsParametersFieldSetLayout);

		softIdentityCutOff = new NumberField();
		softIdentityCutOff.setFieldLabel(AppPropertiesManager.CONSTANTS
				.parameters_soft_identity_cutoff());
		softIdentityCutOff.setAllowBlank(false);
		softIdentityCutOff.setFormat(NumberFormat.getDecimalFormat());
		softIdentityCutOff.setMinValue(0);
		softIdentityCutOff.setMaxValue(1);
		softIdentityCutOff.addPlugin(helpIconPlugin);
		softIdentityCutOff.setData("hint", AppPropertiesManager.CONSTANTS.parameters_soft_identity_cutoff_hint());
		softIdentityCutOff.addKeyListener(new SubmitKeyListener());
		alignmentsParametersFieldSet.add(softIdentityCutOff, formData);
		
		hardIdentityCutOff = new NumberField();
		hardIdentityCutOff.setFieldLabel(AppPropertiesManager.CONSTANTS
				.parameters_hard_identity_cutoff());
		hardIdentityCutOff.setAllowBlank(false);
		hardIdentityCutOff.setFormat(NumberFormat.getDecimalFormat());
		hardIdentityCutOff.setMinValue(0);
		hardIdentityCutOff.setMaxValue(1);
		hardIdentityCutOff.addPlugin(helpIconPlugin);
		hardIdentityCutOff.setData("hint", AppPropertiesManager.CONSTANTS.parameters_hard_identity_cutoff_hint());
		hardIdentityCutOff.addKeyListener(new SubmitKeyListener());
		alignmentsParametersFieldSet.add(hardIdentityCutOff, formData);

		maxNrOfSequences = new NumberField();
		maxNrOfSequences.setFieldLabel(AppPropertiesManager.CONSTANTS
				.parameters_max_num_sequences());
		maxNrOfSequences.setAllowBlank(false);
		maxNrOfSequences.setAllowNegative(false);
		maxNrOfSequences.setPropertyEditorType(Integer.class);
		maxNrOfSequences.setName("maxNrOfSequences");
		maxNrOfSequences.addPlugin(helpIconPlugin);
		maxNrOfSequences.setData("hint", AppPropertiesManager.CONSTANTS.parameters_max_num_sequences_hint());
		maxNrOfSequences.addKeyListener(new SubmitKeyListener());
		alignmentsParametersFieldSet.add(maxNrOfSequences, formData);
		
		searchModeValues = new ListStore<SearchModeComboModel>();

		for (String value : searchModeDefaultList)
		{
			SearchModeComboModel model = new SearchModeComboModel(
					value);
			searchModeValues.add(model);
		}

		searchModeCombo = new ComboBox<SearchModeComboModel>();
		searchModeCombo.setFieldLabel(AppPropertiesManager.CONSTANTS
				.parameters_search_mode());
		searchModeCombo.setWidth(150);
		searchModeCombo.setStore(searchModeValues);
		searchModeCombo.setTypeAhead(true);
		searchModeCombo.setTriggerAction(TriggerAction.ALL);
		searchModeCombo.setDisplayField("searchMode");
		searchModeCombo.setEditable(false);
		searchModeCombo.addPlugin(helpIconPlugin);
		searchModeCombo.setData("hint", AppPropertiesManager.CONSTANTS.parameters_search_mode_hint());
		alignmentsParametersFieldSet.add(searchModeCombo, formData);
		
		return alignmentsParametersFieldSet;
	}
	
	/**
	 * Creates method fieldset.
	 * @param supportedMethod supported method for which fieldset is to be generated
	 * @param helpIconPlugin help icon plugin
	 * @param reducedAlphabetDefaultList reduced alphabet values list
	 * @return method fieldset
	 */
	private FieldSet createMethodFieldSet(SupportedMethod supportedMethod,
										  ComponentPlugin helpIconPlugin,
										  List<Integer> reducedAlphabetDefaultList)
	{
		FormLayout fieldSetLayout = new FormLayout();
		fieldSetLayout.setLabelWidth(LABEL_WIDTH);

		FieldSet methodsFieldset = new FieldSet();
		methodsFieldset.setCheckboxToggle(true);
		methodsFieldset.setExpanded(false);
		methodsFieldset.setLayout(fieldSetLayout);
		
		if(supportedMethod.getName().equals("Entropy"))
		{
			methodsFieldset.setHeading(AppPropertiesManager.CONSTANTS
					.parameters_entropy());
			
			reducedAlphabetValues = new ListStore<ReducedAlphabetComboModel>();

			for (Integer value : reducedAlphabetDefaultList)
			{
				ReducedAlphabetComboModel model = new ReducedAlphabetComboModel(
						value);
				reducedAlphabetValues.add(model);
			}

			reducedAlphabetCombo = new ComboBox<ReducedAlphabetComboModel>();
			reducedAlphabetCombo.setFieldLabel(AppPropertiesManager.CONSTANTS
					.parameters_reduced_alphabet());
			reducedAlphabetCombo.setWidth(150);
			reducedAlphabetCombo.setStore(reducedAlphabetValues);
			reducedAlphabetCombo.setTypeAhead(true);
			reducedAlphabetCombo.setTriggerAction(TriggerAction.ALL);
			reducedAlphabetCombo.setDisplayField("reducedAlphabet");
			reducedAlphabetCombo.setEditable(false);
			reducedAlphabetCombo.addPlugin(helpIconPlugin);
			reducedAlphabetCombo.setData("hint", AppPropertiesManager.CONSTANTS.parameters_reduced_alphabet_hint());
			methodsFieldset.add(reducedAlphabetCombo, formData);
			
			methodsFieldset.addListener(Events.Expand, new Listener<FieldSetEvent>() 
			{
				public void handleEvent(FieldSetEvent be) 
				{
					for(FieldSet fieldSet : methodsFieldsets)
					{
						
						if((fieldSet.getHeading() != null) &&
							(fieldSet.getHeading().equals("Geometry")))
						{
							fieldSet.setExpanded(true);
						}
					}
				}
			});
		}
		else if(supportedMethod.getName().equals("Geometry"))
		{
			methodsFieldset.setHeading(AppPropertiesManager.CONSTANTS
					.parameters_geometry());
			
			methodsFieldset.addListener(Events.Collapse, new Listener<FieldSetEvent>() 
			{
				public void handleEvent(FieldSetEvent be) 
				{
					for(FieldSet fieldSet : methodsFieldsets)
					{
						if((fieldSet.getHeading() != null) &&
						   (fieldSet.getHeading().equals("Entropy")))
						{
							fieldSet.setExpanded(false);
						}
					}
				}
			});
		}
		
		return methodsFieldset;
	}

	/**
	 * Sets default values of input parameters.
	 * @param defaultParameters default values of input parameters
	 */
	public void fillDefaultValues(InputParameters defaultParameters) 
	{
		softIdentityCutOff.setValue(defaultParameters.getSoftIdentityCutoff());
		hardIdentityCutOff.setValue(defaultParameters.getHardIdentityCutoff());
		
		maxNrOfSequences.setValue(defaultParameters.getMaxNrOfSequences());

		ReducedAlphabetComboModel defaultValueModel = new ReducedAlphabetComboModel(
				defaultParameters.getReducedAlphabet());
		reducedAlphabetCombo.setValue(defaultValueModel);
		
		SearchModeComboModel defaultSearchModeValueModel = new SearchModeComboModel(
				defaultParameters.getSearchMode());
		searchModeCombo.setValue(defaultSearchModeValueModel);
		
		List<String> defaultMethods = defaultParameters.getMethods();
		
		if(defaultMethods != null)
		{
			for(String method : defaultMethods)
			{
				for(FieldSet fieldSet : methodsFieldsets)
				{
					if((fieldSet.getHeading() != null) &&
						(fieldSet.getHeading().equals(method)))
					{
						fieldSet.setExpanded(true);
					}
				}
			}
		}
	}

	/**
	 * Retrieves input parameters set by the user.
	 * @return input parameters set by the user
	 */
	public InputParameters getCurrentInputParameters() 
	{
		InputParameters currentInputParameters = new InputParameters();
		currentInputParameters.setSoftIdentityCutoff(softIdentityCutOff.getValue()
				.floatValue());
		currentInputParameters.setHardIdentityCutoff(hardIdentityCutOff.getValue()
				.floatValue());
		currentInputParameters.setMaxNrOfSequences(maxNrOfSequences.getValue()
				.intValue());
		currentInputParameters.setReducedAlphabet(reducedAlphabetCombo
				.getValue().getReducedAlphabet());
		currentInputParameters.setSearchMode(searchModeCombo
				.getValue().getSearchMode());
		
		List<String> selectedMethods = new ArrayList<String>();
		
		for(FieldSet fieldSet : methodsFieldsets)
		{
			if(fieldSet.isExpanded())
			{
				selectedMethods.add(fieldSet.getHeading());
			}
		}
			
		currentInputParameters.setMethods(selectedMethods);

		return currentInputParameters;
	}
	
	/**
	 * Checks whether there is any method selected by the user.
	 * @return information whether any method is selected
	 */
	public boolean checkIfAnyMethodSelected()
	{
		boolean isAnyMethodSelected = false;
		
		for(FieldSet fieldSet : methodsFieldsets)
		{
			if(fieldSet.isExpanded())
			{
				isAnyMethodSelected = true;
			}
		}
		
		return isAnyMethodSelected;
	}
}
