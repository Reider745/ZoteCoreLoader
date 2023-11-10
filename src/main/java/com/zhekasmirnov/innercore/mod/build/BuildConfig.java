package com.zhekasmirnov.innercore.mod.build;

import com.zhekasmirnov.apparatus.minecraft.version.ResourceGameVersion;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.api.mod.API;
import com.zhekasmirnov.innercore.mod.build.enums.BuildConfigError;
import com.zhekasmirnov.innercore.mod.build.enums.BuildType;
import com.zhekasmirnov.innercore.mod.build.enums.ResourceDirType;
import com.zhekasmirnov.innercore.mod.build.enums.SourceType;
import com.zhekasmirnov.innercore.mod.executable.CompilerConfig;
import com.zhekasmirnov.innercore.utils.FileTools;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/*
    Format:
    {
        defaultConfig: {
            api: "",
            buildType: "release|develop",
            libraryDir: "lib/"
            optimizationLevel: 9
        },

        buildDirs: [
            {dir: "dir/to/build/", targetSource: "target/source/file"},
            ...
        ],

        compile: [
            {path: "target/source/file", sourceName: "", sourceType: "preloader|launcher|mod|library", optimizationLevel: 9, api: ""}, // api and optimizationLevel can replace the default ones
            ...
        ],

        resources: [
            {path: "resource/dir/", resourceType: "resource|gui"}
        ],

        nativeDirs: [
            {path: "native/dir/"}
        ],

        javaDirs: [
            {path: "java/dir/"}
        ]
    }
 */

public class BuildConfig {
    private File configFile;

    private boolean isValid = false;
    private JSONObject configJson;
    private BuildConfigError configError = BuildConfigError.NONE;

    public static class DeclaredDirectory {
        public final String path;
        public final ResourceGameVersion version;

        public DeclaredDirectory(String path, ResourceGameVersion version) {
            this.path = path;
            this.version = version;
        }

        public File getFile(File root) {
            return new File(root, path);
        }

        public static DeclaredDirectory fromJson(JSONObject json, String pathPropertyName) {
            String path = json.optString(pathPropertyName);
            if (path == null) {
                return null;
            }
            ResourceGameVersion version = new ResourceGameVersion(json);
            return new DeclaredDirectory(path, version);
        }
    }

    public BuildConfig() {
        configJson = new JSONObject();
        isValid = true;
    }

    public BuildConfig(JSONObject obj) {
        configJson = obj;
        isValid = true;
    }

    public BuildConfig(File file) {
        configFile = file;
        isValid = false;

        try {
            configJson = FileTools.readJSON(file.getAbsolutePath());
        } catch (IOException e) {
            configError = BuildConfigError.FILE_ERROR;
            e.printStackTrace();
            return;
        } catch (JSONException e) {
            configError = BuildConfigError.PARSE_ERROR;
            e.printStackTrace();
            return;
        }

        isValid = true;
    }

