package com.zhekasmirnov.innercore.api.mod.ui.memory;

/**
 * Created by zheka on 12.09.2017.
 */

@Deprecated(since = "Zote")
public class SourceBitmapWrap extends BitmapWrap {
    private String name;

    SourceBitmapWrap(String name, int w, int h) {
        super();
        this.name = name;
        this.width = w;
        this.height = h;
        this.isStored = true;
    }

    @Override
    public boolean store() {
        return true;
    }

    @Override
    public boolean restore() {
        return true;
    }

    @Override
    public BitmapWrap resize(int w, int h) {
        BitmapWrap wrap = BitmapWrap.wrap(name, w, h);
        storeIfNeeded();
        return wrap;
    }
}
