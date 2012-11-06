package enigma.loaders;

import enigma.console.Console;
import enigma.console.TextWindow;
import enigma.core.Enigma;
import enigma.util.Util;

/** 
 * Creates a <code>Console</code> in which to run a Java program.  The preferred method
 * of running Java programs in <code>Consoles</code> is to modify the program to request
 * a <code>Console</code>, but in cases where this is impossible or undesireable
 * <code>ConsoleProxyLoader</code> can be used to force the program to run in a
 * <code>Console</code>.
 *
 *@status.stable
 *@author Ethan Royael Nicholas (<a href="mailto:ethan@ethannicholas.com">ethan@ethannicholas.com</a>)
 *@see Console
 *@see TextWindow
 */
public class ConsoleProxyLoader {
    /** 
     * Runs the specified program in a <code>Console</code>.  The first parameter is the
     * title of the <code>Console</code>, and the second parameter is the name of the
     * class to run.  All other parameters are passed as arguments to the class'
     * <code>main</code> method.
     *
     *@param args title, class name, and other command-line arguments
     */
    public static void main(String[] args) {
        if (args.length < 2) {
            Util.println(Util.msg(ConsoleProxyLoader.class, "usage.information"));
            return;
        }
        String[] childArgs = new String[args.length - 2];
        System.arraycopy(args, 2, childArgs, 0, childArgs.length);
        Enigma.getConsole(args[0]);
        JavaProgramLoader.runClass(args[1], childArgs);
    }
}
