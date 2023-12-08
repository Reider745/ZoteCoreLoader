package com.zhekasmirnov.innercore.api.unlimited;

import com.zhekasmirnov.apparatus.minecraft.enums.GameEnums;
import com.zhekasmirnov.apparatus.minecraft.version.MinecraftVersions;
import com.zhekasmirnov.innercore.api.NativeBlock;
import com.zhekasmirnov.innercore.api.NativeItemModel;
import com.zhekasmirnov.innercore.api.mod.ScriptableObjectHelper;
import org.mozilla.javascript.ScriptableObject;

import java.util.HashMap;

/**
 * Created by zheka on 08.08.2017.
 */

public class SpecialType {
    private static HashMap<String, SpecialType> specialTypeByName = new HashMap<>();

    private static final String NONE_NAME = "__none__";
    public static final SpecialType NONE = getSpecialType(NONE_NAME);
    public static final SpecialType DEFAULT = getSpecialType("__default__");
    public static final SpecialType OPAQUE;

    enum BlockColorSource {
        NONE(0),
        LEAVES(1),
        GRASS(2),
        WATER(3);

        public final int id;

        BlockColorSource(int id) {
            this.id = id;
        }
    }

    static {
        OPAQUE = createSpecialType("opaque");
        OPAQUE.solid = true;
        OPAQUE.base = 1;
        OPAQUE.lightopacity = 15;
        OPAQUE.explosionres = 4;
        OPAQUE.renderlayer = 2;
        OPAQUE.translucency = 0;
        OPAQUE.sound = "stone";
        OPAQUE.approve();
    }

    public final String name;

    private boolean isApproved = false;

    public SpecialType approve() {
        isApproved = true;
        return this;
    }

    public boolean isApproved() {
        return isApproved;
    }

    public SpecialType (String name) {
        this.name = name;
    }


    public static SpecialType getSpecialType(String name) {
        if (specialTypeByName.containsKey(name)) {
            return specialTypeByName.get(name);
        }
        SpecialType type = new SpecialType(name);
        specialTypeByName.put(name, type);
        return type;
    }

    public static SpecialType createSpecialType(String name) {
        SpecialType type = getSpecialType(name);
        if (!type.equals(DEFAULT)) {
            type.approve();
        }
        return type;
    }



    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SpecialType) {
            SpecialType type = ((SpecialType) obj);
            if (name.equals(NONE_NAME) || type.name.equals(NONE_NAME)){
                return true;
            }
            return type.name.equals(name);
        }
        return super.equals(obj);
    }

    public String sound = ""; 
    public int material = 3;
    public int base = 0;
    public int rendertype = 0;
    public int renderlayer = GameEnums.getInt(GameEnums.getSingleton().getEnum("block_render_layer", "alpha"));
    public int lightlevel = 0;
    public int lightopacity = 0;
    public float explosionres = 3;
    public float destroytime = 1;
    public float friction = 0.6000000238418579f;
    public float translucency = 1;
    public boolean solid = false;
    public boolean can_contain_liquid = false;
    public boolean can_be_extra_block = false;
    public boolean renderallfaces = false;
    public int mapcolor = 0;
    public String color_source = "";

    public void setupBlock(int id) {
        NativeBlock.setMaterial(id, material);
        NativeBlock.setMaterialBase(id, base);
        NativeBlock.setSoundType(id, sound);
        NativeBlock.setSolid(id, solid);
        NativeBlock.setCanContainLiquid(id, can_contain_liquid);
        NativeBlock.setCanBeExtraBlock(id, can_be_extra_block);
        NativeBlock.setRenderAllFaces(id, renderallfaces);
        // NativeBlock.setRenderType(id, rendertype);
        // NativeBlock.setRenderLayer(id, renderlayer);
        NativeBlock.setLightLevel(id, lightlevel);
        // NativeBlock.setLightOpacity(id, lightopacity);
        NativeBlock.setExplosionResistance(id, explosionres);
        NativeBlock.setFriction(id, friction);
        NativeBlock.setDestroyTime(id, destroytime);
        NativeBlock.setTranslucency(id, translucency);
        NativeBlock.setMapColor(id, mapcolor);

        BlockColorSource colorSource = BlockColorSource.NONE;
        try {
            colorSource = color_source != null ? BlockColorSource.valueOf(color_source.toUpperCase()) : BlockColorSource.NONE;
        } catch (IllegalArgumentException ignore) { }
        NativeBlock.setBlockColorSource(id, colorSource.id);

        for (int data = 0; data < 16; data++) {
            BlockVariant variant = BlockRegistry.getBlockVariant(id, data);
            if (variant != null) {
                variant.renderType = rendertype;
                NativeItemModel.getFor(id, data).updateForBlockVariant(variant);
            }
        }
    }

    public void setupProperties(ScriptableObject properties) {
        if (properties != null) {
            base = ScriptableObjectHelper.getIntProperty(properties, "base", base);
            material = ScriptableObjectHelper.getIntProperty(properties, "material", material);
            sound = ScriptableObjectHelper.getStringProperty(properties, "sound", sound);
            solid = ScriptableObjectHelper.getBooleanProperty(properties, "solid", solid);
            can_contain_liquid = ScriptableObjectHelper.getBooleanProperty(properties, "can_contain_liquid", can_contain_liquid);
            can_be_extra_block = ScriptableObjectHelper.getBooleanProperty(properties, "can_be_extra_block", can_be_extra_block);
            renderallfaces = ScriptableObjectHelper.getBooleanProperty(properties, "renderallfaces", renderallfaces);
            rendertype = ScriptableObjectHelper.getIntProperty(properties, "rendertype", rendertype);
            renderlayer = GameEnums.getSingleton().getIntEnumOrConvertFromLegacyVersion("block_render_layer", ScriptableObjectHelper.getProperty(properties, "renderlayer", null), renderlayer, MinecraftVersions.MINECRAFT_1_11_4);
            lightlevel = ScriptableObjectHelper.getIntProperty(properties, "lightlevel", lightlevel);
            lightopacity = ScriptableObjectHelper.getIntProperty(properties, "lightopacity", lightopacity);
            mapcolor = ScriptableObjectHelper.getIntProperty(properties, "mapcolor", mapcolor);
            explosionres = ScriptableObjectHelper.getFloatProperty(properties, "explosionres", explosionres);
            friction = ScriptableObjectHelper.getFloatProperty(properties, "friction", friction);
            destroytime = ScriptableObjectHelper.getFloatProperty(properties, "destroytime", destroytime);
            translucency = ScriptableObjectHelper.getFloatProperty(properties, "translucency", translucency);
            color_source = ScriptableObjectHelper.getStringProperty(properties, "color_source", color_source);
        }
    }
}
