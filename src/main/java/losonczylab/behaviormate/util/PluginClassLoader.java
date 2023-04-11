package losonczylab.behaviormate.util;

import java.io.*;

/**
 * Placeholder
 */
public class PluginClassLoader extends ClassLoader {
    /**
     * Directory to load plugins from.
     */
    File directory;

    /**
     * Placeholder
     * @param dir Directory to load plugins from.
     */
    public PluginClassLoader (File dir) {
        //System.out.println("PluginsClassLoader constructor: dir = " + dir.toString());
        directory = dir;
    }

    /**
     * Placeholder
     *
     * @param name Placeholder
     * @return Placeholder
     * @throws ClassNotFoundException
     */
    public Class loadClass (String name) throws ClassNotFoundException {
        //System.out.println("PluginsClassLoader.loadClass: name = " + name);
        return loadClass(name, true);
    }

    /**
     * Placeholder
     *
     * @param classname Placeholder
     * @param resolve Placeholder
     * @return Placeholder
     * @throws ClassNotFoundException
     */
    public Class loadClass (String classname, boolean resolve) throws ClassNotFoundException {
        try {
            Class c = findLoadedClass(classname);

            if (c == null) {
                try { c = findSystemClass(classname); }
                catch (Exception ex) {}
            }

            if (c == null) {
                String filename = classname.replace('.', File.separatorChar)+".class";
                File f = new File(directory, filename);
                int length = (int) f.length();
                byte[] classbytes = new byte[length];
                DataInputStream in = new DataInputStream(new FileInputStream(f));
                in.readFully(classbytes);
                in.close();
                c = defineClass(classname, classbytes, 0, length);
            }

            if (resolve) {
                resolveClass(c);
            }

            return c;
        }
        catch (Exception ex) {
            throw new ClassNotFoundException(ex.toString());
        }
    }
}