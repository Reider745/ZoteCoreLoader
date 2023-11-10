package com.reider745.api.hooks;

public class HookController  {
    private boolean replace;
    private Object result = null;
    private final Arguments arguments;
    private final Object self;

    public HookController(boolean replace, final Arguments arguments, Object self){
        this.replace = replace;
        this.arguments = arguments;
        this.self = self;
    }

    public final void setReplace(boolean replace) {
        this.replace = replace;
    }
    public final boolean isReplace() {
        return replace;
    }

    public final void setResult(Object result) {
        this.result = result;
    }
    public final Object getResult(){
        return result;
    }

    public final Arguments getArguments() {
        return arguments;
    }

    public final <T>T getSelf() {
        return (T) self;
    }
}