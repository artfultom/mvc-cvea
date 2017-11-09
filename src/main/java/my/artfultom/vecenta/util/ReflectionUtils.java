package my.artfultom.vecenta.util;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class ReflectionUtils {

    public static List<Class> findAllClassesInPackage(String packageName) throws IOException, ClassNotFoundException {
        List<Class> result = new ArrayList<>();

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        Enumeration<URL> resources = classLoader.getResources(packageName.replace('.', '/'));

        List<File> dirs = new ArrayList<>();

        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();

            dirs.add(new File(resource.getFile()));
        }

        for (File directory : dirs) {
            result.addAll(findClasses(directory, packageName));
        }

        return result;
    }

    private static List<Class> findClasses(File directory, String packageName) throws ClassNotFoundException {
        List<Class> classes = new ArrayList<>();

        if (directory.exists()) {
            File[] files = directory.listFiles();

            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        classes.addAll(findClasses(file, packageName + "." + file.getName()));
                    } else {
                        if (file.getName().endsWith(".class")) {
                            classes.add(Class.forName(
                                    packageName + '.' + file.getName().substring(0, file.getName().length() - 6)
                            ));
                        }
                    }
                }
            }
        }

        return classes;
    }
}
