package eppic.rest.jobs;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

	private String baseUrlJobRetrieval;


	public EmailMessageData() {

	}


	public String getEmailJobSubmittedTitle(String submissionId) {
		return String.format(emailJobSubmittedTitle, submissionId);
	}
	public void setEmailJobSubmittedTitle(String emailJobSubmittedTitle) {
		validateString(1, emailJobSubmittedTitle, "emailJobSubmittedTitle");
		this.emailJobSubmittedTitle = emailJobSubmittedTitle;
	}
	public String getEmailJobSubmittedMessage(String submissionId) {
		return String.format(emailJobSubmittedMessage, baseUrlJobRetrieval,submissionId);
	}
	public void setEmailJobSubmittedMessage(String emailJobSubmittedMessage) {
		validateString(2, emailJobSubmittedMessage,"emailJobSubmittedMessage");
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
	public String getEmailJobErrorTitle(String submissionId) {
		return String.format(emailJobErrorTitle, submissionId);
	}
	public void setEmailJobErrorTitle(String emailJobErrorTitle) {
		validateString(1, emailJobErrorTitle,"emailJobErrorTitle");
		this.emailJobErrorTitle = emailJobErrorTitle;
	}
	public String getEmailJobErrorMessage(String submissionId) {
		return String.format(emailJobErrorMessage, baseUrlJobRetrieval, submissionId);
	}
	public void setEmailJobErrorMessage(String emailJobErrorMessage) {
		validateString(2, emailJobErrorMessage,"emailJobErrorMessage");
		this.emailJobErrorMessage = emailJobErrorMessage;
	}
	public String getEmailJobFinishedTitle(String submissionId) {
		return String.format(emailJobFinishedTitle, submissionId);
	}
	public void setEmailJobFinishedTitle(String emailJobFinishedTitle) {
		validateString(1, emailJobFinishedTitle,"emailJobFinishedTitle");
		this.emailJobFinishedTitle = emailJobFinishedTitle;
	}
	public String getEmailJobFinishedMessage(String submissionId) {
		return String.format(emailJobFinishedMessage, baseUrlJobRetrieval, submissionId);
	}
	public void setEmailJobFinishedMessage(String emailJobFinishedMessage) {
		validateString(2, emailJobFinishedMessage,"emailJobFinishedMessage");
		this.emailJobFinishedMessage = emailJobFinishedMessage;
	}

	public String getBaseUrlJobRetrieval() {
		return baseUrlJobRetrieval;
	}

	public void setBaseUrlJobRetrieval(String baseUrlJobRetrieval) {
		this.baseUrlJobRetrieval = baseUrlJobRetrieval;
	}

	private void validateString(int occurrencesOfReplacementStr, String strToCheck, String fieldName) {
		int occurrences = countOccurrencesSubstring("%s", strToCheck);
		if (occurrences != occurrencesOfReplacementStr) {
			throw new IllegalArgumentException("'" + fieldName + "' needs "+occurrencesOfReplacementStr+" '%s' exactly, but found " + occurrences);
		}
	}

	protected static int countOccurrencesSubstring(String substring, String string) {
		Matcher m = Pattern.compile(substring).matcher(string);
		int count = 0;
		while (m.find()) {
			count++;
		}
		return count;
	}

	@Override
	public String toString() {
		return "EmailMessageData{" +
				"emailJobSubmittedTitle='" + emailJobSubmittedTitle + '\'' +
				", emailJobSubmittedMessage='" + emailJobSubmittedMessage + '\'' +
				", emailJobSubmitErrorTitle='" + emailJobSubmitErrorTitle + '\'' +
				", emailJobSubmitErrorMessage='" + emailJobSubmitErrorMessage + '\'' +
				", emailJobErrorTitle='" + emailJobErrorTitle + '\'' +
				", emailJobErrorMessage='" + emailJobErrorMessage + '\'' +
				", emailJobFinishedTitle='" + emailJobFinishedTitle + '\'' +
				", emailJobFinishedMessage='" + emailJobFinishedMessage + '\'' +
				", baseUrlJobRetrieval='" + baseUrlJobRetrieval + '\'' +
				'}';
	}
}
