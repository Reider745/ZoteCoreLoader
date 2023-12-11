// THIS DATA IS USED FOR VERSION 1.16

var ENCHANT_TAG_GROUP = TagRegistry.getOrCreateGroup("enchant");

/* --------------- tool materials ---------------- */
ToolAPI.addToolMaterial("wood", {
    level: 1,
    durability: 60,
    damage: 2,
    efficiency: 2
});

ToolAPI.addToolMaterial("stone", {
    level: 2,
    durability: 132,
    damage: 3,
    efficiency: 4
});

ToolAPI.addToolMaterial("iron", {
    level: 3,
    durability: 251,
    damage: 4,
    efficiency: 6
});

ToolAPI.addToolMaterial("golden", {
    level: 1,
    durability: 33,
    damage: 2,
    efficiency: 12
});

ToolAPI.addToolMaterial("diamond", {
    level: 4,
    durability: 1562,
    damage: 5,
    efficiency: 8
});

ToolAPI.addToolMaterial("netherite", {
    level: 4,
    durability: 2032,
    damage: 6,
    efficiency: 9
});


/* --------------- block materials ---------------- */
ToolAPI.addBlockMaterial("stone", 10/3); // +
ToolAPI.addBlockMaterial("wood", 1); // +
ToolAPI.addBlockMaterial("dirt", 1); // +
ToolAPI.addBlockMaterial("plant", 1); // +
ToolAPI.addBlockMaterial("fibre", 1); // +
ToolAPI.addBlockMaterial("cobweb", 10/3); // +
ToolAPI.addBlockMaterial("unbreaking", 999999999); // +





/* -------------- tool registration --------------- */
// pickaxes
ToolAPI.registerTool(VanillaItemID.wooden_pickaxe, "wood", ["stone"], {isNative: true, damage: 1});
ToolAPI.registerTool(VanillaItemID.stone_pickaxe, "stone", ["stone"], {isNative: true, damage: 1});
ToolAPI.registerTool(VanillaItemID.iron_pickaxe, "iron", ["stone"], {isNative: true, damage: 1});
ToolAPI.registerTool(VanillaItemID.golden_pickaxe, "golden", ["stone"], {isNative: true, damage: 1});
ToolAPI.registerTool(VanillaItemID.diamond_pickaxe, "diamond", ["stone"], {isNative: true, damage: 1});
ToolAPI.registerTool(VanillaItemID.netherite_pickaxe, "netherite", ["stone"], {isNative: true, damage: 1});

// axes
ToolAPI.registerTool(VanillaItemID.wooden_axe, "wood", ["wood"], {isNative: true, damage: 2});
ToolAPI.registerTool(VanillaItemID.stone_axe, "stone", ["wood"], {isNative: true, damage: 2});
ToolAPI.registerTool(VanillaItemID.iron_axe, "iron", ["wood"], {isNative: true, damage: 2});
ToolAPI.registerTool(VanillaItemID.golden_axe, "golden", ["wood"], {isNative: true, damage: 2});
ToolAPI.registerTool(VanillaItemID.diamond_axe, "diamond", ["wood"], {isNative: true, damage: 2});
ToolAPI.registerTool(VanillaItemID.netherite_axe, "netherite", ["wood"], {isNative: true, damage: 2});

// shovels
ToolAPI.registerTool(VanillaItemID.wooden_shovel, "wood", ["dirt"], {isNative: true, damage: 0});
ToolAPI.registerTool(VanillaItemID.stone_shovel, "stone", ["dirt"], {isNative: true, damage: 0});
ToolAPI.registerTool(VanillaItemID.iron_shovel, "iron", ["dirt"], {isNative: true, damage: 0});
ToolAPI.registerTool(VanillaItemID.golden_shovel, "golden", ["dirt"], {isNative: true, damage: 0});
ToolAPI.registerTool(VanillaItemID.diamond_shovel, "diamond", ["dirt"], {isNative: true, damage: 0});
ToolAPI.registerTool(VanillaItemID.netherite_shovel, "netherite", ["dirt"], {isNative: true, damage: 0});

// hoes
ToolAPI.registerTool(VanillaItemID.wooden_hoe, "wood", ["plant"], {isNative: true, damage: 1});
ToolAPI.registerTool(VanillaItemID.stone_hoe, "stone", ["plant"], {isNative: true, damage: 1});
ToolAPI.registerTool(VanillaItemID.iron_hoe, "iron", ["plant"], {isNative: true, damage: 1});
ToolAPI.registerTool(VanillaItemID.golden_hoe, "golden", ["plant"], {isNative: true, damage: 1});
ToolAPI.registerTool(VanillaItemID.diamond_hoe, "diamond", ["plant"], {isNative: true, damage: 1});
ToolAPI.registerTool(VanillaItemID.netherite_hoe, "netherite", ["plant"], {isNative: true, damage: 1});

// sheers
ToolAPI.registerTool(VanillaItemID.shears, "iron", ["fibre"], {isNative: true, damage: 0});

// swords 
ToolAPI.registerSword(VanillaItemID.wooden_sword, "wood", {isNative: true});
ToolAPI.registerSword(VanillaItemID.stone_sword, "stone", {isNative: true});
ToolAPI.registerSword(VanillaItemID.iron_sword, "iron", {isNative: true});
ToolAPI.registerSword(VanillaItemID.golden_sword, "golden", {isNative: true});
ToolAPI.registerSword(VanillaItemID.diamond_sword, "diamond", {isNative: true});
ToolAPI.registerSword(VanillaItemID.netherite_sword, "netherite", {isNative: true});



