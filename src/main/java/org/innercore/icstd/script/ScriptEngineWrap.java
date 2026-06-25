package org.innercore.icstd.script;

import com.zhekasmirnov.innercore.api.mod.API;
import com.zhekasmirnov.innercore.api.mod.ScriptableObjectHelper;
import com.zhekasmirnov.innercore.mod.executable.Compiler;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

public class ScriptEngineWrap {
    static Context assureContextForCurrentThread() { return Compiler.assureContextForCurrentThread(); }
    public static Object getOrCreateOpaqueScriptContext() { return Compiler.assureContextForCurrentThread(); }

    public static Object evalInScope(String src, ScriptObjectWrap scope, String name) {
        return assureContextForCurrentThread().evaluateString((Scriptable) scope.passToScript(), src, name, 0, null);
    }

    public static CompiledScriptWrap compileScriptFromReader(Reader src, Object apiInst, String name) {
        try {
            Context ctx = assureContextForCurrentThread();
            ScriptObjectWrap scope = ScriptObjectWrap.create(ctx.initStandardObjects(apiInst != null ? ((API) apiInst).newInstance() : ScriptableObjectHelper.createEmpty(), false));
            return new CompiledScriptWrap(ctx.compileReader(src, name, 1, null), scope);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static CompiledScriptWrap compileScript(String src, API apiInst, String name) {
        return compileScriptFromReader(new StringReader(src), apiInst, name);
    }

    public static void initForThisThread() {
        assureContextForCurrentThread();
    }

}
