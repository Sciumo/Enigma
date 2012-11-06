package enigma.shells.commandline;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import enigma.console.Console;
import enigma.console.TextAttributes;
import enigma.console.TextWindow;
import enigma.core.Enigma;
import enigma.core.Environment;
import enigma.loaders.JavaProgramLoader;
import enigma.shells.commandline.commands.Start;
import enigma.util.Util;

/** 
 * The Enigma command-line shell.  When its <code>main</code> method is invoked, it
 * spawns a <code>Console</code> window (if one is not already available) and 
 * executes commands entered by the user.
 *
 *@status.stable
 *@author Ethan Royael Nicholas (<a href="mailto:ethan@ethannicholas.com">ethan@ethannicholas.com</a>)
 */
public class CommandLineShell {
    private static TextAttributes promptAttributes = Enigma.getSystemTextAttributes("attributes.shell.prompt");
    private static TextAttributes defaultAttributes = Enigma.getSystemTextAttributes("attributes.console.default");
    
    private Thread childThread;
    private Environment env;
    private Map<String,String> compatibilityAliases = new HashMap<String,String>();
    
    
    /** Constructs a new <code>CommandLineShell</code>. */
    public CommandLineShell() {
        compatibilityAliases.put("Ls", "List");
        compatibilityAliases.put("Dir", "List");
        compatibilityAliases.put("Echo", "Say");
        compatibilityAliases.put("Cat", "Type");
    }


