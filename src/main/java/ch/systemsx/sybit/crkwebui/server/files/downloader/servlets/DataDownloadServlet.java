package ch.systemsx.sybit.crkwebui.server.files.downloader.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.PersistenceContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import ch.systemsx.sybit.crkwebui.server.commons.servlets.BaseServlet;
import ch.systemsx.sybit.crkwebui.server.db.dao.DataDownloadIPDAO;
import ch.systemsx.sybit.crkwebui.server.db.dao.HomologsInfoItemDAO;
import ch.systemsx.sybit.crkwebui.server.db.dao.InterfaceItemDAO;
import ch.systemsx.sybit.crkwebui.server.db.dao.JobDAO;
import ch.systemsx.sybit.crkwebui.server.db.dao.PDBScoreDAO;
import ch.systemsx.sybit.crkwebui.server.db.dao.jpa.DataDownloadIPDAOJpa;
import ch.systemsx.sybit.crkwebui.server.db.dao.jpa.HomologsInfoItemDAOJpa;
import ch.systemsx.sybit.crkwebui.server.db.dao.jpa.InterfaceItemDAOJpa;
import ch.systemsx.sybit.crkwebui.server.db.dao.jpa.JobDAOJpa;
import ch.systemsx.sybit.crkwebui.server.db.dao.jpa.PDBScoreDAOJpa;
import ch.systemsx.sybit.crkwebui.server.files.downloader.generators.JobListWithInterfacesGenerator;
import ch.systemsx.sybit.crkwebui.server.files.downloader.validators.DataDownloadServletInputValidator;
import ch.systemsx.sybit.crkwebui.server.ip.validators.IPVerifier;
import ch.systemsx.sybit.crkwebui.shared.exceptions.DaoException;
import ch.systemsx.sybit.crkwebui.shared.exceptions.ValidationException;
import ch.systemsx.sybit.crkwebui.shared.model.HomologsInfoItem;
import ch.systemsx.sybit.crkwebui.shared.model.InterfaceItem;
import ch.systemsx.sybit.crkwebui.shared.model.PDBScoreItem;

/**
 * Servlet used to download results in xml format
 * @author biyani_n
 *
 */
@PersistenceContext(name="crkjpa", unitName="crkjpa")
public class DataDownloadServlet extends BaseServlet{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private List<PDBScoreItem> pdbList;
	
	//Parameters
	private int maxNumJobIds;
	private int defaultNrOfAllowedSubmissionsForIP;

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		
		maxNumJobIds = Integer.parseInt(properties.getProperty("max_jobs_in_one_call","1"));
		defaultNrOfAllowedSubmissionsForIP = Integer.parseInt(properties.getProperty("nr_of_allowed_submissions_for_ip","100"));
	
	}

	/**
	 * Returns file specified by the parameters.
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException
			{
		String type = request.getParameter("type");
		String jobIdCommaSep = request.getParameter("id");
		String getSeqInfo = request.getParameter("getSeqInfo");
		
		String requestIP = request.getRemoteAddr();

		try
		{	
			addIPToDB(requestIP);
			
			Map<String, List<Integer>> jobIdMap = JobListWithInterfacesGenerator.generateJobList(jobIdCommaSep);
			
			DataDownloadServletInputValidator.validateFileDownloadInput(type, jobIdMap, getSeqInfo, maxNumJobIds);
			
			IPVerifier.verifyIfCanBeSubmitted(requestIP, 
										      defaultNrOfAllowedSubmissionsForIP, 
										      true);
			
			pdbList = new ArrayList<PDBScoreItem>();

			for(String jobId: jobIdMap.keySet()){
				pdbList.add(getResultData(jobId, jobIdMap.get(jobId), getSeqInfo));
			}

			createXMLResponse(response);

		}
		catch(ValidationException e)
		{
			response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED, "Input values are incorrect: " + e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Inserts the ip to the DB
	 * @param ip
	 * @throws DaoException
	 */
	private void addIPToDB(String ip) throws DaoException{
		DataDownloadIPDAO downloadDAO = new DataDownloadIPDAOJpa();
		downloadDAO.insertNewIP(ip, new Date());
	}
	
	/**
	 * Retrieves pdb score item for job.
	 * @param jobId identifier of the job
	 * @param interfaceIdList list of interface ids to be retrieved (null for everything)
	 * @return pdb score item
	 * @throws Exception when can not retrieve result of the job
	 */
	private PDBScoreItem getResultData(String jobId, List<Integer> interfaceIdList, String getSeqInfo) throws Exception
	{
		JobDAO jobDAO = new JobDAOJpa();
		int inputType = jobDAO.getInputTypeForJob(jobId);

		PDBScoreDAO pdbScoreDAO = new PDBScoreDAOJpa();
		PDBScoreItem pdbScoreItem = pdbScoreDAO.getPDBScore(jobId);

		InterfaceItemDAO interfaceItemDAO = new InterfaceItemDAOJpa();
		List<InterfaceItem> interfaceItems;
		if(interfaceIdList != null)
			interfaceItems = interfaceItemDAO.getInterfacesWithScores(pdbScoreItem.getUid(), interfaceIdList);
		else
			interfaceItems = interfaceItemDAO.getInterfacesWithScores(pdbScoreItem.getUid());
		
		pdbScoreItem.setInterfaceItems(interfaceItems);

		if(getSeqInfo == null || getSeqInfo.equals("t")){	
			HomologsInfoItemDAO homologsInfoItemDAO = new HomologsInfoItemDAOJpa();
			List<HomologsInfoItem> homologsInfoItems = homologsInfoItemDAO.getHomologsInfoItems(pdbScoreItem.getUid());
			pdbScoreItem.setHomologsInfoItems(homologsInfoItems);
		}
		
		pdbScoreItem.setInputType(inputType);

		return pdbScoreItem;
	}

	/**
	 * converts the contents of the class to xml file
	 * @param response to write xml file
	 * @throws IOException 
	 */
	public void createXMLResponse(HttpServletResponse response) throws IOException{

		if(pdbList == null) return;

		try {

			response.setContentType("text/xml");
			response.setCharacterEncoding("UTF-8");
			
			PrintWriter writer = response.getWriter();

			// create JAXB context and initializing Marshaller
			JAXBContext jaxbContext = JAXBContext.newInstance(PDBScoreItem.class);
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

			// for getting nice formatted output
			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			jaxbMarshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);

			// Writing to console
			writer.append("<eppicAnalysisList>");
			
			for(PDBScoreItem pdb:pdbList){
				jaxbMarshaller.marshal(pdb, writer);
			}

			writer.append("</eppicAnalysisList>");

		} catch (JAXBException e) {
			// some exception occured
			e.printStackTrace();
		}
	}
}
