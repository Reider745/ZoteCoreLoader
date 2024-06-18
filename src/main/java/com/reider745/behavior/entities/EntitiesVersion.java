package com.reider745.behavior.entities;

import org.json.JSONObject;

public abstract class EntitiesVersion {
    public abstract EntityContentFactory load(JSONObject json);
}
