package com.reider745.hooks.world;

import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.blockentity.BlockEntitySpawnable;
import cn.nukkit.level.DimensionData;
import cn.nukkit.level.format.ChunkSection;
import cn.nukkit.level.format.LevelProvider;
import cn.nukkit.level.format.generic.BaseChunk;
import cn.nukkit.level.format.generic.serializer.NetworkChunkData;
import cn.nukkit.level.format.generic.serializer.NetworkChunkSerializer;
import cn.nukkit.level.format.leveldb.LevelDBProvider;
import cn.nukkit.level.format.leveldb.serializer.ChunkDataLoader;
import cn.nukkit.level.format.leveldb.structure.ChunkBuilder;
import cn.nukkit.level.format.leveldb.structure.ChunkState;
import cn.nukkit.level.format.leveldb.structure.LevelDBChunk;
import cn.nukkit.level.generator.OldNormal;
import cn.nukkit.level.util.PalettedBlockStorage;
import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.protocol.BiomeDefinitionListPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.utils.BinaryStream;
import cn.nukkit.utils.ThreadCache;
import com.reider745.InnerCoreServer;
import com.reider745.api.hooks.TypeHook;
import com.reider745.api.hooks.annotation.Hooks;
import com.reider745.api.hooks.annotation.Inject;
import com.reider745.world.BiomesMethods;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.WriteBatch;

import java.io.IOException;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;


@Hooks(className = "cn.nukkit.level.format.leveldb.structure.LevelDBChunk")
public class BiomesHooks {
    @Inject(className = "cn.nukkit.network.protocol.BiomeDefinitionListPacket")
    public static void encode(BiomeDefinitionListPacket self) {
        BiomesMethods.encode(self);
    }

    // Фикс отправки биома на клиент
    @Inject(type = TypeHook.AFTER_NOT_REPLACE)
    public static void setBiomeId(LevelDBChunk chunk, int x, int y, int z, int id) {
        chunk.getBiomeIdArray()[x << 4 | z] = (byte) id;
    }

    @Inject(type = TypeHook.AFTER_NOT_REPLACE)
    public static void setBiomeId(LevelDBChunk chunk, int x, int z, int id) {
        chunk.getBiomeIdArray()[x << 4 | z] = (byte) id;
    }

    // Исправление отображения биома, после сохранения
    private static final ChunkDataLoader LOADER = (chunk, provider) -> {
        for(int x = 0;x < 16;x++)
            for(int z = 0;z < 16;z++)
                setBiomeId(chunk, x, z, chunk.getBiomeId(x, z));
    };

    @Inject(className = "cn.nukkit.level.format.leveldb.structure.ChunkBuilder", type = TypeHook.BEFORE_NOT_REPLACE)
    public static void build(ChunkBuilder self) {
        self.dataLoader(LOADER);
    }

    @Inject(className = "cn.nukkit.level.generator.OldNormal", type = TypeHook.AFTER_NOT_REPLACE)
    public static void generateChunk(OldNormal normal, int chunkX, int chunkZ) {
        // TODO: Не поддерживает "пользовательские измерения" от чистого накита
        BiomesMethods.onChunkPopulate(normal.getChunkManager().getChunk(chunkX, chunkZ), chunkX, chunkZ, normal.getDimensionData().getDimensionId());
    }

    private static byte[] serializeEntities(BaseChunk chunk, int protocolId) {
        List<CompoundTag> tagList = new ObjectArrayList<>();
        for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
            if (blockEntity instanceof BlockEntitySpawnable) {
                tagList.add(((BlockEntitySpawnable) blockEntity).getSpawnCompound(protocolId));
            }
        }

        try {
            return NBTIO.write(tagList, ByteOrder.LITTLE_ENDIAN, true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }



    //1.14 - 1.17
    /*@Inject(className = "cn.nukkit.level.format.generic.serializer.NetworkChunkSerializer", type = TypeHook.RETURN)
    public static void serialize(IntSet protocols, BaseChunk chunk, Consumer<NetworkChunkSerializer.NetworkChunkSerializerCallback> callback, boolean antiXray, DimensionData dimensionData) {
        for (int protocolId : protocols) {
            int subChunkCount = 0;
            ChunkSection[] sections = chunk.getSections();
            for (int i = sections.length - 1; i >= 0; i--) {
                if (!sections[i].isEmpty()) {
                    subChunkCount = i + 1;
                    break;
                }
            }

            BinaryStream stream = ThreadCache.binaryStream.get().reset();
            NetworkChunkData networkChunkData = new NetworkChunkData(protocolId, subChunkCount, antiXray, dimensionData);

            subChunkCount = Math.max(1, subChunkCount - chunk.getSectionOffset());
            networkChunkData.setChunkSections(subChunkCount);

            int offset = chunk.getSectionOffset();
            for (int i = offset; i < subChunkCount + offset; i++) {
                sections[i].writeTo(protocolId, stream, antiXray);
            }

            stream.put(chunk.getBiomeIdArray());

            stream.putByte((byte) 0);
            if (chunk.getBlockEntities().isEmpty()) {
                stream.put(new byte[0]);
            } else {
                stream.put(serializeEntities(chunk, protocolId));
            }

            callback.accept(new NetworkChunkSerializer.NetworkChunkSerializerCallback(protocolId, stream, networkChunkData.getChunkSections()));
        }
    }*/

    /*@Inject(className = "cn.nukkit.level.format.leveldb.serializer.Data3dSerializer")
    public static void serialize(WriteBatch db, LevelDBChunk chunk) {
        System.out.println("ABOBA-serialize");
    }

    @Inject(className = "cn.nukkit.level.format.leveldb.serializer.Data3dSerializer")
    public static void deserialize(DB dB, ChunkBuilder chunkBuilder){
        System.out.println("ABOBA-deserialize");
    }*/
}
