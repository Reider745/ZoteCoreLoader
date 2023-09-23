package com.reider745.api.pointers;

public class PointerClass {
    public final long pointer;
    public final PointersStorage storage;

    public PointerClass(){
        final Class<?> cl = getClass();
        storage = PointersStorage.getStorageForClassType(cl.getPackage()+"."+cl.getName());
        pointer = storage.addPointer(this);
    }

    public final long getPointer(){
        return pointer;
    }

    @Override
    protected final void finalize() throws Throwable {
        storage.removePointer(pointer);
    }
}
