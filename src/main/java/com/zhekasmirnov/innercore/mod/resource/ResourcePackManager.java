package com.zhekasmirnov.innercore.mod.resource;

import com.zhekasmirnov.innercore.ui.LoadingUI;
import com.zhekasmirnov.innercore.utils.FileTools;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.Iterator;

/**
 * Created by zheka on 03.07.2017.
 */

public class ResourcePackManager {
    public static final String LOGGER_TAG = "INNERCORE-RESOURCES";
    public static ResourcePackManager instance;

    static {
        FileTools.assureDir(FileTools.DIR_MINECRAFT + "minecraftpe");
        FileTools.assureDir(FileTools.DIR_MINECRAFT + "resource_packs");

        instance = new ResourcePackManager();
    }

    public ResourceStorage resourceStorage;
    public String resourcePackDefinition, resourcePackList;

    public ResourcePackManager() {
        resourcePackDefinition = FileTools.DIR_MINECRAFT + "minecraftpe/valid_known_packs.json";
        resourcePackList = FileTools.DIR_MINECRAFT + "minecraftpe/global_resource_packs.json";

        resourceStorage = new ResourceStorage();
    }

    public void initializeResources() {
        try {
            resourceStorage.build();
            LoadingUI.setProgress(0.05f);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getBlockTextureName(String name, int id) {
        if (instance.resourceStorage != null) {
            String result = instance.resourceStorage.blockTextureDescriptor.getTextureName(name, id);
            return result;
        }
        return null;
    }

    public static String getItemTextureName(String name, int id) {
        if (instance.resourceStorage != null) {
            String result = instance.resourceStorage.itemTextureDescriptor.getTextureName(name, id);
            return result;
        }
        return null;
    }

    public static boolean isValidBlockTexture(String name, int id) {
        return getBlockTextureName(name, id) != null;
    }

    public static boolean isValidItemTexture(String name, int id) {
        return getItemTextureName(name, id) != null;
    }

    public static String getSourcePath() {
        return FileTools.DIR_PACK + "assets/resource_packs/vanilla/";
    }



    public class ListWatcher {
        JSONArray list;
        String path;

        public ListWatcher (String path) {
            this.path = path;
            try {
                this.list = FileTools.readJSONArray(path);
            } catch (Exception e) {
                try {
                    this.list = new JSONArray(new JSONTokener("[]"));
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
            }
        }

        public void save() {
            try {
                FileTools.writeJSON(path, list);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void clear() {
            this.list = new JSONArray();
        }

        public void add(JSONObject newElement) throws JSONException {
            for (int i = 0; i < list.length(); i++) {
                JSONObject element = list.getJSONObject(i);
                Iterator<String> elementKeys = element.keys();

                boolean equal = true;
                while (elementKeys.hasNext()) {
                    String elementtKey = elementKeys.next();
                    if (!(newElement.has(elementtKey) && newElement.getString(elementtKey).equals(element.getString(elementtKey)))) {
                        equal = false;
                        break;
                    }
                }

                if (equal)
                    return;
            }

            list.put(list.length(), newElement);
        }
    }

    public class DefinitionListWatcher extends ListWatcher {
        public DefinitionListWatcher(String path) {
            super(path);
        }

        public void add(String path) throws JSONException {
            JSONObject element = new JSONObject();
            element.put("file_system", "RawPath");
            element.put("path", path);
            super.add(element);
        }
    }

    public class ResourceListWatcher extends ListWatcher {
        public ResourceListWatcher(String path) {
            super(path);
        }

        public void add(String id) throws JSONException {
            JSONObject element = new JSONObject();
            element.put("pack_id", id);
            element.put("version", "1.0.0");
            super.add(element);
        }
    }
}
