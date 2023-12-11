package com.zhekasmirnov.innercore.api.mod;

import com.zhekasmirnov.horizon.runtime.logger.Logger;
import com.zhekasmirnov.innercore.api.annotations.APIIgnore;
import com.zhekasmirnov.innercore.api.annotations.APIStaticModule;
import com.zhekasmirnov.innercore.api.constants.ConstantRegistry;
import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.api.mod.adaptedscript.AdaptedScriptAPI;
import com.zhekasmirnov.innercore.api.mod.adaptedscript.PreferencesWindowAPI;
import com.zhekasmirnov.innercore.api.mod.coreengine.CoreEngineAPI;
import com.zhekasmirnov.innercore.api.mod.preloader.PreloaderAPI;
import com.zhekasmirnov.innercore.api.mod.util.DebugAPI;
import com.zhekasmirnov.innercore.api.runtime.Callback;
import com.zhekasmirnov.innercore.mod.build.Mod;
import com.zhekasmirnov.innercore.mod.executable.Compiler;
import com.zhekasmirnov.innercore.mod.executable.Executable;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeJavaClass;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.annotations.JSFunction;
import org.mozilla.javascript.annotations.JSStaticFunction;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Created by zheka on 28.07.2017.
 */

public abstract class API extends ScriptableObject {
    public static final String LOGGER_TAG = "INNERCORE-API";

    public abstract String getName();

    public abstract int getLevel();

    public abstract void onLoaded();

    public abstract void onModLoaded(Mod mod);

    public abstract void onCallback(String name, Object[] args);

    public abstract void setupCallbacks(Executable executable);

    protected boolean isLoaded = false;
    protected ArrayList<Executable> executables = new ArrayList<>();

    public void prepareExecutable(Executable executable) {
        if (!isLoaded) {
            ICLog.d(LOGGER_TAG, "loading API: " + this.getName());
            isLoaded = true;
            onLoaded();
        }

        ICLog.d(LOGGER_TAG, "adding executable: api=" + this.getName() + " exec=" + executable.name);
        executables.add(executable);
        setupCallbacks(executable);
    }

    public void invokeExecutableCallback(String name, Object[] args) {
        Compiler.assureContextForCurrentThread();
        for (Executable executable : executables) {
            executable.callFunction(name, args);
        }
    }

    protected void addExecutableCallback(Executable exec, String callbackName, String funcName) {
        Function func = exec.getFunction(funcName);
        if (func != null) {
            Callback.addCallback(callbackName, func, 0);
        }
    }

    @JSStaticFunction
    public String getCurrentAPIName() {
        return getName();
    }

    @JSStaticFunction
    public int getCurrentAPILevel() {
        return getLevel();
    }

    public String getClassName() {
        return getName();
    }

    private static ScriptableObject currentScopeToInject = null;

    protected void injectIntoScope(ScriptableObject scope, List<String> filter) {
        currentScopeToInject = scope;
        API.injectIntoScope(getClass(), scope, filter);
        ConstantRegistry.injectConstants(scope);
    }

    public void injectIntoScope(ScriptableObject scope) {
        injectIntoScope(scope, null);
    }

    private static HashMap<Class<?>, ArrayList<String>> getAllClassMethods(Class<?> clazz, List<String> filter) {
        HashMap<Class<?>, ArrayList<String>> methodMap = new HashMap<>();

        while (clazz != null && clazz.getName().contains("com.zhekasmirnov.innercore")) {
            Method[] methods = clazz.getMethods();

            ArrayList<String> names = new ArrayList<>();
            if (!methodMap.containsKey(clazz)) {
                methodMap.put(clazz, names);
            }

            for (Method method : methods) {
                if (filter != null && !filter.contains(method.getName())) {
                    continue;
                }
                if (method.getAnnotation(JSFunction.class) != null
                        || method.getAnnotation(JSStaticFunction.class) != null) {
                    Class<?> methodClass = method.getDeclaringClass();
                    if (methodClass == clazz) {
                        names.add(method.getName());
                    }
                }
            }

            clazz = clazz.getSuperclass();
        }

        return methodMap;
    }

    private static Collection<Class<?>> getSubclasses(Class<?> clazz) {
        HashSet<Class<?>> allClasses = new HashSet<>();

        while (clazz != null && clazz.getName().contains("com.zhekasmirnov.innercore")) {
            Class<?>[] classes = clazz.getClasses();
            for (Class<?> cla$$ : classes) {
                allClasses.add(cla$$);
            }
            clazz = clazz.getSuperclass();
        }

        return allClasses;
    }

