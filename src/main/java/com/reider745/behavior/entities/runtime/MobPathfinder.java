package com.reider745.behavior.entities.runtime;

import cn.nukkit.block.Block;
import cn.nukkit.block.BlockDoor;
import cn.nukkit.math.Vector3;
import com.reider745.behavior.entities.CustomEntity;
import org.json.JSONObject;

import java.util.*;

/** Bounded A* over loaded blocks. Routes and search state belong to one actor. */
final class MobPathfinder {
    private record Cell(int x, int y, int z) {}
    private record Node(Cell cell, double score) {}
    private final CustomEntity entity;
    private List<Vector3> route = List.of();
    private int index;
    private int refresh;
    private Vector3 destination;

    MobPathfinder(CustomEntity entity) { this.entity = entity; }

    Vector3 next(Vector3 target, int ticks, boolean flying, boolean swimming) {
        if (target == null) { route = List.of(); return null; }
        refresh -= ticks;
        if (destination == null || destination.distanceSquared(target) > 4 || refresh <= 0 || index >= route.size()) {
            destination = target.clone();
            refresh = 20;
            route = search(target, flying, swimming);
            index = 0;
        }
        while (index < route.size() && entity.distanceSquared(route.get(index)) < .5) index++;
        return index < route.size() ? route.get(index) : null;
    }

    private List<Vector3> search(Vector3 target, boolean flying, boolean swimming) {
        Cell start = new Cell(entity.getFloorX(), entity.getFloorY(), entity.getFloorZ());
        Cell goal = new Cell(target.getFloorX(), target.getFloorY(), target.getFloorZ());
        PriorityQueue<Node> open = new PriorityQueue<>(Comparator.comparingDouble(Node::score));
        Map<Cell, Double> distance = new HashMap<>();
        Map<Cell, Cell> parent = new HashMap<>();
        Set<Cell> visited = new HashSet<>();
        distance.put(start, 0d);
        open.add(new Node(start, heuristic(start, goal)));
        Cell best = start;
        int budget = 512;
        while (!open.isEmpty() && budget-- > 0) {
            Cell cell = open.remove().cell;
            if (!visited.add(cell)) continue;
            if (heuristic(cell, goal) < heuristic(best, goal)) best = cell;
            if (heuristic(cell, goal) < 1.1) { best = cell; break; }
            for (int[] direction : new int[][]{{1,0,0},{-1,0,0},{0,0,1},{0,0,-1},{0,1,0},{0,-1,0}}) {
                if (!flying && !swimming && direction[1] != 0) continue;
                Cell next = new Cell(cell.x + direction[0], cell.y + direction[1], cell.z + direction[2]);
                if (heuristic(start, next) > 32) continue;
                if (!flying && !swimming) {
                    if (!passable(next) && passable(new Cell(next.x, next.y + 1, next.z)) && passable(new Cell(cell.x, cell.y + 1, cell.z))) {
                        if (!entity.getFactory().isJump && !entity.components().has("minecraft:jump.dynamic")) continue;
                        next = new Cell(next.x, next.y + 1, next.z);
                    }
                    int drops = 0;
                    while (passable(next) && !supported(next) && drops++ < 3) next = new Cell(next.x, next.y - 1, next.z);
                    if (!supported(next)) continue;
                }
                if (!passable(next) || swimming && !water(next)) continue;
                double score = distance.get(cell) + 1 + Math.abs(next.y - cell.y) * .5;
                if (score >= distance.getOrDefault(next, Double.MAX_VALUE)) continue;
                distance.put(next, score);
                parent.put(next, cell);
                open.add(new Node(next, score + heuristic(next, goal)));
            }
        }
        List<Vector3> result = new ArrayList<>();
        for (Cell cell = best; !cell.equals(start); cell = parent.get(cell)) {
            if (cell == null) return List.of();
            result.add(new Vector3(cell.x + .5, cell.y, cell.z + .5));
        }
        Collections.reverse(result);
        return result;
    }

    private boolean water(Cell cell) {
        int id = entity.level.getBlockIdAt(cell.x, cell.y, cell.z);
        return id == 8 || id == 9;
    }

    private boolean solid(int x, int y, int z) {
        return entity.level.getChunkIfLoaded(x >> 4, z >> 4) != null && entity.level.getBlock(x, y, z).isSolid();
    }

    private boolean supported(Cell cell) {
        // Wide mobs can land with part of their collision box on a ledge before
        // their centre crosses its edge. Use the same footprint as passable().
        double radius = Math.max(.1, entity.getWidth() * entity.getScale() / 2 - .01);
        for (int x = (int) Math.floor(cell.x + .5 - radius); x <= (int) Math.floor(cell.x + .5 + radius); x++) {
            for (int z = (int) Math.floor(cell.z + .5 - radius); z <= (int) Math.floor(cell.z + .5 + radius); z++) {
                if (solid(x, cell.y - 1, z)) return true;
            }
        }
        return false;
    }

    private boolean passable(Cell cell) {
        if (entity.level.getChunkIfLoaded(cell.x >> 4, cell.z >> 4) == null) return false;
        JSONObject navigation = entity.components().optJSONObject("minecraft:navigation.walk");
        double radius = Math.max(.1, entity.getWidth() * entity.getScale() / 2 - .01);
        for (int x = (int) Math.floor(cell.x + .5 - radius); x <= (int) Math.floor(cell.x + .5 + radius); x++) {
            for (int z = (int) Math.floor(cell.z + .5 - radius); z <= (int) Math.floor(cell.z + .5 + radius); z++) {
                for (int y = cell.y; y < Math.ceil(cell.y + entity.getHeight() * entity.getScale()); y++) {
                    Block block = entity.level.getBlock(x, y, z);
                    if (block instanceof BlockDoor door) {
                        if (!door.isOpen() && (door.getId() == 71 || navigation == null || !navigation.optBoolean("can_open_doors", false))) return false;
                        continue;
                    }
                    if (block.isSolid()) return false;
                    if (navigation != null && navigation.optBoolean("avoid_damage_blocks", true)
                            && Set.of(10, 11, 51, 81, 213).contains(block.getId())) return false;
                    if (navigation != null && !navigation.optBoolean("can_path_over_water", true)
                            && (block.getId() == 8 || block.getId() == 9)) return false;
                }
            }
        }
        return true;
    }

    private static double heuristic(Cell a, Cell b) {
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y) + Math.abs(a.z - b.z);
    }
}
