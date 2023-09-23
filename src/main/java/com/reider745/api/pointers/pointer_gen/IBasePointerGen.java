package com.reider745.api.pointers.pointer_gen;

public interface IBasePointerGen {
    long next();
    void remove(final long ptr);
}
