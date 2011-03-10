package ch.systemsx.sybit.crkwebui.client.gui;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.sybit.crkwebui.client.controllers.MainController;
import ch.systemsx.sybit.crkwebui.client.model.ReducedAlphabetComboModel;
import ch.systemsx.sybit.crkwebui.shared.model.InputParameters;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.core.El;
import com.extjs.gxt.ui.client.core.XDOM;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ComponentPlugin;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.form.NumberField;
import com.extjs.gxt.ui.client.widget.form.Radio;
import com.extjs.gxt.ui.client.widget.form.RadioGroup;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.extjs.gxt.ui.client.widget.tips.ToolTipConfig;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.Window;

public class OptionsInputPanel extends FieldSet 
{
	private FormData formData;
	private ListStore<ReducedAlphabetComboModel> reducedAlphabetValues;
	private ComboBox<ReducedAlphabetComboModel> reducedAlphabetCombo;
	private NumberField selecton;
	private NumberField identityCutOff;
	private Radio useTCoffeeFast;
	private Radio useTCoffeeNormal;
	private RadioGroup useTCoffee;
	private NumberField maxNrOfSequences;
	private Radio usePisaYes;
	private Radio usePisaNo;
	private RadioGroup usePisa;
	private NumberField asaCalcParam;
	private Radio useNAccessYes;
	private Radio useNAccessNo;
	private RadioGroup useNAccess;
	private FieldSet[] methodsFieldsets;

