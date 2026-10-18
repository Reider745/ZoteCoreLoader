package com.reider745.behavior;

import com.reider745.behavior.entities.runtime.*;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MobAnimationsTest {

    @Test void animationCompletionCanDriveControllerTransitionsAndTimelinesLoop() {
        MobAssets.register(null, "animation.test.short", new JSONObject("""
                {"animation_length":0.1,"timeline":{"0.0":["v.frames = v.frames + 1;"]}}
                """), false);
        MobAssets.register(null, "controller.animation.completion", new JSONObject("""
                {"states":{"default":{"animations":["short"],"transitions":[{"done":"q.all_animations_finished"}]},
                "done":{"on_entry":["v.done = 1;"]}}}
                """), true);
        MobDefinition definition = new MobDefinition(new JSONObject("""
                {"description":{"identifier":"test:completion","animations":{
                  "main":"controller.animation.completion","short":"animation.test.short"},
                  "scripts":{"animate":["main"]}},"components":{}}
                """), null);
        MobRuntime runtime = new MobRuntime(definition, new MobRuntimeTest.Host());
        runtime.initialize(null);
        MobAnimations animation = new MobAnimations(runtime);
        animation.tick(.05);
        assertEquals(0, runtime.molang().evaluate("v.done"));
        animation.tick(.05);
        animation.tick(.05);
        assertEquals(1, runtime.molang().evaluate("v.done"));
        assertEquals(1, runtime.molang().evaluate("v.frames"));
    }
    @Test void controllerTransitionsRunEntryAndExitAndPersistState() {
        MobAssets.register(null, "controller.animation.test", new JSONObject("""
                {"initial_state":"default","states":{
                 "default":{"on_entry":["v.entered = 1;"],"on_exit":["v.exited = 1;"],
                            "transitions":[{"active":"q.is_alive"}]},
                 "active":{"on_entry":["v.active = 1;"]}}}
                """), true);
        MobDefinition definition = new MobDefinition(new JSONObject("""
                {"description":{"identifier":"test:animated","animations":{"main":"controller.animation.test"},
                  "scripts":{"animate":["main"]}},"components":{}}
                """), null);
        MobRuntimeTest.Host host = new MobRuntimeTest.Host();
        MobRuntime runtime = new MobRuntime(definition, host);
        runtime.initialize(null);
        MobAnimations animations = new MobAnimations(runtime);
        animations.tick(.05);
        assertEquals(1, runtime.molang().evaluate("v.entered"));
        assertEquals(1, runtime.molang().evaluate("v.exited"));
        assertEquals(1, runtime.molang().evaluate("v.active"));
        MobAnimations restored = new MobAnimations(runtime);
        restored.restore(animations.save());
        runtime.molang().variable("active", 0);
        restored.tick(.05);
        assertEquals(0, runtime.molang().evaluate("v.active"));
        assertEquals("active", restored.save().getJSONObject("controllers")
                .getJSONObject("controller.animation.test").getString("state"));
    }
}
