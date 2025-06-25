package com.reider745.block.wall;

import cn.nukkit.Player;
import cn.nukkit.block.*;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemTool;
import cn.nukkit.level.Level;
import cn.nukkit.math.AxisAlignedBB;
import cn.nukkit.math.BlockFace;
import cn.nukkit.math.SimpleAxisAlignedBB;
import cn.nukkit.math.Vector3;
import cn.nukkit.utils.Faceable;

import java.util.*;

/*
Author MagicDroidX
Ported to Nukkit-Mot: @KoshakMineDEV
 */
public abstract class BlockWall extends BlockTransparentMeta {
    // Bit masks and shifts for each property
    public static final int NORTH_MASK = 0b11;      // Bits 0-1 (values 0-2)
    public static final int WEST_MASK = 0b11 << 2; // Bits 2-3 (values 0-2)
    public static final int EAST_MASK = 0b11 << 4; // Bits 4-5 (values 0-2)
    public static final int SOUTH_MASK = 0b11 << 6; // Bits 6-7 (values 0-2)
    public static final int POST_MASK = 0b1 << 8; // Bit  8   (values 0-1)

    private static final double MIN_POST_BB = 5.0 / 16;
    private static final double MAX_POST_BB = 11.0 / 16;

    public enum WallType {
        COBBLESTONE(BlockID.COBBLESTONE_WALL, "Cobblestone Wall"),
        MOSSY_COBBLESTONE(1226, "Mossy cobblestone Wall"),
        GRANITE(1227, "Granite wall"),
        DIORITE(1228, "Diorite wall"),
        ANDESITE(1229, "Andesite wall"),
        SANDSTONE(1230, "Sandstone wall"),
        BRICK(1231, "Brick wall"),
        STONE_BRICK(1232, "Stone brick wall"),
        MOSSY_STONE_BRICK(1233, "Mossy stone brick wall"),
        END_BRICK(1235, "End brick wall"),
        NETHER_BRICK(1234, "Nether brick wall"),
        PRISMARINE(1236, "Prismarine wall"),
        RED_SANDSTONE(1237, "Red sandstone wall"),
        RED_NETHER_BRICK(1238, "Red nether brick wall"),
        BLACKSTONE(BlockID.BLACKSTONE_WALL, "Blackstone wall"),
        POLISHED_BLACKSTONE(BlockID.POLISHED_BLACKSTONE_WALL, "Polished blackstone wall"),
        POLISHED_BLACKSTONE_BRICK(BlockID.POLISHED_BLACKSTONE_BRICK_WALL, "Polished blackstone brick wall");

        public final int id;
        public final String name;