    protected static void injectIntoScope(Class<?> apiClass, ScriptableObject scope, List<String> filter) {
        // Log.d(LOGGER_TAG, "injecting api class: " + apiClass.getSimpleName());

        HashMap<Class<?>, ArrayList<String>> methodMap = getAllClassMethods(apiClass, filter);

        Iterator<Class<?>> methodIterator = methodMap.keySet().iterator();
        while (methodIterator.hasNext()) {
            Class<?> methodClass = methodIterator.next();
            ArrayList<String> names = methodMap.get(methodClass);
            String[] nameArr = new String[names.size()];
            names.toArray(nameArr);
            scope.defineFunctionProperties(nameArr, methodClass, ScriptableObject.DONTENUM);
        }

        Collection<Class<?>> classes = getSubclasses(apiClass);
        for (final Class<?> module : classes) {
            if (filter != null && !filter.contains(module.getSimpleName())) {
                continue;
            }

            if (module.getAnnotation(APIStaticModule.class) != null) {
                ScriptableObject childScope = new ScriptableObject() {
                    @Override
                    public String getClassName() {
                        return module.getSimpleName();
                    }
                };

                scope.defineProperty(module.getSimpleName(), childScope, ScriptableObject.DONTENUM);

                injectIntoScope(module, childScope, null);
            } else if (module.getAnnotation(APIIgnore.class) == null) {
                try {
                    scope.defineProperty(module.getSimpleName(),
                            new NativeJavaClass(currentScopeToInject, module, false), ScriptableObject.DONTENUM);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @JSStaticFunction
    public void createDump(Object _aClass) {
        Class<?> aClass = null;
        if (_aClass instanceof Class) {
            aClass = (Class<?>) _aClass;
        }
        if (aClass == null) {
            aClass = getClass();
        }

        String str = createDumpString(aClass, "", "");

        DebugAPI.dialog(str);
    }

    private String dumpMethod(Method method) {
        String str = method.getName() + "(";

        Class<?>[] params = method.getParameterTypes();
        for (int i = 0; i < params.length; i++) {
            if (i > 0) {
                str += ", ";
            }
            str += params[i].getSimpleName();
        }
        str += ")";

        return str;
    }

    private String createDumpString(Class<?> aClass, String prefix, String str) {
        Method[] methods = aClass.getMethods();
        for (Method method : methods) {
            if (method.getAnnotation(JSStaticFunction.class) != null) {
                str += prefix + dumpMethod(method);
                str += "\n";

                Class<?> returnType = method.getReturnType();
                if (returnType != null && !returnType.isPrimitive()) {
                    Method[] returnMethods = returnType.getMethods();
                    for (Method returnMethod : returnMethods) {
                        if ((returnMethod.getModifiers() & Modifier.STATIC) == 0
                                && !returnMethod.getName().equals("getClassName")
                                && returnMethod.getDeclaringClass().getPackage().toString().contains("zhekasmirnov")) {
                            str += "\t" + dumpMethod(returnMethod) + "\n";
                        }
                    }
                }
            }
        }

        str += "\n";

        Class<?>[] classes = aClass.getClasses();
        for (Class<?> module : classes) {
            if (module.getAnnotation(APIStaticModule.class) != null) {
                str = createDumpString(module, prefix + module.getSimpleName() + ".", str);
            }
        }

        return str;
    }

    private static ArrayList<API> APIInstanceList = new ArrayList<>();

    public static void loadAllAPIs() {
        registerInstance(new AdaptedScriptAPI());
        registerInstance(new PreferencesWindowAPI());
        registerInstance(new CoreEngineAPI());
        registerInstance(new PreloaderAPI());
    }

    protected static void registerInstance(API instance) {
        if (!APIInstanceList.contains(instance)) {
            Logger.debug(LOGGER_TAG, "Register Mod API: " + instance.getName());
            APIInstanceList.add(instance);
        }
    }

    public static API getInstanceByName(String name) {
        for (int i = 0; i < APIInstanceList.size(); i++) {
            API apiInstance = APIInstanceList.get(i);
            if (apiInstance.getName().equals(name)) {
                return apiInstance;
            }
        }
        return null;
    }

    public static void invokeCallback(String name, Object... args) {
        for (API apiInstance : APIInstanceList) {
            apiInstance.onCallback(name, args);
        }
    }

    public static ArrayList<Function> collectAllCallbacks(String name) {
        ArrayList<Function> callbacks = new ArrayList<>();

        for (API apiInstance : APIInstanceList) {
            for (Executable exec : apiInstance.executables) {
                Function callback = exec.getFunction(name);
                if (callback != null) {
                    callbacks.add(callback);
                }
            }
        }

        return callbacks;
    }

    public API newInstance() {
        try {
            Constructor<? extends API> constructor = getClass().getConstructor();
            API instance = constructor.newInstance();
            return instance;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void debugLookUpClass(Class<?> clazz) {
        ICLog.d("LOOKUP", "starting at " + clazz);
        debugLookUpClass(clazz, "");
    }

    private static void debugLookUpClass(Class<?> clazz, String prefix) {
        if (clazz == null) {
            return;
        }

        String _prefix = prefix + "  ";
        HashMap<Class<?>, ArrayList<String>> methodMap = getAllClassMethods(clazz, null);

        for (Class<?> cla$$ : methodMap.keySet()) {
            ICLog.d("LOOKUP", prefix + "methods in class " + cla$$.getSimpleName() + ": ");
            for (String name : methodMap.get(cla$$)) {
                ICLog.d("LOOKUP", _prefix + name + "(...)");
            }
        }

        Collection<Class<?>> classes = getSubclasses(clazz);
        for (Class<?> cla$$ : classes) {
            if (cla$$ != null && cla$$.getAnnotation(APIIgnore.class) == null) {
                if (cla$$.getAnnotation(APIStaticModule.class) != null) {
                    ICLog.d("LOOKUP", prefix + "looking up module " + cla$$.getSimpleName() + ":");
                } else {
                    ICLog.d("LOOKUP", prefix + "looking up constructor class " + cla$$.getSimpleName() + ":");
                }
                debugLookUpClass(cla$$, _prefix);
            }
        }
    }
}
