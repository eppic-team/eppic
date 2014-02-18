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
import ch.systemsx.sybit.crkwebui.server.db.dao.DataDownloadTrackingDAO;
import ch.systemsx.sybit.crkwebui.server.db.dao.ChainClusterDAO;
import ch.systemsx.sybit.crkwebui.server.db.dao.InterfaceClusterDAO;
import ch.systemsx.sybit.crkwebui.server.db.dao.InterfaceDAO;
import ch.systemsx.sybit.crkwebui.server.db.dao.JobDAO;
import ch.systemsx.sybit.crkwebui.server.db.dao.PDBInfoDAO;
import ch.systemsx.sybit.crkwebui.server.db.dao.jpa.DataDownloadTrackingDAOJpa;
import ch.systemsx.sybit.crkwebui.server.db.dao.jpa.ChainClusterDAOJpa;
import ch.systemsx.sybit.crkwebui.server.db.dao.jpa.InterfaceClusterDAOJpa;
import ch.systemsx.sybit.crkwebui.server.db.dao.jpa.InterfaceDAOJpa;
import ch.systemsx.sybit.crkwebui.server.db.dao.jpa.JobDAOJpa;
import ch.systemsx.sybit.crkwebui.server.db.dao.jpa.PDBInfoDAOJpa;
import ch.systemsx.sybit.crkwebui.server.db.data.InputWithType;
import ch.systemsx.sybit.crkwebui.server.files.downloader.generators.JobListWithInterfacesGenerator;
import ch.systemsx.sybit.crkwebui.server.files.downloader.validators.DataDownloadServletInputValidator;
import ch.systemsx.sybit.crkwebui.server.ip.validators.IPVerifier;
import ch.systemsx.sybit.crkwebui.shared.exceptions.DaoException;
import ch.systemsx.sybit.crkwebui.shared.exceptions.ValidationException;
import ch.systemsx.sybit.crkwebui.shared.model.ChainCluster;
import ch.systemsx.sybit.crkwebui.shared.model.Interface;
import ch.systemsx.sybit.crkwebui.shared.model.InterfaceCluster;
import ch.systemsx.sybit.crkwebui.shared.model.PdbInfo;

/**
 * Servlet used to download results in xml format
 * @author biyani_n
 *
 */
@PersistenceContext(name="eppicjpa", unitName="eppicjpa")
public class DataDownloadServlet extends BaseServlet{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private List<PdbInfo> pdbList;
	
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
			
			pdbList = new ArrayList<PdbInfo>();

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
		DataDownloadTrackingDAO downloadDAO = new DataDownloadTrackingDAOJpa();
		downloadDAO.insertNewIP(ip, new Date());
	}
	
	/**
	 * Retrieves pdb score item for job.
	 * @param jobId identifier of the job
	 * @param interfaceIdList list of interface ids to be retrieved (null for everything)
	 * @return pdb score item
	 * @throws Exception when can not retrieve result of the job
	 */
	private PdbInfo getResultData(String jobId, List<Integer> interfaceIdList, String getSeqInfo) throws Exception
	{
		JobDAO jobDAO = new JobDAOJpa();
		InputWithType input = jobDAO.getInputWithTypeForJob(jobId);

		PDBInfoDAO pdbInfoDAO = new PDBInfoDAOJpa();
		PdbInfo pdbInfo = pdbInfoDAO.getPDBScore(jobId);
		
		InterfaceClusterDAO clusterDAO = new InterfaceClusterDAOJpa();
		List<InterfaceCluster> clusters = clusterDAO.getInterfaceClustersWithoutInterfaces(pdbInfo.getUid());
		pdbInfo.setInterfaceClusters(clusters);

		InterfaceDAO interfaceDAO = new InterfaceDAOJpa();
		for(InterfaceCluster cluster: clusters){
			List<Interface> interfaceItems;
			if(interfaceIdList != null){
				interfaceItems = interfaceDAO.getInterfacesWithScores(cluster.getUid(), interfaceIdList);
			}
			else{
				interfaceItems = interfaceDAO.getInterfacesWithScores(cluster.getUid());
			}
			cluster.setInterfaces(interfaceItems);
		}

		if(getSeqInfo == null || getSeqInfo.equals("t")){	
			ChainClusterDAO chainClusterDAO = new ChainClusterDAOJpa();
			List<ChainCluster> chainClusters = chainClusterDAO.getChainClusters(pdbInfo.getUid());
			pdbInfo.setChainClusters(chainClusters);
		}
		
		pdbInfo.setInputType(input.getInputType());
		pdbInfo.setInputName(input.getInputName());
		
		return pdbInfo;
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
			JAXBContext jaxbContext = JAXBContext.newInstance(PdbInfo.class);
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

			// for getting nice formatted output
			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			jaxbMarshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);

			// Writing to console
			writer.append("<eppicAnalysisList>");
			
			for(PdbInfo pdb:pdbList){
				jaxbMarshaller.marshal(pdb, writer);
			}

			writer.append("</eppicAnalysisList>");

		} catch (JAXBException e) {
			// some exception occured
			e.printStackTrace();
		}
	}
}
