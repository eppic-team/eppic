package ch.systemsx.sybit.crkwebui.server.email.data;

import java.io.Serializable;

public class EmailMessageData implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String emailJobSubmittedTitle;
	private String emailJobSubmittedMessage;
	private String emailJobSubmitErrorTitle;
	private String emailJobSubmitErrorMessage;
	private String emailJobErrorTitle;
	private String emailJobErrorMessage;
	private String emailJobFinishedTitle;
	private String emailJobFinishedMessage; 

	
	public EmailMessageData() {
		
	}
	
	
	public String getEmailJobSubmittedTitle() {
		return emailJobSubmittedTitle;
	}
	public void setEmailJobSubmittedTitle(String emailJobSubmittedTitle) {
		this.emailJobSubmittedTitle = emailJobSubmittedTitle;
	}
	public String getEmailJobSubmittedMessage() {
		return emailJobSubmittedMessage;
	}
	public void setEmailJobSubmittedMessage(String emailJobSubmittedMessage) {
		this.emailJobSubmittedMessage = emailJobSubmittedMessage;
	}
	public String getEmailJobSubmitErrorTitle() {
		return emailJobSubmitErrorTitle;
	}
	public void setEmailJobSubmitErrorTitle(String emailJobSubmitErrorTitle) {
		this.emailJobSubmitErrorTitle = emailJobSubmitErrorTitle;
	}
	public String getEmailJobSubmitErrorMessage() {
		return emailJobSubmitErrorMessage;
	}
	public void setEmailJobSubmitErrorMessage(String emailJobSubmitErrorMessage) {
		this.emailJobSubmitErrorMessage = emailJobSubmitErrorMessage;
	}
	public String getEmailJobErrorTitle() {
		return emailJobErrorTitle;
	}
	public void setEmailJobErrorTitle(String emailJobErrorTitle) {
		this.emailJobErrorTitle = emailJobErrorTitle;
	}
	public String getEmailJobErrorMessage() {
		return emailJobErrorMessage;
	}
	public void setEmailJobErrorMessage(String emailJobErrorMessage) {
		this.emailJobErrorMessage = emailJobErrorMessage;
	}
	public String getEmailJobFinishedTitle() {
		return emailJobFinishedTitle;
	}
	public void setEmailJobFinishedTitle(String emailJobFinishedTitle) {
		this.emailJobFinishedTitle = emailJobFinishedTitle;
	}
	public String getEmailJobFinishedMessage() {
		return emailJobFinishedMessage;
	}
	public void setEmailJobFinishedMessage(String emailJobFinishedMessage) {
		this.emailJobFinishedMessage = emailJobFinishedMessage;
	}
	
}