/* -------------- block registration --------------- */
ToolAPI.registerBlockMaterialAsArray("stone", [
    1, // stone
    4, // cobblestone
    14, // gold ore
    15, // iron ore
    16, // coal ore
    21, // lapis ore
    22, // lapis block
    23, // dispenser
    24, // sandstone
    27, // power rail
    28, // trigger rail
    29, // sticky piston,
    33, // piston
    41, // gold block
    42, // iron block
    43, // double stone slab
    44, // stone slab
    45, // bricks
    48, // mossy cobblestone
    49, // obsidian
    52, // spawner
    56, // diamond ore
    57, // diamond block
    61, 62, // furnace
    66, // rail
    67, // cobble stairs
    70, // pressure plate (stone)
    71, // iron door
    73, 74, // redstone ore
    77, // stone button
    79, // ice
    87, // netherrack
    89, // glowstone
    97, // monster egg
    98, // stone brick
    101, // iron bars
    108, 109, // brick stairs
    112, 113, 114, // nether brick blocks
    116, // enchanting table
    117, // brewing stand
    118, // cauldron
    120, 121, // end blocks
    123, 124, // lamp
    125, // dropper
    126, // activator rail
    128, // sandstone stairs
    129, // emerald ore
    130, // ender chest
    133, // emerald block
    138, // beacon
    139, // cobblestone wall
    145, // anvil
    147, 148, // pressure plates
    152, // redstone block
    153, // nether quartz
    154, // hopper
    155, 156, // quartz
    159, // stained clay
    167, // iron trapdoor
    168, // prismarine
    172, // hardened clay
    173, // coal block
    174, // packed ice
    179, 180, // red sandstone
    181, // double stone slab 2
    182, // stone slab 2
    201, // purpur
    203, // purpur stairs
    205, // shulker box (undyed)
    206, // end brick
    213, // magma
    215, // red nether brick
    216, // bone block
    218, // shulker box
    219, 220, 221, 222, 223, 224, 225, 226, 227, 228, 229, 231, 232, 233, 234, 235, // glazed terracotta
    236, // concrete
    245, // stonecutter
    251, // observer,
    257, 258, 259, // prismarine stairs
    266, // blue ice
    387, // coral block
    412, // conduit
    417, // stone slab 3
    421, // stone slab 4
    422, // double stone slab 3
    423, // double stone slab 4
    424, 425, 426, 427, 428, 429, 430, 431, 432, 433, 434, 435, // stone stairs
    438, // smooth stone
    439, // red nether brick stairs
    440, // smooth quartz stairs
    450, // grindstone
    451, 469, // blast furnace
    452, // stonecutter
    453, 454, // smoker
    461, // bell
    463, // lantern
    465, // lava cauldron
    VanillaTileID.ancient_debris,
    VanillaTileID.basalt,
    VanillaTileID.blackstone,
    VanillaTileID.blackstone_double_slab,
    VanillaTileID.blackstone_slab,
    VanillaTileID.blackstone_stairs,
    VanillaTileID.blackstone_wall,
    VanillaTileID.chain,
    VanillaTileID.chiseled_nether_bricks,
    VanillaTileID.chiseled_polished_blackstone,
    VanillaTileID.cracked_nether_bricks,
    VanillaTileID.cracked_polished_blackstone_bricks,
    VanillaTileID.crimson_nylium,
    VanillaTileID.crying_obsidian,
    VanillaTileID.gilded_blackstone,
    VanillaTileID.lodestone,
    VanillaTileID.nether_gold_ore,
    VanillaTileID.netherite_block,
    VanillaTileID.polished_basalt,
    VanillaTileID.polished_blackstone,
    VanillaTileID.polished_blackstone_brick_double_slab,
    VanillaTileID.polished_blackstone_brick_slab,
    VanillaTileID.polished_blackstone_brick_stairs,
    VanillaTileID.polished_blackstone_brick_wall,
    VanillaTileID.polished_blackstone_bricks,
    VanillaTileID.polished_blackstone_button,
    VanillaTileID.polished_blackstone_double_slab,
    VanillaTileID.polished_blackstone_pressure_plate,
    VanillaTileID.polished_blackstone_slab,
    VanillaTileID.polished_blackstone_stairs,
    VanillaTileID.polished_blackstone_wall,
    VanillaTileID.quartz_bricks,
    VanillaTileID.respawn_anchor,
    VanillaTileID.soul_lantern,
    VanillaTileID.warped_nylium,
], true);

