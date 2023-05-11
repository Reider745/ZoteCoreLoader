package com.reider745.pointers;

import com.zhekasmirnov.horizon.runtime.logger.Logger;

import java.util.HashMap;

public class PointClass  {
    private static HashMap<String, PointerManager> pointers = new HashMap<>();

    private static PointerManager register(String name){
        if(pointers.containsKey(name))
            return pointers.get(name);
        PointerManager manager = new PointerManager();
        pointers.put(name, manager);
        return manager;
    }

    public static <T>T getClassByPointer(String name, long pointer){
        return (T) pointers.get(name).get(pointer);
    }

    private PointerManager pointerManager;
    private long pointer;

    public PointClass(){
        pointerManager = register(this.getClass().getName());
        pointer = pointerManager.put(this);
    }

    public long getPointer(){
        return pointer;
    }

    @Override
    public void finalize() {
        pointerManager.clear(pointer);
    }
}
