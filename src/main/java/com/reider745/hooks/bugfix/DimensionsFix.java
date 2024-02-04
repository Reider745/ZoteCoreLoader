package com.reider745.hooks.bugfix;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.entity.data.ByteEntityData;
import cn.nukkit.entity.data.property.EntityProperty;
import cn.nukkit.event.player.PlayerLoginEvent;
import cn.nukkit.item.Item;
import cn.nukkit.item.trim.TrimFactory;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.protocol.*;
import cn.nukkit.network.protocol.types.ContainerIds;
import cn.nukkit.network.protocol.types.ExperimentData;
import cn.nukkit.network.protocol.types.GameType;
import cn.nukkit.utils.TextFormat;
import cn.nukkit.utils.Utils;
import com.reider745.InnerCoreServer;
import com.reider745.api.hooks.HookClass;
import com.reider745.api.hooks.annotation.Hooks;
import com.reider745.api.hooks.annotation.Inject;
import com.reider745.network.StartGamePacketFix;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.List;

@Hooks
public class DimensionsFix implements HookClass {
    private static int getClientFriendlyGamemode(Player self, int gamemode) {
        gamemode &= 0x03;
        if (gamemode == Player.SPECTATOR) {
            //1.19.30+使用真正的旁观模式
            if (self.getServer().useClientSpectator && self.protocol >= ProtocolInfo.v1_19_30) {
                return GameType.SPECTATOR.ordinal();
            }
            return Player.CREATIVE;
        }
        return gamemode;
    }

    private static List<ExperimentData> experiments;

    public static void init() {
        final int protocol = InnerCoreServer.PROTOCOL;

        List<ExperimentData> experiments = new ObjectArrayList<>();
        //TODO Multiversion 当新版本删除部分实验性玩法时，这里也需要加上判断
        if (Server.getInstance().enableExperimentMode) {
            experiments.add(new ExperimentData("data_driven_items", true));
            experiments.add(new ExperimentData("experimental_custom_ui", true));
            experiments.add(new ExperimentData("upcoming_creator_features", true));
            experiments.add(new ExperimentData("experimental_molang_features", true));
            if (protocol >= ProtocolInfo.v1_20_0_23) {
                experiments.add(new ExperimentData("cameras", true));
                if (protocol >= ProtocolInfo.v1_20_10_21 && protocol < ProtocolInfo.v1_20_30_24) {
                    experiments.add(new ExperimentData("short_sneaking", true));
                }
            }
        }
        DimensionsFix.experiments = experiments;
    }

