package com.reider745.behavior.entities;

import org.json.JSONObject;

import java.util.ArrayList;

public abstract class EntitiesVersion {
    public abstract ArrayList<TagContentFactory> getListTags();
    public abstract EntityContentFactory load(JSONObject json);
}
