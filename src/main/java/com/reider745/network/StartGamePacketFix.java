package com.reider745.network;

import cn.nukkit.Server;
import cn.nukkit.item.RuntimeItems;
import cn.nukkit.level.GlobalBlockPalette;
import cn.nukkit.nbt.NBTIO;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.network.protocol.StartGamePacket;
import cn.nukkit.network.protocol.types.ExperimentData;

import java.io.IOException;
import java.util.UUID;

public class StartGamePacketFix extends StartGamePacket {
    public int dimensionFix;

    @Override
    public void encode() {
        this.reset();
        this.putEntityUniqueId(this.entityUniqueId);
        this.putEntityRuntimeId(this.entityRuntimeId);
        this.putVarInt(this.playerGamemode);
        this.putVector3f(this.x, this.y, this.z);
        this.putLFloat(this.yaw);
        this.putLFloat(this.pitch);

        /* Level settings start */
        if (protocol >= ProtocolInfo.v1_18_30) {
            this.putLLong(this.seed);
        }else {
            this.putVarInt(this.seed);
        }
        if (protocol >= 407) {
            this.putLShort(0x00); // SpawnBiomeType - Default
            this.putString(protocol >= ProtocolInfo.v1_16_100 ? "plains" : ""); // UserDefinedBiomeName
        }
        this.putVarInt(this.dimensionFix);
        this.putVarInt(this.generator);
        this.putVarInt(this.worldGamemode);
        this.putVarInt(this.difficulty);
        this.putBlockVector3(this.spawnX, this.spawnY, this.spawnZ);
        this.putBoolean(this.hasAchievementsDisabled);
        if (protocol >= ProtocolInfo.v1_19_10) {
            this.putBoolean(this.worldEditor);
            if (protocol >= ProtocolInfo.v1_19_80) {
                this.putBoolean(this.createdInEditor);
                this.putBoolean(this.exportedFromEditor);
            }
        }
        this.putVarInt(this.dayCycleStopTime);
        if (protocol >= 388) {
            this.putVarInt(this.eduEditionOffer);
        } else {
            this.putBoolean(this.eduMode);
        }
        if (protocol > 224) {
            this.putBoolean(this.hasEduFeaturesEnabled);
            if (protocol >= 407) {
                this.putString(""); // Education Edition Product ID
            }
        }
        this.putLFloat(this.rainLevel);
        this.putLFloat(this.lightningLevel);
        if (protocol >= 332) {
            this.putBoolean(this.hasConfirmedPlatformLockedContent);
        }
        this.putBoolean(this.multiplayerGame);
        this.putBoolean(this.broadcastToLAN);
        if (protocol >= 332) {
            this.putVarInt(this.xblBroadcastIntent);
            this.putVarInt(this.platformBroadcastIntent);
        } else {
            this.putBoolean(this.broadcastToXboxLive);
        }
        this.putBoolean(this.commandsEnabled);
        this.putBoolean(this.isTexturePacksRequired);
        this.putGameRules(protocol, gameRules);
        if (protocol >= ProtocolInfo.v1_16_100) {
            if (Server.getInstance().enableExperimentMode && !this.experiments.isEmpty()) {
                this.putLInt(this.experiments.size()); // Experiment count
                for (ExperimentData experiment : this.experiments) {
                    this.putString(experiment.getName());
                    this.putBoolean(experiment.isEnabled());
                }
                this.putBoolean(true); // Were experiments previously toggled
            } else {
                this.putLInt(0); // Experiment count
                this.putBoolean(false); // Were experiments previously toggled
            }
        }
        this.putBoolean(this.bonusChest);
        if (protocol > 201) {
            this.putBoolean(this.hasStartWithMapEnabled);
        }
        if (protocol < 332) {
            this.putBoolean(this.trustPlayers);
        }
        this.putVarInt(this.permissionLevel);
        if (protocol < 332) {
            this.putVarInt(this.gamePublish);
        }
        if (protocol >= 201) {
            this.putLInt(this.serverChunkTickRange);
        }
        if (protocol >= 223 && protocol < 332) {
            this.putBoolean(this.broadcastToPlatform);
            this.putVarInt(this.platformBroadcastMode);
            this.putBoolean(this.xblBroadcastIntentOld);
        }
        if (protocol > 224) {
            this.putBoolean(this.hasLockedBehaviorPack);
            this.putBoolean(this.hasLockedResourcePack);
            this.putBoolean(this.isFromLockedWorldTemplate);
        }
        if (protocol >= 291) {
            this.putBoolean(this.isUsingMsaGamertagsOnly);
            if (protocol >= 313) {
                this.putBoolean(this.isFromWorldTemplate);
                this.putBoolean(this.isWorldTemplateOptionLocked);
                if (protocol >= 361) {
                    this.putBoolean(this.isOnlySpawningV1Villagers);
                    if (protocol >= ProtocolInfo.v1_13_0) {
                        if (protocol >= ProtocolInfo.v1_19_20) {
                            this.putBoolean(this.isDisablingPersonas);
                            this.putBoolean(this.isDisablingCustomSkins);
                            if (protocol >= ProtocolInfo.v1_19_60) {
                                this.putBoolean(this.emoteChatMuted);
                            }
                        }
                        this.putString(this.vanillaVersion);
                    }
                }
            }
            if (protocol >= ProtocolInfo.v1_16_0) {
                this.putLInt(protocol >= ProtocolInfo.v1_16_100 ? 16 : 0); // Limited world width
                this.putLInt(protocol >= ProtocolInfo.v1_16_100 ? 16 : 0); // Limited world height
                this.putBoolean(false); // Nether type
                if (protocol >= ProtocolInfo.v1_17_30) { // EduSharedUriResource
                    this.putString(""); // buttonName
                    this.putString(""); // linkUri
                }
                this.putBoolean(/*Server.getInstance().enableExperimentMode*/ false); //Force Experimental Gameplay (exclusive to debug clients)
                if (protocol >= ProtocolInfo.v1_19_20) {
                    this.putByte(this.chatRestrictionLevel);
                    this.putBoolean(this.disablePlayerInteractions);
                }
            }
        }
        /* Level settings end */

        this.putString(this.levelId);
        this.putString(this.worldName);
        this.putString(this.premiumWorldTemplateId);
        this.putBoolean(this.isTrial);
        if (protocol >= ProtocolInfo.v1_13_0) {
            if (protocol >= ProtocolInfo.v1_16_100) {
                if (protocol >= ProtocolInfo.v1_16_210) {
                    this.putVarInt(this.isMovementServerAuthoritative ? 1 : 0); // 2 - rewind
                    this.putVarInt(0); // RewindHistorySize
                    this.putBoolean(this.isServerAuthoritativeBlockBreaking); // isServerAuthoritativeBlockBreaking
                } else {
                    this.putVarInt(this.isMovementServerAuthoritative ? 1 : 0); // 2 - rewind
                }
            } else {
                this.putBoolean(this.isMovementServerAuthoritative);
            }
        }
        this.putLLong(this.currentTick);
        this.putVarInt(this.enchantmentSeed);
        if (protocol > ProtocolInfo.v1_5_0) {
            if (protocol >= ProtocolInfo.v1_16_100) {
                this.putUnsignedVarInt(0); // Custom blocks
            } else {
                this.put(GlobalBlockPalette.getCompiledTable(this.protocol));
            }
            if (protocol >= ProtocolInfo.v1_12_0) {
                this.put(RuntimeItems.getMapping(protocol).getItemPalette());
            }
            this.putString(this.multiplayerCorrelationId);
            if (protocol == 354 && version != null && version.startsWith("1.11.4")) {
                this.putBoolean(this.isOnlySpawningV1Villagers);
            } else if (protocol >= ProtocolInfo.v1_16_0) {
                this.putBoolean(false); // isInventoryServerAuthoritative
                if (protocol >= ProtocolInfo.v1_16_230_50) {
                    this.putString(""); // serverEngine
                    if (protocol >= ProtocolInfo.v1_18_0) {
                        if (protocol >= ProtocolInfo.v1_19_0_29) {
                            try {
                                this.put(NBTIO.writeNetwork(this.playerPropertyData));
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        this.putLLong(0L); // BlockRegistryChecksum
                        if (protocol >= ProtocolInfo.v1_19_0_29) {
                            this.putUUID(new UUID(0, 0)); // worldTemplateId
                            if (protocol >= ProtocolInfo.v1_19_20) {
                                this.putBoolean(this.clientSideGenerationEnabled);
                                if (protocol >= ProtocolInfo.v1_19_80) {
                                    this.putBoolean(this.blockNetworkIdsHashed);
                                    if (protocol >= ProtocolInfo.v1_20_0_23) {
                                        this.putBoolean(this.networkPermissions.isServerAuthSounds());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
