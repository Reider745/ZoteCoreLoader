package com.reider745.hooks;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

import com.reider745.api.hooks.HookClass;
import com.reider745.api.hooks.annotation.Hooks;
import com.reider745.api.hooks.annotation.Inject;

import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.blockentity.BlockEntitySpawnable;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.weather.LightningStrikeEvent;
import cn.nukkit.level.DimensionData;
import cn.nukkit.level.biome.Biome;
import cn.nukkit.level.format.ChunkSection;
import cn.nukkit.level.format.generic.BaseChunk;
import cn.nukkit.level.format.generic.serializer.NetworkChunkSerializer;
import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.utils.BinaryStream;
import cn.nukkit.utils.ThreadCache;

import java.nio.ByteOrder;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import javassist.CtClass;

/**
 * Special christmas adaptation, do not use anywhere else.
 */
@Hooks(className = "cn.nukkit.level.format.generic.serializer.NetworkChunkSerializer")
public class SnowfallEverywhere implements HookClass, Listener {
	public static boolean isActive = false;

	public void init(CtClass clazz) {
		isActive = true;
	}

	@Inject
	public static void serialize(IntSet protocols, BaseChunk chunk,
			Consumer<NetworkChunkSerializer.NetworkChunkSerializerCallback> callback, DimensionData dimensionData) {
		for (int protocolId : protocols) {
			byte[] blockEntities;
			if (chunk.getBlockEntities().isEmpty()) {
				blockEntities = new byte[0];
			} else {
				blockEntities = serializeEntities(chunk, protocolId);
			}

			int subChunkCount = 0;
			ChunkSection[] sections = chunk.getSections();
			for (int i = sections.length - 1; i >= 0; i--) {
				if (!sections[i].isEmpty()) {
					subChunkCount = i + 1;
					break;
				}
			}

			BinaryStream stream = ThreadCache.binaryStream.get().reset();
			for (int i = 0; i < subChunkCount; i++) {
				sections[i].writeTo(protocolId, stream, false);
			}

			stream.put(toSnowfallBiomeIdArray(chunk.getBiomeIdArray()));
			// Border blocks
			stream.putByte((byte) 0);
			stream.put(blockEntities);

			callback.accept(
					new NetworkChunkSerializer.NetworkChunkSerializerCallback(protocolId, stream, subChunkCount));
		}
	}

	private static byte[] toSnowfallBiomeIdArray(byte[] biomes) {
		for (int i = 0, l = biomes.length; i < l; i++) {
			Biome biome = Biome.getBiome(biomes[i] & 0xFF);
			if (biome.isFreezing() || !biome.canRain()) {
				continue;
			}
			biomes[i] = (byte) switch (biomes[i] & 0xFF) {
				case 0, 24 -> 10; // OCEAN, DEEP_OCEAN -> FROZEN_OCEAN
				case 7 -> 11; // RIVER -> FROZEN_RIVER
				case 14, 15, 16, 25 -> 26; // MUSHROOM_ISLAND, MUSHROOM_ISLAND_SHORE, BEACH, STONE_BEACH -> COLD_BEACH
				// PLAINS, FOREST, SWAMP, JUNGLE, BIRCH_FOREST, ROOFED_FOREST, SUNFLOWER_PLAINS,
				// FLOWER_FOREST, SWAMPLAND_M, JUNGLE_M, JUNGLE_EDGE_M, BIRCH_FOREST_M,
				// BIRCH_FOREST_HILLS_M, ROOFED_FOREST_M, EXTREME_HILLS_PLUS_M -> ICE_PLAINS
				case 1, 4, 6, 21, 27, 29, 34, 131, 132, 134, 149, 151, 155, 156, 157, 162 -> 12;
				// EXTREME_HILLS, FOREST_HILLS, EXTREME_HILLS_EDGE, JUNGLE_HILLS, JUNGLE_EDGE,
				// BIRCH_FOREST_HILLS, EXTREME_HILLS_PLUS, EXTREME_HILLS_M -> ICE_PLAINS_SPIKES
				case 3, 18, 20, 22, 23, 28, 129 -> 140;
				case 5, 32, 160 -> 30; // TAIGA, MEGA_TAIGA, MEGA_SPRUCE_TAIGA -> COLD_TAIGA
				case 133 -> 158; // TAIGA_M -> COLD_TAIGA_M
				case 19, 33 -> 32; // TAIGA_HILLS, MEGA_TAIGA_HILLS -> COLD_TAIGA_HILLS
				default -> 10; // FROZEN_OCEAN
			};
		}
		return biomes;
	}

	private static byte[] serializeEntities(BaseChunk chunk, int protocol) {
		List<CompoundTag> tagList = new ObjectArrayList<>();
		for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
			if (blockEntity instanceof BlockEntitySpawnable) {
				tagList.add(((BlockEntitySpawnable) blockEntity).getSpawnCompound(protocol));
			}
		}

		try {
			return NBTIO.write(tagList, ByteOrder.LITTLE_ENDIAN, true);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@EventHandler
	public void onLightningStrike(LightningStrikeEvent event) {
		event.setCancelled();
	}
}
