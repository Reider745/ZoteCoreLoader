package org.innercore.codegen;

import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithAnnotations;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.innercore.codegen.Encoding.*;

/**
 * Parses {@code @ApiBinding}-annotated Java sources (syntactically, via JavaParser)
 * and emits normalized {@link BindingRecord}s into a {@link BindingStore}.
 *
 * <p>Port of the reference {@code JavaBindingResolver}. Unlike the reference, type
 * resolution derives each class's package from its own {@code package} declaration
 * and the file set is discovered recursively, rather than from a configured flat
 * directory list.
 */
public final class JavaBindingResolver {
    private final BindingStore store;
    private boolean ok = true;

    public JavaBindingResolver(BindingStore store) {
        this.store = store;
        StaticJavaParser.getParserConfiguration()
                .setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_17);
    }

    public boolean isOk() {
        return ok;
    }

    // --- annotation helpers --------------------------------------------------

    private static String simpleAnnotationName(AnnotationExpr ann) {
        return ann.getName().getIdentifier();
    }

    private static String unquote(String literalOrRaw) {
        return literalOrRaw;
    }

    /**
     * Returns a map of the annotation's members (with implicit "value" key) if the
     * given annotation is present, else null. Mirrors the reference get_java_annotation.
     */
    private Map<String, String> getJavaAnnotation(NodeWithAnnotations<?> node, String annName) {
        Map<String, String> result = null;
        for (AnnotationExpr ann : node.getAnnotations()) {
            if (!simpleAnnotationName(ann).equals(annName)) {
                continue;
            }
            if (result == null) {
                result = new HashMap<String, String>();
                result.put("value", "");
            }
            if (ann instanceof SingleMemberAnnotationExpr) {
                SingleMemberAnnotationExpr single = (SingleMemberAnnotationExpr) ann;
                if (single.getMemberValue() instanceof StringLiteralExpr) {
                    result.put("value", ((StringLiteralExpr) single.getMemberValue()).asString());
                }
            } else if (ann instanceof NormalAnnotationExpr) {
                for (MemberValuePair pair : ((NormalAnnotationExpr) ann).getPairs()) {
                    String val;
                    if (pair.getValue() instanceof StringLiteralExpr) {
                        val = ((StringLiteralExpr) pair.getValue()).asString();
                    } else {
                        val = unquote(pair.getValue().toString());
                    }
                    result.put(pair.getNameAsString(), val);
                }
            }
            // MarkerAnnotationExpr: nothing more to add.
        }
        return result;
    }

    private boolean hasAnnotation(NodeWithAnnotations<?> node, String annName) {
        for (AnnotationExpr ann : node.getAnnotations()) {
            if (simpleAnnotationName(ann).equals(annName)) {
                return true;
            }
        }
        return false;
    }

    // --- type resolution -----------------------------------------------------

    /** The textual (possibly dotted) name of a non-array type, ignoring generics. */
    private String resolveJavaTypeName(Type type) {
        if (type.isClassOrInterfaceType()) {
            ClassOrInterfaceType ct = type.asClassOrInterfaceType();
            String name = ct.getNameAsString();
            Optional<ClassOrInterfaceType> scope = ct.getScope();
            while (scope.isPresent()) {
                name = scope.get().getNameAsString() + "." + name;
                scope = scope.get().getScope();
            }
            return name;
        }
        if (type.isPrimitiveType()) {
            return type.asPrimitiveType().asString();
        }
        if (type.isVoidType()) {
            return BASE_TYPE_VOID;
        }
        return type.asString();
    }

    private static String packageOf(CompilationUnit cu) {
        return cu.getPackageDeclaration().map(p -> p.getNameAsString()).orElse("");
    }

    /** Port of stringify_java_type: encode a Java type into the internal type DSL. */
    private String stringifyJavaType(Type rawType, ClassOrInterfaceDeclaration cls,
                                     CompilationUnit cu, String diagnosticsPrefix) {
        Type base = rawType;
        int dims = 0;
        while (base.isArrayType()) {
            dims++;
            base = base.asArrayType().getComponentType();
        }
        String prefix = repeat(TYPE_PREFIX_LIST, dims);

        String declName = resolveJavaTypeName(base);
        if (declName.equals(BASE_TYPE_VOID) || BASE_PRIMITIVE_TYPES.contains(declName)
                || declName.equals(BASE_TYPE_STRING)) {
            return prefix + declName;
        }

        for (ImportDeclaration imp : cu.getImports()) {
            if (!imp.isAsterisk() && imp.getNameAsString().endsWith("." + declName)) {
                return prefix + TYPE_PREFIX_JAVA_CLS + imp.getNameAsString();
            }
        }

        String pkg = packageOf(cu);
        String canonical = pkg + "." + cls.getNameAsString();
        if (declName.equals(cls.getNameAsString()) || declName.equals(canonical)) {
            return prefix + TYPE_PREFIX_JAVA_CLS + canonical;
        }

        for (String systemType : DEFAULT_JAVA_IMPORTS) {
            String simple = systemType.contains(".")
                    ? systemType.substring(systemType.lastIndexOf('.') + 1) : systemType;
            if (declName.equals(simple) || declName.equals(systemType)) {
                return prefix + TYPE_PREFIX_JAVA_CLS + declName;
            }
        }

        for (BodyDeclaration<?> member : cls.getMembers()) {
            if (member instanceof TypeDeclaration
                    && ((TypeDeclaration<?>) member).getNameAsString().equals(declName)) {
                return prefix + TYPE_PREFIX_JAVA_CLS + pkg + "." + cls.getNameAsString() + "." + declName;
            }
        }

        System.out.println((diagnosticsPrefix != null && !diagnosticsPrefix.isEmpty()
                ? diagnosticsPrefix : canonical)
                + " has unresolved import " + declName + ", guessing type placed in same directory");
        return prefix + TYPE_PREFIX_JAVA_CLS + pkg + "." + declName;
    }

    // --- declaration resolution ----------------------------------------------

    private VarDecl resolveMethodReturn(MethodDeclaration method, ClassOrInterfaceDeclaration cls,
                                        CompilationUnit cu, String diag) {
        if (method.getType().isVoidType()) {
            return new VarDecl(BASE_TYPE_VOID);
        }
        String typ = stringifyJavaType(method.getType(), cls, cu, diag);
        if (typ == null) {
            return null;
        }
        VarDecl d = new VarDecl(typ);
        d.nullable = hasAnnotation(method, "Nullable");
        return d;
    }

    private VarDecl resolveConstructorReturn(ConstructorDeclaration ctor, ClassOrInterfaceDeclaration cls,
                                             CompilationUnit cu) {
        VarDecl d = new VarDecl(TYPE_PREFIX_JAVA_CLS + packageOf(cu) + "." + cls.getNameAsString());
        d.nullable = hasAnnotation(ctor, "Nullable");
        return d;
    }

    private VarDecl resolveParameter(Parameter param, ClassOrInterfaceDeclaration cls,
                                     CompilationUnit cu, String diag) {
        String typ = stringifyJavaType(param.getType(), cls, cu, diag);
        if (typ == null) {
            return null;
        }
        VarDecl d = new VarDecl(typ);
        d.name = param.getNameAsString();
        d.nullable = hasAnnotation(param, "Nullable");
        return d;
    }

    private VarDecl resolveField(FieldDeclaration field, ClassOrInterfaceDeclaration cls,
                                 CompilationUnit cu, String diag) {
        Type type = field.getVariable(0).getType();
        String typ = stringifyJavaType(type, cls, cu, diag);
        if (typ == null) {
            return null;
        }
        VarDecl d = new VarDecl(typ);
        d.name = field.getVariable(0).getNameAsString();
        d.nullable = hasAnnotation(field, "Nullable");
        return d;
    }

    // --- binding extraction --------------------------------------------------

    private boolean processMethodBinding(Object decl, ClassOrInterfaceDeclaration cls,
                                         String moduleName, String fullClsName, CompilationUnit cu) {
        boolean isCtor = decl instanceof ConstructorDeclaration;
        NodeWithAnnotations<?> annotated = (NodeWithAnnotations<?>) decl;
        Map<String, String> bindAnn = getJavaAnnotation(annotated, "ApiBinding");
        if (bindAnn == null) {
            return true;
        }

        String memberName = isCtor ? cls.getNameAsString() : ((MethodDeclaration) decl).getNameAsString();
        String bindName = bindAnn.get("value");
        if (bindName == null || bindName.isEmpty()) {
            bindName = memberName;
        }
        if (isCtor) {
            bindName = "nativeConstruct";
        }

        String diag = moduleName + "." + bindName;
        BindingRecord record = new BindingRecord();
        boolean argsOk = true;

        List<Parameter> params = isCtor
                ? ((ConstructorDeclaration) decl).getParameters()
                : ((MethodDeclaration) decl).getParameters();
        for (Parameter param : params) {
            VarDecl argDecl = resolveParameter(param, cls, cu, diag);
            if (argDecl == null) {
                argsOk = false;
                continue;
            }
            record.args.add(argDecl);
        }

        VarDecl resultType = isCtor
                ? resolveConstructorReturn((ConstructorDeclaration) decl, cls, cu)
                : resolveMethodReturn((MethodDeclaration) decl, cls, cu, diag);
        if (resultType == null || !argsOk) {
            return false;
        }

        boolean isStatic = !isCtor && ((MethodDeclaration) decl).isStatic();
        String typ = isStatic ? FN_TYPE_STATIC_METHOD : FN_TYPE_METHOD;
        if ("getPointer".equals(bindName)) {
            typ = FN_TYPE_JAVA_GET_POINTER;
            record.attrs.add("scriptable-hidden");
        }
        if (isCtor) {
            typ = FN_TYPE_CONSTRUCTOR;
        }

        record.bind = moduleName + "." + bindName;
        record.cls = fullClsName;
        record.src = memberName;
        record.typ = typ;
        record.wrap = false;
        record.ret = resultType;
        store.addRecord(record);
        return true;
    }

    private boolean processFieldBinding(FieldDeclaration field, ClassOrInterfaceDeclaration cls,
                                        String moduleName, String fullClsName, CompilationUnit cu) {
        Map<String, String> bindAnn = getJavaAnnotation(field, "ApiBinding");
        if (bindAnn == null) {
            return true;
        }

        String memberName = field.getVariable(0).getNameAsString();
        String bindName = bindAnn.get("value");
        if (bindName == null || bindName.isEmpty()) {
            bindName = memberName;
        }

        VarDecl resultType = resolveField(field, cls, cu, moduleName + "." + bindName);
        if (resultType == null) {
            return false;
        }

        BindingRecord record = new BindingRecord();
        if (field.isFinal()) {
            record.attrs.add("final");
        }
        boolean isStatic = field.isStatic();
        record.bind = moduleName + "." + bindName;
        record.cls = fullClsName;
        record.src = memberName;
        record.typ = isStatic ? FN_TYPE_STATIC_FIELD : FN_TYPE_FIELD;
        record.wrap = false;
        record.ret = resultType;
        store.addRecord(record);
        return true;
    }

    private boolean processClassBinding(ClassOrInterfaceDeclaration cls, CompilationUnit cu) {
        Map<String, String> moduleAnn = getJavaAnnotation(cls, "ApiBinding");
        if (moduleAnn == null) {
            return true;
        }

        String moduleName = moduleAnn.get("value");
        if (moduleName == null || moduleName.isEmpty()) {
            moduleName = cls.getNameAsString();
        }
        String fullClsName = packageOf(cu) + "." + cls.getNameAsString();
        store.addModule(moduleName, fullClsName);
        System.out.println("parsing bindings of " + fullClsName);

        boolean classOk = true;
        for (BodyDeclaration<?> member : cls.getMembers()) {
            if (member instanceof MethodDeclaration || member instanceof ConstructorDeclaration) {
                if (!processMethodBinding(member, cls, moduleName, fullClsName, cu)) {
                    classOk = false;
                }
            } else if (member instanceof FieldDeclaration) {
                if (!processFieldBinding((FieldDeclaration) member, cls, moduleName, fullClsName, cu)) {
                    classOk = false;
                }
            }
        }
        return classOk;
    }

    private void processFile(File file) {
        CompilationUnit cu;
        try {
            cu = StaticJavaParser.parse(file);
        } catch (Exception e) {
            ok = false;
            System.out.println("failed to parse " + file + ": " + e.getMessage());
            return;
        }
        for (TypeDeclaration<?> type : cu.getTypes()) {
            if (type instanceof ClassOrInterfaceDeclaration
                    && !((ClassOrInterfaceDeclaration) type).isInterface()) {
                if (!processClassBinding((ClassOrInterfaceDeclaration) type, cu)) {
                    System.out.println("cannot resolve java file " + file + " bindings");
                    ok = false;
                }
            }
        }
    }

    /** Resolves bindings from an explicit list of {@code .java} files (e.g. supplied by Gradle). */
    public boolean resolveFiles(List<File> files) {
        for (File file : files) {
            if (file.isFile() && file.getName().endsWith(".java")) {
                processFile(file);
            }
        }
        return ok;
    }

    /** Recursively scans the given roots for {@code .java} files and resolves bindings. */
    public boolean resolve(List<File> roots, File excludeDir) {
        for (File root : roots) {
            if (!root.isDirectory()) {
                System.out.println("non-existing codegen directory skipped: " + root);
                continue;
            }
            List<File> javaFiles;
            try (Stream<java.nio.file.Path> walk = Files.walk(root.toPath())) {
                final String excludePath = excludeDir != null
                        ? excludeDir.getAbsolutePath() : null;
                javaFiles = walk
                        .filter(Files::isRegularFile)
                        .map(java.nio.file.Path::toFile)
                        .filter(f -> f.getName().endsWith(".java"))
                        .filter(f -> excludePath == null
                                || !f.getAbsolutePath().startsWith(excludePath))
                        .collect(Collectors.toList());
            } catch (IOException e) {
                ok = false;
                System.out.println("failed to walk " + root + ": " + e.getMessage());
                continue;
            }
            for (File file : javaFiles) {
                processFile(file);
            }
        }
        return ok;
    }
}
