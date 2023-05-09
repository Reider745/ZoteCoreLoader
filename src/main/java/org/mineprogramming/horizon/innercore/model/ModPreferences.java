package org.mineprogramming.horizon.innercore.model;

import java.io.File;
import java.io.IOException;

import com.zhekasmirnov.innercore.utils.FileTools;

import org.json.JSONException;
import org.json.JSONObject;

public class ModPreferences {
    private File file;
    private JSONObject json;

    public ModPreferences(File root) {
        this.file = new File(root, "preferences.json");
        try {
            json = FileTools.readJSON(file.getAbsolutePath());
        } catch (IOException | JSONException e) {
            json = new JSONObject();
        }
    }

    public void setIcmodsData(int id, int version){
        try {
            json.put("icmods_id", id);
            json.put("icmods_version", version);
            FileTools.writeJSON(file.getAbsolutePath(), json);
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
    }


    public int getIcmodsId(){
        return json.optInt("icmods_id", 0);
    }
    

    public int getIcmodsVersion(){
        return json.optInt("icmods_version", 0);
    }
}