    public void save(File file) {
        try {
            FileTools.writeJSON(file.getAbsolutePath(), configJson);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void save() {
        save(configFile);
    }

    public boolean isValid() {
        return isValid;
    }



    /* setups primary config structure */
    public void validate() {
        try {
            if (configJson.optJSONObject("defaultConfig") == null) {
                configJson.put("defaultConfig", new JSONObject());
            }
            if (configJson.optJSONArray("buildDirs") == null) {
                configJson.put("buildDirs", new JSONArray());
            }
            if (configJson.optJSONArray("compile") == null) {
                configJson.put("compile", new JSONArray());
            }
            if (configJson.optJSONArray("resources") == null) {
                configJson.put("resources", new JSONArray());
            }
            if (configJson.optJSONArray("nativeDirs") == null) {
                configJson.put("nativeDirs", new JSONArray());
            }
            if (configJson.optJSONArray("javaDirs") == null) {
                configJson.put("javaDirs", new JSONArray());
            }
        } catch (JSONException e) {
            isValid = false;
            e.printStackTrace();
        }
    }

    public DefaultConfig defaultConfig;
    public ArrayList<BuildableDir> buildableDirs = new ArrayList<>();
    public ArrayList<ResourceDir> resourceDirs = new ArrayList<>();
    public ArrayList<Source> sourcesToCompile = new ArrayList<>();
    public ArrayList<DeclaredDirectory> javaDirectories = new ArrayList<>();
    public ArrayList<DeclaredDirectory> nativeDirectories = new ArrayList<>();

    public boolean read() {
        if (isValid) {
            validate();
        }
        if (!isValid) {
            return false;
        }

        defaultConfig = DefaultConfig.fromJson(configJson.optJSONObject("defaultConfig"));

        buildableDirs.clear();
        JSONArray buildableDirsJson = configJson.optJSONArray("buildDirs");
        for (int i = 0; i < buildableDirsJson.length(); i++) {
            buildableDirs.add(BuildableDir.fromJson(buildableDirsJson.optJSONObject(i)));
        }

        resourceDirs.clear();
        JSONArray resourceDirsJson = configJson.optJSONArray("resources");
        for (int i = 0; i < resourceDirsJson.length(); i++) {
            resourceDirs.add(ResourceDir.fromJson(resourceDirsJson.optJSONObject(i)));
        }

        sourcesToCompile.clear();
        JSONArray sourcesJson = configJson.optJSONArray("compile");
        for (int i = 0; i < sourcesJson.length(); i++) {
            sourcesToCompile.add(Source.fromJson(sourcesJson.optJSONObject(i), defaultConfig));
        }

        javaDirectories.clear();
        JSONArray javaJson = configJson.optJSONArray("javaDirs");
        for (int i = 0; i < javaJson.length(); i++) {
            JSONObject directoryJson = javaJson.optJSONObject(i);
            try{
                DeclaredDirectory directory = DeclaredDirectory.fromJson(directoryJson, "path");
                if (directory != null) {
                    javaDirectories.add(directory);
                }
            } catch(Exception e){
                ICLog.e("InnerCore-BuildConfig", "invalid java directory object", e);
            }
        }

        nativeDirectories.clear();
        JSONArray nativeJson = configJson.optJSONArray("nativeDirs");
        for (int i = 0; i < nativeJson.length(); i++) {
            JSONObject directoryJson = nativeJson.optJSONObject(i);
            try{
                DeclaredDirectory directory = DeclaredDirectory.fromJson(directoryJson, "path");
                if (directory != null) {
                    nativeDirectories.add(directory);
                }
            } catch(Exception e){
                ICLog.e("InnerCore-BuildConfig", "invalid native directory object", e);
            }
        }

        return true;
    }

    public BuildType getBuildType() {
        return defaultConfig.buildType;
    }

    public API getDefaultAPI() {
        return defaultConfig.apiInstance;
    }

    public String getName() {
        if (configFile != null) {
            return configFile.getParentFile().getName();
        }
        return "Unknown Mod";
    }

    public ArrayList<Source> getAllSourcesToCompile(boolean useApi) {
        ArrayList<Source> sources = new ArrayList<>(sourcesToCompile);
        if (defaultConfig.libDir != null) {
            File libraryDir = new File(configFile.getParent(), defaultConfig.libDir);
            if (libraryDir.exists() && libraryDir.isDirectory()) {
                File[] files = libraryDir.listFiles();
                if (files != null) {
                    for (File file : files) {
                        Logger.debug("LIB-DIR", "found library file " + file + " local_path=" + defaultConfig.libDir + file.getName());
                        if (!file.isDirectory()) {
                            Source source = new Source(new JSONObject());
                            if (useApi) {
                                source.setAPI(defaultConfig.apiInstance);
                            }
                            source.setPath(defaultConfig.libDir + file.getName());
                            source.setSourceName(file.getName());
                            source.setSourceType(SourceType.LIBRARY);
                            sources.add(source);
                        }
                    }
                }
            }
        }

        return sources;
    }



    public static class DefaultConfig {
        public final ResourceGameVersion gameVersion;
        public JSONObject json;

        public API apiInstance;
        public int optimizationLevel;
        public BuildType buildType;

        public String libDir = null;
        public String resourcePacksDir = null;
        public String behaviorPacksDir = null;
        public String setupScriptDir = null;

        private DefaultConfig(JSONObject json) {
            this.json = json;
            gameVersion = new ResourceGameVersion(json);
        }

        public void setAPI(API api) {
            this.apiInstance = api;
            try {
                json.put("api", api.getName());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        public void setOptimizationLevel(int level) {
            this.optimizationLevel = BuildConfig.validateOptimizationLevel(level);
            try {
                json.put("optimizationLevel", level);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        public void setBuildType(BuildType type) {
            this.buildType = type;
            try {
                json.put("buildType", type.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        public void setLibDir(String dir) {
            this.libDir = dir;
            try {
                json.put("libraryDir", dir);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        public void setMinecraftResourcePacksDir(String dir) {
            this.resourcePacksDir = dir;
            try {
                json.put("resourcePacksDir", dir);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        public void setMinecraftBehaviorPacksDir(String dir) {
            this.behaviorPacksDir = dir;
            try {
                json.put("behaviorPacksDir", dir);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        public void setSetupScriptDir(String dir) {
            this.setupScriptDir = dir;
            try {
                json.put("setupScript", dir);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        public static DefaultConfig fromJson(JSONObject json) {
            DefaultConfig defaultConfig = new DefaultConfig(json);

            defaultConfig.apiInstance = BuildConfig.getAPIFromJSON(json);
            defaultConfig.optimizationLevel = BuildConfig.getOptimizationLevelFromJSON(json);
            defaultConfig.buildType = BuildConfig.getBuildTypeFromJSON(json);
            defaultConfig.libDir = json.optString("libraryDir", null);
            defaultConfig.resourcePacksDir = json.optString("resourcePacksDir", null);
            defaultConfig.behaviorPacksDir = json.optString("behaviorPacksDir", null);
            defaultConfig.setupScriptDir = json.optString("setupScript", null);

            return defaultConfig;
        }
    }

    public static class BuildableDir {
        public JSONObject json;

        public String dir;
        public String targetSource;

        private BuildableDir(JSONObject json) {
            this.json = json;
        }

        public void setDir(String dir) {
            this.dir = dir;
            try {
                json.put("dir", dir);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        public void setTargetSource(String targetSource) {
            this.targetSource = targetSource;
            try {
                json.put("targetSource", targetSource);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        public static BuildableDir fromJson(JSONObject json) {
            BuildableDir buildableDir = new BuildableDir(json);

            buildableDir.dir = json.optString("dir");
            buildableDir.targetSource = json.optString("targetSource");

            return buildableDir;
        }

        public boolean isRelatedSource(Source source) {
            if (source != null && targetSource != null) {
                return targetSource.equals(source.path);
            }
            return false;
        }
    }

    public static class ResourceDir {
        public JSONObject json;
        public final ResourceGameVersion gameVersion;

        public String path;
        public ResourceDirType resourceType;

        private ResourceDir(JSONObject json) {
            this.json = json;
            this.gameVersion = new ResourceGameVersion(json);
        }

        public void setPath(String path) {
            this.path = path;
            try {
                json.put("path", path);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        public void setResourceType(ResourceDirType type) {
            this.resourceType = type;
            try {
                json.put("resourceType", type.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        public static ResourceDir fromJson(JSONObject json) {
            ResourceDir resourceDir = new ResourceDir(json);

            resourceDir.path = json.optString("path");
            resourceDir.resourceType = BuildConfig.getResourceDirTypeFromJSON(json);

            return resourceDir;
        }
    }

    public static class Source {
        public final ResourceGameVersion gameVersion;
        public JSONObject json;

        public String path;
        public String sourceName;
        public SourceType sourceType;
        public int optimizationLevel;
        public API apiInstance;

        private Source(JSONObject json) {
            this.json = json;
            gameVersion = new ResourceGameVersion(json);
        }

        public void setPath(String path) {
            this.path = path;
            try {
                json.put("path", path);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        public void setSourceName(String sourceName) {
            this.sourceName = sourceName;
            try {
                json.put("sourceName", sourceName);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        public void setSourceType(SourceType sourceType) {
            this.sourceType = sourceType;
            try {
                json.put("sourceType", sourceType.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        public void setOptimizationLevel(int level) {
            this.optimizationLevel = BuildConfig.validateOptimizationLevel(level);
            try {
                json.put("optimizationLevel", level);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        public void setAPI(API api) {
            this.apiInstance = api;
            try {
                json.put("api", api.getName());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        public CompilerConfig getCompilerConfig() {
            CompilerConfig config = new CompilerConfig(apiInstance);
            config.setName(sourceName);
            config.setOptimizationLevel(optimizationLevel);
            config.isLibrary = sourceType == SourceType.LIBRARY;
            return config;
        }

        public static Source fromJson(JSONObject json, DefaultConfig config) {
            Source source = new Source(json);

            source.path = json.optString("path");
            source.sourceType = BuildConfig.getSourceTypeFromJSON(json);

            if (json.has("sourceName")) {
                source.sourceName = json.optString("sourceName", "Unknown Source");
            }
            else {
                source.sourceName = source.path.substring(source.path.lastIndexOf("/") + 1);
            }
            if (json.has("api")) {
                source.apiInstance = BuildConfig.getAPIFromJSON(json);
            }
            else {
                if (source.sourceType == SourceType.PRELOADER) {
                    source.apiInstance = API.getInstanceByName("Preloader");
                }
                else {
                    source.apiInstance = config.apiInstance;
                }
            }
            if (json.has("optimizationLevel")){
                source.optimizationLevel = BuildConfig.getOptimizationLevelFromJSON(json);
            }
            else {
                source.optimizationLevel = config.optimizationLevel;
            }

            return source;
        }
    }






    /* gets API instance from "api" tag of given json object, returns null, if nothing found */
    public static API getAPIFromJSON(JSONObject obj) {
        if (obj.has("api")) {
            try {
                return API.getInstanceByName(obj.getString("api"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static int validateOptimizationLevel(int level) {
        return Math.min(9, Math.max(-1, level));
    }

    public static int getOptimizationLevelFromJSON(JSONObject obj) {
        if (obj.has("optimizationLevel")) {
            return validateOptimizationLevel(obj.optInt("optimizationLevel", -1));
        }
        return 9;
    }

    /* gets resource type from json object, if it is invalid or not specified, sets it to default */
    public static ResourceDirType getResourceDirTypeFromJSON(JSONObject obj) {
        ResourceDirType result = ResourceDirType.RESOURCE;
        try {
            if (obj.has("resourceType")) {
                result = ResourceDirType.fromString(obj.getString("resourceType"));
            }
            obj.put("resourceType", result.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static SourceType getSourceTypeFromJSON(JSONObject obj) {
        SourceType result = SourceType.MOD;
        try {
            if (obj.has("sourceType")) {
                result = SourceType.fromString(obj.getString("sourceType"));
            }
            obj.put("sourceType", result.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static BuildType getBuildTypeFromJSON(JSONObject obj) {
        BuildType result = BuildType.DEVELOP;
        try {
            if (obj.has("buildType")) {
                result = BuildType.fromString(obj.getString("buildType"));
            }
            obj.put("buildType", result.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }



    public BuildableDir findRelatedBuildableDir(Source source) {
        for (BuildableDir dir : buildableDirs) {
            if (dir.isRelatedSource(source)) {
                return dir;
            }
        }
        return null;
    }
}
