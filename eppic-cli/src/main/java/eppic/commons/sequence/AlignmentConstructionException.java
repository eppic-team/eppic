package eppic.commons.sequence;

/**
 * Exception to be thrown whenever the construction of an alignment object 
 * failes due to some malformed data that is not directly connected to some 
 * file format error. The latter shall be notified by specialized file format 
 * error exceptions.
 * 
 * @author Lars Petzold
 */
public class AlignmentConstructionException extends Exception {
    static final long serialVersionUID = 1L;

    public AlignmentConstructionException() {
    }

    public AlignmentConstructionException(String arg0) {
	super(arg0);
    }

    public AlignmentConstructionException(Throwable arg0) {
	super(arg0);
    }

    public AlignmentConstructionException(String arg0, Throwable arg1) {
	super(arg0, arg1);
    }
}
