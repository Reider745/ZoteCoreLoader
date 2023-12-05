package com.reider745.api.hooks;

import com.reider745.api.hooks.annotation.*;
import javassist.*;
import javassist.bytecode.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

public class JarEditor {
    private final Loader loader;
    private final ClassPool cp = new ClassPool();
    private final ClassPath ccp;

    public JarEditor() {
        loader = new Loader(JarEditor.class.getClassLoader(), cp);
        ccp = new LoaderClassPath(loader);

        cp.insertClassPath(ccp);
    }

    public static class RetNull {
    }

    private static class HookData {
        public String className, method, sig;
        public Method replaced;
        public TypeHook typeHook;
        public boolean controller;
        public ArgumentTypes arguments_map;

        public HookData(String className, String method, String sig, Method replaced, TypeHook typeHook,
                boolean controller, ArgumentTypes arguments_map) {
            this.className = className;
            this.method = method;
            this.sig = sig;
            this.replaced = replaced;
            this.typeHook = typeHook;
            this.controller = controller;
            this.arguments_map = arguments_map;
        }
    }

    private static final String NAME_CONTROLLER = HookController.class.getName();
    private static final String NAME_ARGUMENTS_MAP = Arguments.class.getName();
    private static final HashMap<String, ArrayList<HookData>> ALL_HOOKS = new HashMap<>();

    private void addHookInitialization(String className, String method, String sig, Method replaced, TypeHook typeHook,
            boolean controller, ArgumentTypes argumentMap) {
        ArrayList<HookData> hooks = ALL_HOOKS.get(className);

        if (hooks == null)
            hooks = new ArrayList<>();

        hooks.add(new HookData(className, method, sig, replaced, typeHook, controller, argumentMap));
        ALL_HOOKS.put(className, hooks);
    }

    private static String getClassName(Object class_name) {
        if (class_name instanceof Class<?> clazz)
            return clazz.getName();
        return (String) class_name;
    }

    public JarEditor registerHooksInitializationForClass(Class<? extends HookClass> clazz)
            throws InstantiationException, IllegalAccessException, NotFoundException, NoSuchMethodException,
            SecurityException, IllegalArgumentException, InvocationTargetException {
        Constructor<?> hookConstructor = clazz.getDeclaredConstructor(new Class[0]);
        try {
            hookConstructor.setAccessible(true);
        } catch (SecurityException e) {
        }
        HookClass instance = (HookClass) hookConstructor.newInstance();
        Annotation[] annotatedsClass = clazz.getAnnotations();
        for (Annotation annotationClass : annotatedsClass)
            if (annotationClass instanceof Hooks hooks) {
                String className = getClassName(hooks.class_name());
                Method[] methods = clazz.getMethods();

                for (Method method : methods) {
                    Annotation[] annotationsMethod = method.getAnnotations();

                    for (Annotation annotationMethod : annotationsMethod)
                        if (annotationMethod instanceof Inject inject) {
                            String method_name = inject.method();
                            String className_ = getClassName(inject.class_name());
                            String sig = inject.signature();

                            this.addHookInitialization(className_.equals("-1") ? className : className_,
                                    method_name.equals("-1") ? method.getName() : method_name, sig, method,
                                    inject.type_hook(), isController(method), inject.arguments_map());
                        } else if (annotationMethod instanceof Injects inject) {
                            String method_name = inject.method();
                            String className_ = getClassName(inject.class_name());
                            String[] sigs = inject.signature();
                            String className__ = className_.equals("-1") ? className : className_;

                            for (String sig : sigs)
                                this.addHookInitialization(className__,
                                        method_name.equals("-1") ? method.getName() : method_name, sig, method,
                                        inject.type_hook(), isController(method), inject.arguments_map());
                        }
                }

                if (!className.equals(""))
                    instance.init(cp.get(className));
                replaceField(className, instance);
            }
        return this;
    }

    public void init() {
        ALL_HOOKS.forEach((key, list) -> {
            System.out.println("===== " + key + " =====");
            list.forEach(data -> addHook(data.className, data.method, data.sig, data.replaced, data.typeHook,
                    data.controller, data.arguments_map));
        });
    }

