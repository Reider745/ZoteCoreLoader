package org.innercore.codegen;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Type-encoding constants and codegen feature flags, ported from the reference
 * Python codegen. The internal type encoding is a small string DSL:
 * <ul>
 *   <li>primitives: {@code void/boolean/int/long/float/double}</li>
 *   <li>{@code String}</li>
 *   <li>{@code @<fully.qualified.Name>} for Java class types</li>
 *   <li>{@code [} prefix, one per array dimension</li>
 *   <li>{@code *} prefix for C++ pointers (reserved; unused in the Java-only pipeline)</li>
 * </ul>
 */
public final class Encoding {
    private Encoding() {}

    // Feature flags (mirror the reference module-level constants).
    public static final boolean SUPPORT_CRIT_NATIVE = false;
    public static final boolean ALLOW_CROSSTYPE_RESOLUTIONS = true;
    public static final boolean ALLOW_COERCE_TO_NUMBER = true;

    public static final String TYPE_PREFIX_PTR = "*";
    public static final String TYPE_PREFIX_LIST = "[";
    public static final String TYPE_PREFIX_JAVA_CLS = "@";

    public static final String BASE_TYPE_VOID = "void";
    public static final String BASE_TYPE_BOOL = "boolean";
    public static final String BASE_TYPE_INT = "int";
    public static final String BASE_TYPE_LONG = "long";
    public static final String BASE_TYPE_FLOAT = "float";
    public static final String BASE_TYPE_DOUBLE = "double";
    public static final String BASE_TYPE_STRING = "String";

    public static final List<String> BASE_PRIMITIVE_TYPES = Arrays.asList(
            BASE_TYPE_BOOL, BASE_TYPE_INT, BASE_TYPE_LONG, BASE_TYPE_FLOAT, BASE_TYPE_DOUBLE);

    // Binding "function" kinds.
    public static final String FN_TYPE_STATIC_METHOD = "static-method";
    public static final String FN_TYPE_METHOD = "method";
    public static final String FN_TYPE_STATIC_FIELD = "static-field";
    public static final String FN_TYPE_FIELD = "field";
    public static final String FN_TYPE_CONSTRUCTOR = "constructor";
    public static final String FN_TYPE_REF_CNT = "refcnt";
    public static final String FN_TYPE_JAVA_GET_POINTER = "java-get-ptr";

    /** Types treated as already-imported when resolving simple type names. */
    public static final Set<String> DEFAULT_JAVA_IMPORTS = new LinkedHashSet<String>(Arrays.asList(
            "java.lang.Object",
            "java.lang.Class",
            "java.lang.String",
            "java.lang.Integer",
            "java.lang.Long",
            "java.lang.Float",
            "java.lang.Double",
            "java.lang.Number",
            "java.lang.Boolean",
            "java.lang.Character",
            "java.lang.Runnable",
            "java.lang.CharSequence",
            "org.mozilla.javascript.Scriptable",
            "org.mozilla.javascript.ScriptableObject",
            "org.mozilla.javascript.NativeArray"));

    public static boolean isFieldType(String fnType) {
        return FN_TYPE_FIELD.equals(fnType) || FN_TYPE_STATIC_FIELD.equals(fnType);
    }

    public static String repeat(String s, int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            sb.append(s);
        }
        return sb.toString();
    }
}
