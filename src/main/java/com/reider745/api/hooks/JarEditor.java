package com.reider745.api.hooks;

import com.reider745.Main;
import com.reider745.api.hooks.annotation.*;
import javassist.*;
import javassist.bytecode.*;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

public class JarEditor {
    private final HookClassLoader classLoader;
    private final ClassPool classPool = new ClassPool();
    private final ClassPath classPath;
    private final ArrayList<String> initClasses = new ArrayList<>();

    public JarEditor(ClassLoader main) {
        classLoader = new HookClassLoader(main, classPool);
        classPath = new LoaderClassPath(classLoader);

        classPool.insertClassPath(classPath);
    }

    public void addInitClass(String clazz) {
        initClasses.add(clazz);
    }

    public static final class RetNull {
    }

    private static record HookData(String className, String method, String signature, CtMethod handler, TypeHook typeHook, boolean controller, ArgumentTypes argumentsMap) {
    }

    private static final String NAME_CONTROLLER = HookController.class.getName();
    private static final String NAME_ARGUMENTS_MAP = Arguments.class.getName();
    private static final HashMap<String, ArrayList<HookData>> ALL_HOOKS = new HashMap<>();
    private static final HashMap<String, String> loadedHookClass = new HashMap<>();

    public ClassPool getClassPool() {
        return classPool;
    }

    private void addHookInitialization(String className, String method, String sig, CtMethod replaced, TypeHook typeHook,
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

    public HookClassLoader getClassLoader() {
        return classLoader;
    }

    public JarEditor registerHooksInitializationForClass(String clazz)
            throws InstantiationException, IllegalAccessException, NotFoundException, NoSuchMethodException,
            SecurityException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException, CannotCompileException {
        CtClass ctClass = classPool.get(clazz);

        for (Object annotationClass : ctClass.getAnnotations()) {
            if (annotationClass instanceof Hooks hooks) {
                String classNameDefault = getClassName(hooks.className());

                for (CtMethod method : ctClass.getMethods()) {
                    for (Object annotationMethod : method.getAnnotations()) {
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
                        } else if(annotationMethod instanceof ShadowGetField shadowGetField) {
                            final String name = shadowGetField.field();

                            String className = shadowGetField.className();
                            if(className.isEmpty()) className = classNameDefault;

                            final CtClass ctClass1 = classPool.get(className);
                            final CtField field = ctClass1.getDeclaredField(name);

                            int modi = field.getModifiers();
                            if((modi & Modifier.PUBLIC) == 0 && (modi & Modifier.PROTECTED) == 0)
                                field.setModifiers(modi & ~Modifier.PRIVATE | Modifier.PUBLIC);
                            else if((modi & Modifier.PROTECTED) != 0){
                                field.setModifiers(modi & ~Modifier.PROTECTED | Modifier.PUBLIC);
                            }

                            final CtClass[] args = method.getParameterTypes();

                            if(args.length == 1 && args[0].getName().equals(ctClass1.getName())) {
                                method.setBody("{return $1." + name + ";}");
                            }else
                                method.setBody("{return "+className+"."+name+";}");
                        } else if(annotationMethod instanceof ShadowSetField shadowSetField) {
                            final String name = shadowSetField.field();
                            String className = shadowSetField.className();
                            if(className.isEmpty()) className = classNameDefault;
                            final CtClass ctClass1 = classPool.get(className);
                            final CtField field = ctClass1.getDeclaredField(name);

                            int modi = field.getModifiers();
                            if((modi & Modifier.PUBLIC) == 0 && (modi & Modifier.PROTECTED) == 0)
                                field.setModifiers(modi & ~Modifier.PRIVATE | Modifier.PUBLIC);
                            else if((modi & Modifier.PROTECTED) != 0){
                                field.setModifiers(modi & ~Modifier.PROTECTED | Modifier.PUBLIC);
                            }

                            if((modi & Modifier.FINAL) == 0)
                                field.setModifiers(modi & ~Modifier.FINAL);

                            final CtClass[] args = method.getParameterTypes();

                            if(args[0].getName().equals(ctClass1.getName())) {
                                method.setBody("{$1." + name + " = $2;}");
                            }else
                                method.setBody("{return "+className+"."+name+" = $1;}");
                        } else if(annotationMethod instanceof ShadowMethod shadowMethod) {
                            String name = shadowMethod.method();
                            if(name.isEmpty()) name = method.getName();

                            String className = shadowMethod.className();
                            if(className.isEmpty()) className = classNameDefault;

                            final CtClass ctClass1 = classPool.get(className);
                            CtMethod methodOrg = null;

                            final CtMethod[] methods = ctClass1.getDeclaredMethods();
                            for(CtMethod method_ : methods){
                                final String sig = shadowMethod.signature();
                                if(method_.getName().equals(name) && (sig.isEmpty() || method_.getSignature().equals(sig))){
                                    methodOrg = method_;
                                    break;
                                }
                            }


                            int modi = methodOrg.getModifiers();
                            if((modi & Modifier.PUBLIC) == 0 && (modi & Modifier.PROTECTED) == 0)
                                methodOrg.setModifiers(modi & ~Modifier.PRIVATE | Modifier.PUBLIC);
                            else if((modi & Modifier.PROTECTED) != 0){
                                methodOrg.setModifiers(modi & ~Modifier.PROTECTED | Modifier.PUBLIC);
                            }

                            final CtClass[] args = method.getParameterTypes();
                            final CtClass[] orgArgs = methodOrg.getParameterTypes();
                            String arg = "";
                            final boolean isStatic = args.length == orgArgs.length;

                            for(int i = isStatic ? 1 : 2;i < args.length + 1;i++) {
                                if (!arg.isEmpty())
                                    arg += ", ";
                                arg += "$" + i;
                            }

                            String returnStr;

                            if(method.getReturnType().getName().equals("void"))
                                returnStr = "";
                            else
                                returnStr = "return ";

                            if(isStatic){
                                method.setBody("{"+returnStr+className+"."+name+"("+arg+");}");
                            }else
                                method.setBody("{"+returnStr+"$1."+name+"("+arg+");}");
                        }
                    }
                }

                loadedHookClass.put(clazz, classNameDefault);
            }
        }
        return this;
    }

