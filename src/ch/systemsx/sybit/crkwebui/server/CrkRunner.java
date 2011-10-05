package ch.systemsx.sybit.crkwebui.server;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.ggf.drmaa.JobInfo;
import org.ggf.drmaa.JobTemplate;
import org.ggf.drmaa.Session;

import ch.systemsx.sybit.crkwebui.server.db.model.JobDAO;
import ch.systemsx.sybit.crkwebui.server.db.model.JobDAOImpl;
import ch.systemsx.sybit.crkwebui.shared.CrkWebException;
import ch.systemsx.sybit.crkwebui.shared.model.InputParameters;
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
	
	private boolean wasFileUploaded;
	private String[] downloadFileZipExcludeSufixes;
//	private boolean isWaiting;
	
	public CrkRunner(
					 EmailSender emailSender, 
					 String input,
					 String resultPath,
					 String destinationDirectory,
					 String jobId,
					 InputParameters inputParameters,
					 String crkApplicationLocation,
					 Session sgeSession,
					 boolean wasFileUploaded,
					 String[] downloadFileZipExcludeSufixes)
	{
		this.inputParameters = inputParameters;
		this.input = input;
		this.resultPath = resultPath;
		this.destinationDirectoryName = destinationDirectory;
		this.jobId = jobId;
		this.crkApplicationLocation = crkApplicationLocation;

		this.emailSender = emailSender;
		
		this.sgeSession = sgeSession;
		
		this.wasFileUploaded = wasFileUploaded;
		this.downloadFileZipExcludeSufixes = downloadFileZipExcludeSufixes;
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
			emailSender.send("Crk: " + input + " submitted", message);
			
			File runFile = new File(destinationDirectoryName + "/crkrun");
			runFile.createNewFile();

			FileOutputStream outputStream = new FileOutputStream(logFile);
			BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(
					outputStream);
			String logMessage = "Processing started - please wait\n";
			bufferedOutputStream.write(logMessage.getBytes());
			bufferedOutputStream.close();
			outputStream.close();
			
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

			List<String> command = new ArrayList<String>();
//			command.add("java");
			command.add("-jar");
			command.add(crkApplicationLocation);
			command.add("-i");
			
			String inputLocation = input;
			
			if(wasFileUploaded)
			{
				inputLocation = destinationDirectoryName + "/" + input;
			}
			
			command.add(inputLocation);
			command.add("-o");
			command.add(destinationDirectoryName);
			command.add("-q");
			command.add(String.valueOf(inputParameters.getMaxNrOfSequences()));
			
			if(inputParameters.isUsePISA())
			{
				command.add("-p");
			}
			
			if(inputParameters.isUseNACCESS())
			{
				command.add("-n");
			}
						
			command.add("-d");
			command.add(String.valueOf(inputParameters.getIdentityCutoff()));
			command.add("-r");
			command.add(String.valueOf(inputParameters.getReducedAlphabet()));
			command.add("-A");
			command.add(String.valueOf(inputParameters.getAsaCalc()));
			command.add("-a");
			command.add(String.valueOf("1"));
			
			if(inputParameters.getMethods() != null)
			{
				for(String method : inputParameters.getMethods())
				{
					if(method.equals("KaKs"))
					{
						command.add("-k");
					}
					else if(method.equals("Entropy"))
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
	      	
	      	final List<String> prefixesToExclude = new ArrayList<String>();
	      	prefixesToExclude.add(jobId + ".");
	      	prefixesToExclude.add("crklog");
	      	
	      	
			File destinationDirectory = new File(destinationDirectoryName);
			String[] directoryContent = destinationDirectory.list(new FilenameFilter() 
			{
				public boolean accept(File dir, String name)
				{
					if(downloadFileZipExcludeSufixes != null)
					{
						for(String sufix : downloadFileZipExcludeSufixes)
						{
							if (name.endsWith(sufix)) 
							{
								return false;
							}
						}
					}
					
					for(String prefix : prefixesToExclude)
					{
						if (name.startsWith(prefix)) 
						{
							return false;
						}
					}
					
					return true;
				}
			});
		    
		    byte[] buffer = new byte[1024];
		    
	        String generatedZip = destinationDirectoryName + "/" + input + ".zip";
	        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(generatedZip));
	    
	        if(directoryContent != null)
	        {
		        for (int i=0; i<directoryContent.length; i++) 
		        {
		        	File source = new File(destinationDirectoryName + "/" + directoryContent[i]);
		        	{
		        		if(source.isFile())
		        		{
		        			FileInputStream in = new FileInputStream(source);
				            out.putNextEntry(new ZipEntry(directoryContent[i]));
				    
				            int length;
				            while ((length = in.read(buffer)) > 0) 
				            {
				                out.write(buffer, 0, length);
				            }
				    
				            out.closeEntry();
				            in.close();
		        		}
		        	}
		            
		        }
		    } 
	        
	        out.close();
			
	        JobDAO jobDao = new JobDAOImpl();
	        jobDao.updateStatusOfJob(jobId, StatusOfJob.FINISHED);
//			DBUtils.updateStatusOfJob(generatedDirectoryName, StatusOfJob.FINISHED);

			outputStream = new FileOutputStream(logFile, true);
			bufferedOutputStream = new BufferedOutputStream(
					outputStream);
			logMessage = "Processing finished\n";
			bufferedOutputStream.write(logMessage.getBytes());
			bufferedOutputStream.close();
			outputStream.close();

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
		String message = input + " - error during processing the data.\n\n" + errorMessage;
		
		try
		{
			FileOutputStream outputStream = new FileOutputStream(logFile, true);
			BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(
					outputStream);
			bufferedOutputStream.write(message.getBytes());
			bufferedOutputStream.close();
			outputStream.close();
		}
		catch(Exception ex)
		{
			
		}
		
		try 
		{
			JobDAO jobDao = new JobDAOImpl();
			jobDao.updateStatusOfJob(jobId, StatusOfJob.ERROR);
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

		emailSender.send("Crk: " + input + " error during processing",
				message + "\n\n" + resultPath);
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
	
	private boolean isInterrupted;
	
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
