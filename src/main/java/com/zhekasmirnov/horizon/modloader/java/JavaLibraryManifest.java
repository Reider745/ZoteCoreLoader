package com.zhekasmirnov.horizon.modloader.java;

import com.zhekasmirnov.horizon.util.FileUtils;
import com.zhekasmirnov.horizon.util.JSONUtils;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JavaLibraryManifest {
    @SuppressWarnings("unused")
    private final File file;
    private final File directory;
    private final JSONObject content;
    public final String[] arguments;
    public final boolean verbose;
    public final List<File> sourceDirs = new ArrayList<>();
    public final List<File> libraryDirs = new ArrayList<>();
    public final List<File> libraryPaths = new ArrayList<>();
    public final List<String> bootClasses = new ArrayList<>();

    public JavaLibraryManifest(File file) throws IOException, JSONException {
        this.file = file;
        this.directory = file.getParentFile();
        this.content = FileUtils.readJSON(file);
        this.verbose = this.content.optBoolean("verbose");
        JSONArray arguments = this.content.optJSONArray("options");
        if (arguments != null) {
            this.arguments = (String[]) JSONUtils.toList(arguments).toArray(new String[arguments.length()]);
        } else {
            this.arguments = new String[0];
        }
        JSONArray bootClasses = this.content.optJSONArray("boot-classes");
        if (bootClasses != null) {
            this.bootClasses.addAll(JSONUtils.toList(bootClasses));
        }
        JSONArray sourceDirs = this.content.optJSONArray("source-dirs");
        if (sourceDirs != null) {
            for (Object path : JSONUtils.toList(sourceDirs)) {
                File dir = new File(this.directory, (String) path);
                if (dir.exists() && dir.isDirectory()) {
                    this.sourceDirs.add(dir);
                }
            }
        }
        JSONArray libraryDirs = this.content.optJSONArray("library-dirs");
        if (libraryDirs != null) {
            for (Object path : JSONUtils.toList(libraryDirs)) {
                File dir = new File(this.directory, (String) path);
                if (dir.exists() && dir.isDirectory()) {
                    this.libraryDirs.add(dir);
                    for (File lib : dir.listFiles()) {
                        String name = lib.getName();
                        if (name.endsWith(".zip") || name.endsWith(".jar") || name.endsWith(".dex")) {
                            this.libraryPaths.add(lib);
                        } else {
                            throw new IllegalArgumentException(
                                    "illegal java library, it can be dex file, zip or jar archive: " + lib);
                        }
                    }
                    continue;
                }
            }
        }
    }
}
