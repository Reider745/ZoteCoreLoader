package org.mineprogramming.horizon.innercore.model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.modpack.ModPack;
import com.zhekasmirnov.innercore.modpack.ModPackContext;
import com.zhekasmirnov.innercore.modpack.ModPackDirectory;
import com.zhekasmirnov.innercore.modpack.ModPack.TaskReporter;
import com.zhekasmirnov.innercore.utils.FileTools;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ModItem extends Item {
    private String location;

    private List<ModDependency> installList = new ArrayList<>();

    private ModCompilationHelper compilationHelper;

    // parced from https://icmods.mineprogramming.org/api/list
    public ModItem(JSONObject obj) {
        super(obj);

        ModTracker tracker = ModTracker.getCurrent();
        String location = tracker.getLocation(getId());
        setLocation(location);
    }

    @Override
    public void updateInfo(JSONObject obj) {
        super.updateInfo(obj);
        //Uri icon = Uri.parse("https://icmods.mineprogramming.org/api/img/" + obj.optString("icon_full"));
        //setIcon(icon);
    }

    public ModItem(String location) {
        setLocation(location);
        File modRoot = getDirectory();

        File modInfoFile = new File(modRoot, "mod.info");
        File iconFile = new File(modRoot, "mod_icon.png");

        try {
            JSONObject modInfo = FileTools.readJSON(modInfoFile.getAbsolutePath());
            setTitle(modInfo.optString("name"));
            setDescriptionShort(modInfo.optString("description"));
            setAuthorName(modInfo.optString("author"));
            setVersionName(modInfo.optString("version"));
        } catch (IOException | JSONException e) {
            setTitle(modRoot.getName());
        }

        //if(iconFile.exists()){
            //setIcon(Uri.fromFile(iconFile));
        //}

        ModPreferences preferences = new ModPreferences(modRoot);
        setId(preferences.getIcmodsId());
        setVersionCode(preferences.getIcmodsVersion());
    }

    public File getDirectory() {
        ModPack modPack = ModPackContext.getInstance().getCurrentModPack();
        return modPack.getRequestHandler(ModPackDirectory.DirectoryType.MODS).get(location);
    }

    public void setLocation(String location) {
        this.location = location;
        setInstalled(location != null);
    }

    public void updateDependencies(JSONArray obj) {
        installList.clear();
        for (int i = 0; i < obj.length(); i++) {
            ModDependency dependency = new ModDependency(obj.optJSONObject(i));
            installList.add(dependency);
        }
        installList.add(new ModDependency(getId(), getTitle(), getVersionCode()));
    }

    public List<ModDependency> buildInstallList(ModTracker tracker) {
        List<ModDependency> list = new ArrayList<>();
        for (ModDependency dependency : installList) {
            if (!tracker.isInstalled(dependency.getId())) {
                list.add(dependency);
            }
        }
        return list;
    }

    public void onInstalled(File file, String location) {
        setLocation(location);

        String configPath = getConfigPath();
        File toFile = new File(configPath);
        File fromFile = new File(file, "config.json");
        if(!toFile.equals(fromFile)){
            toFile.delete();
            toFile.getParentFile().mkdirs();
            ICLog.d("DEBUG", "moving mod config: " + fromFile + " -> " + toFile);
            fromFile.renameTo(toFile);
        }
    }

    public void onDeleted() {
        setLocation(null);
    }

    private void initializeCompilationHelper(){
        if(compilationHelper == null){
            compilationHelper = new ModCompilationHelper(getDirectory());
        }
    }

    
    //public void compile(Context context, TaskReporter reporter){
        //initializeCompilationHelper();
        //compilationHelper.compile(context, reporter);
    //}

    public boolean isCompiled(){
        initializeCompilationHelper();
        return compilationHelper.isCompiled();
    }

    public void setDevelop(){
        initializeCompilationHelper();
        compilationHelper.setDevelop();
    }

	public String getConfigPath() {
        ModPack currentPack = ModPackContext.getInstance().getCurrentModPack();
        File file = currentPack.getRequestHandler(ModPackDirectory.DirectoryType.CONFIG)
            .get(location, "config.json");
		return file.getAbsolutePath();
	}

	public String getLocation() {
		return location;
	}
    
}
