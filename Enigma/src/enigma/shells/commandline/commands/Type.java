package enigma.shells.commandline.commands;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import enigma.console.TextWindowNotAvailableException;
import enigma.core.Enigma;
import enigma.util.Util;

public class Type {
    public static void main(String[] args) {
        if (args.length == 0) {
            Util.println(Util.msg(Type.class, "usage.info"));
            return;
        }
        List<File> files = new ArrayList<File>();
        for (int i = 0; i < args.length; i++) {
            File[] expanded = Enigma.expandWildcards(args[i]);
            if (expanded == null || expanded.length == 0)
                Util.println(Util.msg(Type.class, "file.not.found", new String[] { args[i] }));
            else
                files.addAll(Arrays.asList(expanded));
        }
        Enigma.getConsole("type");
        type((File[]) files.toArray(new File[files.size()]));
    }
    
    
    public static void type(File[] paths) {
        if (paths.length == 1)
            type(paths[0]);
        else {
            for (int i = 0; i < paths.length; i++) {
                Util.println(Util.msg(Type.class, "file.name", new File[] { paths[i] }));
                Util.println();
                type(paths[i]);
                if (i < paths.length - 1)
                    Util.println();
            }
        }
    }
    
    
    public static void type(File path) {
        try {
            if (!path.exists()) {
                Util.println(Util.msg(Type.class, "file.not.found", new Object[] { path }));
                return;
            }
            if (path.isDirectory()) {
                Util.println(Util.msg(Type.class, "file.is.directory", new Object[] { path }));
                return;
            }
            InputStream file = new FileInputStream(path);
            byte[] buffer = new byte[2048];
            int c;
            while ((c = file.read(buffer)) > 0)
                System.out.write(buffer, 0, c);
            try {
                if (Enigma.getConsole().getTextWindow().getCursorX() != 0)
                    Util.println();
            }
            catch (TextWindowNotAvailableException e) {
                // ignore
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
