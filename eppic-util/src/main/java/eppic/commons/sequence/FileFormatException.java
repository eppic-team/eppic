package eppic.commons.sequence;

/**
 * Exception to be thrown when parsing a file which does not comply to its format.
 * The error message passed should specify the type of file.
 * @author duarte
 *
 */
public class FileFormatException extends Exception {
    static final long serialVersionUID = 1L;

    public FileFormatException() {
    }

    public FileFormatException(String arg0) {
	super(arg0);
    }

    public FileFormatException(Throwable arg0) {
	super(arg0);
    }

    public FileFormatException(String arg0, Throwable arg1) {
	super(arg0, arg1);
    }
    
    public FileFormatException(String arg0, String file, long line ) {
	super(file+"["+line+"]: "+arg0);
    }
    
    public FileFormatException(String arg0, Throwable arg1, String file, long line ) {
	super(file+"["+line+"]: "+arg0, arg1);
    }
    
    public FileFormatException(String arg0, String file, long startByte, long stopByte ) {
	super(file+"["+startByte+"-"+stopByte+"]: "+arg0);
    }
    
    public FileFormatException(String arg0, Throwable arg1, String file, long startByte, long stopByte ) {
	super(file+"["+startByte+"-"+stopByte+"]: "+arg0, arg1);
    }
}
