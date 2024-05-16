package eppic.rest.jobs;

import java.util.Properties;
import java.util.concurrent.Executors;

import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

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

		Properties properties = new Properties();
		properties.put("mail.smtp.host", emailData.getHost());
		properties.put("mail.smtp.port", emailData.getPort());
		// this seems to be needed for google's smtp server - JD 2017-09-01
		// https://stackoverflow.com/questions/67556270/javax-net-ssl-sslhandshakeexception-no-appropriate-protocol-protocol-is-disabl
		properties.put("mail.smtp.auth", "true");
		properties.put("mail.smtp.socketFactory.port", emailData.getPort());
		properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		properties.put("mail.smtp.socketFactory.fallback", "false");
		properties.put("mail.smtp.starttls.enable", "true");
		properties.put("mail.smtp.starttls.required", "true");
		properties.put("mail.smtp.ssl.protocols", "TLSv1.2");


		Session session = Session.getDefaultInstance(properties, new Authenticator() {
			// this seems to be needed for google's smtp server - JD 2017-09-01
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(emailData.getEmailSenderUserName(), emailData.getEmailSenderPassword());
			}
		});
		Message simpleMessage = new MimeMessage(session);

		InternetAddress toAddress = new InternetAddress(emailData.getEmailRecipient());
		Address[] replyTos = {new InternetAddress(emailData.getReplyToAddress())};

		simpleMessage.setReplyTo(replyTos);
		simpleMessage.setRecipient(RecipientType.TO, toAddress);
		simpleMessage.setSubject(subject);
		simpleMessage.setText(bodyText);
		simpleMessage.saveChanges();

		Transport transport = session.getTransport("smtp");
		// apparently the password is not needed here, don't know why -JD 2017-09-01
		transport.connect(properties.getProperty("mail.smtp.host"),
				emailData.getEmailSenderUserName(), "");
		transport.sendMessage(simpleMessage,
				simpleMessage.getAllRecipients());
		transport.close();

		logger.info("Successfully sent email for recipient {} with subject '{}'", emailData.getEmailRecipient(), subject);
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
