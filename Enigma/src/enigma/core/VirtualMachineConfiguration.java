package enigma.core;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/** 
 * Parameters to use when invoking a new Java virtual machine.
 *
 *@status.experimental
 *@author Ethan Royael Nicholas (<a href="mailto:ethan@ethannicholas.com">ethan@ethannicholas.com</a>)
 */
public class VirtualMachineConfiguration {
    private String className;
    private File[] classPath;
    private File[] extDirs;
    private File jarFile;
    private String[] args;
    
    
    public VirtualMachineConfiguration(File jarFile) {
        this(jarFile, new String[0]);
    }
    
    
    public VirtualMachineConfiguration(File jarFile, String[] args) {
        this.args = args;
        this.jarFile = jarFile;
    }
    
    
    public VirtualMachineConfiguration(String className) {
        this(className, new String[0]);
    }
    
    
    public VirtualMachineConfiguration(String className, String[] args) {
        this(className, args, null);
    }
    
    
    public VirtualMachineConfiguration(String className, String[] args, File[] classPath) {
        this(className, args, classPath, null);
    }
    
        
    public VirtualMachineConfiguration(String className, String[] args, File[] classPath, File[] extDirs) {
        this.args      = args;
        this.className = className;
        this.classPath = classPath != null ? classPath : parsePaths(System.getProperty("java.class.path"));
        this.extDirs   = extDirs;
    }
    
    
    private static File[] parsePaths(String paths) {
        List<File> result = new ArrayList<File>();
        StringTokenizer st = new StringTokenizer(paths, File.pathSeparator);
        while (st.hasMoreTokens())
            result.add(new File(st.nextToken()));
        return (File[]) result.toArray(new File[result.size()]);
    }
    
    
    public String[] getArguments() {
        return args;
    }
    
    
    public File getJarFile() {
        return jarFile;
    }
    
    
    public String getMainClassName() {
        return className;
    }
    
    
    public File[] getClassPath() {
        return classPath;
    }
    
    public File[] getExtDirs() {
    	return extDirs;
    }
}
