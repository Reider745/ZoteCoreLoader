package com.zhekasmirnov.horizon.modloader.java;

import android.content.Context;
import com.googlecode.d2j.dex.Dex2jar;
import com.zhekasmirnov.horizon.util.FileUtils;
import com.zhekasmirnov.innercore.utils.FileTools;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;

public class JavaDirectory {
    public final /* Mod */ Object mod;
    public final File directory;
    public final JavaLibraryManifest manifest;

    public JavaDirectory(/* Mod */ Object mod, File directory) {
        this.mod = mod;
        System.out.println("java dir=" + directory);
        if (!directory.isDirectory()) {
            throw new IllegalStateException("non-directory file passed to JavaDirectory constructor: " + directory);
        }
        this.directory = directory;
        try {
            JavaLibraryManifest manifest = new JavaLibraryManifest(new File(directory, "manifest"));
            this.manifest = manifest;
        } catch (IOException err) {
            throw new RuntimeException("failed to read java library manifest for " + directory, err);
        } catch (JSONException err2) {
            throw new RuntimeException("failed to read java library manifest for " + directory, err2);
        }
    }

    public String getName() {
        return this.directory.getName();
    }

    public File getSubDirectory(String path, boolean createIfRequired) {
        File dir = new File(this.directory, path);
        if (!dir.exists()) {
            if (createIfRequired) {
                dir.mkdirs();
            } else {
                return null;
            }
        }
        if (!dir.isDirectory()) {
            return null;
        }
        return dir;
    }

    public File getDestinationDirectory() {
        return getSubDirectory(".build/classes", true);
    }

    public File getJarDirectory() {
        return getSubDirectory(".build/jar", true);
    }

    private static String makeSeparatedString(List<File> files) {
        StringBuilder string = new StringBuilder();
        for (File src : files) {
            if (string.length() > 0) {
                string.append(':');
            }
            string.append(src.getAbsolutePath());
        }
        return string.toString();
    }

    public File getBuildDexFile() {
        File buildDir = getSubDirectory(".build", true);
        if (buildDir != null) {
            return new File(buildDir, "build.dex");
        }
        return null;
    }

    public File getCompiledDexFile() {
        return new File(this.directory, ".compiled.dex");
    }

    public String getSourceDirectories() {
        return makeSeparatedString(this.manifest.sourceDirs);
    }

    public String getLibraryPaths(List<File> bootPaths) {
        List<File> all = new ArrayList<>();
        all.addAll(bootPaths);
        for (File lib : this.manifest.libraryPaths) {
            if (lib.getName().endsWith(".dex")) {
                try {
                    Dex2jar dex2jar = Dex2jar.from(lib);
                    String libPath = lib.getAbsolutePath();
                    File jar = new File(libPath.substring(0, libPath.length() - 4) + ".jar");
                    dex2jar.to(jar.toPath());
                    all.add(jar);
                } catch (IOException e) {
                    throw new RuntimeException("Cannot create jar file of dex " + lib, e);
                }
            } else {
                all.add(lib);
            }
        }
        all.addAll(this.manifest.libraryPaths);
        return makeSeparatedString(all);
    }

    public String[] getArguments() {
        return this.manifest.arguments;
    }

    public boolean isVerboseRequired() {
        return this.manifest.verbose;
    }

    public String[] getAllSourceFiles() {
        ArrayList<String> javaFiles = new ArrayList<>();
        for (File sourcePath : this.manifest.sourceDirs) {
            getAllSourceFiles(javaFiles, sourcePath);
        }
        System.out.println("source size: " + javaFiles.size());
        String[] sources = new String[javaFiles.size()];
        return (String[]) javaFiles.toArray(sources);
    }

    private void getAllSourceFiles(ArrayList<String> toAdd, File parent) {
        if (!parent.exists()) {
            return;
        }
        for (File child : parent.listFiles()) {
            if (child.isDirectory()) {
                getAllSourceFiles(toAdd, child);
            } else if (child.exists() && child.isFile() && child.getName().endsWith(".java")) {
                toAdd.add(child.getAbsolutePath());
            }
        }
    }

    public List<String> getBootClassNames() {
        return this.manifest.bootClasses;
    }

    @Deprecated
    public JavaLibrary addToExecutionDirectory(/* ExecutionDirectory */ Object executionDirectory, Context context) {
        return addToExecutionDirectory();
    }

    public JavaLibrary addToExecutionDirectory() {
        JavaLibrary library;
        File compiled = getCompiledClassesFile();
        if (compiled.exists() && !compiled.isDirectory()) {
            List<File> files = getCompiledClassesFiles();
            library = new JavaLibrary(this, files);
        } else {
            // new JavaCompiler(context).compile(this);
            File dex = getCompiledDexFile();
            if (dex.exists()) {
                library = new JavaLibrary(this, dex);
            } else {
                File build = getBuildDexFile();
                if (build.exists()) {
                    library = new JavaLibrary(this, build);
                } else {
                    throw new RuntimeException("failed to build library " + this + " for some reason");
                }
            }
        }
        return library;
    }

    @Deprecated
    public void compileToClassesFile(Context context) {
        compileToClassesFile();
    }

    public void compileToClassesFile() {
        // new JavaCompiler(context).compile(this);
        File dex = getCompiledDexFile();
        if (dex.exists()) {
            try {
                FileTools.copy(dex, getCompiledClassesFile());
                return;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        throw new RuntimeException("failed to build library " + this + " for some reason");
    }

    public File getCompiledClassesFile() {
        return new File(this.directory, "classes.dex");
    }

    public List<File> getCompiledClassesFiles() {
        String[] files = this.directory.list();
        List<File> result = new ArrayList<>(files.length);
        for (String file : files) {
            if (file.matches("classes[0-9]*\\.dex")) {
                result.add(new File(this.directory, file));
            }
        }
        return result;
    }

    public boolean isInDevMode() {
        return getCompiledClassesFile().exists();
    }

    public void setPreCompiled(boolean preCompiled) {
        FileUtils.setFileFlag(this.directory, "not_precompiled", !preCompiled);
    }

    public boolean isPreCompiled() {
        return !FileUtils.getFileFlag(this.directory, "not_precompiled");
    }
}
