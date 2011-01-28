package ch.systemsx.sybit.crkwebui.server;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import ch.systemsx.sybit.crkwebui.server.data.EmailData;
import crk.CRKMain;

public class CrkRunner implements Runnable
{
	private EmailSender emailSender;
	private EmailData emailData;
	private String fileName;
	private String resultPath;
	private String destinationDirectory;
	private String sessionId;
	private String generatedDirectoryName;
	
	public CrkRunner(EmailData emailData,
					 String fileName,
					 String resultPath,
					 String destinationDirectory,
					 String sessionId,
					 String generatedDirectoryName)
	{
		this.fileName = fileName;
		this.resultPath = resultPath;
		this.destinationDirectory = destinationDirectory;
		this.generatedDirectoryName = generatedDirectoryName;
		
		emailSender = new EmailSender(emailData);
		this.emailData = emailData;
		this.sessionId = sessionId;
	}
	
	public void run()
	{
		String message = fileName + " job submitted. To see the status of the processing please go to: " + resultPath;
		
		try
		{
			emailSender.send("Crk: " + fileName + " submitted", message);
		
			File runFile = new File(destinationDirectory + "/crkrun");
			runFile.createNewFile();
			
			String errorMessage = DBUtils.insertNewJob(generatedDirectoryName, 
													   sessionId,
													   emailData.getEmailRecipient(),
													   fileName);
			
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
				
				CRKMain crkApp = new CRKMain();
				crkApp.init(destinationDirectory + "/" + fileName, destinationDirectory);
				
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
