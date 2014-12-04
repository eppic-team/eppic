package eppic.commons.sequence;

/**
 * A exception to be thrown when Uniprot versions of two sources don't match
 * @author duarte_j
 *
 */
public class UniprotVerMisMatchException extends Exception {
	static final long serialVersionUID = 1L;

	public UniprotVerMisMatchException() {
	}

	public UniprotVerMisMatchException(String arg0) {
		super(arg0);
	}

	public UniprotVerMisMatchException(Throwable arg0) {
		super(arg0);
	}

	public UniprotVerMisMatchException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}
}
