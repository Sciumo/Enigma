package enigma.event;

import java.util.EventListener;

/**
 * A listener which may be notified when mouse actions occur on a <code>TextWindow</code>
 *
 *@status.stable
 *@see TextMouseEvent 
 *@see TextMouseAdapter
 */
public interface TextMouseListener extends EventListener {
    /**
     * Invoked when the mouse has been clicked on a <code>TextWindow</code>.
     *
     *@param e the <code>TextMouseEvent</code> object encapsulating the event
     */
    void mouseClicked(TextMouseEvent e);

    /**
     * Invoked when the mouse has been pressed on a <code>TextWindow</code>.
     *
     *@param e the <code>TextMouseEvent</code> object encapsulating the event
     */
    void mousePressed(TextMouseEvent e);

    /**
     * Invoked when the mouse has been released on a <code>TextWindow</code>.
     *
     *@param e the <code>TextMouseEvent</code> object encapsulating the event
     */
    void mouseReleased(TextMouseEvent e);
}

