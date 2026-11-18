package eppic.rest.jobs;

import com.sun.net.httpserver.HttpServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

public class TestGoogleOAuth2TokenProvider {

    private HttpServer server;
    private String tokenUrl;
    private final AtomicInteger numRequests = new AtomicInteger();
    private final AtomicReference<String> lastRequestBody = new AtomicReference<>();
    private volatile int status = 200;
    private volatile String responseBody = "{\"access_token\":\"the-token\",\"expires_in\":3599,\"token_type\":\"Bearer\"}";

    @Before
    public void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/token", exchange -> {
            numRequests.incrementAndGet();
            lastRequestBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            byte[] body = responseBody.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(status, body.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body);
            }
        });
        server.start();
        tokenUrl = "http://127.0.0.1:" + server.getAddress().getPort() + "/token";
    }

    @After
    public void tearDown() {
        server.stop(0);
    }

    @Test
    public void testGetsAndCachesToken() throws IOException {
        GoogleOAuth2TokenProvider provider = new GoogleOAuth2TokenProvider(tokenUrl, "my-client", "my secret&", "my-refresh");
        assertEquals("the-token", provider.getAccessToken());
        assertEquals("the-token", provider.getAccessToken());
        assertEquals(1, numRequests.get());

        String body = lastRequestBody.get();
        assertTrue(body.contains("grant_type=refresh_token"));
        assertTrue(body.contains("client_id=my-client"));
        assertTrue(body.contains("client_secret=my+secret%26"));
        assertTrue(body.contains("refresh_token=my-refresh"));
    }

    @Test
    public void testRefreshesExpiredToken() throws IOException {
        responseBody = "{\"access_token\":\"short-lived\",\"expires_in\":10}";
        GoogleOAuth2TokenProvider provider = new GoogleOAuth2TokenProvider(tokenUrl, "c", "s", "r");
        provider.getAccessToken();
        // expiry is within the safety margin, so a new token must be requested
        provider.getAccessToken();
        assertEquals(2, numRequests.get());
    }

    @Test
    public void testErrorResponse() {
        status = 400;
        responseBody = "{\"error\":\"invalid_grant\",\"error_description\":\"Token has been expired or revoked.\"}";
        GoogleOAuth2TokenProvider provider = new GoogleOAuth2TokenProvider(tokenUrl, "c", "s", "r");
        IOException e = assertThrows(IOException.class, provider::getAccessToken);
        assertTrue(e.getMessage().contains("invalid_grant"));
        assertTrue(e.getMessage().contains("400"));
    }

    @Test
    public void testMissingCredentials() {
        GoogleOAuth2TokenProvider provider = new GoogleOAuth2TokenProvider(tokenUrl, "c", "", "r");
        assertThrows(IOException.class, provider::getAccessToken);
        assertEquals(0, numRequests.get());
    }

    @Test
    public void testSmtpProperties() {
        EmailData data = new EmailData();
        data.setHost("smtp.gmail.com");
        data.setPort("465");
        Properties props = EmailSender.getSmtpProperties(data);
        assertEquals("XOAUTH2", props.getProperty("mail.smtp.auth.mechanisms"));
        assertEquals("true", props.getProperty("mail.smtp.ssl.enable"));
        assertNull(props.getProperty("mail.smtp.starttls.enable"));

        data.setPort("587");
        props = EmailSender.getSmtpProperties(data);
        assertNull(props.getProperty("mail.smtp.ssl.enable"));
        assertEquals("true", props.getProperty("mail.smtp.starttls.required"));
    }
}
