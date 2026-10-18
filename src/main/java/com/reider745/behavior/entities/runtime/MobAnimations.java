package com.reider745.behavior.entities.runtime;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

/** Server controllers and animation timelines. Bone transforms remain client-side. */
public final class MobAnimations {
    private record Playback(String state, double time) {}
    private final MobRuntime runtime;
    private final Map<String, Playback> states = new LinkedHashMap<>();
    private final Map<String, Double> animations = new LinkedHashMap<>();
    private final Set<String> visited = new HashSet<>();

    public MobAnimations(MobRuntime runtime) { this.runtime = runtime; }

    public void tick(double seconds) {
        JSONObject description = runtime.definition().section("description");
        JSONObject aliases = description.optJSONObject("animations");
        JSONObject scripts = description.optJSONObject("scripts");
        if (aliases == null || scripts == null) return;
        visited.clear();
        playList(scripts.opt("animate"), aliases, seconds, 0);
        for (String name : List.copyOf(states.keySet())) if (!visited.contains(name)) {
            JSONObject controller = MobAssets.get(runtime.definition().source(), name, true);
            if (controller != null) actions(controller.getJSONObject("states").getJSONObject(states.get(name).state).opt("on_exit"));
            states.remove(name);
        }
        animations.keySet().removeIf(name -> !visited.contains(name));
    }

    private void playList(Object entries, JSONObject aliases, double seconds, int depth) {
        if (depth > 32) throw new IllegalArgumentException("Recursive animation controller");
        JSONArray list = entries instanceof JSONArray array ? array : entries == null ? new JSONArray() : new JSONArray().put(entries);
        for (int i = 0; i < list.length(); i++) {
            Object entry = list.get(i);
            if (entry instanceof String name) play(aliases.optString(name, name), aliases, seconds, depth + 1);
            else if (entry instanceof JSONObject weights) for (String name : weights.keySet()) {
                if (runtime.molang().evaluate(weights.get(name)) > 0) play(aliases.optString(name, name), aliases, seconds, depth + 1);
            }
        }
    }

    private void play(String name, JSONObject aliases, double seconds, int depth) {
        if (!visited.add(name)) return;
        JSONObject controller = MobAssets.get(runtime.definition().source(), name, true);
        if (controller != null) {
            JSONObject definitions = controller.getJSONObject("states");
            Playback playback = states.get(name);
            if (playback == null) {
                playback = new Playback(controller.optString("initial_state", "default"), 0);
                actions(definitions.getJSONObject(playback.state).opt("on_entry"));
            }
            JSONObject state = definitions.getJSONObject(playback.state);
            runtime.animationTime(playback.time);
            runtime.animationQuery("state_time", playback.time);
            animationCompletion(state.opt("animations"), aliases);
            JSONObject variables = state.optJSONObject("variables");
            if (variables != null) for (String variable : variables.keySet()) {
                Object value = variables.get(variable);
                double result;
                if (value instanceof JSONObject remap) {
                    double input = runtime.molang().evaluate(remap.opt("input"));
                    JSONObject curve = remap.optJSONObject("remap_curve");
                    result = curve == null || curve.isEmpty() ? input : remap(input, curve);
                } else result = runtime.molang().evaluate(value);
                runtime.molang().variable(variable.replaceFirst("^(variable|v)\\.", ""), result);
            }
            JSONArray transitions = state.optJSONArray("transitions");
            if (transitions != null) for (int i = 0; i < transitions.length(); i++) {
                JSONObject transition = transitions.getJSONObject(i);
                boolean changed = false;
                for (String destination : transition.keySet()) if (runtime.molang().evaluate(transition.get(destination)) != 0) {
                    if (!definitions.has(destination)) throw new IllegalArgumentException("Unknown animation state " + destination);
                    actions(state.opt("on_exit"));
                    playback = new Playback(destination, 0);
                    state = definitions.getJSONObject(destination);
                    actions(state.opt("on_entry"));
                    changed = true;
                    break;
                }
                if (changed) break;
            }
            states.put(name, new Playback(playback.state, playback.time + seconds));
            runtime.animationTime(playback.time);
            playList(state.opt("animations"), aliases, seconds, depth + 1);
            return;
        }
        JSONObject animation = MobAssets.get(runtime.definition().source(), name, false);
        if (animation == null) return; // References to resource-pack animations are evaluated by the client.
        double previous = animations.getOrDefault(name, -Double.MIN_VALUE);
        double current = Math.max(0, previous) + seconds;
        double length = animation.optDouble("animation_length", 0);
        JSONObject timeline = animation.optJSONObject("timeline");
        if (timeline != null) timeline(timeline, previous, length > 0 ? Math.min(current, length) : current);
        if (length > 0 && current >= length && animation.optBoolean("loop", false)) {
            current %= length;
            if (timeline != null) timeline(timeline, -Double.MIN_VALUE, current);
        } else if (length > 0) current = Math.min(current, length);
        animations.put(name, current);
    }