    @Inject(className = "cn.nukkit.Player")
    public static void completeLoginSequence(Player self){
        final Server server = self.getServer();

        if (self.loggedIn) {
            server.getLogger().debug("(BUG) Tried to call completeLoginSequence but player is already logged in");
            return;
        }

        PlayerLoginEvent ev;
        server.getPluginManager().callEvent(ev = new PlayerLoginEvent(self, "Plugin reason"));
        if (ev.isCancelled()) {
            self.close(self.getLeaveMessage(), ev.getKickMessage());
            return;
        }

        StartGamePacketFix startGamePacket = new StartGamePacketFix();
        startGamePacket.entityUniqueId = self.getId();
        startGamePacket.entityRuntimeId = self.getId();
        startGamePacket.playerGamemode = getClientFriendlyGamemode(self, self.gamemode);
        startGamePacket.x = (float) self.x;
        startGamePacket.y = (float) self.y;
        startGamePacket.z = (float) self.z;
        startGamePacket.yaw = (float) self.yaw;
        startGamePacket.pitch = (float) self.pitch;
        startGamePacket.dimensionFix = self.level.getDimension();
        startGamePacket.generator = (byte) ((self.level.getDimension() + 1) & 0xff); //0 旧世界, 1 主世界, 2 下界, 3末地
        startGamePacket.worldGamemode = startGamePacket.playerGamemode;
        startGamePacket.difficulty = server.getDifficulty();
        startGamePacket.spawnX = (int) self.x;
        startGamePacket.spawnY = (int) self.y;
        startGamePacket.spawnZ = (int) self.z;
        startGamePacket.commandsEnabled = self.isEnableClientCommand();
        startGamePacket.experiments.addAll(experiments);
        startGamePacket.gameRules = self.getLevel().getGameRules();
        startGamePacket.worldName = self.getServer().getNetwork().getName();
        startGamePacket.version = self.getLoginChainData().getGameVersion();
        startGamePacket.vanillaVersion = Utils.getVersionByProtocol(self.protocol);
        if (self.getLevel().isRaining()) {
            startGamePacket.rainLevel = self.getLevel().getRainTime();
            if (self.getLevel().isThundering()) {
                startGamePacket.lightningLevel = self.getLevel().getThunderTime();
            }
        }
        startGamePacket.isMovementServerAuthoritative = self.isMovementServerAuthoritative();
        startGamePacket.isServerAuthoritativeBlockBreaking = self.isServerAuthoritativeBlockBreaking();
        startGamePacket.playerPropertyData = EntityProperty.getPlayerPropertyCache();
        self.forceDataPacket(startGamePacket, null);

        self.loggedIn = true;
        server.getLogger().info(self.getServer().getLanguage().translateString("nukkit.player.logIn",
                TextFormat.AQUA + self.getDisplayName() + TextFormat.WHITE,
                self.getAddress(),
                String.valueOf(self.getPort()),
                self.protocol + " (" + Utils.getVersionByProtocol(self.protocol) + ")"));

        self.setDataFlag(Player.DATA_FLAGS, Player.DATA_FLAG_CAN_CLIMB, true, false);
        self.setDataFlag(Player.DATA_FLAGS, Player.DATA_FLAG_CAN_SHOW_NAMETAG, true, false);
        self.setDataProperty(new ByteEntityData(Player.DATA_ALWAYS_SHOW_NAMETAG, 1), false);

        try {
            if (self.protocol >= ProtocolInfo.v1_8_0) {
                if (self.protocol >= ProtocolInfo.v1_12_0) {
                    if (self.protocol >= ProtocolInfo.v1_16_100) {
                        if (self.protocol >= ProtocolInfo.v1_17_0) {
                            //注册实体属性
                            for (SyncEntityPropertyPacket pk : EntityProperty.getPacketCache()) {
                                self.dataPacket(pk);
                            }
                        }
                        ItemComponentPacket itemComponentPacket = new ItemComponentPacket();
                        if (server.enableExperimentMode && !Item.getCustomItemDefinition().isEmpty()) {
                            Int2ObjectOpenHashMap<ItemComponentPacket.Entry> entries = new Int2ObjectOpenHashMap<>();
                            int i = 0;
                            for (var entry : Item.getCustomItemDefinition().entrySet()) {
                                try {
                                    CompoundTag data = entry.getValue().getNbt(self.protocol);
                                    data.putShort("minecraft:identifier", i);
                                    entries.put(i, new ItemComponentPacket.Entry(entry.getKey(), data));
                                    i++;
                                } catch (Exception e) {
                                    server.getLogger().error("ItemComponentPacket encoding error", e);
                                }
                            }
                            itemComponentPacket.setEntries(entries.values().toArray(ItemComponentPacket.Entry.EMPTY_ARRAY));
                        }
                        self.dataPacket(itemComponentPacket);
                    }
                    self.dataPacket(new BiomeDefinitionListPacket());
                }
                self.dataPacket(new AvailableEntityIdentifiersPacket());
            }

            if (self.protocol >= ProtocolInfo.v1_16_100) {
                SetSpawnPositionPacket pk = new SetSpawnPositionPacket();
                pk.spawnType = SetSpawnPositionPacket.TYPE_PLAYER_SPAWN;
                pk.x = (int) self.x;
                pk.y = (int) self.y;
                pk.z = (int) self.z;
                pk.dimension = self.level.getDimension();
                self.dataPacket(pk);
            }
            self.getLevel().sendTime(self);

            SetDifficultyPacket difficultyPacket = new SetDifficultyPacket();
            difficultyPacket.difficulty = server.getDifficulty();
            self.dataPacket(difficultyPacket);

            SetCommandsEnabledPacket commandsPacket = new SetCommandsEnabledPacket();
            commandsPacket.enabled = self.isEnableClientCommand();
            self.dataPacket(commandsPacket);

            self.getAdventureSettings().update();

            GameRulesChangedPacket gameRulesPK = new GameRulesChangedPacket();
            gameRulesPK.gameRulesMap = self.getLevel().getGameRules().getGameRules();
            self.dataPacket(gameRulesPK);

            server.sendFullPlayerListData(self);
            self.sendAttributes();

            if (self.protocol < ProtocolInfo.v1_16_0 && self.gamemode == Player.SPECTATOR) {
                InventoryContentPacket inventoryContentPacket = new InventoryContentPacket();
                inventoryContentPacket.inventoryId = ContainerIds.CREATIVE;
                self.dataPacket(inventoryContentPacket);
            } else {
                self.getInventory().sendCreativeContents();
            }
            self.sendAllInventories();
            self.getInventory().sendHeldItemIfNotAir(self);

            // BDS sends armor trim templates and materials before the CraftingDataPacket
            if (self.protocol >= ProtocolInfo.v1_19_80) {
                TrimDataPacket trimDataPacket = new TrimDataPacket();
                trimDataPacket.getMaterials().addAll(TrimFactory.trimMaterials);
                trimDataPacket.getPatterns().addAll(TrimFactory.trimPatterns);
                self.dataPacket(trimDataPacket);
            }

            server.sendRecipeList(self);

            if (self.isEnableClientCommand()) {
                self.sendCommandData();
            }

            self.sendPotionEffects(self);

            if (self.isSpectator()) {
                self.setDataFlag(Player.DATA_FLAGS, Player.DATA_FLAG_SILENT, true);
                self.setDataFlag(Player.DATA_FLAGS, Player.DATA_FLAG_HAS_COLLISION, false);
            }
            self.sendData(self, self.getDataProperties().clone());

            // TODO: Jetpacks! Actually for disabling flight without affecting it.
            // if (!server.checkOpMovement && self.isOp()) {
                self.setCheckMovement(false);
            // }

            if (self.isOp() || self.hasPermission("nukkit.textcolor")) {
                self.setRemoveFormat(false);
            }

            server.onPlayerCompleteLoginSequence(self);
        } catch (Exception e) {
            self.close("", "Internal Server Error");
            server.getLogger().logException(e);
        }
    }
}
