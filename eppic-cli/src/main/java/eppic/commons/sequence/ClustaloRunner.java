package eppic.commons.sequence;

import java.io.File;
import java.io.IOException;

public class ClustaloRunner {

	private File clustaloBin;
	private String cmdLine;
	
	public ClustaloRunner(File clustaloBin) {
		this.clustaloBin = clustaloBin;
	}
	
	public String buildCmdLine(File inFile, File outFile, int nThreads) {
	
		// --force forces file overwriting, default behaviour is if file exists it stops
		this.cmdLine = clustaloBin+" -i "+inFile+" -o "+outFile+" --threads="+nThreads+" --force";
		
		return cmdLine;
	}
	
	/**
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void runClustalo() throws IOException, InterruptedException {


		Process proc = Runtime.getRuntime().exec(cmdLine);

		//BufferedReader errorBR = new BufferedReader(new InputStreamReader(proc.getErrorStream()));

		int exitValue = proc.waitFor();

		// throwing exception if exit state is not 0 
		if (exitValue!=0) {
			throw new IOException("clustalo exited with value "+exitValue);
		}

		
	}
	
	public String getCmdLine() {
		return cmdLine;
	}
}