ToolAPI.registerBlockMaterialAsArray("wood", [
    5, // planks
    17, // log
    25, // note block
    47, // bookshelf
    53, // oak stairs
    54, // chest
    58, // workbench
    63, 68, // oak sign
    64, // door
    65, // ladder
    72, // wooden pressure plate
    85, // fence
    86, 91, // pumpkin
    96, // wooden trapdoor
    99, 100, // mushroom
    103, // melon
    106, // vine
    107, // fence gate
    127, // cocoa
    134, 135, 136, 163, 164, // wooden stairs
    143, // wooden button
    146, // trapped chest
    151, 178, // daylight sensor
    157, // wooden double slab
    158, // wooden slab
    162, // log2
    183, 184, 185, 186, 187, // fence gates
    193, 194, 195, 196, 197, // wooden doors
    260, 261, 262, 263, 264, 265, // stripped log
    395, 396, 397, 398, 399, // wooden buttons
    400, 401, 402, 403, 404, // wooden trapdoors
    405, 406, 407, 408, 409, // wooden pressure plates
    410, // carved pumpkin
    436, 437, // spruce sign
    441, 442, // birch sign
    443, 444, // acacia sign
    445, 446, // dark oak sign
    449, // lectern
    455, // cartography table
    456, // fletching table
    457, // smithing table
    458, // barrel
    459, // loom
    464, // campfire
    467, // wood
    468, // composter
    VanillaTileID.bee_nest,
    VanillaTileID.beehive,
    VanillaTileID.crimson_door,
    VanillaTileID.crimson_double_slab,
    VanillaTileID.crimson_fence,
    VanillaTileID.crimson_fence_gate,
    VanillaTileID.crimson_hyphae,
    VanillaTileID.crimson_planks,
    VanillaTileID.crimson_pressure_plate,
    VanillaTileID.crimson_slab,
    VanillaTileID.crimson_stairs,
    VanillaTileID.crimson_standing_sign,
    VanillaTileID.crimson_trapdoor,
    VanillaTileID.crimson_wall_sign,
    VanillaTileID.soul_campfire,
    VanillaTileID.stripped_crimson_hyphae,
    VanillaTileID.stripped_crimson_stem,
    VanillaTileID.stripped_warped_hyphae,
    VanillaTileID.stripped_warped_stem,
    VanillaTileID.warped_button,
    VanillaTileID.warped_door,
    VanillaTileID.warped_double_slab,
    VanillaTileID.warped_fence,
    VanillaTileID.warped_fence_gate,
    VanillaTileID.warped_hyphae,
    VanillaTileID.warped_planks,
    VanillaTileID.warped_pressure_plate,
    VanillaTileID.warped_slab,
    VanillaTileID.warped_stairs,
    VanillaTileID.warped_standing_sign,
    VanillaTileID.warped_trapdoor,
    VanillaTileID.warped_wall_sign,
    VanillaTileID.warped_wart_block,
], true);

ToolAPI.registerBlockMaterialAsArray("dirt", [
    2, // grass
    3, // dirt
    12, // sand
    13, // gravel
    60, // farmland
    78, 80, // snow
    82, // clay
    88, // soul sand
    110, // mycelium
    198, // grass path
    237, // concrete
    243, // podzol
    VanillaTileID.soul_soil,
], true);

ToolAPI.registerBlockMaterialAsArray("fibre", [
    30, // web
], true);


ToolAPI.registerBlockMaterialAsArray("plant", [
    6, // sapling
    18, 161, // leaves
    31, 32, // grass
    111, // lilypad
    175, // tall grass
    VanillaTileID.nether_wart_block,
    VanillaTileID.warped_wart_block,
    VanillaTileID.shroomlight,
    VanillaTileID.hay_block,
    VanillaTileID.target,
    VanillaTileID.dried_kelp_block,
    VanillaTileID.sponge,
], true);


ToolAPI.registerBlockMaterialAsArray("cobweb", [
    // for older versions
], true);

ToolAPI.registerBlockMaterialAsArray("unbreaking", [
    8, 9, 10, 11, // liquid
    7, 95, // bedrock
    90, 119, 120, // portal blocks
    137, 188, 189 // command block
], true);




/* --------------- DESTROY FUNCS ---------------- */
BlockRegistry.registerDropFunctionForID(VanillaTileID.stone, function(coords, id, data, level, enchant, item) {
    if (level > 0) {
        if (data == 0 && !enchant.silk && !ENCHANT_TAG_GROUP.getTags(item).contains("smelting")) {
            return [[4, 1, 0]];
        }
        else{
            return [[id, 1, data]];
        }
    }
    return [];
}, 1);

BlockRegistry.registerDropFunctionForID(VanillaTileID.grass, function(coords, id, data, level, enchant) {
    if (enchant.silk) {
        return [[id, 1, 0]];
    }
    return [[3, 1, 0]];
});

BlockRegistry.setDestroyLevelForID(VanillaTileID.cobblestone, 1);

BlockRegistry.registerDropFunctionForID(VanillaTileID.gravel, function(coords, id, data, level, enchant) {
    if (Math.random() < 1 / (10 - enchant.fortune * 3)) {
        return [[VanillaItemID.flint, 1, 0]];
    }
    return [[id, 1, 0]];
});

BlockRegistry.registerDropFunctionForID(VanillaTileID.gold_ore, function(coords, id, data, level, enchant, item, blockSource) {
    if (level >= 3) {
        if (ENCHANT_TAG_GROUP.getTags(item).contains("smelting")) {
            ToolAPI.dropOreExp(coords, 2, 3, enchant.experience, blockSource);
            return [[VanillaItemID.gold_ingot, 1, 0]];
        } else {
            return [[VanillaTileID.gold_ore, 1, 0]]
        }
    }
    return [];
}, 3);

BlockRegistry.registerDropFunctionForID(VanillaTileID.iron_ore, function(coords, id, data, level, enchant, item, blockSource) {
    if (level >= 2) {
        if (ENCHANT_TAG_GROUP.getTags(item).contains("smelting")) {
            ToolAPI.dropOreExp(coords, 2, 2, enchant.experience, blockSource);
            return [[VanillaItemID.iron_ingot, 1, 0]];
        } else {
            return [[VanillaTileID.iron_ore, 1, 0]]
        }
    }
    return [];
}, 2);

BlockRegistry.registerDropFunctionForID(VanillaTileID.coal_ore, function(coords, id, data, level, enchant, item, blockSource) {
    if (level >= 1) {
        if (enchant.silk) {
            return [[id, 1, data]];
        }
        ToolAPI.dropOreExp(coords, 0, 2, enchant.experience, blockSource);
        return ToolAPI.fortuneDropModifier([[VanillaItemID.coal, 1, 0]], enchant.fortune);
    }
    return [];
}, 1);

BlockRegistry.registerDropFunctionForID(VanillaTileID.glass, function(coords, id, data, level, enchant) {
    if (enchant.silk) {
        return [[id, 1, 0]];
    }
    return [];
});

