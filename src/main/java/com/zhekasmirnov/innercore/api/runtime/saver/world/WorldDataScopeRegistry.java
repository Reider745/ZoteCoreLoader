package com.zhekasmirnov.innercore.api.runtime.saver.world;

import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.api.mod.ScriptableObjectHelper;
import com.zhekasmirnov.innercore.api.runtime.Callback;
import org.innercore.icstd.serialization.IScopedReader;
import org.innercore.icstd.serialization.IScopedWriter;
import org.innercore.internal.serialization.JsonIO;
import org.innercore.icstd.serialization.SerializationContext;
import org.json.JSONException;
import org.json.JSONObject;
import org.mozilla.javascript.ScriptableObject;

import java.util.*;

public class WorldDataScopeRegistry {
    private static final WorldDataScopeRegistry instance = new WorldDataScopeRegistry();

    static {
        instance.addScope("_legacy_global", new ScriptableSaverScope() {
            @Override
            public void read(Object object) {
                if (object == null) {
                    object = ScriptableObjectHelper.createEmpty();
                }
                Callback.invokeAPICallback("ReadSaves", object);
            }

            @Override
            public ScriptableObject save() {
                ScriptableObject scope = ScriptableObjectHelper.createEmpty();
                Callback.invokeAPICallback("WriteSaves", scope);
                return scope;
            }
        });
    }

    public static WorldDataScopeRegistry getInstance() {
        return instance;
    }


    public interface SaverScope {
        void read(IScopedReader io, SerializationContext ctx);
        void write(IScopedWriter io, SerializationContext ctx);
    }

    // per-world backup of raw scope data, used to keep the last known copy of a scope in the saves
    // file when the scope fails to read or write, instead of dropping or overwriting it with defaults
    public static class RetainedScopeData {
        private final Map<String, Object> lastKnownData = new HashMap<>();
        private final Set<String> failedToRead = new HashSet<>();

        public void clear() {
            lastKnownData.clear();
            failedToRead.clear();
        }
    }



    private final Map<String, SaverScope> scopeMap = new HashMap<>();

    public void addScope(String name, SaverScope scope) {
        if (scope == null) {
            return;
        }
        while (scopeMap.containsKey(name)) {
            String renamed = name + (name.hashCode() & 0xFF);
            ICLog.e("SAVER", "saves scope name collision: '" + name + "' is already registered, new scope will be registered as '" + renamed + "' and its data will depend on registration order");
            name = renamed;
        }
        scopeMap.put(name, scope);
    }

    public void readAllScopes(JSONObject json, SerializationContext ctx, RetainedScopeData retained, Map<String, Object> outMissingScopes) {
        for (Map.Entry<String, SaverScope> entry : scopeMap.entrySet()) {
            String key = entry.getKey();
            Object data = json.opt(key);
            if (data != null) {
                retained.lastKnownData.put(key, data);
            }
            JsonIO.ScopedReader reader = null;
            try {
                reader = new JsonIO.ScopedReader(data);
                entry.getValue().read(reader, ctx);
            } catch (Throwable err) {
                // a scope is marked as failed only when it had data to lose - a reader failing on
                // empty data must not suppress saving fresh data
                if (data != null) {
                    retained.failedToRead.add(key);
                }
                ctx.addError(err, data, "global saves scope failed to read: " + key);
            }
            if (reader != null) {
                reader.forceEndScope(ctx);
            }
        }

        for (Iterator<String> it = json.keys(); it.hasNext(); ) {
            String key = it.next();
            if (!scopeMap.containsKey(key)) {
                outMissingScopes.put(key, json.opt(key));
            }
        }
    }

    private static void putJsonSafe(JSONObject json, String key, Object data) {
        try {
            json.put(key, data);
        } catch (JSONException ignore) {
        }
    }

    public void saveAllScopes(JSONObject json, SerializationContext ctx, RetainedScopeData retained) {
        for (Map.Entry<String, SaverScope> entry : scopeMap.entrySet()) {
            String key = entry.getKey();
            if (retained.failedToRead.contains(key)) {
                // the scope never received its saved data, writing it would replace the data with
                // defaults - keep the last known copy instead
                Object lastKnown = retained.lastKnownData.get(key);
                if (lastKnown != null) {
                    ICLog.i("SAVER", "scope " + key + " failed to read on world load, keeping its previous data");
                    putJsonSafe(json, key, lastKnown);
                }
                continue;
            }
            try {
                JsonIO.ScopedWriter writer = new JsonIO.ScopedWriter();
                entry.getValue().write(writer, ctx);
                writer.forceEndScope(ctx);
                json.put(entry.getKey(), writer.getResult());
                Object result = writer.getResult();
                putJsonSafe(json, key, result);
                if (result != null) {
                    retained.lastKnownData.put(key, result);
                } else {
                    retained.lastKnownData.remove(key);
                }
            } catch (Throwable err) {
                ctx.addError(err, null, "global saves scope failed to read: " + entry.getKey());
                ctx.addError(err, null, "global saves scope failed to write: " + key);
                // don't lose the scope - write the data from the last successful read or write
                Object lastKnown = retained.lastKnownData.get(key);
                if (lastKnown != null) {
                    putJsonSafe(json, key, lastKnown);
                }
            }
        }
    }

}
