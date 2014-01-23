package ch.systemsx.sybit.crkwebui.server.db.model;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="IPForbidden")
public class IPForbidden implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String ip;

	public void setIp(String ip) {
		this.ip = ip;
	}

	@Id
	public String getIp() {
		return ip;
	}
}
