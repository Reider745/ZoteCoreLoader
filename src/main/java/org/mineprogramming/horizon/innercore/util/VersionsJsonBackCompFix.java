package org.mineprogramming.horizon.innercore.util;

import com.zhekasmirnov.horizon.util.FileUtils;
import com.zhekasmirnov.innercore.api.log.ICLog;
import org.json.JSONException;
import org.json.JSONObject;
import org.mineprogramming.horizon.innercore.model.ModPreferences;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

public class VersionsJsonBackCompFix {
    private final File defaultPackRoot;

    public VersionsJsonBackCompFix(File defaultPackRoot) {
        this.defaultPackRoot = defaultPackRoot;
    }

    public void runFixIfRequired() {
        File versionsFile = new File(defaultPackRoot, "versions.json");
        ICLog.d("DEBUG", "runFixIfRequired " + versionsFile.getAbsolutePath());

        JSONObject versions;
        try {
            versions = FileUtils.readJSON(versionsFile);
            if (versions == null) {
                return;
            }
        } catch (Exception exception) {
            return;
        }

        for (Iterator<String> it = versions.keys(); it.hasNext(); ) {
            String key = it.next();
            try {
                int id = Integer.parseInt(key);
                JSONObject data = versions.optJSONObject(key);
                if (data != null) {
                    String directoryName = data.optString("directory");
                    int version = data.optInt("version");
                    if (directoryName != null && directoryName.length() > 0) {
                        File directory = new File(defaultPackRoot, directoryName);
                        if (directory.isDirectory()) {
                            ICLog.d("Versions-Json-Fix", "parsed mod external data from versions.json: dir=" + directoryName + ", id=" + id + ", version=" + version);
                            ModPreferences preferences = new ModPreferences(directory);
                            preferences.setIcmodsData(id, version);
                        } else {
                            ICLog.d("Versions-Json-Fix", "missing mod directory in versions.json: " + directoryName);
                        }
                    }
                }
            } catch (NumberFormatException ignore) { }
        }

        versionsFile.delete();
    }


}
