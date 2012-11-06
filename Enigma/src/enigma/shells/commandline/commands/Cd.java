package enigma.shells.commandline.commands;

import java.io.*;
import java.util.*;

import enigma.core.*;
import enigma.util.Util;

/** 
 * Changes the current working directory.
 *
 *@status.stable
 *@author Ethan Royael Nicholas (<a href="mailto:ethan@ethannicholas.com">ethan@ethannicholas.com</a>)
 */
public class Cd {
    /** Changes the current working directory. */
    public static void main(String[] args) {
        if (args.length == 0) {
            Util.println(Util.msg(Cd.class, "usage.info"));
            return;
        }
        else if (args.length > 1) {
            Util.println(Util.msg(Cd.class, "too.many.arguments", new Object[] { Arrays.asList(args) }));
            return;
        }
            
        File path = Enigma.resolvePath(args[0]);
        if (path.exists()) {
            try {
                path = new File(path.getCanonicalPath());
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            Environment env = Enigma.getEnvironment();
            env.setCurrentPath(path);
        }
        else
            Util.println(Util.msg(Cd.class, "path.not.found", new Object[] { path }));
    }
}
