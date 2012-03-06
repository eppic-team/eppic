package ch.systemsx.sybit.crkwebui.server;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

import model.PDBScoreItemDB;

import org.ggf.drmaa.JobInfo;
import org.ggf.drmaa.JobTemplate;
import org.ggf.drmaa.Session;

import ch.systemsx.sybit.crkwebui.server.db.model.JobDAO;
import ch.systemsx.sybit.crkwebui.server.db.model.JobDAOImpl;
import ch.systemsx.sybit.crkwebui.server.util.RunJobDataValidator;
import ch.systemsx.sybit.crkwebui.shared.CrkWebException;
import ch.systemsx.sybit.crkwebui.shared.model.InputParameters;
import ch.systemsx.sybit.crkwebui.shared.model.InputType;
import ch.systemsx.sybit.crkwebui.shared.model.StatusOfJob;

/**
 * This class is used to start crk application
 * @author srebniak_a
 *
 */
public class CrkRunner implements Runnable 
{
	private EmailSender emailSender;
	private String input;
	private String resultPath;
	private String destinationDirectoryName;
	private String jobId;
	private InputParameters inputParameters;
	private File logFile;
	private String crkApplicationLocation;
	
	private Session sgeSession;
	private String submissionId; 
	
	private int inputType;
//	private boolean isWaiting;

	private boolean isInterrupted;
	
	public CrkRunner(
					 EmailSender emailSender, 
					 String input,
					 String resultPath,
					 String destinationDirectory,
					 String jobId,
					 InputParameters inputParameters,
					 String crkApplicationLocation,
					 Session sgeSession,
					 int inputType)
	{
		this.inputParameters = inputParameters;
		this.input = input;
		this.resultPath = resultPath;
		this.destinationDirectoryName = destinationDirectory;
		this.jobId = jobId;
		this.crkApplicationLocation = crkApplicationLocation;

		this.emailSender = emailSender;
		
		this.sgeSession = sgeSession;
		
		this.inputType = inputType;

	}

