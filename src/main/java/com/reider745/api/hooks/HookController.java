package com.reider745.api.hooks;

public class HookController<T> {
    private boolean replace;
    private Object result = null;
    private final Arguments arguments;
    private final T self;

    public HookController(boolean replace, Arguments arguments, T self) {
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
        setReplace(true);
        this.result = result;
    }

    public final Object getResult() {
        return result;
    }

    public final Arguments getArguments() {
        return arguments;
    }

    public final T getSelf() {
        return self;
    }
}
