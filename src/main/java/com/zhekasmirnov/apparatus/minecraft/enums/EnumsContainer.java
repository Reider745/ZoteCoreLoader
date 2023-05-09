package com.zhekasmirnov.apparatus.minecraft.enums;

import com.zhekasmirnov.apparatus.minecraft.version.MinecraftVersion;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class EnumsContainer {
    public static class Scope {
        private final Map<String, Object> map = new HashMap<>();
        private final Map<Object, String> inverseMap = new HashMap<>();

        public Set<String> getAllEnumNames() {
            return map.keySet();
        }

        public Object getEnum(String name) {
            return map.get(name);
        }

        public String getKeyForEnum(Object value) {
            return inverseMap.get(value);
        }

        public void put(String name, Object value) {
            map.put(name, value);
            inverseMap.put(value, name);
        }

        public void addEnumsFromJson(JSONObject scopeJson) {
            for (Iterator<String> it = scopeJson.keys(); it.hasNext(); ) {
                String name = it.next();
                Object value = scopeJson.opt(name);
                if (value != null) {
                    put(name, value);
                }
            }
        }
    }

    private final MinecraftVersion version;
    private final Map<String, Scope> scopeMap = new HashMap<>();

    public EnumsContainer(MinecraftVersion version) {
        this.version = version;
    }

    public MinecraftVersion getVersion() {
        return version;
    }

    public Set<String> getAllScopeNames() {
        return scopeMap.keySet();
    }

    public Scope getScope(String scopeName) {
        return scopeMap.get(scopeName);
    }

    public Scope getOrAddScope(String scopeName) {
        Scope scope = scopeMap.get(scopeName);
        if (scope == null) {
            scopeMap.put(scopeName, scope = new Scope());
        }
        return scope;
    }

    public Object getEnum(String scopeName, String name) {
        Scope scope = scopeMap.get(scopeName);
        if (scope != null) {
            return scope.getEnum(name);
        }
        return null;
    }

    public String getKeyForEnum(String scopeName, Object value) {
        Scope scope = scopeMap.get(scopeName);
        if (scope != null) {
            return scope.getKeyForEnum(value);
        }
        return null;
    }

    public void addEnumsFromJson(JSONObject enumsJson) {
        for (Iterator<String> it = enumsJson.keys(); it.hasNext(); ) {
            String scopeName = it.next();
            JSONObject scopeJson = enumsJson.optJSONObject(scopeName);
            if (scopeJson != null) {
                getOrAddScope(scopeName).addEnumsFromJson(scopeJson);
            }
        }
    }

}
