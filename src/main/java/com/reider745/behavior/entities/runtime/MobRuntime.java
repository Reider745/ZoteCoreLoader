package com.reider745.behavior.entities.runtime;

import com.reider745.behavior.molang.MolangContext;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

/** Per-entity components, events, timers, scripts and persistent Molang variables. */
public final class MobRuntime {
    private final MobDefinition definition;
    private final MobHost host;
    private final LinkedHashSet<String> groups = new LinkedHashSet<>();
    private final ArrayDeque<String> events = new ArrayDeque<>();
    private final MolangContext molang;
    private JSONObject components;
    private boolean processing;
    private double timer = Double.NaN;
    private double age;
    private final List<String> queuedEvents = new ArrayList<>();
    private boolean dirty;
    private double animationTime;
    private final Map<String, Object> animationQueries = new HashMap<>();

    public MobRuntime(MobDefinition definition, MobHost host) {
        this.definition = definition;
        this.host = host;
        molang = new MolangContext((name, args) -> animationQueries.containsKey(name) ? animationQueries.get(name) : switch (name) {
            case "life_time" -> age;
            case "anim_time" -> animationTime;
            default -> host.query(name, args);
        });
        components = definition.components(groups);
    }

    public void initialize(JSONObject saved) {
        if (saved != null) {
            JSONArray active = saved.optJSONArray("groups");
            if (active != null) for (int i = 0; i < active.length(); i++) {
                String name = active.getString(i);
                if (definition.groups.contains(name)) groups.add(name);
            }
            age = saved.optDouble("age", 0);
            timer = saved.has("timer") ? saved.getDouble("timer") : Double.NaN;
            molang.restore(saved.optJSONObject("variables"));
        }
        components = definition.components(groups);
        host.componentsChanged(components(), true);
        if (saved == null) {
            scripts(definition.section("description").optJSONObject("scripts"), "initialize");
            event("minecraft:entity_spawned");
        }
    }

    public JSONObject components() { return new JSONObject(components.toString()); }
    public Set<String> groups() { return Collections.unmodifiableSet(groups); }
    public MolangContext molang() { return molang; }
    public MobDefinition definition() { return definition; }
    public void action(String action, Object value) { host.action(action, value); }
    public void animationTime(double value) { animationTime = value; }
    public void animationQuery(String name, Object value) { animationQueries.put(name, value); }

    public void event(String name) {
        if (name != null && !name.isEmpty()) events.add(name);
        if (processing || events.isEmpty()) return;
        processing = true;
        int budget = 256;
        try {
            JSONObject definitions = definition.section("events");
            while (!events.isEmpty()) {
                if (--budget < 0) throw new IllegalArgumentException("Recursive entity event chain: " + definition.identifier);
                String event = events.remove();
                JSONObject body = definitions.optJSONObject(event);
                if (body != null) execute(body, 0);
                commit();
            }
        } finally {
            processing = false;
            events.clear();
        }
    }

    public void execute(JSONObject body) {
        execute(body, 0);
        commit();
        event(null);
    }

    private void execute(JSONObject body, int depth) {
        if (depth > 32) throw new IllegalArgumentException("Entity event nesting exceeds 32");
        if (!MobFilters.test(body.opt("filters"), host)) return;
        JSONObject remove = body.optJSONObject("remove");
        if (remove != null) modify(remove.optJSONArray("component_groups"), false);
        JSONObject add = body.optJSONObject("add");
        if (add != null) modify(add.optJSONArray("component_groups"), true);
        JSONArray sequence = body.optJSONArray("sequence");
        if (sequence != null) for (int i = 0; i < sequence.length(); i++) execute(sequence.getJSONObject(i), depth + 1);
        JSONArray randomize = body.optJSONArray("randomize");
        if (randomize != null) {
            List<JSONObject> choices = new ArrayList<>();
            double total = 0;
            for (int i = 0; i < randomize.length(); i++) {
                JSONObject choice = randomize.getJSONObject(i);
                if (MobFilters.test(choice.opt("filters"), host)) {
                    total += Math.max(0, choice.optDouble("weight", 1));
                    choices.add(choice);
                }
            }
            double selected = host.random() * total;
            if (total > 0) for (JSONObject choice : choices) {
                selected -= Math.max(0, choice.optDouble("weight", 1));
                if (selected < 0) { execute(choice, depth + 1); break; }
            }
        }
        Object trigger = body.opt("trigger");
        if (trigger instanceof String name) events.add(name);
        else if (trigger instanceof JSONObject target) {
            if (target.optString("target", "self").equals("self")) events.add(target.getString("event"));
            else host.action("trigger", target);
        }
        if (body.has("run_command")) host.action("run_command", body.get("run_command"));
        if (body.has("play_sound")) host.action("play_sound", body.get("play_sound"));
        if (body.has("particle")) host.action("particle", body.get("particle"));
    }

