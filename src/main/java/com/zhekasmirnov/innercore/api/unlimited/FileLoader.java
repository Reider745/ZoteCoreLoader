package com.zhekasmirnov.innercore.api.unlimited;

/**
 * Created by zheka on 08.08.2017.
 */

import com.zhekasmirnov.innercore.utils.FileTools;
import org.json.JSONObject;

import java.io.File;

public class FileLoader {
    private File file;

    private JSONObject data;
    private JSONObject uids;

    public FileLoader(File file) {
        this.file = file;

        try {
            data = FileTools.readJSON(file.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
            data = new JSONObject();
        }

        uids = data.optJSONObject("id");

        if (uids != null) {
            IDRegistry.fromJson(uids);
        }
    }

    public void save() {
        try {
            data.put("id", IDRegistry.toJson());

            FileTools.writeJSON(file.getAbsolutePath(), data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
