package enigma.shells.commandline.commands;

import java.io.File;
import java.io.InputStream;
import java.io.PrintWriter;

import enigma.core.Enigma;
import enigma.util.Util;

/** 
 * Executes the specified external command.
 *
 *@status.stable
 *@author Ethan Royael Nicholas (<a href="mailto:ethan@ethannicholas.com">ethan@ethannicholas.com</a>)
 */
public class Exec {
    Process process;
    Thread inReader;
    Thread errorReader;
    Thread outWriter;
    
    /** Executes the specified external command. */
    @SuppressWarnings("deprecation")
	public void exec(String[] args) {
        try {
            Enigma.getConsole(new File(args[0]).getName());
            
            StringBuffer argString = new StringBuffer();
            for (int i = 0; i < args.length; i++) {
                 argString.append(' ');
                 argString.append(args[i]);
            }

            process = Runtime.getRuntime().exec(argString.toString(), null, Enigma.getEnvironment().getCurrentPath());
            inReader = new Thread() {
                public void run() {
                    try {
                        InputStream in = process.getInputStream();
                        int c;
                        byte[] buffer = new byte[256];
                        while ((c = in.read(buffer)) > 0)
                            System.out.write(buffer, 0, c);
                    }
                    catch (Throwable e) {
                        if (!(e instanceof ThreadDeath))
                            e.printStackTrace();
                    }
                }
            };
            inReader.start();

            errorReader = new Thread() {
                public void run() {
                    try {
                        InputStream in = process.getErrorStream();
                        int c;
                        byte[] buffer = new byte[256];
                        while ((c = in.read(buffer)) > 0)
                            System.err.write(buffer, 0, c);
                    }
                    catch (Throwable e) {
                        if (!(e instanceof ThreadDeath))
                            e.printStackTrace();
                    }
                }
            };
            errorReader.start();
            
            outWriter = new Thread() {
                public void run() {
                    try {
                        PrintWriter out = new PrintWriter(process.getOutputStream());
                        for (;;) {
                            String line = Enigma.getConsole().readLine();
                            out.println(line);
                            out.flush();
                        }
                    }
                    catch (Throwable e) {
                        if (!(e instanceof ThreadDeath))
                            e.printStackTrace();
                    }
                }
            };
            outWriter.start();
            
            try {
                process.waitFor();
            }
            catch (InterruptedException e) {
                Util.println("**** Destroying exec'ed process");
                process.destroy();
            }

            // shut down inReader
            try {
                int count = 0;
                while (inReader.isAlive() && count++ < 3) {
                    inReader.interrupt();
                    inReader.join(100);
                }
            }
            catch (InterruptedException e) {
            }
            if (inReader.isAlive())
                inReader.stop();

            // shut down errorReader
            try {
                int count = 0;
                while (errorReader.isAlive() && count++ < 3) {
                    errorReader.interrupt();
                    errorReader.join(100);
                }
            }
            catch (InterruptedException e) {
            }
            if (errorReader.isAlive())
                errorReader.stop();
            
            // shut down outWriter
            try {
                int count = 0;
                while (outWriter.isAlive() && count++ < 3) {
                    outWriter.interrupt();
                    outWriter.join(100);
                }
            }
            catch (InterruptedException e) {
            }
            if (outWriter.isAlive())
                outWriter.stop();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        catch (ThreadDeath e) { // no time, shut down violently
            if (process != null) {
                Util.println("*** Destroying exec'ed process");
                process.destroy();
            }
            if (inReader != null && inReader.isAlive())
                inReader.stop();
            if (errorReader != null && errorReader.isAlive())
                errorReader.stop();
            if (outWriter != null && outWriter.isAlive())
                outWriter.stop();
        }
    }
    
    
    public static void main(String[] args) {
        new Exec().exec(args);
    }
}
