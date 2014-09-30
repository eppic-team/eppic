package eppic;

import org.apache.commons.logging.Log;

public class EppicException extends Exception {

	private static final long serialVersionUID = 1L;

	private Exception origException;
	private String message;
	private boolean fatal;
	
	public EppicException(Exception origException, String message, boolean fatal) {
		this.origException = origException;
		this.message = message;
		this.fatal = fatal;
	}
	
	public String getMessage() {
		return message;
	}
	
	public boolean isFatal() {
		return fatal;
	}
	
	public Exception getOrigException() {
		return origException;
	}
	
	public void log (Log logger) {
		if (fatal) {
			logger.fatal(message);
		} else {
			logger.error(message);
		}
	}
	
	public void exitIfFatal(int exitStatus) {
		if (isFatal()) System.exit(exitStatus);
	}
}
