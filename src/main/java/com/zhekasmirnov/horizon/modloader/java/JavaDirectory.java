package com.zhekasmirnov.horizon.modloader.java;

import android.content.Context;
import com.googlecode.d2j.dex.Dex2jar;
import com.zhekasmirnov.horizon.util.FileUtils;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.json.JSONException;
public class JavaDirectory {
    public final File directory;
    public final JavaLibraryManifest manifest;
    public final Object mod;

    public JavaDirectory(Object var1, File var2) {
        this.mod = var1;
        PrintStream var3 = System.out;
        StringBuilder var6 = new StringBuilder();
        var6.append("java dir=");
        var6.append(var2);
        var3.println(var6.toString());
        if (var2.isDirectory()) {
            this.directory = var2;
            File var7 = new File(var2, "oat");
            if (var7.isDirectory()) {
                FileUtils.clearFileTree(var7, true);
            }

            JavaLibraryManifest var8;
            try {
                var7 = new File(var2, "manifest");
                var8 = new JavaLibraryManifest(var7);
            } catch (IOException var4) {
                var6 = new StringBuilder();
                var6.append("failed to read java library manifest for ");
                var6.append(var2);
                throw new RuntimeException(var6.toString(), var4);
            } catch (JSONException var5) {
                StringBuilder var9 = new StringBuilder();
                var9.append("failed to read java library manifest for ");
                var9.append(var2);
                throw new RuntimeException(var9.toString(), var5);
            }

            this.manifest = var8;
        } else {
            var6 = new StringBuilder();
            var6.append("non-directory file passed to JavaDirectory constructor: ");
            var6.append(var2);
            throw new IllegalStateException(var6.toString());
        }
    }

    private void getAllSourceFiles(ArrayList<String> var1, File var2) {
        if (var2.exists()) {
            File[] var6 = var2.listFiles();
            int var4 = var6.length;

            for(int var3 = 0; var3 < var4; ++var3) {
                File var5 = var6[var3];
                if (var5.isDirectory()) {
                    this.getAllSourceFiles(var1, var5);
                } else if (var5.exists() && var5.isFile() && var5.getName().endsWith(".java")) {
                    var1.add(var5.getAbsolutePath());
                }
            }

        }
    }

    private static String makeSeparatedString(List<File> var0) {
        StringBuilder var1 = new StringBuilder();

        File var3;
        for(Iterator var2 = var0.iterator(); var2.hasNext(); var1.append(var3.getAbsolutePath())) {
            var3 = (File)var2.next();
            if (var1.length() > 0) {
                var1.append(':');
            }
        }

        return var1.toString();
    }

    /*public JavaLibrary addToExecutionDirectory(ExecutionDirectory var1, Context var2) {
        File var3 = this.getCompiledClassesFile();
        JavaLibrary var4;
        if (var3.exists() && !var3.isDirectory()) {
            var4 = new JavaLibrary(this, this.getCompiledClassesFiles());
        } else {
            (new JavaCompiler(var2)).compile(this);
            var3 = this.getCompiledDexFile();
            if (var3.exists()) {
                var4 = new JavaLibrary(this, var3);
            } else {
                var3 = this.getBuildDexFile();
                if (!var3.exists()) {
                    StringBuilder var5 = new StringBuilder();
                    var5.append("failed to build library ");
                    var5.append(this);
                    var5.append(" for some reason");
                    throw new RuntimeException(var5.toString());
                }

                var4 = new JavaLibrary(this, var3);
            }
        }

        return var4;
    }*/

/*    public void compileToClassesFile(Context var1) {
        (new JavaCompiler(var1)).compile(this);
        File var3 = this.getCompiledDexFile();
        if (var3.exists()) {
            try {
                FileUtils.copy(var3, this.getCompiledClassesFile());
            } catch (IOException var2) {
                throw new RuntimeException(var2);
            }
        } else {
            StringBuilder var4 = new StringBuilder();
            var4.append("failed to build library ");
            var4.append(this);
            var4.append(" for some reason");
            throw new RuntimeException(var4.toString());
        }
    }*/

    public String[] getAllSourceFiles() {
        ArrayList var1 = new ArrayList();
        Iterator var2 = this.manifest.sourceDirs.iterator();

        while(var2.hasNext()) {
            this.getAllSourceFiles(var1, (File)var2.next());
        }

        PrintStream var4 = System.out;
        StringBuilder var3 = new StringBuilder();
        var3.append("source size: ");
        var3.append(var1.size());
        var4.println(var3.toString());
        return (String[])var1.toArray(new String[var1.size()]);
    }

    public String[] getArguments() {
        return this.manifest.arguments;
    }

    public List<String> getBootClassNames() {
        return this.manifest.bootClasses;
    }

    public File getBuildDexFile() {
        File var1 = this.getSubDirectory(".build", true);
        if (var1 != null) {
            var1 = new File(var1, "build.dex");
        } else {
            var1 = null;
        }

        return var1;
    }

    public File getCompiledClassesFile() {
        return new File(this.directory, "classes.dex");
    }

    public List<File> getCompiledClassesFiles() {
        String[] var3 = this.directory.list();
        ArrayList var4 = new ArrayList(var3.length);
        int var2 = var3.length;

        for(int var1 = 0; var1 < var2; ++var1) {
            String var5 = var3[var1];
            if (var5.matches("classes[0-9]*\\.dex")) {
                var4.add(new File(this.directory, var5));
            }
        }

        return var4;
    }

    public File getCompiledDexFile() {
        return new File(this.directory, ".compiled.dex");
    }

    public File getDestinationDirectory() {
        return this.getSubDirectory(".build/classes", true);
    }

    public File getJarDirectory() {
        return this.getSubDirectory(".build/jar", true);
    }

    public String getLibraryPaths(List<File> var1) {
        ArrayList var2 = new ArrayList();
        var2.addAll(var1);
        Iterator var4 = this.manifest.libraryPaths.iterator();

        while(var4.hasNext()) {
            File var7 = (File)var4.next();
            if (var7.getName().endsWith(".dex")) {
                try {
                    Dex2jar var5 = Dex2jar.from(var7);
                    File var8 = new File(var7.getAbsolutePath().replace(".dex", ".jar"));
                    var5.to(var8.toPath());
                    var2.add(var8);
                } catch (IOException var6) {
                    StringBuilder var3 = new StringBuilder();
                    var3.append("Cannot create jar file of dex ");
                    var3.append(var7);
                    throw new RuntimeException(var3.toString(), var6);
                }
            } else {
                var2.add(var7);
            }
        }

        var2.addAll(this.manifest.libraryPaths);
        return makeSeparatedString(var2);
    }

    public String getName() {
        return this.directory.getName();
    }

    public String getSourceDirectories() {
        return makeSeparatedString(this.manifest.sourceDirs);
    }

    public File getSubDirectory(String var1, boolean var2) {
        File var3 = new File(this.directory, var1);
        if (!var3.exists()) {
            if (!var2) {
                return null;
            }

            var3.mkdirs();
        }

        return !var3.isDirectory() ? null : var3;
    }

    public boolean isInDevMode() {
        return this.getCompiledClassesFile().exists();
    }

    public boolean isPreCompiled() {
        return FileUtils.getFileFlag(this.directory, "not_precompiled") ^ true;
    }

    public boolean isVerboseRequired() {
        return this.manifest.verbose;
    }

    public void setPreCompiled(boolean var1) {
        FileUtils.setFileFlag(this.directory, "not_precompiled", var1 ^ true);
    }
}
