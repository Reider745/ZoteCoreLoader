package org.mineprogramming.horizon.innercore.model;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.modpack.DirectorySetRequestHandler;
import com.zhekasmirnov.innercore.modpack.ModPack;
import com.zhekasmirnov.innercore.modpack.ModPackContext;
import com.zhekasmirnov.innercore.modpack.ModPackDirectory;

public class ModTracker {
    private final ModPack modPack;
    private DirectorySetRequestHandler requestHandler;
    
    private List<String> modLocations;
    private Map<Integer, Integer> modVersions;
    private Map<Integer, String> modLocationsById;

    private static ModTracker currentTracker;

    private boolean locationsDirty = true;
    private boolean versionsDirty = true;

    public static ModTracker getCurrent(){
        ModPack currentPack = ModPackContext.getInstance().getCurrentModPack();
        
        if(currentTracker == null || currentTracker.modPack != currentPack){
            currentTracker = new ModTracker(currentPack);
        }

        return currentTracker;
    }

    public static ModTracker forPack(ModPack modPack){
        if(currentTracker != null && currentTracker.modPack == modPack){
            return currentTracker;
        } else {
            return new ModTracker(modPack);
        }
    }

    private ModTracker(ModPack modPack){
        this.modPack = modPack;
    }

    public List<String> getModLocations(){
        rebuildLocationsListIfRequired();
        return modLocations;
    }

    public int getModsCount(){
        rebuildLocationsListIfRequired();
        return modLocations.size();
    }

	public boolean isInstalled(int id) {
        rebuildVersionsListIfRequired();
		return modVersions.containsKey(id);
    }

    public String getLocation(int id){
        rebuildVersionsListIfRequired();
        return modLocationsById.get(id);
    }

    public Map<Integer, Integer> getVersions(){
        rebuildVersionsListIfRequired();
        return modVersions;
    }
    
    private void rebuildLocationsList(){
        requestHandler = modPack.getRequestHandler(ModPackDirectory.DirectoryType.MODS);
        modLocations = requestHandler.getAllLocations();
        locationsDirty = false;
    }

    public void rebuildLocationsListIfRequired(){
        if(modLocations == null || locationsDirty){
            rebuildLocationsList();
            versionsDirty = true;
        }
    }

    private void rebuildVersionsList(){
        rebuildLocationsListIfRequired();
        modVersions = new HashMap<>();
        modLocationsById = new HashMap<>();
        
        for(String location: modLocations){
            File root = requestHandler.get(location);
            ModPreferences preferences = new ModPreferences(root);
            int id = preferences.getIcmodsId();
            if(id != 0){
                modVersions.put(id, preferences.getIcmodsVersion());
                modLocationsById.put(id, location);
            }
        }

        versionsDirty = false;
    }

    public void rebuildVersionsListIfRequired(){
        if(modVersions == null || versionsDirty){
            rebuildVersionsList();
        }
    }

	public void invalidate() {
        locationsDirty = true;
        versionsDirty = true;
	}
}
