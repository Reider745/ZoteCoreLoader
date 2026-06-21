package com.zhekasmirnov.innercore.api.mod.util;

import org.mozilla.javascript.*;

public abstract class ScriptableAccessorImpl implements AccessorSlot.Getter, AccessorSlot.Setter {
    private Function getterFunction = null;
    @Override
    public Function asGetterFunction(String name, Scriptable scope) {
        if (getterFunction == null)
            getterFunction = new ScriptableFunctionImpl() {
                @Override
                public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
                    return ScriptableAccessorImpl.this.getValue(thisObj);
                }
            };
        return getterFunction;
    }

    private Function setterFunction = null;
    @Override
    public Function asSetterFunction(String name, Scriptable scope) {
        if (setterFunction == null)
            setterFunction = new ScriptableFunctionImpl() {
                @Override
                public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
                    return ScriptableAccessorImpl.this.setValue(args.length > 0 ? args[0] : Undefined.instance, thisObj, thisObj);
                }
            };
        return setterFunction;
    }

    public void defineSelfAsProperty(ScriptableObject o, String name, int attributes) {
        o.defineAccessorProperty(name, this, this, attributes);
    }
}
