// THIS DATA IS USED FOR VERSION 1.11

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


/* --------------- block materials ---------------- */
ToolAPI.addBlockMaterial("stone", 10 / 3); // +
ToolAPI.addBlockMaterial("wood", 1); // +
ToolAPI.addBlockMaterial("dirt", 1); // +
ToolAPI.addBlockMaterial("plant", 1); // +
ToolAPI.addBlockMaterial("fibre", 1); // +
ToolAPI.addBlockMaterial("cobweb", 10 / 3); // +
ToolAPI.addBlockMaterial("unbreaking", 999999999); // +




/* -------------- tool registration --------------- */
// pickaxes
ToolAPI.registerTool(270, "wood", ["stone"], {
    isNative: true,
    damage: 1
}); // wooden
ToolAPI.registerTool(274, "stone", ["stone"], {
    isNative: true,
    damage: 1
}); // stone
ToolAPI.registerTool(257, "iron", ["stone"], {
    isNative: true,
    damage: 1
}); // iron
ToolAPI.registerTool(285, "golden", ["stone"], {
    isNative: true,
    damage: 1
}); // golden
ToolAPI.registerTool(278, "diamond", ["stone"], {
    isNative: true,
    damage: 1
}); // diamond

// axes
ToolAPI.registerTool(271, "wood", ["wood"], {
    isNative: true,
    damage: 2
}); // wooden
ToolAPI.registerTool(275, "stone", ["wood"], {
    isNative: true,
    damage: 2
}); // stone
ToolAPI.registerTool(258, "iron", ["wood"], {
    isNative: true,
    damage: 2
}); // iron
ToolAPI.registerTool(286, "golden", ["wood"], {
    isNative: true,
    damage: 2
}); // golden
ToolAPI.registerTool(279, "diamond", ["wood"], {
    isNative: true,
    damage: 2
}); // diamond

// shovels
ToolAPI.registerTool(269, "wood", ["dirt"], {
    isNative: true,
    damage: 0
}); // wooden
ToolAPI.registerTool(273, "stone", ["dirt"], {
    isNative: true,
    damage: 0
}); // stone
ToolAPI.registerTool(256, "iron", ["dirt"], {
    isNative: true,
    damage: 0
}); // iron
ToolAPI.registerTool(284, "golden", ["dirt"], {
    isNative: true,
    damage: 0
}); // golden
ToolAPI.registerTool(277, "diamond", ["dirt"], {
    isNative: true,
    damage: 0
}); // diamond



// sheers
ToolAPI.registerTool(359, "iron", ["fibre"], {isNative: true, damage: 0});

// swords 
ToolAPI.registerSword(268, "wood", {isNative: true});
ToolAPI.registerSword(272, "stone", {isNative: true});
ToolAPI.registerSword(267, "iron", {isNative: true});
ToolAPI.registerSword(283, "golden", {isNative: true});
ToolAPI.registerSword(276, "diamond", {isNative: true});



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
], true);

ToolAPI.registerBlockMaterialAsArray("wood", [
    5, // planks
    17, // log1
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
    107, // fence gate
    127, // cocoa
    134, 135, 136, 163, 164, // wood stairs
    143, // wooden button
    146, // trapped chest
    151, 178, // daylight sensor
    157, // wooden double slab
    158, // wooden slab
    162, // log2
    183, 184, 185, 186, 187, // fence gate 2
    193, 194, 195, 196, 197, // door 2
    260, 261, 262, 263, 264, 265, // stripped log
    395, 396, 397, 398, 399, // wooden button 2
    400, 401, 402, 403, 404, // wooden trapdoor 2
    405, 406, 407, 408, 409, // wooden pressure plate 2
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
], true);

ToolAPI.registerBlockMaterialAsArray("fibre", [
    30, // web
], true);


