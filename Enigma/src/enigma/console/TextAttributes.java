package enigma.console;

import java.awt.Color;

/** 
 * Attributes associated with a character in a <code>TextWindow</code>.  <code>TextAttributes</code>
 * are immutable, and the same instance may be shared by many characters.
 *
 *@status.stable
 *@author Ethan Royael Nicholas (<a href="mailto:ethan@ethannicholas.com">ethan@ethannicholas.com</a>)
 */
public class TextAttributes {
    private Color foreground;
    private Color background;


    /** 
     * Constructs a new <code>TextAttributes</code> with the specified foreground color
     * and a black background.
     *
     *@param foreground the foreground color
     *@throws NullPointerException if <code>foreground</code> is <code>null</code>
     */
    public TextAttributes(Color foreground) {
        this(foreground, Color.BLACK);
    }
        

    /** 
     * Constructs a new <code>TextAttributes</code> with the specified foreground and 
     * background colors.
     *
     *@param foreground the foreground color
     *@param background the background color
     *@throws NullPointerException if either <code>foreground</code> or <code>background</code> are <code>null</code>
     */
    public TextAttributes(Color foreground, Color background) {
        if (foreground == null)
            throw new NullPointerException("foreground must be non-null");
        if (background == null)
            throw new NullPointerException("background must be non-null");
        this.foreground = foreground;
        this.background = background;
    }
    
    
    /** 
     * Returns the foreground color of this <code>TextAttributes</code>.
     *
     *@return the foreground color
     */
    public Color getForeground() {
        return foreground;
    }
    

    /** 
     * Returns the background color of this <code>TextAttributes</code>.
     *
     *@return the background color
     */
    public Color getBackground() {
        return background;
    }
    
    
    /** 
     * Converts this object to a <code>String</code> representation.
     *
     *@return a <code>String</code> representing this object
     */
    public String toString() {
        return "TextAttributes(" + getForeground() + ", " + getBackground() + ")";
    }
}
