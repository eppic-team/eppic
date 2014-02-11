package ch.systemsx.sybit.crkwebui.server.db.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="DataDownloadIP")
public class DataDownloadIP{
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private long uid;
	
	private String ip;
	private Date downloadDate;
	
	
	public DataDownloadIP(String ip, Date downloadDate){
		this.ip = ip;
		this.downloadDate = downloadDate;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public Date getDownloadDate() {
		return downloadDate;
	}

	public void setDownloadDate(Date downloadDate) {
		this.downloadDate = downloadDate;
	}

}
