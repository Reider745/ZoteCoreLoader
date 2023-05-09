package org.mineprogramming.horizon.innercore.model;

import org.json.JSONObject;

public class ModDependency {
    private final int id;
    private final String title;
    private final int version;

    public ModDependency(int id, String title, int version){
        this.id = id;
        this.title = title;
        this.version = version;
    }

    public ModDependency(JSONObject jsonObject) {
        this.id = jsonObject.optInt("id");
        this.title = jsonObject.optString("title");
        this.version = jsonObject.optInt("version");
	}

	public int getId(){
        return id;
    }

    public String getTitle(){
        return title;
    }

    public String getReadableFileName() {
        String name = title;
        name = name.replaceAll("\\s", "_");
        name = name.replaceAll("[^A-Za-z0-9_\\-]", "");
        return name;
    }

	public int getVersion() {
		return version;
	}
}