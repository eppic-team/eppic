package ch.systemsx.sybit.crkwebui.client.gui;

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
	
	public OptionsInputPanel()
	{
	    this.setHeading("Advanced");  
	    this.setCollapsible(true);
	    this.setBorders(false);
	    
	    formData = new FormData("-20"); 
	    
	    FormLayout layout = new FormLayout();  
	    layout.setLabelWidth(150); 
	    this.setLayout(layout);  
	  
	    FieldSet methodsFieldSet = new FieldSet();  
	    methodsFieldSet.setHeading("Method");  
	    
	    FormLayout entropySetLayout = new FormLayout();  
	    entropySetLayout.setLabelWidth(150); 
	    
	    FieldSet entropyFieldSet = new FieldSet();  
	    entropyFieldSet.setHeading("Entropy"); 
	    entropyFieldSet.setCheckboxToggle(true);
	    entropyFieldSet.setLayout(entropySetLayout);
	    
	    ListStore<BaseModel> values = new ListStore<BaseModel>();  
	  
	    ComboBox<BaseModel> reducedAlphabetCombo = new ComboBox<BaseModel>();  
	    reducedAlphabetCombo.setFieldLabel("Reduced alphabet");
	    reducedAlphabetCombo.setWidth(150);  
	    reducedAlphabetCombo.setStore(values);  
	    reducedAlphabetCombo.setTypeAhead(true);  
	    reducedAlphabetCombo.setTriggerAction(TriggerAction.ALL);  
	    entropyFieldSet.add(reducedAlphabetCombo, formData); 
	    
	    this.add(entropyFieldSet);
	    
	    
	    
	    FormLayout kaksFieldSetLayout = new FormLayout();  
	    kaksFieldSetLayout.setLabelWidth(150); 
	    
	    FieldSet kaksFieldSet = new FieldSet();  
	    kaksFieldSet.setHeading("KAKS"); 
	    kaksFieldSet.setCheckboxToggle(true);
	    kaksFieldSet.setLayout(kaksFieldSetLayout);
	    
	    TextField<String> selecton = new TextField<String>();  
	    selecton.setFieldLabel("Selecton");  
	    selecton.setAllowBlank(false);  
	    kaksFieldSet.add(selecton, formData);  
	    
	    this.add(kaksFieldSet);
	    
//	    this.add(methodsFieldSet);  
	    
	    
	    
	    FormLayout allignmentsParametersFieldSetLayout = new FormLayout();  
	    allignmentsParametersFieldSetLayout.setLabelWidth(150); 
	    
	    FieldSet allignmentsParametersFieldSet = new FieldSet();  
	    allignmentsParametersFieldSet.setHeading("Allignment"); 
	    allignmentsParametersFieldSet.setLayout(allignmentsParametersFieldSetLayout);
	    
	    TextField<String> identityCutOff = new TextField<String>();  
	    identityCutOff.setFieldLabel("Identity cutoff");  
	    identityCutOff.setAllowBlank(false);  
	    allignmentsParametersFieldSet.add(identityCutOff, formData); 
	    
	    Radio useTCoffeeYes = new Radio();  
	    useTCoffeeYes.setBoxLabel("Yes");  
	    useTCoffeeYes.setValue(true);  
	  
	    Radio useTCoffeeNo = new Radio();  
	    useTCoffeeNo.setBoxLabel("No");  
	  
	    RadioGroup useTCoffee = new RadioGroup();  
	    useTCoffee.setFieldLabel("Use TCoffee");  
	    useTCoffee.add(useTCoffeeYes);  
	    useTCoffee.add(useTCoffeeNo);  
	    allignmentsParametersFieldSet.add(useTCoffee, formData); 
	    
	    TextField<String> maxNumSequences = new TextField<String>();  
	    maxNumSequences.setFieldLabel("Max num sequences");  
	    maxNumSequences.setAllowBlank(false);  
	    allignmentsParametersFieldSet.add(maxNumSequences, formData); 
	    
	    this.add(allignmentsParametersFieldSet);  
	    
	    
	    FormLayout othersParametersFieldSetLayout = new FormLayout();  
	    othersParametersFieldSetLayout.setLabelWidth(150); 
	    
	    FieldSet othersParametersFieldSet = new FieldSet();  
	    othersParametersFieldSet.setHeading("Others"); 
	    othersParametersFieldSet.setLayout(othersParametersFieldSetLayout);
	    
	    Radio usePisaYes = new Radio();  
	    usePisaYes.setBoxLabel("Yes");  
	    usePisaYes.setValue(true);  
	  
	    Radio usePisaNo = new Radio();  
	    usePisaNo.setBoxLabel("No");  
	  
	    RadioGroup usePisa = new RadioGroup();  
	    usePisa.setFieldLabel("Use PISA");  
	    usePisa.add(usePisaYes);  
	    usePisa.add(usePisaNo);  
	    othersParametersFieldSet.add(usePisa, formData);  
	    
	    TextField<String> asaCalcParam = new TextField<String>();  
	    asaCalcParam.setFieldLabel("ASA calc");  
	    asaCalcParam.setAllowBlank(false);  
	    othersParametersFieldSet.add(asaCalcParam, formData); 
	    
	    Radio useNAccessYes = new Radio();  
	    useNAccessYes.setBoxLabel("Yes");  
	    useNAccessYes.setValue(true);  
	  
	    Radio useNAccessNo = new Radio();  
	    useNAccessNo.setBoxLabel("No");  
	  
	    RadioGroup useNAccess = new RadioGroup();  
	    useNAccess.setFieldLabel("Use NACCESS");  
	    useNAccess.add(useNAccessYes);  
	    useNAccess.add(useNAccessNo);  
	    othersParametersFieldSet.add(useNAccess, formData); 
	    
	    this.add(othersParametersFieldSet);  
	  
	    
	}
}
