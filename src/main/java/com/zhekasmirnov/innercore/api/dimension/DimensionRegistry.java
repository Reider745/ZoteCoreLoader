package com.zhekasmirnov.innercore.api.dimension;

import com.reider745.InnerCoreServer;
import com.zhekasmirnov.innercore.api.NativeAPI;
import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.api.runtime.LevelInfo;
import com.zhekasmirnov.innercore.api.runtime.other.PrintStacking;
import com.zhekasmirnov.innercore.utils.FileTools;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by zheka on 12.11.2017.
 */

@Deprecated
public class DimensionRegistry {
    private static CustomDimension currentDimension;

    public static void setCurrentCustomDimension(CustomDimension dimension) {
        InnerCoreServer.useNotSupport("DimensionRegistry.setCurrentCustomDimension(dimension)");
        currentDimension = dimension;
        onDataSaved();
    }

    public static CustomDimension getCurrentCustomDimension() {
        InnerCoreServer.useNotSupport("DimensionRegistry.getCurrentCustomDimension()");
        return null;
    }

    public static int getCurrentDimensionId() {
        if (currentDimension != null) {
            return currentDimension.getId();
        }
        return NativeAPI.getDimension();
    }

    public static int getCurrentDimensionIdImmediate() {
        currentDimension = getCurrentCustomDimension();
        if (currentDimension != null) {
            return currentDimension.getId();
        }
        return NativeAPI.getDimension();
    }

    private static int frame = 0;

    public static void update() {
        if (frame++ % 10 == 0) {
            currentDimension = getCurrentCustomDimension();
        }
    }

    private static HashMap<Long, CustomDimension> registeredDimensions = new HashMap<>();

    static void mapCustomDimension(CustomDimension dimension) {
        registeredDimensions.put(dimension.pointer, dimension);

        if (dimension.getRegion() != null) {
            InnerCoreServer.useNotSupport("DimensionRegistry.mapCustomDimension(dimension)");
        } else {
            ICLog.i("ERROR",
                    "failed to map dimension " + dimension.getId() + " region is missing (maybe it was not found).");
        }
    }

    public static CustomDimension getDimensionById(int id) {
        for (CustomDimension dimension : registeredDimensions.values()) {
            if (dimension.getId() == id) {
                return dimension;
            }
        }
        return null;
    }

    static {
        DimensionDataHandler.readStoredData();
    }

    static DimensionData registerCustomDimension(String strId) {
        HashMap<String, DimensionData> dimensionMap = DimensionDataHandler.getDimensionMap();
        if (dimensionMap.containsKey(strId)) {
            return dimensionMap.get(strId);
        }

        int id = 7;
        Collection<DimensionData> allData = dimensionMap.values();
        for (DimensionData data : allData) {
            if (data.id >= id) {
                id = data.id + 1;
            }
        }

        Region region = findFreeRegion();
        if (region == null) {
            throw new RuntimeException(
                    "failed to find region for dimension " + strId + " maybe too much dimensions were registered.");
        }

        DimensionData data = new DimensionData(id, region);
        dimensionMap.put(strId, data);
        DimensionDataHandler.writeStoredData();

        return data;
    }

