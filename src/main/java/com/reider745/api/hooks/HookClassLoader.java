//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.reider745.api.hooks;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.ProtectionDomain;
import java.util.*;

import javassist.ClassPool;
import javassist.Translator;
import javassist.bytecode.ClassFile;
import org.jetbrains.annotations.Nullable;

public class HookClassLoader extends ClassLoader {
    private HashMap<String, ClassLoader> notDefinedHere;
    private Vector<String> notDefinedPackages;
    private ClassPool classPool;
    private Translator translator;
    private ProtectionDomain domain;
    public boolean doDelegation;

    public HookClassLoader() {
        this((ClassPool) null);
    }

    public HookClassLoader(ClassPool classPool) {
        this.doDelegation = true;
        this.init(classPool);
    }

    public HookClassLoader(ClassLoader parent, ClassPool classPool) {
        super(parent);
        this.doDelegation = true;
        this.init(classPool);
    }

    private void init(ClassPool classPool) {
        this.notDefinedHere = new HashMap<>();
        this.notDefinedPackages = new Vector<>();
        this.classPool = classPool;
        this.translator = null;
        this.domain = null;
        this.delegateLoadingOf("javassist.Loader");
    }

    public void delegateLoadingOf(String classname) {
        if (classname.endsWith(".")) {
            this.notDefinedPackages.addElement(classname);
        } else {
            this.notDefinedHere.put(classname, this);
        }
    }

    public void setDomain(ProtectionDomain domain) {
        this.domain = domain;
    }

    public void setClassPool(ClassPool classPool) {
        this.classPool = classPool;
    }

    public void addTranslator(ClassPool classPool, Translator translator) throws Exception {
        this.classPool = classPool;
        this.translator = translator;
        translator.start(classPool);
    }

    public void run(String[] args) throws Throwable {
        if (args.length >= 1) {
            this.run(args[0], (String[]) Arrays.copyOfRange(args, 1, args.length));
        }
    }

    public void run(String className, String[] args) throws Throwable {
        Class<?> c = this.loadClass(className);

        try {
            c.getDeclaredMethod("main", String[].class).invoke((Object) null, new Object[] { args });
        } catch (InvocationTargetException var5) {
            throw var5.getTargetException();
        }
    }

    private static ArrayList<String> DENIED_FIND_CLASSES = new ArrayList<>();

    static {
        DENIED_FIND_CLASSES.add("jdk.internal.reflect.ConstructorAccessorImpl");
        DENIED_FIND_CLASSES.add("jdk.internal.reflect.MethodAccessorImpl");
    }

    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        name = name.intern();
        synchronized (name) {
            Class<?> c = this.findLoadedClass(name);
            if (c == null) {
                c = this.loadClassByDelegation(name);
            }
            if (c == null && !DENIED_FIND_CLASSES.contains(name)) {
                c = this.findClass(name);
            }
            if (c == null) {
                c = this.delegateToParent(name);
            }
            if (resolve) {
                this.resolveClass(c);
            }
            return c;
        }
    }

    protected Class<?> findClass(String name) throws ClassNotFoundException {
        byte[] bytecode;

        try {
            if (this.classPool != null) {
                if (this.translator != null) {
                    this.translator.onLoad(this.classPool, name);
                }

                try {
                    bytecode = this.classPool.get(name).toBytecode();
                } catch (Exception var7) {
                    return null;
                }
            } else {
                String jarname = "/" + name.replace('.', '/') + ".class";
                InputStream in = this.getClass().getResourceAsStream(jarname);
                if (in == null) {
                    return null;
                }

                bytecode = null;
            }
        } catch (Exception e) {
            throw new ClassNotFoundException("caught an exception while obtaining a class file for " + name, e);
        }

        int indexOfDot = name.lastIndexOf('.');
        if (indexOfDot != -1) {
            String packageName = name.substring(0, indexOfDot);
            if (this.isDefinedPackage(packageName)) {
                try {
                    this.definePackage(packageName, (String) null, (String) null, (String) null, (String) null,
                            (String) null,
                            (String) null, (URL) null);
                } catch (IllegalArgumentException var6) {
                }
            }
        }

        try {
            return this.domain == null ? this.defineClass(name, bytecode, 0, bytecode.length)
                    : this.defineClass(name, bytecode, 0, bytecode.length, this.domain);
        } catch (Exception e) {
            return super.findClass(name);
        }

    }

    @SuppressWarnings("deprecation")
    private boolean isDefinedPackage(String name) {
        if (ClassFile.MAJOR_VERSION >= 53) {
            return this.getDefinedPackage(name) == null;
        } else {
            return this.getPackage(name) == null;
        }
    }

    protected Class<?> loadClassByDelegation(String name) throws ClassNotFoundException {
        Class<?> c = null;
        if (this.doDelegation && (name.startsWith("java.") || name.startsWith("javax.") || name.startsWith("sun.")
                || name.startsWith("com.sun.") || name.startsWith("org.w3c.") || name.startsWith("org.xml.")
                || this.notDelegated(name))) {
            c = this.delegateToParent(name);
        }

        return c;
    }

    private boolean notDelegated(String name) {
        if (this.notDefinedHere.containsKey(name)) {
            return true;
        } else {
            Iterator<String> var2 = this.notDefinedPackages.iterator();

            String pack;
            do {
                if (!var2.hasNext()) {
                    return false;
                }

                pack = (String) var2.next();
            } while (!name.startsWith(pack));

            return true;
        }
    }

    protected Class<?> delegateToParent(String classname) throws ClassNotFoundException {
        ClassLoader cl = this.getParent();
        return cl != null ? cl.loadClass(classname) : this.findSystemClass(classname);
    }

    public static String PATH_TO_ASSETS = null;

    @Nullable
    @Override
    public URL getResource(String name) {
        if (PATH_TO_ASSETS != null) {
            try {
                return new URL("file:///" + PATH_TO_ASSETS + "/" + name);
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }
        return super.getResource(name);
    }
}
