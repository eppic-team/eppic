package ch.systemsx.sybit.crkwebui.server;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import ch.systemsx.sybit.crkwebui.server.data.EmailData;
import crk.CRKMain;

public class CrkRunner implements Runnable
{
	private EmailSender emailSender;
	private String fileName;
	private String resultPath;
	private String destinationDirectory;
	private String generatedDirectoryName;
	
	public CrkRunner(EmailSender emailSender,
					 String fileName,
					 String resultPath,
					 String destinationDirectory,
					 String generatedDirectoryName)
	{
		this.fileName = fileName;
		this.resultPath = resultPath;
		this.destinationDirectory = destinationDirectory;
		this.generatedDirectoryName = generatedDirectoryName;
		
		this.emailSender = emailSender;
	}
	
	public void run()
	{
		String message = fileName + " job submitted. To see the status of the processing please go to: " + resultPath;
		
		try
		{
			emailSender.send("Crk: " + fileName + " submitted", message);
		
			File runFile = new File(destinationDirectory + "/crkrun");
			runFile.createNewFile();
			
			String errorMessage = null;
			
			if(errorMessage == null)
			{
				// generatuin of this file should be moved to crk or check whether file is not locked
				File logFile = new File(destinationDirectory + "/crklog");
				
				FileOutputStream outputStream = new FileOutputStream(logFile);
				BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
				String logMessage = "Processing started\n";
				bufferedOutputStream.write(logMessage.getBytes());
				bufferedOutputStream.close();
				outputStream.close();
				
//				PrintStream logStream = new PrintStream(logFile);
//				CRKMain crkMain = new CRKMain(logStream);
//				crkMain.setDefaults();
//
//				crkMain.getCRKParams().setPdbCode(destinationDirectory + "/" + fileName);
//				crkMain.getCRKParams().setOutDir(new File(destinationDirectory));
//				
//				// turn off jaligner logging (we only use NeedlemanWunschGotoh from that package)
//				// (for some reason this doesn't work if condensated into one line, it seems that one needs to instantiate the logger and then call setLevel)
//				// (and even weirder, for some reason it doesn't work if you put the code in its own separate method!)
//				java.util.logging.Logger jalLogger = java.util.logging.Logger.getLogger("NeedlemanWunschGotoh");
//				jalLogger.setLevel(java.util.logging.Level.OFF);
//				
//				crkMain.setUpLogging();
//
//				crkMain.loadConfigFile();
//
//				// 0 load pdb
//				crkMain.doLoadPdb();
//
//				// 1 finding interfaces
//				if (crkMain.getCRKParams().getInterfSerFile()!=null) {
//					crkMain.doLoadInterfacesFromFile();
//				} else {
//					crkMain.doFindInterfaces();
//				}
//
//				// 2 finding evolutionary context
//				if (crkMain.getCRKParams().getChainEvContextSerFile()!=null) {
//					crkMain.doLoadEvolContextFromFile();
//				} else {
//					crkMain.doFindEvolContext();
//				}
//
//				// 3 scoring
//				crkMain.doScoring();
				
				errorMessage = DBUtils.updateStatusOfJob(generatedDirectoryName, "Finished");
				
				if(errorMessage == null)
				{
					outputStream = new FileOutputStream(logFile, true);
					bufferedOutputStream = new BufferedOutputStream(outputStream);
					logMessage = "Processing finished\n";
					bufferedOutputStream.write(logMessage.getBytes());
					bufferedOutputStream.close();
					outputStream.close();
				
					message = fileName + " processing finished. To see the status of the processing please go to: " + resultPath;
					emailSender.send("Crk: " + fileName + " processing finished", message);
				}
				else
				{
					outputStream = new FileOutputStream(logFile, true);
					bufferedOutputStream = new BufferedOutputStream(outputStream);
					logMessage = "Error during updating database\n";
					bufferedOutputStream.write(logMessage.getBytes());
					bufferedOutputStream.close();
					outputStream.close();
				}
			}
			else
			{
				DBUtils.updateStatusOfJob(generatedDirectoryName, "error");
				
				File logFile = new File(destinationDirectory + "/crklog");
				FileOutputStream outputStream = new FileOutputStream(logFile);
				BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
				bufferedOutputStream.write(errorMessage.getBytes());
				bufferedOutputStream.close();
				outputStream.close();
			}
			
			runFile.delete();
		}
		catch(Exception e)
		{
			DBUtils.updateStatusOfJob(generatedDirectoryName, "error");
			
			File errorFile = new File(destinationDirectory + "/crkerr");
			
			try
			{
				errorFile.createNewFile();
			} 
			catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			message = fileName + " - error during processing the data.\n\n" + e.getMessage();
			emailSender.send("Crk: " + fileName + " error during processing", message);
		}
		
	}
}