BlockRegistry.registerDropFunctionForID(VanillaTileID.lapis_ore, function(coords, id, data, level, enchant, item, blockSource) {
    if (level >= 2) {
        if (enchant.silk) {
            return [[id, 1, data]];
        }
        var drop = [];
        var count = 4 + Math.floor(Math.random() * 6);
        for (var i = 0; i < count; i++) {
            drop.push([VanillaItemID.lapis_lazuli, 1, 0]);
        }
        ToolAPI.dropOreExp(coords, 2, 5, enchant.experience, blockSource);
        return ToolAPI.fortuneDropModifier(drop, enchant.fortune);
    }
    return [];
}, 2);

BlockRegistry.setDestroyLevelForID(VanillaTileID.lapis_block, 2);
BlockRegistry.setDestroyLevelForID(VanillaTileID.dispenser, 1, true);
BlockRegistry.setDestroyLevelForID(VanillaTileID.sandstone, 1);

BlockRegistry.registerDropFunctionForID(VanillaTileID.web, function(coords, id, data, level, enchant) {
    if (level >= 1) {
        if (enchant.silk)
            return [[id, 1, 0]];
        return [[287, 1, 0]];
    }
    return [];
}, 1);

BlockRegistry.setDestroyLevelForID(VanillaTileID.gold_block, 3);
BlockRegistry.setDestroyLevelForID(VanillaTileID.iron_block, 2);

BlockRegistry.registerDropFunctionForID(VanillaTileID.double_stone_slab, function(coords, id, data, level) {
    if (level >= 1) {
        return [[44, 1, data], [44, 1, data]];
    }
    return [];
}, 1);

BlockRegistry.registerDropFunctionForID(VanillaTileID.stone_slab, function(coords, id, data, level) {
    if (level >= 1) {
        return [[id, 1, data%8]];
    }
    return [];
}, 1);

BlockRegistry.setDestroyLevelForID(VanillaTileID.brick_block, 1);
BlockRegistry.setDestroyLevelForID(VanillaTileID.mossy_cobblestone, 1);
BlockRegistry.setDestroyLevelForID(VanillaTileID.obsidian, 4);

BlockRegistry.registerDropFunctionForID(VanillaTileID.mob_spawner, function(coords, id, data, level, enchant, item, blockSource) {
    ToolAPI.dropOreExp(coords, 15, 43, enchant.experience, blockSource);
    return [];
}, 1);

BlockRegistry.registerDropFunctionForID(VanillaTileID.diamond_ore, function(coords, id, data, level, enchant, item, blockSource) {
    if (level >= 3) {
        if (enchant.silk) {
            return [[id, 1, data]];
        }
        ToolAPI.dropOreExp(coords, 3, 7, enchant.experience, blockSource);
        return ToolAPI.fortuneDropModifier([[VanillaItemID.diamond, 1, 0]], enchant.fortune);
    }
    return [];
}, 3);

BlockRegistry.setDestroyLevelForID(VanillaTileID.diamond_block, 3);
BlockRegistry.setDestroyLevelForID(VanillaTileID.furnace, 1, true);
BlockRegistry.registerDropFunctionForID(VanillaTileID.lit_furnace, function(coords, id, data, level, enchant) {
    if (level >= 1) {
        return [[61, 1, 0]];
    }
    return [];
}, 1);

BlockRegistry.setDestroyLevelForID(VanillaTileID.stone_stairs, 1, true);
BlockRegistry.setDestroyLevelForID(VanillaTileID.stone_pressure_plate, 1, true);
BlockRegistry.registerDropFunctionForID(VanillaTileID.iron_door, function(coords, id, data, level, enchant) {
    if (level >= 1) {
        return [[330, 1, 0]];
    }
    return [];
}, 1);

BlockRegistry.registerDropFunctionForID(VanillaTileID.redstone_ore, function(coords, id, data, level, enchant, item, blockSource) {
    if (level >= 3) {
        if (enchant.silk) {
            return [[id, 1, data]];
        }
        ToolAPI.dropOreExp(coords, 2, 5, enchant.experience, blockSource);
        var drop = [];
        var count = 4 + Math.floor(Math.random() * (2 + enchant.fortune));
        for (var i = 0; i < count; i++) {
            drop.push([331, 1, 0]);
        }
        return drop;
    }
    return [];
}, 3);

BlockRegistry.registerDropFunctionForID(VanillaTileID.lit_redstone_ore, function(coords, id, data, level, enchant, item, blockSource) {
    if (level >= 3) {
        if (enchant.silk) {
            return [[73, 1, data]];
        }
        ToolAPI.dropOreExp(coords, 2, 5, enchant.experience, blockSource);
        var drop = [];
        var count = 4 + Math.floor(Math.random() * (2 + enchant.fortune));
        for (var i = 0; i < count; i++) {
            drop.push([331, 1, 0]);
        }
        return drop;
    }
    return [];
}, 3);

BlockRegistry.registerDropFunctionForID(VanillaTileID.snow_layer, function(coords, id, data, level, enchant) {
    if (level > 0) {
        if (data%8 == 7) return [[332, 4, 0]];
        if (data%8 >= 5) return [[332, 3, 0]];
        if (data%8 >= 3) return [[332, 2, 0]];
        return [[332, 1, 0]];
    }
    return [];
});
BlockRegistry.registerDropFunctionForID(VanillaTileID.snow, function(coords, id, data, level, enchant) {
    if (enchant.silk) {
        return [[id, 1, 0]];
    }
    return [[332, 1, 0], [332, 1, 0], [332, 1, 0], [332, 1, 0]];
});

