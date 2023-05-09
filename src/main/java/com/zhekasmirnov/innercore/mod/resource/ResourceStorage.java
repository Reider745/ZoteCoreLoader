package com.zhekasmirnov.innercore.mod.resource;

//import com.zhekasmirnov.horizon.modloader.resource.directory.Resource;
import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.mod.resource.pack.IResourcePack;
import com.zhekasmirnov.innercore.mod.resource.types.ResourceFile;
import com.zhekasmirnov.innercore.mod.resource.types.TextureAnimationFile;
import com.zhekasmirnov.innercore.mod.resource.types.TextureAtlasDescription;
import com.zhekasmirnov.innercore.mod.resource.types.enums.TextureType;
import com.zhekasmirnov.innercore.utils.FileTools;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by zheka on 03.07.2017.
 */

public class ResourceStorage implements IResourcePack {
    public static final String VANILLA_RESOURCE = "resource_packs/vanilla/";

    public String getAbsolutePath() {
        return "/";
    }

    public String getPackName() {
        return "Inner Core Resource Storage";
    }

    TextureAtlasDescription blockTextureDescriptor;
    TextureAtlasDescription itemTextureDescriptor;
    JSONArray animationList;
    JSONArray textureList;

    public void build() throws IOException, JSONException {
        itemTextureDescriptor = new TextureAtlasDescription("textures/item_texture.json");
        blockTextureDescriptor = new TextureAtlasDescription("textures/terrain_texture.json");

        animationList = FileTools.getAssetAsJSONArray(VANILLA_RESOURCE + "textures/flipbook_textures.json");
        textureList = FileTools.getAssetAsJSONArray(VANILLA_RESOURCE + "textures/textures_list.json");
    }

    public String getId() {
        return "innercore-resource-main";
    }

    private HashMap<String, String> resourceLinks = new HashMap<>();

    public String getLinkedFilePath(String path) {
        String res = resourceLinks.get(path);
        return res != null ? res : path;
    }


    /*public void addResourceFile(TextureType type, Resource resource, String name) {
        try {
            switch (type){
            case ITEM:
                itemTextureDescriptor.addTexturePath(name, resource.getIndex(), resource.getPath());
                break;

            case BLOCK:
                blockTextureDescriptor.addTexturePath(name, resource.getIndex(), resource.getPath());
                break;

            default:
                break;
            }
        } catch (JSONException e){
            ICLog.e(ResourcePackManager.LOGGER_TAG, "Cannot add texture path", e);
        }
        
    }*/

    private void addAsAnimation(ResourceFile file) {
        TextureAnimationFile animationFile = new TextureAnimationFile(file);

        if (animationFile.isValid()) {
            try {
                JSONObject animationJson = animationFile.constructAnimation();
                animationList.put(animationJson);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public static native void nativeAddTextureToLoad(String path);

    private static ArrayList<String> textureLoadQueue = new ArrayList<>();
    public static void loadAllTextures() {
        for (String path : textureLoadQueue) {
            nativeAddTextureToLoad(path);
        }
    }

    public static void addTextureToLoad(String path) {
        textureLoadQueue.add(path);
        if (textureLoadQueue.size() < 2) {
            textureLoadQueue.add(path);
        }
    }
}
