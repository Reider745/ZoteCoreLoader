package org.innercore.icstd.serialization;

public interface SerializationMethodsComponent {
    void read(IScopedReader io, SerializationContext ctx, String typeId);
    void write(IScopedWriter io, SerializationContext ctx, String typeId);
}
