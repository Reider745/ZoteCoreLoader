package com.reider745.api.hooks;

import com.reider745.api.hooks.annotation.*;
import javassist.*;
import javassist.bytecode.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

public class JarEditor {
    private final Loader classLoader;
    private final ClassPool classPool = new ClassPool();
    private final ClassPath classPath;

    public JarEditor() {
        classLoader = new Loader(JarEditor.class.getClassLoader(), classPool);
        classPath = new LoaderClassPath(classLoader);

        classPool.insertClassPath(classPath);
    }

    public static class RetNull {
    }

    private static class HookData {
        public String className, method, signature;
        public Method handler;
        public TypeHook typeHook;
        public boolean controller;
        public ArgumentTypes argumentsMap;

        public HookData(String className, String method, String signature, Method handler, TypeHook typeHook,
                boolean controller, ArgumentTypes argumentsMap) {
            this.className = className;
            this.method = method;
            this.signature = signature;
            this.handler = handler;
            this.typeHook = typeHook;
            this.controller = controller;
            this.argumentsMap = argumentsMap;
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

    private static String getClassName(Object className) {
        if (className instanceof Class<?> clazz)
            return clazz.getName();
        return (String) className;
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

        for (Annotation annotationClass : clazz.getAnnotations()) {
            if (annotationClass instanceof Hooks hooks) {
                String classNameDefault = getClassName(hooks.className());

                for (Method method : clazz.getMethods()) {
                    for (Annotation annotationMethod : method.getAnnotations()) {
                        if (annotationMethod instanceof Inject inject) {
                            String methodName = inject.method();
                            String classNameInjector = getClassName(inject.className());
                            String signatureInjector = inject.signature();

                            this.addHookInitialization(
                                    classNameInjector.isBlank() ? classNameDefault : classNameInjector,
                                    methodName.isBlank() ? method.getName() : methodName, signatureInjector, method,
                                    inject.type(), isController(method), inject.argumentMap());
                        } else if (annotationMethod instanceof Injects inject) {
                            String methodName = inject.method();
                            String classNameInjector = getClassName(inject.className());
                            String[] signatures = inject.signature();
                            String className = classNameInjector.isBlank() ? classNameDefault : classNameInjector;

                            for (String signature : signatures)
                                this.addHookInitialization(className,
                                        methodName.isBlank() ? method.getName() : methodName, signature, method,
                                        inject.type(), isController(method), inject.argumentMap());
                        }
                    }
                }

                if (!classNameDefault.isBlank()) {
                    instance.init(classPool.get(classNameDefault));
                }
                replaceField(classNameDefault, instance);
            }
        }
        return this;
    }

    public void init() {
        ALL_HOOKS.forEach((key, list) -> {
            System.out.println("===== " + key + " =====");
            list.forEach(data -> addHook(data.className, data.method, data.signature, data.handler, data.typeHook,
                    data.controller, data.argumentsMap));
        });
    }

    private String getCode(Method replaced, String[] arguments, String[] argumentTypes, String returnType,
            TypeHook typeHook, boolean isStatic, boolean controller, ArgumentTypes argumentMapMode) {
        String funcName = replaced.getDeclaringClass().getName() + "." + replaced.getName();
        String code = "";
        int offset = 0;

        boolean argumentMap = ArgumentTypes.MAP == argumentMapMode;
        if (ArgumentTypes.AUTO == argumentMapMode) {
            argumentMap = isMapArguments(replaced, argumentTypes, isStatic, controller);
        }

        if (argumentMap) {
            code += NAME_ARGUMENTS_MAP + " _args_hook = new " + NAME_ARGUMENTS_MAP + "();\n";

            for (int i = 0; i < arguments.length; i++) {
                String parameterName = arguments[i];
                if (parameterName.equals("this")) {
                    offset = 1;
                    continue;
                }

                code += "_args_hook.put(\"" + parameterName + "\","
                        + Arguments.getConvertCode(argumentTypes[i - offset], true, parameterName) + ");\n";
            }
        }

        StringBuilder parametersBuilder = new StringBuilder();

        if (isStatic) {
            parametersBuilder.append("this");
        }

        for (int i = 1; i <= argumentTypes.length; i++) {
            if (parametersBuilder.toString().equals("")) {
                parametersBuilder.append("$" + i);
            } else {
                parametersBuilder.append(",$" + i);
            }
        }

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

        if (controller || argumentMap) {
            code += NAME_CONTROLLER + " _ctr_hook = new " + NAME_CONTROLLER + "("
                    + (typeHook == TypeHook.BEFORE_REPLACE || typeHook == TypeHook.AFTER_REPLACE ? "true" : "false")
                    + ", " + (argumentMap ? "_args_hook" : "null") + ", " + (!isStatic ? "null" : "this") + ");\n";
        }
        String parametersToBeInserted = controller
                ? "_ctr_hook" + (parametersBuilder.isEmpty() ? "" : ", " + parametersBuilder)
                : parametersBuilder.toString();

        if (argumentMap) {
            parametersToBeInserted = "_ctr_hook";
        }
        code += (typeHook != TypeHook.RETURN ? (funcName + "(" + parametersToBeInserted + ");\n")
                : ("return " + funcName + "(" + parametersToBeInserted + ");\n"));

        if ((controller || argumentMap) && typeHook != TypeHook.BEFORE_NOT_REPLACE
                && typeHook != TypeHook.AFTER_NOT_REPLACE && typeHook != TypeHook.RETURN) {
            code += "if (_ctr_hook.isReplace())\n" +
                    (returnType.equals("void") ? "return;"
                            : "return " + Arguments.getConvertCode(returnType, false, "_ctr_hook.getResult()"))
                    + "\n";
        } else if (typeHook == TypeHook.BEFORE_REPLACE || typeHook == TypeHook.AFTER_REPLACE) {
            code += "return;";
        }

        return code;
    }

    private static boolean canMethodHook(String methodSignature, String signature, boolean isController, boolean isStatic,
            Method method, String[] types) {
        if (!signature.isBlank()) {
            return methodSignature.equals(signature);
        }

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

    private static String[] getArguments(CtClass[] parameters, MethodInfo methodInfo, boolean isStatic) {
        int offset = isStatic ? 1 : 0;
        int size = parameters.length + offset;

        String[] arguments = new String[size];

        CodeAttribute codeAttribute = methodInfo.getCodeAttribute();
        LocalVariableAttribute attr = (LocalVariableAttribute) codeAttribute.getAttribute(LocalVariableAttribute.tag);

        try {
            for (int i = 0; i < size; i++) {
                arguments[i] = attr.variableNameByIndex(i);
            }
        } catch (Exception e) {
        }

        return arguments;
    }

    private static String[] getArgumentTypes(CtClass[] ctClasses) {
        String[] types = new String[ctClasses.length];
        for (int i = 0; i < ctClasses.length; i++) {
            types[i] = ctClasses[i].getName();
        }
        return types;
    }

    private boolean isController(Method replaced) {
        Class<?>[] parameters = replaced.getParameterTypes();
        return parameters.length >= 1 && parameters[0].getName().equals(NAME_CONTROLLER);
    }

    private boolean isMapArguments(Method replaced, String[] types, boolean isStatic, boolean isController) {
        Class<?>[] parameters = replaced.getParameterTypes();
        if (isController) {
            return parameters.length <= 1 + (isStatic ? 0 : 1);
        }
        return false;
    }

    private void replaceField(String className, HookClass fieldReplaced) {
        try {
            CtClass ctClass = classPool.get(className);

            CtField[] ctField = ctClass.getDeclaredFields();
            try {
                for (CtField field : ctField) {
                    fieldReplaced.rebuildField(ctClass, field);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            ctClass.rebuildClassFile();
        } catch (Exception e) {
        }
    }

    private void addHook(String className, String method, String signature, Method replaced, TypeHook typeHook,
            boolean controller, ArgumentTypes argumentMap) {
        try {
            // String[] argumentTypes = Arguments.parseSignature(sig);

            ArrayList<String> signatures = new ArrayList<>();
            CtClass ctClass = classPool.get(className);

            if (method.equals("<init>")) {
                CtConstructor[] constructors = ctClass.getDeclaredConstructors();

                for (CtConstructor ctmBuffer : constructors) {
                    String constructorSignature = ctmBuffer.getSignature();
                    boolean isStatic = !Modifier.isStatic(ctmBuffer.getModifiers());
                    String[] arguments = getArguments(ctmBuffer.getParameterTypes(), ctmBuffer.getMethodInfo(),
                            isStatic);
                    String[] argumentTypes = getArgumentTypes(ctmBuffer.getParameterTypes());

                    if (canMethodHook(constructorSignature, signature, controller, isStatic, replaced, argumentTypes)) {
                        if (signature.isBlank())
                            argumentTypes = getArgumentTypes(ctmBuffer.getParameterTypes());

                        if (typeHook == TypeHook.AUTO) {
                            typeHook = TypeHook.BEFORE_NOT_REPLACE;
                        }

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
                        System.out.println("Success register hook for " + method + signature + " " + typeHook.name());
                        return;
                    } else {
                        signatures.add("<init>" + constructorSignature);
                    }
                }
            } else {
                CtMethod[] methods = ctClass.getDeclaredMethods();

                for (CtMethod ctmBuffer : methods) {
                    String methodName = ctmBuffer.getName();

                    if (methodName.equals(method)) {
                        String methodSignature = ctmBuffer.getSignature();
                        boolean isStatic = !Modifier.isStatic(ctmBuffer.getModifiers());
                        String[] arguments = getArguments(ctmBuffer.getParameterTypes(), ctmBuffer.getMethodInfo(),
                                isStatic);
                        String[] arguments_types = getArgumentTypes(ctmBuffer.getParameterTypes());

                        if (canMethodHook(methodSignature, signature, controller, isStatic, replaced,
                                arguments_types)) {
                            if (signature.isBlank()) {
                                arguments_types = getArgumentTypes(ctmBuffer.getParameterTypes());
                            }

                            String returnType = ctmBuffer.getReturnType().getName();
                            if (typeHook == TypeHook.AUTO) {
                                if (replaced.getReturnType().getName().equals("void") || returnType.equals("void")) {
                                    typeHook = TypeHook.BEFORE_NOT_REPLACE;
                                } else {
                                    typeHook = TypeHook.RETURN;
                                }
                            }

                            String code = getCode(replaced, arguments, arguments_types, returnType, typeHook, isStatic,
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
                            signatures.add(methodName + methodSignature);
                        }
                    }
                }
            }

            if (!signatures.isEmpty()) {
                System.out.println("* All signatures:");
                System.out.println(" ".repeat(2) + String.join(" ".repeat(2) + System.lineSeparator(), signatures));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Failed register hook for " + method + signature + " " + typeHook.name());
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
            classLoader.loadClass(mainClass).getMethod("main", String[].class).invoke(null, new Object[] { args });
        } catch (Exception e) {
            e.printStackTrace();
        }

        return this;
    }
}
