package com.zhekasmirnov.apparatus.minecraft.enums;

import com.zhekasmirnov.apparatus.minecraft.version.MinecraftVersion;
import com.zhekasmirnov.apparatus.minecraft.version.MinecraftVersions;
import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.utils.FileTools;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class EnumsScopeInfo {
    private static final Map<String, EnumsScopeInfo> scopeInfoMap = new HashMap<>();

    static {
        try {
            JSONObject scopeInfoJson = FileTools.getAssetAsJSON("innercore/enum/enum-scopes.json");
            for (Iterator<String> it = scopeInfoJson.keys(); it.hasNext(); ) {
                String name = it.next();
                JSONObject json = scopeInfoJson.optJSONObject(name);
                if (json != null) {
                    scopeInfoMap.put(name, new EnumsScopeInfo(json));
                }
            }
        } catch (JSONException e) {
            ICLog.e("ERROR", "EnumsScopeInfo failed to get scope info from assets", e);
        }
    }

    public static EnumsScopeInfo getForScope(String name) {
        return scopeInfoMap.get(name);
    }

    public static Set<String> getAllScopesWithInfo() {
        return scopeInfoMap.keySet();
    }


    private String typeName;
    private MinecraftVersion jsScopeVersion;

    public EnumsScopeInfo() {

    }

    public EnumsScopeInfo(JSONObject json) {
        if (json != null) {
            String typeName = json.optString("typename");
            if (typeName != null) {
                setTypeName(typeName);
            }

            MinecraftVersion jsScopeVersion = MinecraftVersions.getVersionByCode(json.optInt("jsScopeVersion"));
            setJsScopeVersion(jsScopeVersion != null ? jsScopeVersion : MinecraftVersions.getCurrent());
        }
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public MinecraftVersion getJsScopeVersion() {
        return jsScopeVersion;
    }

    public void setJsScopeVersion(MinecraftVersion jsScopeVersion) {
        this.jsScopeVersion = jsScopeVersion;
    }
}
