package ch.systemsx.sybit.crkwebui.server.files.downloader.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import ch.systemsx.sybit.crkwebui.server.commons.servlets.BaseServlet;
import ch.systemsx.sybit.crkwebui.server.db.dao.HomologsInfoItemDAO;
import ch.systemsx.sybit.crkwebui.server.db.dao.InterfaceItemDAO;
import ch.systemsx.sybit.crkwebui.server.db.dao.JobDAO;
import ch.systemsx.sybit.crkwebui.server.db.dao.PDBScoreDAO;
import ch.systemsx.sybit.crkwebui.server.db.dao.jpa.HomologsInfoItemDAOJpa;
import ch.systemsx.sybit.crkwebui.server.db.dao.jpa.InterfaceItemDAOJpa;
import ch.systemsx.sybit.crkwebui.server.db.dao.jpa.JobDAOJpa;
import ch.systemsx.sybit.crkwebui.server.db.dao.jpa.PDBScoreDAOJpa;
import ch.systemsx.sybit.crkwebui.shared.exceptions.ValidationException;
import ch.systemsx.sybit.crkwebui.shared.model.HomologsInfoItem;
import ch.systemsx.sybit.crkwebui.shared.model.InterfaceItem;
import ch.systemsx.sybit.crkwebui.shared.model.PDBScoreItem;
import ch.systemsx.sybit.crkwebui.shared.model.StatusOfJob;

