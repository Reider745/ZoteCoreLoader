// THIS DATA IS USED FOR VERSION 1.16

LiquidRegistry.registerLiquid("water", "water", ["_liquid_water_texture_0"]);
LiquidRegistry.registerLiquid("lava", "lava", ["_liquid_lava_texture_0"]);
LiquidRegistry.registerLiquid("milk", "milk", ["_liquid_milk_texture_0"]);

LiquidRegistry.registerItem("water", {
    id: VanillaItemID.bucket,
    data: 0
}, {
    id: VanillaItemID.water_bucket,
    data: 0
});

LiquidRegistry.registerItem("water", {
    id: VanillaItemID.glass_bottle,
    data: 0
}, {
    id: VanillaItemID.potion,
    data: 0
});

LiquidRegistry.registerItem("lava", {
    id: VanillaItemID.bucket,
    data: 0
}, {
    id: VanillaItemID.lava_bucket,
    data: 0
});

LiquidRegistry.registerItem("milk", {
    id: VanillaItemID.bucket,
    data: 0
}, {
    id: VanillaItemID.milk_bucket,
    data: 0
});


LiquidRegistry.registerBlock("water", 8, true);
LiquidRegistry.registerBlock("water", 9);
LiquidRegistry.registerBlock("lava", 10, true);
LiquidRegistry.registerBlock("lava", 11);
