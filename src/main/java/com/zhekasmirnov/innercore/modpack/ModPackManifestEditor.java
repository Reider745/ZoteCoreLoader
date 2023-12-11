package com.zhekasmirnov.innercore.modpack;

import java.io.File;
import java.io.IOException;

import com.zhekasmirnov.innercore.utils.FileTools;

import org.json.JSONException;
import org.json.JSONObject;

public class ModPackManifestEditor {
    private final ModPackManifest manifest;
    private final File file;
    private final JSONObject json;

    ModPackManifestEditor(ModPackManifest manifest, File file) throws IOException, JSONException {
        this.manifest = manifest;
        this.file = file;
        if (file == null) {
            throw new IllegalStateException("Manifest wasn't loaded from a file");
        }

        if (file.isFile()) {
            json = FileTools.readJSON(file);
        } else {
            json = new JSONObject();
        }
    }

    public ModPackManifestEditor addIfMissing(String key, Object value) throws JSONException {
        String current = json.optString(key, null);
        if (current == null || current.isEmpty()) {
            json.put(key, value);
        }
        return this;
    }

    public ModPackManifestEditor put(String key, Object value) throws JSONException {
        json.put(key, value);
        return this;
    }

    public void commit() throws IOException, JSONException {
        FileTools.writeJSON(file, json);
        manifest.loadFile(file);
    }
}
