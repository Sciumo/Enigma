package enigma.core;

import java.io.*;

/** 
 * Reflects a Java virtual machine which was spawned as a sub-process.
 *
 *@status.experimental
 *@author Ethan Royael Nicholas (<a href="mailto:ethan@ethannicholas.com">ethan@ethannicholas.com</a>)
 */
public interface VirtualMachine {
    public InputStream getInputStream();
    public InputStream getErrorStream();
    public OutputStream getOutputStream();
    public void waitFor() throws InterruptedException;
    public void destroy();
}
