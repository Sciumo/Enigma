package enigma.shells.commandline.commands;

import java.io.*;
import java.util.*;

import enigma.core.*;
import enigma.util.Util;

public class Mkdir {
    public static void main(String[] args) {
        if (args.length == 0) {
            Util.println(Mkdir.class, "usage.info");
            return;
        }
        else if (args.length > 1) {
            Util.println(Mkdir.class, "too.many.arguments", new Object[] { Arrays.asList(args) });
            return;
        }
            
        File path = Enigma.resolvePath(args[0]);
        if (!path.exists()) {
            boolean result = path.mkdirs();
            if (!result)
                Util.println(Mkdir.class, "error.creating", new Object[] { path });
        }
        else
            Util.println(Mkdir.class, "path.already.exists", new Object[] { path });
    }
}
