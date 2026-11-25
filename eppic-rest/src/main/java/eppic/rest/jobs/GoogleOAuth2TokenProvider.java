package eppic.rest.jobs;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Obtains short-lived OAuth2 access tokens from Google by exchanging a long-lived refresh token,
 * to be used for authenticating to Gmail's SMTP server with the XOAUTH2 mechanism.
 * Google no longer accepts plain password authentication for SMTP.
 * Access tokens are cached until shortly before they expire.
 */
public class GoogleOAuth2TokenProvider {

	private static final Logger logger = LoggerFactory.getLogger(GoogleOAuth2TokenProvider.class);

	public static final String GOOGLE_TOKEN_URL = "https://oauth2.googleapis.com/token";

	/**
	 * Renew the token this long before it actually expires, to account for clock skew and request time
	 */
	private static final Duration EXPIRY_MARGIN = Duration.ofSeconds(60);

	private static final Duration HTTP_TIMEOUT = Duration.ofSeconds(30);

	private static final ObjectMapper MAPPER = new ObjectMapper();

	private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder().connectTimeout(HTTP_TIMEOUT).build();

	/**
	 * One provider per set of credentials, so that the cached token is shared by all emails sent
	 */
	private static final Map<String, GoogleOAuth2TokenProvider> PROVIDERS = new ConcurrentHashMap<>();

	private final String tokenUrl;
	private final String clientId;
	private final String clientSecret;
	private final String refreshToken;

	private String accessToken;
	private Instant expiresAt;

	GoogleOAuth2TokenProvider(String tokenUrl, String clientId, String clientSecret, String refreshToken) {
		this.tokenUrl = tokenUrl;
		this.clientId = clientId;
		this.clientSecret = clientSecret;
		this.refreshToken = refreshToken;
	}

	/**
	 * Gets the shared provider for the given credentials
	 */
	public static GoogleOAuth2TokenProvider getInstance(String clientId, String clientSecret, String refreshToken) {
		String key = clientId + "\n" + clientSecret + "\n" + refreshToken;
		return PROVIDERS.computeIfAbsent(key, k -> new GoogleOAuth2TokenProvider(GOOGLE_TOKEN_URL, clientId, clientSecret, refreshToken));
	}

	/**
	 * Returns a valid access token, requesting a new one from Google if there is none or it is about to expire.
	 * @throws IOException if the token can't be obtained
	 */
	public synchronized String getAccessToken() throws IOException {
		if (accessToken == null || Instant.now().isAfter(expiresAt.minus(EXPIRY_MARGIN))) {
			refresh();
		}
		return accessToken;
	}

	private void refresh() throws IOException {
		if (isBlank(clientId) || isBlank(clientSecret) || isBlank(refreshToken)) {
			throw new IOException("OAuth2 credentials for sending email are not configured (client id, client secret and refresh token are all needed)");
		}

		String form = Map.of(
				"client_id", clientId,
				"client_secret", clientSecret,
				"refresh_token", refreshToken,
				"grant_type", "refresh_token")
				.entrySet().stream()
				.map(e -> URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8) + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
				.collect(Collectors.joining("&"));

		HttpRequest request = HttpRequest.newBuilder(URI.create(tokenUrl))
				.timeout(HTTP_TIMEOUT)
				.header("Content-Type", "application/x-www-form-urlencoded")
				.POST(HttpRequest.BodyPublishers.ofString(form))
				.build();

		HttpResponse<String> response;
		try {
			response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IOException("Interrupted while requesting OAuth2 access token", e);
		}

		JsonNode json;
		try {
			json = MAPPER.readTree(response.body());
		} catch (IOException e) {
			throw new IOException("Could not parse OAuth2 token response (HTTP " + response.statusCode() + ")", e);
		}

		if (response.statusCode() != 200 || !json.hasNonNull("access_token")) {
			// the error response contains no secrets, e.g. {"error":"invalid_grant","error_description":"Token has been expired or revoked."}
			throw new IOException("Could not obtain OAuth2 access token (HTTP " + response.statusCode() + "): "
					+ json.path("error").asText("") + " " + json.path("error_description").asText(""));
		}

		accessToken = json.get("access_token").asText();
		long expiresIn = json.path("expires_in").asLong(3600);
		expiresAt = Instant.now().plusSeconds(expiresIn);
		logger.info("Obtained new OAuth2 access token for sending email, valid for {} s", expiresIn);
	}

	private static boolean isBlank(String s) {
		return s == null || s.isBlank();
	}
}
