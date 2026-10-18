package com.reider745.behavior.entities.runtime;

import org.json.JSONObject;
import team.unnamed.mocha.runtime.value.Value;

import java.util.List;

/** World-facing operations are kept separate from the deterministic event/state engine. */
public interface MobHost {
    Object query(String name, List<Value> arguments);
    Object filter(String test, String subject, String domain, Object value);
    void componentsChanged(JSONObject components, boolean initial);
    void action(String action, Object value);
    double random();
}