ToolAPI.registerBlockMaterialAsArray("plant", [
    6, // sapling
    18, 161, // leaves
    31, 32, // grass
    81, // cactus
    106, // vine
    111, // lilypad
    175, // tall grass
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
BlockRegistry.registerDropFunctionForID(1, function(coords, id, data, level, enchant) { // stone
    if (level > 0) {
        if (data == 0 && !enchant.silk) {
            return [
                [4, 1, 0]
            ];
        } else {
            return [
                [id, 1, data]
            ];
        }
    }
    return [];
}, 1);

BlockRegistry.registerDropFunctionForID(2, function(coords, id, data, level, enchant) { //grass
    if (enchant.silk) {
        return [
            [2, 1, 0]
        ];
    }
    return [
        [3, 1, 0]
    ];
});

BlockRegistry.setDestroyLevelForID(4, 1); // cobblestone

BlockRegistry.registerDropFunctionForID(13, function(coords, id, data, level, enchant) { // gravel
    if (Math.random() < [0.1, 0.14, 0.25, 1][enchant.fortune || 0]) {
        return [
            [318, 1, 0]
        ];
    }
    return [
        [13, 1, 0]
    ];
});

BlockRegistry.setDestroyLevelForID(14, 3); // gold ore
BlockRegistry.setDestroyLevelForID(15, 2); // iron ore

BlockRegistry.registerDropFunctionForID(16, function(coords, id, data, level, enchant, item, region) { // coal ore
    if (level >= 1) {
        if (enchant.silk) {
            return [
                [id, 1, data]
            ];
        }
        ToolAPI.dropOreExp(coords, 0, 2, enchant.experience, region);
        return ToolAPI.fortuneDropModifier([
            [263, 1, 0]
        ], enchant.fortune);
    }
    return [];
}, 1);

BlockRegistry.registerDropFunctionForID(20, function(coords, id, data, level, enchant) { // glass
    if (enchant.silk) {
        return [
            [20, 1, 0]
        ];
    }
    return [];
});

BlockRegistry.registerDropFunctionForID(21, function(coords, id, data, level, enchant, item, region) { // lapis ore
    if (level >= 1) {
        if (enchant.silk) {
            return [
                [id, 1, data]
            ];
        }
        var drop = [];
        var count = 4 + parseInt(Math.random() * 6);
        for (var i = 0; i < count; i++) {
            drop.push([351, 1, 4]);
        }
        ToolAPI.dropOreExp(coords, 2, 5, enchant.experience, region);
        return ToolAPI.fortuneDropModifier(drop, enchant.fortune);
    }
    return [];
}, 2);

BlockRegistry.setDestroyLevelForID(22, 2); // lapis block
BlockRegistry.setDestroyLevelForID(23, 1, true); // dispenser
BlockRegistry.setDestroyLevelForID(24, 1); // sandstone

BlockRegistry.registerDropFunctionForID(30, function(coords, id, data, level, enchant) { // cobweb
    if (level >= 1) {
        if (enchant.silk)
            return [
                [id, 1, 0]
            ];
        return [
            [287, 1, 0]
        ];
    }
    return [];
}, 1);

BlockRegistry.setDestroyLevelForID(41, 3); // gold block
BlockRegistry.setDestroyLevelForID(42, 2); // iron block

BlockRegistry.registerDropFunctionForID(43, function(coords, id, data, level) { // double slab
    if (level >= 1) {
        return [
            [44, 1, data],
            [44, 1, data]
        ];
    }
    return [];
}, 1);

BlockRegistry.registerDropFunctionForID(44, function(coords, id, data, level) { // stone slabs
    if (level >= 1) {
        return [
            [id, 1, data % 8]
        ];
    }
    return [];
}, 1);

BlockRegistry.setDestroyLevelForID(45, 1); // bricks
BlockRegistry.setDestroyLevelForID(48, 1); // mossy cobblestone
BlockRegistry.setDestroyLevelForID(49, 4); // obsidian

BlockRegistry.registerDropFunctionForID(52, function(coords, id, data, level, enchant, item, region) { // mob spawner
    ToolAPI.dropOreExp(coords, 15, 43, enchant.experience, region);
    return [];
}, 1);

BlockRegistry.registerDropFunctionForID(56, function(coords, id, data, level, enchant, item, region) { // diamond ore
    if (level >= 3) {
        if (enchant.silk) {
            return [
                [id, 1, data]
            ];
        }
        ToolAPI.dropOreExp(coords, 3, 7, enchant.experience, region);
        return ToolAPI.fortuneDropModifier([
            [264, 1, 0]
        ], enchant.fortune);
    }
    return [];
}, 3);

BlockRegistry.setDestroyLevelForID(57, 3); // diamond block
BlockRegistry.setDestroyLevelForID(61, 1, true); // furnace
BlockRegistry.registerDropFunctionForID(62, function(coords, id, data, level, enchant) { // burning furnace
    if (level >= 1) {
        return [
            [61, 1, 0]
        ];
    }
    return [];
}, 1);

BlockRegistry.setDestroyLevelForID(67, 1, true); // cobble stairs
BlockRegistry.setDestroyLevelForID(70, 1, true); // pressure plate
BlockRegistry.registerDropFunctionForID(71, function(coords, id, data, level, enchant) { // iron door
    if (level >= 2) {
        return [
            [330, 1, 0]
        ];
    }
    return [];
}, 1);

BlockRegistry.registerDropFunctionForID(73, function(coords, id, data, level, enchant, item, region) { // redstone ore
    if (level >= 3) {
        if (enchant.silk) {
            return [
                [id, 1, data]
            ];
        }
        ToolAPI.dropOreExp(coords, 2, 5, enchant.experience, region);
        var drop = [];
        var count = 4 + parseInt(Math.random() * (2 + enchant.fortune));
        for (var i = 0; i < count; i++) {
            drop.push([331, 1, 0]);
        }
        return drop;
    }
    return [];
}, 3);

BlockRegistry.registerDropFunctionForID(74, function(coords, id, data, level, enchant, item, region) { // redstone ore
    if (level >= 3) {
        if (enchant.silk) {
            return [
                [73, 1, data]
            ];
        }
        ToolAPI.dropOreExp(coords, 2, 5, enchant.experience, region);
        var drop = [];
        var count = 4 + parseInt(Math.random() * (2 + enchant.fortune));
        for (var i = 0; i < count; i++) {
            drop.push([331, 1, 0]);
        }
        return drop;
    }
    return [];
}, 3);

BlockRegistry.registerDropFunctionForID(78, function(coords, id, data, level, enchant) { // snow layer
    if (level > 0) {
        if (data % 8 == 7) return [
            [332, 4, 0]
        ];
        if (data % 8 >= 5) return [
            [332, 3, 0]
        ];
        if (data % 8 >= 3) return [
            [332, 2, 0]
        ];
        return [
            [332, 1, 0]
        ];
    }
    return [];
});
BlockRegistry.registerDropFunctionForID(80, function(coords, id, data, level, enchant) { // snow block
    if (enchant.silk) {
        return [
            [80, 1, 0]
        ];
    }
    return [
        [332, 1, 0],
        [332, 1, 0],
        [332, 1, 0],
        [332, 1, 0]
    ];
});

BlockRegistry.setDestroyLevelForID(87, 1); // netherrack
BlockRegistry.setDestroyLevelForID(98, 1); // stone brick
BlockRegistry.setDestroyLevelForID(101, 1); // iron bars

BlockRegistry.registerDropFunctionForID(102, function(coords, id, data, level, enchant) { // glass pane
    if (enchant.silk) {
        return [
            [102, 1, 0]
        ];
    }
    return [];
});

BlockRegistry.setDestroyLevelForID(108, 1, true); // brick stairs
BlockRegistry.setDestroyLevelForID(109, 1, true); // stone brick stairs

BlockRegistry.registerDropFunctionForID(110, function(coords, id, data, level, enchant) { // mycelium
    if (enchant.silk) {
        return [
            [110, 1, 0]
        ];
    }
    return [
        [3, 1, 0]
    ];
});

BlockRegistry.setDestroyLevelForID(112, 1); // nether brick
BlockRegistry.setDestroyLevelForID(113, 1); // nether brick fence
BlockRegistry.setDestroyLevelForID(114, 1, true); // nether brick stairs
BlockRegistry.setDestroyLevelForID(116, 1); // ench table

BlockRegistry.registerDropFunctionForID(117, function(coords, id, data, level) { // brewing stand
    if (level >= 1) {
        return [
            [379, 1, 0]
        ]
    }
    return [];
}, 1);

BlockRegistry.registerDropFunctionForID(118, function(coords, id, data, level) { // cauldron
    if (level >= 1) {
        return [
            [380, 1, 0]
        ]
    }
    return [];
}, 1);

BlockRegistry.setDestroyLevelForID(121, 1); // end stone
BlockRegistry.setDestroyLevelForID(125, 1, true); // dropper
BlockRegistry.setDestroyLevelForID(128, 1, true); // sandstone stairs

BlockRegistry.registerDropFunctionForID(129, function(coords, id, data, level, enchant, item, region) { // emerald ore
    if (level >= 3) {
        if (enchant.silk) {
            return [
                [id, 1, data]
            ];
        }
        ToolAPI.dropOreExp(coords, 3, 7, enchant.experience, region);
        return ToolAPI.fortuneDropModifier([
            [388, 1, 0]
        ], enchant.fortune);
    }
    return [];
}, 3);

BlockRegistry.registerDropFunctionForID(130, function(coords, id, data, level, enchant) { // ender chest
    if (level >= 1) {
        if (enchant.silk) {
            return [
                [id, 1, 0]
            ];
        }
        return [
            [49, 8, 0]
        ]
    }
    return [];
}, 1);

BlockRegistry.setDestroyLevelForID(133, 3); // emerald block
BlockRegistry.setDestroyLevelForID(139, 1); // cobblestone wall
BlockRegistry.setDestroyLevelForID(145, 1); // anvil
BlockRegistry.setDestroyLevelForID(147, 1, true); // pressure plate
BlockRegistry.setDestroyLevelForID(148, 1, true); // pressure plate
BlockRegistry.setDestroyLevelForID(152, 1); // redstone block

BlockRegistry.registerDropFunctionForID(153, function(coords, id, data, level, enchant, item, region) { // nether quartz ore
    if (level >= 2) {
        if (enchant.silk) {
            return [
                [id, 1, data]
            ];
        }
        ToolAPI.dropOreExp(coords, 2, 5, enchant.experience, region);
        return ToolAPI.fortuneDropModifier([
            [406, 1, 0]
        ], enchant.fortune);
    }
    return [];
}, 2);

BlockRegistry.registerDropFunctionForID(154, function(coords, id, data, level) { // hopper
    if (level >= 1) {
        return [
            [410, 1, 0]
        ]
    }
    return [];
}, 1);

BlockRegistry.setDestroyLevelForID(155, 1); // quartz
BlockRegistry.setDestroyLevelForID(156, 1, true); // quartz stairs
BlockRegistry.setDestroyLevelForID(159, 1); // stained clay

BlockRegistry.registerDropFunctionForID(160, function(coords, id, data, level, enchant) { // stained glass pane
    if (enchant.silk) {
        return [
            [id, 1, data]
        ];
    }
    return [];
});

BlockRegistry.setDestroyLevelForID(167, 2, true); // iron trapdoor
BlockRegistry.setDestroyLevelForID(168, 1); // prismarine

BlockRegistry.registerDropFunctionForID(169, function(coords, id, data, level, enchant) { // sea lantern
    if (enchant.silk) {
        return [
            [id, 1, data]
        ];
    }
    var drop = [];
    var count = 2 + parseInt(Math.random() * (2 + enchant.fortune));
    if (count > 5) count = 5;
    for (var i = 0; i < count; i++) {
        drop.push([422, 1, 0]);
    }
    return drop;
});

BlockRegistry.setDestroyLevelForID(172, 1); // hardened clay
BlockRegistry.setDestroyLevelForID(173, 1); // coal block
BlockRegistry.setDestroyLevelForID(179, 1); // red sandstone
BlockRegistry.setDestroyLevelForID(180, 1, true); // red sandstone stairs

BlockRegistry.registerDropFunctionForID(181, function(coords, id, data, level, enchant) { // double slab 2
    if (level >= 1) {
        return [
            [182, 1, data],
            [182, 1, data]
        ];
    }
    return [];
}, 1);

BlockRegistry.registerDropFunctionForID(182, function(coords, id, data, level, enchant) { // stone slab 2
    if (level >= 1) {
        return [
            [id, 1, data % 8]
        ];
    }
    return [];
}, 1);

BlockRegistry.registerDropFunctionForID(194, function(coords, id, data, level, enchant) { // packed ice
    if (level >= 1 && enchant.silk) {
        return [
            [194, 1, data]
        ];
    }
    return [];
});

BlockRegistry.registerDropFunctionForID(198, function(coords, id, data, level, enchant) { // grass path
    if (enchant.silk) {
        return [
            [198, 1, 0]
        ];
    }
    return [
        [3, 1, 0]
    ];
});

BlockRegistry.setDestroyLevelForID(201, 1); // purpur
BlockRegistry.setDestroyLevelForID(203, 1, true); // purpur stairs
BlockRegistry.setDestroyLevelForID(206, 1); // end brick
BlockRegistry.setDestroyLevelForID(213, 1); // magma
BlockRegistry.setDestroyLevelForID(215, 1); // red nether brick
BlockRegistry.setDestroyLevelForID(216, 1, true); // bone block

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

BlockRegistry.setDestroyLevelForID(236, 1); // concrete

BlockRegistry.registerDropFunctionForID(241, function(coords, id, data, level, enchant) { // stained glass
    if (enchant.silk) {
        return [
            [id, 1, data]
        ];
    }
    return [];
});

BlockRegistry.setDestroyLevelForID(245, 1); // stonecutter
BlockRegistry.setDestroyLevelForID(251, 1, true); // observer

BlockRegistry.registerDropFunctionForID(243, function(coords, id, data, level, enchant) { // podzol
    if (enchant.silk) {
        return [
            [243, 1, 0]
        ];
    }
    return [
        [3, 1, 0]
    ];
});

// prismarine stairs
BlockRegistry.setDestroyLevelForID(257, 1, true);
BlockRegistry.setDestroyLevelForID(258, 1, true);
BlockRegistry.setDestroyLevelForID(259, 1, true);

BlockRegistry.registerDropFunctionForID(266, function(coords, id, data, level, enchant) { // blue ice
    if (level >= 1 && enchant.silk) {
        return [
            [BlockRegistry.convertBlockToItemId(266), 1, data]
        ];
    }
    return [];
});

BlockRegistry.registerDropFunctionForID(386, function(coords, id, data, level, enchant) { // coral
    if (level >= 1 && enchant.silk) {
        return [
            [BlockRegistry.convertBlockToItemId(386), 1, data]
        ];
    }
    return [];
});

BlockRegistry.registerDropFunctionForID(387, function(coords, id, data, level, enchant) { // coral block
    if (level >= 1) {
        if (enchant.silk) {
            return [
                [-132, 1, data]
            ];
        }
        return [
            [BlockRegistry.convertBlockToItemId(387), 1, data + 8]
        ];
    }
    return [];
});

BlockRegistry.registerDropFunctionForID(388, function(coords, id, data, level, enchant) { // coral fan
    if (level >= 1 && enchant.silk) {
        return [
            [BlockRegistry.convertBlockToItemId(388), 1, data % 8]
        ];
    }
    return [];
});

BlockRegistry.registerDropFunctionForID(389, function(coords, id, data, level, enchant) { // dead coral fan
    if (level >= 1 && enchant.silk) {
        return [
            [BlockRegistry.convertBlockToItemId(389), 1, data % 8]
        ];
    }
    return [];
});

BlockRegistry.registerDropFunctionForID(390, function(coords, id, data, level, enchant) { // wall coral fan
    if (level >= 1 && enchant.silk) {
        var itemID = BlockRegistry.convertBlockToItemId((data % 4 < 2) ? 388 : 389);
        return [
            [itemID, 1, data % 2]
        ];
    }
    return [];
});

BlockRegistry.registerDropFunctionForID(391, function(coords, id, data, level, enchant) { // wall coral fan 2
    if (level >= 1 && enchant.silk) {
        var itemID = BlockRegistry.convertBlockToItemId((data % 4 < 2) ? 388 : 389);
        return [
            [itemID, 1, data % 2 + 2]
        ];
    }
    return [];
});

BlockRegistry.registerDropFunctionForID(392, function(coords, id, data, level, enchant) { // wall coral fan 3
    if (level >= 1 && enchant.silk) {
        var itemID = BlockRegistry.convertBlockToItemId((data % 4 < 2) ? 388 : 389);
        return [
            [itemID, 1, 4]
        ];
    }
    return [];
});

BlockRegistry.registerDropFunctionForID(417, function(coords, id, data, level, enchant) { // stone slab 3
    if (level >= 1) {
        return [
            [-162, 1, data % 8]
        ];
    }
    return [];
}, 1);

BlockRegistry.registerDropFunctionForID(421, function(coords, id, data, level, enchant) { // stone slab 4
    if (level >= 1) {
        return [
            [-166, 1, data % 8]
        ];
    }
    return [];
}, 1);

BlockRegistry.registerDropFunctionForID(422, function(coords, id, data, level, enchant) { // double slab 3
    if (level >= 1) {
        return [
            [-162, 1, data],
            [-162, 1, data]
        ];
    }
    return [];
}, 1);

BlockRegistry.registerDropFunctionForID(423, function(coords, id, data, level, enchant) { // double slab 4
    if (level >= 1) {
        return [
            [-166, 1, data],
            [-166, 1, data]
        ];
    }
    return [];
}, 1);

// stone stairs
BlockRegistry.setDestroyLevelForID(424, 1, true);
BlockRegistry.setDestroyLevelForID(425, 1, true);
BlockRegistry.setDestroyLevelForID(426, 1, true);
BlockRegistry.setDestroyLevelForID(427, 1, true);
BlockRegistry.setDestroyLevelForID(428, 1, true);
BlockRegistry.setDestroyLevelForID(429, 1, true);
BlockRegistry.setDestroyLevelForID(430, 1, true);
BlockRegistry.setDestroyLevelForID(431, 1, true);
BlockRegistry.setDestroyLevelForID(432, 1, true);
BlockRegistry.setDestroyLevelForID(433, 1, true);
BlockRegistry.setDestroyLevelForID(434, 1, true);
BlockRegistry.setDestroyLevelForID(435, 1, true);

BlockRegistry.setDestroyLevelForID(438, 1); // smooth stone
BlockRegistry.setDestroyLevelForID(439, 1, true); // red nether brick stairs
BlockRegistry.setDestroyLevelForID(440, 1, true); // smooth quartz stairs
BlockRegistry.setDestroyLevelForID(450, 1, true); // grindstone
BlockRegistry.setDestroyLevelForID(451, 1, true); // blast furnace
BlockRegistry.setDestroyLevelForID(452, 1, true); // stonecutter
BlockRegistry.setDestroyLevelForID(453, 1, true); // smoker
BlockRegistry.registerDropFunctionForID(454, function(coords, id, data, level) { // lit smoker
    if (level >= 1) {
        return [
            [BlockRegistry.convertBlockToItemId(453), 1, 0]
        ];
    }
    return [];
}, 1);
BlockRegistry.setDestroyLevelForID(461, 1, true); // bell
BlockRegistry.setDestroyLevelForID(463, 1, true); // lantern

BlockRegistry.registerDropFunctionForID(465, function(coords, id, data, level) { // lava cauldron
    if (level >= 1) {
        return [
            [380, 1, 0]
        ]
    }
    return [];
}, 1);

BlockRegistry.registerDropFunctionForID(469, function(coords, id, data, level) { // lit blast furnace
    if (level >= 1) {
        return [
            [BlockRegistry.convertBlockToItemId(451), 1, 0]
        ];
    }
    return [];
}, 1);

BlockRegistry.registerDropFunctionForID(30, function(coords, id, data, level, enchant, item) {
    if(enchant.silk){
        return [[id, 1, 0]];
    } 
    if(level >= 1){
        var toolData = ToolAPI.getToolData(item.id);
        if(toolData && toolData.isWeapon){
            return [[287, 1, 0]];
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
            if (toolData && toolData.modifyEnchant) {
                toolData.modifyEnchant(enchant, item);
            }
            if (ToolAPI.getToolLevelViaBlock(item.id, block.id) > 0 && enchant.silk) {
                var blockSource = BlockSource.getDefaultForActor(player);
                blockSource.destroyBlock(coords.x, coords.y, coords.z);
                blockSource.spawnDroppedItem(coords.x + .5, coords.y + .5, coords.z + .5, block.id, 1, 0, null);
            }
        }
    }
});

