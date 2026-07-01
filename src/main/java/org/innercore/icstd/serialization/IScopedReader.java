package org.innercore.icstd.serialization;

public interface IScopedReader {
    boolean isEmpty();
    boolean isObject();
    boolean isArray();

    IObjectReader asObject();
    IArrayReader asArray();

    void endScope();
    boolean isInScope();
    void forceEndScope(SerializationContext ctx);
}
