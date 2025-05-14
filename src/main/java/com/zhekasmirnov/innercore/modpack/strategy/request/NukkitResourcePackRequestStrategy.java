package com.zhekasmirnov.innercore.modpack.strategy.request;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class NukkitResourcePackRequestStrategy extends DefaultDirectoryRequestStrategy {
    @Override
    public List<String> getAllLocations() {
        List<String> result = new ArrayList<>();
        File[] files = getAssignedDirectory().getLocation().listFiles();
        if (files != null) {
            for (File file : files) {
                result.add(file.getName());
            }
        }
        return result;
    }
}
