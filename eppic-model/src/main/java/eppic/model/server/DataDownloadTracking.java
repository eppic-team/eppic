package eppic.model.server;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="DataDownloadTracking")
public class DataDownloadTracking{
	
	@Id
	@GeneratedValue(strategy=GenerationType.TABLE)
	private long uid;
	
	private String ip;
	private Date downloadDate;
	
	public DataDownloadTracking() {}
	
	public DataDownloadTracking(String ip, Date downloadDate){
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