    /** 
     * Spawns a <code>Console</code> window, if one is not already available, and 
     * repeatedly prompts the user to enter commands which are then executed.
     */
    public void runShell() {
        env = Enigma.getEnvironment();
        env.setProperty("path", new File(new File(System.getProperty("java.home")).getParent(), "bin").getPath());

        String title = Util.msg(CommandLineShell.class, "console.title");
        Console console = Enigma.getConsole(title);
        Util.println("This is a preview version of the Enigma shell, intended");
        Util.println("to demonstrate some of the Console API's capabilities.");
        Util.println("Command support is minimal at the moment.");
        Util.println();
        if (console.isTextWindowAvailable()) {
            TextWindow window = console.getTextWindow();
            window.addKeyListener(new KeyAdapter() {
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_CANCEL)
                        killCurrentOperation();
                }
            });
        }
                
        for (;;) {
            try {
                displayPrompt();
                processCommand(console.readLine());
                console.setTitle(title);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    
    /** Displays the prompt on the console. */
    protected void displayPrompt() {
        Console console = Enigma.getConsole();
        console.setTextAttributes(promptAttributes);
        System.out.print(env.getCurrentPath() + " $ ");
        console.setTextAttributes(defaultAttributes);
    }
    
    
    /** 
     * Processes the specified command line.  The command line is parsed into tokens,
     * delimited by spaces, with quoted sections treated as a single token.  The first
     * token is first checked to see if it is the name of an internal command, and
     * failing that, the path is checked for external commands of that name.  The
     * remaining tokens are passed as arguments to the child process.  
     * <p><code>processCommand</code> blocks until the child process returns, although
     * the <code>start</code> command may be used to spawn a child process which will 
     * run in parallel.</p>
     *
     *@param commandLine the command line to process
     *@see #processCommand(String[])
     *@see Start
     */
    public void processCommand(String commandLine) {
        commandLine = commandLine.trim();
        if (commandLine.length() == 2 && commandLine.charAt(1) == ':')
            commandLine = "cd " + commandLine;
        List<String> tokens = new ArrayList<String>();
        StringBuffer currentToken = new StringBuffer();
        int state = 0;
        int length = commandLine.length();
        for (int i = 0; i < length; i++) {
            char c = commandLine.charAt(i);
            switch (state) {
                case 0: // normal text
                    if (c == '"')
                        state = 1;
                    else if (c == ' ' || c == '|' || c == '<' || c == '>') {
                        if (currentToken.length() > 0)
                            tokens.add(currentToken.toString());
                        currentToken.setLength(0);
                        if (c != ' ')
                            tokens.add(String.valueOf(c));
                    }
                    else
                        currentToken.append(c);
                    break;
                case 1: // in double-quoted section
                    if (c == '"') {
                        if (currentToken.length() > 0)
                            tokens.add(currentToken.toString());
                        currentToken.setLength(0);
                        state = 0;
                    }
                    else
                        currentToken.append(c);
                    break;
                default: //assert false : "illegal state";
            }
        }
        if (state == 1) // unmatched quote, insert as literal quote
            currentToken.insert(0, '"');
        if (currentToken.length() > 0)
            tokens.add(currentToken.toString());
        processCommand((String[]) tokens.toArray(new String[tokens.size()]));
    }
    

    // changes the first character to uppercase and all others to lowercase    
    private String normalize(String commandName) {
        commandName = commandName.toLowerCase();
        if (commandName.length() > 0)
            commandName = Character.toUpperCase(commandName.charAt(0)) + commandName.substring(1);
        return commandName;
    }
    
    
    /** 
     * Processes the specified command line, which is represented as a sequence of 
     * tokens.  The first token is first checked to see if it is the name of an internal 
     * command, and failing that, the path is checked for external commands of that name.  
     * The remaining tokens are passed as arguments to the child process.  
     * <p><code>processCommand</code> blocks until the child process returns, although
     * the <code>start</code> command may be used to spawn a child process which will 
     * run in parallel.</p>
     *
     *@param tokens the tokens representing a parsed command line 
     *@see #processCommand(String)
     *@see Start
     */
    @SuppressWarnings("deprecation")
	public void processCommand(String[] tokens) {
        if (tokens.length == 0)
            return;
        String command = normalize(tokens[0]);
        if (compatibilityAliases.containsKey(command)) {
            String newCommand = (String) compatibilityAliases.get(command);
            Util.println(Util.msg(getClass(), "compatibility.alias", 
                                new String[] { command.toLowerCase(), newCommand.toLowerCase() }));
            Util.println();
            command = newCommand;
        }
        
        try {
            String className = "enigma.shells.commandline.commands." + command;
            final Class<?> clazz = Class.forName(className);
            final String[] args = new String[tokens.length - 1];
            System.arraycopy(tokens, 1, args, 0, args.length);
            childThread = new Thread() {
                public void run() {
                    JavaProgramLoader.runClass(clazz, args);
                }
            };
            childThread.start();
            try {
                childThread.join();
            }
            catch (InterruptedException e) {
                childThread.stop();
            }
            Util.println();
        }
        catch (ClassNotFoundException e) { // not a built-in command
            processExternalCommand(tokens);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    

    // returns the prefix to use with Runtime.exec for the specified file, or null if non-executable    
    private String getInvokerFor(File file) {
        String name = file.getName();
        String ext = name.indexOf(".") != -1 ? name.substring(name.indexOf(".")) : "";
        if (ext.equals(".exe"))
            return "";
        else if (ext.equals(".jar"))
            return "java -jar ";
        else if (ext.equals(".bat"))
            return "";
        else if (ext.equals(""))
            return "";
        return null;
    }    
    
    
    // locates a valid executable with fileName as either the full name or prefix
    private File scanDirectoryForExecutable(File directory, String fileName) {
        boolean caseSensitive = Enigma.isFileSystemCaseSensitive(directory);
        String fileNameDot = fileName + ".";
        File[] files = directory.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                if (!files[i].isFile())
                    continue;
                String name = files[i].getName();
                if (!caseSensitive)
                    name = name.toLowerCase();
                if (name.equals(fileName) || name.startsWith(fileNameDot)) {
                    if (getInvokerFor(files[i]) != null)
                        return files[i];
                }
            }
        }
        return null;
    }
    

    // runs an external command (OS executable or shell script)    
    @SuppressWarnings("deprecation")
	private void processExternalCommand(final String[] tokens) {
        // FIXME: searching broken for mixed case-sensitive / insensitive systems
        if (!Enigma.isFileSystemCaseSensitive(env.getCurrentPath()))
            tokens[0] = tokens[0].toLowerCase();
        File match = null;
        
        // look for literal path
        File possible = Enigma.resolvePath(tokens[0]);
        if (possible.getName().indexOf(".") == -1) {
            match = scanDirectoryForExecutable(possible.getParentFile(), possible.getName());
        }
        else if (possible.exists()) {
            if (getInvokerFor(possible) != null)
                match = possible;
        }
        
        // if not found yet, scan path
        String path = env.getProperty("path");
        if (match == null && path != null) {
            StringTokenizer st = new StringTokenizer(path, System.getProperty("path.separator"));
            while (st.hasMoreTokens()) {
                File dir = Enigma.resolvePath(st.nextToken().trim());
                match = scanDirectoryForExecutable(dir, tokens[0]);
            }
        }
        
        if (match == null) {
            Util.println(Util.msg(getClass(), "command.not.found", new String[] { tokens[0] }));
            Util.println();
            return;
        }
        final String className = "enigma.shells.commandline.commands.Exec";
        tokens[0] = getInvokerFor(match) + match.getPath();
        childThread = new Thread() {
            public void run() {
                JavaProgramLoader.runClass(className, tokens);
            }
        };
        childThread.start();
        try {
            childThread.join();
        }
        catch (InterruptedException e) {
            childThread.stop();
        }
    }    
    
    
    /** 
     * Terminates the currently-running child process, which allows the shell to
     * prompt for the next command.
     */
    @SuppressWarnings("deprecation")
	public synchronized void killCurrentOperation() {
        Util.println("**** Killing child process");
        if (childThread != null) {
            int count = 0;
            // try interrupting it first
            try {
                while (childThread != null && childThread.isAlive() && count++ < 10) {
                    childThread.interrupt();
                    childThread.join(50);
                }
            }
            catch (InterruptedException e) {
            }
            
            while (childThread != null && childThread.isAlive() && count++ < 20)
                childThread.stop();
                
            childThread = null;
        }
    }
    

    /** 
     * Invokes the <code>runShell</code> method on a newly-created <code>CommandLineShell</code>. 
     *
     *@see #runShell
     */    
    public static void main(String[] arg) {
        new CommandLineShell().runShell();
    }
}