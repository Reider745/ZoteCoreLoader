package com.zhekasmirnov.innercore.api.mod;

import com.zhekasmirnov.innercore.mod.executable.Compiler;
import org.json.JSONException;
import org.json.JSONObject;
import org.mozilla.javascript.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by zheka on 31.07.2017.
 */

public class ScriptableObjectHelper {
    public static Object getProperty(Scriptable object, String key, Object fallback) {
        Object prop = object.get(key, object);
        return prop == Scriptable.NOT_FOUND ? fallback : prop;
    }

    public static Object getJavaProperty(Scriptable object, String key, Class<?> clazz, Object fallback) {
        try {
            return Context.jsToJava(getProperty(object, key, fallback), clazz);
        } catch (Exception e) {
            return fallback;
        }
    }

    public static Scriptable getScriptableProperty(Scriptable object, String key, Scriptable fallback) {
        Object result = getProperty(object, key, fallback);
        if (result instanceof Scriptable) {
            return (Scriptable) result;
        }
        return fallback;
    }

    public static ScriptableObject getScriptableObjectProperty(Scriptable object, String key, ScriptableObject fallback) {
        Object result = getProperty(object, key, fallback);
        if (result instanceof ScriptableObject) {
            return (ScriptableObject) result;
        }
        return fallback;
    }

    public static NativeArray getNativeArrayProperty(Scriptable object, String key, NativeArray fallback) {
        Object result = getProperty(object, key, fallback);
        if (result instanceof NativeArray) {
            return (NativeArray) result;
        }
        return fallback;
    }

    public static String getStringProperty(Scriptable object, String key, CharSequence fallback) {
        Object result = getProperty(object, key, fallback);
        if (result instanceof CharSequence) {
            return result.toString();
        }
        return fallback != null ? fallback.toString() : null;
    }

    public static boolean getBooleanProperty(Scriptable object, String key, boolean fallback) {
        Object result = getProperty(object, key, fallback);
        if (result instanceof Boolean) {
            return (boolean) result;
        }
        return fallback;
    }

    public static float getFloatProperty(Scriptable object, String key, float fallback) {
        Object result = getProperty(object, key, fallback);
        if (result instanceof Double) {
            return (float) (double) result;
        }
        if (result instanceof Integer) {
            return (float) (int) result;
        }
        if (result instanceof Float) {
            return (float) result;
        }
        return  fallback;
    }

    public static double getDoubleProperty(Scriptable object, String key, double fallback) {
        Object result = getProperty(object, key, fallback);
        if (result instanceof Number) {
            return ((Number) result).doubleValue();
        }
        return fallback;
    }

    public static long[] scriptableToLongArray(NativeArray array) {
        if (array != null) {
            long[] longs = new long[array.size()];
            int offset = 0;
            for (Object obj : array.toArray()) {
                if (obj != null) {
                    longs[offset++] = ((Number) obj).longValue();
                }
            }
            if (offset != longs.length) {
                return Arrays.copyOf(longs, offset);
            }
            return longs;
        }
        return new long[0];
    }

    public static NativeArray longToScriptableArray(long[] longs) {
        Object[] array = new Object[longs.length];
        int offset = 0;
        for (long obj : longs) {
            array[offset++] = obj;
        }
        return ScriptableObjectHelper.createArray(array);
    }

    public static int getIntProperty(Scriptable object, String key, int fallback) {
        Object result = getProperty(object, key, fallback);
        if (result instanceof Double) {
            return (int) (double) result;
        }
        if (result instanceof Integer) {
            return (int) result;
        }
        if (result instanceof Float) {
            return (int) (float) result;
        }
        return  fallback;
    }

    public static int getLongProperty(Scriptable object, String key, int fallback) {
        Object result = getProperty(object, key, fallback);
        if (result instanceof Double) {
            return (int) (double) result;
        }
        if (result instanceof Integer) {
            return (int) result;
        }
        if (result instanceof Float) {
            return (int) (float) result;
        }
        return  fallback;
    }