	public void run() 
	{
		submissionId = null;
		
		String message = input
				+ " job submitted. To see the status of the processing please go to: "
				+ resultPath;

		logFile = new File(destinationDirectoryName + "/crklog");
		
		try 
		{
			writeMessage("Processing started - please wait\n");
			
			emailSender.send("Crk: " + input + " submitted", message);
			
			File runFile = new File(destinationDirectoryName + "/crkrun");
			runFile.createNewFile();

//			synchronized(this)
//			{
//				if(!((CrkThreadGroup)getThreadGroup()).checkIfCanBeRun())
//				{
//					try 
//					{
//						isWaiting = true;
//						this.wait();
//					}
//					catch (InterruptedException e) 
//					{
//						e.printStackTrace();
//					}
//				}
//			}

			RunJobDataValidator.validateJobId(jobId);
			RunJobDataValidator.validateInput(input);
			RunJobDataValidator.validateInputParameters(inputParameters);
			
			List<String> command = new ArrayList<String>();
//			command.add("java");
			command.add("-jar");
			command.add(crkApplicationLocation);
			command.add("-i");
			
			String inputLocation = input;
			
			if(inputType == InputType.FILE.getIndex())
			{
				inputLocation = destinationDirectoryName + "/" + input;
			}
			
			command.add(inputLocation);
			command.add("-o");
			command.add(destinationDirectoryName);
			command.add("-q");
			command.add(String.valueOf(inputParameters.getMaxNrOfSequences()));
			
			command.add("-d");
			command.add(String.valueOf(inputParameters.getSoftIdentityCutoff()));
			command.add("-D");
			command.add(String.valueOf(inputParameters.getHardIdentityCutoff()));
			command.add("-r");
			command.add(String.valueOf(inputParameters.getReducedAlphabet()));
			command.add("-H");
			command.add(inputParameters.getSearchMode().toLowerCase());
			command.add("-a");
			command.add(String.valueOf("1"));
			
			if(inputParameters.getMethods() != null)
			{
				for(String method : inputParameters.getMethods())
				{
					if(method.equals("Entropy"))
					{
						command.add("-s");
					}
				}
			}
			
			command.add("-L");
			command.add(destinationDirectoryName + "/crklog");
			command.add("-l");
			
			
//			// using ProcessBuilder to spawn an process
//			ProcessBuilder processBuilder = new ProcessBuilder(command);
//			processBuilder.redirectErrorStream(true);
//
//			Process crkProcess = processBuilder.start();
//			
////			BufferedReader br = new BufferedReader( new InputStreamReader( crkProcess.getErrorStream() ));
////            
////			StringBuffer errorLog = new StringBuffer();
////			
////			String line;
////            while ( ( line = br.readLine() ) != null )
////            {
////            	errorLog.append(line);
////            }
////            
////            br.close();
//            
//			int exitValue = crkProcess.waitFor();
//			
//			if(exitValue != 0)
//			{
//				throw new CrkWebException("Error during calculations");
//			}
			
			
//			SessionFactory factory = SessionFactory.getFactory();
//			sgeSession = factory.getSession();
//
//			sgeSession.init("");
			
			JobTemplate jobTemplate = sgeSession.createJobTemplate();
			jobTemplate.setRemoteCommand("java");
			jobTemplate.setArgs(command);
			jobTemplate.setJobName(jobId);
			jobTemplate.setErrorPath(":" + destinationDirectoryName);
			jobTemplate.setOutputPath(":" + destinationDirectoryName);

	      	submissionId = sgeSession.runJob(jobTemplate);

	      	sgeSession.deleteJobTemplate(jobTemplate);

//	      	while((sgeSession.getJobProgramStatus(submissionId) != Session.DONE) && 
//	      		  (sgeSession.getJobProgramStatus(submissionId) != Session.FAILED))
//	      	{
//	      		
//	      	}
//	      	
//	      	if(sgeSession.getJobProgramStatus(submissionId) == Session.FAILED)
//	      	{
//	      		throw new CrkWebException("Error during calculations");
//	      	}
	      	
	      	JobInfo info = sgeSession.wait(submissionId, Session.TIMEOUT_WAIT_FOREVER);

	      	if(info.getExitStatus() != 0)
	      	{
	      		throw new CrkWebException("Error during calculations: " + info.getExitStatus());
	      	}

//	      	sgeSession.exit();
			   
			
//			PrintStream logStream = new PrintStream(logFile);
//			
//			CRKMain crkMain = new CRKMain(logStream);
//			ProcessBuilder processBuilder = new ProcessBuilder(arg0);
//			crkMain.setDefaults();
//
//			crkMain.getCRKParams().setPdbCode(fileName);
//			crkMain.getCRKParams()
//					.setOutDir(new File(destinationDirectoryName));
//			
//			if(crkMain.getCRKParams().isInputAFile())
//			{
//				crkMain.getCRKParams().setInFile(new File(destinationDirectoryName + "/" + fileName));
//			}
//			
//			crkMain.getCRKParams().setMaxNumSeqsSelecton(inputParameters.getMaxNrOfSequences());
//			crkMain.getCRKParams().setSelectonEpsilon(inputParameters.getSelecton());
//			crkMain.getCRKParams().setUsePisa(inputParameters.isUsePISA());
//			crkMain.getCRKParams().setUseNaccess(inputParameters.isUseNACCESS());
//			crkMain.getCRKParams().setUseTcoffeeVeryFastMode(inputParameters.isUseTCoffee());
//			crkMain.getCRKParams().setIdCutoff(inputParameters.getIdentityCutoff());
//			crkMain.getCRKParams().setReducedAlphabet(inputParameters.getReducedAlphabet());
//			crkMain.getCRKParams().setnSpherePointsASAcalc(inputParameters.getAsaCalc());
//			crkMain.getCRKParams().setNumThreads(1);
//			
//			if(inputParameters.getMethods() != null)
//			{
//				for(String method : inputParameters.getMethods())
//				{
//					if(method.equals("KaKs"))
//					{
//						crkMain.getCRKParams().setDoScoreCRK(true);
//					}
//				}
//			}
//			
//			crkMain.getCRKParams().checkCommandLineInput();
//
//			// turn off jaligner logging (we only use NeedlemanWunschGotoh
//			// from that package)
//			// (for some reason this doesn't work if condensated into one
//			// line, it seems that one needs to instantiate the logger and
//			// then call setLevel)
//			// (and even weirder, for some reason it doesn't work if you put
//			// the code in its own separate method!)
//			java.util.logging.Logger jalLogger = java.util.logging.Logger
//					.getLogger("NeedlemanWunschGotoh");
//			jalLogger.setLevel(java.util.logging.Level.OFF);
//
//			crkMain.setUpLogging();
//
//			crkMain.loadConfigFile();
//
//			// 0 load pdb
//			crkMain.doLoadPdb();
//
//			// 1 finding interfaces
//			if (crkMain.getCRKParams().getInterfSerFile() != null) {
//				crkMain.doLoadInterfacesFromFile();
//			} else {
//				crkMain.doFindInterfaces();
//			}
//
//			// 2 finding evolutionary context
//			if (crkMain.getCRKParams().getChainEvContextSerFile() != null) {
//				crkMain.doLoadEvolContextFromFile();
//			} else {
//				crkMain.doFindEvolContext();
//			}
//
//			// 3 scoring
//			crkMain.doScoring();
//			
	      	
		    
		    // zip file generation now moved to the worker
		    //generateZipFile(destinationDirectoryName + "/" + input + ".zip");
		    
		    String webuiFileName = input;
		    
		    if(webuiFileName.contains("."))
		    {
		    	webuiFileName = webuiFileName.substring(0, webuiFileName.lastIndexOf("."));
		    }
		    
		    webuiFileName += ".webui.dat";
		    	
	        PDBScoreItemDB pdbScoreItem = retrieveResult(destinationDirectoryName + "/" + webuiFileName);
	        
			JobDAO jobDao = new JobDAOImpl();
			jobDao.setPdbScoreItemForJob(jobId, pdbScoreItem);
//			DBUtils.updateStatusOfJob(generatedDirectoryName, StatusOfJob.FINISHED);

			writeMessage("Processing finished\n");

			message = input
					+ " processing finished. To see the status of the processing please go to: "
					+ resultPath;
			emailSender.send("Crk: " + input
					+ " processing finished", message);

			runFile.delete();
		} 
//		catch(CRKException e)
//		{
//			handleException(e.getMessage());
//		}
		catch (Throwable e) 
		{
			e.printStackTrace();
			
			if(!isInterrupted)
			{
				handleException(e.getMessage());
			}
		}
//		finally
//		{
//			((CrkThreadGroup)getThreadGroup()).runNextInQueue();
//		}

	}
	
