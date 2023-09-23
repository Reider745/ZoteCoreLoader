package com.reider745.api.pointers.pointer_gen;

import java.util.ArrayList;
import java.util.Random;

public class PointerGenSlowest implements IBasePointerGen {
    public final ArrayList<Long> usePointers = new ArrayList<>();
    public final Random random = new Random();

    @Override
    public final long next() {
        while (true){
            final long ptr = random.nextLong();
            if(!usePointers.contains(ptr)){
                usePointers.add(ptr);
                return ptr;
            }
        }
    }

    @Override
    public final void remove(long ptr) {
        usePointers.remove(ptr);
    }
}