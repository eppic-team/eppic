package ch.systemsx.sybit.crkwebui.server.email.managers;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import ch.systemsx.sybit.crkwebui.server.email.data.EmailData;

/**
 * This class is used to send emails.
 * @author srebniak_a
 *
 */
public class EmailSender
{
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
	 */
	public void send(String recipient,
					 String subject,
					 String text)
	{
		if ((recipient != null)
				&& (!recipient.equals("")))
		{
			Properties properties = new Properties();
			properties.put("mail.smtp.host", emailData.getHost());
			properties.put("mail.smtp.port", emailData.getPort());

			Session session = Session.getDefaultInstance(properties);
			Message simpleMessage = new MimeMessage(session);

			InternetAddress fromAddress = null;
			InternetAddress toAddress = null;

			try
			{
				fromAddress = new InternetAddress(emailData.getEmailSender());
				toAddress = new InternetAddress(recipient);
			}
			catch (AddressException e)
			{
				e.printStackTrace();
			}

			try
			{
				simpleMessage.setFrom(fromAddress);
				simpleMessage.setRecipient(RecipientType.TO, toAddress);
				simpleMessage.setSubject(subject);
				simpleMessage.setText(text);
				simpleMessage.saveChanges();

				Transport transport = session.getTransport("smtp");
				transport.connect(properties.getProperty("mail.smtp.host"),
						emailData.getEmailSender(), "");
				transport.sendMessage(simpleMessage,
						simpleMessage.getAllRecipients());
				transport.close();

//				Transport.send(simpleMessage);
			}
			catch (MessagingException e)
			{
				e.printStackTrace();
			}
		}
	}
}