    private String getCode(Method replaced, String[] arguments, String[] argumentTypes, String returnType,
            TypeHook typeHook, boolean isStatic, boolean controller, ArgumentTypes argumentMapMode) {
        String funcName = replaced.getDeclaringClass().getName() + "." + replaced.getName();
        String code = "";
        int offset = 0;

        boolean argumentMap = ArgumentTypes.MAP == argumentMapMode;
        if (ArgumentTypes.AUTO == argumentMapMode)
            argumentMap = isMapArguments(replaced, argumentTypes, isStatic, controller);

        if (argumentMap) {
            code += NAME_ARGUMENTS_MAP + " _args_hook = new " + NAME_ARGUMENTS_MAP + "();\n";

            for (int i = 0; i < arguments.length; i++) {
                String arg_name = arguments[i];
                if (arg_name.equals("this")) {
                    offset = 1;
                    continue;
                }

                code += "_args_hook.put(\"" + arg_name + "\","
                        + Arguments.getConvertCode(argumentTypes[i - offset], true, arg_name) + ");\n";
            }
        }

        StringBuilder args = new StringBuilder();

        if (isStatic)
            args.append("this");

        for (int i = 1; i <= argumentTypes.length; i++)
            if (args.toString().equals(""))
                args.append("$" + i);
            else
                args.append(",$" + i);

        /*
         * StringBuilder args = new StringBuilder();
         * int arg_index = 1;
         * for (String name : ar)
         * if (args.toString().equals(""))
         * if (name.equals("this"))
         * args.append(name);
         * else {
         * args.append("$"+arg_index);
         * arg_index++;
         * }
         * else {
         * args.append(",").append("$"+arg_index);
         * arg_index++;
         * }
         */
        /*
         * if (args.toString().equals(""))
         * args.append(name);
         * else
         * args.append(",").append(name);
         */

        if (controller || argumentMap)
            code += NAME_CONTROLLER + " _ctr_hook = new " + NAME_CONTROLLER + "("
                    + (typeHook == TypeHook.BEFORE_REPLACE || typeHook == TypeHook.AFTER_REPLACE ? "true" : "false")
                    + ", " + (argumentMap ? "_args_hook" : "null") + ", " + (!isStatic ? "null" : "this") + ");\n";
        String arg_func = controller ? "_ctr_hook" + (args.toString().equals("") ? "" : ", " + args) : args.toString();

        if (argumentMap)
            arg_func = "_ctr_hook";
        code += (typeHook != TypeHook.RETURN ? (funcName + "(" + arg_func + ");\n")
                : ("return " + funcName + "(" + arg_func + ");\n"));

        if ((controller || argumentMap) && typeHook != TypeHook.BEFORE_NOT_REPLACE
                && typeHook != TypeHook.AFTER_NOT_REPLACE && typeHook != TypeHook.RETURN)
            code += "if (_ctr_hook.isReplace())\n" +
                    (returnType.equals("void") ? "return;"
                            : "return " + Arguments.getConvertCode(returnType, false, "_ctr_hook.getResult()"))
                    + "\n";
        else if (typeHook == TypeHook.BEFORE_REPLACE || typeHook == TypeHook.AFTER_REPLACE)
            code += "return;";

        return code;
    }

    private static boolean canMethodHook(String methodSignature, String sig, boolean isController, boolean isStatic,
            Method method, String[] types) {
        if (!sig.equals("-1"))
            return methodSignature.equals(sig);

        Class<?>[] parameters = method.getParameterTypes();
        if (parameters.length > (isController ? 1 : 0)) {
            int offset = isStatic ? 1 : 0;

            if (types.length != parameters.length - offset)
                return false;

            for (int i = offset; i < parameters.length; i++) {
                Class<?> parameter = parameters[i];
                if (!parameter.getName().equals(types[i - offset]))
                    return false;
            }

            return true;
        }
        return true;
    }

    private static String[] getArguments(CtClass[] types, MethodInfo methodInfo, boolean isStatic) {
        int offset = isStatic ? 1 : 0;
        int size = types.length + offset;

        String[] arguments = new String[size];

        CodeAttribute codeAttribute = methodInfo.getCodeAttribute();
        LocalVariableAttribute attr = (LocalVariableAttribute) codeAttribute.getAttribute(LocalVariableAttribute.tag);

        try {
            for (int i = 0; i < size; i++)
                arguments[i] = attr.variableNameByIndex(i);
        } catch (Exception e) {
        }

        return arguments;
    }

    private static String[] getTypesArguments(CtClass[] ctClasses) {
        String[] types = new String[ctClasses.length];
        for (int i = 0; i < ctClasses.length; i++)
            types[i] = ctClasses[i].getName();

        return types;
    }

    private boolean isController(Method replaced) {
        Class<?>[] parameters = replaced.getParameterTypes();
        return parameters.length >= 1 && parameters[0].getName().equals(NAME_CONTROLLER);
    }

    private boolean isMapArguments(Method replaced, String[] types, boolean isStatic, boolean isController) {
        Class<?>[] parameters = replaced.getParameterTypes();
        if (isController)
            return parameters.length <= 1 + (isStatic ? 0 : 1);
        return false;
    }

    private void replaceField(String className, HookClass fieldReplaced) {
        try {
            CtClass ctClass = cp.get(className);

            CtField[] ctField = ctClass.getDeclaredFields();
            try {
                for (CtField field : ctField)
                    fieldReplaced.rebuildField(ctClass, field);
            } catch (Exception e) {
                e.printStackTrace();
            }

            ctClass.rebuildClassFile();
        } catch (Exception e) {
        }
    }

