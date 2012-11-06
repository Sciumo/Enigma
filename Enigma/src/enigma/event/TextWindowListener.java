package enigma.event;

import java.util.EventListener;

/** 
 * A listener for receiving TextWindowEvents.  TextWindowEvents are
 * fired in response to resize events.
 * 
 *@status.stable
 *@author Ethan Royael Nicholas (<a href="mailto:ethan@ethannicholas.com">ethan@ethannicholas.com</a>)
 */
public interface TextWindowListener extends EventListener {
    /** 
     * Invoked when the number of rows or columns in the
     * TextWindow change.  Some operations such as changing
     * font size may cause the TextWindow's displayed size
     * to alter without changing the number of rows or
     * columns displayed; no event is generated in this
     * case.
     */
    void textWindowResized(TextWindowEvent e);
}
