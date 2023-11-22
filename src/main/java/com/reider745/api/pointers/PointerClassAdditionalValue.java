package com.reider745.api.pointers;

import java.lang.ref.WeakReference;

public class PointerClassAdditionalValue<T> extends ClassPointer<T> {
    private Object value = null;

    public PointerClassAdditionalValue(WeakReference<T> reference) {
        super(reference);
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
