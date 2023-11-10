package com.reider745.api.hooks;

import com.reider745.api.hooks.annotation.Hooks;
import com.reider745.api.hooks.annotation.AutoInject;
import javassist.*;

import java.io.File;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;

public class JarFileLoader {
    private final URLClassLoader loader;
    private final ClassPool cp = new ClassPool();
    private final ClassPath ccp;



    public JarFileLoader(String path) throws Exception {
        URL jarUrl = new URL("file:///" + path);

        loader = new URLClassLoader(new URL[]{jarUrl});
        ccp = new LoaderClassPath(loader);

        cp.insertClassPath(ccp);
    }

    public JarFileLoader(File file) throws Exception {
        this(file.getAbsolutePath());
    }

    public static class RetNull {}

    private static final String NameController = HookController.class.getName();
    private static final String NameArgumentsMap = Arguments.class.getName();

    public JarFileLoader registerHooksForClass(Class<?> clazz) {
        Annotation[] annotatedsClass = clazz.getAnnotations();
        for(Annotation annotationClass : annotatedsClass)
            if(annotationClass instanceof Hooks) {
                String className = ((Hooks) annotationClass).class_name();
                System.out.println("====="+className+"=====");
                Method[] methods = clazz.getMethods();

                for(Method method : methods){
                    Annotation[] annotationsMethod = method.getAnnotations();

                    for (Annotation annotationMethod : annotationsMethod)
                        if(annotationMethod instanceof AutoInject){
                            AutoInject inject = (AutoInject) annotationMethod;
                            String method_name = inject.method();
                            String className_ = inject.class_name();
                            String sig = inject.signature();

                            this.addHook(className_.equals("-1") ? className : className_, method_name.equals("-1") ? method.getName() : method_name, sig, method, inject.type_hook(), inject.arguments(), Arguments.parseSignature(sig), inject.static_method());
                        }
                }
            }
        return this;
    }

    private String getCode(Method replaced, String[] arguments, String[] arguments_types, String retType, TypeHook typeHook, boolean static_method){
        String funcName = replaced.getDeclaringClass().getName() + "." + replaced.getName();
        String code = NameArgumentsMap+" _args_hook = new "+NameArgumentsMap+"();\n";

        for(int i = 0;i < arguments.length;i++) {
            String arg_name = arguments[i];

            code += "_args_hook.put(\"" + arg_name + "\"," + Arguments.getConvertCode(arguments_types[i], true, arg_name) + ");\n";
        }

        code += NameController + " _ctr_hook = new " + NameController + "(" + (typeHook == TypeHook.BEFORE_REPLACE || typeHook == TypeHook.AFTER_REPLACE ? "true" : "false") + ", _args_hook, "+(static_method ? "null" : "this")+");\n";

        code += (typeHook != TypeHook.RETURN ? (funcName + "(_ctr_hook);\n") : ("return "+funcName + "(_ctr_hook);\n"))+
                "if(_ctr_hook.isReplace())\n"+
                (retType.equals("void") ? "return;" : "return " + Arguments.getConvertCode(retType, false, "_ctr_hook.getResult()")) + "\n";
        return code;
    }

    private static boolean canMethodHook(String method_sig, String sig, String[] args){
        if(!sig.equals("-1"))
            return method_sig.equals(sig);
        else {
            return Arguments.parseSignature(method_sig).length == args.length;
        }
    }

    private void addHook(String className, String method, String sig, Method replaced, TypeHook typeHook, String[] arguments, String[] arguments_types, boolean static_method){
        if(!sig.equals("-1") && arguments.length > arguments_types.length) throw new RuntimeException(replaced.getName()+":"+method+":"+sig+" arguments.length != arguments_types.length");

        try{
            ArrayList<String> signatures = new ArrayList<>();
            CtClass ctClass = cp.get(className);
            if(method.equals("<init>")){
                CtConstructor[] constructors = ctClass.getDeclaredConstructors();

                for (CtConstructor ctmBuffer : constructors) {
                    String _sig = ctmBuffer.getSignature();

                    if (canMethodHook(_sig, sig, arguments)) {
                        if(sig.equals("-1"))
                            arguments_types = Arguments.parseSignature(_sig);

                        String code = getCode(replaced, arguments, arguments_types, "void", typeHook, static_method);

                        switch (typeHook) {
                            case BEFORE, BEFORE_REPLACE, RETURN -> ctmBuffer.insertBefore(code);
                            case AFTER, AFTER_REPLACE -> ctmBuffer.insertAfter(code);
                        }
                        System.out.println("Success register hook for "+method+sig + " "+typeHook.name());
                        return;
                    } else {
                        signatures.add("Search <init>:"+_sig);
                    }
                }
            }else{
                CtMethod[] methods = ctClass.getDeclaredMethods();
                for (CtMethod ctmBuffer : methods) {
                    String name_ = ctmBuffer.getName();

                    if(name_.equals(method) ){
                        String sig_ = ctmBuffer.getSignature();

                        if(canMethodHook(sig_, sig, arguments)) {
                            if(sig.equals("-1"))
                                arguments_types = Arguments.parseSignature(sig_);

                            String retType = ctmBuffer.getReturnType().getName();
                            if(typeHook == TypeHook.AUTO){
                                if(replaced.getReturnType().getName().equals("void") || retType.equals("void")) typeHook = TypeHook.BEFORE;
                                else typeHook = TypeHook.RETURN;
                            }

                            String code = getCode(replaced, arguments, arguments_types, retType, typeHook, static_method);

                            switch (typeHook) {
                                case BEFORE, BEFORE_REPLACE, RETURN -> ctmBuffer.insertBefore(code);
                                case AFTER, AFTER_REPLACE -> ctmBuffer.insertAfter(code);
                            }
                            System.out.println("Success register hook for "+method+sig + " "+typeHook.name());
                            return;
                        }else{
                            signatures.add("Search " + name_+":"+sig_);
                        }
                    }
                }
            }

            System.out.println("All signatures: "+signatures.toString());
        }catch (Exception e){
            e.printStackTrace();
        }
        System.out.println("Failed register hook for "+method+sig + " "+typeHook.name());
    }

    public static Method get(Class<?> clazz, String method){
        try{
            return clazz.getMethod(method, HookController.class);
        }catch (Exception e){
            return null;
        }
    }



    public JarFileLoader run(String mainClass, String... args)  {
        try {
            new HookClassLoader(this.loader, cp).loadClass(mainClass).getMethod("main", String[].class).invoke(null, new Object[]{args});
        } catch (Exception e) {
            e.printStackTrace();
        }

        return this;
    }
}
