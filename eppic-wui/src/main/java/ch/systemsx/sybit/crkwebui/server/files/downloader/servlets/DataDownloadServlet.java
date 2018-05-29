package ch.systemsx.sybit.crkwebui.server.files.downloader.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;

import javax.persistence.PersistenceContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import ch.systemsx.sybit.crkwebui.server.files.downloader.generators.JobListWithInterfacesGenerator;
import org.eclipse.persistence.jaxb.MarshallerProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.systemsx.sybit.crkwebui.server.commons.servlets.BaseServlet;
import ch.systemsx.sybit.crkwebui.server.files.downloader.validators.DataDownloadServletInputValidator;
import ch.systemsx.sybit.crkwebui.server.ip.validators.IPVerifier;
import ch.systemsx.sybit.crkwebui.shared.exceptions.ValidationException;
import eppic.model.dto.Assembly;
import eppic.model.dto.ChainCluster;
import eppic.model.dto.InputWithType;
import eppic.model.dto.Interface;
import eppic.model.dto.InterfaceCluster;
import eppic.model.dto.PdbInfo;
import eppic.db.dao.AssemblyDAO;
import eppic.db.dao.ChainClusterDAO;
import eppic.db.dao.DaoException;
import eppic.db.dao.DataDownloadTrackingDAO;
import eppic.db.dao.InterfaceClusterDAO;
import eppic.db.dao.InterfaceDAO;
import eppic.db.dao.JobDAO;
import eppic.db.dao.PDBInfoDAO;
import eppic.db.dao.jpa.AssemblyDAOJpa;
import eppic.db.dao.jpa.ChainClusterDAOJpa;
import eppic.db.dao.jpa.DataDownloadTrackingDAOJpa;
import eppic.db.dao.jpa.InterfaceClusterDAOJpa;
import eppic.db.dao.jpa.InterfaceDAOJpa;
import eppic.db.dao.jpa.JobDAOJpa;
import eppic.db.dao.jpa.PDBInfoDAOJpa;

