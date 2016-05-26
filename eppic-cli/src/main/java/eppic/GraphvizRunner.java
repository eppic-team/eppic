package eppic;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import eppic.assembly.gui.LatticeGUIMustache;


public class GraphvizRunner {	
	private File graphvizExec;
	
	public GraphvizRunner(File graphvizExec) {
		this.graphvizExec = graphvizExec;
	}
	
	/**
	 * Runs graphviz on the specified dotfile
	 * 
	 * @param dotFile Input dot file
	 * @param outfile output filename
	 * @param format output format (png, svg, etc.)
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void generateFromDot(File dotFile, File outfile, String format)
	throws IOException, InterruptedException {
		List<String> command = new ArrayList<String>();
		command.add(graphvizExec.getAbsolutePath());
		command.add("-Kneato");
		command.add("-n2");
		command.add("-T");
		command.add(format);
		command.add("-o");
		command.add(outfile.toString());
		command.add(dotFile.toString());

		Process process = new ProcessBuilder(command).start();
		boolean done = process.waitFor(60,TimeUnit.SECONDS);
		
		if (!done) {
			throw new IOException("Graphviz execution timed out.");
		}
		if (process.exitValue()!=0) {
			throw new IOException("Graphviz exited with error status "+process.exitValue());
		}
	}
	
	/**
	 * Runs graphviz on the output of executing the specified GUI, which
	 * should produce dot output.
	 * 
	 * This avoids writing the dot file to disk.
	 * 
	 * @param dotFile Input dot file
	 * @param outfile output filename
	 * @param format output format (png, svg, etc.)
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void generateFromDot(LatticeGUIMustache gui, File outfile, String format)
	throws IOException, InterruptedException {
		List<String> command = new ArrayList<String>();
		command.add(graphvizExec.getAbsolutePath());
		command.add("-Kneato");
		command.add("-n2");
		command.add("-T");
		command.add(format);
		command.add("-o");
		command.add(outfile.toString());

		Process process = new ProcessBuilder(command).start();
		OutputStream dotIn = process.getOutputStream();
		try( PrintWriter writer = new PrintWriter(dotIn) ){
			gui.execute(writer);
		}
		
		boolean done = process.waitFor(10,TimeUnit.SECONDS);
		
		if (!done) {
			throw new IOException("Graphviz execution timed out.");
		}
		if (process.exitValue()!=0) {
			throw new IOException("Graphviz exited with error status "+process.exitValue());
		}
	}
}