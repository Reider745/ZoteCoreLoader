package org.innercore.codegen;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.innercore.codegen.Encoding.*;

/**
 * Consumes the {@link BindingStore} and emits, per module, a
 * {@code <Module>BindGenBase.java} class plus a shared {@code ApiModuleMap.java}.
 *
 * <p>Faithful port of the reference {@code JavaBindingGenerator}. The native-record
 * code paths are preserved so that, once a C++ resolver contributes native records,
 * generation works unchanged; the Java-only pipeline simply never exercises them.
 */
public final class JavaBindingGenerator {
    private static final String IND = "    ";

    private final BindingStore store;
    private final TypeResolver typeResolver = new TypeResolver();
    private final java.util.Set<String> typesWithCustomPointerGetter = new java.util.HashSet<String>();

    private CodeWriter w = new CodeWriter();
    private boolean prepareOk = true;
    private Map<String, String> cppTypeToJava = new LinkedHashMap<String, String>();

    public JavaBindingGenerator(BindingStore store) {
        this.store = store;
    }

    // --- holders -------------------------------------------------------------

    private static final class ModuleData {
        final Map<String, List<BindingRecord>> bindings = new LinkedHashMap<String, List<BindingRecord>>();
        final Map<String, String> cppCls = new LinkedHashMap<String, String>();
        String cppClsResolved = null;
    }

    private static final class Prepared {
        final String className;
        Map<String, List<BindingOverload>> bindings;

        Prepared(String className, Map<String, List<BindingOverload>> bindings) {
            this.className = className;
            this.bindings = bindings;
        }
    }

    private static final class JavaBindingClassRequirements {
        boolean needsPointerField = false;
        boolean needsRefCount = false;
        boolean needsChangePointerMethod = false;
    }

    /** (overload, argIdx, subResolutions); empty subs == leaf call. */
    private static final class Resolution {
        final BindingOverload overload;
        final int argIdx;
        final List<Resolution> subs;

        Resolution(BindingOverload overload, int argIdx, List<Resolution> subs) {
            this.overload = overload;
            this.argIdx = argIdx;
            this.subs = subs;
        }
    }

    private static final class ResolveResult {
        final Resolution res;
        final boolean ok;

        ResolveResult(Resolution res, boolean ok) {
            this.res = res;
            this.ok = ok;
        }
    }

    // --- entry point ---------------------------------------------------------

    public boolean generate(File outputDir, String outputPackage) {
        Map<String, ModuleData> modulesData = loadBindingsData();
        if (modulesData.isEmpty()) {
            System.out.println("no binding data to generate java classes, skipping codegen");
            return true;
        }
        boolean allOk = true;

        // All module classes are themselves scriptable.
        for (String moduleCls : store.javaModules.values()) {
            typeResolver.javaScriptableClasses.add(moduleCls);
        }

        Map<String, Prepared> prepared = prepareBindingOverloads(modulesData);
        allOk = allOk && prepareOk;

        for (Prepared info : prepared.values()) {
            info.bindings = generateDefaultedOverloads(info.bindings);
        }

        Map<String, String> moduleMap = new LinkedHashMap<String, String>();
        for (Map.Entry<String, Prepared> entry : prepared.entrySet()) {
            String moduleName = entry.getKey();
            Prepared info = entry.getValue();
            String genClassName = info.className;

            if (!store.javaModules.containsKey(moduleName)) {
                allOk = false;
                System.out.println("Module " + moduleName + " has no associated java class! "
                        + "You should create java class with @ApiBinding(\"" + moduleName + "\") to proceed.");
                continue;
            }

            w = new CodeWriter();
            try {
                if (generateJavaClass(outputPackage, moduleName, genClassName, info.bindings)) {
                    System.out.println("codegen done for " + genClassName + ".java");
                } else {
                    System.out.println("codegen failed for " + genClassName + ".java, see errors above");
                    allOk = false;
                }
                moduleMap.put(moduleName, store.javaModules.get(moduleName));
                writeFile(outputDir, genClassName + ".java");
            } catch (RuntimeException ex) {
                throw new RuntimeException("codegen failed for " + genClassName + ".java", ex);
            }
        }

        if (!moduleMap.isEmpty()) {
            w = new CodeWriter();
            generateModuleMap(outputPackage, moduleMap, !allOk);
            writeFile(outputDir, "ApiModuleMap.java");
        }

        return allOk;
    }

