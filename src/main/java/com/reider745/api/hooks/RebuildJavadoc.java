package com.reider745.api.hooks;

import javassist.*;
import javassist.bytecode.AccessFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.stream.Stream;

public class RebuildJavadoc {
    private static final String[] progressHandles = new String[] { "::", ":.", ": ", ". ", "  ", " .", " :", ".:" };

    private static Logger logger;
    private Set<String> singletonClasses;
    private Set<String> rewrittenSingletonClasses;
    private int offset, length;

    public RebuildJavadoc() {
        if (logger == null) {
            logger = LoggerFactory.getLogger(RebuildJavadoc.class);
        }
        singletonClasses = null;
        rewrittenSingletonClasses = null;
    }

    private static ArrayList<String> fetchClassesFromJar(File jarPath) {
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
            logger.error("An error occurred while reading the JAR file", e);
        }
        return classNames;
    }

    private static String getStubValue(CtClass retType) {
        return switch (retType.getName()) {
            case "java.lang.String", "java.lang.CharSequence" -> "\"\"";
            case "byte" -> "(byte)0";
            case "short" -> "(short)0";
            case "int" -> "0";
            case "long" -> "0L";
            case "float" -> "0.0f";
            case "double" -> "0.0d";
            case "char" -> "'\\u0000'";
            case "boolean" -> "false";
            case "void" -> null;
            default -> "null";
        };
    }

    private static CtConstructor getMinimumConstructor(CtConstructor[] constructors) throws NotFoundException {
        int parameterCount = Integer.MAX_VALUE;
        CtConstructor minimumConstructor = null;
        for (CtConstructor constructor : constructors) {
            CtClass[] availableTypes = constructor.getParameterTypes();
            if (availableTypes.length < parameterCount) {
                minimumConstructor = constructor;
                if ((parameterCount = availableTypes.length) == 0) {
                    break;
                }
            }
        }
        return minimumConstructor;
    }

    private static String getMinimumConstructorParameters(CtClass ctClass) throws NotFoundException {
        StringBuilder parameters = new StringBuilder();
        CtClass superclazz = ctClass.getSuperclass();
        if (superclazz != null) {
            CtConstructor constructor = getMinimumConstructor(superclazz.getConstructors());
            if (constructor != null) {
                for (CtClass parameter : constructor.getParameterTypes()) {
                    if (!parameters.isEmpty()) {
                        parameters.append(", ");
                    }
                    parameters.append(getStubValue(parameter));
                }
            }
        }
        return parameters.toString();
    }

    private String getStubReturnForMethod(CtMethod method) throws NotFoundException {
        CtClass returnType = method.getReturnType();
        if ((method.getModifiers() & AccessFlag.STATIC) == 0 && returnType.equals(method.getDeclaringClass())) {
            return "this";
        }
        if (returnType.getName().contains(".")) {
            singletonClasses.add(returnType.getName());
        }
        return getStubValue(returnType);
    }

    private void overrideClass(CtClass ctClass)
            throws CannotCompileException, IOException, NotFoundException {
        if (ctClass.isFrozen()) {
            ctClass.defrost();
        }
        boolean isEnum = ctClass.isEnum();
        boolean isInterface = ctClass.isInterface();
        String classSignature = null;
        if (isEnum) {
            classSignature = ctClass.getName().replace(".", "/");
        }
        for (CtMethod method : ctClass.getDeclaredMethods()) {
            if (isEnum) {
                switch (method.getName()) {
                    case "values":
                        if (method.getSignature().equals("()[L" + classSignature + ";")) {
                            continue;
                        }
                        break;
                    case "valueOf":
                        if (method.getSignature().equals("(Ljava/lang/String;)L" + classSignature + ";")) {
                            continue;
                        }
                }
            }
            if (isInterface && method.isEmpty()) {
                continue;
            }
            int modifiers = method.getModifiers();
            if ((modifiers & AccessFlag.ABSTRACT) != 0) {
                continue;
            }
            if ((modifiers & AccessFlag.NATIVE) != 0) {
                method.setModifiers(modifiers & ~AccessFlag.NATIVE);
            }
            String desiredValue = "null";
            try {
                desiredValue = getStubReturnForMethod(method);
            } catch (NotFoundException e) {
                // ?
            }
            try {
                method.setBody(desiredValue != null ? "return " + desiredValue + ";" : ";");
            } catch (CannotCompileException e) {
                // ?
            }
        }
        String parameters = getMinimumConstructorParameters(ctClass);
        for (CtConstructor constructor : ctClass.getDeclaredConstructors()) {
            if (parameters.isEmpty()) {
                constructor.setBody(null);
            } else {
                constructor.setBody("super(" + parameters + ");");
            }
        }
    }

    private void constructSingletonClass(CtClass ctClass)
            throws NotFoundException, CannotCompileException, IOException {
        if (ctClass.isFrozen()) {
            ctClass.defrost();
        }
        if (ctClass.isEnum() || ctClass.isInterface() || (ctClass.getModifiers() & AccessFlag.ABSTRACT) != 0) {
            return;
        }
        CtConstructor minimumConstructor = getMinimumConstructor(ctClass.getDeclaredConstructors());
        if (minimumConstructor == null) {
            minimumConstructor = new CtConstructor(new CtClass[0], ctClass);
            String parameters = getMinimumConstructorParameters(ctClass);
            if (!parameters.isEmpty()) {
                minimumConstructor.setBody("super(" + parameters + ");");
            }
            ctClass.addConstructor(minimumConstructor);
        }
        StringBuilder parameters = new StringBuilder();
        for (CtClass parameter : minimumConstructor.getParameterTypes()) {
            if (!parameters.isEmpty()) {
                parameters.append(", ");
            }
            parameters.append(getStubValue(parameter));
        }
        CtField newInstanceField = new CtField(ctClass, "singletonInternalProxy", ctClass);
        newInstanceField.setModifiers(AccessFlag.PRIVATE | AccessFlag.STATIC);
        ctClass.addField(newInstanceField);
        CtMethod newInstance = new CtMethod(ctClass, "getSingletonInternalProxy", new CtClass[0], ctClass);
        newInstance.setExceptionTypes(minimumConstructor.getExceptionTypes());
        newInstance.setModifiers(AccessFlag.PUBLIC | AccessFlag.STATIC);
        StringBuilder bodyBuilder = new StringBuilder();
        bodyBuilder.append("{ if (singletonInternalProxy == null) { singletonInternalProxy = new ");
        if (parameters.isEmpty()) {
            bodyBuilder.append(ctClass.getName() + "()");
        } else {
            bodyBuilder.append(ctClass.getName() + "(" + parameters.toString() + ")");
        }
        bodyBuilder.append("; } return singletonInternalProxy; }");
        newInstance.setBody(bodyBuilder.toString());
        ctClass.addMethod(newInstance);
    }

    private void overrideSingletonsInClass(CtClass ctClass)
            throws CannotCompileException, IOException {
        if (ctClass.isFrozen()) {
            ctClass.defrost();
        }
        for (CtMethod method : ctClass.getDeclaredMethods()) {
            if (method.getName().equals("getSingletonInternalProxy")) {
                continue;
            }
            try {
                CtClass returnClass = method.getReturnType();
                if (returnClass.isEnum() || returnClass.isInterface()
                        || (returnClass.getModifiers() & AccessFlag.ABSTRACT) != 0) {
                    continue;
                }
                String returnType = returnClass.getName();
                int modifiers = method.getModifiers();
                if ((modifiers & AccessFlag.ABSTRACT) != 0
                        || ((modifiers & AccessFlag.STATIC) == 0 && returnType.equals(ctClass.getName()))) {
                    continue;
                }
                if (rewrittenSingletonClasses.contains(returnType)) {
                    method.setBody("return " + returnType + ".getSingletonInternalProxy();");
                }
            } catch (NotFoundException e) {
                // ?
            }
        }
    }

    public void rebuild(File jarFile, File outputJarFile) {
        final long startupMillis = System.currentTimeMillis();
        System.out.print("Preparing '" + jarFile.getName() + "'...");
        try {
            if (!jarFile.canRead()) {
                throw new SecurityException("No read permissions for the JAR file");
            }
            final File outputDirectory = Paths.get("build", "zotecore", jarFile.getName()).toAbsolutePath().toFile();
            final String outputDirectoryPath = outputDirectory.getPath();
            final Path outputDirectoryRoot = Paths.get(outputDirectoryPath);
            if (outputDirectory.exists()) {
                Files.walk(outputDirectoryRoot)
                        .sorted(Comparator.reverseOrder())
                        .forEach(childPath -> {
                            try {
                                Files.delete(childPath);
                            } catch (IOException e) {
                                // ?
                            }
                        });
            }
            outputDirectory.mkdirs();
            if (!outputDirectory.canWrite()) {
                throw new SecurityException("No write permissions for the output directory");
            }
            ClassPool.releaseUnmodifiedClassFile = false;
            final ClassPool classPool = new ClassPool();
            classPool.insertClassPath(jarFile.getPath());
            singletonClasses = new HashSet<>();
            ArrayList<String> classes = fetchClassesFromJar(jarFile);
            offset = 0;
            length = classes.size() * 2;
            HashMap<String, CtClass> cachedClasses = new HashMap<>();
            for (String className : classes) {
                if (!(className.startsWith("java.") || className.startsWith("javax.")
                        || className.startsWith("org.json."))) {
                    CtClass ctClass = classPool.get(className);
                    cachedClasses.put(className, ctClass);
                    overrideClass(ctClass);
                }
                if (offset % 100 == 0) {
                    System.out.print(String.format(
                            "\r%s Stubbing... %.1f%% : " + className.substring(0, Math.min(className.length(), 40)),
                            progressHandles[(int) (System.currentTimeMillis() / 100 % progressHandles.length)],
                            (float) ((1000L * offset) / (length + singletonClasses.size())) / 10f));
                }
                offset++;
            }
            boolean singletonsRequired = !singletonClasses.isEmpty();
            if (singletonsRequired) {
                rewrittenSingletonClasses = new HashSet<>();
                length += singletonClasses.size();
                for (String className : singletonClasses) {
                    if (!(className.startsWith("java.") || className.startsWith("javax.")
                            || className.startsWith("org.json."))) {
                        CtClass ctClass = cachedClasses.get(className);
                        if (ctClass != null) {
                            constructSingletonClass(ctClass);
                            rewrittenSingletonClasses.add(className);
                        }
                    }
                    if (offset % 100 == 0) {
                        System.out.print(String.format(
                                "\r%s Constructing... %.1f%% : "
                                        + className.substring(0, Math.min(className.length(), 40)),
                                progressHandles[(int) (System.currentTimeMillis() / 100 % progressHandles.length)],
                                (float) ((1000L * offset) / length) / 10f));
                    }
                    offset++;
                }
            }
            for (String className : classes) {
                if (!(className.startsWith("java.") || className.startsWith("javax.")
                        || className.startsWith("org.json."))) {
                    CtClass ctClass = cachedClasses.get(className);
                    if (singletonsRequired) {
                        overrideSingletonsInClass(ctClass);
                    }
                    ctClass.writeFile(outputDirectoryPath);
                    length++;
                }
                if (offset % 100 == 0) {
                    System.out.print(String.format(
                            "\r%s Overriding... %.1f%% : " + className.substring(0, Math.min(className.length(), 40)),
                            progressHandles[(int) (System.currentTimeMillis() / 100 % progressHandles.length)],
                            (float) ((1000L * offset) / (length + singletonClasses.size())) / 10f));
                }
                offset++;
            }
            final Manifest jarManifest = new Manifest();
            jarManifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
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
                        if (offset % 100 == 0) {
                            System.out.print(String.format(
                                    "\r%s Flushing jar... %.1f%% : "
                                            + relativePath.substring(0, Math.min(relativePath.length(), 40)),
                                    progressHandles[(int) (System.currentTimeMillis() / 100 % progressHandles.length)],
                                    (float) ((1000L * offset) / (length + singletonClasses.size())) / 10f));
                        }
                        offset++;
                    });
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            System.out.print("\r" + " ".repeat(70));
            System.out.println(String.format(
                    "\rRebuilding completed in %.1fs.",
                    (System.currentTimeMillis() - startupMillis) / 1000f));
        } catch (Exception e) {
            logger.error("An error occurred during rebuilding", e);
        } finally {
            System.out.println();
        }
    }

    public static void main(String[] args) {
        File jarFile = Paths.get(".todo", "android.jar").toAbsolutePath().toFile();
        File outputJarFile = Paths.get("iclibs", "android.jar").toAbsolutePath().toFile();
        new RebuildJavadoc().rebuild(jarFile, outputJarFile);
    }
}
