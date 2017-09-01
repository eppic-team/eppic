package ch.systemsx.sybit.crkwebui.server.email.managers;

import java.util.Properties;
import java.util.concurrent.Executors;

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

import ch.systemsx.sybit.crkwebui.server.email.data.EmailData;

/**
 * This class is used to send emails.
 * @author srebniak_a
 *
 */
public class EmailSender
{
	private static final Logger logger = LoggerFactory.getLogger(EmailSender.class);
	
	private EmailData emailData;

	public EmailSender(EmailData emailData)
	{
		this.emailData = emailData;
	}

	/**
	 * Sends email.
	 * @param recipient email recipient
	 * @param subject subject of the email
	 * @param text content of the email
	 * @throws MessagingException 
	 */
	public void send(String recipient,
					 String subject,
					 String text) throws MessagingException {
		
		if ((recipient != null)
				&& (!recipient.equals(""))) {
			
			Properties properties = new Properties();
			properties.put("mail.smtp.host", emailData.getHost());
			properties.put("mail.smtp.port", emailData.getPort());
			// this seems to be needed for google's smtp server - JD 2017-09-01
			properties.put("mail.smtp.auth", "true");
			properties.put("mail.smtp.socketFactory.port", emailData.getPort());
			properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
			properties.put("mail.smtp.socketFactory.fallback", "false");
			properties.put("mail.smtp.starttls.enable", "true");


			Session session = Session.getDefaultInstance(properties, new Authenticator() {
				// this seems to be needed for google's smtp server - JD 2017-09-01
                @Override                
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(emailData.getEmailSenderUserName(), emailData.getEmailSenderPassword());
                }
            });
			Message simpleMessage = new MimeMessage(session);

			InternetAddress fromAddress = null;
			InternetAddress toAddress = null;

			fromAddress = new InternetAddress(emailData.getFromAdress());
			toAddress = new InternetAddress(recipient);

			simpleMessage.setFrom(fromAddress);
			simpleMessage.setRecipient(RecipientType.TO, toAddress);
			simpleMessage.setSubject(subject);
			simpleMessage.setText(text);
			simpleMessage.saveChanges();

			Transport transport = session.getTransport("smtp");
			// apparently the password is not needed here, don't know why -JD 2017-09-01
			transport.connect(properties.getProperty("mail.smtp.host"),
					emailData.getEmailSenderUserName(), "");
			transport.sendMessage(simpleMessage,
					simpleMessage.getAllRecipients());
			transport.close();
			
		}
	}
	
	/**
	 * Sends email in an independent thread so that the server doesn't get blocked
	 * if something goes wrong in the sending.
	 * @param recipient
	 * @param subject
	 * @param text
	 */
	public void sendInSeparateThread(String recipient, String subject, String text) {
		Executors.newSingleThreadExecutor().execute(new Runnable() {
		    @Override
		    public void run() {
		    	try {
		    		send(recipient, subject, text);
		    	} catch (MessagingException e) {
					logger.error("Could not send email for recipient {}. Email title was: {}. Error: {}", recipient, subject, e.getMessage());
				}
		    }
		});
	}
}
