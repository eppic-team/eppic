package ch.systemsx.sybit.crkwebui.client.gui;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.sybit.crkwebui.client.controllers.MainController;
import ch.systemsx.sybit.crkwebui.client.model.ReducedAlphabetComboModel;
import ch.systemsx.sybit.crkwebui.shared.model.InputParameters;

import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.form.NumberField;
import com.extjs.gxt.ui.client.widget.form.Radio;
import com.extjs.gxt.ui.client.widget.form.RadioGroup;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.google.gwt.i18n.client.NumberFormat;

public class OptionsInputPanel extends FieldSet 
{
	private FormData formData;
	private ListStore<ReducedAlphabetComboModel> reducedAlphabetValues;
	private ComboBox<ReducedAlphabetComboModel> reducedAlphabetCombo;
	private NumberField selecton;
	private NumberField identityCutOff;
	private Radio useTCoffeeYes;
	private Radio useTCoffeeNo;
	private RadioGroup useTCoffee;
	private NumberField maxNrOfSequences;
	private Radio usePisaYes;
	private Radio usePisaNo;
	private RadioGroup usePisa;
	private NumberField asaCalcParam;
	private Radio useNAccessYes;
	private Radio useNAccessNo;
	private RadioGroup useNAccess;
	private FieldSet kaksFieldSet;
	private FieldSet entropyFieldSet;

	public OptionsInputPanel(InputParameters defaultInputParameters,
			List<Integer> reducedAlphabetDefaultList) 
	{
		this.setHeading(MainController.CONSTANTS.input_advanced());
		this.setCollapsible(true);
		this.setBorders(false);

		formData = new FormData("-20");

		FormLayout layout = new FormLayout();
		layout.setLabelWidth(200);
		this.setLayout(layout);

		FormLayout entropySetLayout = new FormLayout();
		entropySetLayout.setLabelWidth(200);

		entropyFieldSet = new FieldSet();
		entropyFieldSet.setHeading(MainController.CONSTANTS
				.parameters_entropy());
		entropyFieldSet.setCheckboxToggle(true);
		entropyFieldSet.setExpanded(true);
		entropyFieldSet.setLayout(entropySetLayout);

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
		entropyFieldSet.add(reducedAlphabetCombo, formData);

		this.add(entropyFieldSet);

		FormLayout kaksFieldSetLayout = new FormLayout();
		kaksFieldSetLayout.setLabelWidth(200);

		kaksFieldSet = new FieldSet();
		kaksFieldSet.setHeading(MainController.CONSTANTS.parameters_kaks());
		kaksFieldSet.setCheckboxToggle(true);
		kaksFieldSet.setExpanded(false);
		kaksFieldSet.setLayout(kaksFieldSetLayout);

		selecton = new NumberField();
		selecton.setFieldLabel(MainController.CONSTANTS.parameters_selecton());
		selecton.setAllowBlank(false);
		selecton.setFormat(NumberFormat.getDecimalFormat());
		kaksFieldSet.add(selecton, formData);

		this.add(kaksFieldSet);

		
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
		allignmentsParametersFieldSet.add(identityCutOff, formData);

		useTCoffeeYes = new Radio();
		useTCoffeeYes.setBoxLabel(MainController.CONSTANTS.yes());
		useTCoffeeYes.setValue(true);

		useTCoffeeNo = new Radio();
		useTCoffeeNo.setBoxLabel(MainController.CONSTANTS.no());

		useTCoffee = new RadioGroup();
		useTCoffee.setFieldLabel(MainController.CONSTANTS
				.parameters_use_tcoffee());
		useTCoffee.add(useTCoffeeYes);
		useTCoffee.add(useTCoffeeNo);
		allignmentsParametersFieldSet.add(useTCoffee, formData);

		maxNrOfSequences = new NumberField();
		maxNrOfSequences.setFieldLabel(MainController.CONSTANTS
				.parameters_max_num_sequences());
		maxNrOfSequences.setAllowBlank(false);
		maxNrOfSequences.setName("maxNrOfSequences");
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
			useTCoffeeYes.setValue(true);
		} else {
			useTCoffeeNo.setValue(true);
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
				if(method.equals("Kaks"))
				{
					kaksFieldSet.setExpanded(true);
				}
				else if(method.equals("Entropy"))
				{
					entropyFieldSet.setExpanded(true);
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

		if (useTCoffeeYes.getValue() == true) {
			currentInputParameters.setUseTCoffee(true);
		} else {
			currentInputParameters.setUseTCoffee(false);
		}
		
		List<String> selectedMethods = new ArrayList<String>();
		if(kaksFieldSet.isExpanded())
		{
			selectedMethods.add("Kaks");
		}
		else if(entropyFieldSet.isExpanded())
		{
			selectedMethods.add("Entropy");
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