        WallType(int id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    public BlockWall() {
        this(0);
    }

    public BlockWall(int meta) {
        super(meta);
    }

    public abstract WallType getWallType();

    @Override
    public int getId() {
        return getWallType().id;
    }

    @Override
    public String getName() {
        return getWallType().name;
    }

    @Override
    public boolean isSolid() {
        return false;
    }

    @Override
    public double getHardness() {
        return 2;
    }

    @Override
    public double getResistance() {
        return 30;
    }

    @Override
    public int getToolTier() {
        return ItemTool.TIER_WOODEN;
    }

    @Override
    public Item toItem() {
        return Item.get(getItemId());
    }

    private boolean shouldBeTall(Block above, BlockFace face) {
        return switch (above.getId()) {
            case AIR, SKULL_BLOCK -> false;

            // If the bell is standing and follow the path, make it tall
            default -> {
                if (above instanceof BlockWall) {
                    yield ((BlockWall) above).getConnectionType(face) != WallConnectionType.NONE;
                } else if (above instanceof BlockPressurePlateBase || above instanceof BlockStairs) {
                    yield true;
                }
                yield above.isSolid() && !above.isTransparent() || shouldBeTallBasedOnBoundingBox(above, face);
            }
        };
    }

    private boolean shouldBeTallBasedOnBoundingBox(Block above, BlockFace face) {
        AxisAlignedBB boundingBox = above.getBoundingBox();
        if (boundingBox == null) {
            return false;
        }
        boundingBox = boundingBox.getOffsetBoundingBox(-above.x, -above.y, -above.z);
        if (boundingBox.getMinY() > 0) {
            return false;
        }
        int offset = face.getXOffset();
        if (offset < 0) {
            return boundingBox.getMinX() < MIN_POST_BB
                    && boundingBox.getMinZ() < MIN_POST_BB && MAX_POST_BB < boundingBox.getMaxZ();
        } else if (offset > 0) {
            return MAX_POST_BB < boundingBox.getMaxX()
                    && MAX_POST_BB < boundingBox.getMaxZ() && boundingBox.getMinZ() < MAX_POST_BB;
        } else {
            offset = face.getZOffset();
            if (offset < 0) {
                return boundingBox.getMinZ() < MIN_POST_BB
                        && boundingBox.getMinX() < MIN_POST_BB && MIN_POST_BB < boundingBox.getMaxX();
            } else if (offset > 0) {
                return MAX_POST_BB < boundingBox.getMaxZ()
                        && MAX_POST_BB < boundingBox.getMaxX() && boundingBox.getMinX() < MAX_POST_BB;
            }
        }
        return false;
    }

    public boolean autoConfigureState() {
        final int previousMeta = getDamage();

        setWallPost(true);

        Block above = up(1, 0);

        for (BlockFace blockFace : BlockFace.Plane.HORIZONTAL) {
            Block side = getSideAtLayer(0, blockFace);
            if (canConnect(side)) {
                connect(blockFace, above, false);
            } else {
                disconnect(blockFace);
            }
        }

        recheckPostConditions(above);
        return getDamage() != previousMeta;
    }

    @Override
    public int onUpdate(int type) {
        if (type == Level.BLOCK_UPDATE_NORMAL) {
            if (autoConfigureState()) {
                level.setBlock(this, this, true);
            }
            return type;
        }
        return 0;
    }

    @Override
    public boolean place(Item item, Block block, Block target, BlockFace face, double fx, double fy, double fz, Player player) {
        autoConfigureState();
        return super.place(item, block, target, face, fx, fy, fz, player);
    }

    public boolean isWallPost() {
        return (getPropertyValue(POST_MASK) != 0);
    }

    public void setWallPost(boolean wallPost) {
        setPropertyValue(POST_MASK, wallPost ? 1 : 0);
    }

    public void clearConnections() {
        setPropertyValue(EAST_MASK, WallConnectionType.NONE);
        setPropertyValue(WEST_MASK, WallConnectionType.NONE);
        setPropertyValue(NORTH_MASK, WallConnectionType.NONE);
        setPropertyValue(SOUTH_MASK, WallConnectionType.NONE);
    }

    public Map<BlockFace, WallConnectionType> getWallConnections() {
        EnumMap<BlockFace, WallConnectionType> connections = new EnumMap<>(BlockFace.class);
        for (BlockFace blockFace : BlockFace.Plane.HORIZONTAL) {
            WallConnectionType connectionType = getConnectionType(blockFace);
            if (connectionType != WallConnectionType.NONE) {
                connections.put(blockFace, connectionType);
            }
        }
        return connections;
    }

    public WallConnectionType getConnectionType(BlockFace blockFace) {
        return switch (blockFace) {
            case NORTH -> WallConnectionType.values()[getPropertyValue(NORTH_MASK)];
            case SOUTH -> WallConnectionType.values()[getPropertyValue(SOUTH_MASK)];
            case WEST -> WallConnectionType.values()[getPropertyValue(WEST_MASK)];
            case EAST -> WallConnectionType.values()[getPropertyValue(EAST_MASK)];
            default -> WallConnectionType.NONE;
        };
    }

    public boolean setConnection(BlockFace blockFace, WallConnectionType type) {
        return switch (blockFace) {
            case NORTH -> {
                setPropertyValue(NORTH_MASK, type);
                yield true;
            }
            case SOUTH -> {
                setPropertyValue(SOUTH_MASK, type);
                yield true;
            }
            case WEST -> {
                setPropertyValue(WEST_MASK, type);
                yield true;
            }
            case EAST -> {
                setPropertyValue(EAST_MASK, type);
                yield true;
            }
            default -> false;
        };
    }

    public void autoUpdatePostFlag() {
        setWallPost(recheckPostConditions(up(1, 0)));
    }

    public boolean hasConnections() {
        return ((WallConnectionType.values()[getPropertyValue(EAST_MASK)]) != WallConnectionType.NONE)
                || ((WallConnectionType.values()[getPropertyValue(WEST_MASK)]) != WallConnectionType.NONE)
                || ((WallConnectionType.values()[getPropertyValue(NORTH_MASK)]) != WallConnectionType.NONE)
                || ((WallConnectionType.values()[getPropertyValue(SOUTH_MASK)]) != WallConnectionType.NONE);
    }

    private boolean recheckPostConditions(Block above) {
        // If nothing is connected, it should be a post
        if (!hasConnections()) {
            return true;
        }

        // If it's not straight, it should be a post
        Map<BlockFace, WallConnectionType> connections = getWallConnections();
        if (connections.size() != 2) {
            return true;
        }

        Iterator<Map.Entry<BlockFace, WallConnectionType>> iterator = connections.entrySet().iterator();
        Map.Entry<BlockFace, WallConnectionType> entryA = iterator.next();
        Map.Entry<BlockFace, WallConnectionType> entryB = iterator.next();
        if (entryA.getValue() != entryB.getValue() || entryA.getKey().getOpposite() != entryB.getKey()) {
            return true;
        }

        BlockFace.Axis axis = entryA.getKey().getAxis();

        switch (above.getId()) {
            // These special blocks forces walls to become a post
            case FLOWER_POT_BLOCK, SKULL_BLOCK, CONDUIT, STANDING_BANNER, TURTLE_EGG -> {
                return true;
            }

            // End rods make it become a post if it's placed on the wall
            case END_ROD -> {
                if (((Faceable) above).getBlockFace() == BlockFace.UP) {
                    return true;
                }
            }

            // If the bell is standing and don't follow the path, make it a post
            default -> {
                if (above instanceof BlockWall) {
                    // If the wall above is a post, it should also be a post

                    if (((BlockWall) above).isWallPost()) {
                        return true;
                    }

                } else if (above.getId() == LEVER || above instanceof BlockTorch || above instanceof BlockButton) {
                    // These blocks make this become a post if they are placed down (facing up)

                    if (((Faceable) above).getBlockFace() == BlockFace.UP) {
                        return true;
                    }

                } else if (above instanceof BlockFenceGate) {
                    // If the gate don't follow the path, make it a post

                    if (((Faceable) above).getBlockFace().getAxis() == axis) {
                        return true;
                    }

                }
            }
        }

        // Sign posts always makes the wall become a post
        return above instanceof BlockSignPost;
    }

    public boolean isSameHeightStraight() {
        Map<BlockFace, WallConnectionType> connections = getWallConnections();
        if (connections.size() != 2) {
            return false;
        }

        Iterator<Map.Entry<BlockFace, WallConnectionType>> iterator = connections.entrySet().iterator();
        Map.Entry<BlockFace, WallConnectionType> a = iterator.next();
        Map.Entry<BlockFace, WallConnectionType> b = iterator.next();
        return a.getValue() == b.getValue() && a.getKey().getOpposite() == b.getKey();
    }

    public boolean connect(BlockFace blockFace) {
        return connect(blockFace, true);
    }

    public boolean connect(BlockFace blockFace, boolean recheckPost) {
        if (blockFace.getHorizontalIndex() < 0) {
            return false;
        }

        Block above = getSideAtLayer(0, BlockFace.UP);
        return connect(blockFace, above, recheckPost);
    }

    private boolean connect(BlockFace blockFace, Block above, boolean recheckPost) {
        WallConnectionType type = shouldBeTall(above, blockFace) ? WallConnectionType.TALL : WallConnectionType.SHORT;
        if (setConnection(blockFace, type)) {
            if (recheckPost) {
                recheckPostConditions(above);
            }
            return true;
        }
        return false;
    }

    public boolean disconnect(BlockFace blockFace) {
        if (blockFace.getHorizontalIndex() < 0) {
            return false;
        }

        if (setConnection(blockFace, WallConnectionType.NONE)) {
            autoUpdatePostFlag();
            return true;
        }
        return false;
    }

    @Override
    protected AxisAlignedBB recalculateBoundingBox() {

        boolean north = this.canConnect(this.getSide(BlockFace.NORTH));
        boolean south = this.canConnect(this.getSide(BlockFace.SOUTH));
        boolean west = this.canConnect(this.getSide(BlockFace.WEST));
        boolean east = this.canConnect(this.getSide(BlockFace.EAST));

        double n = north ? 0 : 0.25;
        double s = south ? 1 : 0.75;
        double w = west ? 0 : 0.25;
        double e = east ? 1 : 0.75;

        if (north && south && !west && !east) {
            w = 0.3125;
            e = 0.6875;
        } else if (!north && !south && west && east) {
            n = 0.3125;
            s = 0.6875;
        }

        return new SimpleAxisAlignedBB(
                this.x + w,
                this.y,
                this.z + n,
                this.x + e,
                this.y + 1.5,
                this.z + s
        );
    }

    public boolean canConnect(Block block) {
        switch (block.getId()) {
            case GLASS_PANE, IRON_BARS, GLASS -> {
                return true;
            }
            default -> {
                if (block instanceof BlockGlassStained || block instanceof BlockGlassPaneStained || block instanceof BlockWall) {
                    return true;
                }
                if (block instanceof BlockFenceGate fenceGate) {
                    return fenceGate.getBlockFace().getAxis() != calculateAxis(this, block);
                }
                if (block instanceof BlockStairs) {
                    return ((BlockStairs) block).getBlockFace().getOpposite() == calculateFace(this, block);
                }
                if (block instanceof BlockTrapdoor trapdoor) {
                    return trapdoor.isOpen() && trapdoor.getBlockFace() == calculateFace(this, trapdoor);
                }
                return block.isSolid() && !block.isTransparent();
            }
        }
    }

    public boolean isConnected(BlockFace face) {
        return getConnectionType(face) != WallConnectionType.NONE;
    }

    @Override
    public WaterloggingType getWaterloggingType() {
        return WaterloggingType.WHEN_PLACED_IN_WATER;
    }

    @Override
    public int getToolType() {
        return ItemTool.TYPE_PICKAXE;
    }

    @Override
    public boolean canHarvestWithHand() {
        return false;
    }

    public void setPropertyValue(int mask, int value) {
        int data = getDamage();
        setDamage((data & ~mask) | ((value << Integer.numberOfTrailingZeros(mask)) & mask));
    }

    public void setPropertyValue(int mask, WallConnectionType type) {
        int data = getDamage();
        setDamage((data & ~mask) | ((type.ordinal() << Integer.numberOfTrailingZeros(mask)) & mask));
    }

    // Reusable method to get a property from the state
    public int getPropertyValue(int mask) {
        int data = getDamage();
        return (data & mask) >>> Integer.numberOfTrailingZeros(mask);
    }

    public enum WallConnectionType {
        NONE,
        SHORT,
        TALL
    }

    public static BlockFace.Axis calculateAxis(Vector3 base, Vector3 side) {
        Vector3 vector = side.subtract(base);
        return vector.x != 0 ? BlockFace.Axis.X : vector.z != 0 ? BlockFace.Axis.Z : BlockFace.Axis.Y;
    }

    public static BlockFace calculateFace(Vector3 base, Vector3 side) {
        Vector3 vector = side.subtract(base);
        BlockFace.Axis axis = vector.x != 0 ? BlockFace.Axis.X : vector.z != 0 ? BlockFace.Axis.Z : BlockFace.Axis.Y;
        double direction = vector.getAxis(axis);
        return BlockFace.fromAxis(direction < 0 ? BlockFace.AxisDirection.NEGATIVE : BlockFace.AxisDirection.POSITIVE, axis);
    }
}
