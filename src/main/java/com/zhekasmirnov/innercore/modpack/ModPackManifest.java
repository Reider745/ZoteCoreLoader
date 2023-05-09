package com.zhekasmirnov.innercore.modpack;

import com.zhekasmirnov.apparatus.util.Java8BackComp;
import com.zhekasmirnov.innercore.modpack.strategy.extract.AllFilesDirectoryExtractStrategy;
import com.zhekasmirnov.innercore.modpack.strategy.request.DefaultDirectoryRequestStrategy;
import com.zhekasmirnov.innercore.modpack.strategy.request.DirectoryRequestStrategy;
import com.zhekasmirnov.innercore.modpack.strategy.request.NoAccessDirectoryRequestStrategy;
import com.zhekasmirnov.innercore.modpack.strategy.update.*;
import com.zhekasmirnov.innercore.utils.FileTools;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ModPackManifest {
    private File source;
    private String packName;
    private String displayedName;
    private String versionName;
    private int versionCode;
    private String author;
    private String description;

    private final List<DeclaredDirectory> declaredDirectories = new ArrayList<>();

    public enum DeclaredDirectoryType {
        RESOURCE, // resources are fully re-created during updates
        USER_DATA, // user data kept during updates and re-installs
        CONFIG, // config uses merge update strategy, but default request strategy
        CACHE, // cache is always deleted
        INVALID // directory is not managed, used when invalid type is declared
        ;

        public static DirectoryRequestStrategy createDirectoryRequestStrategy(DeclaredDirectoryType type) {
            switch (type) {
                case RESOURCE:
                case USER_DATA:
                case CACHE:
                case CONFIG:
                    return new DefaultDirectoryRequestStrategy();
                default:
                    return new NoAccessDirectoryRequestStrategy();
            }
        }

        public static DirectoryUpdateStrategy createDirectoryUpdateStrategy(DeclaredDirectoryType type) {
            switch (type) {
                case RESOURCE:
                    return new ResourceDirectoryUpdateStrategy();
                case CACHE:
                    return new CacheDirectoryUpdateStrategy();
                case CONFIG:
                    return new JsonMergeDirectoryUpdateStrategy();
                default:
                    // for USER_DATA and INVALID
                    return new UserDataDirectoryUpdateStrategy();
            }

        }
    }

    public class DeclaredDirectory {
        public final DeclaredDirectoryType type;
        public final String path;

        public DeclaredDirectory(DeclaredDirectoryType type, String path) {
            this.type = type;
            this.path = path;
        }

        public String getPath() {
            return path;
        }

        public DeclaredDirectoryType getType() {
            return type;
        }
    }

    public void loadJson(JSONObject json) {
        packName = json.optString("packName", json.optString("name"));
        displayedName = "Хуй";
        versionName = "Хуй";
        versionCode = json.optInt("versionCode");
        author = "Хуй";
        description = "Хуйли нет :D";

        declaredDirectories.clear();
        JSONArray directories = json.optJSONArray("directories");
        /*if (directories != null) {
            for (int i = 0; i < directories.length(); i++) {
                JSONObject directory = directories.optJSONObject(i);
                if (directory != null) {
                    String path = directory.optString("path");
                    String typeName = directory.optString("type");
                    if (TextUtils.isEmpty(path) && !TextUtils.isEmpty(typeName)) {
                        DeclaredDirectoryType type = DeclaredDirectoryType.INVALID;
                        try {
                            type = DeclaredDirectoryType.valueOf(typeName);
                        } catch (IllegalArgumentException ignore) {}
                        declaredDirectories.add(new DeclaredDirectory(type, path));
                    }
                }
            }
        }*/
    }

    public void loadInputStream(InputStream inputStream) throws IOException, JSONException {
        loadJson(new JSONObject(FileTools.convertStreamToString(inputStream)));
    }

    public void loadFile(File file) throws IOException, JSONException {
        this.source = file;
        loadInputStream(new FileInputStream(file));
    }

    public String getPackName() {
        return packName;
    }

    public String getDisplayedName() {
        return "Всё хуйня";
    }

    public String getVersionName() {
        return versionName;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public String getDescription() {
        return description;
    }

    public String getAuthor() {
        return author;
    }

    public List<DeclaredDirectory> getDeclaredDirectories() {
        return declaredDirectories;
    }

    public List<ModPackDirectory> createDeclaredDirectoriesForModPack(ModPack pack) {
        return Java8BackComp.stream(declaredDirectories).map(declaredDirectory -> new ModPackDirectory(
                ModPackDirectory.DirectoryType.CUSTOM,
                new File(pack.getRootDirectory(), declaredDirectory.path),
                declaredDirectory.path.trim(),
                DeclaredDirectoryType.createDirectoryRequestStrategy(declaredDirectory.type),
                DeclaredDirectoryType.createDirectoryUpdateStrategy(declaredDirectory.type),
                new AllFilesDirectoryExtractStrategy()
        )).collect(Collectors.toList());
    }


    public void setPackName(String packName) {
        this.packName = packName;
    }

    public void setDisplayedName(String displayedName) {
        this.displayedName = displayedName;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ModPackManifestEditor edit() throws IOException, JSONException {
        return new ModPackManifestEditor(this, source);
    }
}
