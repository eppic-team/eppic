package eppic.rest.jobs;

import java.io.IOException;
import java.util.Properties;

import jakarta.mail.Address;
import jakarta.mail.Message;
import jakarta.mail.Message.RecipientType;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is used to send emails, via Gmail's SMTP server authenticating with OAuth2 (XOAUTH2).
 * The same setup as nodemailer's gmail service with OAuth2 credentials.
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

		String accessToken;
		try {
			accessToken = GoogleOAuth2TokenProvider.getInstance(
					emailData.getOauth2ClientId(),
					emailData.getOauth2ClientSecret(),
					emailData.getOauth2RefreshToken())
					.getAccessToken();
		} catch (IOException e) {
			throw new MessagingException("Could not get OAuth2 access token for sending email: " + e.getMessage(), e);
		}

		Session session = Session.getInstance(getSmtpProperties(emailData));

		Message simpleMessage = new MimeMessage(session);

		InternetAddress toAddress = new InternetAddress(emailData.getEmailRecipient());
		Address[] replyTos = {new InternetAddress(emailData.getReplyToAddress())};

		// the sender must be the authenticated account, otherwise gmail rewrites it
		simpleMessage.setFrom(new InternetAddress(emailData.getEmailSenderUserName()));
		simpleMessage.setReplyTo(replyTos);
		simpleMessage.setRecipient(RecipientType.TO, toAddress);
		simpleMessage.setSubject(subject);
		simpleMessage.setText(bodyText);
		simpleMessage.saveChanges();

		try (Transport transport = session.getTransport("smtp")) {
			// with XOAUTH2 the access token goes in place of the password
			transport.connect(emailData.getHost(), Integer.parseInt(emailData.getPort()),
					emailData.getEmailSenderUserName(), accessToken);
			transport.sendMessage(simpleMessage, simpleMessage.getAllRecipients());
		}

		logger.info("Successfully sent email for recipient {} with subject '{}'", emailData.getEmailRecipient(), subject);
	}

	static Properties getSmtpProperties(EmailData emailData) {
		Properties properties = new Properties();
		properties.put("mail.smtp.host", emailData.getHost());
		properties.put("mail.smtp.port", emailData.getPort());
		properties.put("mail.smtp.auth", "true");
		properties.put("mail.smtp.auth.mechanisms", "XOAUTH2");
		// port 465 is implicit TLS (SMTPS), 587 is plain connection upgraded with STARTTLS
		if ("465".equals(emailData.getPort())) {
			properties.put("mail.smtp.ssl.enable", "true");
		} else {
			properties.put("mail.smtp.starttls.enable", "true");
			properties.put("mail.smtp.starttls.required", "true");
		}
		properties.put("mail.smtp.ssl.checkserveridentity", "true");
		properties.put("mail.smtp.connectiontimeout", "30000");
		properties.put("mail.smtp.timeout", "30000");
		properties.put("mail.smtp.writetimeout", "30000");
		return properties;
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
}
