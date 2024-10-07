package eppic.rest.jobs;

import java.io.IOException;
import java.util.concurrent.Executors;

import jakarta.mail.MessagingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is used to send emails.
 * @author srebniak_a
 *
 */
public class EmailSender
{
	private static final Logger logger = LoggerFactory.getLogger(EmailSender.class);

	private final EmailData emailData;

	public EmailSender(EmailData emailData)
	{
		this.emailData = emailData;
	}

	/**
	 * Sends email.
	 * @throws MessagingException
	 */
	public void send(String subject, String bodyText) throws MessagingException {
		if (emailData.getSendingMethod() == SendingMethod.SMTP) {
			EmailSenderSmtp.send(subject, bodyText, emailData);
		} else if (emailData.getSendingMethod() == SendingMethod.GOOGLE) {
			EmailSenderGoogle.send(subject, bodyText, emailData);
		} else {
			throw new UnsupportedOperationException("Email sending method not supported");
		}
	}

	public void sendSubmittedEmail(String submissionId) throws MessagingException {
		send(
				emailData.getEmailMessageData().getEmailJobSubmittedTitle(submissionId),
				emailData.getEmailMessageData().getEmailJobSubmittedMessage(submissionId));
	}

	public void sendFinishSuccesfullyEmail(String submissionId) throws MessagingException {
		send(
				emailData.getEmailMessageData().getEmailJobFinishedTitle(submissionId),
				emailData.getEmailMessageData().getEmailJobFinishedMessage(submissionId));
	}

	public void sendFinishWithErrorEmail(String submissionId) throws MessagingException {
		send(
				emailData.getEmailMessageData().getEmailJobErrorTitle(submissionId),
				emailData.getEmailMessageData().getEmailJobErrorMessage(submissionId));
	}

	/**
	 * Sends email in an independent thread so that the server doesn't get blocked
	 * if something goes wrong in the sending.
	 */
	public void sendInSeparateThread(String subject, String bodyText) {
		Executors.newSingleThreadExecutor().execute(() -> {
			try {
				send(subject, bodyText);
			} catch (MessagingException e) {
				logger.error("Could not send email for recipient {}. Email title was: {}. Error: {}", emailData.getEmailRecipient(), subject, e.getMessage());
			}
		});
	}
}
