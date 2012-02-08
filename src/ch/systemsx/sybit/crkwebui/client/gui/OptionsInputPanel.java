package ch.systemsx.sybit.crkwebui.client.gui;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.sybit.crkwebui.client.controllers.MainController;
import ch.systemsx.sybit.crkwebui.client.model.ReducedAlphabetComboModel;
import ch.systemsx.sybit.crkwebui.client.model.SearchModeComboModel;
import ch.systemsx.sybit.crkwebui.shared.model.InputParameters;
import ch.systemsx.sybit.crkwebui.shared.model.SupportedMethod;

import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldSetEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.form.NumberField;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.Window;

/**
 * This panel is used to display advanced parameters which can be selected by the user
 * @author srebniak_a
 *
 */
public class OptionsInputPanel extends FieldSet 
{
	private FormData formData;
	private ListStore<ReducedAlphabetComboModel> reducedAlphabetValues;
	private ComboBox<ReducedAlphabetComboModel> reducedAlphabetCombo;
	private NumberField selecton;
	private NumberField softIdentityCutOff;
	private NumberField hardIdentityCutOff;
	private ListStore<SearchModeComboModel> searchModeValues;
	private ComboBox<SearchModeComboModel> searchModeCombo;
	private NumberField maxNrOfSequences;
	private NumberField asaCalcParam;
	private FieldSet[] methodsFieldsets;

	public OptionsInputPanel(InputParameters defaultInputParameters,
							 List<Integer> reducedAlphabetDefaultList,
							 List<String> searchModeDefaultList,
							 List<SupportedMethod> supportedMethods) 
	{
		this.setHeading(MainController.CONSTANTS.input_advanced());
		this.setCollapsible(true);
		this.setBorders(false);

		formData = new FormData("-20");

		FormLayout layout = new FormLayout();
		layout.setLabelWidth(200);
		this.setLayout(layout);
		
		int height = 400;
		
		if(height > Window.getClientHeight() * 0.4)
		{
			height = (int) (Window.getClientHeight() * 0.4);
		}
		
//		ComponentPlugin plugin = new ComponentPlugin()
//		{
//			public void init(Component component) 
//			{
//				component.addListener(Events.Render, new Listener<ComponentEvent>() 
//				{
//					public void handleEvent(ComponentEvent be) {
//						El elem = be.getComponent().el().findParent(".x-form-element", 3);
//						elem.appendChild(XDOM.create("<div style='color: #615f5f;padding: 1 0 2 0px;'>" + be.getComponent().getData("hint") + "</div>"));
//					}
//				});
//			}
//		};

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
				reducedAlphabetCombo.setData("hint", MainController.CONSTANTS.parameters_reduced_alphabet_hint());
				methodsFieldsets[i].add(reducedAlphabetCombo, formData);
				
				methodsFieldsets[i].addListener(Events.Collapse, new Listener<FieldSetEvent>() 
				{
					public void handleEvent(FieldSetEvent be) 
					{
						for(FieldSet fieldSet : methodsFieldsets)
						{
							if((fieldSet.getHeading() != null) &&
								(fieldSet.getHeading().equals("KaKs")))
							{
								fieldSet.setExpanded(false);
							}
						}
					}
				});
					
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
			else if(supportedMethods.get(i).getName().equals("Kaks"))
			{
				methodsFieldsets[i].setHeading(MainController.CONSTANTS.parameters_kaks());
				
				selecton = new NumberField();
				selecton.setFieldLabel(MainController.CONSTANTS.parameters_selecton());
				selecton.setAllowBlank(false);
				selecton.setFormat(NumberFormat.getDecimalFormat());
				selecton.setMinValue(0);
				selecton.setMaxValue(1);
				methodsFieldsets[i].add(selecton, formData);
				
				methodsFieldsets[i].addListener(Events.Expand, new Listener<FieldSetEvent>() 
				{
					public void handleEvent(FieldSetEvent be) 
					{
						for(FieldSet fieldSet : methodsFieldsets)
						{
							if((fieldSet.getHeading() != null) &&
							   (fieldSet.getHeading().equals("Entropy")))
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
		softIdentityCutOff.setData("hint", MainController.CONSTANTS.parameters_soft_identity_cutoff_hint());
		allignmentsParametersFieldSet.add(softIdentityCutOff, formData);
		
		hardIdentityCutOff = new NumberField();
		hardIdentityCutOff.setFieldLabel(MainController.CONSTANTS
				.parameters_hard_identity_cutoff());
		hardIdentityCutOff.setAllowBlank(false);
		hardIdentityCutOff.setFormat(NumberFormat.getDecimalFormat());
		hardIdentityCutOff.setMinValue(0);
		hardIdentityCutOff.setMaxValue(1);
		hardIdentityCutOff.setData("hint", MainController.CONSTANTS.parameters_hard_identity_cutoff_hint());
		allignmentsParametersFieldSet.add(hardIdentityCutOff, formData);

		maxNrOfSequences = new NumberField();
		maxNrOfSequences.setFieldLabel(MainController.CONSTANTS
				.parameters_max_num_sequences());
		maxNrOfSequences.setAllowBlank(false);
		maxNrOfSequences.setAllowNegative(false);
		maxNrOfSequences.setPropertyEditorType(Integer.class);
		maxNrOfSequences.setName("maxNrOfSequences");
//		maxNrOfSequences.addPlugin(plugin);
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
		searchModeCombo.setData("hint", MainController.CONSTANTS.parameters_search_mode_hint());
		allignmentsParametersFieldSet.add(searchModeCombo, formData);

		this.add(allignmentsParametersFieldSet);

		FormLayout othersParametersFieldSetLayout = new FormLayout();
		othersParametersFieldSetLayout.setLabelWidth(200);

		FieldSet othersParametersFieldSet = new FieldSet();
		othersParametersFieldSet.setHeading(MainController.CONSTANTS
				.parameters_others());
		othersParametersFieldSet.setLayout(othersParametersFieldSetLayout);

		asaCalcParam = new NumberField();
		asaCalcParam.setFieldLabel(MainController.CONSTANTS
				.parameters_asa_calc());
		asaCalcParam.setAllowBlank(false);
		asaCalcParam.setAllowNegative(false);
		asaCalcParam.setPropertyEditorType(Integer.class);
//		asaCalcParam.addPlugin(plugin);
		asaCalcParam.setData("hint", MainController.CONSTANTS.parameters_asa_calc_hint());
		othersParametersFieldSet.add(asaCalcParam, formData);

		this.add(othersParametersFieldSet);

		fillDefaultValues(defaultInputParameters);
	}

	public void fillDefaultValues(InputParameters defaultParameters) 
	{
		asaCalcParam.setValue(defaultParameters.getAsaCalc());
		softIdentityCutOff.setValue(defaultParameters.getSoftIdentityCutoff());
		hardIdentityCutOff.setValue(defaultParameters.getHardIdentityCutoff());
		
		if(selecton != null)
		{
			selecton.setValue(defaultParameters.getSelecton());
		}
		
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

	public InputParameters getCurrentInputParameters() 
	{
		InputParameters currentInputParameters = new InputParameters();
		currentInputParameters.setAsaCalc(asaCalcParam.getValue().intValue());
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
		
		if(selecton != null)
		{
			currentInputParameters.setSelecton(selecton.getValue().floatValue());
		}

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
