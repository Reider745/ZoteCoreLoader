package org.mineprogramming.horizon.innercore.model;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipFile;

import com.zhekasmirnov.innercore.modpack.ModPack;
import com.zhekasmirnov.innercore.modpack.ModPackContext;
import com.zhekasmirnov.innercore.modpack.ModPackManifest;
import com.zhekasmirnov.innercore.modpack.ModPackPreferences;
import com.zhekasmirnov.innercore.modpack.ModPackStorage;
import com.zhekasmirnov.innercore.modpack.installation.ModpackInstallationSource;
import com.zhekasmirnov.innercore.modpack.installation.ZipFileInstallationSource;

import org.json.JSONException;
import org.json.JSONObject;

public class ModPackItem extends Item {
    private ModPack modPack;

    private int modsCount;
    private boolean isModified;

    private ModpackInstallationSource installationSource;

    private File packArchive;

    public ModPackItem(File packArchive) throws IOException, JSONException {
        this.packArchive = packArchive;
        this.installationSource = new ZipFileInstallationSource(new ZipFile(packArchive));
        ModPackManifest manifest = installationSource.getTempManifest();

        setTitle(manifest.getDisplayedName());
        setVersionName(manifest.getVersionName());
        setAuthorName(manifest.getAuthor());
        setDescriptionShort(manifest.getDescription());

        setInstalled(false);
    }

    public ModPackItem(ModPack modPack) {
        this.modPack = modPack;
        ModPackManifest manifest = modPack.getManifest();

        setTitle(manifest.getDisplayedName());
        setVersionName(manifest.getVersionName());
        setAuthorName(manifest.getAuthor());
        setDescriptionShort(manifest.getDescription());
        setInstalled(true);

        File iconFile = modPack.getIconFile();
        if (iconFile.exists()) {

        }

        ModPackPreferences prefs = modPack.getPreferences();
        int icmodsId = prefs.getInt("icmods_id", 0);
        setId(icmodsId);
        int icmodsVersion = prefs.getInt("icmods_version", 0);
        setVersionCode(icmodsVersion);
        boolean isModified = prefs.getBoolean("modified", true);
        setModified(isModified);

        modsCount = ModTracker.forPack(modPack).getModsCount();
    }

    public boolean isModified() {
        return isModified;
    }

    private void setModified(boolean isModified) {
        this.isModified = isModified;
    }

    // parced from https://icmods.mineprogramming.org/api/list
    public ModPackItem(JSONObject obj) {
        super(obj);
        setTags(obj.optJSONArray("tags"));
        modsCount = obj.optInt("mod_count");

        modPack = findLocal();
        if (modPack != null) {
            setInstalled(true);
        }
    }

    private ModPack findLocal() {
        ModPackStorage storage = ModPackContext.getInstance().getStorage();
        List<ModPack> installed = storage.getAllModPacks();
        for (ModPack pack : installed) {
            ModPackPreferences prefs = pack.getPreferences();
            if (prefs.getInt("icmods_id", 0) == getId()) {
                return pack;
            }
        }

        return null;
    }



    public void onDeleted() {
        modPack = null;
        setInstalled(false);
    }

    public ModPack getModPack() {
        return modPack;
    }

    public int getModsCount() {
        return modsCount;
    }

    public boolean isSelected() {
        return modPack == ModPackContext.getInstance().getCurrentModPack();
    }

    public boolean isDefault() {
        return modPack == ModPackContext.getInstance().getStorage().getDefaultModPack();
    }

    public boolean isArchived(){
        return installationSource != null;
    }



    public void deleteArchive(){
        packArchive.delete();
    }

}
