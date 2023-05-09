package com.zhekasmirnov.innercore.api.runtime.saver.world;

import com.zhekasmirnov.innercore.api.mod.ScriptableObjectHelper;
import com.zhekasmirnov.innercore.api.runtime.Callback;
import org.json.JSONObject;
import org.mozilla.javascript.Scriptable;
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
        void readJson(Object json) throws Exception;
        Object saveAsJson() throws Exception;
    }

    public interface SavesErrorHandler {
        void handle(String name, Throwable error);
    }

    public interface MissingScopeHandler {
        void handle(String name, Object data);
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

    public void readAllScopes(JSONObject json, SavesErrorHandler errorHandler, MissingScopeHandler missingScopeHandler) {
        for (Map.Entry<String, SaverScope> entry : scopeMap.entrySet()) {
            String key = entry.getKey();
            Object data = json.opt(key);
            try {
                entry.getValue().readJson(data);
            } catch (Throwable err) {
                errorHandler.handle(key, err);
            }
        }

        for (Iterator<String> it = json.keys(); it.hasNext(); ) {
            String key = it.next();
            if (!scopeMap.containsKey(key)) {
                missingScopeHandler.handle(key, json.opt(key));
            }
        }
    }

    public void saveAllScopes(JSONObject json, SavesErrorHandler handler) {
        for (Map.Entry<String, SaverScope> entry : scopeMap.entrySet()) {
            try {
                json.put(entry.getKey(), entry.getValue().saveAsJson());
            } catch (Throwable err) {
                handler.handle(entry.getKey(), err);
            }
        }
    }
}
