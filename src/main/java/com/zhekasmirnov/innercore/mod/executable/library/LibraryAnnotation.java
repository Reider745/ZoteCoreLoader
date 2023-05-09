package com.zhekasmirnov.innercore.mod.executable.library;

import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.api.mod.util.ScriptableFunctionImpl;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by zheka on 24.02.2018.
 */

public class LibraryAnnotation {
    private static final String NAME_ID = "$_annotation";


    private final String name;
    private final Class[] parameterTypes;

    public LibraryAnnotation(String name, Class[] parameterTypes) {
        this.name = name;
        this.parameterTypes = parameterTypes;
    }

    public LibraryAnnotation(String name) {
        this(name, new Class[0]);
    }

    public String getName() {
        return name;
    }



    public static class AnnotationInstance {
        private final LibraryAnnotation parent;
        private final Object[] params;

        private AnnotationInstance(LibraryAnnotation parent, Object[] params) {
            this.parent = parent;
            this.params = params;
        }

        public Object[] getParams() {
            return params;
        }

        public <T> T getParameter(int id, Class<? extends T> type) {
            return (T) params[id];
        }
    }

    public static class AnnotationSet {
        private final Object target;
        private final HashSet<AnnotationInstance> instances = new HashSet<>();

        public AnnotationSet(Object target) {
            this.target = target;
        }

        public AnnotationSet(Object target, ArrayList<AnnotationInstance> arr) {
            this(target);
            instances.addAll(arr);
        }

        public Object getTarget() {
            return target;
        }

        public ArrayList<AnnotationInstance> findAll(String name) {
            ArrayList<AnnotationInstance> found = new ArrayList<>();
            for (AnnotationInstance instance : instances) {
                if (name.equals(instance.parent.getName())) {
                    found.add(instance);
                }
            }
            return found;
        }

        public AnnotationInstance find(String name) {
            ArrayList<AnnotationInstance> found = findAll(name);
            return found.size() > 0 ? found.get(0) : null;
        }
    }

    public void injectMethod(final Scriptable scope) {
        scope.put(name, scope, new ScriptableFunctionImpl() {
            @Override
            public Object call(Context context, Scriptable scriptable, Scriptable parent, Object[] parameters) {
                checkParameters(parameters);

                String key = NAME_ID + name;
                while (scope.has(key, scope)) {
                    key = "$" + key;
                }

                ICLog.d("LIBRARY", "annotation injected " + key);
                scope.put(key, scope, new AnnotationInstance(LibraryAnnotation.this, parameters));
                return null;
            }
        });
    }

    private static String objectToTypeName(Object obj) {
        if (obj == null) {
            return "null";
        }
        return obj.getClass().getSimpleName();
    }

    private void reportInvalidParameters(Object[] parameters) {
        StringBuilder message = new StringBuilder();
        message.append(name).append(" got invalid parameters: required (");

        for (Class type : parameterTypes) {
            message.append(type).append(", ");
        }
        message.append(") got (");
        for (Object param : parameters) {
            message.append(objectToTypeName(param)).append(", ");
        }

        message.append(")");
        throw new IllegalArgumentException(message.toString());
    }

    private void checkParameters(Object[] parameters) {
        if (parameters.length != parameterTypes.length) {
            reportInvalidParameters(parameters);
        }

        for (int i = 0; i < parameters.length; i++) {
            if (!parameterTypes[i].isInstance(parameters[i])) {
                reportInvalidParameters(parameters);
            }
        }
    }



    public static ArrayList<AnnotationSet> getAllAnnotations(Scriptable scope) {
        ArrayList<AnnotationSet> allAnnotations = new ArrayList<>();
        Object[] ids = scope.getIds();

        ArrayList<AnnotationInstance> annotations = new ArrayList<>();
        for (Object id : ids) {
            if (id instanceof String) {
                String key = (String) id;
                if (key.contains(NAME_ID)) {
                    annotations.add((AnnotationInstance) scope.get(key, scope));
                    continue;
                }
            }

            Object obj;
            if (id instanceof String) {
                obj = scope.get((String) id, scope);
            }
            else {
                obj = scope.get((int) id, scope);
            }

            allAnnotations.add(new AnnotationSet(obj, annotations));
            annotations.clear();
        }

        return allAnnotations;
    }
}
