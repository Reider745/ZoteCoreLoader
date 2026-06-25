package org.innercore.icstd.event;

public interface IManagedEventListener<T> {
    void onEvent(EventController ctrl, T event);
}
