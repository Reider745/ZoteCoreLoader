package org.innercore.icstd.script;

import com.zhekasmirnov.innercore.api.mod.ScriptableObjectHelper;
import com.zhekasmirnov.innercore.api.mod.util.ScriptableFunctionImpl;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mozilla.javascript.*;

import java.util.*;

public class ScriptObjectWrap {
    protected final Scriptable obj;

    private ScriptObjectWrap(Scriptable obj) {
        this.obj = obj;
    }

    // --------- basics ---------

    public static Object UNDEFINED = Undefined.instance;

    public static boolean isUndefined(Object o) {
        return Undefined.isUndefined(o);
    }

    // use only for things received from script
    public static ScriptObjectWrap create(Object o) {
        while (o instanceof Wrapper) {
            o = ((Wrapper) o).unwrap();
        }
        if (o instanceof ScriptObjectWrap) {
            return (ScriptObjectWrap) o;
        }
        if (o instanceof Scriptable) {
            return new ScriptObjectWrap((Scriptable) o);
        }
        return null;
    }

    public static ScriptObjectWrap createOrMakeEmptyObj(Object o) {
        ScriptObjectWrap wrap = ScriptObjectWrap.create(o);
        return wrap != null ? wrap : ScriptObjectWrap.createNewEmptyObject();
    }

    public static ScriptObjectWrap createOrThrow(Object o, String paramName) {
        ScriptObjectWrap wrap = ScriptObjectWrap.create(o);
        if (wrap == null) throw new IllegalArgumentException("expected script object for " + paramName + " got " + o);
        return wrap;
    }

