package enigma.console;

import java.awt.event.*;
import enigma.event.*;

/** 
 * <p>A rectangular grid of characters with support for keyboard and
 * mouse events.  <code>TextWindow</code> does not provide high-level 
 * operations such as text editing or stream input and output, although 
 * such operations can be layered on top of it.  The 
 * {@link Console Console class} wraps a <code>TextWindow</code>
 * to provide these high-level capabilities.</p>
 * <p>TextWindow is write-only: there is no way to read characters
 * back off of the screen.  This is partly to help enforce good design,
 * as that is seldom a reasonable thing to do, and partly because
 * it should be possible to implement <code>TextWindows</code> that
 * use a variety of terminal protocols, most of which do not offer
 * screen-reading functionality.</p>
 * <p>{@link enigma.console.java2d.Java2DTextWindow} provides an
 * implementation of TextWindow using the Java2D APIs for its
 * rendering.</p>
 *
 *@status.unstable
 *@see Console
 *@see DefaultConsoleImpl
 *@author Ethan Royael Nicholas (<a href="mailto:ethan@ethannicholas.com">ethan@ethannicholas.com</a>)
 */
public interface TextWindow {
    int CURSOR_INSERT = 0;
    int CURSOR_OVERSTRIKE = 1;
    int CURSOR_INVISIBLE = 2;


    /** 
     * Returns the title of this window.  Not all <code>TextWindow</code> implementations
     * will display the title for users to see.
     *
     *@see #setTitle
     */
    String getTitle();
    
    
    /** 
     * Sets the title of this window.  Not all <code>TextWindow</code> implementations
     * will display the title for users to see.
     *
     *@see #getTitle
     */
    void setTitle(String title);
    
    
    /** Returns the height of this window in rows. */
    int getRows();
    
    
   
    /** Returns the width of this window in rows. */
    int getColumns();
    
    
    /** 
     * Returns the column in which the cursor is currently positioned.
     * Cursor position is relevant both because it is generally displayed
     * to the user and because it normally controls where output will
     * show up.
     *
     *@return the column in which the cursor is currently positioned
     *@see #getCursorY
     */
    int getCursorX();
    
    
    /** 
     * Returns the row in which the cursor is currently positioned.
     *
     * Cursor position is relevant both because it is generally displayed
     * to the user and because it normally controls where output will
     * show up.
     *@return the row in which the cursor is currently positioned
     *@see #getCursorX
     */
    int getCursorY();
    
    
    /** 
     * Moves the cursor to the specified position.
     *
     *@param x the new column to position the cursor
     *@param y the new row to position the cursor
     *@throws IllegalArgumentException if the cursor would be positioned outside of the <code>TextWindow's</code> area
     *@see #getCursorX
     *@see #getCursorY
     */
    void setCursorPosition(int x, int y);
    
    
    /** 
     * Returns the cursor's current appearance.
     *
     *@return one of {@link #CURSOR_INVISIBLE}, {@link #CURSOR_INSERT}, or {@link #CURSOR_OVERSTRIKE}.
     *@see #setCursorType
     */
    int getCursorType();

    
    /** 
     * Sets the cursor's appearance.
     *
     *@param type one of {@link #CURSOR_INVISIBLE}, {@link #CURSOR_INSERT}, or {@link #CURSOR_OVERSTRIKE}.
     *@see #getCursorType
     */
    void setCursorType(int type);

    
    /** 
     * Adds a listener for <code>TextWindowEvents</code>.  <code>TextWindowEvents</code> are fired
     * in response to resize events.
     *
     *@param l the listener to add
     *@see #removeTextWindowListener
     */
    void addTextWindowListener(TextWindowListener l);
    

    /** 
     * Removes a <code>TextWindowListener</code>.  It is not an error to attempt to
     * remove a listener which is not currently registered.
     *
     *@param l the listener to remove
     *@see #addTextWindowListener
     */
    void removeTextWindowListener(TextWindowListener l);

    
    /** 
     * Adds a listener for <code>KeyEvents</code>.  Note that the use of the AWT 
     * <code>KeyListener</code> and <code>KeyEvent</code> classes in no way implies 
     * that the AWT is actually being used (or even available).
     *
     *@param l the listener to add
     *@see #removeKeyListener
     */
    void addKeyListener(KeyListener l);
    

    /** 
     * Removes a <code>KeyListener</code>.  It is not an error to attempt to
     * remove a listener which is not currently registered.
     *
     *@param l the listener to remove
     *@see #addKeyListener
     */
    void removeKeyListener(KeyListener l);

    
    /** 
     * Adds a listener for mouse events.  Not all <code>TextWindow</code> implementations
     * will necessarily offer mouse support.  <code>TextWindows</code> without mouse
     * support will allow mouse listeners to be added and removed from without error, but
     * will simply never notify the listeners of any events.
     * <p>It is important to note that the Y coordinate of a <code>TextMouseEvent</code>
     * may be negative when the user has scrolled a <code>TextWindow</code> backwards.
     * Negative Y coordinates indicate lines from the scrollback buffer.</p>
     *
     *@param l the listener to add
     *@see #removeTextMouseListener
     */
    void addTextMouseListener(TextMouseListener l);
    

    /** 
     * Removes a mouse listener.  It is not an error to attempt to
     * remove a listener which is not currently registered.
     *
     *@param l the listener to remove
     *@see #addTextMouseListener
     */
    void removeTextMouseListener(TextMouseListener l);

    
    /** 
     * Adds a listener for mouse motion events.  Not all <code>TextWindow</code> implementations
     * will necessarily offer mouse support.  <code>TextWindows</code> without mouse
     * support will allow mouse listeners to be added and removed from without error, but
     * will simply never notify the listeners of any events.
     * <p>It is important to note that the Y coordinate of a <code>TextMouseEvent</code>
     * may be negative when the user has scrolled a <code>TextWindow</code> backwards.
     * Negative Y coordinates indicate lines from the scrollback buffer.</p>
     *
     *@param l the listener to add
     *@see #removeTextMouseMotionListener
     */
    void addTextMouseMotionListener(TextMouseMotionListener l);
    