    private void modify(JSONArray values, boolean add) {
        if (values == null) return;
        for (int i = 0; i < values.length(); i++) {
            String name = values.getString(i);
            if (!definition.groups.contains(name)) throw new IllegalArgumentException("Unknown component group: " + name);
            if (add) {
                groups.remove(name); // Re-adding refreshes precedence, as on Bedrock.
                groups.add(name);
                dirty = true;
            } else {
                dirty |= groups.remove(name);
            }
        }
    }

    private void commit() {
        if (!dirty) return;
        JSONObject previousTimer = components.optJSONObject("minecraft:timer");
        components = definition.components(groups);
        if (!Objects.equals(String.valueOf(previousTimer), String.valueOf(components.opt("minecraft:timer")))) {
            timer = Double.NaN;
        }
        dirty = false;
        host.componentsChanged(components(), false);
    }

    public void trigger(Object trigger) {
        if (trigger instanceof String value) event(value);
        else if (trigger instanceof JSONObject object && MobFilters.test(object.opt("filters"), host)) {
            if (object.optString("target", "self").equals("self")) event(object.optString("event"));
            else host.action("trigger", object);
        }
    }

    public void tick(int ticks) {
        double seconds = Math.max(0, ticks) / 20d;
        age += seconds;
        scripts(definition.section("description").optJSONObject("scripts"), "pre_animation");
        JSONObject timerComponent = components.optJSONObject("minecraft:timer");
        if (timerComponent != null) {
            if (Double.isNaN(timer)) timer = duration(timerComponent.opt("time"), 1);
            timer -= seconds;
            if (timer <= 0) {
                JSONObject activeTimer = timerComponent;
                timer = activeTimer.optBoolean("looping", true) ? Math.max(.05, duration(activeTimer.opt("time"), 1))
                        : Double.POSITIVE_INFINITY;
                trigger(activeTimer.opt("time_down_event"));
            }
        }
        JSONObject sensor = components.optJSONObject("minecraft:environment_sensor");
        if (sensor != null) {
            Object triggers = sensor.opt("triggers");
            if (triggers instanceof JSONArray array) {
                for (int i = 0; i < array.length(); i++) trigger(array.get(i));
            } else trigger(triggers);
        }
        for (String event : List.copyOf(queuedEvents)) event(event);
        queuedEvents.clear();
    }

    public double duration(Object value, double fallback) {
        if (value == null) return fallback;
        if (value instanceof JSONArray range) return range.getDouble(0) + host.random() * (range.getDouble(1) - range.getDouble(0));
        if (value instanceof JSONObject range) {
            double low = range.optDouble("range_min", range.optDouble("min", fallback));
            return low + host.random() * (range.optDouble("range_max", range.optDouble("max", low)) - low);
        }
        return molang.evaluate(value);
    }

    public void scripts(JSONObject scripts, String key) {
        if (scripts == null) return;
        Object expressions = scripts.opt(key);
        if (expressions instanceof JSONArray array) {
            for (int i = 0; i < array.length(); i++) molang.evaluate(array.get(i));
        } else if (expressions != null) molang.evaluate(expressions);
    }

    public JSONObject save() {
        JSONObject result = new JSONObject().put("groups", groups).put("variables", molang.save()).put("age", age);
        if (Double.isFinite(timer)) result.put("timer", timer);
        else if (timer == Double.POSITIVE_INFINITY) result.put("timer", Double.MAX_VALUE);
        return result;
    }
}
