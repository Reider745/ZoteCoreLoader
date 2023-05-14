package com.reider745.pointers;

import com.reider745.pointers.PointClass;

import java.util.HashMap;

public class PointerManager {
    private long start_pointer = -9223372036854775808l;
    private HashMap<Long, PointClass> pointers = new HashMap<>();

    public PointClass get(long pointer){
        synchronized (pointers) {
            return pointers.get(pointer);
        }
    }

    public long put(PointClass object){
        synchronized (pointers) {
            long pointer = start_pointer;
            pointers.put(pointer, object);
            start_pointer++;
            return pointer;
        }
    }

    public boolean has(long pointer){
        return pointers.get(pointer) != null;
    }

    public void clear(long pointer){
        pointers.remove(pointer);
    }
}
