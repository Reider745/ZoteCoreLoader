package org.innercore.icstd.event;

public interface IEventListener<T> {
    void onEvent(T event);
}
