package com.zhekasmirnov.apparatus.mcpe;

import com.reider745.entity.EntityMethod;

public class NativeStaticUtils {
    public static boolean isExistingEntity(long entityUid) {
        return EntityMethod.getEntityById(entityUid) != null;
    }
}
