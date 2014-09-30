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
	private String emailSender;
	
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
	 * Retrieves email sender address.
	 * @return email sender address
	 */
	public String getEmailSender() {
		return emailSender;
	}

	/**
	 * Sets email sender address
	 * @param emailSender address of the email used to send emails
	 */
	public void setEmailSender(String emailSender) {
		this.emailSender = emailSender;
	}

	/**
	 * Retrieves password used to send emails.
	 * @return password used to send emails
	 */
	public String getEmailSenderPassword() {
		return emailSenderPassword;
	}

	/**
	 * Sets password used to send emails.
	 * @param emailSenderPassword password used to send emails
	 */
	public void setEmailSenderPassword(String emailSenderPassword) {
		this.emailSenderPassword = emailSenderPassword;
	}

}
