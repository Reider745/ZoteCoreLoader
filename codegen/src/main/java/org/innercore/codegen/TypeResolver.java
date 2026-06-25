package org.innercore.codegen;

import java.util.ArrayList;
import java.util.List;

import static org.innercore.codegen.Encoding.*;

/**
 * Helpers that translate the internal type encoding into Java/JS forms.
 *
 * <p>Only the Java-relevant subset of the reference {@code TypeResolver} is ported.
 * The JNI/C++ helpers (get_jni_type, get_mangled_type_name, crit-native checks, ...)
 * are intentionally omitted; they belong with the future C++ resolver.
 */
public final class TypeResolver {
    /** Rhino types that are passed through without a jsToJava/javaToJS conversion. */
    public final List<String> javaScriptableClasses = new ArrayList<String>();

    public TypeResolver() {
        javaScriptableClasses.add("org.mozilla.javascript.Scriptable");
        javaScriptableClasses.add("org.mozilla.javascript.ScriptableObject");
        javaScriptableClasses.add("org.mozilla.javascript.NativeArray");
    }

    /** Decode an internal type into a Java type literal (e.g. {@code @a.b.C} -> {@code a.b.C}, {@code [int} -> {@code int[]}). */
    public String getJavaType(String t) {
        String suffix = "";
        while (t.startsWith(TYPE_PREFIX_LIST)) {
            suffix += "[]";
            t = t.substring(1);
        }
        if (t.startsWith(TYPE_PREFIX_PTR)) {
            t = t.substring(1);
        }
        if (t.startsWith(TYPE_PREFIX_JAVA_CLS)) {
            t = t.substring(1);
        }
        return t + suffix;
    }

    /** Pointers collapse to {@code long} on the Java side; everything else is unchanged. */
    public String getJavaToNativeType(String t) {
        if (t.startsWith(TYPE_PREFIX_PTR)) {
            return "long";
        }
        return t;
    }

    /** The coarse JS argument type used for overload discrimination. */
    public String getJavaJsArgType(String t) {
        assert !t.startsWith(TYPE_PREFIX_PTR);
        boolean isList = t.startsWith(TYPE_PREFIX_LIST);
        while (t.startsWith(TYPE_PREFIX_LIST)) {
            t = t.substring(1);
        }
        if (isList) {
            return "NativeArray";
        }
        if (t.startsWith(TYPE_PREFIX_JAVA_CLS)) {
            t = t.substring(1);
        }
        if (t.equals(BASE_TYPE_INT) || t.equals(BASE_TYPE_LONG)
                || t.equals(BASE_TYPE_FLOAT) || t.equals(BASE_TYPE_DOUBLE)) {
            return "Number";
        }
        return t;
    }

    /** Whether the (result, args) signature qualifies for {@code @CriticalNative} (always false while disabled). */
    public boolean isCritNativeSignature(VarDecl resultType, List<VarDecl> argsDecl) {
        if (!SUPPORT_CRIT_NATIVE) {
            return false;
        }
        if (resultType.type.equals(BASE_TYPE_STRING)) {
            return false;
        }
        for (VarDecl arg : argsDecl) {
            if (arg.type.equals(BASE_TYPE_STRING)) {
                return false;
            }
        }
        return true;
    }
}
