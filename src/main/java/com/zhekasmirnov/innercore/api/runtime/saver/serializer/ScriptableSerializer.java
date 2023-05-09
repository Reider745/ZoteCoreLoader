package com.zhekasmirnov.innercore.api.runtime.saver.serializer;

import com.zhekasmirnov.innercore.api.mod.ScriptableObjectHelper;
import com.zhekasmirnov.innercore.api.runtime.saver.ObjectSaverRegistry;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;

import java.util.*;

public class ScriptableSerializer {
    public interface SerializationErrorHandler {
        void handle(Exception err);
    }

    private static Object avoidInvalidJsonValues(Object object) {
        if (object instanceof Undefined) {
            return null;
        }
        if (object instanceof Number) {
            Number number = (Number) object;
            double d = number.doubleValue();
            float f = number.floatValue();
            if (Double.isNaN(d) || Float.isNaN(f) || Double.isInfinite(d) || Float.isInfinite(f)) {
                return 0.0f;
            }
        }
        return object;
    }

    private static Object scriptableToJson0(Object input, SerializationErrorHandler handler, Set<ScriptableObject> iteratedObjects) {
        // this will unwrap object if required, run saver, if exists, or just return object itself
        input = ObjectSaverRegistry.saveOrSkipObject(input);

        // if instance of scriptable, serialize to json, otherwise return as it is
        if (input instanceof ScriptableObject) {
            ScriptableObject scriptableObject = (ScriptableObject) input;

            // check for cyclic reference
            if (iteratedObjects.contains(scriptableObject)) {
                return null;
            }
            iteratedObjects.add(scriptableObject);

            // if it an array
            if (scriptableObject instanceof NativeArray) {
                JSONArray json = new JSONArray();
                for (Object element : ((NativeArray) scriptableObject).toArray()) {
                    // iterate array and serialize every element
                    json.put(scriptableToJson0(element, handler, iteratedObjects));
                }

                // remove from iterated and return
                iteratedObjects.remove(scriptableObject);
                return json;
            } else {
                // if it is object
                JSONObject json = new JSONObject();
                for (Object key : scriptableObject.getIds()) {
                    Object value = scriptableObject.get(key);
                    try {
                        // iterate and serialize every value
                        json.put(key + "", scriptableToJson0(value, handler, iteratedObjects));
                    } catch (JSONException e) {
                        if (handler != null) {
                            handler.handle(e);
                        }
                    }
                }

                // remove from iterated and return
                iteratedObjects.remove(scriptableObject);
                return json;
            }
        } else {
            return avoidInvalidJsonValues(input);
        }
    }

    // receives scriptable or plain object, that should be serialized using scriptable based serialization and returns resulting json, base type value or null
    // this method will replace cycle references with null
    public static Object scriptableToJson(Object object, SerializationErrorHandler handler) {
        return scriptableToJson0(object, handler, new HashSet<>());
    }

    // receives base type value, json object or array and deserializes it into scriptable or any other object type, by using scriptable based serialization
    public static Object scriptableFromJson(Object object) {
        if (object instanceof JSONObject) {
            JSONObject json = (JSONObject) object;
            ScriptableObject scriptable = ScriptableObjectHelper.createEmpty();
            for (Iterator<String> it = json.keys(); it.hasNext(); ) {
                String key = it.next();
                scriptable.put(key, scriptable, scriptableFromJson(json.opt(key)));
            }
            return ObjectSaverRegistry.readObject(scriptable);
        } else if (object instanceof JSONArray) {
            JSONArray json = (JSONArray) object;
            List<Object> array = new ArrayList<>();
            for (int i = 0; i < json.length(); i++) {
                array.add(scriptableFromJson(json.opt(i)));
            }
            return ScriptableObjectHelper.createArray(array);
        } else {
            // assume this is a basic type object
            return object;
        }
    }

    public static String jsonToString(Object json) {
        return "" + json;
    }

    public static Object stringToJson(String str) throws JSONException {
        if (str == null) {
            return null;
        }
        return new JSONObject("{\"a\": " + str + "}").get("a");
    }
}
