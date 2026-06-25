package org.innercore.icstd.serialization;

import com.zhekasmirnov.innercore.api.log.DialogHelper;
import com.zhekasmirnov.innercore.api.log.ICLog;
import org.innercore.internal.serialization.JsonIO;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class ObjectSerializerDebug {

    public static String serializedToDebugString(Object o, boolean allowCvt) {
        Object cvt = o;
        if (cvt == null)
            cvt = o;
        try {
            if (cvt instanceof JSONObject) {
                return ((JSONObject) cvt).toString(2);
            } else if (cvt instanceof JSONArray) {
                return ((JSONArray) cvt).toString(2);
            }
        } catch (JSONException e) { ICLog.e("SAVER", "JSON serialization failed", e); }
        return "" + o;
    }

    public static boolean isJsonEqual(Object o1, Object o2, boolean ignoreMissingNulls, List<String> verbose) {
        if (o1 == JSONObject.NULL)
            o1 = null;
        if (o2 == JSONObject.NULL)
            o2 = null;
        if (Objects.equals(o1, o2))
            return true;
        if (o1 instanceof Number && o2 instanceof Number && !(o1 instanceof Long || o2 instanceof Long)) {
            return ((Number) o1).doubleValue() == ((Number) o2).doubleValue();
        }
        if (o1 instanceof JSONObject && o2 instanceof JSONObject) {
            JSONObject j1 = (JSONObject) o1;
            JSONObject j2 = (JSONObject) o2;
            for (Iterator<String> iter = j1.keys(); iter.hasNext(); ) {
                String key = iter.next();
                if (!ignoreMissingNulls) {
                    if (!j2.has(key)) {
                        if (verbose != null) {
                            verbose.add("key mismatch: " + key);
                        }
                        return false;
                    }
                }
                else if (!isJsonEqual(j1.opt(key), j2.opt(key), ignoreMissingNulls, verbose))
                    return false;
            }
            for (Iterator<String> iter = j2.keys(); iter.hasNext(); ) {
                String key = iter.next();
                if (!ignoreMissingNulls) {
                    if (!j1.has(key)) {
                        if (verbose != null) {
                            verbose.add("key mismatch: " + key);
                        }
                        return false;
                    }
                }
                else if (!isJsonEqual(j1.opt(key), j2.opt(key), ignoreMissingNulls, verbose))
                    return false;
            }
            if (!ignoreMissingNulls) {
                for (Iterator<String> iter = j1.keys(); iter.hasNext(); ) {
                    String key = iter.next();
                    if (!isJsonEqual(j1.opt(key), j2.opt(key), ignoreMissingNulls, verbose))
                        return false;
                }
            }
            return true;
        }
        if (o1 instanceof JSONArray && o2 instanceof JSONArray) {
            JSONArray j1 = (JSONArray) o1;
            JSONArray j2 = (JSONArray) o2;
            if (j1.length() != j2.length()) {
                if (verbose != null) { verbose.add("length mismatch: " + j1.length() + " != " + j2.length()); }
                return false;
            }
            for (int i = 0; i < j1.length(); i++) {
                if (!isJsonEqual(j1.opt(i), j2.opt(i), ignoreMissingNulls, verbose))
                    return false;
            }
            return true;
        }
        if (verbose != null) { verbose.add("value mismatch: " + o1 + " != " + o2); }
        return false;
    }

    private static Object reSerialize(Object src, boolean forceLegacyFormat) {
        try {
            SerializationContext ctx = new SerializationContext();
            ctx.forceLegacyFormat = forceLegacyFormat;
            JsonIO.ScopedReader reader = new JsonIO.ScopedReader(src);
            Object deserialized = ScriptableObjectSerializer.readScriptable(reader, ctx);
            JsonIO.ScopedWriter writer = new JsonIO.ScopedWriter();
            ScriptableObjectSerializer.writeScriptable(writer, deserialized, ctx);
            return writer.getResult();
        } catch (Throwable t) {
            ICLog.e("SAVER", "Serialization test failed", t);
            return null;
        }
    }

    private static boolean validateEqual(String testName, String o1name, Object o1, String o2name, Object o2) {
        List<String> verbose = new ArrayList<>();
        boolean passed = isJsonEqual(o1, o2, true, verbose);
        ICLog.d("SAVER", "SERIALIZATION " + testName + " PASSED: " + passed + " " + verbose);
        if (!passed) {
            ICLog.i("SAVER", o1name + ":");
            ICLog.i("SAVER", serializedToDebugString(o1, false));
            ICLog.i("SAVER", o2name + ":");
            ICLog.i("SAVER", serializedToDebugString(o2, false));
            DialogHelper.openFormattedDialog("NEW SAVER TEST FAILED!", "NEW SAVER TEST FAILED!");
        }
        return passed;
    }

    public static void runJsonSerializationTest(Object json) {
        //ICLog.i("SAVER", NativeJSON.stringify(Compiler.assureContextForCurrentThread(), ScriptableObjectHelper.getDefaultScope(), deserialized, null, "  ").toString());
        // original (legacy) -> serialized1 (legacy)
        Object serialized1 = reSerialize(json, true);

        // original (legacy) -> serialized2 (modern) -> serialized3(modern) -> serialized4 (legacy)
        Object serialized2 = reSerialize(json, false);
        Object serialized3 = reSerialize(serialized2, false);
        Object serialized4 = reSerialize(serialized3, true);

        validateEqual("TEST 1", "ORIGINAL", json, "SERIALIZED1", serialized1);
        if (validateEqual("TEST 2", "SERIALIZED2", serialized2, "SERIALIZED3", serialized3)) {
            if (validateEqual("TEST 3", "ORIGINAL", json, "SERIALIZED4", serialized4)) {
                ICLog.d("SAVER", "SERIALIZED3 " + serializedToDebugString(serialized3, false));
                ICLog.d("SAVER", "ORIGINAL " + serializedToDebugString(json, false));
            }
        }
    }
}
