package com.reider745.behavior;

import com.reider745.behavior.entities.runtime.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import team.unnamed.mocha.runtime.value.Value;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class MobRuntimeTest {
    static final class Host implements MobHost {
        JSONObject components;
        int changes;
        double random = .25;
        boolean alive = true;
        final List<String> commands = new ArrayList<>();
        public Object query(String name, List<Value> args) { return name.equals("is_alive") && alive ? 1 : 0; }
        public Object filter(String test, String subject, String domain, Object value) {
            return switch (test) {
                case "is_alive" -> alive;
                case "is_family" -> value.equals("player");
                default -> null;
            };
        }
        public void componentsChanged(JSONObject components, boolean initial) { this.components = components; changes++; }
        public void action(String action, Object value) { commands.add(action + ":" + value); }
        public double random() { return random; }
    }

    static MobDefinition definition() {
        return new MobDefinition(new JSONObject("""
                {"description":{"identifier":"test:runtime","scripts":{"initialize":["v.counter = 1;"]}},
                 "components":{"minecraft:health":{"value":20,"max":20}},
                 "component_groups":{
                   "baby":{"minecraft:is_baby":{},"minecraft:health":{"value":5,"max":5}},
                   "timer":{"minecraft:timer":{"looping":false,"time":0.1,"time_down_event":{"event":"grow"}}},
                   "adult":{"minecraft:health":{"value":40,"max":40}}
                 },
                 "events":{
                   "minecraft:entity_spawned":{"add":{"component_groups":["baby","timer"]}},
                   "grow":{"remove":{"component_groups":["baby","timer"]},"add":{"component_groups":["adult"]}},
                   "remove_adult":{"remove":{"component_groups":["adult"]}},
                   "random":{"randomize":[
                       {"weight":1,"add":{"component_groups":["baby"]}},
                       {"weight":3,"add":{"component_groups":["adult"]}}]},
                   "cycle":{"trigger":"cycle"},
                   "chain":{"sequence":[{"add":{"component_groups":["baby"]}},{"trigger":"grow"}]}
                 }}
                """), null);
    }

    @Test void groupsTimersAndComponentsAreIsolatedBetweenActors() {
        MobDefinition definition = definition();
        Host firstHost = new Host(), secondHost = new Host();
        MobRuntime first = new MobRuntime(definition, firstHost), second = new MobRuntime(definition, secondHost);
        first.initialize(null); second.initialize(null);
        assertEquals(5, first.components().getJSONObject("minecraft:health").getInt("value"));
        first.tick(2);
        assertEquals(40, first.components().getJSONObject("minecraft:health").getInt("value"));
        assertEquals(5, second.components().getJSONObject("minecraft:health").getInt("value"));
        first.event("remove_adult");
        assertEquals(20, first.components().getJSONObject("minecraft:health").getInt("value"));
        assertFalse(first.groups().contains("baby"));
        assertEquals(20, definition.components(List.of()).getJSONObject("minecraft:health").getInt("value"));
    }

    @Test void restoredActorsDoNotReplaySpawnEventsAndTimersResume() {
        Host firstHost = new Host();
        MobRuntime first = new MobRuntime(definition(), firstHost);
        first.initialize(null); first.tick(1);
        first.molang().evaluate("v.counter = 7;");
        JSONObject saved = first.save();
        MobRuntime restored = new MobRuntime(definition(), new Host());
        restored.initialize(saved);
        assertEquals(7, restored.molang().evaluate("v.counter"));
        assertEquals(first.groups(), restored.groups());
        restored.tick(1);
        assertTrue(restored.groups().contains("adult"));
        assertFalse(first.groups().contains("adult"));
    }

    @Test void sequenceTriggerQueueRandomWeightsAndRecursionBudgetWork() {
        Host host = new Host();
        MobRuntime runtime = new MobRuntime(definition(), host);
        runtime.initialize(new JSONObject());
        runtime.event("chain");
        assertEquals(Set.of("adult"), runtime.groups());
        runtime.event("remove_adult");
        host.random = .1;
        runtime.event("random");
        assertEquals(Set.of("baby"), runtime.groups());
        assertThrows(IllegalArgumentException.class, () -> runtime.event("cycle"));
        runtime.event("grow");
        assertEquals(Set.of("adult"), runtime.groups());
    }

    @Test void filtersSupportSingletonAndNestedTreesAndFailClosed() {
        Host host = new Host();
        assertTrue(MobFilters.test(new JSONObject("""
                {"all_of":[{"test":"is_family","subject":"other","value":"player"},
                {"none_of":[{"test":"is_alive","value":false}]}]}
                """), host));
        assertFalse(MobFilters.test(new JSONObject("{\"test\":\"unknown\",\"operator\":\"!=\",\"value\":false}"), host));
        assertTrue(MobFilters.compare(5, 4, ">"));
        assertTrue(MobFilters.compare(true, 1, "equals"));
        host.alive = false;
        assertFalse(MobFilters.test(new JSONObject("{\"test\":\"is_alive\"}"), host));
    }
}
