package com.zhekasmirnov.innercore.api.mod.preloader;

/**
 * Created by zheka on 15.09.2017.
 */

import java.io.File;

@Deprecated(since = "Zote")
public class TextureFileEntry {
    @SuppressWarnings("unused")
    private File source;

    public TextureFileEntry(File source) {
        this.source = source;
    }
}


