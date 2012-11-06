package enigma.shells.commandline.commands;

import java.awt.Color;

import enigma.console.TextAttributes;
import enigma.core.Enigma;
import enigma.util.Util;

public class Help {
    public static void main(String[] arg) {
        Util.println("'Real' help is not yet available in this preview version.");
        Util.println("However, feel free to play around with the following commands:");
        Util.println("");
        Enigma.getConsole().setTextAttributes(new TextAttributes(new Color(255, 128, 128), Color.black));
        Util.println("cd");
        Util.println("exit");
        Util.println("list");
        Util.println("mkdir");
        Util.println("say");
        Util.println("start");
        Util.println("type");
        Enigma.getConsole().setTextAttributes(Enigma.getSystemTextAttributes("attributes.console.default"));
        Util.println("");
        Util.println("A few other commands are supported, but they're not likely to");
        Util.println("be of much use yet.");
    }
}