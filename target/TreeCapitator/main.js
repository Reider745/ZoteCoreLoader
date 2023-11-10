IDRegistry.genItemID("test");
Item.createItem("test", "Test item", {
    name: "stick"
});

IDRegistry.genBlockID("test");
Block.createBlock("test", [
    {
        name: "Test block 1",
        inCreative: true,
        texture: [
            ["dirt", 0],
            ["dirt", 0],

            ["stone", 0],
            ["stone", 0],
            ["stone", 0],
            ["stone", 0]
        ]
    }, {
        name: "Test block 2",
        inCreative: true,
        texture: [
            ["grass", 0],
            ["grass", 0],
            
            ["stone", 0],
            ["stone", 0],
            ["stone", 0],
            ["stone", 0]
        ]
    }
]);

Callback.addCallback("ItemUse", function(coords, item, block, is, player){
    if(item.id == ItemID.test || item.id == BlockID.test){
        alert(item.id + " " + item.count + " " + item.data);
        alert(block.id+" "+block.data)
    }
});





















































/*var NEW_CORE_API = getMCPEVersion().main > 28;
var TreeCapitator;
(function (TreeCapitator) {
    var treeData = [];
    var dirtTiles = {
        2: true,
        3: true,
        60: true
    };
    TreeCapitator.calculateDestroyTime = __config__.getBool("increase_tree_destroy_time");
    function getTreeData(block) {
        for (var i in treeData) {
            var tree = treeData[i];
            if (this.isTreeBlock(block, tree.log)) {
                return tree;
            }
        }
        return null;
    }
    TreeCapitator.getTreeData = getTreeData;
    function isTreeBlock(block, treeBlocks) {
        var id = block.id, data = block.data % 4;
        for (var i in treeBlocks) {
            var tile = treeBlocks[i];
            if (tile[0] == id && (tile[1] == -1 || tile[1] == data)) {
                return true;
            }
        }
        return false;
    }
    TreeCapitator.isTreeBlock = isTreeBlock;
    function isDirtTile(blockID) {
        return dirtTiles[blockID] || false;
    }
    TreeCapitator.isDirtTile = isDirtTile;
    *//** format
    [id, data] or [[id1, data1], [id2, data2], ...]
    use data -1 for all block variations
    *//*
    function registerTree(log, leaves, leavesRadius) {
        if (leavesRadius === void 0) { leavesRadius = 5; }
        if (typeof log[0] !== "object")
            log = [log];
        if (typeof leaves[0] !== "object")
            leaves = [leaves];
        treeData.push({ log: log, leaves: leaves, radius: leavesRadius });
    }
    TreeCapitator.registerTree = registerTree;
    function registerDirtTile(blockID) {
        dirtTiles[blockID] = true;
    }
    TreeCapitator.registerDirtTile = registerDirtTile;
})(TreeCapitator || (TreeCapitator = {}));
TreeCapitator.registerTree([17, 0], [18, 0], 6);
TreeCapitator.registerTree([17, 1], [18, 1]);
TreeCapitator.registerTree([17, 2], [18, 2]);
TreeCapitator.registerTree([17, 3], [18, 3], 7);
TreeCapitator.registerTree([162, 0], [161, 0]);
TreeCapitator.registerTree([162, 1], [161, 1], 6);
ModAPI.registerAPI("TreeCapitator", TreeCapitator);
var TreeLogger;
(function (TreeLogger) {
    var TreeDestroyData = *//** @class *//* (function () {
        function TreeDestroyData() {
            this.log = {};
            this.leaves = {};
            this.logCount = 0;
            this.hasLeaves = false;
        }
        return TreeDestroyData;
    }());
    var destroyData;
    function checkLog(region, x, y, z, tree) {
        destroyData.log[x + ':' + y + ':' + z] = true;
        destroyData.logCount++;
        for (var xx = x - 1; xx <= x + 1; xx++)
            for (var zz = z - 1; zz <= z + 1; zz++)
                for (var yy = y; yy <= y + 1; yy++) {
                    var block = region.getBlock(xx, yy, zz);
                    if (!destroyData.hasLeaves && TreeCapitator.isTreeBlock(block, tree.leaves)) {
                        destroyData.hasLeaves = true;
                    }
                    if (!destroyData.log[xx + ':' + yy + ':' + zz] && TreeCapitator.isTreeBlock(block, tree.log)) {
                        checkLog(region, xx, yy, zz, tree);
                    }
                }
    }
    function getDrop(region, x, y, z, block, player, item, enchant) {
        if (item === void 0) { item = { id: 0, count: 0, data: 0 }; }
        if (NEW_CORE_API) {
            region.breakBlock(x, y, z, true, player, item);
            return true;
        }
        region.setBlock(x, y, z, 0, 0);
        //@ts-ignore
        var dropFunc = Block.dropFunctions[block.id];
        if (dropFunc) {
            enchant = enchant || ToolAPI.getEnchantExtraData();
            var drop = dropFunc({ x: x, y: y, z: z }, block.id, block.data, ToolAPI.getToolLevel(item.id), enchant, item, region);
            for (var i in drop) {
                region.spawnDroppedItem(x, y, z, drop[i][0], drop[i][1], drop[i][2], drop[i][3] || null);
            }
            return true;
        }
        return false;
    }
    function destroyLog(region, x, y, z, block, tree, player, item, enchant) {
        if (!getDrop(region, x, y, z, block, player, item, enchant)) {
            region.spawnDroppedItem(x, y, z, block.id, 1, block.data % 4);
        }
        checkLeavesFor6Sides(region, x, y, z, tree.leaves);
    }
    function destroyLeaves(region, x, y, z, player) {
        var block = region.getBlock(x, y, z);
        if (!getDrop(region, x, y, z, block, player)) {
            var id = block.id, data = block.data % 4;
            if (id == 18) {
                if (data != 3 && Math.random() < 1 / 20 || data == 3 && Math.random() < 1 / 40) {
                    region.spawnDroppedItem(x, y, z, 6, 1, data);
                }
                if (data == 0 && Math.random() < 1 / 200) {
                    region.spawnDroppedItem(x, y, z, 260, 1, 0);
                }
            }
            if (id == 161 && Math.random() < 1 / 20) {
                region.spawnDroppedItem(x, y, z, 6, 1, data + 4);
            }
            if (Math.random() < 1 / 50) {
                region.spawnDroppedItem(x, y, z, 280, 1, 0);
            }
        }
    }
    function checkLeaves(region, x, y, z, leaves) {
        if (TreeCapitator.isTreeBlock(region.getBlock(x, y, z), leaves)) {
            destroyData.leaves[x + ':' + y + ':' + z] = true;
        }
    }
    function checkLeavesFor6Sides(region, x, y, z, leaves) {
        checkLeaves(region, x - 1, y, z, leaves);
        checkLeaves(region, x + 1, y, z, leaves);
        checkLeaves(region, x, y, z - 1, leaves);
        checkLeaves(region, x, y, z + 1, leaves);
        checkLeaves(region, x, y - 1, z, leaves);
        checkLeaves(region, x, y + 1, z, leaves);
    }
    function isChoppingTree(player, region, coords, block, item) {
        var tree = TreeCapitator.getTreeData(block);
        if (tree && !Entity.getSneaking(player) && ToolAPI.getToolLevelViaBlock(item.id, block.id) > 0) {
            for (var y = coords.y; y > 0; y--) {
                var block_1 = region.getBlock(coords.x, y - 1, coords.z);
                if (TreeCapitator.isDirtTile(block_1.id)) {
                    return true;
                }
                if (!TreeCapitator.isTreeBlock(block_1, tree.log)) {
                    break;
                }
            }
        }
        return false;
    }
    function getTreeSize(region, coords, block) {
        var tree = TreeCapitator.getTreeData(block);
        destroyData = new TreeDestroyData();
        checkLog(region, coords.x, coords.y, coords.z, tree);
        if (destroyData.hasLeaves) {
            return destroyData.logCount;
        }
        return 0;
    }
    function convertCoords(coords) {
        var coordArray = coords.split(':');
        return {
            x: parseInt(coordArray[0]),
            y: parseInt(coordArray[1]),
            z: parseInt(coordArray[2])
        };
    }
    function startDestroy(coords, block, player) {
        var region = BlockSource.getDefaultForActor(player);
        var item = Entity.getCarriedItem(player);
        if (region && TreeCapitator.calculateDestroyTime && isChoppingTree(player, region, coords, block, item)) {
            var treeSize = getTreeSize(region, coords, block);
            if (treeSize > 0) {
                var destroyTime = ToolAPI.getDestroyTimeViaTool(block, item, coords);
                Block.setTempDestroyTime(block.id, destroyTime * treeSize);
            }
        }
    }
    TreeLogger.startDestroy = startDestroy;
    function onDestroy(coords, block, player) {
        var region = BlockSource.getDefaultForActor(player);
        var item = Entity.getCarriedItem(player);
        if (isChoppingTree(player, region, coords, block, item) && getTreeSize(region, coords, block) > 0) {
            if (NEW_CORE_API)
                region.setDestroyParticlesEnabled(false);
            var toolData = ToolAPI.getToolData(item.id);
            var enchant = ToolAPI.getEnchantExtraData(item.extra);
            var skipToolDamage = !toolData.isNative;
            if (toolData.modifyEnchant) {
                toolData.modifyEnchant(enchant, item);
            }
            var tree = TreeCapitator.getTreeData(block);
            for (var coordKey in destroyData.log) {
                var coords_1 = convertCoords(coordKey);
                block = region.getBlock(coords_1.x, coords_1.y, coords_1.z);
                destroyLog(region, coords_1.x, coords_1.y, coords_1.z, block, tree, player, item, enchant);
                if (!skipToolDamage && Game.isItemSpendingAllowed(player)) {
                    if (!(toolData.onDestroy && toolData.onDestroy(item, coords_1, block, player)) && Math.random() < 1 / (enchant.unbreaking + 1)) {
                        item.data++;
                        if (toolData.isWeapon) {
                            item.data++;
                        }
                    }
                    if (item.data >= toolData.toolMaterial.durability) {
                        if (!(toolData.onBroke && toolData.onBroke(item))) {
                            item.id = toolData.brokenId;
                            item.count = 1;
                            item.data = 0;
                            World.playSoundAtEntity(player, "random.break", 1, 1);
                        }
                        break;
                    }
                }
                skipToolDamage = false;
            }
            Entity.setCarriedItem(player, item.id, item.count, item.data, item.extra);
            for (var i = 1; i <= tree.radius; i++) {
                var leavesToDestroy = destroyData.leaves;
                destroyData.leaves = {};
                for (var coordKey in leavesToDestroy) {
                    var coords_2 = convertCoords(coordKey);
                    destroyLeaves(region, coords_2.x, coords_2.y, coords_2.z, player);
                    if (i < tree.radius) {
                        checkLeavesFor6Sides(region, coords_2.x, coords_2.y, coords_2.z, tree.leaves);
                    }
                }
            }
        }
    }
    TreeLogger.onDestroy = onDestroy;
})(TreeLogger || (TreeLogger = {}));
Callback.addCallback("DestroyBlockStart", function (coords, block, player) {
    TreeLogger.startDestroy(coords, block, player);
});
Callback.addCallback("DestroyBlock", function (coords, block, player) {
    TreeLogger.onDestroy(coords, block, player);
});*/


/*
Кэлбеки
DestroyBlockStart
DestroyBlock

Entity.getCarriedItem
Entity.setCarriedIte
World.playSoundAtEntity
Game.isItemSpendingAllowed
region.getBlock
Block.setTempDestroyTime
Entity.getSneaking
region.spawnDroppedItem
*/