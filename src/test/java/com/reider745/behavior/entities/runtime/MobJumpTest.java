package com.reider745.behavior.entities.runtime;

import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityLiving;
import cn.nukkit.block.BlockAir;
import cn.nukkit.block.BlockStone;
import cn.nukkit.level.Level;
import cn.nukkit.math.AxisAlignedBB;
import cn.nukkit.math.SimpleAxisAlignedBB;
import cn.nukkit.math.Vector3;
import com.reider745.behavior.entities.CustomEntity;
import com.reider745.behavior.entities.EntityContentFactory;
import org.json.JSONObject;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MobJumpTest {
    // Keep Nukkit's actual move/collision/ground handling; omit chunk migration and fall damage.
    static class Actor extends CustomEntity {
        Actor() { super(null, null); }
        void prepare(float width, float height) {
            x = -2.5; y = 64; z = .5;
            onGround = true;
            moveMultiplier = 1;
            boundingBox = new SimpleAxisAlignedBB(x - width / 2, y, z - width / 2,
                    x + width / 2, y + height, z + width / 2);
        }
        @Override protected void checkChunks() {}
        @Override protected void updateFallState(boolean onGround) {}
    }

    private Actor actor(float width, float height) {
        Actor entity = mock(Actor.class, withSettings().stubOnly().defaultAnswer(CALLS_REAL_METHODS));
        entity.prepare(width, height);
        entity.level = mock(Level.class, withSettings().stubOnly());
        EntityContentFactory factory = new EntityContentFactory();
        factory.isJump = true;
        factory.isMovement = true;
        factory.isPhysics = true;
        factory.movement = .2f;
        JSONObject components = new JSONObject("""
                {"minecraft:jump.static":{},"minecraft:physics":{},
                 "minecraft:navigation.walk":{},"minecraft:movement.basic":{},
                 "minecraft:behavior.move_towards_target":{}}
                """);
        doReturn(factory).when(entity).getFactory();
        doReturn(components).when(entity).components();
        doReturn(width).when(entity).getWidth();
        doReturn(height).when(entity).getHeight();
        doReturn(1f).when(entity).getScale();
        doReturn(false).when(entity).isInsideOfWater();
        doReturn(false).when(entity).isImmobile();
        doReturn(null).when(entity).raycast(any(Entity.class));
        doNothing().when(entity).lookAt(any(Vector3.class));
        doNothing().when(entity).updateMovement();

        EntityLiving target = mock(EntityLiving.class, withSettings().stubOnly().defaultAnswer(CALLS_REAL_METHODS));
        target.x = 4.5; target.y = 65; target.z = .5;
        doReturn(true).when(target).isAlive();
        doReturn(new cn.nukkit.level.Position(4.5, 65, .5, entity.level)).when(target).getPosition();
        doReturn(target).when(entity).getTarget();

        when(entity.level.getChunkIfLoaded(anyInt(), anyInt()))
                .thenReturn(mock(cn.nukkit.level.format.generic.BaseFullChunk.class));
        when(entity.level.getBlock(anyInt(), anyInt(), anyInt())).thenAnswer(call -> {
            int x = call.getArgument(0), y = call.getArgument(1);
            return y < 64 || x >= 1 && y == 64 ? new BlockStone() : new BlockAir();
        });
        when(entity.level.getCollidingEntities(any(AxisAlignedBB.class))).thenReturn(new Entity[0]);
        when(entity.level.getCollisionCubes(eq(entity), any(AxisAlignedBB.class), eq(false)))
                .thenReturn(new AxisAlignedBB[]{
                        new SimpleAxisAlignedBB(-20, 63, -20, 20, 64, 20),
                        new SimpleAxisAlignedBB(1, 64, -20, 20, 65, 20)
                });
        return entity;
    }

    @ParameterizedTest
    // Collision dimensions of swift_bp's wobble, dartback and twisted_warden.
    @CsvSource({"1,0.65,0.6", "3,0.65,0.6", "5,0.65,0.6",
            "1,1.6,1.3", "3,1.6,1.3", "5,1.6,1.3",
            "1,2.8,4.4", "3,2.8,4.4", "5,2.8,4.4"})
    void jumpsOntoOneBlockLedgeAndLands(int ticks, float width, float height) {
        Actor entity = actor(width, height);
        MobAI ai = new MobAI(entity, new NukkitMobHost(entity));
        double highest = entity.y;
        for (int elapsed = 0; elapsed < 40; elapsed += ticks) {
            ai.tick(ticks);
            highest = Math.max(highest, entity.y);
        }
        assertTrue(highest >= 65, "Jump must clear the ledge, highest Y=" + highest);
        assertTrue(highest < 65.5, "Jump must not gain height from repeated ground impulses");
        assertTrue(entity.x > 1.5, "Mob must advance onto the ledge, X=" + entity.x);
        assertEquals(65, entity.y, .001, "Mob must land on the upper surface");
        assertTrue(entity.onGround);
    }
}
