package org.innercore.codegen;

/** A variable / argument / return declaration in the internal type encoding. */
public final class VarDecl {
    public String type = "";
    public String name = "";
    /** Default value expression (used by the C++ pipeline; always empty for Java). */
    public String def = "";
    public boolean nullable = false;

    public VarDecl() {}

    public VarDecl(String type) {
        this.type = type;
    }

    public VarDecl copy() {
        VarDecl v = new VarDecl();
        v.type = this.type;
        v.name = this.name;
        v.def = this.def;
        v.nullable = this.nullable;
        return v;
    }
}
