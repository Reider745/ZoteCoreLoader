package com.zhekasmirnov.horizon.modloader.java;

import com.zhekasmirnov.horizon.util.FileUtils;
import com.zhekasmirnov.horizon.util.JSONUtils;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
public class JavaLibraryManifest {
    public final String[] arguments;
    public final List<String> bootClasses = new ArrayList();
    private final JSONObject content;
    private final File directory;
    private final File file;
    public final List<File> libraryDirs = new ArrayList();
    public final List<File> libraryPaths = new ArrayList();
    public final List<File> sourceDirs = new ArrayList();
    public final boolean verbose;

    public JavaLibraryManifest(File var1) throws IOException, JSONException {
        this.file = var1;
        this.directory = var1.getParentFile();
        JSONObject var7 = FileUtils.readJSON(var1);
        this.content = var7;
        this.verbose = var7.optBoolean("verbose");
        JSONArray var8 = this.content.optJSONArray("options");
        if (var8 != null) {
            this.arguments = (String[])JSONUtils.toList(var8).toArray(new String[var8.length()]);
        } else {
            this.arguments = new String[0];
        }

        var8 = this.content.optJSONArray("boot-classes");
        if (var8 != null) {
            this.bootClasses.addAll(JSONUtils.toList(var8));
        }

        var8 = this.content.optJSONArray("source-dirs");
        String var4;
        Iterator var9;
        File var11;
        if (var8 != null) {
            var9 = JSONUtils.toList(var8).iterator();

            while(var9.hasNext()) {
                var4 = (String)var9.next();
                var11 = new File(this.directory, var4);
                if (var11.exists() && var11.isDirectory()) {
                    this.sourceDirs.add(var11);
                }
            }
        }

        var8 = this.content.optJSONArray("library-dirs");
        if (var8 != null) {
            var9 = JSONUtils.toList(var8).iterator();

            while(true) {
                do {
                    do {
                        if (!var9.hasNext()) {
                            return;
                        }

                        var4 = (String)var9.next();
                        var11 = new File(this.directory, var4);
                    } while(!var11.exists());
                } while(!var11.isDirectory());

                this.libraryDirs.add(var11);
                File[] var5 = var11.listFiles();
                int var3 = var5.length;

                for(int var2 = 0; var2 < var3; ++var2) {
                    var11 = var5[var2];
                    String var6 = var11.getName();
                    if (!var6.endsWith(".zip") && !var6.endsWith(".jar") && !var6.endsWith(".dex")) {
                        StringBuilder var10 = new StringBuilder();
                        var10.append("illegal java library, it can be dex file, zip or jar archive: ");
                        var10.append(var11);
                        throw new IllegalArgumentException(var10.toString());
                    }

                    this.libraryPaths.add(var11);
                }
            }
        }
    }
}