	public OptionsInputPanel(InputParameters defaultInputParameters,
							 List<Integer> reducedAlphabetDefaultList,
							 String[] supportedMethods,
							 int windowHeight) 
	{
		this.setHeading(MainController.CONSTANTS.input_advanced());
		this.setCollapsible(true);
		this.setBorders(false);

		formData = new FormData("-20");

		FormLayout layout = new FormLayout();
		layout.setLabelWidth(200);
		this.setLayout(layout);
		
		int height = 400;
		windowHeight = Window.getClientHeight();
		
		if(height > windowHeight * 0.4)
		{
			height = (int) (windowHeight * 0.4);
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

		methodsFieldsets = new FieldSet[supportedMethods.length];
		
		for(int i=0; i<supportedMethods.length; i++)
		{
			FormLayout fieldSetLayout = new FormLayout();
			fieldSetLayout.setLabelWidth(200);

			methodsFieldsets[i] = new FieldSet();
			methodsFieldsets[i].setCheckboxToggle(true);
			methodsFieldsets[i].setExpanded(false);
			methodsFieldsets[i].setLayout(fieldSetLayout);
			
			if(supportedMethods[i].equals("Entropy"))
			{
				methodsFieldsets[i].setHeading(MainController.CONSTANTS
						.parameters_entropy());
				
				reducedAlphabetValues = new ListStore<ReducedAlphabetComboModel>();

				for (Integer value : reducedAlphabetDefaultList) {
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
				methodsFieldsets[i].add(reducedAlphabetCombo, formData);
			}
			else if(supportedMethods[i].equals("Kaks"))
			{
				methodsFieldsets[i].setHeading(MainController.CONSTANTS.parameters_kaks());
				
				selecton = new NumberField();
				selecton.setFieldLabel(MainController.CONSTANTS.parameters_selecton());
				selecton.setAllowBlank(false);
				selecton.setFormat(NumberFormat.getDecimalFormat());
				selecton.setMinValue(0);
				selecton.setMaxValue(1);
				methodsFieldsets[i].add(selecton, formData);
			}
			
			this.add(methodsFieldsets[i]);
		}
		
		FormLayout allignmentsParametersFieldSetLayout = new FormLayout();
		allignmentsParametersFieldSetLayout.setLabelWidth(200);

		FieldSet allignmentsParametersFieldSet = new FieldSet();
		allignmentsParametersFieldSet.setHeading(MainController.CONSTANTS
				.parameters_allignment());
		allignmentsParametersFieldSet
				.setLayout(allignmentsParametersFieldSetLayout);

		identityCutOff = new NumberField();
		identityCutOff.setFieldLabel(MainController.CONSTANTS
				.parameters_identity_cutoff());
		identityCutOff.setAllowBlank(false);
		identityCutOff.setFormat(NumberFormat.getDecimalFormat());
		identityCutOff.setMinValue(0);
		identityCutOff.setMaxValue(1);
		allignmentsParametersFieldSet.add(identityCutOff, formData);

		useTCoffeeFast = new Radio();
		useTCoffeeFast.setBoxLabel(MainController.CONSTANTS.parameters_use_tcoffee_fast());
		useTCoffeeFast.setValue(true);

		useTCoffeeNormal = new Radio();
		useTCoffeeNormal.setBoxLabel(MainController.CONSTANTS.parameters_use_tcoffee_normal());

		useTCoffee = new RadioGroup();
		useTCoffee.setFieldLabel(MainController.CONSTANTS
				.parameters_use_tcoffee());
		useTCoffee.add(useTCoffeeFast);
		useTCoffee.add(useTCoffeeNormal);
//		useTCoffee.addPlugin(plugin);
		useTCoffee.setData("hint", MainController.CONSTANTS.parameters_use_tcoffee_hint());
		allignmentsParametersFieldSet.add(useTCoffee, formData);

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

		this.add(allignmentsParametersFieldSet);

		FormLayout othersParametersFieldSetLayout = new FormLayout();
		othersParametersFieldSetLayout.setLabelWidth(200);

		FieldSet othersParametersFieldSet = new FieldSet();
		othersParametersFieldSet.setHeading(MainController.CONSTANTS
				.parameters_others());
		othersParametersFieldSet.setLayout(othersParametersFieldSetLayout);

		usePisaYes = new Radio();
		usePisaYes.setBoxLabel(MainController.CONSTANTS.yes());
		usePisaYes.setValue(true);

		usePisaNo = new Radio();
		usePisaNo.setBoxLabel(MainController.CONSTANTS.no());

		usePisa = new RadioGroup();
		usePisa.setFieldLabel(MainController.CONSTANTS.parameters_use_pisa());
		usePisa.add(usePisaYes);
		usePisa.add(usePisaNo);
		othersParametersFieldSet.add(usePisa, formData);

		asaCalcParam = new NumberField();
		asaCalcParam.setFieldLabel(MainController.CONSTANTS
				.parameters_asa_calc());
		asaCalcParam.setAllowBlank(false);
		asaCalcParam.setAllowNegative(false);
		asaCalcParam.setPropertyEditorType(Integer.class);
//		asaCalcParam.addPlugin(plugin);
		asaCalcParam.setData("hint", MainController.CONSTANTS.parameters_asa_calc_hint());
		othersParametersFieldSet.add(asaCalcParam, formData);

		useNAccessYes = new Radio();
		useNAccessYes.setBoxLabel(MainController.CONSTANTS.yes());
		useNAccessYes.setValue(true);

		useNAccessNo = new Radio();
		useNAccessNo.setBoxLabel(MainController.CONSTANTS.no());

		useNAccess = new RadioGroup();
		useNAccess.setFieldLabel(MainController.CONSTANTS
				.parameters_use_naccess());
		useNAccess.add(useNAccessYes);
		useNAccess.add(useNAccessNo);
//		useNAccess.addPlugin(plugin);
		useNAccess.setData("hint", MainController.CONSTANTS.parameters_use_naccess_hint());
		othersParametersFieldSet.add(useNAccess, formData);

		this.add(othersParametersFieldSet);

		fillDefaultValues(defaultInputParameters);
	}

	public void fillDefaultValues(InputParameters defaultParameters) {
		if (defaultParameters.isUseNACCESS() == true) {
			useNAccessYes.setValue(true);
		} else {
			useNAccessNo.setValue(true);
		}

		if (defaultParameters.isUsePISA() == true) {
			usePisaYes.setValue(true);
		} else {
			usePisaNo.setValue(true);
		}

		if (defaultParameters.isUseTCoffee() == true) {
			useTCoffeeFast.setValue(true);
		} else {
			useTCoffeeNormal.setValue(true);
		}

		asaCalcParam.setValue(defaultParameters.getAsaCalc());
		identityCutOff.setValue(defaultParameters.getIdentityCutoff());
		selecton.setValue(defaultParameters.getSelecton());
		maxNrOfSequences.setValue(defaultParameters.getMaxNrOfSequences());

		ReducedAlphabetComboModel defaultValueModel = new ReducedAlphabetComboModel(
				defaultParameters.getReducedAlphabet());
		reducedAlphabetCombo.setValue(defaultValueModel);
		
		String[] defaultMethods = defaultParameters.getMethods();
		
		if(defaultMethods != null)
		{
			for(String method : defaultMethods)
			{
				for(FieldSet fieldSet : methodsFieldsets)
				{
					if(fieldSet.getHeading().equals(method))
					{
						fieldSet.setExpanded(true);
					}
				}
			}
		}
	}

	public InputParameters getCurrentInputParameters() {
		InputParameters currentInputParameters = new InputParameters();
		currentInputParameters.setAsaCalc(asaCalcParam.getValue().intValue());
		currentInputParameters.setIdentityCutoff(identityCutOff.getValue()
				.floatValue());
		currentInputParameters.setMaxNrOfSequences(maxNrOfSequences.getValue()
				.intValue());
		currentInputParameters.setReducedAlphabet(reducedAlphabetCombo
				.getValue().getReducedAlphabet());
		currentInputParameters.setSelecton(selecton.getValue().floatValue());

		if (useNAccessYes.getValue() == true) {
			currentInputParameters.setUseNACCESS(true);
		} else {
			currentInputParameters.setUseNACCESS(false);
		}

		if (usePisaYes.getValue() == true) {
			currentInputParameters.setUsePISA(true);
		} else {
			currentInputParameters.setUsePISA(false);
		}

		if (useTCoffeeFast.getValue() == true) {
			currentInputParameters.setUseTCoffee(true);
		} else {
			currentInputParameters.setUseTCoffee(false);
		}
		
		List<String> selectedMethods = new ArrayList<String>();
		
		for(FieldSet fieldSet : methodsFieldsets)
		{
			if(fieldSet.isExpanded())
			{
				selectedMethods.add(fieldSet.getHeading());
			}
		}
		
		String[] selectedMethodsArray = new String[selectedMethods.size()];
		for(int i=0; i<selectedMethods.size(); i++)
		{
			selectedMethodsArray[i] = selectedMethods.get(i);
		}
			
		currentInputParameters.setMethods(selectedMethodsArray);

		return currentInputParameters;
	}
}