/**
 * Servlet used to download results in xml/json format.
 * Adapted to both json or xml by using eclipselink JAXB implementation.
 * @author Nikhil Biyani
 * @author Jose Duarte
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
	//private int maxNumJobIds;
	private int defaultNrOfAllowedSubmissionsForIP;

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);

		//maxNumJobIds = Integer.parseInt(properties.getProperty("max_jobs_in_one_call","1"));
		defaultNrOfAllowedSubmissionsForIP = Integer.parseInt(properties.getProperty("nr_of_allowed_submissions_for_ip","100"));

	}

	/**
	 * Returns file specified by the parameters.
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException
			{
		String type = request.getParameter("type");
		String jobId = request.getParameter("id");
		String assemblyIds = request.getParameter("assemblyIds");
		String interfaceClusterIds = request.getParameter("interfaceClusterIds");
		String interfaceIds = request.getParameter("interfaceIds");
		String getSeqInfo = request.getParameter("withSeqInfo");
		String getResInfo = request.getParameter("withResInfo");

		String requestIP = request.getRemoteAddr();

		logger.info("Data download requested for '{}' with type '{}'. Request IP: {}", jobId, type, requestIP);

		try
		{
			addIPToDB(requestIP);

			DataDownloadServletInputValidator.validateFileDownloadInput(type, jobId, getSeqInfo, getResInfo);

			IPVerifier.verifyIfCanBeSubmitted(requestIP,
										      defaultNrOfAllowedSubmissionsForIP,
										      true);

			List<PdbInfo> pdbList = new ArrayList<>();

			Set<Integer> interfaceClusterIdList = JobListWithInterfacesGenerator.createIntegerList(interfaceClusterIds);
			Set<Integer> interfaceIdList = JobListWithInterfacesGenerator.createIntegerList(interfaceIds);
			Set<Integer> assemblyIdList = JobListWithInterfacesGenerator.createIntegerList(assemblyIds);

			// by default no seq info is added, unless requested explicitly (before 3.0.7 default was always return chains and sequences)
			boolean getSeqs = (getSeqInfo != null && getSeqInfo.equals("t"));
			// by default no res info is added, unless requested explicitly (before 3.0.7 default was always return residue info)
			boolean getRes = (getResInfo != null && getResInfo.equals("t"));

			pdbList.add(getResultData(jobId, interfaceClusterIdList, interfaceIdList, assemblyIdList, getSeqs, getRes));

			createResponse(response, pdbList, type);

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
	 * @param interfaceClusterIdList list of interface cluster ids to be retrieved
	 * @param interfaceIdList list of interface ids to be retrieved (null for everything)
	 * @param assemblyIdList list of assembly ids to be retrieved
	 * @param getSeqInfo whether to retrieve sequence info or not
	 * @param getResInfo whether to retrieve residue info or not
	 * @return pdb info item
	 * @throws DaoException when can not retrieve result of the job
	 */
	public static PdbInfo getResultData(String jobId,
								  Set<Integer> interfaceClusterIdList,
								  Set<Integer> interfaceIdList,
								  Set<Integer> assemblyIdList,
								  boolean getSeqInfo,
								  boolean getResInfo) throws DaoException
	{
		JobDAO jobDAO = new JobDAOJpa();
		InputWithType input = jobDAO.getInputWithTypeForJob(jobId);

		PDBInfoDAO pdbInfoDAO = new PDBInfoDAOJpa();
		PdbInfo pdbInfo = pdbInfoDAO.getPDBInfo(jobId);

		InterfaceClusterDAO clusterDAO = new InterfaceClusterDAOJpa();
		List<InterfaceCluster> clusters = clusterDAO.getInterfaceClusters(pdbInfo.getUid(), true, false);

		InterfaceDAO interfaceDAO = new InterfaceDAOJpa();

		// filter the clusters if list provided
		if (interfaceClusterIdList!=null) {
			clusters = clusters.stream().filter(c -> interfaceClusterIdList.contains(c.getClusterId())).collect(Collectors.toList());
		}

		for(InterfaceCluster cluster: clusters){

			logger.debug("Getting data for interface cluster uid {}", cluster.getUid());
			List<Interface> interfaceItems;
			if(interfaceIdList != null){
				logger.debug("Interface id list requested: {}", interfaceIdList.toString());
				if (getResInfo)
					interfaceItems = interfaceDAO.getInterfacesForCluster(cluster.getUid(), interfaceIdList, true, true);
				else
					interfaceItems = interfaceDAO.getInterfacesForCluster(cluster.getUid(), interfaceIdList, true, false);
			}
			else{
				if (getResInfo)
					interfaceItems = interfaceDAO.getInterfacesForCluster(cluster.getUid(), true, true);
				else
					interfaceItems = interfaceDAO.getInterfacesForCluster(cluster.getUid(), true, false);
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

		if(getSeqInfo){
			ChainClusterDAO chainClusterDAO = new ChainClusterDAOJpa();
			List<ChainCluster> chainClusters = chainClusterDAO.getChainClusters(pdbInfo.getUid());
			pdbInfo.setChainClusters(chainClusters);
		}

		pdbInfo.setInputType(input.getInputType());
		pdbInfo.setInputName(input.getInputName());


		// assemblies info
		AssemblyDAO assemblyDAO = new AssemblyDAOJpa();

		List<Assembly> assemblies = assemblyDAO.getAssemblies(pdbInfo.getUid(), true);
		// filtering out if a list of ids provided
		if (assemblyIdList!=null) {
			assemblies = assemblies.stream().filter(a -> assemblyIdList.contains(a.getId())).collect(Collectors.toList());
		}
		pdbInfo.setAssemblies(assemblies);

		return pdbInfo;
	}

	/**
	 * Converts the contents of the class to xml/json file
	 * @param response to write xml file
	 * @param pdbList
	 * @param format either "json" or "xml"
	 * @throws IOException
	 * @throws JAXBException
	 */
	private void createResponse(HttpServletResponse response, List<PdbInfo> pdbList, String format) throws IOException, JAXBException{

		if(pdbList == null) return;

		if (format.equals("xml")) {
			response.setContentType("text/xml");
		} else if (format.equals("json")) {
			response.setContentType("application/json");
		}
		// the validator should catch any other wrong value for format

		response.setCharacterEncoding("UTF-8");

		PrintWriter writer = response.getWriter();

		serializePdbInfoList(pdbList, writer, format);

	}

	protected void serializePdbInfoList(List<PdbInfo> pdbList, PrintWriter writer, String format) throws JAXBException {
	    // create JAXB context and initializing Marshaller
	    JAXBContext jaxbContext = JAXBContext.newInstance(PdbInfo.class);
	    Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

	    // for getting nice formatted output
	    jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
	    jaxbMarshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);

		// for json, following https://stackoverflow.com/questions/15357366/converting-java-object-to-json-using-marshaller
	    if (format.equals("xml")) {
			jaxbMarshaller.setProperty(MarshallerProperties.MEDIA_TYPE, "application/xml");
		} else if (format.equals("json")) {
			jaxbMarshaller.setProperty(MarshallerProperties.MEDIA_TYPE, "application/json");
			jaxbMarshaller.setProperty(MarshallerProperties.JSON_INCLUDE_ROOT, false);
		}

		// Writing to console
		if (format.equals("xml")) {
			writer.append("<eppicAnalysisList>");
		}

	    for(PdbInfo pdb:pdbList){
	    	jaxbMarshaller.marshal(pdb, writer);
	    }

		if (format.equals("xml")) {
			writer.append("</eppicAnalysisList>");
		}
	}
}
