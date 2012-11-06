package enigma.core;


import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.Permission;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import enigma.console.Console;
import enigma.console.DefaultConsoleImpl;
import enigma.console.TextAttributes;
import enigma.console.TextWindow;
import enigma.console.java2d.Java2DTextWindow;
import enigma.core.windows.WindowsEnvironmentImpl;
import enigma.util.Util;

/** 
 * Provides general Enigma services, such wildcard expansion and <code>Console</code>
 * creation.
 *
 *@status.unstable
 *@author Ethan Royael Nicholas (<a href="mailto:ethan@ethannicholas.com">ethan@ethannicholas.com</a>)
 */
public class Enigma {
    private static final Permission SET_ENVIRONMENT_PERMISSION = new RuntimePermission("enigma.core.Enigma.setEnvironment");
    private static final Permission INSTALL_CONSOLE_PERMISSION = new RuntimePermission("enigma.core.Enigma.installConsole");
    
    private static Properties properties;
    private static Environment environment;
    private static Console console;
    private static Random random = new Random();
    
    // non-instantiable
    private Enigma() { }
    
    
    /** Returns the <code>Environment</code> associated with the current application. */
    public static Environment getEnvironment() {
        if (environment == null)
            environment = new WindowsEnvironmentImpl();
        return environment;
    }
    
    
    /** 
     * Sets the <code>Environment</code> associated with the current application.
     * If there is a security manager, its <code>checkPermission</code> method is first 
     * called with a <code>RuntimePermission("enigma.core.Enigma.setEnvironment")</code> 
     * permission to see if it's okay to change the <code>Environment</code>. 
     *
     *@throws SecurityException if the security manager denies the request
     */
    public static void setEnvironment(Environment environment) throws SecurityException {
        if (System.getSecurityManager() != null)
            System.getSecurityManager().checkPermission(SET_ENVIRONMENT_PERMISSION);
        Enigma.environment = environment;
    }
    
    
    /** 
     * Returns the <code>Console</code> associated with the current virtual machine.  If a
     * <code>Console</code> does not yet exist, one will be created, and the <code>System.in, 
     * System.out, </code>and <code>System.err</code> streams will be mapped to the new 
     * <code>Console's InputStream</code> and <code>OutputStream</code>.  In a graphical  
     * environment, the <code>Console</code> will typically be displayed in a <code>Frame</code>.
     *
     *@return the <code>Console</code> associated with the current virtual machine, first creating one if necessary
     */
    public static Console getConsole() {    
        return getConsole(null);
    }
    
    
    /** 
     * Creates a new <code>TextWindow</code> appropriate for the current environment.
     *
     *@param title the display title of the <code>TextWindow</code>
     */
    public static TextWindow createTextWindow(String title) {
        final Java2DTextWindow textPane = new Java2DTextWindow(80, 30, 500);
        final JFrame f = new JFrame(title != null ? title : Util.msg(Enigma.class, "default.console.title"));
        f.setLocation(random.nextInt(100), random.nextInt(100));
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        final JScrollPane scrollPane = new JScrollPane(textPane);
        f.getContentPane().add(scrollPane);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                f.pack(); // force peer to be created so that insets are known
                f.setSize(f.getPreferredSize());
                f.setVisible(true);
                // workaround for 1.3.1: requestFocus not working unless run in another invokeLater
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        textPane.requestFocus();
                    }
                });
            }
        });
        return textPane;
    }
    
    /** 
     * Returns the <code>Console</code> associated with the current virtual machine.  If a
     * <code>Console</code> does not yet exist, one will be created with the specified
     * title, and the <code>System.in, System.out, </code>and <code>System.err</code> 
     * streams will be mapped to the new <code>Console's InputStream</code> and 
     * <code>OutputStream</code>.  If a <code>Console</code> already exists for the current
     * virtual machine, its title will be set to the specified title.  In a graphical 
     * environment, the <code>Console</code> will typically be displayed in a
     * <code>Frame</code>.
     *
     *@param title the preferred console title, or <code>null</code> to use the default
     *@return the <code>Console</code> associated with the current virtual machine, first creating one if necessary
     */
    public static Console getConsole(final String title) {
        // need to be able to use DISPOSE_ON_CLOSE and have better notification of
        // window close
        if (console == null) {
            AccessController.doPrivileged(new PrivilegedAction<Object>() {
                public Object run() {
                    installConsole(new DefaultConsoleImpl(createTextWindow(title)));
                    return null;
                }
            });
        }
        else if (title != null)
            console.setTitle(title);
        return console;
    }
    
    
    /**
     * Installs the specified <code>Console</code> as the current virtual machine's
     * default <code>Console</code>.  This method is normally invoked automatically
     * by <code>getConsole()</code>, and it should only be called manually when a
     * non-default <code>Console</code> is desired.  After calling 
     * <code>installConsole</code>, subsequent calls to <code>getConsole()</code>
     * will return the newly-installed <code>Console</code>.
     *
     *@param console the <code>Console</code> to install
     *@throws SecurityException if the security manager denies the request
     *@see #getConsole
     */
    public static void installConsole(Console console) {
        if (System.getSecurityManager() != null)
            System.getSecurityManager().checkPermission(INSTALL_CONSOLE_PERMISSION);
        Enigma.console = console;
        System.setIn(console.getInputStream());
        System.setOut(console.getOutputStream());
        System.setErr(console.getOutputStream());
    }


    /** 
     * Returns <code>true</code> if the filesystem on which the specified
     * file lives is case-sensitive.  Case-preserving but case-insensitive
     * (as with Microsoft Windows) file systems return <code>false</code>;
     * the only issue in question here is whether two filenames that 
     * differ only by case are considered to be the same file or two 
     * different files.
     *
     *@param path the file whose filesystem is being examined
     *@return <code>true</code> if the specified file's filesystem is case-sensitive, <code>false</code> otherwise.
     */
    public static boolean isFileSystemCaseSensitive(File path) {
        return false;
    }


    /** 
     * Resolves the specified absolute or relative path against the 
     * <code>Environment's</code> current working directory to compute a 
     * full filename.  If the path is absolute, the path itself is returned 
     * (as a <code>File</code>), otherwise the path is treated as relative 
     * to the current working directory.  Use of this method is useful for 
     * proper pathname handling, as the presence of drive letter prefixes 
     * under Microsoft Windows can otherwise require special-casing.
     *
     *@param path
     *@see Environment#getCurrentPath
     *@see #expandWildcards
     */
    public static File resolvePath(String path) {
        if (path.length() == 2 && path.charAt(1) == ':')
            path += "\\";
        File file = new File(path);
        if (file.isAbsolute() || path.startsWith(File.separator))
            return file;
        return new File(getEnvironment().getCurrentPath(), path);
    }
    
    
    private static boolean checked14;
    private static boolean is14;
    
    private static void check14() {
        if (!checked14) {
            checked14 = true;
            StringTokenizer st = new StringTokenizer(System.getProperty("java.specification.version"), ".");
            try {
                int major = Integer.parseInt(st.nextToken());
                int minor = Integer.parseInt(st.nextToken());
                is14 = major > 1 || (major == 1 && minor >= 4);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        if (!is14)
            throw new UnsupportedOperationException("this operation requires Java 1.4 or higher");
    }
    
    
    /** 
     * Expands a potentially-wildcard-containing path into an array of files.
     * Ordinary Unix rules are followed, in that an asterisk ('*') matches 
     * zero or more characters, and a question mark ('?') matches any single
     * character.
     * <p>If the path is relative, it is resolved against the
     * current working directory (see {@link Environment#getCurrentPath}).  
     * If it is a simple file name (i.e. doesn't contain wildcard characters) 
     * which refers to an existing file, then a single-element array containing
     * the specified file is returned.  If the wildcard expression or
     * simple file name does not match any existing files, a zero-element
     * array is returned (never <code>null</code>).
     *
     *@param raw the path expression to be expanded
     *@return an array of <code>Files</code> referred to by the path expression
     *@see Environment#getCurrentPath
     *@see #resolvePath
     */
    public static File[] expandWildcards(String path) {
        check14();
        // use reflection to access method to avoid getting an error in 1.3
        // during classloading
        try {
            Class<Java14Utilities> utils = Java14Utilities.class;
            Method expandWildcards = utils.getMethod("expandWildcards", new Class[] { String.class });
            return (File[]) expandWildcards.invoke(null, new Object[] { path });
        }
        catch (Exception e) {
            throw new Error(e);
        }
    }


    /** 
     * Creates a new virtual machine which will run in parallel to the current virtual 
     * machine.
     *
     *@param cfg a <code>VirtualMachineConfiguration</code> specifying the parameters of the new virtual machine
     *@return a <code>VirtualMachine</code> object which allows you to interact with the newly-created virtual machine
     *@throws SecurityException if the security manager's <code>checkExec()</code> method denies the request
     *@status.experimental
     */
    public static VirtualMachine createVirtualMachine(VirtualMachineConfiguration cfg) throws VirtualMachineInstantiationException, SecurityException {
        try {
            File java = new File(System.getProperty("java.home"), "bin" + File.separator + "java");
            String program;
            if (cfg.getMainClassName() != null) {
                program = cfg.getMainClassName();
            }
            else if (cfg.getJarFile() != null) {
                program = "-jar " + cfg.getJarFile();
            }
            else
                throw new IllegalArgumentException("main class or jar file must be defined");
            
            String[] args = cfg.getArguments();
            StringBuffer argString = new StringBuffer();
            if (args != null)
                for (int i = 0; i < args.length; i++) {
                    argString.append(' ');
                    argString.append(args[i]);
                }
            
            StringBuffer javaArgs = new StringBuffer();
            
            File[] classPath = cfg.getClassPath();
            if (classPath != null && classPath.length > 0) {
                javaArgs.append("-cp");
                for (int i = 0; i < classPath.length; i++) {
                    javaArgs.append(' ');
                    javaArgs.append(classPath[i]);
                }
            }
            
            final Process p = Runtime.getRuntime().exec(java + " " + javaArgs + " " + program + " " + argString);
            return new VirtualMachine() {
                public InputStream getInputStream() {
                    return p.getInputStream();
                }

                
                public InputStream getErrorStream() {
                    return p.getErrorStream();
                }

                
                public OutputStream getOutputStream() {
                    return p.getOutputStream();
                }
                
                
                public void waitFor() throws InterruptedException {
                    p.waitFor();
                }
                
                
                public void destroy() {
                    p.destroy();
                }
            };
        }
        catch (Exception e) {
            throw new VirtualMachineInstantiationException(e.toString());
        }            
    }
    
    
    public static String getSystemProperty(String key) {
        if (properties == null) {
            try {
                properties = new Properties();
                properties.load(Enigma.class.getResourceAsStream("resources/properties/defaults.properties"));
            }
            catch (IOException e) {
                throw new RuntimeException(e.toString());
            }
        }
        
        return properties.getProperty(key);
    }
    
    
    private static TextAttributes parseTextAttributes(String value) {
        StringTokenizer st = new StringTokenizer(value, ", \t");
        int tokens = st.countTokens();
        if (tokens != 3 && tokens != 6)
            throw new IllegalArgumentException("expected three or six comma-separated numbers, got '" + value + "'");
        Color foreground = new Color(Integer.parseInt(st.nextToken()),
                                     Integer.parseInt(st.nextToken()),
                                     Integer.parseInt(st.nextToken()));
        Color background;
        if (tokens == 6)
            background = new Color(Integer.parseInt(st.nextToken()),
                                   Integer.parseInt(st.nextToken()),
                                   Integer.parseInt(st.nextToken()));
        else
            background = getSystemTextAttributes("attributes.console.default").getBackground();
        return new TextAttributes(foreground, background);
    }
    
    
    public static TextAttributes getSystemTextAttributes(String key) {
        return parseTextAttributes(getSystemProperty(key));
    }
    
    
    static class Java14Utilities {
        public static File[] expandWildcards(String path) {
            if (path.indexOf("*") != -1 || path.indexOf("?") != -1) {
                String parent;
                String pattern = path;
                if (path.indexOf(File.separator) != -1) {
                    parent = path.substring(0, path.lastIndexOf(File.separator));
                    pattern = path.substring(path.lastIndexOf(File.separator) + 1);
                }
                else
                    parent = getEnvironment().getCurrentPath().getPath();
                File parentFile = resolvePath(parent);
                File[] files = parentFile.listFiles();
                if (files == null)
                    return new File[0];
    
                boolean caseSensitive = isFileSystemCaseSensitive(parentFile);
                if (!caseSensitive)
                    pattern = pattern.toLowerCase();
                pattern = pattern.replaceAll("([\\.\\\\])", "\\\\$1").replaceAll("\\*", ".*").replaceAll("\\?", ".");
                Matcher matcher = Pattern.compile(pattern).matcher("");
                List<File> result = new ArrayList<File>();
                for (int i = 0; i < files.length; i++) {
                    String name = files[i].getName();
                    if (!caseSensitive)
                        name = name.toLowerCase();
                    matcher.reset(name);
                    if (matcher.matches())
                        result.add(files[i]);
                }
                return (File[]) result.toArray(new File[result.size()]);
            }
            else { // no wildcard characters in path
                File file = resolvePath(path);
                if (file.exists())
                    return new File[] { file };
                else
                    return new File[0];
            }
        }
    }
}