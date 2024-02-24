package com.zhekasmirnov.apparatus.minecraft.enums;

import com.zhekasmirnov.apparatus.minecraft.version.MinecraftVersion;
import com.zhekasmirnov.innercore.api.mod.ScriptableObjectHelper;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

public class EnumsJsInjector {
    private final Scriptable jsScope;
    private boolean convertToUpperCase = false;

    public EnumsJsInjector(Scriptable jsScope, boolean convertToUpperCase) {
        this.jsScope = jsScope;
        this.convertToUpperCase = convertToUpperCase;
    }

    public void injectEnumScope(MinecraftVersion version, String scopeName, String typeName) {
        ScriptableObject scopeScriptable = ScriptableObjectHelper.createEmpty();
        EnumsContainer container = GameEnums.getSingleton().getOrAddContainerForVersion(version);
        EnumsContainer.Scope scope = container.getScope(scopeName);

        if (scope != null) {
            for (String name : scope.getAllEnumNames()) {
                scopeScriptable.put(convertToUpperCase ? name.toUpperCase() : name, scopeScriptable, scope.getEnum(name));
            }
        }
        jsScope.put(typeName, jsScope, scopeScriptable);
    }

    public void injectEnumScopeUsingGlobalInfo(String scopeName, String typeNamePrefix) {
        EnumsScopeInfo info = EnumsScopeInfo.getForScope(scopeName);
        if (info != null) {
            injectEnumScope(info.getJsScopeVersion(), scopeName, typeNamePrefix + info.getTypeName());
        }
    }

    public void injectAllEnumScopes(String typeNamePrefix) {
        for (String scopeName : EnumsScopeInfo.getAllScopesWithInfo()) {
            injectEnumScopeUsingGlobalInfo(scopeName, typeNamePrefix);
        }
    }
}
