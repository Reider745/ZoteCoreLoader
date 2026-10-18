package com.reider745.behavior.entities.runtime;

import cn.nukkit.block.BlockAir;
import cn.nukkit.block.BlockStone;
import cn.nukkit.level.Level;
import cn.nukkit.level.format.generic.BaseFullChunk;
import cn.nukkit.math.Vector3;
import com.reider745.behavior.entities.CustomEntity;
import com.reider745.behavior.entities.EntityContentFactory;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MobPathfinderTest {
    private CustomEntity actor(boolean wall, boolean loaded) {
        CustomEntity entity = mock(CustomEntity.class, CALLS_REAL_METHODS);
        entity.x = .5; entity.y = 64; entity.z = .5;
        entity.level = mock(Level.class);
        doReturn(.6f).when(entity).getWidth();
        doReturn(1.8f).when(entity).getHeight();
        doReturn(1f).when(entity).getScale();
        doReturn(new JSONObject("{\"minecraft:navigation.walk\":{}}")).when(entity).components();
        EntityContentFactory factory = new EntityContentFactory();
        factory.isJump = false;
        doReturn(factory).when(entity).getFactory();
        when(entity.level.getChunkIfLoaded(anyInt(), anyInt())).thenReturn(loaded ? mock(BaseFullChunk.class) : null);
        when(entity.level.getBlock(anyInt(), anyInt(), anyInt())).thenAnswer(invocation -> {
            int x = invocation.getArgument(0), y = invocation.getArgument(1), z = invocation.getArgument(2);
            return y < 64 || wall && x == 2 && z == 0 && y < 67 ? new BlockStone() : new BlockAir();
        });
        return entity;
    }

    @Test void walksAroundSolidObstaclesRatherThanIntoThem() {
        CustomEntity entity = actor(true, true);
        MobPathfinder path = new MobPathfinder(entity);
        Vector3 waypoint = path.next(new Vector3(4.5, 64, .5), 1, false, false);
        assertNotNull(waypoint);
        boolean deviated = false;
        for (int i = 0; i < 10 && waypoint != null; i++) {
            assertFalse(waypoint.getFloorX() == 2 && waypoint.getFloorZ() == 0);
            deviated |= waypoint.getFloorZ() != 0;
            entity.x = waypoint.x; entity.y = waypoint.y; entity.z = waypoint.z;
            waypoint = path.next(new Vector3(4.5, 64, .5), 1, false, false);
        }
        assertTrue(deviated);
    }

    @Test void neverRequestsChunkGenerationForUnloadedTerrain() {
        CustomEntity entity = actor(false, false);
        assertNull(new MobPathfinder(entity).next(new Vector3(4.5,64,.5), 1, false, false));
        verify(entity.level, never()).getChunk(anyInt(), anyInt(), anyBoolean());
    }
}
