package com.zhekasmirnov.innercore.mod.resource;

import com.reider745.InnerCoreServer;
import com.zhekasmirnov.innercore.mod.resource.pack.IResourcePack;
import com.zhekasmirnov.innercore.mod.resource.types.TextureAtlasDescription;
import com.zhekasmirnov.innercore.mod.resource.types.enums.TextureType;
import com.zhekasmirnov.innercore.utils.FileTools;
import org.json.JSONArray;
import org.json.JSONException;
import java.io.IOException;
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

    @Deprecated(since = "Zote")
    public void addResourceFile(TextureType type, Object resource, String name) {
    }

    @Deprecated(since = "Zote")
    public static void nativeAddTextureToLoad(String path) {
        InnerCoreServer.useClientMethod("ResourceStorage.nativeAddTextureToLoad(path)");
    }

    @Deprecated(since = "Zote")
    public static void loadAllTextures() {
    }

    @Deprecated(since = "Zote")
    public static void addTextureToLoad(String path) {
    }
}
