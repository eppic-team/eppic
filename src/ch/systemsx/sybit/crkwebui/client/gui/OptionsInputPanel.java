package ch.systemsx.sybit.crkwebui.client.gui;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.sybit.crkwebui.client.controllers.MainController;
import ch.systemsx.sybit.crkwebui.client.model.ReducedAlphabetComboModel;
import ch.systemsx.sybit.crkwebui.client.model.SearchModeComboModel;
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

	public OptionsInputPanel(final MainController mainController) 
	{
		InputParameters defaultInputParameters = mainController.getSettings().getDefaultParametersValues();
		List<Integer> reducedAlphabetDefaultList= mainController.getSettings().getReducedAlphabetList();
		List<String> searchModeDefaultList = mainController.getSettings().getSearchModeList();
		List<SupportedMethod> supportedMethods = mainController.getSettings().getScoresTypes();
		
		this.setHeading(MainController.CONSTANTS.input_advanced());
		this.setCollapsible(true);
		this.setBorders(false);

		formData = new FormData("-20");

		final FormLayout layout = new FormLayout();
		
		final int defaultFieldLengthForHelp = layout.getDefaultWidth() - layout.getLabelWidth() + LABEL_WIDTH;
		
		layout.setLabelWidth(LABEL_WIDTH);
		this.setLayout(layout);
		
		int height = 400;
		
		if(height > Window.getClientHeight() * 0.4)
		{
			height = (int) (Window.getClientHeight() * 0.4);
		}
		
		ComponentPlugin plugin = new ComponentPlugin()
		{
			public void init(Component component) 
			{
				component.addListener(Events.Render, new Listener<ComponentEvent>() 
				{
					public void handleEvent(ComponentEvent be) 
					{
						HelpPanel helpPanel = new HelpPanel(mainController, (String)be.getComponent().getData("hint"));
						final WidgetComponent helpImage = helpPanel.getImageComponent();
						
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

		methodsFieldsets = new FieldSet[supportedMethods.size()];
		
		for(int i=0; i<supportedMethods.size(); i++)
		{
			FormLayout fieldSetLayout = new FormLayout();
			fieldSetLayout.setLabelWidth(200);

			methodsFieldsets[i] = new FieldSet();
			methodsFieldsets[i].setCheckboxToggle(true);
			methodsFieldsets[i].setExpanded(false);
			methodsFieldsets[i].setLayout(fieldSetLayout);
			
			if(supportedMethods.get(i).getName().equals("Entropy"))
			{
				methodsFieldsets[i].setHeading(MainController.CONSTANTS
						.parameters_entropy());
				
				reducedAlphabetValues = new ListStore<ReducedAlphabetComboModel>();

				for (Integer value : reducedAlphabetDefaultList)
				{
					ReducedAlphabetComboModel model = new ReducedAlphabetComboModel(
							value);
					reducedAlphabetValues.add(model);
				}

				reducedAlphabetCombo = new ComboBox<ReducedAlphabetComboModel>();
				reducedAlphabetCombo.setFieldLabel(MainController.CONSTANTS
						.parameters_reduced_alphabet());
				reducedAlphabetCombo.setWidth(150);
				reducedAlphabetCombo.setStore(reducedAlphabetValues);
				reducedAlphabetCombo.setTypeAhead(true);
				reducedAlphabetCombo.setTriggerAction(TriggerAction.ALL);
				reducedAlphabetCombo.setDisplayField("reducedAlphabet");
				reducedAlphabetCombo.setEditable(false);
				reducedAlphabetCombo.addPlugin(plugin);
				reducedAlphabetCombo.setData("hint", MainController.CONSTANTS.parameters_reduced_alphabet_hint());
				methodsFieldsets[i].add(reducedAlphabetCombo, formData);
				
				methodsFieldsets[i].addListener(Events.Expand, new Listener<FieldSetEvent>() 
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
			else if(supportedMethods.get(i).getName().equals("Geometry"))
			{
				methodsFieldsets[i].setHeading(MainController.CONSTANTS
						.parameters_geometry());
				
				methodsFieldsets[i].addListener(Events.Collapse, new Listener<FieldSetEvent>() 
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
			
			if(supportedMethods.get(i).isHasFieldSet())
			{
				this.add(methodsFieldsets[i]);
			}
		}
		
		FormLayout allignmentsParametersFieldSetLayout = new FormLayout();
		allignmentsParametersFieldSetLayout.setLabelWidth(200);

		FieldSet allignmentsParametersFieldSet = new FieldSet();
		allignmentsParametersFieldSet.setHeading(MainController.CONSTANTS
				.parameters_allignment());
		allignmentsParametersFieldSet
				.setLayout(allignmentsParametersFieldSetLayout);

		softIdentityCutOff = new NumberField();
		softIdentityCutOff.setFieldLabel(MainController.CONSTANTS
				.parameters_soft_identity_cutoff());
		softIdentityCutOff.setAllowBlank(false);
		softIdentityCutOff.setFormat(NumberFormat.getDecimalFormat());
		softIdentityCutOff.setMinValue(0);
		softIdentityCutOff.setMaxValue(1);
		softIdentityCutOff.addPlugin(plugin);
		softIdentityCutOff.setData("hint", MainController.CONSTANTS.parameters_soft_identity_cutoff_hint());
		allignmentsParametersFieldSet.add(softIdentityCutOff, formData);
		
		hardIdentityCutOff = new NumberField();
		hardIdentityCutOff.setFieldLabel(MainController.CONSTANTS
				.parameters_hard_identity_cutoff());
		hardIdentityCutOff.setAllowBlank(false);
		hardIdentityCutOff.setFormat(NumberFormat.getDecimalFormat());
		hardIdentityCutOff.setMinValue(0);
		hardIdentityCutOff.setMaxValue(1);
		hardIdentityCutOff.addPlugin(plugin);
		hardIdentityCutOff.setData("hint", MainController.CONSTANTS.parameters_hard_identity_cutoff_hint());
		allignmentsParametersFieldSet.add(hardIdentityCutOff, formData);

		maxNrOfSequences = new NumberField();
		maxNrOfSequences.setFieldLabel(MainController.CONSTANTS
				.parameters_max_num_sequences());
		maxNrOfSequences.setAllowBlank(false);
		maxNrOfSequences.setAllowNegative(false);
		maxNrOfSequences.setPropertyEditorType(Integer.class);
		maxNrOfSequences.setName("maxNrOfSequences");
		maxNrOfSequences.addPlugin(plugin);
		maxNrOfSequences.setData("hint", MainController.CONSTANTS.parameters_max_num_sequences_hint());
		allignmentsParametersFieldSet.add(maxNrOfSequences, formData);
		
		searchModeValues = new ListStore<SearchModeComboModel>();

		for (String value : searchModeDefaultList)
		{
			SearchModeComboModel model = new SearchModeComboModel(
					value);
			searchModeValues.add(model);
		}

		searchModeCombo = new ComboBox<SearchModeComboModel>();
		searchModeCombo.setFieldLabel(MainController.CONSTANTS
				.parameters_search_mode());
		searchModeCombo.setWidth(150);
		searchModeCombo.setStore(searchModeValues);
		searchModeCombo.setTypeAhead(true);
		searchModeCombo.setTriggerAction(TriggerAction.ALL);
		searchModeCombo.setDisplayField("searchMode");
		searchModeCombo.setEditable(false);
		searchModeCombo.addPlugin(plugin);
		searchModeCombo.setData("hint", MainController.CONSTANTS.parameters_search_mode_hint());
		allignmentsParametersFieldSet.add(searchModeCombo, formData);

		this.add(allignmentsParametersFieldSet);

		fillDefaultValues(defaultInputParameters);
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
