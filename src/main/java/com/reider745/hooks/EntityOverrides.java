package com.reider745.hooks;

import cn.nukkit.entity.Entity;
import cn.nukkit.entity.custom.EntityDefinition;
import cn.nukkit.entity.custom.EntityManager;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import com.reider745.api.ReflectHelper;
import com.reider745.api.hooks.HookClass;
import com.reider745.api.hooks.annotation.Hooks;
import com.reider745.api.hooks.annotation.Inject;
import com.reider745.behavior.entities.CustomEntity;
import com.reider745.behavior.entities.EntityContentFactory;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import com.zhekasmirnov.horizon.util.FileUtils;
import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Hooks(className = "cn.nukkit.entity.item.EntityArmorStand")
public class EntityOverrides implements HookClass {
    @Override
    public void init(CtClass ctClass) {
        try {
            CtMethod onInteractMethod = ctClass.getDeclaredMethod("onInteract");
            onInteractMethod.insertAt(onInteractMethod.getMethodInfo().getLineNumber(70),
                    "if (flag && item instanceof com.reider745.item.CustomArmorItem) {"
                            + "i = ((com.reider745.item.CustomArmorItem) item).getArmorSlot();"
                            + "isArmorSlot = true;"
                            + "}");
        } catch (NotFoundException | CannotCompileException e) {
            System.out.println("Nukkit-MOT has been updated and overrides for `EntityArmorStand.onInteract` method are no longer available.");
        }
    }

    private static Field fieldId;
    private static final HashMap<String, Integer> playerIdForUUID = new HashMap<>();
    private static int id_start = Integer.MIN_VALUE;

    static {
        try {
            fieldId = Entity.class.getDeclaredField("id");
            fieldId.setAccessible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try{
            final File file = new File("players_uuid.json");
            if(file.exists()){
                final JSONObject json = FileUtils.readJSON(file);

                for(String key : json.keySet()) {
                    Integer id = json.getInt(key);
                    id_start = Math.max(id_start, id);
                    playerIdForUUID.put(key, id);
                }
            }
        }catch (IOException e){
            Logger.error("Read players uuid", e);
            throw new RuntimeException("Error loaded ZoteCoreLoader");
        }
    }

    public static int getOrCreate(UUID uuid) throws IOException {
        final String strUUID = uuid.toString();
        Integer id = playerIdForUUID.get(strUUID);

        if(id == null){
            id = ++id_start;
            playerIdForUUID.put(strUUID, id);
            FileUtils.writeJSON(new File("players_uuid.json"), new JSONObject(playerIdForUUID));
        }

        return id;
    }

    @Inject(className = "cn.nukkit.entity.Entity")
    public static void scheduleUpdate(Entity self){
        try {
            if(self.isPlayer)
                fieldId.set(self, (long) getOrCreate(self.getUniqueId()));
        } catch (IllegalAccessException | IOException e) {
            self.getServer().getLogger().logException(e);
        }
    }


    // Патч для кастумных мобов в наборах параметров
    @Inject(
            className = "cn.nukkit.entity.Entity",
            signature = "(Ljava/lang/String;Lcn/nukkit/level/format/FullChunk;Lcn/nukkit/nbt/tag/CompoundTag;[Ljava/lang/Object;)Lcn/nukkit/entity/Entity;"
    )
    public static Entity createEntity(String name, FullChunk chunk, CompoundTag nbt, Object... args) {
        final String identifier = Character.toLowerCase(name.charAt(0)) + name.substring(1);
        final Map<String, Class<? extends Entity>> knownEntities = ReflectHelper.getField(Entity.class, "knownEntities");

        EntityContentFactory factory = EntityContentFactory.factoryMap.get(name);
        if(factory != null) {
            nbt.putString("___id___", name);
            return new CustomEntity(chunk, nbt);
        }
        factory = EntityContentFactory.factoryMap.get(identifier);
        if(factory != null) {
            nbt.putString("___id___", identifier);
            return new CustomEntity(chunk, nbt);
        }

        if (knownEntities.containsKey(name)) {
            return ReflectHelper.invoke(
                    Entity.class,
                    "createEntity",
                    new Object[]{knownEntities.get(name), chunk, nbt, args},
                    Class.class,
                    FullChunk.class,
                    CompoundTag.class,
                    Object[].class
            );
        }
        EntityDefinition entityDefinition = EntityManager.get().getDefinition(name);
        if (entityDefinition != null) {
            return ReflectHelper.invoke(
                    Entity.class,
                    "createEntity",
                    new Object[]{entityDefinition.getImplementation(), chunk, nbt, args},
                    Class.class,
                    FullChunk.class,
                    CompoundTag.class,
                    Object[].class
            );
        }
        return null;
    }

    // @Inject(className = "cn.nukkit.entity.EntityHumanType", type = TypeHook.BEFORE, signature = "(Lcn/nukkit/item/Item;Lcn/nukkit/entity/Entity;FZLcn/nukkit/event/entity/EntityDamageEvent$DamageCause;)Lcn/nukkit/item/Item;")
    // public static void damageArmor(HookController<EntityHumanType> controller, EntityHumanType human, Item armor, Entity damager,
    //         float damage, boolean shield, DamageCause cause) {
    //     if (armor instanceof CustomArmorItem && armor.getDamage() >= armor.getMaxDurability()) {
    //         controller.setReplace(true);
    //         controller.setResult(armor);
    //     }
    // }
}
