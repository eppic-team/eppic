package ch.systemsx.sybit.crkwebui.client.gui;

import ch.systemsx.sybit.crkwebui.client.controllers.AppPropertiesManager;
import ch.systemsx.sybit.crkwebui.client.controllers.ApplicationContext;
import ch.systemsx.sybit.crkwebui.client.controllers.CrkWebServiceProvider;
import ch.systemsx.sybit.crkwebui.client.controllers.EventBusManager;
import ch.systemsx.sybit.crkwebui.client.events.GetFocusOnPdbCodeFieldEvent;
import ch.systemsx.sybit.crkwebui.client.events.HideWaitingEvent;
import ch.systemsx.sybit.crkwebui.client.events.ShowErrorEvent;
import ch.systemsx.sybit.crkwebui.client.events.ShowWaitingEvent;
import ch.systemsx.sybit.crkwebui.client.events.SubmitJobEvent;
import ch.systemsx.sybit.crkwebui.client.gui.validators.EmailFieldValidator;
import ch.systemsx.sybit.crkwebui.client.gui.validators.PdbCodeFieldValidator;
import ch.systemsx.sybit.crkwebui.client.handlers.GetFocusOnPdbCodeFieldHandler;
import ch.systemsx.sybit.crkwebui.client.handlers.SubmitJobHandler;
import ch.systemsx.sybit.crkwebui.client.listeners.SubmitKeyListener;
import ch.systemsx.sybit.crkwebui.shared.model.RunJobData;
import ch.systemsx.sybit.crkwebui.shared.validators.InputParametersComparator;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FormEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.form.FileUploadField;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.FormPanel.Encoding;
import com.extjs.gxt.ui.client.widget.form.FormPanel.Method;
import com.extjs.gxt.ui.client.widget.form.Radio;
import com.extjs.gxt.ui.client.widget.form.RadioGroup;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.History;

/**
 * Panel used to submit new job.
 * @author srebniak_a
 *
 */
public class InputDataPanel extends DisplayPanel
{
	private RecaptchaPanel recaptchaPanel;

	private FormPanel formPanel;

	private RadioGroup inputRadioGroup;
	private Radio pdbCodeRadio;
	private Radio pdbFileRadio;

	private FileUploadField file;
	private TextField<String> pdbCodeField;
	private TextField<String> emailTextField;
	
	private OptionsInputPanel optionsInputPanel;

	public InputDataPanel()
	{
		init();
	}

