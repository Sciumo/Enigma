package enigma.console;

/** 
 * Thrown to indicate that a <code>TextWindow</code> is not available in the current
 * environment.
 *
 *@status.stable
 *@author Ethan Royael Nicholas (<a href="mailto:ethan@ethannicholas.com">ethan@ethannicholas.com</a>)
 */  
public class TextWindowNotAvailableException extends RuntimeException {
    public TextWindowNotAvailableException() {
        super();
    }


    public TextWindowNotAvailableException(String detail) {
        super(detail);
    }
}