package eppic.rest.jobs;

import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.Message;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.apache.commons.codec.binary.Base64;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Properties;

public class EmailSenderGoogle {

    public static void send(String subject, String bodyText, EmailData emailData) throws MessagingException {

        // see https://developers.google.com/gmail/api/guides/sending

        /* Load pre-authorized user credentials from the environment.
           TODO(developer) - See https://developers.google.com/identity for
            guides on implementing OAuth2 for your application.*/
        GoogleCredentials credentials = GoogleCredentials.getApplicationDefault()
                .createScoped(GmailScopes.GMAIL_SEND);

        // TODO possible way to pass creds???
        //GoogleCredentials mycreds = GoogleCredentials.newBuilder().asdasdsadas;

        HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credentials);

        // Create the gmail API client
        Gmail service = new Gmail.Builder(new NetHttpTransport(),
                GsonFactory.getDefaultInstance(),
                requestInitializer)
                .setApplicationName("Gmail samples")
                .build();

        // Encode as MIME message
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);
        MimeMessage email = new MimeMessage(session);
        email.setFrom(new InternetAddress(emailData.getReplyToAddress()));
        email.addRecipient(jakarta.mail.Message.RecipientType.TO, new InternetAddress(emailData.getEmailRecipient()));
        email.setSubject(subject);
        email.setText(bodyText);

        // Encode and wrap the MIME message into a gmail message
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        email.writeTo(buffer);
        byte[] rawMessageBytes = buffer.toByteArray();
        String encodedEmail = Base64.encodeBase64URLSafeString(rawMessageBytes);
        Message message = new Message();
        message.setRaw(encodedEmail);

        try {
            // Create send message
            message = service.users().messages().send("me", message).execute();
            System.out.println("Message id: " + message.getId());
            //System.out.println(message.toPrettyString());
        } catch (GoogleJsonResponseException e) {
            // TODO(developer) - handle error appropriately
            GoogleJsonError error = e.getDetails();
            if (error.getCode() == 403) {
                System.err.println("Unable to send message: " + e.getDetails());
            } else {
                throw new MessagingException(e.getMessage());
            }
        } catch (IOException e) {
            throw new MessagingException(e.getMessage());
        }

    }
}
