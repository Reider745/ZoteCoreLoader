package com.zhekasmirnov.innercore.mod.resource.types;

import com.zhekasmirnov.apparatus.minecraft.version.MinecraftVersions;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.utils.FileTools;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Iterator;

/**
 * Created by zheka on 27.07.2017.
 */

public class TextureAtlasDescription {
    public JSONObject jsonObject;
    public JSONObject textureData;

    public TextureAtlasDescription(String resourcesPath) {
        textureData = new JSONObject();
        jsonObject = new JSONObject();
        try {
            jsonObject.put("texture_data", textureData);
        } catch (JSONException ignore) {
        }

        for (String resourcePack : MinecraftVersions.getCurrent().getVanillaResourcePacksDirs()) {
            try {
                String manifestFile = resourcePack + resourcesPath;
                if (FileTools.exists(manifestFile)) {
                    JSONObject packTextureData = FileTools.getAssetAsJSON(manifestFile)
                            .optJSONObject("texture_data");
                    if (packTextureData != null) {
                        for (Iterator<String> it = packTextureData.keys(); it.hasNext();) {
                            String key = it.next();
                            textureData.put(key, packTextureData.opt(key));
                        }
                    }
                }
            } catch (Exception e) {
                Logger.message("TextureAtlasDescription(resourcesPath)", e);
            }
        }
    }

    public TextureAtlasDescription(JSONObject content) {
        try {
            jsonObject = content;
            textureData = this.jsonObject.getJSONObject("texture_data");
        } catch (Exception e) {
            Logger.message("TextureAtlasDescription(content)", e);
        }
    }

    public void addTexturePath(String name, int index, String path) throws JSONException {
        JSONObject textureUnit;
        if (textureData.has(name)) {
            textureUnit = textureData.getJSONObject(name);
        } else {
            textureUnit = new JSONObject();
            textureUnit.put("textures", new JSONArray());
        }

        JSONArray textureArray = textureUnit.optJSONArray("textures");
        if (textureArray == null) {
            textureArray = new JSONArray();
            textureArray.put(0, textureUnit.getString("textures"));
        }
        textureArray.put(index, path);

        textureUnit.put("textures", textureArray);
        textureData.put(name, textureUnit);
    }

    public int getTextureCount(String name) throws JSONException {
        JSONObject textureUnit;
        if (textureData.has(name)) {
            textureUnit = textureData.getJSONObject(name);
            JSONArray textureArray = textureUnit.optJSONArray("textures");
            return textureArray.length();
        }
        return 0;
    }

    public void addTextureFile(File texture, String path) throws JSONException {
        try {
            try {
                String filename = texture.getName();
                filename = filename.substring(0, filename.lastIndexOf('.'));
                String name = filename.substring(0, filename.lastIndexOf('_'));
                String index = filename.substring(filename.lastIndexOf('_') + 1);
                addTexturePath(name, Integer.valueOf(index), path);
            } catch (StringIndexOutOfBoundsException | NumberFormatException e) {
                String filename = texture.getName();
                String name = filename.substring(0, filename.lastIndexOf('.'));
                int index = getTextureCount(name);
                if (index > 0) {
                    ICLog.i("ERROR",
                            "found texture with no index that conflicts with already added texture, add aborted");
                } else {
                    addTexturePath(name, index, path);
                }
            }
        } catch (Exception e) {
            ICLog.i("ERROR", "invalid texture file name: " + texture.getName() + ", failed with error " + e);
        }
    }

    public String getTextureName(String name, int id) {
        if (textureData.has(name)) {
            JSONObject textureUnit = textureData.optJSONObject(name);
            if (textureUnit != null) {
                JSONArray textureArray = textureUnit.optJSONArray("textures");
                if (textureArray != null) {
                    return textureArray.optString(id, null);
                }
                String texture = textureUnit.optString("textures");
                if (texture != null) {
                    return texture;
                }
            }
        }

        return null;
    }
}
