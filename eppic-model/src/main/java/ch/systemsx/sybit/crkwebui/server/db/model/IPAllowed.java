package ch.systemsx.sybit.crkwebui.server.db.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="IPAllowed")
public class IPAllowed 
{
	private String ip;
	private int nrOfAllowedSubmission;

	public void setIp(String ip) {
		this.ip = ip;
	}

	@Id
	public String getIp() {
		return ip;
	}
	
	public void setNrOfAllowedSubmission(int nrOfAllowedSubmission) {
		this.nrOfAllowedSubmission = nrOfAllowedSubmission;
	}

	public int getNrOfAllowedSubmission() {
		return nrOfAllowedSubmission;
	}
}
