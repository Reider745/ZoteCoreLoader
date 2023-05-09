package org.mineprogramming.horizon.innercore.model;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public abstract class Item {
    private String title, descriptionShort, descriptionFull;
   // private Uri icon;

    private boolean installed;

    private int id;
    private int author;
    private String authorName;
    private boolean optimized;
    private boolean networkAdapted;

    private List<Tag> tags = new ArrayList<>();

    private String versionName;
    private int versionCode;

    // parced from https://icmods.mineprogramming.org/api/list
    public Item(JSONObject obj) {
        setTitle(obj.optString("title"));
        setDescriptionShort(obj.optString("description"));
        setId(obj.optInt("id"));
        setOptimized(obj.optInt("horizon_optimized") == 1);
        setNetworkAdapted(obj.optInt("multiplayer") == 1);

        // Uri icon = Uri.parse("https://icmods.mineprogramming.org/api/img/" + obj.optString("icon"));
        //setIcon(icon);
    }

    // parced from https://icmods.mineprogramming.org/api/description
    public void updateInfo(JSONObject obj) {
        setTitle(obj.optString("title"));
        setDescriptionFull(obj.optString("description_full"));
        setVersionCode(obj.optInt("version"));
        setVersionName(obj.optString("version_name"));
        setAuthor(obj.optInt("author"));
        setAuthorName(obj.optString("author_name"));
        setTags(obj.optJSONArray("tags"));
    }

    public Item() { }

    public String getDescriptionFull() {
        return descriptionFull == null? descriptionShort: descriptionFull;
    }

    public void setDescriptionFull(String descriptionFull) {
        this.descriptionFull = descriptionFull;
    }

    // Private setters
    protected void setTags(JSONArray tags) {
        if(tags == null){
            return;
        }
        this.tags.clear();
        for (int i = 0; i < tags.length(); i++) {
            String tag = tags.optString(i);
            this.tags.add(new Tag(tag));
        }
    }

    public List<Tag> getTags() {
        return tags;
    }

    public void setInstalled(boolean installed) {
        this.installed = installed;
    }
    

    /*public void setIcon(Uri image) {
        this.icon = image;
	}*/

    public int getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public int getAuthor() {
        return author;
    }

    public void setAuthor(int author) {
        this.author = author;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId(){
        return id;
    }

    public String getDescription() {
        return descriptionShort;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title){
        this.title = title;
        if(title.equals("")){
            this.title = "Untitled";
        }
    }

    public void setDescriptionShort(String description) {
        this.descriptionShort = description;
    }

    /*public Uri getIcon(){
        return icon;
    }*/

    public void setOptimized(boolean optimized){
        this.optimized = optimized;
    }

	public boolean isOptimized() {
		return optimized;
	}

    public void setNetworkAdapted(boolean networkAdapted) {
        this.networkAdapted = networkAdapted;
    }

    public boolean isNetworkAdapted() {
        return networkAdapted;
    }

    public boolean isInstalled() {
        return installed;
    }
}