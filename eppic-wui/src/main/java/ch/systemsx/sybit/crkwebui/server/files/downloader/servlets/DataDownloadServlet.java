package ch.systemsx.sybit.crkwebui.server.files.downloader.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.systemsx.sybit.crkwebui.server.commons.servlets.BaseServlet;
import ch.systemsx.sybit.crkwebui.server.db.dao.DataDownloadTrackingDAO;
import ch.systemsx.sybit.crkwebui.server.db.dao.AssemblyDAO;
import ch.systemsx.sybit.crkwebui.server.db.dao.ChainClusterDAO;
import ch.systemsx.sybit.crkwebui.server.db.dao.InterfaceClusterDAO;
import ch.systemsx.sybit.crkwebui.server.db.dao.InterfaceDAO;
import ch.systemsx.sybit.crkwebui.server.db.dao.JobDAO;
import ch.systemsx.sybit.crkwebui.server.db.dao.PDBInfoDAO;
import ch.systemsx.sybit.crkwebui.server.db.dao.jpa.DataDownloadTrackingDAOJpa;
import ch.systemsx.sybit.crkwebui.server.db.dao.jpa.AssemblyDAOJpa;
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
import ch.systemsx.sybit.crkwebui.shared.model.Assembly;
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
	 * The servlet name, note that the name is defined in the web.xml file.
	 */
	public static final String SERVLET_NAME = "dataDownload";
	
	private static final long serialVersionUID = 1L;
	
	private static final Logger logger = LoggerFactory.getLogger(DataDownloadServlet.class);
		
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

		logger.info("XML data download requested for '{}'",jobIdCommaSep);
		
		try
		{	
			addIPToDB(requestIP);
			
			Map<String, List<Integer>> jobIdMap = JobListWithInterfacesGenerator.generateJobList(jobIdCommaSep);
			
			DataDownloadServletInputValidator.validateFileDownloadInput(type, jobIdMap, getSeqInfo, maxNumJobIds);
			
			IPVerifier.verifyIfCanBeSubmitted(requestIP, 
										      defaultNrOfAllowedSubmissionsForIP, 
										      true);
			
			List<PdbInfo> pdbList = new ArrayList<PdbInfo>();

			for(String jobId: jobIdMap.keySet()){
				pdbList.add(getResultData(jobId, jobIdMap.get(jobId), getSeqInfo));
			}

			createXMLResponse(response, pdbList);

		}
		catch(ValidationException e) {
			response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED, "Input values are incorrect: " + e.getMessage());
		} catch (DaoException e) {
			throw new ServletException(e);
		} catch (JAXBException e) {
			throw new ServletException(e);
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
	 * Retrieves pdbInfo item for job.
	 * @param jobId identifier of the job
	 * @param interfaceIdList list of interface ids to be retrieved (null for everything)
	 * @return pdb info item
	 * @throws DaoException when can not retrieve result of the job
	 */
	private PdbInfo getResultData(String jobId, List<Integer> interfaceIdList, String getSeqInfo) throws DaoException
	{
		JobDAO jobDAO = new JobDAOJpa();
		InputWithType input = jobDAO.getInputWithTypeForJob(jobId);

		PDBInfoDAO pdbInfoDAO = new PDBInfoDAOJpa();
		PdbInfo pdbInfo = pdbInfoDAO.getPDBInfo(jobId);
		
		InterfaceClusterDAO clusterDAO = new InterfaceClusterDAOJpa();
		List<InterfaceCluster> clusters = clusterDAO.getInterfaceClustersWithoutInterfaces(pdbInfo.getUid());

		InterfaceDAO interfaceDAO = new InterfaceDAOJpa();
		for(InterfaceCluster cluster: clusters){
			logger.debug("Getting data for interface cluster uid {}", cluster.getUid());
			List<Interface> interfaceItems;
			if(interfaceIdList != null){
				logger.debug("Interface id list requested: {}", interfaceIdList.toString()); 
				interfaceItems = interfaceDAO.getInterfacesWithResidues(cluster.getUid(), interfaceIdList);
			}
			else{
				interfaceItems = interfaceDAO.getInterfacesWithResidues(cluster.getUid());
			}
			cluster.setInterfaces(interfaceItems);
		}

		// now we remove interface clusters with no interfaces, which can happen when interfaceIdList is provided
		if (interfaceIdList!=null) {
			Iterator<InterfaceCluster> it = clusters.iterator();
			while (it.hasNext()) {
				InterfaceCluster cluster = it.next();
				if (cluster.getInterfaces().size()==0) {
					it.remove();
					logger.debug("Removing cluster uid="+cluster.getUid()+", clusterId="+cluster.getClusterId()+" since none of its interfaces was requested");
				}
			}
		}
		
		pdbInfo.setInterfaceClusters(clusters);

		if(getSeqInfo == null || getSeqInfo.equals("t")){	
			ChainClusterDAO chainClusterDAO = new ChainClusterDAOJpa();
			List<ChainCluster> chainClusters = chainClusterDAO.getChainClusters(pdbInfo.getUid());
			pdbInfo.setChainClusters(chainClusters);
		}
		
		pdbInfo.setInputType(input.getInputType());
		pdbInfo.setInputName(input.getInputName());
		
		
		// assemblies info
		AssemblyDAO assemblyDAO = new AssemblyDAOJpa();
		
		List<Assembly> assemblies = assemblyDAO.getAssemblies(pdbInfo.getUid());
		pdbInfo.setAssemblies(assemblies);
		
		return pdbInfo;
	}

	/**
	 * converts the contents of the class to xml file
	 * @param response to write xml file
	 * @throws IOException 
	 * @throws JAXBException 
	 */
	void createXMLResponse(HttpServletResponse response, List<PdbInfo> pdbList) throws IOException, JAXBException{

		if(pdbList == null) return;

		response.setContentType("text/xml");
		response.setCharacterEncoding("UTF-8");

		PrintWriter writer = response.getWriter();

		serializePdbInfoList(pdbList, writer);

	}

	public static void serializePdbInfoList(List<PdbInfo> pdbList, PrintWriter writer) throws JAXBException {
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
	}
}
