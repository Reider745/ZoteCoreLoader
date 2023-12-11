package com.zhekasmirnov.innercore.modpack;

import java.io.File;
import java.io.IOException;

import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.utils.FileTools;

import org.json.JSONException;
import org.json.JSONObject;

public class ModPackSelector {
    private final static String PREFERENCES_PATH = FileTools.DIR_WORK + "preferences.json";
    private final static String PACK_SELECTED = "pack_selected";

    public static void setSelected(ModPack pack) {
        ModPackContext packContext = ModPackContext.getInstance();

        JSONObject preferences = readPreferences();
        try {
            preferences.put(PACK_SELECTED, pack.getRootDirectory().getAbsolutePath());
            FileTools.writeJSON(PREFERENCES_PATH, preferences);
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }

        packContext.setCurrentModPack(pack);
    }

    public static void restoreSelected() {
        ModPackContext packContext = ModPackContext.getInstance();
        ModPackStorage packStorage = packContext.getStorage();
        packStorage.rebuildModPackList();

        JSONObject preferences = readPreferences();

        String root = preferences.optString(PACK_SELECTED, null);
        if (root != null) {
            ModPack pack = getPackByRoot(root);
            if (pack != null) {
                packContext.setCurrentModPack(pack);
            }
        }
        if (packContext.getCurrentModPack() == null) {
            packContext.setCurrentModPack(packStorage.getDefaultModPack());
        }
        ICLog.d("ModPackSelector", "selected modpack: " + packContext.getCurrentModPack().getRootDirectory());
    }

    private static ModPack getPackByRoot(String root) {
        ModPackContext packContext = ModPackContext.getInstance();
        ModPackStorage packStorage = packContext.getStorage();
        for (ModPack pack : packStorage.getAllModPacks()) {
            if (pack.getRootDirectory().equals(new File(root))) {
                return pack;
            }
        }
        return null;
    }

    private static JSONObject readPreferences() {
        JSONObject preferences;
        try {
            preferences = FileTools.readJSON(PREFERENCES_PATH);
        } catch (IOException | JSONException e) {
            preferences = new JSONObject();
        }

        return preferences;
    }
}
