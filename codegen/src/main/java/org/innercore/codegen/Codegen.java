package org.innercore.codegen;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Orchestrates the API-binding codegen: clears the output package, resolves
 * {@code @ApiBinding} sources into a {@link BindingStore}, then generates the
 * {@code <Module>BindGenBase} + {@code ApiModuleMap} sources.
 *
 * <p>This is the single-process, Java-only entry point. The C++ (libclang) pass is
 * out of scope; when added, it would contribute records to the same {@link BindingStore}
 * before {@link JavaBindingGenerator#generate} runs.
 */
public final class Codegen {
    private Codegen() {}

    /** Runs codegen over an explicit set of {@code .java} files (used by the Gradle task). */
    public static boolean run(List<File> javaFiles, File outputDir, String outputPackage) {
        prepareOutput(outputDir);
        BindingStore store = new BindingStore();
        boolean resolveOk = new JavaBindingResolver(store).resolveFiles(javaFiles);
        boolean generateOk = new JavaBindingGenerator(store).generate(outputDir, outputPackage);
        return resolveOk && generateOk;
    }

    /** Runs codegen by recursively scanning source roots (used by {@link #main}). */
    public static boolean runWithRoots(List<File> sourceRoots, File outputDir, String outputPackage) {
        prepareOutput(outputDir);
        BindingStore store = new BindingStore();
        boolean resolveOk = new JavaBindingResolver(store).resolve(sourceRoots, outputDir);
        boolean generateOk = new JavaBindingGenerator(store).generate(outputDir, outputPackage);
        return resolveOk && generateOk;
    }

    private static void prepareOutput(File outputDir) {
        clearDirectory(outputDir);
        if (!outputDir.exists() && !outputDir.mkdirs()) {
            throw new RuntimeException("failed to create output directory " + outputDir);
        }
    }

    private static void clearDirectory(File dir) {
        if (!dir.isDirectory()) {
            return;
        }
        try (Stream<java.nio.file.Path> walk = Files.walk(dir.toPath())) {
            List<java.nio.file.Path> children = walk
                    .filter(p -> !p.equals(dir.toPath()))
                    .sorted(Comparator.reverseOrder())
                    .collect(Collectors.toList());
            for (java.nio.file.Path child : children) {
                Files.deleteIfExists(child);
            }
        } catch (IOException e) {
            throw new RuntimeException("failed to clear output directory " + dir, e);
        }
    }

    /** CLI entry point for manual runs/debugging: {@code <outputPackage> <outputDir> <root>...}. */
    public static void main(String[] args) {
        if (args.length < 3) {
            System.err.println("usage: Codegen <outputPackage> <outputDir> <sourceRoot>...");
            System.exit(2);
        }
        String outputPackage = args[0];
        File outputDir = new File(args[1]);
        List<File> roots = new ArrayList<File>();
        for (int i = 2; i < args.length; i++) {
            roots.add(new File(args[i]));
        }
        boolean ok = runWithRoots(roots, outputDir, outputPackage);
        if (!ok) {
            System.err.println("codegen finished with errors");
            System.exit(1);
        }
    }
}