    public CtClass get(String name) throws NotFoundException {
        return classPool.get(name);
    }


    public interface IInit {
        void call(CtClass ctClass) throws IOException, NotFoundException, CannotCompileException, BadBytecode;
    }

    private HashMap<String, IInit> inits = new HashMap<>();
    public void addInit(String name, IInit init){
        inits.put(name, init);
    }

    public interface IRebuildField {
        void call(CtClass ctClass, CtField field);
    }

    private HashMap<String, IRebuildField> rebuildFields = new HashMap<>();
    public void addRebuildField(String name, IRebuildField init){
        rebuildFields.put(name, init);
    }

    public void init(boolean debug) {
        ALL_HOOKS.forEach((key, list) -> {
            if(debug)
                System.out.println("===== " + key + " =====");

            list.forEach(data -> addHook(data.className, data.method, data.signature, data.handler, data.typeHook,
                    data.controller, data.argumentsMap, debug));

        });

        loadedHookClass.forEach((clazz, classNameDefault) -> {
            try{
                replaceField(classNameDefault, rebuildFields.get(clazz));
                IInit init = inits.get(clazz);
                if(init != null)
                    init.call(classPool.get(classNameDefault));

                Class<?> clazzLoad = classLoader.loadClass(clazz);
                if(clazzLoad.getSuperclass().getName().equals("com.reider745.api.hooks.HookClass")){
                    Constructor<?> hookConstructor = clazzLoad.getDeclaredConstructor(new Class[0]);
                    hookConstructor.setAccessible(true);
                    HookClass instance = (HookClass) hookConstructor.newInstance();

                    instance.init();
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        });
    }

    private String getCode(CtMethod replaced, String[] arguments, String[] argumentTypes, String returnType,
                           TypeHook typeHook, boolean isStatic, boolean controller, ArgumentTypes argumentMapMode) throws NotFoundException {
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
                                         CtMethod method, String[] types) throws NotFoundException {
        if (!signature.isBlank()) {
            return methodSignature.equals(signature);
        }

        CtClass[] parameters = method.getParameterTypes();
        if (parameters.length > (isController ? 1 : 0)) {
            int offset = isStatic ? 1 : 0;

            if (types.length != parameters.length - offset)
                return false;

            for (int i = offset; i < parameters.length; i++) {
                CtClass parameter = parameters[i];
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

    private boolean isController(CtMethod replaced) throws NotFoundException {
        CtClass[] parameters = replaced.getParameterTypes();
        return parameters.length != 0 && parameters[0].getName().equals(NAME_CONTROLLER);
    }

    private boolean isMapArguments(CtMethod replaced, String[] types, boolean isStatic, boolean isController) throws NotFoundException {
        CtClass[] parameters = replaced.getParameterTypes();
        if (isController) {
            return parameters.length <= 1 + (isStatic ? 0 : 1);
        }
        return false;
    }

    private void replaceField(String className, IRebuildField fieldReplaced) {
        if(fieldReplaced == null) return;
        try {
            CtClass ctClass = classPool.get(className);

            CtField[] ctField = ctClass.getDeclaredFields();
            try {
                for (CtField field : ctField) {
                    fieldReplaced.call(ctClass, field);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            ctClass.rebuildClassFile();
        } catch (Exception ignore) {}
    }

    private void addHook(String className, String method, String signature, CtMethod replaced, TypeHook typeHook,
                         boolean controller, ArgumentTypes argumentMap, boolean debug) {
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
                        if(debug)
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
                            if(debug)
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
        System.out.println("Failed register hook for " + method + signature + " " + typeHook.name()+", "+className);
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
            Thread.currentThread().setContextClassLoader(classLoader);
            for(String initClass : initClasses) {
                classLoader.loadClass(initClass)
                        .getMethod("preInit", String.class, String[].class)
                        .invoke(null, mainClass, args);
            }
            classLoader.loadClass(mainClass)
                    .getMethod("main", String[].class)
                    .invoke(null, new Object[] { args });
        } catch (Exception e) {
            e.printStackTrace();
        }

        return this;
    }
}