    /** 
     * Removes a mouse motion listener.  It is not an error to attempt to
     * remove a listener which is not currently registered.
     *
     *@param l the listener to remove
     *@see #addTextMouseMotionListener
     */
    void removeTextMouseMotionListener(TextMouseMotionListener l);

    
    /** 
     * <p>Outputs the specified character at the current cursor position, 
     * using the default attributes.  The cursor automatically advances to 
     * the next position, which may cause the cursor to wrap to the next 
     * line or the <code>TextWindow</code> to scroll.</p>
     * <p>If the character is a newline ('\n'),
     * the cursor is automatically advanced to the next line.  A tab ('\t')
     * is expanded to four spaces.  No other control characters are supported 
     * (for instance, a backspace character does not cause the cursor to move 
     * backwards), and unsupported control characters or other unprintable
     * characters will generally render as an error glyph.  All 
     * <code>TextWindow</code> implementations must support the entire ASCII
     * character set, but support for other character ranges is dependent
     * upon the particular <code>TextWindow</code> implementation.</p>
     * <p>This method always functions as if in overstrike mode, meaning 
     * that the character under the cursor is changed and all other 
     * characters are unaffected.</p>
     *
     *@param c the character to output, which should be a printable character, a tab, or a newline
     */
    void output(char c);

    
    /** 
     * As {@link #output(char)}, but uses the specified attributes rather
     * than the default.
     *
     *@param c the character to output, which should be a printable ASCII character, a tab, or a newline
     *@param attributes the <code>TextAttributes</code> with which to render the character
     *@throws NullPointerException if <code>attributes</code> is <code>null</code>
     */
    void output(char c, TextAttributes attributes);

    
    /** 
     * As {@link #output(char)}, but outputs the character at the specified
     * position and does not affect the cursor's position or cause the TextWindow
     * to scroll.
     *
     *@param x the column in which to output the character
     *@param y the row in which to output the character
     *@param c the character to output, which should be a printable ASCII character, a tab, or a newline
     */
    void output(int x, int y, char c);
    

    /** 
     * As {@link #output(int, int, char)}, but uses the specified attributes
     * rather than the default.
     *
     *@param x the column in which to output the character
     *@param y the row in which to output the character
     *@param c the character to output, which should be a printable ASCII character, a tab, or a newline
     *@param attributes the <code>TextAttributes</code> with which to render the character
     *@throws NullPointerException if <code>attributes</code> is <code>null</code>
     */
    void output(int x, int y, char c, TextAttributes attributes);

    
    /** 
     * As {@link #output(char)}, but outputs every character in the specified 
     * section of the character array.
     *
     *@param c the character array from which to output
     *@param offset the position from which to start writing characters
     *@param count the number of characters to output
     *@throws IllegalArgumentException if count is negative
     *@throws IndexOutOfBoundsException if offset is negative or offset + count is greater than the size of the array
     */
    void output(char[] c, int offset, int count);
    

    /** 
     * As {@link #output(char[], int, int)}, but uses the specified attributes
     * instead of the default.
     *
     *@param c the character array from which to output
     *@param offset the position from which to start writing characters
     *@param count the number of characters to output
     *@param attributes the <code>TextAttributes</code> with which to render the characters
     *@throws IllegalArgumentException if count is negative
     *@throws IndexOutOfBoundsException if offset is negative or offset + count is greater than the size of the array
     *@throws NullPointerException if <code>attributes</code> is <code>null</code>
     */
    void output(char[] c, int offset, int count, TextAttributes attributes);

    
    /** 
     * As {@link #output(char)}, but outputs every character in the specified 
     * string.
     *@param s the string to output
     */
    void output(String s);

    
    /** 
     * As {@link #output(String)}, but uses the specified attributes
     * instead of the default.
     *@param s the string to output
     *@param attributes the <code>TextAttributes</code> with which to render the characters
     *@throws NullPointerException if <code>attributes</code> is <code>null</code>
     */
    void output(String s, TextAttributes attributes);
    
    
    /** 
     * Similar to <code>output</code>, but requests that the <code>TextWindow</code>
     * paint the character without delay.  This method should only be used to display
     * characters as the user types them, so that they appear on the screen faster;
     * the disadvantage of this method is that overall throughput is greatly reduced,
     * so it should never be used to output more than one character per keystroke.
     * <p>The request to output the character immediately is merely a hint, and
     * <code>TextWindow</code> implementations are free to treat 
     * <code>outputImmediately</code> no differently from <code>output</code>.
     *
     *@param c the character to output
     *@param attributes the <code>TextAttributes</code> with which to render the character
     */
    void outputImmediately(char c, TextAttributes attributes);
    
    
    /** 
     * Inserts a blank row at the specified position, moving all prior rows
     * up by one.
     *
     *@param row the position at which to insert a row
     */
    void insertRow(int row);
    
    
    /** 
     * Scrolls the window up one page, if scrolling is supported and the window
     * is not already at the top of its scroll range.  Otherwise, this call is
     * ignored.
     */
    void pageUp();
    
    
    /** 
     * Scrolls the window down one page, if scrolling is supported and the window
     * is not already at the bottom of its scroll range.  Otherwise, this call is
     * ignored.
     */
    void pageDown();
}