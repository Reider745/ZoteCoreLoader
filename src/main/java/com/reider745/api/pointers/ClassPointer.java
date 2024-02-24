package com.reider745.api.pointers;

import java.lang.ref.WeakReference;

public class ClassPointer<T> {
    protected WeakReference<T> reference;

    public ClassPointer(WeakReference<T> reference) {
        this.reference = reference;
    }

    public final T get() {
        if (reference == null)
            return null;
        return reference.get();
    }

    public boolean hasClear() {
        return reference == null || reference.get() == null;
    }

    public WeakReference<T> getReference() {
        return reference;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        return obj.equals(get());
    }
}
