package enigma.event;

import java.util.EventObject;

import enigma.console.TextWindow;

/** 
 * <code>TextWindowEvents</code> are fired when <code>TextWindows</code> are resized. 
 *
 *@status.stable
 *@author Ethan Royael Nicholas (<a href="mailto:ethan@ethannicholas.com">ethan@ethannicholas.com</a>)
 */
public class TextWindowEvent extends EventObject {
    /**
	 * 
	 */
	private static final long serialVersionUID = 7874802655168607042L;

	/** The event ID that indicates a resize event. */
    public static final int RESIZED  = 12000;
    
    private int id;

    /** 
     * Constructs a new <code>TextWindowEvent</code> with the specified source and id.
     *
     *@param source the <code>TextWindow</code> which generated this event
     *@param id the event ID
     */
    public TextWindowEvent(TextWindow source, int id) {
        super(source);
        this.id = id;
    }
    

    /** 
     * Returns the ID of this event.
     */
    public int getID() {
        return id;
    }
}
