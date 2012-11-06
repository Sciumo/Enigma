package enigma.shells.commandline.commands;

import java.io.PrintStream;

import enigma.console.terminal.AnsiOutputStream;
import enigma.core.Enigma;
import enigma.util.Util;

public class Ansi {
    public static void main(String[] arg) {
        if (arg.length == 0 || (!arg[0].equalsIgnoreCase("on") && !arg[0].equalsIgnoreCase("off"))) {
            Util.println(Util.msg(Ansi.class, "usage.info"));
            return;
        }
        
        if (arg[0].equalsIgnoreCase("on")) {
            System.setOut(new PrintStream(new AnsiOutputStream(Enigma.getConsole())));
            Util.println(Util.msg(Ansi.class, "support.enabled"));
        }
        else {
            System.setOut(Enigma.getConsole().getOutputStream());
            Util.println(Util.msg(Ansi.class, "support.disabled"));
        }            
    }
}
