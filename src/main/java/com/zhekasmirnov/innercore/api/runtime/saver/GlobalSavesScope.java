package com.zhekasmirnov.innercore.api.runtime.saver;

/**
 * Created by zheka on 20.08.2017.
 */

public abstract class GlobalSavesScope {
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public abstract Object save();
    public abstract void read(Object scope);
}
