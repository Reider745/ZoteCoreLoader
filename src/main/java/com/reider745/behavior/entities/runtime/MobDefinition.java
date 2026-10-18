package com.reider745.behavior.entities.runtime;

import org.json.JSONObject;

import java.util.*;

/** Immutable JSON blueprint; instances only receive defensive copies of component state. */
public final class MobDefinition {
    private final String source;
    private final JSONObject json;
    public final String identifier;
    public final Set<String> groups;

    public MobDefinition(JSONObject json, String source) {
        this.json = new JSONObject(json.toString());
        this.source = source;
        identifier = this.json.getJSONObject("description").getString("identifier");
        groups = Collections.unmodifiableSet(new LinkedHashSet<>(section("component_groups").keySet()));
        if (!identifier.matches("[a-z0-9_.-]+:[a-z0-9_./-]+")) {
            throw new IllegalArgumentException("Invalid entity identifier: " + identifier);
        }
        JSONObject scripts = section("description").optJSONObject("scripts");
        if (scripts != null) for (String stage : List.of("initialize", "pre_animation")) {
            Object value = scripts.opt(stage);
            org.json.JSONArray expressions = value instanceof org.json.JSONArray array ? array
                    : value == null ? new org.json.JSONArray() : new org.json.JSONArray().put(value);
            for (int i = 0; i < expressions.length(); i++) {
                com.reider745.behavior.molang.MolangContext.compile(expressions.getString(i));
            }
        }
    }

    public JSONObject section(String name) {
        JSONObject value = json.optJSONObject(name);
        return value == null ? new JSONObject() : new JSONObject(value.toString());
    }

    public JSONObject components(Collection<String> active) {
        JSONObject result = section("components");
        JSONObject groupDefinitions = section("component_groups");
        for (String name : active) {
            JSONObject group = groupDefinitions.getJSONObject(name);
            for (String key : group.keySet()) result.put(key, group.get(key));
        }
        return result;
    }

    public String source() { return source; }
}
