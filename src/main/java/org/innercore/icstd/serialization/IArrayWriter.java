package org.innercore.icstd.serialization;

public interface IArrayWriter {
    int size();

    IArrayWriter addInt(int value);
    IArrayWriter addInt(int idx, int value);
    IArrayWriter setInt(int idx, int value);
    IArrayWriter addLong(long value);
    IArrayWriter addLong(int idx, long value);
    IArrayWriter setLong(int idx, long value);
    IArrayWriter addShort(short value);
    IArrayWriter addShort(int idx, short value);
    IArrayWriter setShort(int idx, short value);
    IArrayWriter addByte(byte value);
    IArrayWriter addByte(int idx, byte value);
    IArrayWriter setByte(int idx, byte value);
    IArrayWriter addFloat(float value);
    IArrayWriter addFloat(int idx, float value);
    IArrayWriter setFloat(int idx, float value);
    IArrayWriter addDouble(double value);
    IArrayWriter addDouble(int idx, double value);
    IArrayWriter setDouble(int idx, double value);
    IArrayWriter addBoolean(boolean value);
    IArrayWriter addBoolean(int idx, boolean value);
    IArrayWriter setBoolean(int idx, boolean value);
    IArrayWriter addString(String value);
    IArrayWriter addString(int idx, String value);
    IArrayWriter setString(int idx, String value);

    boolean isPutAnySupported();
    void addAny(Object value);
    void addAny(int idx, Object value);
    void setAny(int idx, Object value);

    IScopedWriter beginScope();
    IScopedWriter beginScope(int idx);
    void endScope();
}
