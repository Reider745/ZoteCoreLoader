package com.reider745.api.hooks;

import com.reider745.api.hooks.annotation.Hooks;
import com.reider745.api.hooks.annotation.Inject;
import com.reider745.api.hooks.annotation.Injects;
import javassist.*;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.MethodInfo;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;


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
                        if(annotationMethod instanceof Inject inject){
                            String method_name = inject.method();
                            String className_ = inject.class_name();
                            String sig = inject.signature();

                            this.addHook(className_.equals("-1") ? className : className_, method_name.equals("-1") ? method.getName() : method_name, sig, method, inject.type_hook(), isController(method), inject.arguments_map());
                        }else if(annotationMethod instanceof Injects inject){
                            String method_name = inject.method();
                            String className_ = inject.class_name();
                            String[] sigs = inject.signature();
                            String className__ = className_.equals("-1") ? className : className_;

                            for(String sig : sigs)
                                this.addHook(className__, method_name.equals("-1") ? method.getName() : method_name, sig, method, inject.type_hook(), isController(method), inject.arguments_map());
                        }
                }
            }
        return this;
    }

    private String getCode(Method replaced, String[] arguments, String[] arguments_types, String retType, TypeHook typeHook, boolean isStatic, boolean controller, ArgumentTypes arguments_map_mode){
        String funcName = replaced.getDeclaringClass().getName() + "." + replaced.getName();
        String code = "";
        int offset = 0;

        boolean arguments_map = ArgumentTypes.MAP == arguments_map_mode;
        if(ArgumentTypes.AUTO == arguments_map_mode)
            arguments_map = isMapArguments(replaced, arguments_types, isStatic, controller);

        if(arguments_map){
            code += NameArgumentsMap+" _args_hook = new "+NameArgumentsMap+"();\n";

            for(int i = 0;i < arguments.length;i++) {
                String arg_name = arguments[i];
                if(arg_name.equals("this")){
                    offset = 1;
                    continue;
                }

                code += "_args_hook.put(\"" + arg_name + "\"," + Arguments.getConvertCode(arguments_types[i-offset], true, arg_name) + ");\n";
            }
        }


        StringBuilder args = new StringBuilder();
        for(String name : arguments)
            if(args.toString().equals(""))
                args.append(name);
            else
                args.append(",").append(name);

        if(controller || arguments_map)
            code += NameController + " _ctr_hook = new " + NameController + "(" + (typeHook == TypeHook.BEFORE_REPLACE || typeHook == TypeHook.AFTER_REPLACE ? "true" : "false") + ", "+(arguments_map ? "_args_hook" : "null")+", "+(!isStatic ? "null" : "this")+");\n";
        String arg_func = controller ? "_ctr_hook" +(args.toString().equals("") ? "" : ", " + args) : args.toString();

        if(arguments_map) arg_func = "_ctr_hook";
        code += (typeHook != TypeHook.RETURN ? (funcName + "("+arg_func+");\n") : ("return "+funcName + "("+arg_func+");\n"));
        if((controller || arguments_map) && typeHook != TypeHook.BEFORE_NOT_REPLACE && typeHook != TypeHook.AFTER_NOT_REPLACE && typeHook != TypeHook.RETURN)
            code += "if(_ctr_hook.isReplace())\n"+
                (retType.equals("void") ? "return;" : "return " + Arguments.getConvertCode(retType, false, "_ctr_hook.getResult()")) + "\n";

        return code;
    }

    private static boolean canMethodHook(String method_sig, String sig, boolean isController, boolean isStatic, Method method, String[] types){
        if(!sig.equals("-1"))
            return method_sig.equals(sig);

        Class<?>[] parameters = method.getParameterTypes();
        if(parameters.length > (isController ? 1 : 0)){
            int offset = isStatic ? 1 : 0;

            if(types.length != parameters.length-offset) return false;

            for(int i = offset;i < parameters.length;i++){
                Class<?> parameter = parameters[i];
                if(!parameter.getName().equals(types[i-offset]))
                    return false;
            }

            return true;
        }
        return true;
    }

    private static String[] getArguments(CtClass[] types, MethodInfo methodInfo, boolean isStatic){
        int offset = isStatic ? 1 : 0;
        int size = types.length + offset;

        String[] arguments = new String[size];

        CodeAttribute codeAttribute = methodInfo.getCodeAttribute();
        LocalVariableAttribute attr = (LocalVariableAttribute) codeAttribute.getAttribute(LocalVariableAttribute.tag);


        for (int i = 0; i < size; i++)
            arguments[i] = attr.variableNameByIndex(i);

        return arguments;
    }

    private static String[] getTypesArguments(CtClass[] ctClasses){
        String[] types = new String[ctClasses.length];
        for(int i = 0;i < ctClasses.length;i++)
            types[i] = ctClasses[i].getName();

        return types;
    }

    private boolean isController(Method replaced){
        Class<?>[] parameters = replaced.getParameterTypes();
        return parameters.length == 1 && parameters[0].getName().equals(NameController);
    }

    private boolean isMapArguments(Method replaced, String[] types, boolean isStatic, boolean isController){
        Class<?>[] parameters = replaced.getParameterTypes();
        return parameters.length-(!isStatic ? 0 : 1) != types.length+(isController ? 1 : 0);
    }

    private void addHook(String className, String method, String sig, Method replaced, TypeHook typeHook, boolean controller, ArgumentTypes arguments_map){
        try{
            //String[] arguments_types = Arguments.parseSignature(sig);

            ArrayList<String> signatures = new ArrayList<>();
            CtClass ctClass = cp.get(className);
            if(method.equals("<init>")){
                CtConstructor[] constructors = ctClass.getDeclaredConstructors();

                for (CtConstructor ctmBuffer : constructors) {
                    String _sig = ctmBuffer.getSignature();
                    boolean isStatic = !Modifier.isStatic(ctmBuffer.getModifiers());
                    String[] arguments = getArguments(ctmBuffer.getParameterTypes(), ctmBuffer.getMethodInfo(), isStatic);
                    String[] arguments_types = getTypesArguments(ctmBuffer.getParameterTypes());

                    if (canMethodHook(_sig, sig, controller, isStatic, replaced, arguments_types)) {
                        if(sig.equals("-1"))
                            arguments_types = getTypesArguments(ctmBuffer.getParameterTypes());

                        if(typeHook == TypeHook.AUTO) typeHook = TypeHook.BEFORE;

                        String code = getCode(replaced, arguments, arguments_types, "void", typeHook, isStatic, controller, arguments_map);

                        switch (typeHook) {
                            case BEFORE, BEFORE_REPLACE, RETURN, BEFORE_NOT_REPLACE -> ctmBuffer.insertBefore(code);
                            case AFTER, AFTER_REPLACE, AFTER_NOT_REPLACE -> ctmBuffer.insertAfter(code);
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
                        boolean isStatic = !Modifier.isStatic(ctmBuffer.getModifiers());
                        String[] arguments = getArguments(ctmBuffer.getParameterTypes(), ctmBuffer.getMethodInfo(), isStatic);
                        String[] arguments_types = getTypesArguments(ctmBuffer.getParameterTypes());

                        if(canMethodHook(sig_, sig, controller, isStatic, replaced, arguments_types)) {
                            if(sig.equals("-1"))
                                arguments_types = getTypesArguments(ctmBuffer.getParameterTypes());

                            String retType = ctmBuffer.getReturnType().getName();
                            if(typeHook == TypeHook.AUTO){
                                if(replaced.getReturnType().getName().equals("void") || retType.equals("void")) typeHook = TypeHook.BEFORE_NOT_REPLACE;
                                else typeHook = TypeHook.RETURN;
                            }

                            String code = getCode(replaced, arguments, arguments_types, retType, typeHook, isStatic, controller, arguments_map);

                            System.out.println(code);

                            switch (typeHook) {
                                case BEFORE, BEFORE_NOT_REPLACE, BEFORE_REPLACE, RETURN -> ctmBuffer.insertBefore(code);
                                case AFTER, AFTER_NOT_REPLACE, AFTER_REPLACE -> ctmBuffer.insertAfter(code);
                            }
                            System.out.println("Success register hook for "+ctmBuffer.getName()+ctmBuffer.getSignature() + " "+typeHook.name());
                            return;
                        }else{
                            signatures.add("Search " + name_+":"+sig_);
                        }
                    }
                }
            }

            System.out.println("All signatures: "+signatures);
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
