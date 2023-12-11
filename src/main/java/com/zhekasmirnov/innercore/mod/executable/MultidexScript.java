package com.zhekasmirnov.innercore.mod.executable;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;

import java.util.ArrayList;

/**
 * Created by zheka on 25.10.2017.
 */

public class MultidexScript implements Script {
    private ArrayList<Script> scripts = new ArrayList<>();

    public void addScript(Script script) {
        scripts.add(script);
    }

    public int getScriptCount() {
        return scripts.size();
    }

    @Override
    public Object exec(Context context, Scriptable scriptable) {
        Object result = null;

        Context ctx = Compiler.assureContextForCurrentThread();
        for (Script script : scripts) {
            Object _result = script.exec(ctx, scriptable);
            if (_result != null) {
                result = _result;
            }
        }

        return result;
    }
}
