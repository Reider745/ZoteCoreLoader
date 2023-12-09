package com.zhekasmirnov.innercore.api.unlimited;

import com.zhekasmirnov.horizon.runtime.logger.Logger;
import com.zhekasmirnov.innercore.api.mod.ScriptableObjectHelper;
import com.zhekasmirnov.innercore.api.mod.ui.GuiBlockModel;
import com.zhekasmirnov.innercore.api.runtime.other.NameTranslation;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.ScriptableObject;

/**
 * Created by zheka on 08.08.2017.
 */

public class BlockVariant {
    public final String name;
    public final String[] textures;
    public final int[] textureIds;
    public final boolean inCreative;
    public boolean isTechnical;

    public final int uid, data;

    public int renderType = 0;
    public BlockShape shape = new BlockShape();


    public BlockVariant(int uid, int data, String name, String[] textures, int[] textureIds, boolean inCreative) {
        this.name = name;
        this.textures = textures;
        this.textureIds = textureIds;
        this.inCreative = inCreative;

        this.uid = uid;
        this.data = data;

        validate();
    }

    public BlockVariant(int uid, int data, ScriptableObject obj) {
        this.uid = uid;
        this.data = data;

        String name = ScriptableObjectHelper.getStringProperty(obj, "name", null);
        if (name != null) {
            name = NameTranslation.fixUnicodeIfRequired("block_" + uid + "_" + data, name);
        }
        this.name = name;
        this.inCreative = ScriptableObjectHelper.getBooleanProperty(obj, "inCreative", false);
        this.isTechnical = ScriptableObjectHelper.getBooleanProperty(obj, "isTech", !inCreative);
        this.textures = new String[6];
        this.textureIds = new int[6];

        try {
            NativeArray _texs = ScriptableObjectHelper.getNativeArrayProperty(obj, "texture", ScriptableObjectHelper.getNativeArrayProperty(obj, "textures", null));
            if (_texs != null) {
                Object[] texs = _texs.toArray();
                for (int i = 0; i < 6; i++) {
                    Object _tex = texs[i > texs.length - 1 ? texs.length - 1 : i];
                    if (_tex != null && _tex instanceof NativeArray) {
                        Object[] tex = ((NativeArray) _tex).toArray();
                        if (tex[0] instanceof CharSequence && tex[1] instanceof Number) {
                            textures[i] = tex[0].toString();
                            textureIds[i] = ((Number) tex[1]).intValue();

                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        validate();

        NameTranslation.sendNameToGenerateCache(uid, data, name);
    }

    private void validate() {
        for (int i = 0; i < 6; i++) {
            if (textures[i] == null) {
                textures[i] = "missing_block";
                textureIds[i] = 0;
            }
        }


    }

    public GuiBlockModel getGuiBlockModel() {
        return null;
    }

    public String getSpriteTexturePath() {
        return "";
    }
}