    private static ScriptableObject defaultScope = Context.enter().initStandardObjects();

    public static ScriptableObject getDefaultScope() {
        return defaultScope;
    }

    public static ScriptableObject createEmpty() {
        return (ScriptableObject) Context.enter().newObject(defaultScope);
    }

    public static<K, V> ScriptableObject createFromMap(Map<K, V> map) {
        if (map == null) {
            return null;
        }
        ScriptableObject object = (ScriptableObject) Context.enter().newObject(defaultScope);
        for (Map.Entry<K, V> entry : map.entrySet()) {
            object.put("" + entry.getKey(), object, entry.getValue());
        }
        return object;
    }

    public static NativeArray createEmptyArray() {
        return (NativeArray) Context.enter().newArray(defaultScope, 0);
    }

    public static NativeArray createArray(Object[] arr) {
        return (NativeArray) Context.enter().newArray(defaultScope, arr);
    }

    public static NativeArray createArray(List<?> arr) {
        return (NativeArray) Context.enter().newArray(defaultScope, arr.toArray());
    }


    public static Object unwrap(Object obj) {
        while (obj instanceof Wrapper)
            obj = ((Wrapper) obj).unwrap();
        return obj;
    }

    public static String throwNotNull(String msg) {
        throw new NullPointerException(msg);
    }

    public static float convertToFloat(Object obj) {
        if (obj instanceof CharSequence) {
            return Float.parseFloat(((CharSequence) obj).toString());
        }
        if (obj instanceof Number) {
            return ((Number) obj).floatValue();
        }
        double val = ScriptRuntime.toNumber(obj);
        if (Double.isNaN(val)) throw new IllegalArgumentException("Cannot convert " + obj + " to float");
        return (float) val;
    }

    public static double convertToDouble(Object obj) {
        if (obj instanceof CharSequence) {
            return Double.parseDouble(((CharSequence) obj).toString());
        }
        if (obj instanceof Number) {
            return ((Number) obj).doubleValue();
        }
        double val = ScriptRuntime.toNumber(obj);
        if (Double.isNaN(val)) throw new IllegalArgumentException("Cannot convert " + obj + " to double");
        return val;
    }

    public static int convertToInt(Object obj) {
        if (obj instanceof CharSequence) {
            return (int) Double.parseDouble(((CharSequence) obj).toString());
        }
        if (obj instanceof Number) {
            return ((Number) obj).intValue();
        }
        double val = ScriptRuntime.toNumber(obj);
        if (Double.isNaN(val)) throw new IllegalArgumentException("Cannot convert " + obj + " to double");
        return (int) val;
    }

    public static boolean convertToBool(Object obj) {
        if (obj instanceof Boolean) {
            return (Boolean) obj;
        }
        if (obj == null || obj == Undefined.instance)
            return false;
        return ScriptRuntime.toBoolean(obj);
    }


    public static JSONObject toJSON(ScriptableObject obj) {
        try {
            return toJSON(obj, new JSONObject());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static JSONObject toJSON(ScriptableObject obj, JSONObject json) throws JSONException {
        if (obj == null) {
            return null;
        }

        Object[] keys = obj.getAllIds();
        for (Object key : keys) {
            Object val = obj.get(key);
            if (val.getClass().isPrimitive()) {
                json.put(key.toString(), val);
            }
            else if (val instanceof CharSequence) {
                json.put(key.toString(), val.toString());
            }
            else if (val instanceof ScriptableObject) {
                json.put(key.toString(), toJSON((ScriptableObject) val, new JSONObject()));
            }
        }

        return json;
    }

    public static String stringify(ScriptableObject obj, Object space) {
        Context context = Compiler.assureContextForCurrentThread();
        return (String) NativeJSON.stringify(context, obj.getParentScope(), obj, null, space);
    }
}