public class DataDownloadServlet extends BaseServlet{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private List<PDBScoreItem> pdbList;
	private int maxXMLCalls;
	private boolean getSeqInfo;

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		if( (properties.getProperty("max_jobs_in_one_call")) != null)
			maxXMLCalls = Integer.parseInt(properties.getProperty("max_jobs_in_one_call"));
		else
			maxXMLCalls = 1;
	}

	/**
	 * Returns file specified by the parameters.
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException
			{
		String type = request.getParameter("type");
		String jobIdCommaSep = request.getParameter("id");
		String getSeqInfoStr = request.getParameter("getSeqInfo");

		try
		{	
			//Process download Sequence Info
			if(getSeqInfoStr != null){
				if (getSeqInfoStr.equals("f")){
					getSeqInfo = false;
				} else if(getSeqInfoStr.equals("t")){
					getSeqInfo = true;
				} else{
					throw new ValidationException("Bad value provided with &getSeqInfo=  ; (allowed: t/f)");
				}
					
			} else{
				getSeqInfo = true;
			}
			
			//Process Job Id String
			if(jobIdCommaSep==null) 
				throw new ValidationException("Please provide comma seperated values of either PDB-Codes or EPPIC-JobIds with &id=");
			else{
				//Convert comma sep string to list
				List<String> jobIds = Arrays.asList(jobIdCommaSep.split("\\s*,\\s*"));
				
				//Check for max number of jobs
				if(jobIds.size() > maxXMLCalls)
					throw new ValidationException("Exceded maximum number of jobs allowed to be retrived");
				
				pdbList = new ArrayList<PDBScoreItem>();

				for(String jobId:jobIds){
					if(jobId.contains("_")){
						String[] splitStr = jobId.split("_");
						List<Integer> interfaceIdList = createIntegerList(splitStr[1]);
						pdbList.add(getResultsOfProcessing(splitStr[0], interfaceIdList));
					} else {
						pdbList.add(getResultsOfProcessing(jobId));
					}
				}

			}

			//Process Type of file
			if(type == null){
				throw new ValidationException("Please provide a value of file type to be downloaded with &type=");
			}
			else if(type.equals("xml")) {
				createXMLFile(response);

			} else {
				throw new ValidationException("Data can not be downloaded in the format provided with &type=");
			}

		}
		catch(ValidationException e)
		{
			response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED, "Input values are incorrect: " + e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
		}
			}

	/**
	 * converts a string of integers separated by ':' to a list of integers
	 * @param colonSeparatedString
	 * @return list of integers
	 * @throws ValidationException 
	 */
	private List<Integer> createIntegerList(String colonSeparatedString) throws ValidationException{
		List<Integer> intList = new ArrayList<Integer>();
		List<String> intStrList = Arrays.asList(colonSeparatedString.split("\\s*:\\s*"));
		
		for(String intStr: intStrList){
			try{
				int i = Integer.parseInt(intStr);
				intList.add(i);
			}catch(NumberFormatException e){
				throw new ValidationException("Non-Integer interfaceId provided for some id");
			}
		}
		
		return intList;
	}
	
	
	/**
	 * 
	 * @param jobId
	 * @return
	 * @throws Exception
	 */
	private PDBScoreItem getResultsOfProcessing(String jobId) throws Exception
	{
		String status = null;

		JobDAO jobDAO = new JobDAOJpa();
		status = jobDAO.getStatusForJob(jobId);

		if(status != null)
		{

			if(status.equals(StatusOfJob.FINISHED.getName()))
			{
				return getResultData(jobId);
			}
			else
			{
				throw new ValidationException("Nothing found with the provided id:"+ jobId);
			}
		}
		else
		{
			throw new ValidationException("Nothing found with the provided id:"+ jobId);
		}
	}
	
	/**
	 * 
	 * @param jobId
	 * @return
	 * @throws Exception
	 */
	private PDBScoreItem getResultsOfProcessing(String jobId, List<Integer> interfaceIdList) throws Exception
	{
		String status = null;

		JobDAO jobDAO = new JobDAOJpa();
		status = jobDAO.getStatusForJob(jobId);

		if(status != null)
		{

			if(status.equals(StatusOfJob.FINISHED.getName()))
			{
				return getResultData(jobId, interfaceIdList);
			}
			else
			{
				throw new ValidationException("Nothing found with the provided id:"+ jobId);
			}
		}
		else
		{
			throw new ValidationException("Nothing found with the provided id:"+ jobId);
		}
	}

	/**
	 * Retrieves pdb score item for job.
	 * @param jobId identifier of the job
	 * @return pdb score item
	 * @throws Exception when can not retrieve result of the job
	 */
	private PDBScoreItem getResultData(String jobId) throws Exception
	{
		JobDAO jobDAO = new JobDAOJpa();
		int inputType = jobDAO.getInputTypeForJob(jobId);

		PDBScoreDAO pdbScoreDAO = new PDBScoreDAOJpa();
		PDBScoreItem pdbScoreItem = pdbScoreDAO.getPDBScore(jobId);

		InterfaceItemDAO interfaceItemDAO = new InterfaceItemDAOJpa();
		List<InterfaceItem> interfaceItems = interfaceItemDAO.getInterfacesWithScores(pdbScoreItem.getUid());
		pdbScoreItem.setInterfaceItems(interfaceItems);

		if(getSeqInfo){	
			HomologsInfoItemDAO homologsInfoItemDAO = new HomologsInfoItemDAOJpa();
			List<HomologsInfoItem> homologsInfoItems = homologsInfoItemDAO.getHomologsInfoItems(pdbScoreItem.getUid());
			pdbScoreItem.setHomologsInfoItems(homologsInfoItems);
		}
		
		pdbScoreItem.setInputType(inputType);

		return pdbScoreItem;
	}
	
	/**
	 * Retrieves pdb score item for job.
	 * @param jobId identifier of the job
	 * @return pdb score item
	 * @throws Exception when can not retrieve result of the job
	 */
	private PDBScoreItem getResultData(String jobId, List<Integer> interfaceIdList) throws Exception
	{
		JobDAO jobDAO = new JobDAOJpa();
		int inputType = jobDAO.getInputTypeForJob(jobId);

		PDBScoreDAO pdbScoreDAO = new PDBScoreDAOJpa();
		PDBScoreItem pdbScoreItem = pdbScoreDAO.getPDBScore(jobId);

		InterfaceItemDAO interfaceItemDAO = new InterfaceItemDAOJpa();
		List<InterfaceItem> interfaceItems = interfaceItemDAO.getInterfacesWithScores(pdbScoreItem.getUid(), interfaceIdList);
		pdbScoreItem.setInterfaceItems(interfaceItems);

		if(getSeqInfo){	
			HomologsInfoItemDAO homologsInfoItemDAO = new HomologsInfoItemDAOJpa();
			List<HomologsInfoItem> homologsInfoItems = homologsInfoItemDAO.getHomologsInfoItems(pdbScoreItem.getUid());
			pdbScoreItem.setHomologsInfoItems(homologsInfoItems);
		}
		
		pdbScoreItem.setInputType(inputType);

		return pdbScoreItem;
	}

	/**
	 * converts the contents of the class to xml file
	 * @param Stream to write xml file
	 * @throws IOException 
	 */
	public void createXMLFile(HttpServletResponse response) throws IOException{

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
