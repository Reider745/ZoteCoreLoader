package com.zhekasmirnov.horizon.launcher.env;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.stream.Stream;

import com.googlecode.d2j.dex.Dex2jar;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import com.zhekasmirnov.horizon.util.FileUtils;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.AccessFlag;

public class ClassLoaderPatch {
    private static final List<Object> patchedObjects = new ArrayList<>();

    public static Object newGenericArrayOfType(Class<?> cls, int i) {
        return Array.newInstance(cls, i);
    }

    public static synchronized void addNativeLibraryPath(ClassLoader classLoader, File file) {
        try {
            String currentlyPathes = System.getProperty("java.library.path", "");
            String libraryPath = file.getAbsolutePath();
            String[] pathes = currentlyPathes.split(File.pathSeparator);
            for (String path : pathes) {
                if (path.equals(libraryPath) || (path + "/").equals(libraryPath) || path.equals(libraryPath + "/")) {
                    return;
                }
            }
            if (currentlyPathes.length() == 0) {
                currentlyPathes = "" + libraryPath;
            } else {
                currentlyPathes += File.pathSeparator + libraryPath;
            }
            System.setProperty("java.library.path", currentlyPathes);
        } catch (RuntimeException e) {
            throw new RuntimeException("failed to patch classloader with following error", e);
        }
    }

    public static synchronized void addClasspath(ClassLoader classLoader, File file) {
        try {
            try {
                Field field = classLoader.getClass().getDeclaredField("source");
                field.setAccessible(true);
                if (field.get(classLoader) instanceof ClassPool pool) {
                    pool.appendClassPath(file.getAbsolutePath());
                } else {
                    throw new NoSuchFieldException();
                }
            } catch (NotFoundException | NoSuchFieldException nsm1) {
                try {
                    Method method = classLoader.getClass().getDeclaredMethod("addURL", URL.class);
                    method.setAccessible(true);
                    method.invoke(classLoader, file.toURI().toURL());
                } catch (NoSuchMethodException nsm2) {
                    Method method = classLoader.getClass()
                            .getDeclaredMethod("appendToClassPathForInstrumentation", String.class);
                    method.setAccessible(true);
                    method.invoke(classLoader, file.getAbsolutePath());
                }
            }
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | RuntimeException e) {
            ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
            if (classLoader != systemClassLoader) {
                addClasspath(systemClassLoader, file);
                return;
            }
            throw new RuntimeException("failed to patch classloader with following error", e);
        } catch (MalformedURLException e) {
            throw new RuntimeException("failed to patch classloader with following error", e);
        }
    }

    public static void addDexPath(ClassLoader classLoader, File file) {
        addDexPath(classLoader, file, false);
    }

    public static synchronized void addDexPath(ClassLoader classLoader, File file, boolean patchNativeMethods) {
        try {
            String filePath = file.getAbsolutePath();
            File jarFile = new File(
                    (filePath.endsWith(".dex") ? filePath.substring(0, filePath.length() - 4) : filePath) + ".jar");
            if (!jarFile.isFile()) {
                if (patchNativeMethods) {
                    File temporaryJarFile = new File(jarFile.getParentFile().getPath(), "tmp-" + jarFile.getName());
                    Dex2jar.from(file).to(temporaryJarFile.toPath());
                    patchNativeMethods(temporaryJarFile, jarFile);
                    try {
                        temporaryJarFile.delete();
                    } catch (SecurityException e) {
                        // ?
                    }
                } else {
                    Dex2jar.from(file).to(jarFile.toPath());
                }
            }
            addClasspath(classLoader, jarFile);
            return;
        } catch (CannotCompileException | NotFoundException | IOException e) {
            Logger.error("ClassLoaderPath", "Java library is broken or it class file is not supported yet!");
            Logger.error("ClassLoaderPath", e);
        }
        addClasspath(classLoader, file);
    }

    @SuppressWarnings("unused")
    private static Object removePatchesFromGenericArray(Object[] objArr, Class<?> cls) {
        ArrayList<Object> arrayList = new ArrayList<>();
        int i = 0;
        for (Object obj : objArr) {
            if (!patchedObjects.contains(obj)) {
                arrayList.add(obj);
            }
        }
        Object newGenericArrayOfType = newGenericArrayOfType(cls, arrayList.size());
        Iterator<Object> it = arrayList.iterator();
        while (it.hasNext()) {
            Object obj2 = it.next();
            ((Object[]) newGenericArrayOfType)[i] = obj2;
            i++;
        }
        return newGenericArrayOfType;
    }

