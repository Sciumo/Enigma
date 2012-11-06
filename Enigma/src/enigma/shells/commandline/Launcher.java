package enigma.shells.commandline;

import java.io.*;

import enigma.console.remote.*;

public class Launcher {
    public static void main(String[] args) throws IOException {
        if (args == null || args.length == 0)
            CommandLineShell.main(args);
        else if (args[0].trim().toLowerCase().equals("-server"))
            RemoteConsoleServer.main(new String[] { CommandLineShell.class.getName() });
        else if (args[0].trim().toLowerCase().equals("-connect"))
            RemoteConsoleClient.main(new String[] { args[1] });
    }
}

