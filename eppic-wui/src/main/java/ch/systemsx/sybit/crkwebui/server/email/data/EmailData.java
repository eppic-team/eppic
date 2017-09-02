package ch.systemsx.sybit.crkwebui.server.email.data;

import java.io.Serializable;

/**
 * Email sender data.
 * @author srebniak_a
 * 
 */
public class EmailData implements Serializable 
{
	/**
	 * 
	 */
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
	 * Sender password.
	 */
	private String emailSenderPassword;
	
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

	/**
	 * Retrieves password used for smtp authentication
	 * @return password used to send emails
	 */
	public String getEmailSenderPassword() {
		return emailSenderPassword;
	}

	/**
	 * Sets password used for smtp authentication
	 * @param emailSenderPassword password used to send emails
	 */
	public void setEmailSenderPassword(String emailSenderPassword) {
		this.emailSenderPassword = emailSenderPassword;
	}

	public String getReplyToAddress() {
		return replyToAddress;
	}

	public void setReplyToAddress(String replyToAddress) {
		this.replyToAddress = replyToAddress;
	}

	
}
