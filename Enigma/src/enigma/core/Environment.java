package enigma.core;

import java.io.*;
import java.util.*;

/** 
 * Encapsulates state information about the currently-running application,
 * such as its environment properties and current path.  <code>Environment</code>
 * objects are provided by {@link Enigma#getEnvironment}.
 *
 *@status.stable
 *@author Ethan Royael Nicholas (<a href="mailto:ethan@ethannicholas.com">ethan@ethannicholas.com</a>)
 */
public interface Environment {
    /** 
     * Returns the current working directory, according to this <code>Environment</code>.
     * All built-in Enigma services ignore <code>System's user.dir</code> property and
     * pay attention to the current <code>Environment's</code> working directory instead.
     * Changing <code>user.dir</code> is not safe, as some parts of <code>File</code>
     * pay attention to it and some do not, so the Enigma working directory is used
     * instead.
     *
     *@return the current working directory
     *@see #setCurrentPath
     */
    File getCurrentPath();
    
    
    /** 
     * Changes the current working directory for this <code>Environment</code>.
     * This has no effect on anything save the value returned by {@link #getCurrentPath}.
     * All built-in Enigma services ignore <code>System's user.dir</code> property and 
     * pay attention to the current <code>Environment's</code> working directory instead.
     * Changing <code>user.dir</code> is not safe, as some parts of <code>File</code>
     * pay attention to it and some do not, so the Enigma working directory is used
     * instead.
     *
     *@param path the new current working directory
     */
    void setCurrentPath(File path);
    
    
    /** 
     * Returns the current value of an environment property.  Environment properties
     * are exactly like the environment variables in other operating systems.
     *
     *@param key the name of the environment property to retrieve
     *@return the current value of the specified environment property
     *@see #setProperty
     */
    String getProperty(String key);
    
    
    /** 
     * Sets the current value of an environment property.  Environment properties
     * are exactly like the environment variables in other operating systems.
     *
     *@param key the name of the environment property to retrieve
     *@param value the new value of the specified environment property
     *@see #getProperty
     */
    void setProperty(String key, String value);
    
    
    /** 
     * Returns a map of all environment properties
     *
     *@return a map of all environment properties
     */
    Properties getProperties();
}
