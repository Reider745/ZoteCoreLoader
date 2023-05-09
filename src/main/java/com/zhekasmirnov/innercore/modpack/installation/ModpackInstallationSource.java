package com.zhekasmirnov.innercore.modpack.installation;

import com.zhekasmirnov.innercore.modpack.ModPackManifest;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;

public abstract class ModpackInstallationSource {
    public interface Entry {
        String getName();
        InputStream getInputStream() throws IOException;
    }

    public abstract String getManifestContent() throws IOException;

    public ModPackManifest getTempManifest() throws IOException, JSONException {
        ModPackManifest manifest = new ModPackManifest();
        manifest.loadJson(new JSONObject(getManifestContent()));
        return manifest;
    }

    public abstract int getEntryCount();
    public abstract Enumeration<Entry> entries();
}
