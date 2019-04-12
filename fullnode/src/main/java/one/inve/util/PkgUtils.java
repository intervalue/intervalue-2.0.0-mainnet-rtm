package one.inve.util;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 *
 * Copyright © CHXX Co.,Ltd. All rights reserved.
 *
 * @Description: apply to cases that all classes in a directory and all classes
 *               in jars
 * @author: Francis.Deng
 * @date: 2018年11月17日 上午11:42:59
 * @version: V1.0
 */
public class PkgUtils {

	/**
	 * Scans all classes accessible from the context class loader which belong to
	 * the given package and subpackages.
	 *
	 * @param packageName The base package
	 * @return The classes
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	public static Class[] getClassesInDir(String packageName) throws ClassNotFoundException, IOException {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		assert classLoader != null;
		String path = packageName.replace('.', '/');
		Enumeration<URL> resources = classLoader.getResources(path);
		List<File> dirs = new ArrayList<File>();
		while (resources.hasMoreElements()) {
			URL resource = resources.nextElement();
			// System.out.println(resource);
			dirs.add(new File(resource.getFile()));
		}
		ArrayList<Class> classes = new ArrayList<Class>();
		for (File directory : dirs) {
			classes.addAll(findClasses(directory, packageName));
		}
		return classes.toArray(new Class[classes.size()]);
	}

	/**
	 * Recursive method used to find all classes in a given directory and subdirs.
	 *
	 * @param directory   The base directory
	 * @param packageName The package name for classes found inside the base
	 *                    directory
	 * @return The classes
	 * @throws ClassNotFoundException
	 */
	public static List<Class> findClasses(File directory, String packageName) throws ClassNotFoundException {
		List<Class> classes = new ArrayList<Class>();
		if (!directory.exists()) {
			return classes;
		}
		File[] files = directory.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				assert !file.getName().contains(".");
				classes.addAll(findClasses(file, packageName + "." + file.getName()));
			} else if (file.getName().endsWith(".class")) {
				classes.add(
						Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
			}
		}
		return classes;
	}

	/**
	 * <u>
	 * <li>first,assume all classes in jars</li>
	 * <li>second,assume all classes in directory if not first case</li> </u>
	 *
	 * @param packageName
	 * @return
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	public static Class[] getClasses(String packageName) throws ClassNotFoundException, IOException {
		Class[] classesInJar = getClassesInJar(packageName);

		return classesInJar.length != 0 ? classesInJar : getClassesInDir(packageName);
	}

	/**
	 * class is contained inside jar file in product environment
	 *
	 * @param packageName
	 * @return
	 * @throws ClassNotFoundException
	 */
	public static Class[] getClassesInJar(String packageName) throws ClassNotFoundException {
		ArrayList<Class> result = new ArrayList();
		ArrayList<File> directories = new ArrayList();
		HashMap packageNames = null;
		ClassLoader cld = Thread.currentThread().getContextClassLoader();

		try {
			for (URL jarURL : ((URLClassLoader) Thread.currentThread().getContextClassLoader()).getURLs()) {
				// System.out.println("JAR:" + jarURL.getPath());
				getClassesInSamePackageFromJar(result, packageName, jarURL.getPath());
				String path = packageName;

				Enumeration<URL> resources = cld.getResources(path);
				File directory = null;
				while (resources.hasMoreElements()) {
					String path2 = resources.nextElement().getPath();
					directory = new File(URLDecoder.decode(path2, "UTF-8"));
					directories.add(directory);
				}

				if (packageNames == null) {
					packageNames = new HashMap();
				}
				packageNames.put(directory, packageName);
			}

		} catch (NullPointerException x) {
			throw new ClassNotFoundException(
					packageName + " does not appear to be a valid package (Null pointer exception)");
		} catch (UnsupportedEncodingException encex) {
			throw new ClassNotFoundException(
					packageName + " does not appear to be a valid package (Unsupported encoding)");
		} catch (IOException ioex) {
			throw new ClassNotFoundException(
					"IOException was thrown when trying to get all resources for " + packageName);
		}

//	        for (File directory : directories) {
//	            if (directory.exists()) {
//	                String[] files = directory.list();
//	                for (String file : files) {
//	                    if (file.endsWith(".class")) {
//	                        try {
//							result.add(Class.forName(packageNames.get(directory).toString() + "."
//									+ file.substring(0, file.length() - 6)));
//	                        } catch (Throwable e) {
//
//	                        }
//	                    }
//	                }
//	            } else {
//	                throw new ClassNotFoundException(pckgname + " (" + directory.getPath() + ") does not appear to be a valid package");
//	            }
//	        }
		return result.toArray(new Class[result.size()]);

	}

	/**
	 * Returns the list of classes in the same directories as Classes in classes.
	 *
	 * @param result
	 * @param jarPath
	 *
	 */
	private static void getClassesInSamePackageFromJar(List<Class> result, String packageName, String jarPath) {
		JarFile jarFile = null;
		try {
			jarFile = new JarFile(jarPath);
			Enumeration<JarEntry> en = jarFile.entries();
			while (en.hasMoreElements()) {
				JarEntry entry = en.nextElement();
				String entryName = entry.getName();
				packageName = packageName.replace(".", "/");

				if (entryName != null && entryName.endsWith(".class") && entryName.startsWith(packageName)) {

					try {
						Class entryClass = Class
								.forName(entryName.substring(0, entryName.length() - 6).replace("/", "."));
						if (entryClass != null) {
							result.add(entryClass);
						}
					} catch (Throwable e) {
					}
				}
			}
		} catch (Exception e) {
		} finally {
			try {
				if (jarFile != null) {
					jarFile.close();
				}
			} catch (Exception e) {
			}
		}
	}
}
