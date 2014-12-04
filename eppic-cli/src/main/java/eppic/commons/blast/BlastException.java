package eppic.commons.blast;

public class BlastException extends Exception {

	/**
	 * To be thrown when a blast program exits abnormally 
	 */
	private static final long serialVersionUID = 1L;

	public BlastException() {
	}

	public BlastException(String arg0) {
		super(arg0);
	}

	public BlastException(Throwable arg0) {
		super(arg0);
	}

	public BlastException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

}
