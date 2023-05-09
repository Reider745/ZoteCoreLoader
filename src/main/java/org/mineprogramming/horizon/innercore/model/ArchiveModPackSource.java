package org.mineprogramming.horizon.innercore.model;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.zhekasmirnov.innercore.modpack.ModPackContext;
import com.zhekasmirnov.innercore.modpack.ModPackStorage;

import org.json.JSONException;

public class ArchiveModPackSource extends ItemSource {
    public ArchiveModPackSource() {
        ModPackStorage storage = ModPackContext.getInstance().getStorage();

        List<File> archives = storage.getAllArchivedPacks();
        for (File archive : archives) {
            try {
                addItem(new ModPackItem(archive));
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