    private void addHook(String className, String method, String sig, Method replaced, TypeHook typeHook,
            boolean controller, ArgumentTypes argumentMap) {
        try {
            // String[] argumentTypes = Arguments.parseSignature(sig);

            ArrayList<String> signatures = new ArrayList<>();
            CtClass ctClass = cp.get(className);

            if (method.equals("<init>")) {
                CtConstructor[] constructors = ctClass.getDeclaredConstructors();

                for (CtConstructor ctmBuffer : constructors) {
                    String _sig = ctmBuffer.getSignature();
                    boolean isStatic = !Modifier.isStatic(ctmBuffer.getModifiers());
                    String[] arguments = getArguments(ctmBuffer.getParameterTypes(), ctmBuffer.getMethodInfo(),
                            isStatic);
                    String[] argumentTypes = getTypesArguments(ctmBuffer.getParameterTypes());

                    if (canMethodHook(_sig, sig, controller, isStatic, replaced, argumentTypes)) {
                        if (sig.equals("-1"))
                            argumentTypes = getTypesArguments(ctmBuffer.getParameterTypes());

                        if (typeHook == TypeHook.AUTO)
                            typeHook = TypeHook.BEFORE_NOT_REPLACE;

                        String code = getCode(replaced, arguments, argumentTypes, "void", typeHook, isStatic,
                                controller, argumentMap);
                        switch (typeHook) {
                            case BEFORE, BEFORE_NOT_REPLACE, BEFORE_REPLACE, RETURN, AFTER_REPLACE ->
                                ctmBuffer.insertBefore(code);
                            case AFTER, AFTER_NOT_REPLACE -> ctmBuffer.insertAfter(code);
                            default -> throw new RuntimeException("Hook type not found or does not exist. "
                                    + "Please make sure you have correctly specified the hook type and that it exists in your project.");
                        }
                        ctmBuffer.getMethodInfo().rebuildStackMap(ClassPool.getDefault());
                        System.out.println("Success register hook for " + method + sig + " " + typeHook.name());
                        return;
                    } else {
                        signatures.add("<init>" + _sig);
                    }
                }
            } else {
                CtMethod[] methods = ctClass.getDeclaredMethods();

                for (CtMethod ctmBuffer : methods) {
                    String name_ = ctmBuffer.getName();

                    if (name_.equals(method)) {
                        String sig_ = ctmBuffer.getSignature();
                        boolean isStatic = !Modifier.isStatic(ctmBuffer.getModifiers());
                        String[] arguments = getArguments(ctmBuffer.getParameterTypes(), ctmBuffer.getMethodInfo(),
                                isStatic);
                        String[] arguments_types = getTypesArguments(ctmBuffer.getParameterTypes());

                        if (canMethodHook(sig_, sig, controller, isStatic, replaced, arguments_types)) {
                            if (sig.equals("-1"))
                                arguments_types = getTypesArguments(ctmBuffer.getParameterTypes());

                            String retType = ctmBuffer.getReturnType().getName();
                            if (typeHook == TypeHook.AUTO) {
                                if (replaced.getReturnType().getName().equals("void") || retType.equals("void"))
                                    typeHook = TypeHook.BEFORE_NOT_REPLACE;
                                else
                                    typeHook = TypeHook.RETURN;
                            }

                            String code = getCode(replaced, arguments, arguments_types, retType, typeHook, isStatic,
                                    controller, argumentMap);

                            switch (typeHook) {
                                case RETURN -> ctmBuffer.setBody("{ " + code + " }");
                                case BEFORE, BEFORE_NOT_REPLACE, BEFORE_REPLACE, AFTER_REPLACE ->
                                    ctmBuffer.insertBefore(code);
                                case AFTER, AFTER_NOT_REPLACE -> ctmBuffer.insertAfter(code);
                                default -> throw new RuntimeException("Hook type not found or does not exist. "
                                        + "Please make sure you have correctly specified the hook type and that it exists in your project.");
                            }

                            ctmBuffer.getMethodInfo().rebuildStackMap(ClassPool.getDefault());
                            System.out.println("Success register hook for " + ctmBuffer.getName()
                                    + ctmBuffer.getSignature() + " " + typeHook.name());
                            return;
                        } else {
                            signatures.add(name_ + sig_);
                        }
                    }
                }
            }

            System.out.println("* All signatures: " + signatures);
        } catch (Exception e) {
            System.out.println(android.util.Log.getStackTraceString(e));
            e.printStackTrace();
        }
        System.out.println("Failed register hook for " + method + sig + " " + typeHook.name());
    }

    public static Method get(Class<?> clazz, String method) {
        try {
            return clazz.getMethod(method, HookController.class);
        } catch (Exception e) {
            return null;
        }
    }

    public JarEditor run(String mainClass, String... args) {
        try {
            loader.loadClass(mainClass).getMethod("main", String[].class).invoke(null, new Object[] { args });
        } catch (Exception e) {
            e.printStackTrace();
        }

        return this;
    }
}
