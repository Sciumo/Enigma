package enigma.console;

import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;

/** 
 * A <code>Console</code> is a high-level text component, equivalent to an 
 * xterm window in Unix.  <code>Console</code> provides functionality such 
 * as text editing, command recall, and interfaces to streams and writers.  
 * The default implementation of <code>Console</code> uses a pluggable 
 * <code>TextWindow</code> for its low-level text support.
 *
 *@status.stable
 *@author Ethan Royael Nicholas (<a href="mailto:ethan@ethannicholas.com">ethan@ethannicholas.com</a>)
 *@see DefaultConsoleImpl 
 */
public interface Console {
    /** 
     * Returns the title of this console.  Not all <code>Console</code> implementations
     * will display the title for users to see.
     *
     *@see #setTitle
     */
    String getTitle();
    
    
    /** 
     * Sets the title of this console.  Not all <code>Console</code> implementations
     * will display the title for users to see.
     *
     *@see #getTitle
     */
    void setTitle(String title);
        
        
    /** 
     * Enters edit mode and reads a line of text.  Returns the entered line (which
     * may be of arbitrary length) when the user presses the Enter or Return key.
     */
    String readLine();
    
    
    /** 
     * Enters edit mode and reads a line of text, echoing asterisks ('*') instead
     * of the characters the user is typing.  Returns the entered line (which
     * may be of arbitrary length) when the user presses the Enter or Return key.
     */
    String readPassword();


    /** 
     * Returns a <code>Reader</code> which returns characters typed into this console.  
     * As with <code>System.in.readLine()</code>, editing is supported and text is not 
     * delivered until the user has pressed the Enter or Return key.
     *
     *@return an <code>Reader</code> which reads from this console
     *@see #getWriter
     *@see #getInputStream
     *@see #getOutputStream
     */
    Reader getReader();
    
    
    /** 
     * Returns a <code>PrintWriter</code> which sends its output to this console.
     *
     *@return a <code>PrintWriter</code> which sends its output to this console
     *@see #getReader
     *@see #getInputStream
     *@see #getOutputStream
     */
    PrintWriter getWriter();


    /** 
     * Returns an <code>InputStream</code> which returns characters typed into this 
     * console.  As with <code>System.in.readLine()</code>, editing is supported and 
     * text is not delivered until the user has pressed the Enter or Return key.
     *
     *@return an <code>InputStream</code> which reads from this console
     *@see #getOutputStream
     *@see #getReader
     *@see #getWriter
     */
    InputStream getInputStream();
    
    
    /** 
     * Returns a <code>PrintWriter</code> which sends its output to this console.
     *
     *@return a <code>PrintWriter</code> which sends its output to this console
     *@see #getInputStream
     *@see #getReader
     *@see #getWriter
     */
    PrintStream getOutputStream();
    
    
    /** 
     * <code>Consoles</code> generally also provide access to the low-level <code>TextWindow</code>
     * API, but they are not required to.  <code>isTextWindowAvailable</code> returns <code>true</code>
     * if <code>getTextWindow</code> will succeed, <code>false</code> if it will throw a
     * <code>TextWindowNotAvailableException</code>.
     *
     *@return <code>true</code> if <code>TextWindow</code> support is available, <code>false</code> otherwise
     */
    boolean isTextWindowAvailable();
    
    
    /** 
     * Returns the <code>TextWindow</code> to which this console is attached.
     * <code>Consoles</code> will generally provide <code>TextWindow</code> support,
     * but are not required to.  You may check for <code>TextWindow</code> availability
     * using {@link #isTextWindowAvailable}.  If no <code>TextWindow</code> is available,
     * <code>getTextWindow</code> will throw <code>TextWindowNotAvailableException</code>.
     *
     *@return the <code>TextWindow</code> this console is managing, or <code>null</code> if none
     *@throws TextWindowNotAvailableException if this console does not provide a <code>TextWindow</code>
     */
    TextWindow getTextWindow() throws TextWindowNotAvailableException;
    
    
    /** 
     * Returns the <code>TextAttributes</code> which will be applied to any
     * data sent to this console via its associated <code>PrintStream</code>
     * or <code>PrintWriter</code>.
     *
     *@return the <code>TextAttributes</code> currently in effect
     *@see #setTextAttributes
     *@see #getOutputStream
     *@see #getWriter
     *@see TextWindow
     */
    TextAttributes getTextAttributes();
    
    
    /** 
     * Sets the <code>TextAttributes</code> which will be applied to any
     * data sent to this console via its associated <code>PrintStream</code>
     * or <code>PrintWriter</code>.
     *
     *@param attributes the <code>TextAttributes</code> to apply to output
     *@see #getTextAttributes
     *@see #getOutputStream
     *@see #getWriter
     *@see TextWindow
     *@throws NullPointerException if <code>attributes</code> is <code>null</code>
     */
    void setTextAttributes(TextAttributes attributes);
}
