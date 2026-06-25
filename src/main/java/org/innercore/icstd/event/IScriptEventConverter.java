package org.innercore.icstd.event;

public interface IScriptEventConverter<T> {
    T toEvent(String name, Object[] args);
}
