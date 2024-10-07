package eppic.rest.jobs;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class EmailSenderSmtp {

    private static final Logger logger = LoggerFactory.getLogger(EmailSenderSmtp.class);

    public static void send(String subject, String bodyText, EmailData emailData) throws MessagingException {
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
        simpleMessage.setRecipient(Message.RecipientType.TO, toAddress);
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
}
