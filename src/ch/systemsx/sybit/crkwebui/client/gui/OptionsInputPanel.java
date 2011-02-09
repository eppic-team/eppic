package ch.systemsx.sybit.crkwebui.client.gui;

import ch.systemsx.sybit.crkwebui.client.controllers.MainController;
import ch.systemsx.sybit.crkwebui.shared.model.InputParameters;

import com.extjs.gxt.ui.client.data.BaseModel;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.form.Radio;
import com.extjs.gxt.ui.client.widget.form.RadioGroup;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;

public class OptionsInputPanel extends FieldSet 
{
	private FormData formData;
	private ListStore<BaseModel> reducedAlphabetValues;  
	private ComboBox<BaseModel> reducedAlphabetCombo;  
	private TextField<String> selecton;  
	private TextField<String> identityCutOff;  
	private Radio useTCoffeeYes;  
	private Radio useTCoffeeNo;  
	private RadioGroup useTCoffee;  
	private TextField<String> maxNumSequences;  
	private Radio usePisaYes;  
	private Radio usePisaNo;  
	private RadioGroup usePisa;  
	private TextField<String> asaCalcParam;  
	private Radio useNAccessYes;  
	private Radio useNAccessNo;  
	private RadioGroup useNAccess;  
	
	public OptionsInputPanel(InputParameters defaultInputParameters)
	{
	    this.setHeading(MainController.CONSTANTS.input_advanced());  
	    this.setCollapsible(true);
	    this.setBorders(false);
	    
	    formData = new FormData("-20"); 
	    
	    FormLayout layout = new FormLayout();  
	    layout.setLabelWidth(150); 
	    this.setLayout(layout);  
	  
	    FormLayout entropySetLayout = new FormLayout();  
	    entropySetLayout.setLabelWidth(150); 
	    
	    FieldSet entropyFieldSet = new FieldSet();  
	    entropyFieldSet.setHeading(MainController.CONSTANTS.parameters_entropy()); 
	    entropyFieldSet.setCheckboxToggle(true);
	    entropyFieldSet.setLayout(entropySetLayout);
	    
	    reducedAlphabetValues = new ListStore<BaseModel>();  
	  
	    reducedAlphabetCombo = new ComboBox<BaseModel>();  
	    reducedAlphabetCombo.setFieldLabel(MainController.CONSTANTS.parameters_reduced_alphabet());
	    reducedAlphabetCombo.setWidth(150);  
	    reducedAlphabetCombo.setStore(reducedAlphabetValues);  
	    reducedAlphabetCombo.setTypeAhead(true);  
	    reducedAlphabetCombo.setTriggerAction(TriggerAction.ALL);  
	    entropyFieldSet.add(reducedAlphabetCombo, formData); 
	    
	    this.add(entropyFieldSet);
	    
	    
	    
	    FormLayout kaksFieldSetLayout = new FormLayout();  
	    kaksFieldSetLayout.setLabelWidth(150); 
	    
	    FieldSet kaksFieldSet = new FieldSet();  
	    kaksFieldSet.setHeading(MainController.CONSTANTS.parameters_kaks()); 
	    kaksFieldSet.setCheckboxToggle(true);
	    kaksFieldSet.setLayout(kaksFieldSetLayout);
	    
	    selecton = new TextField<String>();  
	    selecton.setFieldLabel(MainController.CONSTANTS.parameters_selecton());  
	    selecton.setAllowBlank(false);  
	    kaksFieldSet.add(selecton, formData);  
	    
	    this.add(kaksFieldSet);
	    
//	    this.add(methodsFieldSet);  
	    
	    
	    
	    FormLayout allignmentsParametersFieldSetLayout = new FormLayout();  
	    allignmentsParametersFieldSetLayout.setLabelWidth(150); 
	    
	    FieldSet allignmentsParametersFieldSet = new FieldSet();  
	    allignmentsParametersFieldSet.setHeading(MainController.CONSTANTS.parameters_allignment()); 
	    allignmentsParametersFieldSet.setLayout(allignmentsParametersFieldSetLayout);
	    
	    identityCutOff = new TextField<String>();  
	    identityCutOff.setFieldLabel(MainController.CONSTANTS.parameters_identity_cutoff());  
	    identityCutOff.setAllowBlank(false);  
	    allignmentsParametersFieldSet.add(identityCutOff, formData); 
	    
	    useTCoffeeYes = new Radio();  
	    useTCoffeeYes.setBoxLabel(MainController.CONSTANTS.yes());  
	    useTCoffeeYes.setValue(true);  
	  
	    useTCoffeeNo = new Radio();  
	    useTCoffeeNo.setBoxLabel(MainController.CONSTANTS.no());  
	  
	    useTCoffee = new RadioGroup();  
	    useTCoffee.setFieldLabel(MainController.CONSTANTS.parameters_use_tcoffee());  
	    useTCoffee.add(useTCoffeeYes);  
	    useTCoffee.add(useTCoffeeNo);  
	    allignmentsParametersFieldSet.add(useTCoffee, formData); 
	    
	    maxNumSequences = new TextField<String>();  
	    maxNumSequences.setFieldLabel(MainController.CONSTANTS.parameters_max_num_sequences());  
	    maxNumSequences.setAllowBlank(false);  
	    allignmentsParametersFieldSet.add(maxNumSequences, formData); 
	    
	    this.add(allignmentsParametersFieldSet);  
	    
	    
	    FormLayout othersParametersFieldSetLayout = new FormLayout();  
	    othersParametersFieldSetLayout.setLabelWidth(150); 
	    
	    FieldSet othersParametersFieldSet = new FieldSet();  
	    othersParametersFieldSet.setHeading(MainController.CONSTANTS.parameters_others()); 
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
	    
	    asaCalcParam = new TextField<String>();  
	    asaCalcParam.setFieldLabel(MainController.CONSTANTS.parameters_asa_calc());  
	    asaCalcParam.setAllowBlank(false);  
	    othersParametersFieldSet.add(asaCalcParam, formData); 
	    
	    useNAccessYes = new Radio();  
	    useNAccessYes.setBoxLabel(MainController.CONSTANTS.yes());  
	    useNAccessYes.setValue(true);  
	  
	    useNAccessNo = new Radio();  
	    useNAccessNo.setBoxLabel(MainController.CONSTANTS.no());  
	  
	    useNAccess = new RadioGroup();  
	    useNAccess.setFieldLabel(MainController.CONSTANTS.parameters_use_naccess());  
	    useNAccess.add(useNAccessYes);  
	    useNAccess.add(useNAccessNo);  
	    othersParametersFieldSet.add(useNAccess, formData); 
	    
	    this.add(othersParametersFieldSet);  
	    
	    fillDefaultValues(defaultInputParameters);
	}
	
	public void fillDefaultValues(InputParameters defaultParameters)
	{
		if(defaultParameters.isUseNACCESS() == true)
		{
			useNAccessYes.setValue(true);
		}
		else
		{
			useNAccessNo.setValue(true);
		}
		
		if(defaultParameters.isUsePISA() == true)
		{
			usePisaYes.setValue(true);
		}
		else
		{
			usePisaNo.setValue(true);
		}
		
		if(defaultParameters.isUseTCoffee() == true)
		{
			useTCoffeeYes.setValue(true);
		}
		else
		{
			useTCoffeeNo.setValue(true);
		}
		
		asaCalcParam.setValue(String.valueOf(defaultParameters.getAsaCalc()));
		identityCutOff.setValue(String.valueOf(defaultParameters.getIdentityCutoff()));
		selecton.setValue(String.valueOf(defaultParameters.getSelecton()));
		maxNumSequences.setValue(String.valueOf(defaultParameters.getMaxNrOfSequences()));
	}
}