	/**
	 * Initializes content of the panel.
	 */
	private void init()
	{
		this.setLayout(new RowLayout(Orientation.HORIZONTAL));
		this.setBorders(false);
		this.setScrollMode(Scroll.AUTO);

		formPanel = new FormPanel();

		formPanel.getHeader().setVisible(false);
		formPanel.setBorders(true);
		formPanel.setBodyBorder(false);
		formPanel.setAction(GWT.getModuleBaseURL() + "fileUpload");
		formPanel.setEncoding(Encoding.MULTIPART);
		formPanel.setMethod(Method.POST);
		formPanel.setButtonAlign(HorizontalAlignment.CENTER);
		formPanel.setWidth(500);
		formPanel.setAutoHeight(true);
		formPanel.setPadding(20);

		FormLayout layout = new FormLayout();
		layout.setLabelWidth(170);

		FieldSet generalFieldSet = new FieldSet();
		generalFieldSet.setBorders(false);
		generalFieldSet.setLayout(layout);

		pdbCodeRadio = new Radio();
		pdbCodeRadio.setBoxLabel(AppPropertiesManager.CONSTANTS.input_pdb_code_radio());
		pdbCodeRadio.setValue(true);
		pdbCodeRadio.setLabelStyle("font-size: 14px;");

	    pdbFileRadio = new Radio();
	    pdbFileRadio.setBoxLabel(AppPropertiesManager.CONSTANTS.input_upload_file_radio());
	    pdbFileRadio.setLabelStyle("font-size: 14px;");

	    inputRadioGroup = new RadioGroup();
	    inputRadioGroup.setFieldLabel(AppPropertiesManager.CONSTANTS.input_pdb_input_type());
	    if ((AppPropertiesManager.CONSTANTS.input_pdb_input_type() != null) &&
	    	(AppPropertiesManager.CONSTANTS.input_pdb_input_type().equals("")))
	    {
	    	inputRadioGroup.setLabelSeparator("");
	    }
	    inputRadioGroup.add(pdbCodeRadio);
	    inputRadioGroup.add(pdbFileRadio);

	    inputRadioGroup.addListener(Events.Change, new Listener<BaseEvent>(){
	        public void handleEvent(BaseEvent be)
	        {
	        	String selectedType = inputRadioGroup.getValue().getBoxLabel();

	        	if(selectedType.equals(AppPropertiesManager.CONSTANTS.input_pdb_code_radio()))
	        	{
	        		file.setVisible(false);
	        		file.setAllowBlank(true);
	        		file.reset();
	        		pdbCodeField.reset();
	        		pdbCodeField.setAllowBlank(false);
	        		pdbCodeField.setVisible(true);
	        	}
	        	else if(selectedType.equals(AppPropertiesManager.CONSTANTS.input_upload_file_radio()))
	        	{
	        		file.setVisible(true);
	        		file.setAllowBlank(false);
	        		pdbCodeField.setValue("");
	        		pdbCodeField.setVisible(false);
	        		pdbCodeField.setAllowBlank(true);
	        	}

	        	formPanel.layout();
	        }
	    });

	    generalFieldSet.add(inputRadioGroup);

		file = new FileUploadField();
		file.setWidth(200);
		file.setAllowBlank(true);
		file.setName("uploadFormElement");
		file.setFieldLabel(AppPropertiesManager.CONSTANTS.input_file());
		file.setLabelStyle("font-size: 14px;");
		file.setVisible(false);
		generalFieldSet.add(file);

		pdbCodeField = new TextField<String>();
		pdbCodeField.setName("code");
		pdbCodeField.setFieldLabel(AppPropertiesManager.CONSTANTS.input_pdb_code_radio());
		pdbCodeField.setLabelStyle("font-size: 14px;");
		pdbCodeField.setValidator(new PdbCodeFieldValidator());
		pdbCodeField.setAllowBlank(false);
		pdbCodeField.addKeyListener(new SubmitKeyListener());
		generalFieldSet.add(pdbCodeField);
		
		if(ApplicationContext.getSettings().getExamplePdb() != null)
		{
			LayoutContainer examplePanel = createExamplePanel();
			generalFieldSet.add(examplePanel);
		}

		emailTextField = new TextField<String>();
		emailTextField.setName("email");
		emailTextField.setFieldLabel(AppPropertiesManager.CONSTANTS.input_email());
		emailTextField.setLabelStyle("font-size: 14px;");
		emailTextField.setValidator(new EmailFieldValidator());
		emailTextField.addKeyListener(new SubmitKeyListener());
		generalFieldSet.add(emailTextField);
		
		FormPanel breakPanel = new FormPanel();
		breakPanel.getHeader().setVisible(false);
		breakPanel.setBodyBorder(false);
		breakPanel.setBorders(false);
		generalFieldSet.add(breakPanel);

		optionsInputPanel = new OptionsInputPanel(ApplicationContext.getSettings());

		generalFieldSet.add(optionsInputPanel);
		optionsInputPanel.collapse();

		formPanel.addListener(Events.Submit, new Listener<FormEvent>()
		{
			public void handleEvent(FormEvent formEvent)
			{
				String result = formEvent.getResultHtml();

				if((result != null) && (result.startsWith("crkupres:")))
				{
					result = result.replaceAll("\n", "");
					result = result.trim();
					result = result.substring(9);
					runJob(result);
				}
				else if((result != null) && (result.startsWith("err:")))
				{
					result = result.replaceAll("\n", "");
					result = result.trim();
					result = result.substring(4);
					EventBusManager.EVENT_BUS.fireEvent(new ShowErrorEvent(result));
					EventBusManager.EVENT_BUS.fireEvent(new HideWaitingEvent());
				}
				else
				{
					EventBusManager.EVENT_BUS.fireEvent(new ShowErrorEvent(AppPropertiesManager.CONSTANTS.input_submit_error()));
					EventBusManager.EVENT_BUS.fireEvent(new HideWaitingEvent());
				}
			}
		});

		Button resetButton = new Button(AppPropertiesManager.CONSTANTS.input_reset());
		resetButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce)
			{
				resetValues();
			}
		});

		formPanel.addButton(resetButton);

		final Button submitButton = new Button(
				AppPropertiesManager.CONSTANTS.input_submit());
		submitButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce)
			{
				EventBusManager.EVENT_BUS.fireEventFromSource(new SubmitJobEvent(), submitButton);
			}
		});

		formPanel.addButton(submitButton);

		if(ApplicationContext.getSettings().isUseCaptcha())
		{
			recaptchaPanel = new RecaptchaPanel(ApplicationContext.getSettings().getCaptchaPublicKey());

			if(ApplicationContext.getNrOfSubmissions() < ApplicationContext.getSettings().getNrOfAllowedSubmissionsWithoutCaptcha())
			{
				recaptchaPanel.setVisible(false);
			}

			generalFieldSet.add(recaptchaPanel);
		}

		formPanel.add(generalFieldSet);

		//fix for chrome - otherwise unnecessary scrollbars visible
		this.add(new LayoutContainer(), new RowData(0.5, -1, new Margins(0)));
		this.add(formPanel, new RowData(-1, -1, new Margins(0)));
		
		initializeEventsListeners();
	}
	
	/**
	 * Creates panel containing link to example results.
	 * @return panel containing link to example results
	 */
	private LayoutContainer createExamplePanel()
	{
		LayoutContainer examplePanel = new LayoutContainer();
		examplePanel.setStyleAttribute("padding-left", "175px");
		examplePanel.setStyleAttribute("height", "22px");
		examplePanel.setStyleAttribute("padding-bottom", "10px");
		
		Label exampleLinkLabel = new Label(AppPropertiesManager.CONSTANTS.input_example() + ": ");
		
		Label exampleLink = new EmptyLinkWithTooltip(ApplicationContext.getSettings().getExamplePdb(),
													 AppPropertiesManager.CONSTANTS.input_example_hint(),
													 ApplicationContext.getWindowData(),
													 0);
		exampleLink.addStyleName("eppic-horizontal-nav");
		exampleLink.addListener(Events.OnClick, new Listener<BaseEvent>() {

			@Override
			public void handleEvent(BaseEvent be) {
				History.newItem("id/" + ApplicationContext.getSettings().getExamplePdb());
			}
		});
		examplePanel.add(exampleLinkLabel);
		examplePanel.add(exampleLink);
		
		return examplePanel;
	}

	/**
	 * Starts new job. If file is uploaded then jobId is used to identify uploaded file, otherwise jobId is null.
	 * @param jobId identifier of the job
	 */
	private void runJob(String jobId)
	{
		ApplicationContext.setNrOfSubmissions(ApplicationContext.getNrOfSubmissions() + 1);

		RunJobData runJobData = new RunJobData();
		runJobData.setEmailAddress(emailTextField.getValue());

		String input = null;

		if(pdbFileRadio.getValue())
		{
			input = file.getValue();

			if(input.startsWith("C:\\fakepath\\"))
			{
				input = input.substring(12);
			}
			else if(input.contains("\\"))
			{
				input = input.substring(input.lastIndexOf("\\") + 1);
			}
		}
		else
		{
			input = pdbCodeField.getValue();

			if(input != null)
			{
				input = input.toLowerCase().trim();
			}
		}

		runJobData.setInput(input);
		runJobData.setJobId(jobId);
		runJobData.setInputParameters(optionsInputPanel
				.getCurrentInputParameters());

		EventBusManager.EVENT_BUS.fireEvent(new HideWaitingEvent());
		CrkWebServiceProvider.getServiceController().runJob(runJobData);
	}

	/**
	 * Validates and submits form.
	 */
	private void submitForm()
	{
		if(!optionsInputPanel.checkIfAnyMethodSelected())
		{
			MessageBox.info(AppPropertiesManager.CONSTANTS.input_submit_form_invalid_header(),
							AppPropertiesManager.CONSTANTS.input_submit_form_no_methods_selected(),
							null);
		}
		else if (formPanel.isValid())
		{
			EventBusManager.EVENT_BUS.fireEvent(new ShowWaitingEvent(AppPropertiesManager.CONSTANTS.input_submit_waiting_message()));

			if(pdbFileRadio.getValue())
			{
				formPanel.submit();
			}
			else if((ApplicationContext.getSettings().isUsePrecompiledResults()) &&
					(InputParametersComparator.checkIfEquals(optionsInputPanel.getCurrentInputParameters(),
					 										 ApplicationContext.getSettings().getDefaultParametersValues())))
			{
				EventBusManager.EVENT_BUS.fireEvent(new HideWaitingEvent());
				String trimmedJobId = pdbCodeField.getValue().toLowerCase().trim();
				ApplicationContext.setSelectedJobId(trimmedJobId);
				History.newItem("id/" + trimmedJobId);
			}
			else
			{
				runJob(null);
			}
		}
		else
		{
			MessageBox.info(AppPropertiesManager.CONSTANTS.input_submit_form_invalid_header(),
							AppPropertiesManager.CONSTANTS.input_submit_form_invalid_message(),
							null);
		}
	}

	/**
	 * Resets values of the fields.
	 */
	public void resetValues()
	{
		emailTextField.setValue("");

		file.setAllowBlank(true);
		pdbCodeField.setAllowBlank(true);
		pdbCodeField.setValue("");
		file.reset();

		file.setVisible(false);
		file.setAllowBlank(true);
		pdbCodeField.setVisible(true);
		pdbCodeField.setAllowBlank(false);

		pdbCodeRadio.setValue(true);

		optionsInputPanel.fillDefaultValues(ApplicationContext
				.getSettings().getDefaultParametersValues());
	}
	
	/**
	 * Events listeners initialization.
	 */
	private void initializeEventsListeners()
	{
		EventBusManager.EVENT_BUS.addHandler(GetFocusOnPdbCodeFieldEvent.TYPE, new GetFocusOnPdbCodeFieldHandler() {
			
			@Override
			public void onGrabFocusOnPdbCodeField(GetFocusOnPdbCodeFieldEvent event) {
				pdbCodeField.focus();
			}
		});
		
		EventBusManager.EVENT_BUS.addHandler(SubmitJobEvent.TYPE, new SubmitJobHandler() {
			
			@Override
			public void onSubmitJob(SubmitJobEvent event) {
				submitForm();
			}
		});
	}
}
