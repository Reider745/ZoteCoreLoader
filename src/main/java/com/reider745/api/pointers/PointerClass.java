package com.reider745.api.pointers;

public abstract class PointerClass<T> {
    public final long pointer;
    public final PointersStorage storage;

    public PointerClass(){
        this.storage = PointersStorage.getStorageForType(getPointerStorageType());
        pointer = storage.addPointer(this);
    }

    protected PointerClass(PointersStorage storage){
        this.storage = storage;
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
