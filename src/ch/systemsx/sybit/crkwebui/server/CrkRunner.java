package ch.systemsx.sybit.crkwebui.server;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import ch.systemsx.sybit.crkwebui.shared.CrkWebException;
import crk.CRKMain;

public class CrkRunner implements Runnable 
{
	private EmailSender emailSender;
	private String fileName;
	private String resultPath;
	private String destinationDirectoryName;
	private String generatedDirectoryName;

	public CrkRunner(EmailSender emailSender, String fileName,
			String resultPath, String destinationDirectory,
			String generatedDirectoryName) {
		this.fileName = fileName;
		this.resultPath = resultPath;
		this.destinationDirectoryName = destinationDirectory;
		this.generatedDirectoryName = generatedDirectoryName;

		this.emailSender = emailSender;
	}

	public void run() {
		String message = fileName
				+ " job submitted. To see the status of the processing please go to: "
				+ resultPath;

		// generatuin of this file should be moved to crk or check
		// whether file is not locked
		File logFile = new File(destinationDirectoryName + "/crklog");
		
		try 
		{
			emailSender.send("Crk: " + fileName + " submitted", message);

			File runFile = new File(destinationDirectoryName + "/crkrun");
			runFile.createNewFile();

			FileOutputStream outputStream = new FileOutputStream(logFile);
			BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(
					outputStream);
			String logMessage = "Processing started\n";
			bufferedOutputStream.write(logMessage.getBytes());
			bufferedOutputStream.close();
			outputStream.close();

			PrintStream logStream = new PrintStream(logFile);
			CRKMain crkMain = new CRKMain(logStream);
			crkMain.setDefaults();

			crkMain.getCRKParams().setPdbCode(
					destinationDirectoryName + "/" + fileName);
			crkMain.getCRKParams()
					.setOutDir(new File(destinationDirectoryName));

			// turn off jaligner logging (we only use NeedlemanWunschGotoh
			// from that package)
			// (for some reason this doesn't work if condensated into one
			// line, it seems that one needs to instantiate the logger and
			// then call setLevel)
			// (and even weirder, for some reason it doesn't work if you put
			// the code in its own separate method!)
			java.util.logging.Logger jalLogger = java.util.logging.Logger
					.getLogger("NeedlemanWunschGotoh");
			jalLogger.setLevel(java.util.logging.Level.OFF);

			crkMain.setUpLogging();

			crkMain.loadConfigFile();

			// 0 load pdb
			crkMain.doLoadPdb();

			// 1 finding interfaces
			if (crkMain.getCRKParams().getInterfSerFile() != null) {
				crkMain.doLoadInterfacesFromFile();
			} else {
				crkMain.doFindInterfaces();
			}

			// 2 finding evolutionary context
			if (crkMain.getCRKParams().getChainEvContextSerFile() != null) {
				crkMain.doLoadEvolContextFromFile();
			} else {
				crkMain.doFindEvolContext();
			}

			// 3 scoring
			crkMain.doScoring();
			
			
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
		catch (Exception e) 
		{
			message = fileName + " - error during processing the data.\n\n" + e.getMessage();
			
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
					message);
		}

	}
}
