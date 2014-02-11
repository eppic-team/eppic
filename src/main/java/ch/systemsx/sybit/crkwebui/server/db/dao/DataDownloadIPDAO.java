package ch.systemsx.sybit.crkwebui.server.db.dao;

import java.util.Date;

import ch.systemsx.sybit.crkwebui.shared.exceptions.DaoException;

/**
 * interface for data download ip
 * @author biyani_n
 *
 */
public interface DataDownloadIPDAO {

	/**
	 * persists a new entry
	 * @param ip
	 * @param downloadDate
	 * @throws DaoException
	 */
	public void insertNewIP(String ip, Date downloadDate) throws DaoException;
	
	/**
	 * Retrieves number of jobs for specified ip address during last day.
	 * @param ip ip address
	 * @return number of jobs for specified ip address during last day
	 * @throws DaoException when can not retrieve information from data storage
	 */
	public Long getNrOfDownloadsForIPDuringLastDay(String ip) throws DaoException;
	
	/**
	 * Retrieves oldest job download date during the last day for specified ip address.
	 * @param ip ip address
	 * @return oldest job download date during the last day for specified ip address
	 * @throws DaoException when can not retrieve information from data storage
	 */
	public Date getOldestJobDownloadDateDuringLastDay(String ip) throws DaoException;
	
}
