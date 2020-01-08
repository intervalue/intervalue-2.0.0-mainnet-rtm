package one.inve.localfullnode2.utilities.nativelib;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.rocksdb.util.Environment;

import com.sun.jna.Native;

/**
 * 
 * Copyright © INVE FOUNDATION. All rights reserved.
 * 
 * @ClassName: NativeLibraryLoader
 * @Description: This class is used to load the $libName shared library from
 *               within the jar
 * @author Francis.Deng [francis_xiiiv@163.com]
 * @date Jan 3, 2020
 *
 */
public class JNANativeLibraryLoader {
	private final String sharedLibraryName;
	private final String jniLibraryName;
	private final String jniLibraryFileName;
	private final String tempFilePrefix;
	private final String tempFileSuffix = Environment.getJniLibraryExtension();

	public JNANativeLibraryLoader(String libName) {
		this.sharedLibraryName = Environment.getSharedLibraryName(libName);
		this.jniLibraryName = Environment.getJniLibraryName(libName);
		this.jniLibraryFileName = Environment.getJniLibraryFileName(libName);
		this.tempFilePrefix = "lib" + libName + "jni";
	}

	@SuppressWarnings("unchecked")
	public synchronized <T> T load(Class<T> target) throws IOException {
		String tmpdir = System.getProperty("java.io.tmpdir");
		return load(tmpdir, target);
	}

	@SuppressWarnings("unchecked")
	public synchronized <T> T load(final String tmpDir, Class<T> target) throws IOException {
		try {
			return (T) Native.loadLibrary(sharedLibraryName, target);// e.g."libchroniclejni"
		} catch (final UnsatisfiedLinkError ule1) {
			try {
				return (T) Native.loadLibrary(jniLibraryName, target);// e.g."libchroniclejni-linux64"
			} catch (final UnsatisfiedLinkError ule2) {
				return loadLibraryFromJar(tmpDir, target);
			}
		}
	}

	@SuppressWarnings("unchecked")
	<T> T loadLibraryFromJar(final String tmpDir, Class<T> target) throws IOException {
		return (T) Native.loadLibrary(loadLibraryFromJarToTemp(tmpDir).getAbsolutePath(), target);
	}

	File loadLibraryFromJarToTemp(final String tmpDir) throws IOException {
		final File temp;
		if (tmpDir == null || tmpDir.isEmpty()) {
			temp = File.createTempFile(tempFilePrefix, tempFileSuffix);
		} else {
			temp = new File(tmpDir, jniLibraryFileName);
			if (temp.exists() && !temp.delete()) {
				throw new RuntimeException(
						"File: " + temp.getAbsolutePath() + " already exists and cannot be removed.");
			}
			if (!temp.createNewFile()) {
				throw new RuntimeException("File: " + temp.getAbsolutePath() + " could not be created.");
			}
		}

		if (!temp.exists()) {
			throw new RuntimeException("File " + temp.getAbsolutePath() + " does not exist.");
		} else {
			temp.deleteOnExit();
		}

		// attempt to copy the library from the Jar file to the temp destination
		try (final InputStream is = getClass().getClassLoader().getResourceAsStream(jniLibraryFileName)) {
			if (is == null) {
				throw new RuntimeException(jniLibraryFileName + " was not found inside JAR.");
			} else {
				Files.copy(is, temp.toPath(), StandardCopyOption.REPLACE_EXISTING);
			}
		}

		return temp;
	}

}
