package ch.systemsx.sybit.crkwebui.server;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import ch.systemsx.sybit.crkwebui.shared.CrkWebException;
import ch.systemsx.sybit.crkwebui.shared.model.InputParameters;

/**
 * This class is used to start crk application
 * @author srebniak_a
 *
 */
public class CrkRunner implements Runnable 
{
	private EmailSender emailSender;
	private String fileName;
	private String resultPath;
	private String destinationDirectoryName;
	private String generatedDirectoryName;
	private InputParameters inputParameters;
	private File logFile;
	private String crkApplicationLocation;
	
	private boolean isWaiting;
	
	public CrkRunner(
					 EmailSender emailSender, 
					 String fileName,
					 String resultPath,
					 String destinationDirectory,
					 String generatedDirectoryName,
					 InputParameters inputParameters,
					 String crkApplicationLocation)
	{
		this.inputParameters = inputParameters;
		this.fileName = fileName;
		this.resultPath = resultPath;
		this.destinationDirectoryName = destinationDirectory;
		this.generatedDirectoryName = generatedDirectoryName;
		this.crkApplicationLocation = crkApplicationLocation;

		this.emailSender = emailSender;
	}

	public void run() 
	{
		String message = fileName
				+ " job submitted. To see the status of the processing please go to: "
				+ resultPath;

		// generatuin of this file should be moved to crk or check
		// whether file is not locked
		logFile = new File(destinationDirectoryName + "/crklog");
		
		try 
		{
			emailSender.send("Crk: " + fileName + " submitted", message);

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
			command.add("java");
			command.add("-jar");
			command.add(crkApplicationLocation);
			command.add("-i");
			command.add(fileName);
			command.add("-o");
			command.add(destinationDirectoryName);
			command.add("-q");
			command.add(String.valueOf(inputParameters.getMaxNrOfSequences()));
			command.add("-e");
			command.add(String.valueOf(inputParameters.getSelecton()));
			
			if(inputParameters.isUsePISA())
			{
				command.add("-p");
			}
			
			if(inputParameters.isUseNACCESS())
			{
				command.add("-n");
			}
			
			if(!inputParameters.isUseTCoffee())
			{
				command.add("-t");
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
				}
			}
			
			command.add("-L");
			command.add(destinationDirectoryName + "/crklog");
			command.add("-l");
			
			
			// using ProcessBuilder to spawn an process
			ProcessBuilder processBuilder = new ProcessBuilder(command);
			processBuilder.redirectErrorStream(true);

			Process crkProcess = processBuilder.start();
			
//			BufferedReader br = new BufferedReader( new InputStreamReader( crkProcess.getErrorStream() ));
//            
//			StringBuffer errorLog = new StringBuffer();
//			
//			String line;
//            while ( ( line = br.readLine() ) != null )
//            {
//            	errorLog.append(line);
//            }
//            
//            br.close();
            
			int exitValue = crkProcess.waitFor();
			
			if(exitValue != 0)
			{
				throw new CrkWebException(new Exception("Error during calculations"));
			}
			
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
			File destinationDirectory = new File(destinationDirectoryName);
			String[] directoryContent = destinationDirectory.list();
		    
		    byte[] buffer = new byte[1024];
		    
	        String generatedZip = destinationDirectoryName + "/" + fileName + ".zip";
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
			
			DBUtils.updateStatusOfJob(generatedDirectoryName, "Finished");

			outputStream = new FileOutputStream(logFile, true);
			bufferedOutputStream = new BufferedOutputStream(
					outputStream);
			logMessage = "Processing finished\n";
			bufferedOutputStream.write(logMessage.getBytes());
			bufferedOutputStream.close();
			outputStream.close();

			message = fileName
					+ " processing finished. To see the status of the processing please go to: "
					+ resultPath;
			emailSender.send("Crk: " + fileName
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
			handleException(e.getMessage());
		}
//		finally
//		{
//			((CrkThreadGroup)getThreadGroup()).runNextInQueue();
//		}

	}
	
	private void handleException(String errorMessage)
	{
		String message = fileName + " - error during processing the data.\n\n" + errorMessage;
		
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
			DBUtils.updateStatusOfJob(generatedDirectoryName, "Error");
		} 
		catch (CrkWebException e2) 
		{
			e2.printStackTrace();
		}

		File errorFile = new File(destinationDirectoryName + "/crkerr");

		try 
		{
			errorFile.createNewFile();
		}
		catch (IOException e1) 
		{
			e1.printStackTrace();
		}

		emailSender.send("Crk: " + fileName + " error during processing",
				message + "\n\n" + resultPath);
	}
	
	public synchronized boolean isWaiting()
	{
		return isWaiting;
	}
	
	public synchronized void setIsWaiting(boolean isWaiting)
	{
		this.isWaiting = isWaiting;
	}
}
