/**
 * Gxt 3.0.1 Compatible
 */
package ch.systemsx.sybit.crkwebui.client.input.gui.panels;

import ch.systemsx.sybit.crkwebui.client.commons.appdata.AppPropertiesManager;
import ch.systemsx.sybit.crkwebui.client.commons.appdata.ApplicationContext;
import ch.systemsx.sybit.crkwebui.client.commons.events.GetFocusOnPdbCodeFieldEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.HideWaitingEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.ShowErrorEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.ShowWaitingEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.SubmitJobEvent;
import ch.systemsx.sybit.crkwebui.client.commons.gui.info.PopUpInfo;
import ch.systemsx.sybit.crkwebui.client.commons.gui.links.EmptyLinkWithTooltip;
import ch.systemsx.sybit.crkwebui.client.commons.gui.links.LinkWithTooltip;
import ch.systemsx.sybit.crkwebui.client.commons.gui.panels.DisplayPanel;
import ch.systemsx.sybit.crkwebui.client.commons.handlers.GetFocusOnPdbCodeFieldHandler;
import ch.systemsx.sybit.crkwebui.client.commons.handlers.SubmitJobHandler;
import ch.systemsx.sybit.crkwebui.client.commons.managers.EventBusManager;
import ch.systemsx.sybit.crkwebui.client.commons.services.eppic.CrkWebServiceProvider;
import ch.systemsx.sybit.crkwebui.client.commons.util.EscapedStringGenerator;
import ch.systemsx.sybit.crkwebui.client.commons.util.StyleGenerator;
import ch.systemsx.sybit.crkwebui.client.input.listeners.SubmitKeyListener;
import ch.systemsx.sybit.crkwebui.client.input.validators.EmailFieldValidator;
import ch.systemsx.sybit.crkwebui.shared.comparators.InputParametersComparator;
import ch.systemsx.sybit.crkwebui.shared.model.RunJobData;
import ch.systemsx.sybit.crkwebui.shared.validators.PdbCodeVerifier;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.RadioButton;
import com.sencha.gxt.widget.core.client.FramedPanel;
import com.sencha.gxt.widget.core.client.container.BoxLayoutContainer.BoxLayoutPack;
import com.sencha.gxt.widget.core.client.container.HBoxLayoutContainer;
import com.sencha.gxt.widget.core.client.container.HorizontalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.HorizontalLayoutContainer.HorizontalLayoutData;
import com.sencha.gxt.widget.core.client.container.SimpleContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.core.client.dom.ScrollSupport.ScrollMode;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.core.client.util.Padding;
import com.sencha.gxt.widget.core.client.event.ExpandEvent;
import com.sencha.gxt.widget.core.client.event.ExpandEvent.ExpandHandler;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
import com.sencha.gxt.widget.core.client.event.SubmitCompleteEvent;
import com.sencha.gxt.widget.core.client.event.SubmitCompleteEvent.SubmitCompleteHandler;
import com.sencha.gxt.widget.core.client.form.FieldLabel;
import com.sencha.gxt.widget.core.client.form.TextField;
import com.sencha.gxt.widget.core.client.form.FormPanel;
import com.sencha.gxt.widget.core.client.form.FormPanel.Encoding;
import com.sencha.gxt.widget.core.client.form.FormPanel.Method;
import com.sencha.gxt.core.client.util.ToggleGroup;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.form.FileUploadField;

/**
 * Panel used to submit new job.
 * @author srebniak_a
 * @author biyani_n
 *
 */
public class InputDataPanel extends DisplayPanel
{
	private static final int LABEL_WIDTH = 150;
	private static final int FIELD_WIDTH = 250;
	private static final int PADDING_WIDTH = 25;

	private FormPanel formPanel;

	private HBoxLayoutContainer inputRadioGroupContainer;
	private RadioButton pdbCodeRadio;
	private RadioButton pdbFileRadio;
	private ToggleGroup inputRadioGroup;

	private FileUploadField file;
	private TextField pdbCodeField;
	private TextField emailTextField;

	private FieldLabel fileLabel;
	private FieldLabel pdbCodeFieldLabel;
	private FieldLabel emailTextFieldLabel;

	private HorizontalLayoutContainer examplePanel;
	
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
		VerticalLayoutContainer mainContainer = new VerticalLayoutContainer();
		
		mainContainer.add(new SimpleContainer(), new VerticalLayoutData(1, 50, new Margins(0)));

		HorizontalLayoutContainer headerContainer = createHeaderRowContainer();
		mainContainer.add(headerContainer, new VerticalLayoutData(1, 90, new Margins(0,0,0,0)));

