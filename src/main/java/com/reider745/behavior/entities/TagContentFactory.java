package com.reider745.behavior.entities;

import com.reider745.behavior.entities.EntityContentFactory;
import org.json.JSONObject;

public abstract class TagContentFactory {
    public abstract String getNameTag();
    public abstract void loadJSON(EntityContentFactory factory, JSONObject json);
}
