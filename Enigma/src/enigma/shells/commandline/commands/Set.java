package enigma.shells.commandline.commands;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import enigma.core.Enigma;
import enigma.util.Util;

public class Set {
    public static void main(String[] args) {
        if (args.length == 0) {
            Util.println(Util.msg(Set.class, "usage.info"));
            return;
        }
        if (args.length == 1) {
            Pattern equals = Pattern.compile("\\s*(\\S*)\\s*=\\s*(\\S*)\\s*");
            Matcher matcher = equals.matcher(args[0]);
            if (matcher.matches())
                args = new String[] { matcher.group(1), matcher.group(2) };
            else {
                Util.println(Util.msg(Set.class, "wrong.argument.count", new Object[] { Arrays.asList(args) }));
                return;
            }
        }
    
        StringBuffer argString = new StringBuffer();
        for (int i = 1; i < args.length; i++) {
             argString.append(' ');
             argString.append(args[i]);
        }
    
        Enigma.getEnvironment().setProperty(args[0], argString.toString());
    }
}