		HorizontalLayoutContainer fieldsetContainer = createFieldsetRowContainer();
		mainContainer.add(fieldsetContainer, new VerticalLayoutData(1, 1, new Margins(0)));

		if((ApplicationContext.getSettings().getNewsMessage() != null) &&
				(!ApplicationContext.getSettings().getNewsMessage().trim().equals("")))
		{
			NewsPanel newsContainer = new NewsPanel(ApplicationContext.getSettings().getNewsMessage());
			mainContainer.add(newsContainer, new VerticalLayoutData(1, 40, new Margins(0)));
		}

		HorizontalLayoutContainer footerContainer = createFooterRowContainer();
		mainContainer.add(footerContainer, new VerticalLayoutData(1, 40, new Margins(0)));

		this.setData(mainContainer);

		this.addStyleName("eppic-input-bg");
		
		initializeEventsListeners();
	}

	/**
	 * Creates header of input panel (title and logo).
	 * @return header container
	 */
	private HorizontalLayoutContainer createHeaderRowContainer()
	{
		HorizontalLayoutContainer headerContainer = new HorizontalLayoutContainer();

		headerContainer.add(new SimpleContainer(), new HorizontalLayoutData(0.5, 1, new Margins(0, 0, 10, 0)));

		String logoIconSource = "resources/images/eppic-logo.png";
		Image logo = new Image(logoIconSource);
		logo.setWidth("200px");
		logo.setHeight("80px");
		headerContainer.add(logo, new HorizontalLayoutData(-1, 1, new Margins(0, 0, 10, 0)));

		return headerContainer;
	}

	/**
	 * Creates container with submission form.
	 * @return submission form panel container
	 */
	private HorizontalLayoutContainer createFieldsetRowContainer()
	{
		FramedPanel panel = new FramedPanel();
		panel.getHeader().setVisible(false);
		panel.setButtonAlign(BoxLayoutPack.CENTER);
		panel.setWidth(LABEL_WIDTH+FIELD_WIDTH+2*PADDING_WIDTH+20);

		formPanel = createFormPanel();

		panel.add(formPanel);

		final VerticalLayoutContainer formContainer = new VerticalLayoutContainer();
		formContainer.setScrollMode(ScrollMode.AUTOY);
		formContainer.getElement().setPadding(new Padding(PADDING_WIDTH));

		pdbCodeRadio = createPDBCodeFileRadioItem(AppPropertiesManager.CONSTANTS.input_pdb_code_radio());
		pdbCodeRadio.setValue(true);

		pdbFileRadio = createPDBCodeFileRadioItem(AppPropertiesManager.CONSTANTS.input_upload_file_radio());

		inputRadioGroupContainer = createInputRadioGroup();

		formContainer.add(inputRadioGroupContainer, new VerticalLayoutData(-1, -1, new Margins(0, 0, 10, LABEL_WIDTH)));

		fileLabel = createFileUploadField(AppPropertiesManager.CONSTANTS.input_file());
		formContainer.add(fileLabel, new VerticalLayoutData(-1, -1, new Margins(0, 0, 0, 0)));

		pdbCodeFieldLabel = createPDBCodeField(AppPropertiesManager.CONSTANTS.input_pdb_code_radio());
		formContainer.add(pdbCodeFieldLabel, new VerticalLayoutData(-1, -1, new Margins(0, 0, 5, 0)));

		if(ApplicationContext.getSettings().getExamplePdb() != null)
		{
			examplePanel = createExamplePanel();
			formContainer.add(examplePanel, new VerticalLayoutData(-1, -1, new Margins(0, 0, 5, LABEL_WIDTH+10)));
		}else
		{
			examplePanel = new HorizontalLayoutContainer();
		}

		emailTextFieldLabel = createEmailField(AppPropertiesManager.CONSTANTS.input_email());
		formContainer.add(emailTextFieldLabel, new VerticalLayoutData(-1, -1, new Margins(15,0,10,0)));

		if(ApplicationContext.getSettings().getUniprotVersion() != null)
		{
			HorizontalLayoutContainer uniprotVersionPanel = createCurrentUniprotPanel();
			formContainer.add(uniprotVersionPanel, new VerticalLayoutData(-1, -1, new Margins(10, 0, 10, 0)));
		}

		FormPanel breakPanel = new FormPanel();
		breakPanel.setBorders(false);
		formContainer.add(breakPanel);

 		optionsInputPanel = new OptionsInputPanel(ApplicationContext.getSettings());
 		optionsInputPanel.fillDefaultValues(ApplicationContext.getSettings().getDefaultParametersValues());
 		formContainer.add(optionsInputPanel);
		optionsInputPanel.collapse();
		optionsInputPanel.addExpandHandler(new ExpandHandler() {
			
			@Override
			public void onExpand(ExpandEvent event) {
				formContainer.syncSize();
				
			}
		});

		if(ApplicationContext.getSettings().isUseCaptcha())
		{
			RecaptchaPanel recaptchaPanel = new RecaptchaPanel(ApplicationContext.getSettings().getCaptchaPublicKey());

			if(ApplicationContext.getNrOfSubmissions() < ApplicationContext.getSettings().getNrOfAllowedSubmissionsWithoutCaptcha())
			{
				recaptchaPanel.setVisible(false);
			}

			formContainer.add(recaptchaPanel);
		}

		if(ApplicationContext.getSettings().isReadOnlyMode()) {
			setUpReadOnlyMode();
		}
		
		formPanel.setWidget(formContainer);
		
		TextButton resetButton = createResetButton(AppPropertiesManager.CONSTANTS.input_reset());
		panel.addButton(resetButton);
		
		TextButton submitButton = createSubmitButton(AppPropertiesManager.CONSTANTS.input_submit());
		panel.addButton(submitButton);

		HorizontalLayoutContainer panelContainer = new HorizontalLayoutContainer();
		panelContainer.setScrollMode(ScrollMode.AUTOY);
		panelContainer.add(new SimpleContainer(), new HorizontalLayoutData(0.5, -1, new Margins(0)));
		panelContainer.add(panel, new HorizontalLayoutData(-1, -1, new Margins(0, 0, 0, 0)));

		return panelContainer;
	}

	/**
	 * Creates input panel bottom row (citation).
	 * @return bottom row container
	 */
	private HorizontalLayoutContainer createFooterRowContainer()
	{
		HorizontalLayoutContainer footerContainer = new HorizontalLayoutContainer();

		footerContainer.add(new SimpleContainer(), new HorizontalLayoutData(0.5, -1, new Margins(0)));
		footerContainer.add(new HTML(AppPropertiesManager.CONSTANTS.input_citation() + ":&nbsp;"));
		
		LinkWithTooltip citationLink = new LinkWithTooltip(AppPropertiesManager.CONSTANTS.input_citation_link_text(), 
				AppPropertiesManager.CONSTANTS.input_citation_link_tooltip(),
				ApplicationContext.getSettings().getPublicationLinkUrl());
		citationLink.addStyleName("eppic-citation");
		
		footerContainer.add(citationLink);

		return footerContainer;
	}

	/**
	 * Creates start new job form panel.
	 * @return form panel
	 */
	private FormPanel createFormPanel()
	{
		final FormPanel formPanel = new FormPanel();

		formPanel.setAction(GWT.getModuleBaseURL() + "fileUpload");
		formPanel.setEncoding(Encoding.MULTIPART);
		formPanel.setMethod(Method.POST);
		formPanel.setWidth(LABEL_WIDTH+FIELD_WIDTH);

		formPanel.addStyleName("eppic-rounded-border");

		formPanel.addSubmitCompleteHandler(new SubmitCompleteHandler() {
			@Override
			public void onSubmitComplete(SubmitCompleteEvent event) {
				String result = event.getResults();

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

		return formPanel;
	}

	private void setUpReadOnlyMode() {
		pdbFileRadio.setEnabled(false);
		optionsInputPanel.disable();
	}

	/**
	 * Creates radio item used to select file or code submission.
	 * @param label label of the radio item
	 * @return radio item
	 */
	private RadioButton createPDBCodeFileRadioItem(String label)
	{
		RadioButton pdbCodeRadio = new RadioButton("input-button", label);
		pdbCodeRadio.addStyleName("eppic-default-right-padding");
		return pdbCodeRadio;
	}
	
	/**
	 * Creates radio group used to select submission method - file or pdb code.
	 * @param pdbCodeRadio pdb code selector
	 * @param pdbFileRadio pdb file selector
	 * @return radio group
	 */
	private HBoxLayoutContainer createInputRadioGroup()
	{
		inputRadioGroup = new ToggleGroup();

		inputRadioGroup.add(pdbCodeRadio);
		inputRadioGroup.add(pdbFileRadio);
		
		inputRadioGroup.addValueChangeHandler(new ValueChangeHandler<HasValue<Boolean>>() {
			@Override
			public void onValueChange(ValueChangeEvent<HasValue<Boolean>> event) {
				ToggleGroup group = (ToggleGroup)event.getSource();
		        RadioButton radio = (RadioButton)group.getValue();
		        if(radio.equals(pdbCodeRadio)){
		        	selectCodeRadio();
		        }
		        else if(radio.equals(pdbFileRadio)){
		        	selectUploadRadio();
		        }
			}
		});
		
		HBoxLayoutContainer radioContainer = new HBoxLayoutContainer();
		radioContainer.add(pdbCodeRadio);
		radioContainer.add(pdbFileRadio);
		return radioContainer;
	}

	/**
	 * Creates field used to upload pdb file.
	 * @param label label of the field
	 * @return file uploader field
	 */
	private FieldLabel createFileUploadField(String label)
	{
		file = new FileUploadField();
		file.setWidth(FIELD_WIDTH);
		file.setAllowBlank(true);
		file.setName("uploadFormElement");

		FieldLabel fileLabel = new FieldLabel(file);
		fileLabel.setHTML(StyleGenerator.defaultFontStyle(label));
		fileLabel.setLabelWidth(LABEL_WIDTH);
		fileLabel.getElement().applyStyles("fontSize: 14px;");
		fileLabel.setVisible(false);

		return fileLabel;
	}

	/**
	 * Creates field used to provide code of the pdb to submit.
	 * @param label label of the field
	 * @return field used to provide pdb code
	 */
	private FieldLabel createPDBCodeField(String label)
	{
		pdbCodeField = new TextField();
		pdbCodeField.setWidth(FIELD_WIDTH);
		pdbCodeField.setName("code");
		//pdbCodeField.addValidator(new PdbCodeFieldValidator());
		pdbCodeField.setAllowBlank(false);
		pdbCodeField.addKeyDownHandler(new SubmitKeyListener());
		
		FieldLabel pdbCodeFieldLabel = new FieldLabel(pdbCodeField);
		pdbCodeFieldLabel.setHTML(StyleGenerator.defaultFontStyle(label));
		pdbCodeFieldLabel.setLabelWidth(LABEL_WIDTH);
		pdbCodeFieldLabel.getElement().applyStyles("fontSize:14px;");

		return pdbCodeFieldLabel;
	}

	/**
	 * Creates field used to provide email address to notify about results of processing.
	 * @param label label of the field
	 * @return email field
	 */
	private FieldLabel createEmailField(String label)
	{
		emailTextField = new TextField();
		emailTextField.setWidth(FIELD_WIDTH);
		emailTextField.setName("email");
		emailTextField.addValidator(new EmailFieldValidator());
		emailTextField.addKeyDownHandler(new SubmitKeyListener());

		FieldLabel emailTextFieldLabel = new FieldLabel(emailTextField);
		emailTextFieldLabel.setHTML(StyleGenerator.defaultFontStyle(label));
		emailTextFieldLabel.setLabelWidth(LABEL_WIDTH);
		emailTextFieldLabel.getElement().applyStyles("fontSize:14px;");
		emailTextFieldLabel.setVisible(false);

		return emailTextFieldLabel;
	}

	/**
	 * Creates button used to reset all the fields of the form.
	 * @param text text of the button
	 * @return reset button
	 */
	private TextButton createResetButton(String text)
	{
		TextButton resetButton = new TextButton(AppPropertiesManager.CONSTANTS.input_reset());
		resetButton.addSelectHandler(new SelectHandler() {
			@Override
			public void onSelect(SelectEvent event) {
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
	private TextButton createSubmitButton(String text)
	{
		final TextButton submitButton = new TextButton(
				AppPropertiesManager.CONSTANTS.input_submit());
		submitButton.addSelectHandler(new SelectHandler() {
			@Override
			public void onSelect(SelectEvent event) {
				EventBusManager.EVENT_BUS.fireEventFromSource(new SubmitJobEvent(), submitButton);

			}
		});

		return submitButton;
	}

	/**
	 * Creates panel containing current uniprot version.
	 * @return panel containing current uniprot version
	 */
	private HorizontalLayoutContainer createCurrentUniprotPanel()
	{
		HorizontalLayoutContainer uniprotPanel = new HorizontalLayoutContainer();
		uniprotPanel.getElement().applyStyles("textAlign:center; paddingTop:25px; height:22px; paddingBottom:1px;");

		String uniprotVersionText = AppPropertiesManager.CONSTANTS.input_uniprot_version() + ":&nbsp;" +
				                    "<i>"+ ApplicationContext.getSettings().getUniprotVersion() + "</i>" ;
		
		HTMLPanel uniprotVersionHtml = new HTMLPanel(EscapedStringGenerator.generateSafeHtml(uniprotVersionText));
		uniprotVersionHtml.addStyleName("eppic-uniprot-version");
		
		uniprotPanel.add(new SimpleContainer(), new HorizontalLayoutData(0.5, -1, new Margins(0)));
		uniprotPanel.add(uniprotVersionHtml, new HorizontalLayoutData(-1,-1, new Margins(0, 0, 0, 0)));
		
		return uniprotPanel;
	}


	/**
	 * Creates panel containing link to example results.
	 * @return panel containing link to example results
	 */
	private HorizontalLayoutContainer createExamplePanel()
	{
		HorizontalLayoutContainer examplePanel = new HorizontalLayoutContainer();
		examplePanel.getElement().applyStyles("height:22px; paddingBottom:10px;");

		HTML exampleLinkLabel = new HTML(AppPropertiesManager.CONSTANTS.input_example() + ":&nbsp;");

		EmptyLinkWithTooltip exampleLink = new EmptyLinkWithTooltip(ApplicationContext.getSettings().getExamplePdb(),
				AppPropertiesManager.CONSTANTS.input_example_hint());
		exampleLink.addStyleName("eppic-action");
		exampleLink.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				History.newItem("id/" + ApplicationContext.getSettings().getExamplePdb());
			}
		});
		examplePanel.add(exampleLinkLabel, new HorizontalLayoutData(-1,-1));
		examplePanel.add(exampleLink, new HorizontalLayoutData(-1,-1));

		return examplePanel;
	}

	/**
	 * method to trigger events after pdb code radio is selected
	 */
	private void selectCodeRadio(){
		fileLabel.setVisible(false);
		file.setAllowBlank(true);
		file.reset();
		pdbCodeField.reset();
		pdbCodeField.setAllowBlank(false);
		pdbCodeFieldLabel.setVisible(true);
		emailTextFieldLabel.setVisible(false);
		examplePanel.setVisible(true);
	}
	
	/**
	 *  method to trigger events after upload radio is selected
	 */
	private void selectUploadRadio(){
		fileLabel.setVisible(true);
		file.setAllowBlank(false);
		file.reset();
		pdbCodeField.reset();
		pdbCodeFieldLabel.setVisible(false);
		pdbCodeField.setAllowBlank(true);
		emailTextFieldLabel.setVisible(true);
		examplePanel.setVisible(false);
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
			input = pdbCodeField.getCurrentValue();

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
		//Check if pdb is correct
		if(pdbCodeRadio.getValue() && pdbCodeField.getCurrentValue()!=null ){
			String pdbCode = pdbCodeField.getCurrentValue().trim();
			if(!PdbCodeVerifier.isTrimmedValid(pdbCode)){
				PopUpInfo.show(AppPropertiesManager.CONSTANTS.pdb_code_box_wrong_code_header(),
						 AppPropertiesManager.CONSTANTS.pdb_code_box_wrong_code_supporting_text()+ " " + pdbCode);
				pdbCodeField.reset();
				pdbCodeField.focus();
				return;
			}
		}
		
		if (formPanel.isValid(false))
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
				String trimmedJobId = pdbCodeField.getCurrentValue().toLowerCase().trim();
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
			PopUpInfo.show(AppPropertiesManager.CONSTANTS.input_submit_form_invalid_header(),
					AppPropertiesManager.CONSTANTS.input_submit_form_invalid_message());
		}
	}

	/**
	 * Resets values of the fields and resets the radios too.
	 */
	public void resetToDefault()
	{
		emailTextField.reset();
		pdbCodeField.reset();
		file.reset();
		optionsInputPanel.resetValues();
		inputRadioGroup.setValue(pdbCodeRadio);
		selectCodeRadio();
	}
	
	/**
	 * Resets values of the fields.
	 */
	public void resetValues()
	{
		emailTextField.reset();
		pdbCodeField.reset();
		file.reset();
		optionsInputPanel.resetValues();
	}

	/**
	 * Events listeners initialization.
	 */
	private void initializeEventsListeners()
	{
		EventBusManager.EVENT_BUS.addHandler(GetFocusOnPdbCodeFieldEvent.TYPE, new GetFocusOnPdbCodeFieldHandler() {

			@Override
			public void onGrabFocusOnPdbCodeField(GetFocusOnPdbCodeFieldEvent event) {
				pdbCodeRadio.setValue(true);
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
