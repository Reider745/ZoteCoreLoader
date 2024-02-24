package com.reider745.hooks;

import com.reider745.api.hooks.HookClass;
import com.reider745.api.hooks.annotation.Hooks;
import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

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

    // @Inject(className = "cn.nukkit.entity.EntityHumanType", type = TypeHook.BEFORE, signature = "(Lcn/nukkit/item/Item;Lcn/nukkit/entity/Entity;FZLcn/nukkit/event/entity/EntityDamageEvent$DamageCause;)Lcn/nukkit/item/Item;")
    // public static void damageArmor(HookController<EntityHumanType> controller, EntityHumanType human, Item armor, Entity damager,
    //         float damage, boolean shield, DamageCause cause) {
    //     if (armor instanceof CustomArmorItem && armor.getDamage() >= armor.getMaxDurability()) {
    //         controller.setReplace(true);
    //         controller.setResult(armor);
    //     }
    // }
}
