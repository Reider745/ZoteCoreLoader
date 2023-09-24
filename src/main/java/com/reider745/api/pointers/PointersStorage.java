package com.reider745.api.pointers;

import com.reider745.api.pointers.pointer_gen.IBasePointerGen;
import com.reider745.api.pointers.pointer_gen.PointerGenSlowest;

import java.lang.ref.WeakReference;
import java.util.HashMap;

public class PointersStorage {
    private static final HashMap<String, PointersStorage> storages = new HashMap<>();

    private final HashMap<Long, WeakReference<PointerClass>> pointers = new HashMap<>();
    private final IBasePointerGen pointerGen;

    public PointersStorage(String type, final IBasePointerGen pointerGen){
        this.pointerGen = pointerGen;
        storages.put(type, this);
    }

    public PointersStorage(String type){
        this(type, new PointerGenSlowest());
    }

    public final long addPointer(PointerClass pointerClass){
        long ptr = pointerGen.next();
        pointers.put(ptr, new WeakReference<>(pointerClass));
        return ptr;
    }

    public final PointerClass get(long ptr){
        return pointers.get(ptr).get();
    }

    public final void removePointer(long pointer){
        pointers.remove(pointer);
        pointerGen.remove(pointer);
    }

    public static PointersStorage getStorageForType(final String type){
        return storages.get(type);
    }
}
