package enigma.event;

import java.util.EventListener;

/**
 * A listener which may be notified when mouse motion actions occur on a <code>TextWindow</code>
 *
 *@status.stable
 *@see TextMouseEvent 
 *@see TextMouseAdapter
 */
public interface TextMouseMotionListener extends EventListener {
    /**
     * Invoked when the mouse has been moved on a <code>TextWindow</code>.
     *
     *@param e the <code>TextMouseEvent</code> object encapsulating the event
     */
    void mouseMoved(TextMouseEvent e);

    /**
     * Invoked when the mouse has been dragged on a <code>TextWindow</code>.
     *
     *@param e the <code>TextMouseEvent</code> object encapsulating the event
     */
    void mouseDragged(TextMouseEvent e);
}