BlockRegistry.setDestroyLevelForID(VanillaTileID.netherrack, 1);
BlockRegistry.setDestroyLevelForID(VanillaTileID.stonebrick, 1);
BlockRegistry.setDestroyLevelForID(VanillaTileID.iron_bars, 1);

BlockRegistry.registerDropFunctionForID(VanillaTileID.glass_pane, function(coords, id, data, level, enchant) {
    if (enchant.silk) {
        return [[id, 1, 0]];
    }
    return [];
});

BlockRegistry.setDestroyLevelForID(VanillaTileID.brick_stairs, 1, true);
BlockRegistry.setDestroyLevelForID(VanillaTileID.stone_brick_stairs, 1, true);

BlockRegistry.registerDropFunctionForID(VanillaTileID.mycelium, function(coords, id, data, level, enchant) {
    if (enchant.silk) {
        return [[id, 1, 0]];
    }
    return [[3, 1, 0]];
});

BlockRegistry.setDestroyLevelForID(VanillaTileID.nether_brick, 1);
BlockRegistry.setDestroyLevelForID(VanillaTileID.nether_brick_fence, 1);
BlockRegistry.setDestroyLevelForID(VanillaTileID.nether_brick_stairs, 1, true);
BlockRegistry.setDestroyLevelForID(VanillaTileID.enchanting_table, 1);

BlockRegistry.registerDropFunctionForID(VanillaTileID.brewing_stand, function(coords, id, data, level) {
    if (level >= 1) {
        return [[VanillaBlockID.brewing_stand, 1, 0]]
    }
    return [];
}, 1);

BlockRegistry.registerDropFunctionForID(VanillaTileID.cauldron, function(coords,id, data, level) {
    if (level >= 1) {
        return [[VanillaBlockID.cauldron, 1, 0]]
    }
    return [];
}, 1);

BlockRegistry.setDestroyLevelForID(VanillaTileID.end_stone, 1);
BlockRegistry.setDestroyLevelForID(VanillaTileID.dropper, 1, true);
BlockRegistry.setDestroyLevelForID(VanillaTileID.sandstone_stairs, 1, true);

BlockRegistry.registerDropFunctionForID(VanillaTileID.emerald_block, function(coords, id, data, level, enchant, item, blockSource) {
    if (level >= 3) {
        if (enchant.silk) {
            return [[id, 1, data]];
        }
        ToolAPI.dropOreExp(coords, 3, 7, enchant.experience, blockSource);
        return ToolAPI.fortuneDropModifier([[VanillaItemID.emerald, 1, 0]], enchant.fortune);
    }
    return [];
}, 3);

BlockRegistry.registerDropFunctionForID(VanillaTileID.ender_chest, function(coords, id, data, level, enchant) {
    if (level >= 1) {
        if (enchant.silk) {
            return [[id, 1, 0]];
        }
        return [[VanillaBlockID.obsidian, 8, 0]]
    }
    return [];
}, 1);

BlockRegistry.setDestroyLevelForID(VanillaTileID.emerald_block, 3);
BlockRegistry.setDestroyLevelForID(VanillaTileID.cobblestone_wall, 1);
BlockRegistry.setDestroyLevelForID(VanillaTileID.anvil, 1);
BlockRegistry.setDestroyLevelForID(VanillaTileID.light_weighted_pressure_plate, 1, true);
BlockRegistry.setDestroyLevelForID(VanillaTileID.heavy_weighted_pressure_plate, 1, true);
BlockRegistry.setDestroyLevelForID(VanillaTileID.redstone_block, 1);

BlockRegistry.registerDropFunctionForID(VanillaTileID.quartz_ore, function(coords, id, data, level, enchant, item, blockSource) {
    if (level >= 2) {
        if (enchant.silk) {
            return [[id, 1, data]];
        }
        ToolAPI.dropOreExp(coords, 2, 5, enchant.experience, blockSource);
        return ToolAPI.fortuneDropModifier([[406, 1, 0]], enchant.fortune);
    }
    return [];
}, 2);

BlockRegistry.registerDropFunctionForID(VanillaTileID.hopper, function(coords, id, data, level) {
    if (level >= 1) {
        return [[410, 1, 0]]
    }
    return [];
}, 1);

BlockRegistry.setDestroyLevelForID(VanillaTileID.quartz_block, 1);
BlockRegistry.setDestroyLevelForID(VanillaTileID.quartz_stairs, 1, true);

BlockRegistry.registerDropFunctionForID(VanillaTileID.stained_glass_pane, function(coords, id, data, level, enchant) {
    if (enchant.silk) {
        return [[id, 1, data]];
    }
    return [];
});

BlockRegistry.setDestroyLevelForID(VanillaTileID.iron_trapdoor, 2, true);
BlockRegistry.setDestroyLevelForID(VanillaTileID.prismarine, 1);

BlockRegistry.registerDropFunctionForID(VanillaTileID.sealantern, function(coords, id, data, level, enchant) {
    if (enchant.silk) {
        return [[id, 1, data]];
    }
    var drop = [];
    var count = 2 + Math.floor(Math.random() * (2 + enchant.fortune));
    if (count > 5) count = 5;
    for (var i = 0; i < count; i++) {
        drop.push([422, 1, 0]);
    }
    return drop;
});

BlockRegistry.setDestroyLevelForID(VanillaTileID.hardened_clay, 1);
BlockRegistry.setDestroyLevelForID(VanillaTileID.coal_block, 1);
BlockRegistry.setDestroyLevelForID(VanillaTileID.red_sandstone, 1);
BlockRegistry.setDestroyLevelForID(VanillaTileID.red_sandstone_stairs, 1, true);

