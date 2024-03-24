package com.reider745.api.hooks;

import javassist.*;
import javassist.bytecode.AccessFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zhekasmirnov.horizon.launcher.env.ClassLoaderPatch;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RebuildJavadoc {
    private static final String[] INDICATORS = new String[] { "::", ":.", ": ", ". ", "  ", " .", " :", ".:" };

    private static Logger logger;
    private Set<String> singletonClasses;
    private Set<String> rewrittenSingletonClasses;
    private int offset, length;
    private boolean requiresProxies;

    public RebuildJavadoc(boolean requiresProxies) {
        if (logger == null) {
            logger = LoggerFactory.getLogger(RebuildJavadoc.class);
        }
        this.singletonClasses = null;
        this.rewrittenSingletonClasses = null;
        this.requiresProxies = requiresProxies;
    }

    public static String getStubValue(CtClass retType) {
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
        if (requiresProxies) {
            if ((method.getModifiers() & AccessFlag.STATIC) == 0 && returnType.equals(method.getDeclaringClass())) {
                return "this";
            }
            if (returnType.getName().contains(".")) {
                singletonClasses.add(returnType.getName());
            }
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

        if (requiresProxies) {
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
            final ClassPool classPool = new ClassPool(!requiresProxies);
            classPool.appendClassPath(jarFile.getPath());

            singletonClasses = new HashSet<>();
            List<String> classes = ClassLoaderPatch.fetchClassesFromJar(jarFile);
            offset = 0;
            length = classes.size() * 2;
            HashMap<String, CtClass> cachedClasses = new HashMap<>();

            for (String className : classes) {
                if (!(className.startsWith("java.") || className.startsWith("javax.")
                        || className.startsWith("org.json."))) {
                    try {
                        CtClass ctClass = classPool.get(className);
                        overrideClass(ctClass);
                        cachedClasses.put(className, ctClass);
                    } catch (Throwable th) {
                        logger.warn("\r" + className + ": " + th.getLocalizedMessage());
                    }
                }
                if (offset % 100 == 0) {
                    System.out.print(String.format(
                            "\r%s Stubbing... %.1f%% : " + className.substring(0, Math.min(className.length(), 40)),
                            INDICATORS[(int) (System.currentTimeMillis() / 100 % INDICATORS.length)],
                            (float) ((1000L * offset) / (length + singletonClasses.size())) / 10f));
                }
                offset++;
            }

            boolean singletonsRequired = !singletonClasses.isEmpty();
            if (requiresProxies && singletonsRequired) {
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
                                INDICATORS[(int) (System.currentTimeMillis() / 100 % INDICATORS.length)],
                                (float) ((1000L * offset) / length) / 10f));
                    }
                    offset++;
                }
            }

            for (String className : cachedClasses.keySet()) {
                if (!(className.startsWith("java.") || className.startsWith("javax.")
                        || className.startsWith("org.json."))) {
                    CtClass ctClass = cachedClasses.get(className);
                    if (requiresProxies && singletonsRequired) {
                        overrideSingletonsInClass(ctClass);
                    }
                    ctClass.writeFile(outputDirectoryPath);
                    length++;
                }
                if (offset % 100 == 0) {
                    System.out.print(String.format(
                            "\r%s Overriding... %.1f%% : " + className.substring(0, Math.min(className.length(), 40)),
                            INDICATORS[(int) (System.currentTimeMillis() / 100 % INDICATORS.length)],
                            (float) ((1000L * offset) / (length + singletonClasses.size())) / 10f));
                }
                offset++;
            }

            ClassLoaderPatch.compressToJar(outputDirectory, outputJarFile, relativePath -> {
                if (offset % 100 == 0) {
                    System.out.print(String.format(
                            "\r%s Flushing jar... %.1f%% : "
                                    + relativePath.substring(0, Math.min(relativePath.length(), 40)),
                            INDICATORS[(int) (System.currentTimeMillis() / 100 % INDICATORS.length)],
                            (float) ((1000L * offset) / (length + singletonClasses.size())) / 10f));
                }
                offset++;
            });
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
        if (args.length < 2 || args[0] == null || args[1] == null) {
            throw new IllegalArgumentException("in == null || out == null");
        }
        File jarFile = Path.of(args[0]).toAbsolutePath().toFile();
        File outputJarFile = Path.of(args[1]).toAbsolutePath().toFile();
        new RebuildJavadoc(args.length > 2 && args[2] != null ? args[2].equalsIgnoreCase("proxy") : false).rebuild(jarFile, outputJarFile);
    }
}
