package com.zhekasmirnov.innercore.api;

import com.reider745.InnerCoreServer;
import com.reider745.api.ClientOnly;
import com.zhekasmirnov.apparatus.multiplayer.Network;
import com.zhekasmirnov.apparatus.multiplayer.server.ConnectedClient;
import com.zhekasmirnov.innercore.api.runtime.saver.world.WorldDataScopeRegistry;
import org.innercore.icstd.serialization.*;
import org.json.JSONObject;
import org.mozilla.javascript.annotations.JSStaticFunction;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NativeScales {
    static {
        Network.getSingleton().addServerInitializationPacket("_ic.scales_send", client -> {
            final JSONObject json = new JSONObject();

            try {
                for (String scaleId : getAllScales()) {
                    json.put(scaleId, getScaleValue(client.getPlayerUid(), scaleId));
                }
            }catch(Exception ignore) {}

            return json;
        }, (data, meta, dataType) -> {
            if(data instanceof JSONObject) {
                final JSONObject json = (JSONObject) data;

                try {
                    for (Iterator<String> it = json.keys(); it.hasNext(); ) {
                        final String scaleId = it.next();
                        setScaleValue(NativeAPI.getLocalPlayer(), scaleId, json.getDouble(scaleId));
                    }
                }catch(Exception ignore) {}
            }
        });

        Network.getSingleton().addClientPacket("_ic.scales_set", (data, meta, dataType) -> {
            if(data instanceof JSONObject) {
                final JSONObject json = (JSONObject) data;
                try {
                    setScaleValue(NativeAPI.getLocalPlayer(), json.getString("scaleId"), json.getDouble("value"));
                } catch (Exception ignore) {}
            }
        });

        WorldDataScopeRegistry.getInstance().addScope("_ic.scales", new WorldDataScopeRegistry.SaverScope() {
            @Override
            public void read(IScopedReader io, SerializationContext ctx) {
                IObjectReader allScales = io.asObject();
                if (allScales == null) {
                    io.endScope();
                    return;
                }
                for (String mobKey : allScales.iterKeys()) {
                    final long mob;
                    try {
                        mob = Long.parseLong(mobKey);
                    } catch (NumberFormatException e) {
                        ctx.addError("invalid mob uid key in scales scope: " + mobKey);
                        continue;
                    }

                    IObjectReader mobScales = allScales.beginScope(mobKey).asObject();
                    if (mobScales != null) {
                        for (String scaleId : mobScales.iterKeys()) {
                            setScaleValue(mob, scaleId, mobScales.getDouble(scaleId, 0.));
                        }
                    }
                    allScales.endChildScope();
                }
                io.endScope();
            }

            @Override
            public void write(IScopedWriter io, SerializationContext ctx) {
                IObjectWriter allScales = io.asObject();
                for (long mob : getAllMobs()) {
                    IObjectWriter mobScales = allScales.beginScope(String.valueOf(mob)).asObject();
                    for (String scaleId : getAllScales()) {
                        mobScales.putDouble(scaleId, getScaleValue(mob, scaleId));
                    }
                    mobScales.endScope();
                }
                allScales.endScope();
            }
        });
    }

    public static void setServerScaleValue(long player, String scaleId, double value) {
        setScaleValue(player, scaleId, value);

        final ConnectedClient client = Network.getSingleton().getServer().getConnectedClientForPlayer(player);
        if(client != null) {
            final JSONObject data = new JSONObject();

            try {
                data.put("scaleId", scaleId);
                data.put("value", value);
            } catch (Exception ignore) {}

            client.send("_ic.scales_set", data);
        }
    }

    private static class ScaleDescription {
        private final long id;
        private double maxValue = 20;
        private double defaultValue = maxValue;

        private boolean hasResetAfterDeath = true;

        ScaleDescription(long id) {
            this.id = id;
        }
    }

    public static void onDeath(long deaths) {
        for(String key : getAllScales()) {
            for(long mob : getAllMobs()) {
                final ScaleDescription description = SCALES.get(key);
                setServerScaleValue(mob, key, description.defaultValue);
            }
        }
    }

    private static long ID = 0;
    private static final Map<String, ScaleDescription> SCALES = new HashMap<>();
    private static final Map<Long, String> SCALE_IDS = new HashMap<>();
    private static final Map<Long, Map<String, Double>> SCALE_VALUES = new HashMap<>();

    public static void setScaleValue(long player, String ptr, double value) {
        final Map<String, Double> values = SCALE_VALUES.computeIfAbsent(player, (k) -> new ConcurrentHashMap<>());
        values.put(ptr, value);
    }
    public static double getScaleValue(long player, String ptr) {
        final Map<String, Double> values = SCALE_VALUES.computeIfAbsent(player, (k) -> new ConcurrentHashMap<>());
        return values.getOrDefault(ptr, getDefaultValue(SCALES.get(ptr).id));
    }
    public static String[] getAllScales() {
        return SCALES.keySet().toArray(new String[0]);
    }
    public static Long[] getAllMobs() {
        return SCALE_VALUES.keySet().toArray(new Long[0]);
    }
    public static long getScaleByName(String name) {
        final ScaleDescription description = SCALES.get(name);
        if(description != null) {
            return description.id;
        }
        return 0;
    }

    public static long registerScale(String scaleId) {
        final long id = ++ID;

        SCALES.put(scaleId, new  ScaleDescription(id));
        SCALE_IDS.put(id, scaleId);

        return id;
    }

    public static double getMaxValue(long scaleId) {
        final ScaleDescription description = SCALES.get(SCALE_IDS.get(scaleId));
        if(description != null) {
            return description.maxValue;
        }
        return 20;
    }
    public static void setMaxValue(long scaleId, double value) {
        final ScaleDescription description = SCALES.get(SCALE_IDS.get(scaleId));
        if(description != null) {
            description.maxValue = value;
        }
    }

    public static double getDefaultValue(long scaleId) {
        final ScaleDescription description = SCALES.get(SCALE_IDS.get(scaleId));
        if(description != null) {
            return description.defaultValue;
        }
        return 20;
    }
    public static void setDefaultValue(long scaleId, double value) {
        final ScaleDescription description = SCALES.get(SCALE_IDS.get(scaleId));
        if(description != null) {
            description.defaultValue = value;
        }
    }

    @ClientOnly
    public static boolean hasLeft(long scaleId) {
        InnerCoreServer.useClientMethod("NativeScales.hasLeft");
        return true;
    }
    @ClientOnly
    public static void setLeft(long scaleId, boolean value) {
        InnerCoreServer.useClientMethod("NativeScales.setLeft");
    }

    public static boolean hasResetAfterDeath(long scaleId) {
        final ScaleDescription description = SCALES.get(SCALE_IDS.get(scaleId));
        if(description != null) {
            return description.hasResetAfterDeath;
        }
        return false;
    }
    public static void setResetAfterDeath(long scaleId, boolean value) {
        final ScaleDescription description = SCALES.get(SCALE_IDS.get(scaleId));
        if(description != null) {
            description.hasResetAfterDeath = value;
        }
    }

    @ClientOnly
    public static boolean hasDisplay(long scaleId) {
        InnerCoreServer.useClientMethod("NativeScales.hasDisplay");
        return false;
    }
    @ClientOnly
    public static void setDisplay(long scaleId, boolean value) {
        InnerCoreServer.useClientMethod("NativeScales.setDisplay");
    }

    @ClientOnly
    public static String getTextureFull(long scaleId) {
        InnerCoreServer.useClientMethod("NativeScales.getTextureFull");
        return "";
    }
    @ClientOnly
    public static void setTextureFull(long scaleId, String value) {
        InnerCoreServer.useClientMethod("NativeScales.setTextureFull");
    }

    @ClientOnly
    public static String getTextureHalf(long scaleId) {
        InnerCoreServer.useClientMethod("NativeScales.getTextureHalf");
        return "";
    }
    @ClientOnly
    public static void setTextureHalf(long scaleId, String value) {
        InnerCoreServer.useClientMethod("NativeScales.setTextureHalf");
    }

    @ClientOnly
    public static String getTextureEmpty(long scaleId) {
        InnerCoreServer.useClientMethod("NativeScales.getTextureEmpty");
        return "";
    }
    @ClientOnly
    public static void setTextureEmpty(long scaleId, String value) {
        InnerCoreServer.useClientMethod("NativeScales.setTextureEmpty");
    }

    public static class CustomScale {
        private static final Map<String, CustomScale> instances = new HashMap<>();

        @JSStaticFunction
        public static CustomScale getScaleByName(String name) {
            return instances.get(name);
        }

        @JSStaticFunction
        public static CustomScale[] getAllScales() {
            return instances.values().toArray(new CustomScale[0]);
        }

        private final String scaleId;
        private final long ptr;

        public CustomScale(String scaleId, String full, String half, String empty) {
            if(NativeScales.getScaleByName(scaleId) != 0) {
                throw new IllegalArgumentException("Scale with id " + scaleId + " already exists");
            }

            this.scaleId = scaleId;
            ptr = registerScale(scaleId);

            setTextureFull(full);
            setTextureHalf(half);
            setTextureEmpty(empty);

            instances.put(scaleId, this);
        }

        public long getPointer() {
            return ptr;
        }

        public String getScaleId() {
            return scaleId;
        }

        public double getMaxValue() {
            return NativeScales.getMaxValue(ptr);
        }

        public CustomScale setMaxValue(double value) {
            NativeScales.setMaxValue(ptr, value);
            return this;
        }

        public double getDefaultValue() {
            return NativeScales.getDefaultValue(ptr);
        }

        public CustomScale setDefaultValue(double value) {
            NativeScales.setDefaultValue(ptr, value);
            return this;
        }

        public boolean hasLeft() {
            return NativeScales.hasLeft(ptr);
        }

        public CustomScale setLeft(boolean value) {
            NativeScales.setLeft(ptr, value);
            return this;
        }

        public boolean hasResetAfterDeath() {
            return NativeScales.hasResetAfterDeath(ptr);
        }

        public CustomScale setResetAfterDeath(boolean value) {
            NativeScales.setResetAfterDeath(ptr, value);
            return this;
        }

        public boolean hasDisplay() {
            return NativeScales.hasDisplay(ptr);
        }

        public CustomScale setDisplay(boolean value) {
            NativeScales.setDisplay(ptr, value);
            return this;
        }

        public String getTextureFull() {
            return NativeScales.getTextureFull(ptr);
        }

        public CustomScale setTextureFull(String value) {
            NativeScales.setTextureFull(ptr, value);
            return this;
        }

        public String getTextureHalf() {
            return NativeScales.getTextureHalf(ptr);
        }

        public CustomScale setTextureHalf(String value) {
            NativeScales.setTextureHalf(ptr, value);
            return this;
        }

        public String getTextureEmpty() {
            return NativeScales.getTextureEmpty(ptr);
        }

        public CustomScale setTextureEmpty(String value) {
            NativeScales.setTextureEmpty(ptr, value);
            return this;
        }
    }
}
