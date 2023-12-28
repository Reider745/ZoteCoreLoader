package com.reider745.api.pointers;

import com.reider745.api.pointers.pointer_gen.IBasePointerGen;
import com.reider745.api.pointers.pointer_gen.PointerGenSlowest;
import com.reider745.world.BiomesMethods;

import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class PointersStorage<T> {
    private static final HashMap<String, PointersStorage> storages = new HashMap<>();

    private final ConcurrentHashMap<Long, ClassPointer<T>> pointers = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<ClassPointer<T>, Long> pointerForInstance = new ConcurrentHashMap<>();
    private final IBasePointerGen pointerGen;
    public interface INewPointer<T> {
        ClassPointer<T> apply(WeakReference<T> value);
    }

    private final INewPointer newPointer;

    public PointersStorage(String type, final IBasePointerGen pointerGen, final INewPointer<T> newPointer, boolean clear){
        System.out.println("Loaded pointer storage, type - "+type);
        this.pointerGen = pointerGen;
        storages.put(type, this);

        if(clear) new ThreadCheckToClear<T>(this);
        this.newPointer = newPointer;
    }

    public PointersStorage(String type, final IBasePointerGen pointerGen, boolean clear){
        this(type, pointerGen, ClassPointer::new, clear);
    }

    public PointersStorage(String type, final IBasePointerGen pointerGen){
        this(type, pointerGen, ClassPointer::new, true);
    }

    public PointersStorage(String type){
        this(type, new PointerGenSlowest());
    }

    public PointersStorage(String type, final INewPointer<T> newPointer){
        this(type, new PointerGenSlowest(), newPointer, true);
    }

    public final long addPointer(T pointerClass){
        long ptr = pointerGen.next();
        ClassPointer<T> pointer = newPointer.apply(new WeakReference<>(pointerClass));
        pointers.put(ptr, pointer);
        pointerForInstance.put(pointer, ptr);
        return ptr;
    }

    public final long addPointer(ClassPointer<T> pointer){
        long ptr = pointerGen.next();
        pointers.put(ptr, pointer);
        pointerForInstance.put(pointer, ptr);
        return ptr;
    }

    public final T get(long ptr){
        return pointers.get(ptr).get();
    }

    public final ClassPointer<T> getInstance(long ptr){
        return pointers.get(ptr);
    }

    public final long getPointerForInstance(T value){
        Long ptr = pointerForInstance.get(value);
        return ptr == null ? 0 : ptr;
    }

    public final void removePointer(long pointer){
        pointers.remove(pointer);
        pointerGen.remove(pointer);
    }

    public final void replace(long ptr, ClassPointer<T> classPointer){
        if(ptr == 0){
            addPointer(classPointer);
            return;
        }
        pointers.put(ptr, classPointer);
        pointerForInstance.put(classPointer, ptr);
    }

    public ConcurrentHashMap<Long, ClassPointer<T>> getPointers() {
        return pointers;
    }

    public static <T>PointersStorage<T> getStorageForType(final String type){
        return storages.get(type);
    }
}
