package cn.nukkit.entity.ai.controller;

import cn.nukkit.api.PowerNukkitXOnly;
import cn.nukkit.api.Since;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityIntelligent;
import cn.nukkit.math.Vector3;

/**
 * 处理飞行/游泳实体运动
 */
@PowerNukkitXOnly
@Since("1.6.0.0-PNX")
public class SpaceMoveController implements IController {
    @Override
    public boolean control(EntityIntelligent entity) {
        if (entity.hasMoveDirection() && !entity.isShouldUpdateMoveDirection()) {
            Vector3 direction = entity.getMoveDirectionEnd();
            var speed = entity.getMovementSpeed();
            if (entity.motionX * entity.motionX + entity.motionY * entity.motionY + entity.motionZ * entity.motionZ > speed * speed * 0.4756) {
                return false;
            }
            var relativeVector = direction.clone().setComponents(direction.x - entity.x,
                    direction.y - entity.y, direction.z - entity.z);
            var xyzLength = Math.sqrt(relativeVector.x * relativeVector.x + relativeVector.y * relativeVector.y + relativeVector.z * relativeVector.z);
            var k = speed / xyzLength * 0.33;
            var dx = relativeVector.x * k;
            var dy = relativeVector.y * k;
            var dz = relativeVector.z * k;
            entity.addTmpMoveMotion(new Vector3(dx, dy, dz));
            entity.setDataFlag(Entity.DATA_FLAGS, Entity.DATA_FLAG_MOVING, true);
            if (xyzLength < speed) {
                needNewDirection(entity);
                return false;
            }
            return true;
        } else {
            entity.setDataFlag(Entity.DATA_FLAGS, Entity.DATA_FLAG_MOVING, false);
            return false;
        }
    }

    protected void needNewDirection(EntityIntelligent entity) {
        //通知需要新的移动目标
        entity.setShouldUpdateMoveDirection(true);
    }
}
