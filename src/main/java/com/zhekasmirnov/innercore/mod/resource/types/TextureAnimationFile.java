package com.zhekasmirnov.innercore.mod.resource.types;

import android.support.annotation.NonNull;
import com.zhekasmirnov.innercore.mod.resource.types.enums.FileType;
import com.zhekasmirnov.innercore.mod.resource.types.enums.ParseError;
import com.zhekasmirnov.innercore.utils.FileTools;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by zheka on 27.07.2017.
 */

public class TextureAnimationFile extends ResourceFile {
    public TextureAnimationFile(@NonNull String path) {
        super(path);
        read();
    }

    public TextureAnimationFile(ResourceFile file) {
        super(file.getResourcePack(), file);
        read();
    }

    private boolean isValid = false;

    public boolean isValid() {
        return isValid;
    }

    private int delay;
    private String textureName;
    private String tileToAnimate;
    private int replicate = 0;

    /*
     * JSON:
     * {
     * name: texture atlas of animation
     * tile: tile name to animate
     * delay: optional delay in ticks
     * }
     * 
     * TEXTURE NAME:
     * - "tile.anim.png"
     * - "tile.anim.delay.png"
     */

    private void read() {
        if (getType() != FileType.ANIMATION) {
            parseError = ParseError.ANIMATION_INVALID_FILE;
            return;
        }

        switch (getAnimationType()) {
            case DESCRIPTOR:
                try {
                    JSONObject data = FileTools.readJSON(getAbsolutePath());
                    if (data.has("name")) {
                        textureName = data.getString("name");
                    } else {
                        parseError = ParseError.ANIMATION_NAME_MISSING;
                        return;
                    }
                    if (data.has("tile")) {
                        tileToAnimate = data.getString("tile");
                    } else {
                        parseError = ParseError.ANIMATION_TILE_MISSING;
                        return;
                    }
                    if (data.has("delay")) {
                        delay = data.optInt("delay");
                        if (delay < 1) {
                            parseError = ParseError.ANIMATION_INVALID_DELAY;
                        }
                    } else {
                        delay = 1;
                    }
                    replicate = data.optInt("replicate");
                } catch (Exception e) {
                    parseError = ParseError.ANIMATION_INVALID_JSON;
                    e.printStackTrace();
                }

                break;
            case TEXTURE:
                List<String> nameParts = new ArrayList<>();
                Collections.addAll(nameParts, getName().split("\\."));

                replicate = 0;
                if (nameParts.size() > 2 && "liquid".equals(nameParts.get(1))) {
                    replicate = 2;
                    nameParts.remove(1);
                }

                if (nameParts.size() > 2 && nameParts.get(nameParts.size() - 2).equals("anim")) {
                    delay = 1;
                } else if (nameParts.size() > 3 && nameParts.get(nameParts.size() - 3).equals("anim")) {
                    try {
                        delay = Integer.parseInt(nameParts.get(nameParts.size() - 2));
                    } catch (Exception e) {
                        parseError = ParseError.ANIMATION_INVALID_DELAY;
                        return;
                    }
                    if (delay < 1) {
                        parseError = ParseError.ANIMATION_INVALID_DELAY;
                        return;
                    }
                } else {
                    parseError = ParseError.ANIMATION_INVALID_NAME;
                    return;
                }

                tileToAnimate = nameParts.get(0);
                textureName = getLocalPath();

                break;
            default:
                return;
        }

        isValid = true;
    }

    public JSONObject constructAnimation() throws JSONException {
        if (!isValid) {
            return null;
        }

        JSONObject data = new JSONObject();
        data.put("atlas_tile", tileToAnimate + "");
        data.put("flipbook_texture", textureName + "");
        data.put("ticks_per_frame", delay);
        if (replicate > 1)
            data.put("replicate", replicate);
        return data;
    }
}
