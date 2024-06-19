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
import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

import java.lang.reflect.Field;
import java.util.Map;

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

    static {
        try {
            fieldId = Entity.class.getDeclaredField("id");
            fieldId.setAccessible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Inject(className = "cn.nukkit.entity.Entity")
    public static void scheduleUpdate(Entity self){
        try {
            if(self.isPlayer)
                fieldId.set(self, (long) self.getUniqueId().toString().hashCode());
        } catch (IllegalAccessException ignore) {}
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