    private void timeline(JSONObject timeline, double previous, double current) {
        timeline.keySet().stream().sorted(Comparator.comparingDouble(Double::parseDouble)).forEach(timestamp -> {
            double time = Double.parseDouble(timestamp);
            if (time > previous && time <= current) actions(timeline.get(timestamp));
        });
    }

    private void animationCompletion(Object entries, JSONObject aliases) {
        JSONArray list = entries instanceof JSONArray array ? array : entries == null ? new JSONArray() : new JSONArray().put(entries);
        boolean all = true, any = false;
        for (int i = 0; i < list.length(); i++) {
            Object entry = list.get(i);
            List<String> active = new ArrayList<>();
            if (entry instanceof String name) active.add(name);
            else if (entry instanceof JSONObject conditions) for (String name : conditions.keySet()) {
                if (runtime.molang().evaluate(conditions.get(name)) > 0) active.add(name);
            }
            for (String alias : active) {
                String name = aliases.optString(alias, alias);
                JSONObject clip = MobAssets.get(runtime.definition().source(), name, false);
                boolean done = clip != null && !clip.optBoolean("loop", false)
                        && animations.getOrDefault(name, 0d) >= clip.optDouble("animation_length", 0);
                all &= done; any |= done;
            }
        }
        runtime.animationQuery("all_animations_finished", all);
        runtime.animationQuery("any_animation_finished", any);
    }

    private static double remap(double input, JSONObject curve) {
        List<String> knots = new ArrayList<>(curve.keySet());
        knots.sort(Comparator.comparingDouble(Double::parseDouble));
        String previous = knots.get(0);
        for (String next : knots) {
            double x = Double.parseDouble(next), start = Double.parseDouble(previous);
            if (input <= x) {
                if (x == start) return curve.getDouble(next);
                double factor = Math.max(0, Math.min(1, (input - start) / (x - start)));
                return curve.getDouble(previous) + factor * (curve.getDouble(next) - curve.getDouble(previous));
            }
            previous = next;
        }
        return curve.getDouble(previous);
    }

    private void actions(Object entries) {
        JSONArray list = entries instanceof JSONArray array ? array : entries == null ? new JSONArray() : new JSONArray().put(entries);
        for (int i = 0; i < list.length(); i++) {
            String action = list.getString(i).trim();
            if (action.startsWith("/")) runtime.action("run_command", action.substring(1));
            else if (action.startsWith("@s ")) runtime.event(action.substring(3).trim());
            else runtime.molang().evaluate(action);
        }
    }

    public JSONObject save() {
        JSONObject controllers = new JSONObject();
        states.forEach((name, state) -> controllers.put(name, new JSONObject().put("state", state.state).put("time", state.time)));
        return new JSONObject().put("controllers", controllers).put("animations", animations);
    }

    public void restore(JSONObject saved) {
        if (saved == null) return;
        JSONObject controllers = saved.optJSONObject("controllers");
        if (controllers != null) for (String name : controllers.keySet()) {
            JSONObject state = controllers.getJSONObject(name);
            states.put(name, new Playback(state.getString("state"), state.optDouble("time", 0)));
        }
        JSONObject activeAnimations = saved.optJSONObject("animations");
        if (activeAnimations != null) for (String name : activeAnimations.keySet()) animations.put(name, activeAnimations.getDouble(name));
    }
}
