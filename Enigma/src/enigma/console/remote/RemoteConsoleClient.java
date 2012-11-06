package enigma.console.remote;

import java.awt.Color;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import enigma.console.Console;
import enigma.console.TextAttributes;
import enigma.core.Enigma;

/** 
 * Connects via streams to a <code>RemoteConsoleServer</code> in order to display
 * the remote console's contents in the local console.
 * 
 *@status.experimental
 *@author Ethan Royael Nicholas (<a href="mailto:ethan@ethannicholas.com">ethan@ethannicholas.com</a>)
 */
public class RemoteConsoleClient {
    private Console console;
    private InputStream remoteServerIn;
    private OutputStream remoteServerOut;
    
    private static final char ESCAPE = '\032';

    public RemoteConsoleClient(Console console, InputStream remoteServerIn, OutputStream remoteServerOut) {
        if (console == null)
            throw new NullPointerException("console == null");
        if (remoteServerIn == null)
            throw new NullPointerException("remoteServerIn == null");
        if (remoteServerOut == null)
            throw new NullPointerException("remoteServerOut == null");
            
        this.console = console;
        this.remoteServerIn  = new BufferedInputStream(remoteServerIn);
        this.remoteServerOut = new BufferedOutputStream(remoteServerOut);
    }
    
    
    // badly in need of optimization...
    private byte getByte() throws IOException {
        int result = remoteServerIn.read();
        if (result == -1)
            System.exit(0);
        return (byte) result;
    }
    
    
    public void start() {
        new Thread("RemoteConsoleClient") {
            public void run() {
                console.getOutputStream();
                try {
                    for (;;) {
                        byte c = getByte();
                        if (c == ESCAPE)
                            processEscape();
                        else
                            console.getOutputStream().write(c);
                    }
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
    

    private Color parseColorCode(String colorCode) {
        return new Color(Character.digit(colorCode.charAt(0), 16) << 4,
                         Character.digit(colorCode.charAt(1), 16) << 4,
                         Character.digit(colorCode.charAt(2), 16) << 4);
    }
    
    
    private Color readColor() throws IOException {
        StringBuffer code = new StringBuffer(3);
        code.append((char) getByte());
        code.append((char) getByte());
        code.append((char) getByte());
        return parseColorCode(code.toString());
    }
    
    
    public void processEscape() throws IOException {
        byte c = getByte();
        switch (c) {
            case 'R': remoteServerOut.write((console.readLine() + "\n").getBytes());
                      remoteServerOut.flush();
                      break;
            case 'P': remoteServerOut.write((console.readPassword() + "\n").getBytes()); 
                      remoteServerOut.flush();
                      break;
            case 'F': console.setTextAttributes(new TextAttributes(readColor(), console.getTextAttributes().getBackground())); break;
            case 'B': console.setTextAttributes(new TextAttributes(console.getTextAttributes().getForeground(), readColor())); break;
            case ESCAPE: System.out.write(ESCAPE);
        }
    }
            
    
    
    public static void main(String[] arg) throws IOException {
        Socket s = new Socket(arg[0], 23);
        Console console = Enigma.getConsole();
        new RemoteConsoleClient(console, s.getInputStream(), s.getOutputStream()).start();
    }
}