BlockRegistry.registerDropFunctionForID(VanillaTileID.double_stone_slab2, function(coords, id, data, level, enchant) {
    if (level >= 1) {
        return [[182, 1, data], [182, 1, data]];
    }
    return [];
}, 1);

BlockRegistry.registerDropFunctionForID(VanillaTileID.stone_slab2, function(coords, id, data, level, enchant) {
    if (level >= 1) {
        return [[id, 1, data%8]];
    }
    return [];
}, 1);

BlockRegistry.registerDropFunctionForID(VanillaTileID.packed_ice, function(coords, id, data, level, enchant) {
    if (level >= 1 && enchant.silk) {
        return [[id, 1, data]];
    }
    return [];
});

BlockRegistry.registerDropFunctionForID(VanillaTileID.grass_path, function(coords, id, data, level, enchant) {
    if (enchant.silk) {
        return [[id, 1, 0]];
    }
    return [[3, 1, 0]];
});

BlockRegistry.setDestroyLevelForID(VanillaTileID.purpur_block, 1);
BlockRegistry.setDestroyLevelForID(VanillaTileID.purpur_stairs, 1, true);
BlockRegistry.setDestroyLevelForID(VanillaTileID.end_bricks, 1);
BlockRegistry.setDestroyLevelForID(VanillaTileID.magma, 1);
BlockRegistry.setDestroyLevelForID(VanillaTileID.red_nether_brick, 1);
BlockRegistry.setDestroyLevelForID(VanillaTileID.bone_block, 1, true);

// glazed terracotta
BlockRegistry.setDestroyLevelForID(219, 1, true);
BlockRegistry.setDestroyLevelForID(220, 1, true);
BlockRegistry.setDestroyLevelForID(221, 1, true);
BlockRegistry.setDestroyLevelForID(222, 1, true);
BlockRegistry.setDestroyLevelForID(223, 1, true);
BlockRegistry.setDestroyLevelForID(224, 1, true);
BlockRegistry.setDestroyLevelForID(225, 1, true);
BlockRegistry.setDestroyLevelForID(226, 1, true);
BlockRegistry.setDestroyLevelForID(227, 1, true);
BlockRegistry.setDestroyLevelForID(228, 1, true);
BlockRegistry.setDestroyLevelForID(229, 1, true);
BlockRegistry.setDestroyLevelForID(231, 1, true);
BlockRegistry.setDestroyLevelForID(232, 1, true);
BlockRegistry.setDestroyLevelForID(233, 1, true);
BlockRegistry.setDestroyLevelForID(234, 1, true);
BlockRegistry.setDestroyLevelForID(235, 1, true);

BlockRegistry.setDestroyLevelForID(VanillaTileID.concrete, 1);

BlockRegistry.registerDropFunctionForID(VanillaTileID.stained_glass, function(coords, id, data, level, enchant) {
    if (enchant.silk) {
        return [[id, 1, data]];
    }
    return [];
});

BlockRegistry.setDestroyLevelForID(VanillaTileID.stonecutter, 1);
BlockRegistry.setDestroyLevelForID(VanillaTileID.observer, 1, true);

BlockRegistry.registerDropFunctionForID(VanillaTileID.podzol, function(coords, id, data, level, enchant) {
    if (enchant.silk) {
        return [[243, 1, 0]];
    }
    return [[3, 1, 0]];
});

// prismarine stairs
BlockRegistry.setDestroyLevelForID(VanillaTileID.prismarine_stairs, 1, true);
BlockRegistry.setDestroyLevelForID(VanillaTileID.dark_prismarine_stairs, 1, true);
BlockRegistry.setDestroyLevelForID(VanillaTileID.prismarine_bricks_stairs, 1, true);

BlockRegistry.registerDropFunctionForID(VanillaTileID.blue_ice, function(coords, id, data, level, enchant) {
    if (level >= 1 && enchant.silk) {
        return [[BlockRegistry.convertBlockToItemId(id), 1, data]];
    }
    return [];
});

BlockRegistry.registerDropFunctionForID(VanillaTileID.coral, function(coords, id, data, level, enchant) {
    if (level >= 1 && enchant.silk) {
        return [[BlockRegistry.convertBlockToItemId(id), 1, data]];
    }
    return [];
});

BlockRegistry.registerDropFunctionForID(VanillaTileID.coral_block, function(coords, id, data, level, enchant) {
    if (level >= 1) {
        if (enchant.silk) {
            return [[BlockRegistry.convertBlockToItemId(id), 1, data]];
        }
        return [[BlockRegistry.convertBlockToItemId(id), 1, data + 8]];
    }
    return [];
});

BlockRegistry.registerDropFunctionForID(VanillaTileID.coral_fan, function(coords, id, data, level, enchant) {
    if (level >= 1 && enchant.silk) {
        return [[VanillaBlockID.coral_fan, 1, data % 8]];
    }
    return [];
});

BlockRegistry.registerDropFunctionForID(VanillaTileID.coral_fan_dead, function(coords, id, data, level, enchant) {
    if (level >= 1 && enchant.silk) {
        return [[VanillaBlockID.coral_fan_dead, 1, data % 8]];
    }
    return [];
});

BlockRegistry.registerDropFunctionForID(VanillaTileID.coral_fan_hang, function(coords, id, data, level, enchant) {
    if (level >= 1 && enchant.silk) {
        var itemID = (data % 4 < 2)? VanillaBlockID.coral_fan : VanillaBlockID.coral_fan_dead;
        return [[itemID, 1, data%2]];
    }
    return [];
});

