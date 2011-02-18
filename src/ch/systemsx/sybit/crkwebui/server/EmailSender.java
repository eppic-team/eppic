package ch.systemsx.sybit.crkwebui.server;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import ch.systemsx.sybit.crkwebui.server.data.EmailData;

public class EmailSender 
{
	private EmailData emailData;

	public EmailSender(EmailData emailData)
	{
		this.emailData = emailData;
	}

	public void send(String subject, String text) 
	{
		if ((emailData.getEmailRecipient() != null)
				&& (!emailData.getEmailRecipient().equals(""))) 
		{
			Properties props = new Properties();
			props.put("mail.smtp.host", emailData.getHost());
			props.put("mail.smtp.port", emailData.getPort());

			Session session = Session.getDefaultInstance(props);
			Message simpleMessage = new MimeMessage(session);

			InternetAddress fromAddress = null;
			InternetAddress toAddress = null;
			
			try 
			{
				fromAddress = new InternetAddress(emailData.getEmailSender());
				toAddress = new InternetAddress(emailData.getEmailRecipient());
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

//				Transport transport = session.getTransport("smtp");
//				transport.connect(props.getProperty("mail.smtp.host"),
//						emailData.getEmailSender(), "");
//				transport.sendMessage(simpleMessage,
//						simpleMessage.getAllRecipients());
//				transport.close();

				Transport.send(simpleMessage);
			}
			catch (MessagingException e) 
			{
				e.printStackTrace();
			}
		}
	}
}
