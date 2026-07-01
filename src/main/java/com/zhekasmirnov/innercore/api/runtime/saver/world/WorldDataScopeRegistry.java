package com.zhekasmirnov.innercore.api.runtime.saver.world;

import com.zhekasmirnov.innercore.api.mod.ScriptableObjectHelper;
import com.zhekasmirnov.innercore.api.runtime.Callback;
import org.innercore.icstd.serialization.IScopedReader;
import org.innercore.icstd.serialization.IScopedWriter;
import org.innercore.internal.serialization.JsonIO;
import org.innercore.icstd.serialization.SerializationContext;
import org.json.JSONObject;
import org.mozilla.javascript.ScriptableObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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


    private final Map<String, SaverScope> scopeMap = new HashMap<>();

    public void addScope(String name, SaverScope scope) {
        if (scope == null) {
            return;
        }
        while (scopeMap.containsKey(name)) {
            name += name.hashCode() & 0xFF;
        }
        scopeMap.put(name, scope);
    }

    public void readAllScopes(JSONObject json, SerializationContext ctx, Map<String, Object> outMissingScopes) {
        for (Map.Entry<String, SaverScope> entry : scopeMap.entrySet()) {
            String key = entry.getKey();
            Object data = json.opt(key);
            JsonIO.ScopedReader reader = null;
            try {
                reader = new JsonIO.ScopedReader(data);
                entry.getValue().read(reader, ctx);
            } catch (Throwable err) {
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

    public void saveAllScopes(JSONObject json, SerializationContext ctx) {
        for (Map.Entry<String, SaverScope> entry : scopeMap.entrySet()) {
            try {
                JsonIO.ScopedWriter writer = new JsonIO.ScopedWriter();
                entry.getValue().write(writer, ctx);
                writer.forceEndScope(ctx);
                json.put(entry.getKey(), writer.getResult());
            } catch (Throwable err) {
                ctx.addError(err, null, "global saves scope failed to read: " + entry.getKey());
            }
        }
    }
}
