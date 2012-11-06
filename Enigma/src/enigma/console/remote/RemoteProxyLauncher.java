package enigma.console.remote;

import java.net.InetAddress;
import java.net.UnknownHostException;

import enigma.console.Console;
import enigma.core.Enigma;
import enigma.loaders.JavaProgramLoader;
import enigma.util.Util;

class RemoteProxyLauncher {
    public static boolean authenticate() {
        // not implemented, just allow all connections
        Console console = Enigma.getConsole();
        console.setTextAttributes(Enigma.getSystemTextAttributes("attributes.emphasis"));
        String address = Util.msg(RemoteProxyLauncher.class, "unknown.ip");
        try {
            address = InetAddress.getLocalHost().toString();
        }
        catch (UnknownHostException e) {
        }
        Util.println(Util.msg(RemoteProxyLauncher.class, "connected", new Object[] { address }));
        Util.println();
        console.setTextAttributes(Enigma.getSystemTextAttributes("attributes.console.default"));
        return true;
   }
    
    
    public static void main(String[] args) throws Exception {
        Enigma.installConsole(new RemoteConsoleServer(System.in, System.out));
        if (authenticate()) {
            String[] childArgs = new String[args.length - 1];
            System.arraycopy(args, 1, childArgs, 0, childArgs.length);
            JavaProgramLoader.runClass(args[0], childArgs);
        }
    }
}
