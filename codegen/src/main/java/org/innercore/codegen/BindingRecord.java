package org.innercore.codegen;

import java.util.ArrayList;
import java.util.List;

/**
 * A single resolved binding, equivalent to one JSON line in the reference
 * {@code gen_java_bindings.txt} store. Produced by a resolver (Java here; a C++
 * resolver may contribute records too) and consumed by {@link JavaBindingGenerator}.
 */
public final class BindingRecord {
    /** "Module.name". */
    public String bind;
    /** Fully-qualified hand-written class (Java bindings); null for native records. */
    public String cls;
    /** Source member name. */
    public String src;
    /** One of the {@code FN_TYPE_*} constants. */
    public String typ;
    /** Whether a Java wrapper is needed (native pipeline); always false for Java. */
    public boolean wrap;
    public List<VarDecl> args = new ArrayList<VarDecl>();
    public VarDecl ret;
    public List<String> attrs = new ArrayList<String>();
    /** Mangled native name (native records only). */
    public String mgl;
    /** Originating C++ class (native records only). */
    public String cppCls;
}
