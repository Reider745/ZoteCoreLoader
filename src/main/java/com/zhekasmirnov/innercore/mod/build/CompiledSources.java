package com.zhekasmirnov.innercore.mod.build;

/**
 * Created by zheka on 15.08.2017.
 */

import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.mod.executable.Compiler;
import com.zhekasmirnov.innercore.mod.executable.CompilerConfig;
import com.zhekasmirnov.innercore.mod.executable.Executable;
import com.zhekasmirnov.innercore.utils.FileTools;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class CompiledSources {
    private boolean isValid = true;
    private JSONObject sourceList = new JSONObject();

    private File dir;
    private File sourceListFile;

    private void invalidate() {
        isValid = false;
        validateJson();
        validateFiles();
    }

    private void validateFiles() {
        if (dir != null && !dir.isDirectory()) {
            dir.delete();
        }
        if (dir != null && !dir.exists()) {
            dir.mkdirs();
        }
        if (sourceListFile != null && !sourceListFile.exists()) {
            saveSourceList();
        }
    }

    private void validateJson() {
        if (sourceList == null) {
            sourceList = new JSONObject();
        }
    }

    public void saveSourceList() {
        try {
            FileTools.writeJSON(sourceListFile.getAbsolutePath(), sourceList);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public CompiledSources(File dir) {
        this.dir = dir;

        if (!dir.exists()) {
            invalidate();
            return;
        }

        this.sourceListFile = new File(dir, "sources.json");

        if (!this.sourceListFile.exists()) {
            invalidate();
            return;
        }

        try {
            sourceList = FileTools.readJSON(this.sourceListFile.getAbsolutePath());
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            invalidate();
            return;
        }

        validateJson();
        validateFiles();
    }



    public File[] getCompiledSourceFilesFor(String name) {
        JSONObject data = sourceList.optJSONObject(name);

        if (data == null) {
            return null;
        }

        JSONArray sources = data.optJSONArray("links");
        if (sources != null) {
            ArrayList<File> files = new ArrayList<>();
            for (int i = 0; i < sources.length(); i++) {
                String path = sources.optString(i);
                if (path != null) {
                    File file = new File(dir, path);
                    if (file.exists()) {
                        files.add(file);
                    }
                    else {
                        ICLog.d("WARNING", "compiled dex file " + path + " related to source " + name + " has incorrect formatted path");
                    }
                }
                else {
                    ICLog.d("WARNING", "compiled dex file at index " + i + " related to source " + name + " has incorrect formatted path");
                }
            }
            File[] _files = new File[files.size()];
            files.toArray(_files);
            return _files;
        }

        String path = data.optString("path");
        if (path != null) {
            return new File[]{new File(dir, path)};
        }

        return null;
    }

    public Executable getCompiledExecutableFor(String name, CompilerConfig config) throws IOException {
        File[] dexFiles = getCompiledSourceFilesFor(name);
        if (dexFiles != null) {
            //return Compiler.loadDexList(dexFiles, config);
        }
        return null;
    }

    public void addCompiledSource(String name, File file, String className) {
        JSONObject data = sourceList.optJSONObject(name);
        if (data == null) {
            data = new JSONObject();
        }

        try {
            JSONArray links = data.optJSONArray("links");
            if (links == null) {
                links = new JSONArray();
                data.put("links", links);
            }

            links.put(links.length(), file.getName());
            data.put("class_name", className);

            sourceList.put(name, data);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        saveSourceList();
    }

    public File getTargetCompilationFile(String sourcePath) {
        return new File(dir, sourcePath);
    }

    public void reset() {
        validateJson();
        validateFiles();

        File[] files = dir.listFiles();
        for (File file : files) {
            file.delete();
        }

        sourceList = null;

        validateJson();
        validateFiles();
    }
}
