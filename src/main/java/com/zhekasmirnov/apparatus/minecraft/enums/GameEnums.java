package com.zhekasmirnov.apparatus.minecraft.enums;

import com.zhekasmirnov.apparatus.minecraft.version.MinecraftVersion;
import com.zhekasmirnov.apparatus.minecraft.version.MinecraftVersions;
import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.utils.FileTools;
import org.json.JSONException;
import java.util.HashMap;
import java.util.Map;

public class GameEnums {
    private static final GameEnums singleton = new GameEnums();

    public static synchronized GameEnums getSingleton() {
        return singleton;
    }

    static {
        for (MinecraftVersion version : MinecraftVersions.getAllVersions()) {
            try {
                getSingleton().getOrAddContainerForVersion(version).addEnumsFromJson(FileTools.getAssetAsJSON("innercore/enum/enums-" + version.getCode() + ".json"));
            } catch (JSONException exception) {
                ICLog.e("ERROR", "GameEnums failed to load enums for game version " + version, exception);
            }
        }
    }


    private final Map<MinecraftVersion, EnumsContainer> enumsContainerForVersion = new HashMap<>();

    public EnumsContainer getOrAddContainerForVersion(MinecraftVersion version) {
        EnumsContainer container = enumsContainerForVersion.get(version);
        if (container == null) {
            enumsContainerForVersion.put(version, container = new EnumsContainer(version));
        }
        return container;
    }

    public Object getEnum(String scope, String name, MinecraftVersion version) {
        EnumsContainer container = enumsContainerForVersion.get(version);
        Object result = container != null ? container.getEnum(scope, name) : null;
        if (result == null) {
            ICLog.i("ERROR", "GameEnums failed to find enum " + scope + ":" + name + " for game version " + version.getName());
        }
        return result;
    }

    public Object getEnum(String scope, String name) {
        return getEnum(scope, name, MinecraftVersions.getCurrent());
    }

    public String getKeyForEnum(String scope, Object value, MinecraftVersion version) {
        EnumsContainer container = enumsContainerForVersion.get(version);
        return container != null ? container.getKeyForEnum(scope, value) : null;
    }

    public String getKeyForEnum(String scope, Object value) {
        return getKeyForEnum(scope, value, MinecraftVersions.getCurrent());
    }

    public Object convertBetweenVersions(String scope, Object value, MinecraftVersion from, MinecraftVersion to) {
        String name = getKeyForEnum(scope, value, from);
        return name != null ? getEnum(scope, name, to) : null;
    }

    public Object convertFromVersion(String scope, Object value, MinecraftVersion from) {
        return convertBetweenVersions(scope, value, from, MinecraftVersions.getCurrent());
    }

    public Object convertToVersion(String scope, Object value, MinecraftVersion to) {
        return convertBetweenVersions(scope, value, MinecraftVersions.getCurrent(), to);
    }

    // using this to convert
    public int getIntEnumOrConvertFromLegacyVersion(String scope, Object value, int fallback, MinecraftVersion legacyVersion) {
        if (value instanceof Number) {
            return getInt(convertFromVersion(scope, ((Number) value).intValue(), legacyVersion), fallback);
        } else if (value instanceof String) {
            return getInt(getEnum(scope, (String) value), fallback);
        } else {
            return fallback;
        }
    }


    public static int getInt(Object value, int fallback) {
        return value instanceof Number ? ((Number) value).intValue() : fallback;
    }

    public static int getInt(Object value) {
        return getInt(value, 0);
    }

    public static String getString(Object value, String fallback) {
        return value instanceof String ? (String) value : fallback;
    }

    public static String getString(Object value) {
        return getString(value, null);
    }
}
