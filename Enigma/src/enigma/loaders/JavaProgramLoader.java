package enigma.loaders;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


/** 
 * Executes Java programs in the current process.  This allows one to avoid manually
 * looking up and invoking the static <code>main</code> method on a class.
 *
 *@status.experimental
 *@author Ethan Royael Nicholas (<a href="mailto:ethan@ethannicholas.com">ethan@ethannicholas.com</a>)
 */
public class JavaProgramLoader {
    /** 
     * Invokes the <code>main</code> method of the specified class in the
     * current thread.
     *
     *@param clazz the class to run
     *@param args the arguments to the <code>main</code> method
     */
    public static void runClass(Class<?> clazz, String[] args) {
        try {
            Method main = clazz.getDeclaredMethod("main", new Class[] { String[].class });
            main.invoke(null, new Object[] { args });
        }
        catch (NoSuchMethodException e) {
            throw new NoSuchMethodError(e.toString());
        }
        catch (IllegalAccessException e) {
            throw new IllegalAccessError(e.toString());
        }
        catch (InvocationTargetException e) {
            final Throwable target = e.getTargetException();
            throw new RuntimeException() {
                /**
				 * 
				 */
				private static final long serialVersionUID = 4602938133153968637L;


				public void printStackTrace(PrintStream out) {
                    target.printStackTrace();
                }
                
                
                public void printStackTrace(PrintWriter out) {
                    target.printStackTrace();
                }
                
                
                public String toString() {
                    return target.toString();
                }
            };
        }                
    }
    
    
    /** 
     * Invokes the <code>main</code> method of the specified class in the
     * current thread.
     */
    public static void runClass(String className, String[] args) {
        try {
            Class<?> clazz = Class.forName(className);
            runClass(clazz, args);
        }
        catch (ClassNotFoundException e) {
            throw new NoClassDefFoundError(e.toString());
        }
    }
}