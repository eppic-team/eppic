package eppic.rest.jobs;

import java.io.Serializable;

/**
 * Email sender data.
 * @author srebniak_a
 *
 */
public class EmailData implements Serializable
{
	private static final long serialVersionUID = 1L;

	/**
	 * Recipient address.
	 */
	private String emailRecipient;

	/**
	 * Sender address.
	 */
	private String emailSenderUserName;

	/**
	 * OAuth2 client id of the Google app used to send emails.
	 */
	private String oauth2ClientId;

	/**
	 * OAuth2 client secret of the Google app used to send emails.
	 */
	private String oauth2ClientSecret;

	/**
	 * OAuth2 refresh token for the sender account, used to obtain access tokens.
	 */
	private String oauth2RefreshToken;

	/**
	 * SMTP port.
	 */
	private String port;

	/**
	 * SMTP host.
	 */
	private String host;

	/**
	 * The reply-to address for sending emails.
	 */
	private String replyToAddress;

	private EmailMessageData emailMessageData;

	/**
	 * Returns a copy of this email data with the given recipient. This object is not modified, so that
	 * it can hold the settings common to all jobs whilst every job gets its own recipient.
	 * @param emailRecipient the recipient address, null for no recipient
	 * @return a new object with the same settings as this one and the given recipient
	 */
	public EmailData forRecipient(String emailRecipient) {
		EmailData copy = new EmailData();
		copy.emailRecipient = emailRecipient;
		copy.emailSenderUserName = emailSenderUserName;
		copy.oauth2ClientId = oauth2ClientId;
		copy.oauth2ClientSecret = oauth2ClientSecret;
		copy.oauth2RefreshToken = oauth2RefreshToken;
		copy.port = port;
		copy.host = host;
		copy.replyToAddress = replyToAddress;
		copy.emailMessageData = emailMessageData;
		return copy;
	}

	/**
	 * Retrieves SMTP port.
	 * @return SMTP port
	 */
	public String getPort() {
		return port;
	}

	/**
	 * Sets SMTP port.
	 * @param port SMTP port
	 */
	public void setPort(String port) {
		this.port = port;
	}

	/**
	 * Retrieves SMTP host.
	 * @return SMTP host
	 */
	public String getHost() {
		return host;
	}

	/**
	 * Sets SMTP host.
	 * @param host SMTP host
	 */
	public void setHost(String host) {
		this.host = host;
	}

	/**
	 * Retrieves recipient address.
	 * @return recipient address
	 */
	public String getEmailRecipient() {
		return emailRecipient;
	}

	/**
	 * Sets recipient address.
	 * @param emailRecipient recipient address
	 */
	public void setEmailRecipient(String emailRecipient) {
		this.emailRecipient = emailRecipient;
	}

	/**
	 * Retrieves email sender user name for smtp authentication
	 * @return email sender address
	 */
	public String getEmailSenderUserName() {
		return emailSenderUserName;
	}

	/**
	 * Sets email sender user name for smtp authentication
	 * @param emailSender address of the email used to send emails
	 */
	public void setEmailSenderUserName(String emailSender) {
		this.emailSenderUserName = emailSender;
	}

	public String getOauth2ClientId() {
		return oauth2ClientId;
	}

	public void setOauth2ClientId(String oauth2ClientId) {
		this.oauth2ClientId = oauth2ClientId;
	}

	public String getOauth2ClientSecret() {
		return oauth2ClientSecret;
	}

	public void setOauth2ClientSecret(String oauth2ClientSecret) {
		this.oauth2ClientSecret = oauth2ClientSecret;
	}

	public String getOauth2RefreshToken() {
		return oauth2RefreshToken;
	}

	public void setOauth2RefreshToken(String oauth2RefreshToken) {
		this.oauth2RefreshToken = oauth2RefreshToken;
	}

	public String getReplyToAddress() {
		return replyToAddress;
	}

	public void setReplyToAddress(String replyToAddress) {
		this.replyToAddress = replyToAddress;
	}

	public EmailMessageData getEmailMessageData() {
		return emailMessageData;
	}

	public void setEmailMessageData(EmailMessageData emailMessageData) {
		this.emailMessageData = emailMessageData;
	}

	@Override
	public String toString() {
		return "EmailData{" +
				"emailRecipient='" + emailRecipient + '\'' +
				", emailSenderUserName='" + emailSenderUserName + '\'' +
				", oauth2ClientId='" + oauth2ClientId + '\'' +
				", oauth2ClientSecret='" + mask(oauth2ClientSecret) + '\'' +
				", oauth2RefreshToken='" + mask(oauth2RefreshToken) + '\'' +
				", port='" + port + '\'' +
				", host='" + host + '\'' +
				", replyToAddress='" + replyToAddress + '\'' +
				", emailMessageData=" + emailMessageData +
				'}';
	}

	private static String mask(String secret) {
		return secret == null || secret.isEmpty() ? "<not set>" : "*****";
	}
}
