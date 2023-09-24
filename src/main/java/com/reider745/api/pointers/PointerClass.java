package com.reider745.api.pointers;

public abstract class PointerClass {
    public final long pointer;
    public final PointersStorage storage;

    public PointerClass(){
        storage = PointersStorage.getStorageForType(getPointerStorageType());
        pointer = storage.addPointer(this);
    }

    abstract public String getPointerStorageType();

    public final long getPointer(){
        return pointer;
    }

    @Override
    protected final void finalize() throws Throwable {
        storage.removePointer(pointer);
    }
}
