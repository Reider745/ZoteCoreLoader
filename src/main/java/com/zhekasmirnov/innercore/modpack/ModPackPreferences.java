package com.zhekasmirnov.innercore.modpack;

import com.zhekasmirnov.innercore.utils.FileTools;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

public class ModPackPreferences {
    private final ModPack modPack;
    private final File file;

    public ModPackPreferences(ModPack modPack, String fileName) {
        this.modPack = modPack;
        this.file = new File(modPack.getRootDirectory(), fileName);
    }

    public ModPack getModPack() {
        return modPack;
    }

    public File getFile() {
        return file;
    }

    private JSONObject json;

    public ModPackPreferences reload() {
        try {
            json = FileTools.readJSON(file);
        } catch (IOException | JSONException exception) {
            if (json == null) {
                json = new JSONObject();
            }
        }
        return this;
    }

    public ModPackPreferences save() {
        reloadIfRequired();
        try {
            FileTools.writeJSON(file, json);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        return this;
    }

    private void reloadIfRequired() {
        if (json == null) {
            reload();
        }
    }

    public String getString(String key, String fallback) {
        reloadIfRequired();
        return json.optString(key, fallback);
    }

    public int getInt(String key, int fallback) {
        reloadIfRequired();
        return json.optInt(key, fallback);
    }

    public long getLong(String key, long fallback) {
        reloadIfRequired();
        return json.optLong(key, fallback);
    }

    public double getDouble(String key, double fallback) {
        reloadIfRequired();
        return json.optDouble(key, fallback);
    }

    public boolean getBoolean(String key, boolean fallback) {
        reloadIfRequired();
        return json.optBoolean(key, fallback);
    }

    public ModPackPreferences setString(String key, String value) {
        reloadIfRequired();
        try {
            json.put(key, value);
        } catch (JSONException ignore) {
        }
        return this;
    }

    public ModPackPreferences setInt(String key, int value) {
        reloadIfRequired();
        try {
            json.put(key, value);
        } catch (JSONException ignore) {
        }
        return this;
    }

    public ModPackPreferences setLong(String key, long value) {
        reloadIfRequired();
        try {
            json.put(key, value);
        } catch (JSONException ignore) {
        }
        return this;
    }

    public ModPackPreferences setDouble(String key, double value) {
        reloadIfRequired();
        try {
            json.put(key, value);
        } catch (JSONException ignore) {
        }
        return this;
    }

    public ModPackPreferences setBoolean(String key, boolean value) {
        reloadIfRequired();
        try {
            json.put(key, value);
        } catch (JSONException ignore) {
        }
        return this;
    }

}