    private void writeFile(File outputDir, String name) {
        File target = new File(outputDir, name);
        try {
            File parent = target.getParentFile();
            if (parent != null) {
                parent.mkdirs();
            }
            Files.write(target.toPath(), w.text().getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException("failed to write " + target, e);
        }
    }

    // --- writer shims --------------------------------------------------------

    private void writeln(String... lines) {
        w.writeln(lines);
    }

    private void writelnI(int indent, String... lines) {
        w.writeln(indent, lines);
    }

    private void write(String... parts) {
        w.write(parts);
    }

    private void writeI(int indent, String... parts) {
        w.write(indent, parts);
    }

    // --- data loading --------------------------------------------------------

    private static String[] splitBind(String bind) {
        int dot = bind.indexOf('.');
        return new String[]{bind.substring(0, dot), bind.substring(dot + 1)};
    }

    private Map<String, ModuleData> loadBindingsData() {
        Map<String, ModuleData> modules = new LinkedHashMap<String, ModuleData>();
        for (BindingRecord rec : store.records) {
            String[] parts = splitBind(rec.bind);
            String bindModule = parts[0];
            String bindName = parts[1];

            ModuleData md = modules.get(bindModule);
            if (md == null) {
                md = new ModuleData();
                modules.put(bindModule, md);
            }
            if (rec.cppCls != null && !rec.cppCls.isEmpty()) {
                md.cppCls.put(rec.cppCls, bindModule + "." + bindName);
            }
            List<BindingRecord> list = md.bindings.get(bindName);
            if (list == null) {
                list = new ArrayList<BindingRecord>();
                md.bindings.put(bindName, list);
            }
            list.add(rec);
        }

        // Ensure every module declared via a class-level @ApiBinding gets a (possibly empty)
        // base class, even when it has no bound members yet. The reference relied on the C++
        // pass to supply records for such native-driven modules (e.g. Game, Dimensions); in the
        // Java-only pipeline an empty base is still required so the hand-written module class,
        // which extends it, compiles. Once a C++ resolver contributes records, these fill in.
        for (String moduleName : store.javaModules.keySet()) {
            if (!modules.containsKey(moduleName)) {
                modules.put(moduleName, new ModuleData());
            }
        }

        for (Map.Entry<String, ModuleData> e : modules.entrySet()) {
            ModuleData md = e.getValue();
            if (md.cppCls.size() > 1) {
                System.out.println("module '" + e.getKey() + "' bindings declare different CPP classes:");
                for (Map.Entry<String, String> c : md.cppCls.entrySet()) {
                    System.out.println("  " + c.getValue() + " declared " + c.getKey());
                }
            }
            md.cppClsResolved = md.cppCls.isEmpty() ? null : md.cppCls.keySet().iterator().next();
        }
        return modules;
    }

    // --- overload preparation ------------------------------------------------

    private VarDecl deserializeAndCvtType(VarDecl source) {
        VarDecl decl = source.copy();
        if (decl.type.startsWith(TYPE_PREFIX_PTR)) {
            String typ = decl.type.substring(1);
            if (cppTypeToJava.containsKey(typ)) {
                decl.type = TYPE_PREFIX_PTR + cppTypeToJava.get(typ);
            } else {
                System.out.println("failed to resolve CPP type: " + typ);
                decl.type = TYPE_PREFIX_PTR + "CppTypeWasNotRecognized_" + typ;
                prepareOk = false;
            }
        }
        return decl;
    }

    private Map<String, Prepared> prepareBindingOverloads(Map<String, ModuleData> modules) {
        prepareOk = true;
        Map<String, Prepared> prepared = new LinkedHashMap<String, Prepared>();
        cppTypeToJava = new LinkedHashMap<String, String>();
        for (Map.Entry<String, ModuleData> e : modules.entrySet()) {
            if (e.getValue().cppClsResolved != null) {
                cppTypeToJava.put(e.getValue().cppClsResolved, store.javaModules.get(e.getKey()));
            }
        }

        for (Map.Entry<String, ModuleData> me : modules.entrySet()) {
            String moduleName = me.getKey();
            ModuleData module = me.getValue();
            String genClassName = moduleName + "BindGenBase";

            Map<String, List<BindingOverload>> bindings = new LinkedHashMap<String, List<BindingOverload>>();
            for (Map.Entry<String, List<BindingRecord>> be : module.bindings.entrySet()) {
                String name = be.getKey();
                List<BindingOverload> resolvedOverloads = new ArrayList<BindingOverload>();

                for (BindingRecord oj : be.getValue()) {
                    BindingOverload o = new BindingOverload();
                    o.name = name;
                    o.moduleName = moduleName;
                    o.bindType = "native";
                    o.moduleClsName = store.javaModules.get(moduleName);
                    o.callSrc = name;
                    o.ret = deserializeAndCvtType(oj.ret);
                    o.fnType = oj.typ;
                    o.needJavaWrap = oj.wrap;
                    o.mangledName = oj.mgl != null ? oj.mgl : o.name;

                    boolean mismatchedResolution = false;
                    for (BindingOverload included : resolvedOverloads) {
                        if (o.fnType.equals(included.fnType)) {
                            continue;
                        }
                        String prefixMessage = "found";
                        boolean oField = isFieldType(o.fnType);
                        boolean incField = isFieldType(included.fnType);
                        if (oField != incField) {
                            prefixMessage = "failed to include";
                            mismatchedResolution = true;
                        }
                        if (!ALLOW_CROSSTYPE_RESOLUTIONS || !prefixMessage.equals("found")) {
                            System.out.println(prefixMessage + " mismatched type resolution for "
                                    + moduleName + "." + o.name + " " + o.bindType + " " + o.fnType
                                    + " (got " + included.fnType + ", but here is " + o.fnType + ")");
                        }
                        break;
                    }
                    if (mismatchedResolution) {
                        continue;
                    }

                    if (!isFieldType(o.fnType)) {
                        o.args = new ArrayList<VarDecl>();
                        for (VarDecl arg : oj.args) {
                            o.args.add(deserializeAndCvtType(arg));
                        }
                        o.argsCnt = o.args.size();
                    }
                    if (oj.src != null) {
                        o.callSrc = oj.src;
                    }
                    if (oj.cls != null) {
                        o.bindType = "java";
                        o.moduleClsName = oj.cls;
                    }
                    if (oj.attrs != null) {
                        o.attrs = oj.attrs;
                    }
                    if ((o.fnType.equals(FN_TYPE_STATIC_METHOD) || o.fnType.equals(FN_TYPE_STATIC_FIELD))
                            && o.bindType.equals("java")) {
                        o.callSrc = oj.cls + "." + o.callSrc;
                    }
                    if (o.fnType.equals(FN_TYPE_JAVA_GET_POINTER)) {
                        typesWithCustomPointerGetter.add(store.javaModules.get(moduleName));
                    }
                    resolvedOverloads.add(o);
                }
                bindings.put(name, resolvedOverloads);
            }
            prepared.put(moduleName, new Prepared(genClassName, bindings));
        }
        return prepared;
    }

    private Map<String, List<BindingOverload>> generateDefaultedOverloads(
            Map<String, List<BindingOverload>> bindings) {
        for (Map.Entry<String, List<BindingOverload>> e : bindings.entrySet()) {
            String name = e.getKey();
            List<BindingOverload> snapshot = new ArrayList<BindingOverload>(e.getValue());
            for (BindingOverload o : snapshot) {
                for (int i = 0; i < o.argsCnt; i++) {
                    if (o.args.get(o.args.size() - 1 - i).def.isEmpty()) {
                        break;
                    }
                    BindingOverload doo = new BindingOverload();
                    doo.name = o.name;
                    doo.moduleClsName = o.moduleClsName;
                    doo.bindType = "defaulted-overload";
                    doo.fnType = o.fnType;
                    doo.callSrc = o.callSrc;
                    doo.args = o.args;
                    doo.argsCnt = o.argsCnt - 1 - i;
                    doo.ret = o.ret;
                    doo.needJavaWrap = false;
                    doo.attrs = o.attrs;
                    bindings.get(name).add(doo);
                }
            }
        }
        return bindings;
    }

    private JavaBindingClassRequirements analyzeClassRequirements(
            Map<String, List<BindingOverload>> bindings) {
        JavaBindingClassRequirements req = new JavaBindingClassRequirements();
        outer:
        for (List<BindingOverload> allOverloads : bindings.values()) {
            for (BindingOverload overload : allOverloads) {
                if (!req.needsPointerField) {
                    if (overload.attrs.contains("native") && !overload.bindType.equals("defaulted-overload")
                            && (overload.fnType.equals(FN_TYPE_CONSTRUCTOR)
                            || overload.fnType.equals(FN_TYPE_REF_CNT)
                            || overload.fnType.equals(FN_TYPE_METHOD))) {
                        req.needsPointerField = true;
                    }
                }
                if (!req.needsRefCount) {
                    if (overload.fnType.equals(FN_TYPE_REF_CNT)) {
                        req.needsRefCount = true;
                    }
                }
                if (req.needsPointerField && req.needsRefCount) {
                    break outer;
                }
            }
        }
        req.needsChangePointerMethod = req.needsPointerField;
        return req;
    }

    // --- class skeleton ------------------------------------------------------

    private void writeClassHeader(String pkg, String genClassName, JavaBindingClassRequirements req) {
        writeln("package " + pkg + ";");
        writeln();
        writeln(
                "import org.mozilla.javascript.*;",
                "import com.zhekasmirnov.innercore.api.mod.util.ScriptableAccessorImpl;",
                "import com.zhekasmirnov.innercore.api.mod.util.ScriptableFunctionImpl;",
                "import com.zhekasmirnov.innercore.api.mod.util.ScriptableModuleImpl;",
                "import com.zhekasmirnov.innercore.api.mod.util.ScriptableModuleInstanceImpl;",
                "import com.zhekasmirnov.innercore.api.mod.ScriptableObjectHelper;");
        writeln();
        writeln("import dalvik.annotation.optimization.FastNative;");
        if (SUPPORT_CRIT_NATIVE) {
            writeln("import dalvik.annotation.optimization.CriticalNative;");
        }
        writeln();
    }

    private void writeClassDefinition(String moduleName, String genClassName,
                                      JavaBindingClassRequirements req) {
        writeln("public class " + genClassName + " extends ScriptableModuleInstanceImpl {");
        writelnI(1,
                "@Override public String getClassName() { return \"" + moduleName + "\"; }",
                "@Override public String toString() { return \"[object " + moduleName + "]\"; }",
                "@Override public Object getDefaultValue(Class<?> typeHint) { return typeHint == ScriptRuntime.StringClass ? toString() : this; }");
    }

    private void writeConstructorAndFields(String moduleName, String genClassName,
                                           JavaBindingClassRequirements req) {
        if (req.needsPointerField) {
            writeln();
            writelnI(1, "public long pointer;");
            writelnI(1, "protected " + genClassName + " (long ptr, int refCntChange, Object stub) {");
            writelnI(2, "this.pointer = ptr;");
            writelnI(2, "this.setPrototype(JS_PROTO);");
            if (req.needsRefCount) {
                writelnI(2, "if (refCntChange != 0) nativeChangeRefCnt(ptr, refCntChange);");
            }
            writelnI(1, "}");
            writelnI(1, "public long getPointer() { return this.pointer; }");
            String javaModuleCls = store.javaModules.get(moduleName);
            writelnI(1, "public static " + javaModuleCls + " fromNativePtrIncRef(long ptr) { return ptr != 0 ? new "
                    + javaModuleCls + "(ptr, 1, null) : null; }");
            writelnI(1, "public static " + javaModuleCls + " fromNativePtrNoIncRefUnsafe(long ptr) { return ptr != 0 ? new "
                    + javaModuleCls + "(ptr, 0, null) : null; }");

            if (req.needsChangePointerMethod) {
                writeI(1, "protected void setNewPointer(long ptr) {");
                write(req.needsRefCount ? "\n" : " ");
                if (req.needsRefCount) {
                    writelnI(2,
                            "if (this.pointer == ptr) return;",
                            "if (this.pointer != 0) nativeChangeRefCnt(this.pointer, -1);");
                }
                writeI(req.needsRefCount ? 2 : 0, "this.pointer = ptr;");
                write(req.needsRefCount ? "\n" : " ");
                if (req.needsRefCount) {
                    writelnI(2, "if (this.pointer != 0) nativeChangeRefCnt(this.pointer, 1);");
                }
                writelnI(req.needsRefCount ? 1 : 0, "}");
            }
        } else {
            writeln();
            writelnI(1, "protected " + genClassName + "() { super(); setPrototype(JS_PROTO); }");
        }
    }

    // --- visibility / native helpers -----------------------------------------

    private String getMethodVisibility(BindingOverload overload) {
        for (String attribute : overload.attrs) {
            if (attribute.equals("private") || attribute.equals("protected") || attribute.equals("public")) {
                return attribute + " ";
            }
            if (attribute.equals("package-private")) {
                return "";
            }
        }
        if (overload.bindType.equals("native")) {
            if (overload.fnType.equals(FN_TYPE_CONSTRUCTOR)) {
                return "private ";
            }
            if (overload.fnType.equals(FN_TYPE_REF_CNT)) {
                return "protected ";
            }
        }
        return "public ";
    }

    private String getNativeAnnotation(BindingOverload overload) {
        if (typeResolver.isCritNativeSignature(overload.ret, overload.args)) {
            return "@CriticalNative";
        }
        return "@FastNative";
    }

    private List<String> getOverloadNativeCallArgs(BindingOverload overload) {
        List<String> callArgs = new ArrayList<String>();
        for (VarDecl arg : overload.args) {
            if (arg.type.startsWith(TYPE_PREFIX_PTR)) {
                String getPtr = typesWithCustomPointerGetter.contains(arg.type.substring(1))
                        ? "getPointer()" : "pointer";
                if (arg.nullable) {
                    callArgs.add(arg.name + " == null ? 0 : " + arg.name + "." + getPtr);
                } else {
                    callArgs.add(arg.name + "." + getPtr);
                }
            } else {
                callArgs.add(arg.name);
            }
        }
        return callArgs;
    }

    private void generateNativeMethod(BindingOverload overload) {
        String visibility = getMethodVisibility(overload);
        String nativeBindingName = overload.mangledName;
        if (overload.needJavaWrap) {
            visibility = "private ";
        }

        List<VarDecl> nativeArgs = overload.args;
        List<String> callArgs = getOverloadNativeCallArgs(overload);
        if (overload.fnType.equals(FN_TYPE_METHOD) || overload.fnType.equals(FN_TYPE_REF_CNT)) {
            List<VarDecl> withSelf = new ArrayList<VarDecl>();
            VarDecl ptr = new VarDecl(BASE_TYPE_LONG);
            ptr.name = "pointer";
            withSelf.add(ptr);
            withSelf.addAll(overload.args);
            nativeArgs = withSelf;
            callArgs.add(0, "this.pointer");
        }

        writeI(1, getNativeAnnotation(overload) + " " + visibility + "static native "
                + typeResolver.getJavaToNativeType(overload.ret.type) + " " + nativeBindingName);
        writeln("(" + joinArgs(nativeArgs, true) + ");");

        if (overload.needJavaWrap) {
            generateWrapMethod(overload, overload.name, nativeBindingName, callArgs);
        }
    }

    private void generateWrapMethod(BindingOverload overload, String wrapName, String callName,
                                    List<String> callArgs) {
        String visibility = getMethodVisibility(overload);
        if (overload.fnType.equals(FN_TYPE_METHOD)) {
            writeI(1, visibility);
        } else {
            writeI(1, visibility + "static ");
        }
        write(typeResolver.getJavaType(overload.ret.type) + " " + wrapName);
        write("(" + joinJavaArgs(overload.args.subList(0, overload.argsCnt)) + ")");
        write(" { ");
        String callCode = callName + "(" + join(callArgs, ", ") + ")";
        if (!overload.ret.type.equals(BASE_TYPE_VOID)) {
            write("return ");
        }
        if (overload.ret.type.startsWith(TYPE_PREFIX_PTR)) {
            write(typeResolver.getJavaType(overload.ret.type) + ".fromNativePtrNoIncRefUnsafe(" + callCode + ");");
        } else {
            write(callCode + ";");
        }
        writeln(" }");
    }

    private void generateDefaultedOverloadMethod(BindingOverload overload) {
        if (!overload.bindType.equals("defaulted-overload")) {
            return;
        }
        List<String> callArgs = new ArrayList<String>();
        for (int i = 0; i < overload.args.size(); i++) {
            if (i < overload.argsCnt) {
                callArgs.add(overload.args.get(i).name);
            } else {
                callArgs.add(overload.args.get(i).def);
            }
        }

        generateWrapMethod(overload, overload.name, overload.callSrc, callArgs);
    }

    private void generateConstructor(String genClassName, BindingOverload overload) {
        if (!overload.fnType.equals(FN_TYPE_CONSTRUCTOR)) {
            return;
        }
        List<String> callArgs = getOverloadNativeCallArgs(overload);
        writelnI(1, "protected " + genClassName + "(" + joinJavaArgs(overload.args) + ") {");
        writelnI(2,
                "this.pointer = nativeConstruct(" + join(callArgs, ", ") + ");",
                "setPrototype(JS_PROTO);");
        writelnI(1, "}");
    }

    private void generateFinalizer() {
        writelnI(1, "@Override protected void finalize() throws Throwable {");
        writelnI(2,
                "super.finalize();",
                "if (this.pointer != 0) nativeChangeRefCnt(this.pointer, -1);");
        writelnI(1, "}");
    }

    // --- JS binding generation ------------------------------------------------

    private boolean generateJsMethodBinding(String moduleName, String bindName,
                                            List<BindingOverload> allOverloads) {
        int indentCnt = 2;
        writelnI(indentCnt, "if (allowOverrides || !curObj.has(\"" + bindName + "\", curObj)) {");
        indentCnt += 1;
        writeI(indentCnt, "curObj.put(\"" + bindName + "\", curObj, ");
        writeln("new ScriptableFunctionImpl() { @Override public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {");
        indentCnt += 1;
        boolean ok = generateJsBindingCode(moduleName, bindName, allOverloads, indentCnt);
        indentCnt -= 1;
        writelnI(indentCnt, "}});");
        indentCnt -= 1;
        writelnI(indentCnt, "}");
        return ok;
    }

    private boolean generateJsConstructorBinding(String moduleName, String bindName,
                                                 List<BindingOverload> allOverloads) {
        int indentCnt = 3;
        writelnI(indentCnt, "@Override public Scriptable construct(Context cx, Scriptable scope, Object[] args) {");
        indentCnt += 1;
        boolean ok = generateJsBindingCode(moduleName, bindName, allOverloads, indentCnt);
        indentCnt -= 1;
        writelnI(indentCnt, "}");
        return ok;
    }

    private boolean generateJsBindingCode(String moduleName, String bindName,
                                          List<BindingOverload> allOverloads, int indentCnt) {
        boolean ok = true;
        Map<Integer, List<BindingOverload>> overloadsByArgsCnt = new LinkedHashMap<Integer, List<BindingOverload>>();
        int maxArgsCnt = -1;
        int minArgsCnt = 256;
        int lastNullableArgPos = -1;

        for (BindingOverload o : allOverloads) {
            minArgsCnt = Math.min(minArgsCnt, o.argsCnt);
            maxArgsCnt = Math.max(maxArgsCnt, o.argsCnt);
            List<BindingOverload> bucket = overloadsByArgsCnt.get(o.argsCnt);
            if (bucket == null) {
                bucket = new ArrayList<BindingOverload>();
                overloadsByArgsCnt.put(o.argsCnt, bucket);
            }
            for (int index = 0; index < o.args.size(); index++) {
                if (o.args.get(index).nullable) {
                    lastNullableArgPos = Math.max(index, lastNullableArgPos);
                }
            }
            bucket.add(o);
        }

        boolean addReturnUndefined = overloadsByArgsCnt.isEmpty();
        for (List<BindingOverload> overloads : overloadsByArgsCnt.values()) {
            for (BindingOverload o : overloads) {
                if (o.ret.type.equals(BASE_TYPE_VOID)) {
                    addReturnUndefined = true;
                    break;
                }
            }
            if (addReturnUndefined) {
                break;
            }
        }

        String lengthAttribute = "args.length";
        int startStripNullArgsAt = Math.max(minArgsCnt, lastNullableArgPos + 1);
        if (startStripNullArgsAt < maxArgsCnt) {
            writelnI(indentCnt,
                    "int argCount = Math.min(" + lengthAttribute + ", " + maxArgsCnt + ");",
                    "for (; argCount - 1 >= " + startStripNullArgsAt
                            + " && (args[argCount - 1] == null || args[argCount - 1] == Undefined.instance); argCount--);");
            lengthAttribute = "argCount";
        } else if (minArgsCnt > 0) {
            writeI(indentCnt, "if (" + lengthAttribute + " < " + minArgsCnt + ") ");
            writeln("throw new IllegalArgumentException(\"" + moduleName + "." + bindName
                    + ": too few arguments (required=" + minArgsCnt + " got=\" + args.length + \")\");");
        }

        boolean afterElse = false;
        for (int argCnt = maxArgsCnt; argCnt >= 0; argCnt--) {
            if (!overloadsByArgsCnt.containsKey(argCnt)) {
                continue;
            }
            List<BindingOverload> overloads = overloadsByArgsCnt.get(argCnt);

            if (!afterElse) {
                writeI(indentCnt);
            }
            if (minArgsCnt != maxArgsCnt) {
                write("if (" + lengthAttribute + " >= " + argCnt + ") ");
            }

            ResolveResult resolved = resolveOverloads(overloads, argCnt, moduleName + "." + bindName);
            if (!resolved.ok) {
                ok = false;
            }

            if (minArgsCnt != maxArgsCnt) {
                writeln("{ /* overloads for " + argCnt + " args */");
                indentCnt += 1;
            }

            if (argCnt > 0) {
                if (minArgsCnt != maxArgsCnt) {
                    writeI(indentCnt, "");
                }
                for (int argIdx = 0; argIdx < argCnt; argIdx++) {
                    write("Object argVal" + argIdx + " = args[" + argIdx + "];");
                }
                write("\n");
                writeI(indentCnt, "");
            }

            if (!generateOverloadResolutionRecursive(resolved.res, moduleName + "." + bindName, indentCnt)) {
                ok = false;
            }

            if (minArgsCnt != maxArgsCnt) {
                indentCnt -= 1;
                writelnI(indentCnt, "}");
            }

            if (minArgsCnt != maxArgsCnt) {
                writeI(indentCnt, "else ");
                afterElse = true;
            }
        }

        if (afterElse) {
            writeln("throw new IllegalArgumentException(\"" + moduleName + "." + bindName
                    + ": can't find overload with \" + args.length + \" arguments\");");
        }
        if (addReturnUndefined) {
            writelnI(indentCnt, "return Undefined.instance;");
        }
        return ok;
    }

    private boolean doOverloadArgTypesAlias(String t1, String t2) {
        if (t1.equals(t2) || (t1.charAt(0) == '!' && t2.charAt(0) == '!')) {
            return true;
        }
        if ((t1.charAt(0) == '!' && t1.substring(1).equals(t2))
                || (t2.charAt(0) == '!' && t2.substring(1).equals(t1))) {
            return true;
        }
        return false;
    }

    private ResolveResult resolveOverloadsRecursive(List<BindingOverload> overloads,
                                                    List<List<String>> overloadsArgs,
                                                    List<Integer> argIndices) {
        if (overloads.size() == 1) {
            return new ResolveResult(new Resolution(overloads.get(0), -1, new ArrayList<Resolution>()), true);
        }
        if (argIndices.isEmpty()) {
            return new ResolveResult(new Resolution(overloads.get(0), -1, new ArrayList<Resolution>()), false);
        }

        int lastIndex = argIndices.get(argIndices.size() - 1);
        List<List<String>> overloadUniqueArgs = new ArrayList<List<String>>();
        for (int i = 0; i <= lastIndex; i++) {
            overloadUniqueArgs.add(new ArrayList<String>());
        }
        for (int argIndex : argIndices) {
            List<String> uniqueTypes = overloadUniqueArgs.get(argIndex);
            for (List<String> args : overloadsArgs) {
                String argType = args.get(argIndex);
                boolean isUnique = true;
                for (String uniqueArgType : uniqueTypes) {
                    if (doOverloadArgTypesAlias(argType, uniqueArgType)) {
                        isUnique = false;
                        break;
                    }
                }
                if (isUnique) {
                    uniqueTypes.add(argType);
                }
            }
        }

        int bestArgIdx = argIndices.get(0);
        for (int argIndex : argIndices) {
            if (overloadUniqueArgs.get(bestArgIdx).size() < overloadUniqueArgs.get(argIndex).size()) {
                bestArgIdx = argIndex;
            }
        }

        if (overloadUniqueArgs.get(bestArgIdx).size() == 1) {
            System.out.println("resolution failed");
            System.out.println("     " + overloadsArgs);
            System.out.println("     " + overloadUniqueArgs);
            return new ResolveResult(new Resolution(overloads.get(0), -1, new ArrayList<Resolution>()), false);
        }

        boolean allOk = true;
        List<Resolution> overloadResolution = new ArrayList<Resolution>();
        List<Integer> nextArgIndices = new ArrayList<Integer>();
        for (int argIndex : argIndices) {
            if (argIndex != bestArgIdx) {
                nextArgIndices.add(argIndex);
            }
        }
        for (String uniqueArg : overloadUniqueArgs.get(bestArgIdx)) {
            List<BindingOverload> overloadsForType = new ArrayList<BindingOverload>();
            List<List<String>> overloadsArgsForType = new ArrayList<List<String>>();
            for (int i = 0; i < overloads.size(); i++) {
                if (doOverloadArgTypesAlias(overloadsArgs.get(i).get(bestArgIdx), uniqueArg)) {
                    overloadsForType.add(overloads.get(i));
                    overloadsArgsForType.add(overloadsArgs.get(i));
                }
            }
            ResolveResult resolutionForType = resolveOverloadsRecursive(
                    overloadsForType, overloadsArgsForType, nextArgIndices);
            allOk = allOk && resolutionForType.ok;
            overloadResolution.add(resolutionForType.res);
        }
        return new ResolveResult(new Resolution(overloads.get(0), bestArgIdx, overloadResolution), allOk);
    }

    private ResolveResult resolveOverloads(List<BindingOverload> overloads, int argCnt,
                                           String diagnosticsPrefix) {
        if (overloads.size() <= 1) {
            return new ResolveResult(new Resolution(overloads.get(0), -1, new ArrayList<Resolution>()), true);
        }
        List<List<String>> overloadsArgs = new ArrayList<List<String>>();
        for (BindingOverload overload : overloads) {
            List<String> row = new ArrayList<String>();
            for (int i = 0; i < argCnt; i++) {
                VarDecl arg = overload.args.get(i);
                row.add((arg.nullable ? "!" : "") + typeResolver.getJavaJsArgType(arg.type));
            }
            overloadsArgs.add(row);
        }
        List<Integer> indices = new ArrayList<Integer>();
        for (int i = 0; i < argCnt; i++) {
            indices.add(i);
        }
        ResolveResult result = resolveOverloadsRecursive(overloads, overloadsArgs, indices);
        if (!result.ok) {
            System.out.println("failed to generate overload resolution for " + diagnosticsPrefix
                    + " with " + argCnt + " arguments:");
            for (BindingOverload resolution : overloads) {
                List<String> overloadArguments = new ArrayList<String>();
                for (int i = 0; i < argCnt; i++) {
                    VarDecl arg = resolution.args.get(i);
                    overloadArguments.add((arg.nullable ? "@Nullable " : "")
                            + typeResolver.getJavaJsArgType(arg.type));
                }
                List<String> methodArguments = new ArrayList<String>();
                for (VarDecl arg : resolution.args) {
                    methodArguments.add((arg.nullable ? "@Nullable " : "") + arg.type + " " + arg.name);
                }
                System.out.println("    (" + join(overloadArguments, ", ") + ") from "
                        + resolution.bindType + " " + resolution.fnType
                        + " (" + join(methodArguments, ", ") + ")");
            }
        }
        return result;
    }

    private boolean generateOverloadResolutionRecursive(Resolution resolution,
                                                        String fullBindingName, int indentCnt) {
        if (resolution.subs.isEmpty()) {
            return generateSingleOverloadCall(resolution.overload, indentCnt);
        }
        boolean ok = true;
        for (int k = 0; k < resolution.subs.size(); k++) {
            Resolution subResolution = resolution.subs.get(k);
            VarDecl resolutionArg = subResolution.overload.args.get(resolution.argIdx);
            writeln("if (" + getArgumentTypeCheck(resolutionArg, "argVal" + resolution.argIdx) + ") {");
            writeI(indentCnt + 1, "");
            if (!generateOverloadResolutionRecursive(subResolution, fullBindingName, indentCnt + 1)) {
                ok = false;
            }
            writeI(indentCnt, "}");
            if (k < resolution.subs.size() - 1) {
                write(" else ");
            }
        }
        write(" else ");
        writeln("throw new IllegalArgumentException(\"" + fullBindingName
                + ": can't find overload with \" + args.length + \" arguments\");");
        return ok;
    }

    private boolean generateSingleOverloadCall(BindingOverload o, int identCnt) {
        List<String> argsCode = new ArrayList<String>();
        for (int index = 0; index < o.argsCnt; index++) {
            argsCode.add(convertScriptableToJava(o.args.get(index), "argVal" + index, o));
        }

        String callSrc = o.callSrc;
        if (o.fnType.equals(FN_TYPE_METHOD)) {
            callSrc = "((" + o.moduleClsName + ") thisObj)." + o.callSrc;
        } else if (o.fnType.equals(FN_TYPE_CONSTRUCTOR)) {
            callSrc = "new " + o.moduleClsName;
        }

        List<String> commented = new ArrayList<String>();
        for (int i = 0; i < argsCode.size(); i++) {
            commented.add("/* " + o.args.get(i).name + " */ " + argsCode.get(i));
        }
        String callArgs = join(commented, ",\n" + repeat(IND, identCnt + 1));

        boolean requiresNewline = argsCode.size() > 1;
        if (requiresNewline) {
            callSrc = callSrc + "(\n" + repeat(IND, identCnt + 1) + callArgs + "\n" + repeat(IND, identCnt) + ")";
        } else {
            callSrc = callSrc + "(" + callArgs + ")";
        }

        if (!o.ret.type.equals(BASE_TYPE_VOID)) {
            write("return ");
        }
        callSrc = convertJavaToScriptable(o.ret, callSrc, "scope", "cx");
        writeln(callSrc + ";");
        return true;
    }

    private boolean generateJsFieldBinding(String moduleName, String bindName,
                                           List<BindingOverload> allOverloads) {
        boolean ok = true;
        int indentCnt = 2;
        writelnI(indentCnt, "if (allowOverrides || !curObj.has(\"" + bindName + "\", curObj)) {");
        indentCnt += 1;
        writelnI(indentCnt, "new ScriptableAccessorImpl() {");
        indentCnt += 1;

        for (BindingOverload overload : allOverloads) {
            writelnI(indentCnt, "@Override public boolean setValue(Object value, Scriptable owner, Scriptable start) {");
            indentCnt += 1;
            if (overload.attrs.contains("final")) {
                writelnI(indentCnt, "throw new UnsupportedOperationException(\"" + moduleName + "." + bindName
                        + " field can't be written to\");");
            } else {
                writelnI(indentCnt, "((" + overload.moduleClsName + ") start)." + overload.callSrc + " = "
                        + convertScriptableToJava(overload.ret, "value", overload) + ";");
                writelnI(indentCnt, "return true;");
            }
            indentCnt -= 1;
            writelnI(indentCnt, "}");

            writelnI(indentCnt, "@Override public Object getValue(Scriptable start) {");
            indentCnt += 1;
            writelnI(indentCnt, "return " + convertJavaToScriptable(overload.ret,
                    "((" + overload.moduleClsName + ") start)." + overload.callSrc,
                    "ScriptableObjectHelper.getDefaultScope()", "null") + ";");
            indentCnt -= 1;
            writelnI(indentCnt, "}");
        }

        indentCnt -= 1;
        writelnI(indentCnt, "}.defineSelfAsProperty((ScriptableObject) curObj, \"" + bindName + "\", 0);");
        indentCnt -= 1;
        writelnI(indentCnt, "}");
        return ok;
    }

    // --- conversions ---------------------------------------------------------

    private String convertJavaToScriptable(VarDecl arg, String expr, String scope, String ctx) {
        if (arg.type.startsWith(TYPE_PREFIX_JAVA_CLS) || arg.type.startsWith(TYPE_PREFIX_LIST)) {
            if (!typeResolver.javaScriptableClasses.contains(typeResolver.getJavaType(arg.type))) {
                expr = "Context.javaToJS(" + expr + ", " + scope + ", " + ctx + ")";
            }
        }
        return expr;
    }

    private String convertScriptableToJava(VarDecl arg, String expr, BindingOverload overload) {
        String nullCase = "null";
        if (!arg.nullable) {
            nullCase = "ScriptableObjectHelper.throwNotNull(\"" + arg.name + " parameter of "
                    + overload.moduleName + "." + overload.name + "\")";
        }
        if (arg.type.startsWith(TYPE_PREFIX_JAVA_CLS) || arg.type.startsWith(TYPE_PREFIX_LIST)
                || arg.type.startsWith(TYPE_PREFIX_PTR)) {
            String type = typeResolver.getJavaType(arg.type);
            if (typeResolver.javaScriptableClasses.contains(type)) {
                if (arg.nullable) {
                    return "(" + type + ") " + expr;
                } else {
                    return "(" + type + ") (" + expr + " != null && " + expr + " != Undefined.instance ? "
                            + expr + " : " + nullCase + ")";
                }
            } else {
                if (arg.nullable) {
                    return "(" + type + ") Context.jsToJava(" + expr + ", " + type + ".class)";
                } else {
                    return "(" + type + ") Context.jsToJava(" + expr + " != null && " + expr
                            + " != Undefined.instance ? " + expr + " : " + nullCase + ", " + type + ".class)";
                }
            }
        } else if (arg.type.equals(BASE_TYPE_LONG)) {
            return expr + " instanceof Wrapper ? (long) ((Wrapper) " + expr + ").unwrap() : ((Number) "
                    + expr + ").longValue()";
        } else if (arg.type.equals(BASE_TYPE_STRING)) {
            return expr + " instanceof CharSequence ? " + expr + ".toString() : (" + expr + " != null && " + expr
                    + " != Undefined.instance ? ScriptRuntime.toString(" + expr + ") : " + nullCase + ")";
        } else if (arg.type.equals(BASE_TYPE_BOOL)) {
            return expr + " instanceof Boolean ? (Boolean) " + expr + " : ScriptRuntime.toBoolean(" + expr + ")";
        } else {
            if (ALLOW_COERCE_TO_NUMBER) {
                String cap = Character.toUpperCase(arg.type.charAt(0)) + arg.type.substring(1);
                return expr + " instanceof Number ? ((Number) " + expr + ")." + arg.type + "Value() : "
                        + "ScriptableObjectHelper.convertTo" + cap + "(" + expr + ")";
            } else {
                return "((Number) " + expr + ")." + arg.type + "Value()";
            }
        }
    }

    private String getArgumentTypeCheck(VarDecl arg, String argStr) {
        String nullableCheck = arg.nullable
                ? " || " + argStr + " == null || " + argStr + " == Undefined.instance" : "";

        if (arg.type.startsWith(TYPE_PREFIX_JAVA_CLS) || arg.type.startsWith(TYPE_PREFIX_LIST)
                || arg.type.startsWith(TYPE_PREFIX_PTR)) {
            String type = typeResolver.getJavaType(arg.type);
            if (typeResolver.javaScriptableClasses.contains(type)) {
                return argStr + " instanceof " + type + nullableCheck;
            } else {
                return "(" + argStr + " instanceof Wrapper ? ScriptableObjectHelper.unwrap(" + argStr
                        + ") instanceof " + type + " : " + argStr + " instanceof " + type + ")" + nullableCheck;
            }
        } else if (arg.type.equals(BASE_TYPE_LONG)) {
            return argStr + " instanceof Wrapper || " + argStr + " instanceof Number";
        } else if (arg.type.equals(BASE_TYPE_STRING)) {
            return argStr + " instanceof CharSequence" + nullableCheck;
        } else if (arg.type.equals(BASE_TYPE_BOOL)) {
            return argStr + " instanceof Boolean";
        } else {
            return argStr + " instanceof Number";
        }
    }

    // --- class assembly ------------------------------------------------------

    private boolean generateJavaClass(String pkg, String moduleName, String genClassName,
                                      Map<String, List<BindingOverload>> bindings) {
        boolean ok = true;
        JavaBindingClassRequirements req = analyzeClassRequirements(bindings);

        writeClassHeader(pkg, genClassName, req);
        writeClassDefinition(moduleName, genClassName, req);
        writeConstructorAndFields(moduleName, genClassName, req);

        writeln();
        writelnI(1, "// native:");

        for (List<BindingOverload> allOverloads : bindings.values()) {
            for (BindingOverload overload : allOverloads) {
                if (overload.bindType.equals("native") || overload.bindType.equals("defaulted-overload")) {
                    if (overload.bindType.equals("native")) {
                        generateNativeMethod(overload);
                    } else {
                        generateDefaultedOverloadMethod(overload);
                    }
                    if (overload.fnType.equals(FN_TYPE_CONSTRUCTOR)) {
                        generateConstructor(genClassName, overload);
                    }
                    if (overload.fnType.equals(FN_TYPE_REF_CNT)) {
                        generateFinalizer();
                    }
                }
            }
        }

        Map<String, List<BindingOverload>> scriptableBindings = new LinkedHashMap<String, List<BindingOverload>>();
        for (Map.Entry<String, List<BindingOverload>> e : bindings.entrySet()) {
            List<BindingOverload> filtered = new ArrayList<BindingOverload>();
            for (BindingOverload overload : e.getValue()) {
                if (!overload.attrs.contains("scriptable-hidden")) {
                    filtered.add(overload);
                }
            }
            if (!filtered.isEmpty()) {
                scriptableBindings.put(e.getKey(), filtered);
            }
        }

        writeln();
        writelnI(1, "// module object:", "public static Scriptable createEmptyModuleObject() {");
        writelnI(2, "return new ScriptableModuleImpl() {");
        writelnI(3, "@Override public String getClassName() { return \"[ApiModule " + moduleName + "]\"; }");
        for (Map.Entry<String, List<BindingOverload>> e : scriptableBindings.entrySet()) {
            List<BindingOverload> ctors = filterByFnType(e.getValue(), FN_TYPE_CONSTRUCTOR, false);
            if (!ctors.isEmpty() && !generateJsConstructorBinding(moduleName, e.getKey(), ctors)) {
                ok = false;
            }
        }
        writelnI(2, "};");
        writelnI(1, "}");

        writeln();
        writelnI(1, "// static:", "public static Scriptable injectJsStatic(Scriptable curObj, boolean allowOverrides) {");
        for (Map.Entry<String, List<BindingOverload>> e : scriptableBindings.entrySet()) {
            List<BindingOverload> fields = filterByFnType(e.getValue(), FN_TYPE_STATIC_FIELD, false);
            if (!fields.isEmpty() && !generateJsFieldBinding(moduleName, e.getKey(), fields)) {
                ok = false;
            }
        }
        for (Map.Entry<String, List<BindingOverload>> e : scriptableBindings.entrySet()) {
            List<BindingOverload> methods = filterByFnType(e.getValue(), FN_TYPE_STATIC_METHOD, false);
            if (!methods.isEmpty() && !generateJsMethodBinding(moduleName, e.getKey(), methods)) {
                ok = false;
            }
        }
        writelnI(2, "return curObj;");
        writelnI(1, "}");

        writeln();
        writelnI(1, "public static final Scriptable JS_PROTO = injectJsProto(new ScriptableObject() { @Override public String getClassName() { return \""
                + moduleName + "Proto\"; }}, true);");
        writeln();
        writelnI(1, "// prototype:", "public static Scriptable injectJsProto(Scriptable curObj, boolean allowOverrides) {");
        for (Map.Entry<String, List<BindingOverload>> e : scriptableBindings.entrySet()) {
            List<BindingOverload> fields = filterByFnType(e.getValue(), FN_TYPE_FIELD, false);
            if (!fields.isEmpty() && !generateJsFieldBinding(moduleName, e.getKey(), fields)) {
                ok = false;
            }
        }
        for (Map.Entry<String, List<BindingOverload>> e : scriptableBindings.entrySet()) {
            List<BindingOverload> methods = new ArrayList<BindingOverload>();
            for (BindingOverload overload : e.getValue()) {
                if (overload.fnType.equals(FN_TYPE_METHOD) && !overload.bindType.equals("defaulted-overload")) {
                    methods.add(overload);
                }
            }
            if (!methods.isEmpty() && !generateJsMethodBinding(moduleName, e.getKey(), methods)) {
                ok = false;
            }
        }
        writelnI(2, "return curObj;");
        writelnI(1, "}");
        writeln("}");

        return ok;
    }

    private void generateModuleMap(String pkg, Map<String, String> moduleMap, boolean codegenFailed) {
        writeln("package " + pkg + ";", "", "import java.util.HashMap;", "", "public class ApiModuleMap {");
        if (codegenFailed) {
            writelnI(1, "// WARNING: codegen reported errors; this map may be incomplete.");
        }
        writelnI(1, "public static HashMap<String, Class<?>> MODULES = new HashMap<>();", "", "static {");
        for (Map.Entry<String, String> e : moduleMap.entrySet()) {
            writelnI(2, "MODULES.put(\"" + e.getKey() + "\", " + e.getValue() + ".class);");
        }
        writelnI(1, "}");
        writeln("}");
    }

    // --- small utilities -----------------------------------------------------

    private List<BindingOverload> filterByFnType(List<BindingOverload> overloads, String fnType,
                                                 boolean defaultedOnly) {
        List<BindingOverload> result = new ArrayList<BindingOverload>();
        for (BindingOverload o : overloads) {
            if (o.fnType.equals(fnType)) {
                result.add(o);
            }
        }
        return result;
    }

    private String joinArgs(List<VarDecl> args, boolean nativeType) {
        List<String> parts = new ArrayList<String>();
        for (VarDecl arg : args) {
            String type = nativeType ? typeResolver.getJavaToNativeType(arg.type) : typeResolver.getJavaType(arg.type);
            parts.add(type + " " + arg.name);
        }
        return join(parts, ", ");
    }

    private String joinJavaArgs(List<VarDecl> args) {
        List<String> parts = new ArrayList<String>();
        for (VarDecl arg : args) {
            parts.add(typeResolver.getJavaType(arg.type) + " " + arg.name);
        }
        return join(parts, ", ");
    }

    private static String join(List<String> parts, String sep) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.size(); i++) {
            if (i > 0) {
                sb.append(sep);
            }
            sb.append(parts.get(i));
        }
        return sb.toString();
    }
}
