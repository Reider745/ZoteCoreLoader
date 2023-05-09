package com.zhekasmirnov.apparatus.mod;

import com.zhekasmirnov.apparatus.util.Java8BackComp;
import com.zhekasmirnov.horizon.util.FileUtils;
import com.zhekasmirnov.innercore.utils.FileTools;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ContentIdSource {
    private static volatile ContentIdSource globalSingleton = null;

    public static ContentIdSource getGlobal() {
        ContentIdSource localInstance = globalSingleton;
        if (localInstance == null) {
            synchronized (ContentIdSource.class) {
                localInstance = globalSingleton;
                if (localInstance == null) {
                    globalSingleton = localInstance = new ContentIdSource(new File(FileTools.DIR_WORK, "mods/global-id-source.json"));
                    localInstance.read();
                }
            }
        }
        return localInstance;
    }


    private final File file;
    private final Map<String, ContentIdScope> scopes = new HashMap<>();

    public ContentIdSource(File file) {
        this.file = file;
    }

    public ContentIdScope getScope(String scopeName) {
        return scopes.get(scopeName);
    }

    public ContentIdScope getOrCreateScope(String scopeName) {
        return Java8BackComp.computeIfAbsent(scopes, scopeName, key -> new ContentIdScope(scopeName));
    }

    public void read() {
        if (!file.isFile()) return;
        try {
            JSONObject json = FileUtils.readJSON(file);
            for (Iterator<String> it = json.keys(); it.hasNext(); ) {
                String key = it.next();
                getOrCreateScope(key).fromJson(json.optJSONObject(key));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void save() {
        JSONObject json = new JSONObject();
        try {
            for (Map.Entry<String, ContentIdScope> keyAndScope : scopes.entrySet()) {
                json.put(keyAndScope.getKey(), keyAndScope.getValue().toJson());
            }
        } catch (JSONException ignore) {
        }
        try {
            FileUtils.writeJSON(file, json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
