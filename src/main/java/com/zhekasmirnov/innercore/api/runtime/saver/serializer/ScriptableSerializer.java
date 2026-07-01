package com.zhekasmirnov.innercore.api.runtime.saver.serializer;

import org.innercore.internal.serialization.JsonIO;
import org.innercore.icstd.serialization.ScriptableObjectSerializer;
import org.innercore.icstd.serialization.SerializationContext;
import org.json.JSONException;
import org.json.JSONObject;

public class ScriptableSerializer {
    public static String scriptableToJsonString(Object object, SerializationContext ctx) {
        JsonIO.ScopedWriter io = new JsonIO.ScopedWriter();
        ScriptableObjectSerializer.writeScriptable(io, object, ctx);
        io.forceEndScope(ctx);
        return jsonToString(io.getResult());
    }

    public static Object scriptableFromJsonString(String jsonString, SerializationContext ctx, boolean canFail) {
        Object json;
        try {
            json = stringToJson(jsonString);
        } catch (JSONException e) {
            if (!canFail) throw new RuntimeException(e);
            ctx.addError(e, jsonString, "failed to deserialize json");
            return null;
        }
        JsonIO.ScopedReader io = new JsonIO.ScopedReader(json);
        Object result = ScriptableObjectSerializer.readScriptable(io, ctx);
        io.forceEndScope(ctx);
        return result;
    }



    private static String jsonToString(Object json) {
        return "" + json;
    }

    private static Object stringToJson(String str) throws JSONException {
        if (str == null) {
            return null;
        }
        return new JSONObject("{\"a\": " + str + "}").get("a");
    }
}