	private void handleException(String errorMessage)
	{
		String message = input + " - error while processing the data.\n\n" + errorMessage;
		
		FileOutputStream outputStream = null;
		
		try
		{
			outputStream = new FileOutputStream(logFile, true);
			BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
			bufferedOutputStream.write(message.getBytes());
		}
		catch(Exception ex)
		{
			if(outputStream != null)
			{
				try
				{
					outputStream.close();
				}
				catch(Throwable t)
				{
					
				}
			}
		}
		
		try 
		{
			JobDAO jobDao = new JobDAOImpl();
			jobDao.updateStatusOfJob(jobId, StatusOfJob.ERROR.getName());
//			DBUtils.updateStatusOfJob(generatedDirectoryName, StatusOfJob.ERROR);
		} 
		catch (CrkWebException e) 
		{
			e.printStackTrace();
		}

		File errorFile = new File(destinationDirectoryName + "/crkerr");

		try 
		{
			errorFile.createNewFile();
		}
		catch (IOException e) 
		{
			e.printStackTrace();
		}

		emailSender.send("Crk: " + input + ", error while processing",
				message + "\n\n" + resultPath);
	}
	
	private PDBScoreItemDB retrieveResult(String resultFileName) throws CrkWebException
	{
		PDBScoreItemDB pdbScoreItem = null;
		File resultFile = new File(resultFileName);
		
		if (resultFile.exists()) 
		{
			FileInputStream fileInputStream = null;
			ObjectInputStream inputStream = null;
			
			try 
			{
				fileInputStream = new FileInputStream(resultFile);
				inputStream = new ObjectInputStream(fileInputStream);
				pdbScoreItem = (PDBScoreItemDB)inputStream.readObject();
			} 
			catch (Throwable e)
			{
				throw new CrkWebException(e);
			}
			finally
			{
				if(fileInputStream != null)
				{
					try
					{
						fileInputStream.close();
					}
					catch(Throwable t)
					{
						t.printStackTrace();
					}
				}
			}
		}
		else
		{
			throw new CrkWebException("WebUI dat file can not be found");
		}
		
		return pdbScoreItem;
	}
	
	private void writeMessage(String message) throws CrkWebException
	{
		FileOutputStream outputStream = null;
		
		try
		{
			outputStream = new FileOutputStream(logFile, true);
			BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
			bufferedOutputStream.write(message.getBytes());
		}
		catch(Throwable t)
		{
			throw new CrkWebException(t);
		}
		finally
		{
			if(outputStream != null)
			{
				try
				{
					outputStream.close();
				}
				catch(Throwable e)
				{
					
				}
			}
		}
	}
	
	public void stopJob()
	{
		isInterrupted = true;
		
		try 
		{
			if((submissionId != null) && (sgeSession.getJobProgramStatus(submissionId) != Session.FAILED))
			{
				sgeSession.control(submissionId, Session.TERMINATE);
			}
		} 
		catch (Throwable t) 
		{
			t.printStackTrace();
		}
	}
	
//	public synchronized boolean isWaiting()
//	{
//		return isWaiting;
//	}
//	
//	public synchronized void setIsWaiting(boolean isWaiting)
//	{
//		this.isWaiting = isWaiting;
//	}
}
