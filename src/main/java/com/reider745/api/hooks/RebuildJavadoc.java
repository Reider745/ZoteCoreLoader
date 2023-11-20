package com.reider745.api.hooks;

import javassist.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/*
Крч класс автоматически пере собирает javado, ставля на все методы загшлушки
 */
public class RebuildJavadoc {
    private static ArrayList<String> getAllClassesFromJar(File jarPath) {
        ArrayList<String> classNames = new ArrayList<>();
        try {
            JarFile jarFile = new JarFile(jarPath);
            java.util.Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry jarEntry = entries.nextElement();
                if (jarEntry.getName().endsWith(".class")) {
                    String className = jarEntry.getName().replace('/', '.').replace(".class", "");
                    classNames.add(className);
                }
            }
            jarFile.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return classNames;
    }

    private static String getValue(CtClass retType){
        String retTypeValue = "null";
        switch (retType.getName()) {
            case "int" -> retTypeValue = "0";
            case "double" -> retTypeValue = "0.0d";
            case "float" -> retTypeValue = "0.0f";
            case "boolean" -> retTypeValue = "false";
            case "byte" -> retTypeValue = "0";
            case "short" -> retTypeValue = "0";
            case "long" -> retTypeValue = "0l";
            case "char" -> retTypeValue = "'f'";
            case "void" -> retTypeValue = null;
        }
        return retTypeValue;
    }

    private static void rebuildClass(CtClass ctClass, String nameOutput) throws CannotCompileException, IOException, NotFoundException {
        for (CtMethod method : ctClass.getDeclaredMethods()) {
            CtClass retType = method.getReturnType();

            String retTypeValue = "null";
            switch (retType.getName()) {
                case "int" -> retTypeValue = "0";
                case "double" -> retTypeValue = "0.0d";
                case "float" -> retTypeValue = "0.0f";
                case "boolean" -> retTypeValue = "false";
                case "byte" -> retTypeValue = "0";
                case "short" -> retTypeValue = "0";
                case "long" -> retTypeValue = "0l";
                case "char" -> retTypeValue = "'f'";
                case "void" -> retTypeValue = null;
            }
            String code = retTypeValue != null ? "return "+retTypeValue+";" : "String randomName = \"Fuck you\";";
            method.setBody(code);
        }
        String args = "";
        CtClass superClass = ctClass.getSuperclass();
        CtClass[] types = superClass != null ? superClass.getConstructors()[0].getParameterTypes() : new CtClass[]{};
        for (int i = 1; i <= types.length; i++) {
            if(args.equals(""))
                args += getValue(types[i-1]);
            else
                args +=","+getValue(types[i-1]);
        }
        for(CtConstructor constructor : ctClass.getDeclaredConstructors()) {
            if(superClass != null)
                constructor.setBody("super(" + args + ");");
            else
                constructor.setBody("String randomName = \"Fuck you\";");
        }
        ctClass.writeFile(nameOutput);
    }

    private static void rebuild(File jar, String nameOutput){
        try{
            final URLClassLoader loader = new URLClassLoader(new URL[]{jar.toURI().toURL()});
            final LoaderClassPath loaderClassPath = new LoaderClassPath(loader);

            final ClassPool classPool = new ClassPool();
            classPool.insertClassPath(loaderClassPath);


            ArrayList<String> classes = getAllClassesFromJar(jar);
            float count = 0;
            float size = classes.size();
            for (String path : classes) {
                if(!path.startsWith("java.") && !path.startsWith("javax."))
                    rebuildClass(classPool.get(path), nameOutput);
                count++;
                if(count % 100 == 0)
                    System.out.println(count/size*100+"%");
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        rebuild(new File("C:\\vs\\ZotCoreLoader_2\\iclibs\\android.jar"),"output");
    }
}
