package org.innercore.icstd.updatable;

import com.zhekasmirnov.innercore.mod.executable.Compiler;
import org.innercore.icstd.script.ScriptObjectComponent;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

class ScriptUpdatable implements UpdatableComponent, ScriptObjectComponent {
    UpdatableSchedulerScope scope;
    Scriptable object;
    Function function;
    Object[] args;
    Scriptable parentScope;

    ScriptUpdatable(UpdatableSchedulerScope scope, ScriptableObject object, Function function) {
        this.scope = scope;
        this.object = object;
        this.parentScope = function.getParentScope();
        if (this.object == null)
            this.object = parentScope;
        this.function = function;
        args = scope.scriptDtArg;
    }

    @Override
    public void update(int dt) {
        if (scope.scriptCtx == null)
            scope.scriptCtx = Compiler.assureContextForCurrentThread();
        args[0] = dt;
        function.call(scope.scriptCtx, parentScope, object, args);
    }

    @Override
    public Object getScriptObject() {
        return object;
    }
}