package enigma.core;


/** 
 * Thrown to indicate that a new Java virtual machine could not be instantiated with the
 * specified configuration.
 *
 *@status.experimental
 *@author Ethan Royael Nicholas (<a href="mailto:ethan@ethannicholas.com">ethan@ethannicholas.com</a>)
 */
public class VirtualMachineInstantiationException extends Exception {
    /**
	 * 
	 */
	private static final long serialVersionUID = -7590070023097576487L;

	public VirtualMachineInstantiationException(String detail) {
        super(detail);
    }
}