BlockRegistry.registerDropFunctionForID(VanillaTileID.coral_fan_hang2, function(coords, id, data, level, enchant) {
    if (level >= 1 && enchant.silk) {
        var itemID = (data % 4 < 2)? VanillaBlockID.coral_fan : VanillaBlockID.coral_fan_dead;
        return [[itemID, 1, data%2 + 2]];
    }
    return [];
});

BlockRegistry.registerDropFunctionForID(VanillaTileID.coral_fan_hang3, function(coords, id, data, level, enchant) {
    if (level >= 1 && enchant.silk) {
        var itemID = (data % 4 < 2)? VanillaBlockID.coral_fan : VanillaBlockID.coral_fan_dead;
        return [[itemID, 1, 4]];
    }
    return [];
});

BlockRegistry.registerDropFunctionForID(VanillaTileID.stone_slab3, function(coords, id, data, level, enchant) {
    if (level >= 1) {
        return [[VanillaBlockID.stone_slab3, 1, data % 8]];
    }
    return [];
}, 1);

BlockRegistry.registerDropFunctionForID(VanillaTileID.stone_slab4, function(coords, id, data, level, enchant) {
    if (level >= 1) {
        return [[VanillaBlockID.stone_slab4, 1, data % 8]];
    }
    return [];
}, 1);

BlockRegistry.registerDropFunctionForID(VanillaTileID.double_stone_slab3, function(coords, id, data, level, enchant) {
    if (level >= 1) {
        return [[VanillaBlockID.stone_slab3, 1, data], [VanillaBlockID.stone_slab3, 1, data]];
    }
    return [];
}, 1);

BlockRegistry.registerDropFunctionForID(VanillaTileID.double_stone_slab4, function(coords, id, data, level, enchant) {
    if (level >= 1) {
        return [[VanillaBlockID.stone_slab4, 1, data], [VanillaBlockID.stone_slab4, 1, data]];
    }
    return [];
}, 1);

// stone stairs
BlockRegistry.setDestroyLevelForID(VanillaTileID.granite_stairs, 1, true);
BlockRegistry.setDestroyLevelForID(VanillaTileID.diorite_stairs, 1, true);
BlockRegistry.setDestroyLevelForID(VanillaTileID.andesite_stairs, 1, true);
BlockRegistry.setDestroyLevelForID(VanillaTileID.polished_granite_stairs, 1, true);
BlockRegistry.setDestroyLevelForID(VanillaTileID.polished_diorite_stairs, 1, true);
BlockRegistry.setDestroyLevelForID(VanillaTileID.polished_andesite_stairs, 1, true);
BlockRegistry.setDestroyLevelForID(VanillaTileID.mossy_stone_brick_stairs, 1, true);
BlockRegistry.setDestroyLevelForID(VanillaTileID.smooth_red_sandstone_stairs, 1, true);
BlockRegistry.setDestroyLevelForID(VanillaTileID.smooth_sandstone_stairs, 1, true);
BlockRegistry.setDestroyLevelForID(VanillaTileID.end_brick_stairs, 1, true);
BlockRegistry.setDestroyLevelForID(VanillaTileID.mossy_cobblestone_stairs, 1, true);
BlockRegistry.setDestroyLevelForID(VanillaTileID.normal_stone_stairs, 1, true);

BlockRegistry.setDestroyLevelForID(VanillaTileID.smooth_stone, 1);

BlockRegistry.setDestroyLevelForID(VanillaTileID.red_nether_brick_stairs, 1, true);
BlockRegistry.setDestroyLevelForID(VanillaTileID.smooth_quartz_stairs, 1, true);

BlockRegistry.setDestroyLevelForID(VanillaTileID.grindstone, 1, true);
BlockRegistry.setDestroyLevelForID(VanillaTileID.blast_furnace, 1, true);
BlockRegistry.setDestroyLevelForID(VanillaTileID.stonecutter, 1, true);
BlockRegistry.setDestroyLevelForID(VanillaTileID.smoker, 1, true);

BlockRegistry.registerDropFunctionForID(VanillaTileID.lit_smoker, function(coords, id, data, level) {
    if (level >= 1) {
        return [[VanillaBlockID.smoker, 1, 0]];
    }
    return [];
}, 1);

BlockRegistry.setDestroyLevelForID(VanillaTileID.bell, 1, true);
BlockRegistry.setDestroyLevelForID(VanillaTileID.lantern, 1, true);

BlockRegistry.registerDropFunctionForID(VanillaTileID.lava_cauldron, function(coords,id, data, level) {
    if (level >= 1) {
        return [[VanillaItemID.cauldron, 1, 0]]
    }
    return [];
}, 1);

BlockRegistry.registerDropFunctionForID(VanillaTileID.lit_blast_furnace, function(coords, id, data, level) {
    if (level >= 1) {
        return [[VanillaBlockID.blast_furnace, 1, 0]];
    }
    return [];
}, 1);

BlockRegistry.setDestroyLevelForID(VanillaTileID.ancient_debris, 4);
BlockRegistry.setDestroyLevelForID(VanillaTileID.basalt, 1);
BlockRegistry.setDestroyLevelForID(VanillaTileID.blackstone, 1);

BlockRegistry.registerDropFunctionForID(VanillaTileID.blackstone_double_slab, function(coords, id, data, level, enchant) {
    if (level >= 1) {
        let itemID = VanillaBlockID.blackstone_slab;
        return [[itemID, 1, 0], [itemID, 1, 0]];
    }
    return [];
}, 1);

