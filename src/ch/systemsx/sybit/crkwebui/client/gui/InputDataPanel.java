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
import ch.systemsx.sybit.crkwebui.shared.comparators.InputParametersComparator;
import ch.systemsx.sybit.crkwebui.shared.model.RunJobData;

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
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout.VBoxLayoutAlign;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.Image;

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
		LayoutContainer container = new LayoutContainer();
		container.setLayout(new RowLayout());
		
		LayoutContainer headerContainer = createHeaderRowContainer();
		container.add(headerContainer, new RowData(1, 40, new Margins(0)));
		
		LayoutContainer fieldsetContainer = createFieldsetRowContainer();
		container.add(fieldsetContainer, new RowData(1, 1, new Margins(0)));
		
		LayoutContainer footerContainer = createFooterRowContainer();
		container.add(footerContainer, new RowData(1, 20, new Margins(0)));
		
		this.add(container);
		
		initializeEventsListeners();
	}
	
	/**
	 * Creates header of input panel (title and logo).
	 * @return header container
	 */
	private LayoutContainer createHeaderRowContainer()
	{
		LayoutContainer headerContainer = new LayoutContainer();
		headerContainer.setLayout(new RowLayout(Orientation.HORIZONTAL));
		
		headerContainer.add(new LayoutContainer(), new RowData(0.5, 1, new Margins(0, 0, 10, 0)));
		
		Label title = new Label(AppPropertiesManager.CONSTANTS.input_title());
		title.addStyleName("eppic-input-title");
		headerContainer.add(title, new RowData(-1, 1, new Margins(0)));

		LayoutContainer logoContainer = createLogoContainer();
		headerContainer.add(logoContainer, new RowData(0.5, 1, new Margins(0)));
		
		return headerContainer;
	}

	/**
	 * Creates panel containing logo.
	 * @return panel with logo
	 */
	private LayoutContainer createLogoContainer()
	{
		LayoutContainer logoContainer = new LayoutContainer();
		VBoxLayout logoContainerLayout = new VBoxLayout();  
		logoContainerLayout.setVBoxLayoutAlign(VBoxLayoutAlign.RIGHT);  
		logoContainer.setLayout(logoContainerLayout);
		
		String logoIconSource = "resources/images/eppic-logo.png";
		Image logo = new Image(logoIconSource);
		logoContainer.add(logo);
		
		return logoContainer;
	}
	
	/**
	 * Creates container with submission form.
	 * @return submission form panel container
	 */
	private LayoutContainer createFieldsetRowContainer()
	{
		LayoutContainer fieldsetContainer = new LayoutContainer();
		fieldsetContainer.setLayout(new RowLayout(Orientation.HORIZONTAL));
		fieldsetContainer.add(new LayoutContainer(), new RowData(0.5, -1, new Margins(0)));
		
		formPanel = createFormPanel();
		fieldsetContainer.add(formPanel, new RowData(-1, -1, new Margins(0)));
		
		fieldsetContainer.setScrollMode(Scroll.AUTOY);
		return fieldsetContainer;
	}
	
	/**
	 * Creates input panel bottom row (citation).
	 * @return bottom row container
	 */
	private LayoutContainer createFooterRowContainer()
	{
		LayoutContainer footerContainer = new LayoutContainer();
		footerContainer.setLayout(new RowLayout(Orientation.HORIZONTAL));
		footerContainer.add(new LayoutContainer(), new RowData(0.5, -1, new Margins(0)));
		
		Label citationDescription = new Label(AppPropertiesManager.CONSTANTS.input_citation() + " :&nbsp;");
		citationDescription.addStyleName("eppic-nowrap-text");
		footerContainer.add(citationDescription, new RowData(-1, -1, new Margins(0)));
		
		LinkWithTooltip citationLink = new LinkWithTooltip(AppPropertiesManager.CONSTANTS.input_citation_link_text(), 
													   AppPropertiesManager.CONSTANTS.input_citation_link_tooltip(), 
													   ApplicationContext.getWindowData(), 
				   									   0,
				   									   ApplicationContext.getSettings().getPublicationLinkUrl());
		footerContainer.add(citationLink, new RowData(-1, -1, new Margins(0)));
		
		return footerContainer;
	}
	
	/**
	 * Creates start new job form panel.
	 * @return form panel
	 */
	private FormPanel createFormPanel()
	{
		final FormPanel formPanel = new FormPanel();

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

		FieldSet generalFieldSet = createFieldSet();
		formPanel.add(generalFieldSet);
		
		Button resetButton = createResetButton(AppPropertiesManager.CONSTANTS.input_reset());
		formPanel.addButton(resetButton);

		Button submitButton = createSubmitButton(AppPropertiesManager.CONSTANTS.input_submit());
		formPanel.addButton(submitButton);

		return formPanel;
	}
	
	/**
	 * Creates fieldset with general and advanced options.
	 * @return fieldset with data to submit
	 */
	private FieldSet createFieldSet()
	{
		FormLayout layout = new FormLayout();
		layout.setLabelWidth(170);
		
		FieldSet generalFieldSet = new FieldSet();
		generalFieldSet.setBorders(false);
		generalFieldSet.setLayout(layout);

		pdbCodeRadio = createPDBCodeFileRadioItem(AppPropertiesManager.CONSTANTS.input_pdb_code_radio());
		pdbCodeRadio.setValue(true);
		
	    pdbFileRadio = createPDBCodeFileRadioItem(AppPropertiesManager.CONSTANTS.input_upload_file_radio());
	    
		inputRadioGroup = createInputRadioGroup(pdbCodeRadio, pdbFileRadio);
	    generalFieldSet.add(inputRadioGroup);

		file = createFileUploadField(AppPropertiesManager.CONSTANTS.input_file());
		generalFieldSet.add(file);

		pdbCodeField = createPDBCodeField(AppPropertiesManager.CONSTANTS.input_pdb_code_radio());
		generalFieldSet.add(pdbCodeField);
		
		if(ApplicationContext.getSettings().getExamplePdb() != null)
		{
			LayoutContainer examplePanel = createExamplePanel();
			generalFieldSet.add(examplePanel);
		}

		emailTextField = createEmailField(AppPropertiesManager.CONSTANTS.input_email());
		generalFieldSet.add(emailTextField);
		
		FormPanel breakPanel = new FormPanel();
		breakPanel.getHeader().setVisible(false);
		breakPanel.setBodyBorder(false);
		breakPanel.setBorders(false);
		generalFieldSet.add(breakPanel);

		optionsInputPanel = new OptionsInputPanel(ApplicationContext.getSettings());
		generalFieldSet.add(optionsInputPanel);
		optionsInputPanel.collapse();

		if(ApplicationContext.getSettings().isUseCaptcha())
		{
			recaptchaPanel = new RecaptchaPanel(ApplicationContext.getSettings().getCaptchaPublicKey());

			if(ApplicationContext.getNrOfSubmissions() < ApplicationContext.getSettings().getNrOfAllowedSubmissionsWithoutCaptcha())
			{
				recaptchaPanel.setVisible(false);
			}

			generalFieldSet.add(recaptchaPanel);
		}
		
		return generalFieldSet;
	}
	
	/**
	 * Creates radio item used to select file or code submission.
	 * @param label labe of the radio item
	 * @return radio item
	 */
	private Radio createPDBCodeFileRadioItem(String label)
	{
		Radio pdbCodeRadio = new Radio();
		pdbCodeRadio.setBoxLabel(label);
		pdbCodeRadio.setLabelStyle("font-size: 14px;");
		return pdbCodeRadio;
	}
	
	/**
	 * Creates radio group used to select submission method - file or pdb code.
	 * @param pdbCodeRadio pdb code selector
	 * @param pdbFileRadio pdb file selector
	 * @return radio group
	 */
	private RadioGroup createInputRadioGroup(Radio pdbCodeRadio,
											 Radio pdbFileRadio)
	{
	    final RadioGroup inputRadioGroup = new RadioGroup();
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
	    
	    return inputRadioGroup;
	}
	
	/**
	 * Creates field used to upload pdb file.
	 * @param label label of the field
	 * @return file uploader field
	 */
	private FileUploadField createFileUploadField(String label)
	{
		FileUploadField file = new FileUploadField();
		file.setWidth(200);
		file.setAllowBlank(true);
		file.setName("uploadFormElement");
		file.setFieldLabel(label);
		file.setLabelStyle("font-size: 14px;");
		file.setVisible(false);
		return file;
	}
	
	/**
	 * Creates field used to provide code of the pdb to submit.
	 * @param label label of the field
	 * @return field used to provide pdb code
	 */
	private TextField<String> createPDBCodeField(String label)
	{
		TextField<String> pdbCodeField = new TextField<String>();
		pdbCodeField.setName("code");
		pdbCodeField.setFieldLabel(label);
		pdbCodeField.setLabelStyle("font-size: 14px;");
		pdbCodeField.setValidator(new PdbCodeFieldValidator());
		pdbCodeField.setAllowBlank(false);
		pdbCodeField.addKeyListener(new SubmitKeyListener());
		return pdbCodeField;
	}
	
	/**
	 * Creates field used to provide email address to notify about results of processing.
	 * @param label label of the field
	 * @return email field
	 */
	private TextField<String> createEmailField(String label)
	{
		TextField<String> emailTextField = new TextField<String>();
		emailTextField.setName("email");
		emailTextField.setFieldLabel(label);
		emailTextField.setLabelStyle("font-size: 14px;");
		emailTextField.setValidator(new EmailFieldValidator());
		emailTextField.addKeyListener(new SubmitKeyListener());
		return emailTextField;
	}

	/**
	 * Creates button used to reset all the fields of the form.
	 * @param text text of the button
	 * @return reset button
	 */
	private Button createResetButton(String text)
	{
		Button resetButton = new Button(AppPropertiesManager.CONSTANTS.input_reset());
		resetButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce)
			{
				resetValues();
			}
		});
		
		return resetButton;
	}
	
	/**
	 * Creates button used to start new jobs.
	 * @param text text of the button
	 * @return submit button
	 */
	private Button createSubmitButton(String text)
	{
		final Button submitButton = new Button(
				AppPropertiesManager.CONSTANTS.input_submit());
		submitButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce)
			{
				EventBusManager.EVENT_BUS.fireEventFromSource(new SubmitJobEvent(), submitButton);
			}
		});
		
		return submitButton;
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
			final MessageBox messageBox = MessageBox.info(AppPropertiesManager.CONSTANTS.input_submit_form_invalid_header(),
							AppPropertiesManager.CONSTANTS.input_submit_form_no_methods_selected(),
							null);
			
			messageBox.getDialog().setResizable(true);
			if(messageBox.getDialog().getInitialWidth() > ApplicationContext.getWindowData().getWindowWidth() - 20)
			{
				messageBox.getDialog().setWidth(ApplicationContext.getWindowData().getWindowWidth() - 20);
			}
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
			final MessageBox messageBox = MessageBox.info(AppPropertiesManager.CONSTANTS.input_submit_form_invalid_header(),
							AppPropertiesManager.CONSTANTS.input_submit_form_invalid_message(),
							null);
			
			messageBox.getDialog().setResizable(true);
			if(messageBox.getDialog().getInitialWidth() > ApplicationContext.getWindowData().getWindowWidth() - 20)
			{
				messageBox.getDialog().setWidth(ApplicationContext.getWindowData().getWindowWidth() - 20);
			}
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
