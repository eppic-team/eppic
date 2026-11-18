package eppic.rest.jobs;

import org.junit.Test;

import static org.junit.Assert.*;

public class TestEmailData {

    @Test
    public void testForRecipient() {
        EmailData common = new EmailData();
        common.setHost("smtp.example.org");
        common.setPort("465");
        common.setEmailSenderUserName("sender");
        common.setOauth2ClientId("client-id");
        common.setOauth2ClientSecret("client-secret");
        common.setOauth2RefreshToken("refresh-token");
        common.setReplyToAddress("noreply@example.org");
        EmailMessageData messageData = new EmailMessageData();
        common.setEmailMessageData(messageData);

        EmailData first = common.forRecipient("first@example.org");
        EmailData second = common.forRecipient("second@example.org");
        EmailData none = common.forRecipient(null);

        // a later submission must not change the recipient of an earlier one
        assertEquals("first@example.org", first.getEmailRecipient());
        assertEquals("second@example.org", second.getEmailRecipient());
        assertNull(none.getEmailRecipient());
        assertNull(common.getEmailRecipient());

        for (EmailData copy : new EmailData[] {first, second, none}) {
            assertEquals("smtp.example.org", copy.getHost());
            assertEquals("465", copy.getPort());
            assertEquals("sender", copy.getEmailSenderUserName());
            assertEquals("client-id", copy.getOauth2ClientId());
            assertEquals("client-secret", copy.getOauth2ClientSecret());
            assertEquals("refresh-token", copy.getOauth2RefreshToken());
            assertEquals("noreply@example.org", copy.getReplyToAddress());
            assertSame(messageData, copy.getEmailMessageData());
        }
    }
}