BlockRegistry.setDestroyLevelForID(VanillaTileID.blackstone_slab, 1);
BlockRegistry.setDestroyLevelForID(VanillaTileID.blackstone_stairs, 1);
BlockRegistry.setDestroyLevelForID(VanillaTileID.blackstone_wall, 1);
BlockRegistry.setDestroyLevelForID(VanillaTileID.chain, 1);
BlockRegistry.setDestroyLevelForID(VanillaTileID.chiseled_nether_bricks, 1);
BlockRegistry.setDestroyLevelForID(VanillaTileID.chiseled_polished_blackstone, 1);
BlockRegistry.setDestroyLevelForID(VanillaTileID.cracked_nether_bricks, 1);
BlockRegistry.setDestroyLevelForID(VanillaTileID.cracked_polished_blackstone_bricks, 1);
BlockRegistry.setDestroyLevelForID(VanillaTileID.crying_obsidian, 4);

BlockRegistry.registerDropFunctionForID(VanillaTileID.gilded_blackstone, function(coords, id, data, level, enchant) {
    if (level >= 1) {
        if (Math.random() < 1 / (10 - enchant.fortune * 3)) {
            let drop = [];
            let count = 2 + Math.floor(Math.random() * 4);
            for (let i = 0; i < count; i++) {
                drop.push([VanillaItemID.gold_nugget, 1, 0]);
            }
            return drop;
        }
        return [[BlockRegistry.convertBlockToItemId(id), 1, 0]];
    }
    return [];
}, 1);

BlockRegistry.setDestroyLevelForID(VanillaTileID.lodestone, 1);

BlockRegistry.registerDropFunctionForID(VanillaTileID.nether_gold_ore, function(coords, id, data, level, enchant, item, blockSource) {
    if (level >= 1) {
        if (enchant.silk) {
            return [[BlockRegistry.convertBlockToItemId(id), 1, 0]];
        }
        var drop = [];
        var count = 2 + Math.floor(Math.random() * 5);
        for (var i = 0; i < count; i++) {
            drop.push([VanillaItemID.gold_nugget, 1, 0]);
        }
        ToolAPI.dropOreExp(coords, 1, 1, enchant.experience, blockSource);
        return ToolAPI.fortuneDropModifier(drop, enchant.fortune);
    }
    return [];
}, 1);
BlockRegistry.setDestroyLevelForID(VanillaTileID.netherite_block, 4);

BlockRegistry.setDestroyLevelForID(VanillaTileID.polished_basalt, 1);
BlockRegistry.setDestroyLevelForID(VanillaTileID.polished_blackstone, 1);

BlockRegistry.registerDropFunctionForID(VanillaTileID.polished_blackstone_brick_double_slab, function(coords, id, data, level, enchant) {
    if (level >= 1) {
        let itemID = VanillaBlockID.polished_blackstone_brick_slab;
        return [[itemID, 1, 0], [itemID, 1, 0]];
    }
    return [];
}, 1);

BlockRegistry.setDestroyLevelForID(VanillaTileID.polished_blackstone_brick_slab, 1);
BlockRegistry.setDestroyLevelForID(VanillaTileID.polished_blackstone_brick_stairs, 1);
BlockRegistry.setDestroyLevelForID(VanillaTileID.polished_blackstone_brick_wall, 1);
BlockRegistry.setDestroyLevelForID(VanillaTileID.polished_blackstone_bricks, 1);
BlockRegistry.setDestroyLevelForID(VanillaTileID.polished_blackstone_button, 1, true);

BlockRegistry.registerDropFunctionForID(VanillaTileID.polished_blackstone_double_slab, function(coords, id, data, level, enchant) {
    if (level >= 1) {
        let itemID = VanillaBlockID.polished_blackstone_slab;
        return [[itemID, 1, 0], [itemID, 1, 0]];
    }
    return [];
}, 1);

BlockRegistry.setDestroyLevelForID(VanillaTileID.polished_blackstone_pressure_plate, 1);
BlockRegistry.setDestroyLevelForID(VanillaTileID.polished_blackstone_slab, 1);
BlockRegistry.setDestroyLevelForID(VanillaTileID.polished_blackstone_stairs, 1);
BlockRegistry.setDestroyLevelForID(VanillaTileID.polished_blackstone_wall, 1);
BlockRegistry.setDestroyLevelForID(VanillaTileID.quartz_bricks, 1);
BlockRegistry.setDestroyLevelForID(VanillaTileID.soul_lantern, 1, true);
BlockRegistry.setDestroyLevelForID(VanillaTileID.respawn_anchor, 4, true);

BlockRegistry.registerDropFunctionForID(VanillaTileID.web, function(coords, id, data, level, enchant, item) {
    if(enchant.silk){
        return [[id, 1, 0]];
    } 
    if(level >= 1){
        var toolData = ToolAPI.getToolData(item.id);
        if(toolData && toolData.isWeapon){
            return [[VanillaItemID.string, 1, 0]];
        } else {
            return [[id, 1, 0]];
        }
    }
    return [];
}, 1);

// ice
Callback.addCallback("DestroyBlock", function(coords, block, player) {
    if (block.id === 79) {
        var item = Entity.getCarriedItem(player);
        var enchant = ToolAPI.getEnchantExtraData(item.extra);
        var toolData = ToolAPI.getToolData(item.id);
        if (toolData && !toolData.isNative && GameAPI.isItemSpendingAllowed(player) && ToolAPI.getToolLevelViaBlock(item.id, block.id) > 0) {
            var region = BlockSource.getDefaultForActor(player);
            if (toolData && toolData.modifyEnchant) {
                toolData.modifyEnchant(enchant, item, coords, block);
            }
            if (enchant.silk) {
                region.destroyBlock(coords.x, coords.y, coords.z, false);
                region.spawnDroppedItem(coords.x + .5, coords.y + .5, coords.z + .5, block.id, 1, 0);
            }
        }
    }
});
