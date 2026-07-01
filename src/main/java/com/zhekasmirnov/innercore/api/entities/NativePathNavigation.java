package com.zhekasmirnov.innercore.api.entities;

import org.innercore.icstd.entity.PathNavigation;

@Deprecated
public class NativePathNavigation extends PathNavigation {
    public NativePathNavigation(long entity) {
        super(entity);
    }

    public NativePathNavigation(long ptr, int refCnt, Object stub) {
        super(ptr, refCnt, stub);
    }
}
