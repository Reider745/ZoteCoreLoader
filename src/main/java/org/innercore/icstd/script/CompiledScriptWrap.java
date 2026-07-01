package org.innercore.icstd.script;

import org.mozilla.javascript.Script;

public class CompiledScriptWrap {
    private final Script script;
    private final ScriptObjectWrap scopeWrap;

    CompiledScriptWrap(Script script, ScriptObjectWrap scopeWrap) {
        this.script = script;
        this.scopeWrap = scopeWrap;
    }

    public ScriptObjectWrap getScope() {
        return scopeWrap;
    }

    public Object run() {
        return script.exec(ScriptEngineWrap.assureContextForCurrentThread(), scopeWrap.obj);
    }
}
