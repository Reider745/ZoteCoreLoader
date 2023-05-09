package com.zhekasmirnov.innercore.api.runtime.saver;

import com.zhekasmirnov.innercore.api.mod.ScriptableObjectHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by zheka on 19.08.2017.
 */

public class JsonHelper {

    private static class JsonToString {
        private StringBuilder result;

        private boolean beautify = false;
        private int depth = 0;

        private ArrayList<Boolean> isArrayStack = new ArrayList<>();

        public JsonToString(boolean beautify) {
            this.result = new StringBuilder();
            this.beautify = beautify;
            this.depth = 0;
        }

        private boolean isArray() {
            if (isArrayStack.size() > 0) {
                return isArrayStack.get(0);
            }
            return false;
        }

        private void push(boolean isArray) {
            isArrayStack.add(0, isArray);
            depth++;
        }

        private void pop() {
            if (isArrayStack.size() == 0 || depth < 1) {
                throw new IllegalArgumentException("excess object or array end");
            }
            depth--;
            isArrayStack.remove(0);
        }

        private String getIntend() {
            String intend = "";
            if (!beautify) {
                return intend;
            }
            for (int i = 0; i < depth; i++) {
                intend += "  ";
            }
            return intend;
        }

        private void putCommaIfNeeded() {
            int index = this.result.length() - 1;
            while (index != -1) {
                char c = this.result.charAt(index);
                if (c != ' ' && c != '\n') {
                    break;
                }
                index--;
            }
            if (index == -1) {
                return;
            }

            char last = this.result.charAt(index);
            if (last != ',' && last != '{' && last != '[') {
                this.result.append(",");
            }
        }

        private void begin(boolean isArray) {
            this.result.append(isArray ? "[" : "{").append(beautify ? "\n" : "");
            push(isArray);
        }

        private void end() {
            boolean isArr = isArray();
            pop();
            this.result.append(beautify ? "\n" + getIntend() : "").append(isArr ? "]" : "}");
        }

        public void key(Object key) {
            putCommaIfNeeded();

            if (beautify && this.result.charAt(this.result.length() - 1) != '\n') {
                this.result.append("\n");
            }

            if (beautify) {
                this.result.append(getIntend());
            }

            if (!isArray()) {
                this.result.append("\"").append(key).append("\":");
            }
        }

        private static DecimalFormat format = new DecimalFormat("#");

        public void value(Object val) {
            if (val == null) {
                throw new IllegalArgumentException("value cannot be null");
            }

            String strVal;

            if (val instanceof CharSequence) {
                String str = val.toString();
                str = str.replace("\n", "\\n");
                str = str.replace("\"", "\\\"");
                strVal = "\"" + str + "\"";
            }
            else if (val instanceof Number && ((Number) val).doubleValue() == ((Number) val).longValue()) {
                strVal = format.format(val);
            }
            else {
                strVal = val.toString();
            }

            this.result.append(strVal);
        }

        public String getResult() {
            return result.toString();
        }
    }



    private static HashMap<Integer, ScriptableObject> scriptableByHashCode = new HashMap<>();

    public static synchronized String scriptableToJsonString(ScriptableObject scriptableObject, boolean beautify) {
        if (scriptableObject == null) {
            return "{}";
        }
        scriptableByHashCode.clear();
        JsonToString jsonToString = new JsonToString(beautify);
        stringify(jsonToString, scriptableObject);
        return jsonToString.getResult();
    }

    private static void stringify(JsonToString jsonToString, ScriptableObject scriptableObject) {
        scriptableByHashCode.put(scriptableObject.hashCode(), scriptableObject);

        boolean isArray = scriptableObject instanceof NativeArray;

        jsonToString.begin(isArray);

        Object[] keys = scriptableObject.getAllIds();
        for (Object key : keys) {
            if (isArray && key instanceof CharSequence) {
                continue;
            }

            Object val = scriptableObject.get(key);
            if (val != null) {
                val = ObjectSaverRegistry.unwrapIfNeeded(val);
                if (val instanceof ScriptableObject) {
                    if (scriptableByHashCode.containsKey(val.hashCode())) {
                        continue;
                    }
                    val = ObjectSaverRegistry.saveObjectAndCheckSaveIgnoring(val);
                    if (val != null) {
                        jsonToString.key(key);
                        stringify(jsonToString, (ScriptableObject) val);
                    }
                }
                else if (val instanceof CharSequence || val instanceof Number || val instanceof Boolean) {
                    jsonToString.key(key);
                    jsonToString.value(val);
                }
                else {
                    ScriptableObject saveResult = ObjectSaverRegistry.saveObject(val);
                    if (saveResult != null) {
                        jsonToString.key(key);
                        stringify(jsonToString, saveResult);
                    }
                }
            }
        }

        jsonToString.end();
    }

    public static Scriptable parseJsonString(String string) throws JSONException {
        JSONObject json = new JSONObject(string);
        return jsonToScriptable(json);
    }

    public static Scriptable jsonToScriptable(Object object) {
        ScriptableObject scriptable;
        if (object instanceof JSONArray) {
            // scriptable = ScriptableObjectHelper.createEmptyArray();
            JSONArray arr = (JSONArray) object;
            List<Object> list = new ArrayList<>();

            for (int i = 0; i < arr.length(); i++) {
                Object val = arr.opt(i);
                if (val instanceof JSONObject || val instanceof JSONArray) {
                    val = jsonToScriptable(val);
                }
                list.add(val);
                //scriptable.put(i, scriptable, val);
            }

            //scriptable.put("length", scriptable, arr.length());
            scriptable = ScriptableObjectHelper.createArray(list);
        }
        else if (object instanceof JSONObject) {
            scriptable = ScriptableObjectHelper.createEmpty();
            JSONObject json = (JSONObject) object;
            JSONArray arr = json.names();

            if (arr != null) {
                for (int i = 0; i < arr.length(); i++) {
                    String key = arr.optString(i);
                    Object val = json.opt(key);
                    if (val instanceof JSONObject || val instanceof JSONArray) {
                        val = jsonToScriptable(val);
                    }
                    scriptable.put(key, scriptable, val);
                }
            }
        }
        else {
            throw new IllegalArgumentException("FAILED ASSERTION: JsonHelper.jsonToScriptable can get only JSONObject or JSONArray");
        }

        return ObjectSaverRegistry.readObject(scriptable);
    }
}
