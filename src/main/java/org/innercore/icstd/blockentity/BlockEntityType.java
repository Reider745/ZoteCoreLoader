package org.innercore.icstd.blockentity;

import com.zhekasmirnov.apparatus.multiplayer.util.entity.NetworkEntityType;
import org.innercore.icstd.serialization.*;

public class BlockEntityType {
    String typeName;
    Class<? extends BlockEntityComponent> typeClass;
    IBlockEntityFactory<?> factory;
    int blockId;
    String serializationTypeId;
    NetworkEntityType netEntityType;

    private boolean isInitialized = false;

    public BlockEntityType initForClass() {
        if (isInitialized) {
            throw new IllegalStateException("BlockEntityType is already initialized");
        }
        isInitialized = true;
        if (SerializationMethodsComponent.class.isAssignableFrom(typeClass)) {
            if (serializationTypeId == null) {
                serializationTypeId = "b_ent:" + typeName;
            }
            serializationTypeId = ObjectSerializer.registerType(serializationTypeId, (Class) typeClass, new IObjectSerializer() {
                @Override
                public Object read(IScopedReader reader, SerializationContext ctx, String typeId) {
                    Object blockEntity = factory.createBlockEntity();
                    ((SerializationMethodsComponent) blockEntity).read(reader, ctx, typeId);
                    return blockEntity;
                }

                @Override
                public void write(IScopedWriter writer, Object object, SerializationContext ctx, String typeId) {
                    ((SerializationMethodsComponent) object).write(writer, ctx, typeId);
                }
            });
        }
        return this;
    }

    public String getTypeName() {
        return typeName;
    }

    public Class<? extends BlockEntityComponent> getTypeClass() {
        return typeClass;
    }

    public NetworkEntityType getNetEntityType() {
        return netEntityType;
    }

    public String getSerializationTypeId() {
        return serializationTypeId;
    }

    public IBlockEntityFactory<?> getFactory() {
        return factory;
    }

    public int getBlockId() {
        return blockId;
    }
}