    private static Region findFreeRegion() {
        ArrayList<Region> occupiedRegions = new ArrayList<>();

        HashMap<String, DimensionData> dimensionMap = DimensionDataHandler.getDimensionMap();
        Collection<DimensionData> allData = dimensionMap.values();

        for (DimensionData data : allData) {
            occupiedRegions.add(data.region);
        }

        Region region;

        for (int radius = 1; radius < 20; radius++) {
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    if (x == -radius || z == -radius || x == radius || z == radius) {
                        region = new Region(x, z);
                        if (!occupiedRegions.contains(region)) {
                            return region;
                        }
                    }
                }
            }
        }

        return null;
    }

    public static class DimensionData {
        private int id;
        private Region region;

        DimensionData(int id, Region region) {
            this.region = region;
            this.id = id;
        }

        DimensionData(JSONObject json) {
            id = json.optInt("id");
            if (id == 0) {
                throw new IllegalArgumentException("failed to read dimension data");
            }

            JSONArray reg = json.optJSONArray("region");
            if (reg == null || reg.length() != 2) {
                throw new IllegalArgumentException("failed to read dimension data");
            }

            region = new Region(reg.optInt(0), reg.optInt(1));
            if (region.isOverworldRegion()) {
                throw new IllegalArgumentException("failed to read dimension data");
            }
        }

        JSONObject asJson() {
            JSONObject json = new JSONObject();
            try {
                JSONArray reg = new JSONArray();
                reg.put(0, region.regionX);
                reg.put(1, region.regionZ);

                json.put("id", id);
                json.put("region", reg);
            } catch (JSONException e) {
            }

            return json;
        }

        public int getId() {
            return id;
        }

        public Region getRegion() {
            return region;
        }
    }

    private static class DimensionDataHandler {
        private static final Object dataLock = new Object();
        private static final File dataFile = new File(FileTools.DIR_WORK, "mods/.dimension-regions");

        static {
            FileTools.assureFileDir(dataFile);
        }

        static HashMap<String, DimensionData> dimensionDataByStrId = new HashMap<>();

        static void readStoredData() {
            synchronized (dataLock) {
                if (dimensionDataByStrId.size() > 0) {
                    ICLog.i("WARNING", "reading dimension data file with already some dimension data stored.");
                }

                dimensionDataByStrId.clear();

                if (!dataFile.exists()) {
                    return;
                }

                try {
                    JSONObject json = FileTools.readJSON(dataFile.getAbsolutePath());

                    Iterator<String> keys = json.keys();
                    while (keys.hasNext()) {
                        String key = keys.next();

                        try {
                            JSONObject data = json.optJSONObject(key);
                            dimensionDataByStrId.put(key, new DimensionData(data));
                        } catch (Throwable e) {
                            PrintStacking.print("ERROR OCCURED DURING READING DIMENSION DATA");
                            ICLog.e("ERROR", "failed to read existing data for dimension with id: " + key + "", e);
                        }
                    }

                } catch (IOException | JSONException e) {
                    PrintStacking.print("ERROR OCCURED DURING READING DIMENSION DATA");
                    ICLog.e("ERROR", "failed to read dimension data file", e);
                }
            }
        }

        static void writeStoredData() {
            synchronized (dataLock) {
                JSONObject json = new JSONObject();

                Set<String> keys = dimensionDataByStrId.keySet();
                for (String key : keys) {
                    try {
                        json.put(key, dimensionDataByStrId.get(key).asJson());
                    } catch (JSONException e) {
                    }
                }

                try {
                    FileTools.writeJSON(dataFile.getAbsolutePath(), json);
                } catch (IOException e) {
                    ICLog.e("ERROR", "wailed to write stored dimension data", e);
                }
            }
        }

        public static HashMap<String, DimensionData> getDimensionMap() {
            return dimensionDataByStrId;
        }

        private static void loadAndSetupCurrentDimension() {
            synchronized (dataLock) {
                String levelDir = LevelInfo.getAbsoluteDir();
                if (levelDir == null) {
                    return;
                }
                File dimensionId = new File(levelDir, "dimension-id");

                int id = -1;

                try {
                    id = Integer.valueOf(FileTools.readFileText(dimensionId.getAbsolutePath()).trim());
                } catch (IOException err) {
                } catch (Throwable err) {
                    ICLog.e("ERROR", "failed to read dimension id file", err);
                    PrintStacking.print("FAILED TO READ DIMENSION ID");
                }

                setCurrentCustomDimension(getDimensionById(id));
            }
        }

        private static void writeCurrentDimensionId() {
            synchronized (dataLock) {
                String levelDir = LevelInfo.getAbsoluteDir();
                if (levelDir == null) {
                    return;
                }
                File dimensionId = new File(levelDir, "dimension-id");
                try {
                    FileTools.writeFileText(dimensionId.getAbsolutePath(),
                            String.valueOf(currentDimension != null ? currentDimension.getId() : -1));
                } catch (IOException err) {
                    ICLog.e("ERROR", "failed to write dimension id file", err);
                    PrintStacking.print("FAILED TO WRITE DIMENSION ID");
                }
            }
        }
    }

    public static void onLevelCreated() {
        DimensionDataHandler.loadAndSetupCurrentDimension();
    }

    public static void onDataSaved() {
        DimensionDataHandler.writeCurrentDimensionId();
    }
}