    public interface ScriptFunctionImpl {
        Object call(Object[] args);
    }
    public static ScriptObjectWrap createJavaFunction(String name, ScriptFunctionImpl impl) {
        return new ScriptObjectWrap(new ScriptableFunctionImpl() {
            @Override
            public String getClassName() {
                return name;
            }

            @Override
            public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
                return impl.call(args);
            }
        });
    }
    public static ScriptObjectWrap createJavaFunction(ScriptFunctionImpl impl) {
        return createJavaFunction("anonymous_java", impl);
    }

    public static ScriptObjectWrap wrapJavaClass(ScriptObjectWrap scope, String name) {
        try {
            return new ScriptObjectWrap(new NativeJavaClass(scope.obj, Class.forName(name), false));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isScriptObjClass(Class<?> cls) {
        return ScriptObjectWrap.class.isAssignableFrom(cls) || Scriptable.class.isAssignableFrom(cls);
    }

    public static boolean isScriptObj(Object o) {
        while (o instanceof Wrapper) {
            o = ((Wrapper) o).unwrap();
        }
        return o instanceof ScriptObjectWrap || o instanceof Scriptable;
    }

    // convert any java object into representation, that can be sent to script
    public static Object objectToScript(Object o) {
        if (o instanceof ScriptObjectWrap) {
            return ((ScriptObjectWrap) o).passToScript();
        }
        if (!(o instanceof Scriptable)) {
            return Context.javaToJS(o, ScriptableObjectHelper.getDefaultScope());
        }
        return o;
    }

    // rhino only
    public static long scriptObjectToLong(Object o, long def) {
        while (o instanceof Wrapper) {
            o = ((Wrapper) o).unwrap();
        }
        if (o instanceof Number)
            return ((Number) o).longValue();
        return def;
    }

    public static double scriptObjectToDouble(Object o, double def) {
        while (o instanceof Wrapper) {
            o = ((Wrapper) o).unwrap();
        }
        if (o instanceof Long)
            return def;
        if (o instanceof Number)
            return ((Number) o).doubleValue();
        try {
            return Double.parseDouble(((CharSequence) o).toString());
        } catch (Exception e) {
            return def;
        }
    }

    public static boolean scriptObjectToBool(Object o) {
        return Context.toBoolean(o);
    }

    public static<T> T scriptToJava(Object o, Class<T> cls) {
        return (T) Context.jsToJava(o, cls);
    }

    // convert non-wrapped script object into java counterpart
    public static Object objectToJava(Object o) {
        if(o instanceof NativeJavaClass)
            return o;

        while (o instanceof Wrapper) {
            o = ((Wrapper) o).unwrap();
        }
        if (o instanceof Scriptable) {
            return new ScriptObjectWrap((Scriptable) o);
        }
        if (isUndefined(o) || o == UniqueTag.NOT_FOUND)
            return null;
        return o;
    }

    public static ScriptObjectWrap createNewEmptyObject() {
        return new ScriptObjectWrap(ScriptableObjectHelper.createEmpty());
    }

    public static ScriptObjectWrap createNewEmptyArray() {
        return new ScriptObjectWrap(ScriptableObjectHelper.createEmptyArray());
    }

    public static ScriptObjectWrap createNewIntArray(int[] data) {
        Object[] objData = new Object[data.length];
        for (int i = 0; i < data.length; i++)
            objData[i] = data[i];
        return new ScriptObjectWrap(ScriptableObjectHelper.createArray(objData));
    }

    public static ScriptObjectWrap createNewArray(Object[] data) {
        return new ScriptObjectWrap(ScriptableObjectHelper.createArray(data));
    }

    public static ScriptObjectWrap createNewArray(List<?> data) {
        return new ScriptObjectWrap(ScriptableObjectHelper.createArray(data));
    }

    public static<V> ScriptObjectWrap createFromMap(Map<String, V> map) {
        return new ScriptObjectWrap(ScriptableObjectHelper.createFromMap(map));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ScriptObjectWrap wrap = (ScriptObjectWrap) o;
        return Objects.equals(obj, wrap.obj);
    }

    @Override
    public int hashCode() {
        return Objects.hash(obj);
    }

    // intentionally typeless, do not cast the result
    public Object passToScript() {
        return obj;
    }

    public boolean isFunction() {
        return obj instanceof Function;
    }

    public boolean isArray() {
        return obj instanceof NativeArray;
    }

    public int getArrayLength() {
        if (obj instanceof NativeArray) {
            return ((NativeArray) obj).size();
        }
        return -1;
    }

    public Iterable<String> iterStringKeys() {
        if (obj instanceof ScriptableObject && !(obj instanceof NativeArray)) {
            final Scriptable o = obj;
            return () -> {
                final Object[] ids = o.getIds();
                return new Iterator<String>() {
                    int idx = 0;

                    private void search() {
                        while (idx < ids.length) {
                            if (ids[idx] instanceof String)
                                break;
                            idx++;
                        }
                    }

                    @Override
                    public boolean hasNext() {
                        search();
                        return idx < ids.length;
                    }

                    @Override
                    public String next() {
                        search();
                        return (String) ids[idx++];
                    }
                };
            };

        }
        return () -> new Iterator<String>() {
            @Override
            public boolean hasNext() {
                return false;
            }

            @Override
            public String next() {
                return null;
            }
        };
    }

    // --------- getters ---------

    public boolean hasKey(String key) { return obj.has(key, obj); }
    public boolean hasKey(int key) { return obj.has(key, obj); }

    // for java objects only, will return null for scriptable
    public Object getJavaObj(String key) { return objToObj(internalGetAndUnwrap(key)); }
    public Object getJavaObj(int key) { return objToObj(internalGetAndUnwrap(key)); }
    public Object getJavaObj(String key, Object def) {
        Object o = getJavaObj(key);
        return o != null ? o : def;
    }
    public Object getJavaObj(int key, Object def) {
        Object o = getJavaObj(key);
        return o != null ? o : def;
    }
    // java objects or v8::BigNum, useful for native objects wrappers
    public Object getJavaObjOrLong(String key) {
        Object o = internalGetAndUnwrap(key);
        if (o instanceof Long)
            return o;
        return objToObj(o);
    }

    // SLOW, USE ONLY IF REALLY NEEDED
    // automatically decide the type of value
    public Object getAnyFromScript(String key) { return objectToJava(obj.get(key, obj)); }
    public Object getAnyFromScript(int key) { return objectToJava(obj.get(key, obj)); }
    public Object getAnyFromScript(String key, Object def) {
        Object val = getAnyFromScript(key);
        return val != null ? val : def;
    }
    public Object getAnyFromScript(int key, Object def) {
        Object val = getAnyFromScript(key);
        return val != null ? val : def;
    }
    // v8::Object
    public ScriptObjectWrap getScriptObj(String key) { return create(internalGetAndUnwrap(key)); }
    public ScriptObjectWrap getScriptObj(int key) { return create(internalGetAndUnwrap(key)); }
    public ScriptObjectWrap getScriptObj(String key, ScriptObjectWrap def) {
        ScriptObjectWrap val = getScriptObj(key);
        return val != null ? val : def;
    }
    public ScriptObjectWrap getScriptObj(int key, ScriptObjectWrap def) {
        ScriptObjectWrap val = getScriptObj(key);
        return val != null ? val : def;
    }
    // v8::String
    public String getString(String key, String def) { return objToString(obj.get(key, obj), def); }
    public String getString(int key, String def) { return objToString(obj.get(key, obj), def); }
    public String getString(String key) { return objToString(obj.get(key, obj), null); }
    public String getString(int key) { return objToString(obj.get(key, obj), null); }
    // v8::Number
    public int getInt(String key, int def) { return objToInt(obj.get(key, obj), def); }
    public int getInt(int key, int def) { return objToInt(obj.get(key, obj), def); }
    // v8::Number
    public double getDouble(String key, double def) { return objToDouble(obj.get(key, obj), def); }
    public double getDouble(int key, double def) { return objToDouble(obj.get(key, obj), def); }
    public float getFloat(String key, float def) { return (float) getDouble(key, def); }
    public float getFloat(int key, float def) { return (float) getDouble(key, def); }
    // v8::BigInt
    public long getLong(String key, long def) { return objToLong(internalGetAndUnwrap(key), def); }
    public long getLong(int key, long def) { return objToLong(internalGetAndUnwrap(key), def); }
    // v8::BigInt and v8::Number
    public Number getNumber(String key) { return objToNumber(obj.get(key, obj)); }
    public Number getNumber(int key) { return objToNumber(obj.get(key, obj)); }
    // v8::Boolean, consider: do we need strict bools or not?
    public boolean getBoolean(String key, boolean def) { return objToBoolean(obj.get(key, obj), def); }
    public boolean getBoolean(int key, boolean def) { return objToBoolean(obj.get(key, obj), def); }
    // return -1 if no prop exists
    public int getTriBool(String key) { return objToTriBool(obj.get(key, obj)); }
    public int getTriBool(int key) { return objToTriBool(obj.get(key, obj)); }

    // --------- setters ---------
    public void delete(String key) { obj.delete(key); }
    public void delete(int key) { obj.delete(key); }

    public int arrayAllocPush() {
        if (isArray()) {
            NativeArray arr = (NativeArray) obj;
            int pos = (int) arr.getLength();
            arr.put(pos, arr, Undefined.instance);
            return pos;
        }
        return -1;
    }

    public int allocArrayInsert(int idx) {
        if (!isArray())
            return -1;
        NativeArray arr = (NativeArray) obj;
        int id = idx;
        if (hasKey(id)) {
            Object element = Undefined.instance;
            while (true) {
                if (hasKey(id)) {
                    Object temp = arr.get(id);
                    arr.put(id, arr, element);
                    element = temp;
                    id++;
                } else {
                    arr.put(id, arr, element);
                    break;
                }
            }
        }
        return idx;
    }

    // SLOW, USE ONLY IF REALLY NEEDED
    // automatically decide the type of value
    public void setAnyFromJava(String key, Object value) { obj.put(key, obj, objectToScript(value)); }
    public void setAnyFromJava(int key, Object value) { obj.put(key, obj, objectToScript(value)); }
    // for java objects only
    public void setJavaObj(String key, Object value) { obj.put(key, obj, javaObjToScript(value)); }
    public void setJavaObj(int key, Object value) { obj.put(key, obj, javaObjToScript(value)); }
    // v8::Object
    public void setScriptObj(String key, ScriptObjectWrap value) { obj.put(key, obj, value != null ? value.obj : null); }
    public void setScriptObj(int key, ScriptObjectWrap value) { obj.put(key, obj, value != null ? value.obj : null); }
    // v8::String
    public void setString(String key, String value) { obj.put(key, obj, value); }
    public void setString(int key, String value) { obj.put(key, obj, value); }
    // v8::Number
    public void setInt(String key, int value) { obj.put(key, obj, value); }
    public void setInt(int key, int value) { obj.put(key, obj, value); }
    // v8::Number
    public void setDouble(String key, double value) { obj.put(key, obj, value); }
    public void setDouble(int key, double value) { obj.put(key, obj, value); }
    public void setFloat(String key, float value) { obj.put(key, obj, value); }
    public void setFloat(int key, float value) { obj.put(key, obj, value); }
    // v8::BigInt
    public void setLong(String key, long value) { obj.put(key, obj, longToScript(value)); }
    public void setLong(int key, long value) { obj.put(key, obj, longToScript(value)); }
    // v8::Boolean
    public void setBoolean(String key, boolean value) { obj.put(key, obj, value); }
    public void setBoolean(int key, boolean value) { obj.put(key, obj, value); }

    public void copyFieldsFrom(ScriptObjectWrap other) {
        for (Object key : other.obj.getIds()) {
            if (key instanceof String) {
                obj.put((String) key, obj, other.obj.get((String) key, other.obj));
            }
        }
    }

    // --------- function invoke ---------
    public static final Object[] NO_FUNC_ARGS = new Object[0];
    public static final Object NOT_A_FUNC = new Object();

    public void invokeAsVoidFunc(ScriptObjectWrap thisObj, Object[] args) {
        invokeAsFunc(obj, thisObj, args);
    }
    public ScriptObjectWrap invokeAsJavaObjFunc(ScriptObjectWrap thisObj, Object[] args) {
        return ScriptObjectWrap.create(invokeAsFunc(obj, thisObj, args));
    }
    public ScriptObjectWrap invokeAsScriptObjFunc(ScriptObjectWrap thisObj,Object[] args) {
        return ScriptObjectWrap.create(invokeAsFunc(obj, thisObj, args));
    }
    public int invokeAsIntFunc(ScriptObjectWrap thisObj,Object[] args, int def) {
        return objToInt(invokeAsFunc(obj, thisObj, args), def);
    }
    public double invokeAsDoubleFunc(ScriptObjectWrap thisObj,Object[] args, double def) {
        return objToDouble(invokeAsFunc(obj, thisObj, args), def);
    }
    public String invokeAsStringFunc(ScriptObjectWrap thisObj, Object[] args, String def) {
        return objToString(invokeAsFunc(obj, thisObj, args), def);
    }
    public boolean invokeAsBooleanFunc(ScriptObjectWrap thisObj,Object[] args, boolean def) {
        return objToBoolean(invokeAsFunc(obj, thisObj, args), def);
    }

    public void invokeVoidMemberFunc(String name, Object[] args) {
        invokeMemberFunc(name, this, args);
    }
    public ScriptObjectWrap invokeScriptObjMemberFunc(String name, Object[] args) {
        return ScriptObjectWrap.create(invokeMemberFunc(name, this, args));
    }
    public Object invokeJavaObjMemberFunc(String name, Object[] args) {
        return objToObj(invokeMemberFunc(name, this, args));
    }
    public int invokeIntMemberFunc(String name, Object[] args, int def) {
        return objToInt(invokeMemberFunc(name, this, args), def);
    }
    public double invokeDoubleMemberFunc(String name, Object[] args, double def) {
        return objToDouble(invokeMemberFunc(name, this, args), def);
    }
    public String invokeStringMemberFunc(String name, Object[] args, String def) {
        return objToString(invokeMemberFunc(name, this, args), def);
    }
    public boolean invokeBooleanMemberFunc(String name, Object[] args, boolean def) {
        return objToBoolean(invokeMemberFunc(name, this, args), def);
    }


    // --------- json ---------

    public String toJsonString() {
        return NativeJSON.stringify(ScriptEngineWrap.assureContextForCurrentThread(), ScriptableObjectHelper.getDefaultScope(), obj, null, null).toString();
    }

    public static ScriptObjectWrap parseJsonString(String str) {
        return ScriptObjectWrap.create(NativeJSON.parse(ScriptEngineWrap.assureContextForCurrentThread(), ScriptableObjectHelper.getDefaultScope(), str, (context, scriptable, scriptable1, objects) -> objects[1]));
    }

    /*
    public static ScriptObjectWrap parseJson(JSONArray json) {
        ArrayList<Object> arr = new ArrayList<>();
        for (int key = 0; key < json.length(); key++) {
            Object value = json.opt(key);
            if (value instanceof JSONObject) {
                arr.add(parseJson((JSONObject) value));
            } else if (value instanceof JSONArray) {
                arr.add(parseJson((JSONArray) value));
            } else {
                arr.add(value);
            }
        }
        return ScriptObjectWrap.createNewArray(arr);
    }

    public static ScriptObjectWrap parseJson(JSONObject json) {
        ScriptObjectWrap result = ScriptObjectWrap.createNewEmptyObject();
        JSONArray keys = json.names();
        if (keys != null) {
            for (int i = 0; i < keys.length(); i++) {
                String key = keys.optString(i);
                if (key != null) {
                    Object value = json.opt(key);
                    if (value instanceof JSONObject) {
                        result.setScriptObj(key, parseJson((JSONObject) value));
                    } else if (value instanceof JSONArray) {
                        result.setScriptObj(key, parseJson((JSONArray) value));
                    } else {
                        result.setAnyFromJava(key, value);
                    }
                }
            }
        }
        return result;
    }

    public static ScriptObjectWrap parseJsonString(String str) {
        boolean isArray = false;
        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            if (ch == ' ' || ch == '\t' || ch == '\r' || ch == '\n')
                continue;
            isArray = ch == '[';
            break;
        }

        try {
            if (isArray)
                return parseJson(new JSONArray(str));
            return parseJson(new JSONObject(str));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }*/

    public float[] getFloatArray(String key, int minLength, float defaultValue) {
        defaultValue = getFloat(key, defaultValue);
        ScriptObjectWrap array = getScriptObj(key);
        if (array != null) {
            int length = array.getArrayLength();
            float[] jArray = new float[(int) Math.max(length, minLength)];
            for (int i = 0; i < jArray.length; i++) {
                if (i < length)
                    jArray[i] = array.getFloat(i, defaultValue);
                else
                    jArray[i] = defaultValue;
            }
            return jArray;
        } else {
            float[] jArray = new float[minLength];
            for (int i = 0; i < minLength; i++)
                jArray[i] = defaultValue;
            return jArray;
        }
    }


    // --------- helper section, implementation - independent ---------


    public float[] getColorTemplate(String key, float defaultValue) {
        float[] color = getFloatArray(key, 4, defaultValue);
        ScriptObjectWrap wrapper = getScriptObj(key);
        if (wrapper != null) {
            color[0] = wrapper.getFloat("r", color[0]);
            color[1] = wrapper.getFloat("g", color[1]);
            color[2] = wrapper.getFloat("b", color[2]);
            color[3] = wrapper.getFloat("a", color[3]);
        }
        return color;
    }

    public float[] getMinMaxTemplate(String key, float defaultValue) {
        float[] arr = getFloatArray(key, 2, defaultValue);
        ScriptObjectWrap wrapper = getScriptObj(key);
        if (wrapper != null) {
            arr[0] = wrapper.getFloat("min", arr[0]);
            arr[1] = wrapper.getFloat("max", arr[1]);
        }
        if (arr[1] < arr[0]) {
            arr[1] = arr[0];
        }
        return arr;
    }

    public float[] getVec3Template(String key, float defaultValue) {
        float[] arr = getFloatArray(key, 3, defaultValue);
        ScriptObjectWrap wrapper = getScriptObj(key);
        if (wrapper != null) {
            arr[0] = wrapper.getFloat("x", arr[0]);
            arr[1] = wrapper.getFloat("y", arr[1]);
            arr[2] = wrapper.getFloat("z", arr[2]);
        }
        return arr;
    }

    public float[] getUVTemplate(String key) {
        float[] arr = getFloatArray(key, 4, 0);
        ScriptObjectWrap wrapper = getScriptObj(key);
        if (wrapper != null) {
            arr[0] = wrapper.getFloat("u1", arr[0]);
            arr[1] = wrapper.getFloat("v1", arr[1]);
            arr[2] = wrapper.getFloat("u2", arr[2]);
            arr[3] = wrapper.getFloat("v2", arr[3]);
        }
        if (arr[0] == 0 && arr[1] == 0 && arr[2] == 0 && arr[3] == 0) {
            arr[2] = 1;
            arr[3] = 1;
        }
        return arr;
    }

    // --------- rhino internals ---------


    protected Object internalGetAndUnwrap(String key) {
        Object o = obj.get(key, obj);
        while (o instanceof Wrapper)
            o = ((Wrapper) o).unwrap();
        return o;
    }

    protected Object internalGetAndUnwrap(int key) {
        Object o = obj.get(key, obj);
        while (o instanceof Wrapper)
            o = ((Wrapper) o).unwrap();
        return o;
    }

    protected static Object invokeAsFunc(Object o, ScriptObjectWrap thisObj, Object[] args) {
        if (o instanceof Function) {
            Function f = (Function) o;
            Scriptable scope = f.getParentScope();
            Object res = f.call(ScriptEngineWrap.assureContextForCurrentThread(), scope, thisObj != null ? thisObj.obj : scope, args);
            return objectToJava(res);
        }
        return NOT_A_FUNC;
    }

    protected Object invokeMemberFunc(String name, ScriptObjectWrap thisObj, Object[] args) {
        Object o = obj.get(name, obj);
        return invokeAsFunc(o, thisObj, args);
    }

    protected static Object objToObj(Object o) {
        o = objectToJava(o);
        if (o instanceof Number || o instanceof Boolean || o instanceof Scriptable || o instanceof String) {
            return o;
        }
        // todo: debug check, remove
        if (o instanceof ScriptObjectWrap) {
            throw new RuntimeException("script object wrap detected (1)");
        }
        if (o == UniqueTag.NOT_FOUND)
            return null;
        return o;
    }

    protected static String objToString(Object o, String def) {
        o = objectToJava(o);
        if (o instanceof CharSequence) {
            return o.toString();
        }
        return def;
    }

    protected static int objToInt(Object o, int def) {
        o = objectToJava(o);
        if (o instanceof Number) {
            return ((Number) o).intValue();
        }
        return def;
    }

    protected static double objToDouble(Object o, double def) {
        o = objectToJava(o);
        if (o instanceof Number) {
            return ((Number) o).doubleValue();
        }
        return def;
    }

    protected static long objToLong(Object o, long def) {
        o = objectToJava(o);
        if (o instanceof Long) {
            return (Long) o;
        }
        return def;
    }

    protected static Number objToNumber(Object o) {
        o = objectToJava(o);
        while (o instanceof Wrapper)
            o = ((Wrapper) o).unwrap();
        return o instanceof Number ? (Number) o : null;
    }

    protected static boolean objToBoolean(Object o, boolean def) {
        o = objectToJava(o);
        if (o instanceof Boolean) {
            return (Boolean) o;
        }
        return def;
    }

    protected static int objToTriBool(Object o) {
        o = objectToJava(o);
        if (o instanceof Boolean) {
            return (Boolean) o ? 1 : 0;
        }
        return -1;
    }

    protected static Object longToScript(long value) {
        return (Long) value;
    }

    protected static Object javaObjToScript(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Scriptable) {
            throw new RuntimeException("scriptable detected");
        }
        if (value instanceof ScriptObjectWrap) {
            throw new RuntimeException("script object wrap detected (2)");
        }
        if (value instanceof Number || value instanceof Boolean || value instanceof String) {
            throw new RuntimeException("passed number, boolean or string to javaObjToScript");
        }
        return Context.javaToJS(value, ScriptableObjectHelper.getDefaultScope(), ScriptEngineWrap.assureContextForCurrentThread());
    }
}
