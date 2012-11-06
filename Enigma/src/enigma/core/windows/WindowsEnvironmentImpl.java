package enigma.core.windows;

import java.io.File;
import java.util.Properties;

import enigma.core.Environment;

public class WindowsEnvironmentImpl implements Environment, Cloneable {
    private Properties properties = new Properties();
    private File currentPath;
    
    
    public WindowsEnvironmentImpl() {
        currentPath = new File(System.getProperty("user.dir"));
    }
    
    
    public File getCurrentPath() {
        return currentPath;
    }
    
    
    public void setCurrentPath(File currentPath) {
        this.currentPath = currentPath;
    }
    
    
    public String getProperty(String key) {
        return properties.getProperty(key);
    }
    
    
    public void setProperty(String key, String value) {
        properties.put(key, value);
    }
    
    
    public Properties getProperties() {
        return properties;
    }
    
    
    public Object clone() {
        try {
            WindowsEnvironmentImpl result = (WindowsEnvironmentImpl) super.clone();
            result.properties = (Properties) properties.clone();
            return result;
        }
        catch (CloneNotSupportedException e) {
            throw new Error(e.toString());
        }
    }
}
