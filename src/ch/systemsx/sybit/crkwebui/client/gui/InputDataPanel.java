package ch.systemsx.sybit.crkwebui.client.gui;

import ch.systemsx.sybit.crkwebui.client.controllers.MainController;
import ch.systemsx.sybit.crkwebui.client.gui.validators.EmailFieldValidator;
import ch.systemsx.sybit.crkwebui.shared.model.RunJobData;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FormEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.form.FileUploadField;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout.VBoxLayoutAlign;
import com.google.gwt.core.client.GWT;

public class InputDataPanel extends DisplayPanel
{
	private RecaptchaPanel recaptchaPanel;
	
	private FormPanel formPanel;

	public InputDataPanel(MainController mainController) 
	{
		super(mainController);
		init();
	}

	public void init() 
	{
		VBoxLayout vBoxLayout = new VBoxLayout();
		vBoxLayout.setVBoxLayoutAlign(VBoxLayoutAlign.CENTER);
		this.setLayout(vBoxLayout);
		this.setBorders(false);
//		this.setBodyBorder(false);
//		this.getHeader().setVisible(false);
		
		formPanel = new FormPanel();

		// this.setHeading("File Upload");
		// this.setFrame(true);
		formPanel.getHeader().setVisible(false);
		formPanel.setBorders(true);
		formPanel.setBodyBorder(false);
		formPanel.setAction(GWT.getModuleBaseURL() + "fileUpload");
		formPanel.setEncoding(Encoding.MULTIPART);
		formPanel.setMethod(Method.POST);
		formPanel.setButtonAlign(HorizontalAlignment.CENTER);
		formPanel.setWidth(500);
		formPanel.setPadding(20);

		FormLayout layout = new FormLayout();
		layout.setLabelWidth(170);

		FieldSet generalFieldSet = new FieldSet();
		generalFieldSet.setBorders(false);
		generalFieldSet.setLayout(layout);

		final TextField<String> emailTextField = new TextField<String>();
		emailTextField.setName("email");
		emailTextField.setFieldLabel(MainController.CONSTANTS.input_email());
		emailTextField.setValidator(new EmailFieldValidator());
		generalFieldSet.add(emailTextField);

		final FileUploadField file = new FileUploadField();
		file.setWidth(200);
		file.setAllowBlank(false);
		file.setName("uploadFormElement");
		file.setFieldLabel(MainController.CONSTANTS.input_file());
		generalFieldSet.add(file);

		FormPanel breakPanel = new FormPanel();
		breakPanel.getHeader().setVisible(false);
		breakPanel.setBodyBorder(false);
		breakPanel.setBorders(false);
		generalFieldSet.add(breakPanel);

		final OptionsInputPanel optionsInputPanel = new OptionsInputPanel(
				mainController.getSettings().getDefaultParametersValues(),
				mainController.getSettings().getReducedAlphabetList());
		generalFieldSet.add(optionsInputPanel);
		optionsInputPanel.collapse();

		// HiddenField<InputParameters> selectedParameters = new
		// HiddenField<InputParameters>();
		// selectedParameters.setName("crkparameters");
		// selectedParameters.setValue(optionsInputPanel.getCurrentInputParameters());
		// generalFieldSet.add(selectedParameters);
		formPanel.addListener(Events.Submit, new Listener<FormEvent>()
		{
			public void handleEvent(FormEvent formEvent)
			{
				mainController.setNrOfSubmissions(mainController.getNrOfSubmissions() + 1);
				// TODO do checking
				String jobId = formEvent.getResultHtml();
				jobId = jobId.replace("<pre>", "");
				jobId = jobId.replace("</pre>", "");
				jobId = jobId.replaceFirst("\n", "");

				RunJobData runJobData = new RunJobData();
				runJobData.setEmailAddress(emailTextField.getValue());
				runJobData.setFileName(file.getValue());
				runJobData.setJobId(jobId);
				runJobData.setInputParameters(optionsInputPanel
						.getCurrentInputParameters());

				mainController.runJob(runJobData);
			}
		});

		Button resetButton = new Button("Reset");
		resetButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				emailTextField.setValue("");
				file.setValue("");
				optionsInputPanel.fillDefaultValues(mainController
						.getSettings().getDefaultParametersValues());
			}
		});

		formPanel.addButton(resetButton);

		Button submitButton = new Button(
				MainController.CONSTANTS.input_submit());
		submitButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) 
			{
				
				if (formPanel.isValid())
				{
					formPanel.submit();
				}
				else
				{
					MessageBox.info("Action", "Can not upload file - form contains incorrect values", null);
				}
			}
		});

		formPanel.addButton(submitButton);

		recaptchaPanel = new RecaptchaPanel("6Lf9hcESAAAAAEuvcd6IjqSXW3p51Kg22JWhR3vT");
		
		if(mainController.getNrOfSubmissions() < 2)
		{
			recaptchaPanel.setVisible(false);
		}
		
		generalFieldSet.add(recaptchaPanel);
		
		formPanel.add(generalFieldSet);
		
		this.add(formPanel);
	}
	
	public RecaptchaPanel getRecaptchaPanel()
	{
		return recaptchaPanel;
	}
}
