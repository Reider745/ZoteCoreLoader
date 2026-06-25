package org.innercore.icstd.serialization;

public interface IScopedWriter {
    IObjectWriter asObject();
    IArrayWriter asArray();

    void endScope();
    boolean isInScope();
    void forceEndScope(SerializationContext ctx);
}
