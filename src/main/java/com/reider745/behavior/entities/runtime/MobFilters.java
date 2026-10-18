package com.reider745.behavior.entities.runtime;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Objects;

/** Bedrock filter trees, including singleton tests and nested all/any/none groups. */
public final class MobFilters {
    private MobFilters() {}

    public static boolean test(Object filter, MobHost host) {
        if (filter == null || filter == JSONObject.NULL) return true;
        if (filter instanceof Boolean value) return value;
        if (filter instanceof JSONArray array) return group(array, host, "all_of");
        JSONObject json = (JSONObject) filter;
        for (String group : new String[]{"all_of", "any_of", "none_of"}) {
            JSONArray array = json.optJSONArray(group);
            if (array != null && !group(array, host, group)) return false;
        }
        if (!json.has("test")) return true;
        Object expected = json.opt("value");
        if (expected == null) expected = true;
        Object actual = host.filter(json.getString("test"), json.optString("subject", "self"),
                json.optString("domain", ""), expected);
        if (actual == null) return false; // An unknown test never succeeds, even with !=.
        if (actual instanceof Boolean && !(expected instanceof Boolean) && !(expected instanceof Number)) expected = true;
        return compare(actual, expected, json.optString("operator", json.optString("op", "equals")));
    }

    private static boolean group(JSONArray array, MobHost host, String type) {
        boolean any = false;
        for (int i = 0; i < array.length(); i++) {
            boolean value = test(array.get(i), host);
            if (type.equals("all_of") && !value) return false;
            any |= value;
        }
        return type.equals("all_of") || (type.equals("any_of") ? any : !any);
    }

    public static boolean compare(Object actual, Object expected, String operator) {
        int order;
        if ((actual instanceof Number || actual instanceof Boolean)
                && (expected instanceof Number || expected instanceof Boolean)) {
            double a = actual instanceof Boolean b ? b ? 1 : 0 : ((Number) actual).doubleValue();
            double b = expected instanceof Boolean v ? v ? 1 : 0 : ((Number) expected).doubleValue();
            order = Double.compare(a, b);
        } else {
            order = Objects.toString(actual).compareToIgnoreCase(Objects.toString(expected));
        }
        return switch (operator) {
            case "equals", "==", "=" -> order == 0;
            case "not", "not_equals", "!=", "<>" -> order != 0;
            case "<", "less" -> order < 0;
            case "<=", "less_than_or_equals" -> order <= 0;
            case ">", "greater" -> order > 0;
            case ">=", "greater_than_or_equals" -> order >= 0;
            default -> throw new IllegalArgumentException("Unknown filter operator: " + operator);
        };
    }
}
