package ch.systemsx.sybit.crkwebui.server.data;

import java.io.Serializable;

/**
 * This class is used to store data necessary to send emails
 * 
 * @author srebniak_a
 * 
 */
public class EmailData implements Serializable 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String emailRecipient;
	private String emailSender;
	private String emailSenderPassword;
	private String port;
	private String host;

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getEmailRecipient() {
		return emailRecipient;
	}

	public void setEmailRecipient(String emailRecipient) {
		this.emailRecipient = emailRecipient;
	}

	public String getEmailSender() {
		return emailSender;
	}

	public void setEmailSender(String emailSender) {
		this.emailSender = emailSender;
	}

	public String getEmailSenderPassword() {
		return emailSenderPassword;
	}

	public void setEmailSenderPassword(String emailSenderPassword) {
		this.emailSenderPassword = emailSenderPassword;
	}

}
