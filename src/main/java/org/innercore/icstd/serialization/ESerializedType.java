package org.innercore.icstd.serialization;

public enum ESerializedType {
    INVALID(false, false),
    INT(true, false),
    BYTE(true, false),
    SHORT(true, false),
    LONG(true, false),
    FLOAT(true, false),
    DOUBLE(true, false),
    STRING(false, false),
    BOOLEAN(false, false),

    EMPTY_SCOPE(false, true),
    OBJECT_SCOPE(false, true),
    ARRAY_SCOPE(false, true),
    ;

    private final boolean isNumber;
    private final boolean isScope;

    ESerializedType(boolean isNumber, boolean isScope) {
        this.isNumber = isNumber;
        this.isScope = isScope;
    }

    public boolean isInvalid() { return this == INVALID; }
    public boolean isNumber() { return isNumber; }
    public boolean isString() { return this == STRING; }
    public boolean isInt() { return this == INT; }
    public boolean isLong() { return this == LONG; }
    public boolean isFloat() { return this == FLOAT; }
    public boolean isDouble() { return this == DOUBLE; }
    public boolean isShort() { return this == SHORT; }
    public boolean isByte() { return this == BYTE; }
    public boolean isIntegerNumber() { return this == INT || this == LONG || this == BYTE || this == SHORT; }
    public boolean isFloatNumber() { return this == FLOAT || this == DOUBLE; }
    public boolean isBoolean() { return this == BOOLEAN; }

    public boolean isScope() { return isScope; }
    public boolean isEmptyScope() { return this == EMPTY_SCOPE; }
    public boolean isObject() { return this == OBJECT_SCOPE; }
    public boolean isArray() { return this == ARRAY_SCOPE; }
    public boolean isNonEmptyScope() { return isScope && this != EMPTY_SCOPE; }
}
