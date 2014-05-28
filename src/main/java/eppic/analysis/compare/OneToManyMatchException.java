package eppic.analysis.compare;

public class OneToManyMatchException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String msg;
	
	public OneToManyMatchException(String msg) {
		this.msg = msg;
	}
	
	public String getMessage() {
		return msg;
	}
}
