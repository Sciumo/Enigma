package enigma.console.remote;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;

import enigma.console.Console;
import enigma.console.TextAttributes;
import enigma.console.TextWindow;
import enigma.console.TextWindowNotAvailableException;
import enigma.core.Enigma;
import enigma.core.VirtualMachine;
import enigma.core.VirtualMachineConfiguration;
import enigma.util.Util;

/** 
 * An invisible <code>Console</code> which sends its data over streams to be 
 * displayed in a <code>RemoteConsoleClient</code>.
 *
 *@status.experimental
 *@author Ethan Royael Nicholas (<a href="mailto:ethan@ethannicholas.com">ethan@ethannicholas.com</a>)
 */
public class RemoteConsoleServer implements Console {
    private InputStream remoteClientIn;
    private OutputStream remoteClientOut;
    
    private InputStream inputStream;
    private PrintStream outputStream;
    private BufferedReader reader;
    private PrintWriter writer;
    
    private boolean passwordMode;
    
    private TextAttributes textAttributes;
    
    private static final char ESCAPE = '\032';

    // commands:
    //      R     read line    
    //      P     read password
    //      Fccc  change foreground color
    //      Bccc  change background color
    
    public RemoteConsoleServer(InputStream remoteClientIn, OutputStream remoteClientOut) {
        this.remoteClientIn  = remoteClientIn;
        this.remoteClientOut = remoteClientOut;

        inputStream = new FilterInputStream(remoteClientIn) {
            private void checkRead() throws IOException {
                if (available() == 0) {
                    if (passwordMode)
                        writer.write(ESCAPE + "P");
                    else
                        writer.write(ESCAPE + "R");
                    writer.flush();
                } 
            }
            
            public int read() throws IOException {
                checkRead();
                int result = super.read();
                if (result == -1)
                    System.exit(0);
                return result;
            }
            
            
            public int read(byte[] data) throws IOException {
                checkRead();
                int result = super.read(data);
                if (result == -1)
                    System.exit(0);
                return result;
            }


            public int read(byte[] data, int offset, int length) throws IOException {
                checkRead();
                int result = super.read(data, offset, length);
                if (result == -1)
                    System.exit(0);
                return result;
            }
        };
        
        outputStream = new PrintStream(remoteClientOut, true);
        
        try {
            reader = new BufferedReader(new InputStreamReader(inputStream, "utf-8")) {
                private void checkRead() throws IOException {
                    if (!ready()) {
                        if (passwordMode)
                            writer.write(ESCAPE + "P");
                        else
                            writer.write(ESCAPE + "R");
                        writer.flush();
                    } 
                }
        
                public int read(char[] cbuf, int off, int len) throws IOException {
                    checkRead();
                    return super.read(cbuf, off, len);
                }
            };
            
            writer = new PrintWriter(new OutputStreamWriter(outputStream, "utf-8"), true);
        }
        catch (UnsupportedEncodingException e) {
            throw new Error(e);
        }
        
        setTextAttributes(Enigma.getSystemTextAttributes("attributes.console.default"));
    }
   
    
    public String getTitle() {
        return null;
    }
    
    
    public void setTitle(String title) {
        
    }
        
        
    public synchronized String readLine() {
        try {
            return reader.readLine();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    
    public synchronized String readPassword() {
        passwordMode = true;
        try {
            return readLine();
        }
        finally {
            passwordMode = false;
        }
    }


    public Reader getReader() {
        return reader;
    }
    
    
    public PrintWriter getWriter() {
        return writer;
    }


    public InputStream getInputStream() {
        return inputStream;
    }
    

    public PrintStream getOutputStream() {
        return outputStream;
    }
    
    
    public boolean isTextWindowAvailable() {
        return false;
    }
    
    
    public TextWindow getTextWindow() throws TextWindowNotAvailableException {
        throw new TextWindowNotAvailableException();
    }
    
    
    public TextAttributes getTextAttributes() {
        return textAttributes;
    }
    
    
    private String getColorCode(Color color) {
        return Integer.toString(color.getRed() >> 4, 16) +
               Integer.toString(color.getGreen() >> 4, 16) +
               Integer.toString(color.getBlue() >> 4, 16);
    }
    
    
    public void setTextAttributes(TextAttributes textAttributes) {
        Color fg = textAttributes.getForeground();
        if (this.textAttributes == null || !fg.equals(this.textAttributes.getForeground())) {
            writer.flush();
            outputStream.print(ESCAPE + "F" + getColorCode(fg));
        }
            
        Color bg = textAttributes.getBackground();
        if (this.textAttributes == null || !bg.equals(this.textAttributes.getBackground())) {
            writer.flush();
            outputStream.print(ESCAPE + "B" + getColorCode(bg));
        }
        this.textAttributes = textAttributes;
    }
    
    
    private static void spawn(final Socket s, final String className, final String[] args) {
        final boolean[] die = new boolean[1];
        
        new Thread("Subprocess") {
            @SuppressWarnings("deprecation")
			public void run() {
                String address = s.getInetAddress().getHostAddress();
                Util.println(Util.msg(RemoteConsoleServer.class, "connection.received", new Object[] { address }));
                VirtualMachineConfiguration cfg = new VirtualMachineConfiguration(className, args);
                try {
                    final InputStream in = s.getInputStream();
                    final OutputStream out = s.getOutputStream();
                    final VirtualMachine vm = Enigma.createVirtualMachine(cfg);
                    
                    Thread inReader = new Thread("SubprocessReader") {
                        public void run() {
                            try {
                                InputStream in = vm.getInputStream();
                                int c;
                                byte[] buffer = new byte[256];
                                while ((c = in.read(buffer)) > 0 && !die[0])
                                    out.write(buffer, 0, c);
                            }
                            catch (Throwable e) {
                                if (!(e instanceof ThreadDeath))
                                    e.printStackTrace();
                                vm.destroy();
                            }
                        }
                    };
                    inReader.start();
        
                    Thread errorReader = new Thread("SubprocessReader") {
                        public void run() {
                            try {
                                InputStream in = vm.getErrorStream();
                                int c;
                                byte[] buffer = new byte[256];
                                while ((c = in.read(buffer)) > 0 && !die[0])
                                    out.write(buffer, 0, c);
                            }
                            catch (Throwable e) {
                                if (!(e instanceof ThreadDeath))
                                    e.printStackTrace();
                                vm.destroy();
                            }
                        }
                    };
                    errorReader.start();
                    
                    Thread outWriter = new Thread() {
                        public void run() {
                            try {
                                OutputStream out = vm.getOutputStream();
                                int c;
                                byte[] buffer = new byte[256];
                                while ((c = in.read(buffer)) > 0 && !die[0]) {
                                    out.write(buffer, 0, c);
                                    out.flush();
                                }
                            }
                            catch (Throwable e) {
                                if (!(e instanceof ThreadDeath) && !(e instanceof InterruptedException))
                                    e.printStackTrace();
                                vm.destroy();
                            }
                        }
                    };
                    outWriter.start();
                    vm.waitFor();

                    die[0] = true;
                    int count = 0;
                    while (inReader.isAlive() && count < 10) {
                        inReader.interrupt();
                        count++;
                    }

                    count = 0;
                    while (errorReader.isAlive() && count < 10) {
                        errorReader.interrupt();
                        count++;
                    }

                    count = 0;
                    while (outWriter.isAlive() && count < 10) {
                        outWriter.interrupt();
                        count++;
                    }
                    
                    if (inReader.isAlive())
                        inReader.stop();

                    if (errorReader.isAlive())
                        errorReader.stop();

                    if (outWriter.isAlive())
                        outWriter.stop();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                finally {
                    try {
                        s.close();
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }

                    Util.println(Util.msg(RemoteConsoleServer.class, "connection.dropped", new Object[] { address }));
                }
            }
        }.start();
    }
    
    
    public static void main(String[] args) throws IOException {
        Util.println("WARNING: Authentication is not implemented.  All connections will be allowed.");
        int port = 23;
        ServerSocket server = new ServerSocket(port);
        Util.println(Util.msg(RemoteConsoleServer.class, "listen.port", new Object[] { new Integer(port) }));
        for (;;) {
            Socket s = server.accept();
            spawn(s, RemoteProxyLauncher.class.getName(), args);
        }
    }
}