    public static synchronized void revertClassLoaderPatches(ClassLoader classLoader) {
        throw new UnsupportedOperationException();
    }

    public static void patchNativeMethods(File jarFile, File outputJarFile)
            throws NotFoundException, IOException, CannotCompileException {
        final ClassPool classPool = new ClassPool();
        classPool.insertClassPath(jarFile.getPath());

        final File outputDirectory = new File(outputJarFile.getParentFile(), ".patch");
        final String outputDirectoryPath = outputDirectory.getPath();
        if (outputDirectory.exists()) {
            FileUtils.clearFileTree(outputDirectory, true);
        }
        outputDirectory.mkdirs();

        for (String className : fetchClassesFromJar(jarFile)) {
            CtClass ctClass = classPool.get(className);
            stoleNative(ctClass);
            ctClass.writeFile(outputDirectoryPath);
        }
        compressToJar(outputDirectory, outputJarFile, null);

        if (outputDirectory.exists()) {
            FileUtils.clearFileTree(outputDirectory, true);
        }
    }

    private static void stoleNative(CtClass ctClass) {
        if (ctClass.isFrozen()) {
            ctClass.defrost();
        }

        for (CtMethod method : ctClass.getDeclaredMethods()) {
            int modifiers = method.getModifiers();
            if ((modifiers & AccessFlag.NATIVE) != 0) {
                method.setModifiers(modifiers & ~AccessFlag.NATIVE);
            }
        }
    }

    public static List<String> fetchClassesFromJar(File jarPath) {
        ArrayList<String> classNames = new ArrayList<>();
        try {
            JarFile jarFile = new JarFile(jarPath);
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry jarEntry = entries.nextElement();
                if (jarEntry.getName().endsWith(".class")) {
                    classNames.add(
                            jarEntry.getName()
                                    .replace('/', '.')
                                    .replace(".class", ""));
                }
            }
            jarFile.close();
        } catch (IOException e) {
            throw new RuntimeException("An error occurred while reading the JAR file", e);
        }
        return classNames;
    }

    public static void compressToJar(File outputDirectory, File outputJarFile, Consumer<String> walker)
            throws IOException {
        final Manifest jarManifest = new Manifest();
        jarManifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");

        final String outputDirectoryPath = outputDirectory.getPath();
        final Path outputDirectoryRoot = Paths.get(outputDirectoryPath);
        Files.createDirectories(Paths.get(outputDirectoryPath, "META-INF"));
        Path manifestPath = Paths.get(outputDirectoryPath, "META-INF", "MANIFEST.MF");
        Files.deleteIfExists(manifestPath);

        try (final OutputStream outStream = Files.newOutputStream(manifestPath)) {
            jarManifest.write(outStream);
        }
        if (outputJarFile.exists()) {
            outputJarFile.delete();
        }

        try (final JarOutputStream jarStream = new JarOutputStream(new FileOutputStream(outputJarFile))) {
            try (final Stream<Path> walk = Files.walk(outputDirectoryRoot)) {
                walk.forEach(childPath -> {
                    final Path childFilePath = outputDirectoryRoot.relativize(childPath);
                    final String relativePath = childFilePath.toString().replace("\\", "/");

                    if (Files.isRegularFile(childPath)) {
                        final File childFile = childPath.toFile();
                        final JarEntry entry = new JarEntry(relativePath);
                        entry.setTime(childFile.lastModified());

                        try (final BufferedInputStream inStream = new BufferedInputStream(
                                new FileInputStream(childFile))) {
                            jarStream.putNextEntry(entry);
                            byte[] buffer = new byte[1024];
                            int count;
                            while (true) {
                                if ((count = inStream.read(buffer)) == -1) {
                                    break;
                                }
                                jarStream.write(buffer, 0, count);
                            }
                            jarStream.closeEntry();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    if (walker != null) {
                        walker.accept(relativePath);
                    }
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
