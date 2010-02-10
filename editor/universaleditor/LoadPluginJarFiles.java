package universaleditor;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

public class LoadPluginJarFiles {

	// Parameters
    private static final Class[] _parameters = new Class[]{URL.class};
	
	/**
     * Add file to CLASSPATH
     * @param s File name
     * @throws IOException  IOException
     */
    public static void addFile(String s) throws IOException {
        File f = new File(s);
        addFile(f);
    }

    /**
     * Add file to CLASSPATH
     * @param f  File object
     * @throws IOException IOException
     */
    public static void addFile(File f) throws IOException {
        addURL(f.toURL());
    }

    /**
     * Add URL to CLASSPATH
     * @param u URL
     * @throws IOException IOException
     */
    public static void addURL(URL u) throws IOException {

        URLClassLoader sysLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        URL urls[] = sysLoader.getURLs();
        
        int NUMBER_OF_JARS = urls.length;
        
        // Check to see if the jar is already in the classpath -
        for (int i = 0; i < NUMBER_OF_JARS; i++) 
        {
        	// Get the string that I'm going to add -
        	String strAdd = u.toString();
        	String strTmp = urls[i].toString();
        	if (strAdd.equalsIgnoreCase(strTmp))
        	{
        		// ok, we are already on the classpath -
        		return;
        	}
        	else
        	{
        		// Let the user know -
        		System.out.println("Adding "+strAdd+" to the classpath. Party on.");
        	}
        }
        
        Class sysclass = URLClassLoader.class;
        try {
            Method method = sysclass.getDeclaredMethod("addURL", _parameters);
            method.setAccessible(true);
            method.invoke(sysLoader, new Object[]{u});
        } catch (Throwable t) {
            t.printStackTrace();
            throw new IOException("Error, could not add URL to system classloader");
        }
    }
}
