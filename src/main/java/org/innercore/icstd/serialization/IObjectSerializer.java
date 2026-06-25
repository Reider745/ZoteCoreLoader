package org.innercore.icstd.serialization;

public interface IObjectSerializer<T> {
    T read(IScopedReader reader, SerializationContext ctx, String typeId);
    void write(IScopedWriter writer, T object, SerializationContext ctx, String typeId);
}
