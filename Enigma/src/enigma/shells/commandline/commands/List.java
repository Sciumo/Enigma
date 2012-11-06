package enigma.shells.commandline.commands;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;

import enigma.console.Console;
import enigma.console.TextAttributes;
import enigma.core.Enigma;
import enigma.util.Util;

public class List {
    private static final String DIR = Util.msg(List.class, "directory");
    private static final NumberFormat SIZE_FORMAT = NumberFormat.getInstance();

    private static TextAttributes dirAttributes  = Enigma.getSystemTextAttributes("attributes.list.dir");
    private static TextAttributes sizeAttributes = Enigma.getSystemTextAttributes("attributes.list.size");
    private static TextAttributes dateAttributes = Enigma.getSystemTextAttributes("attributes.list.date");
    private static TextAttributes timeAttributes = Enigma.getSystemTextAttributes("attributes.list.time");
    private static TextAttributes nameAttributes = Enigma.getSystemTextAttributes("attributes.console.default");
    
    private int fileCount;
    private int dirCount;
    private long byteCount;
    
    private static final Comparator<Object> fileComparator = new Comparator<Object>() {
        public int compare(Object aRaw, Object bRaw) {
            File a = (File) aRaw;
            File b = (File) bRaw;
            boolean aIsDir = a.isDirectory();
            boolean bIsDir = b.isDirectory();
            if (aIsDir && !bIsDir)
                return -1;
            else if (bIsDir && !aIsDir)
                return 1;
            return String.CASE_INSENSITIVE_ORDER.compare(a.getName(), b.getName());
        }
    };
                
    
    public void list(String[] args) {
        Enigma.getConsole("list");
        if (args.length == 0) 
            args = new String[] { Enigma.getEnvironment().getCurrentPath().toString() };
        listPaths(args);
    }
    
    
    private void listPaths(String[] paths) {
        PrintWriter out = Enigma.getConsole().getWriter();
        if (paths.length == 1)
            list(paths[0]);
        else {
            for (int i = 0; i < paths.length; i++) {
                out.println(Util.msg(List.class, "path.name", new String[] { paths[i] }));
                out.println();
                list(paths[i]);
                if (i < paths.length - 1) {
                    Util.println();
                    Util.println();
                }
            }
        }
        Util.println(Util.msg(List.class, "list.totals", 
            new Object[] { new Integer(fileCount), new Integer(dirCount), new Long(byteCount) }));
    }
    
    
    private File[] getFiles(String path) throws FileNotFoundException {
        File file = Enigma.resolvePath(path);
        if (file != null && file.exists()) {
            if (file.isDirectory())
                return file.listFiles(); // path was single directory
            else
                return new File[] { file }; // path was single file
        }   
        
        // otherwise, look for wildcard path
        File[] result = Enigma.expandWildcards(path);
        if (result == null || result.length == 0)
            throw new FileNotFoundException(path);
        return result;
   }
   
    
    private void list(String path) {
        File[] files;
        try {
            files = getFiles(path);
            Arrays.sort(files, fileComparator);
        }
        catch (FileNotFoundException e) {
            Util.println(Util.msg(List.class, "file.not.found", new String[] { path }));
            return;
        }

        Console console = Enigma.getConsole();
        PrintWriter out = console.getWriter();
        for (int i = 0; i < files.length; i++) {
            printDate(console, files[i].lastModified());
            System.out.print(' ');
            if (files[i].isDirectory()) {
                console.setTextAttributes(dirAttributes);
                out.print(DIR);
                dirCount++;
            }
            else {
                console.setTextAttributes(sizeAttributes);
                out.print(pad(SIZE_FORMAT.format(files[i].length()), 12, ' '));
                fileCount++;
            }
            out.print(' ');
            console.setTextAttributes(nameAttributes);
            out.println(files[i].getName());
            byteCount += files[i].length();
        }
    }
    
    
    private static String pad(int value, int length, char pad) {
        return pad(String.valueOf(value), length, pad);
    }

    private static String pad(String value, int length, char pad) {
        StringBuffer result = new StringBuffer();
        result.append(value);
        while (result.length() < length)
            result.insert(0, pad);
        return result.toString();
    }
    
    
    private void printDate(Console console, long date) {
        Calendar c = Calendar.getInstance();
        c.setTime(new Date(date));
        PrintWriter out = console.getWriter();
        console.setTextAttributes(dateAttributes);
        out.print(pad(c.get(Calendar.MONTH) + 1, 2, ' ') + "-" + pad(c.get(Calendar.DAY_OF_MONTH), 2, '0') + "-" + 
               c.get(Calendar.YEAR) + " ");
        console.setTextAttributes(timeAttributes);
        out.print(pad(c.get(Calendar.HOUR), 2, ' ') + ":" + pad(c.get(Calendar.MINUTE), 2, '0') +
               (c.get(Calendar.AM_PM) == Calendar.AM ? 'a' : 'p'));
    }
    
    
    public static void main(String[] args) {
        new List().list(args);
    }
}