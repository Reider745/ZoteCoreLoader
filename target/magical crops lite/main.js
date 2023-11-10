const BLOCK_TYPE_CROP = Block.createSpecialType({
  base: 59,
  opaque: true,
  rendertype: 6,
  destroytime: 0
}, "crop");


const Crop = {

  data: {},
  isCrop: {},
  crops: {},

  config: {
    drop: __config__.getNumber("DropChance"),
    growth: __config__.getNumber("GrowthChance"),
    severe: __config__.getBool("SevereGrowth.enabled"),
    farmland: __config__.getBool("SevereGrowth.farmland"),
    sametype: __config__.getBool("SevereGrowth.sametype"),
    particle: __config__.getBool("ShowParticle")
  },

  setModel: function(id, data, height){
    const render = new ICRender.Model();
    const model = BlockRenderer.createModel();
    model.addBox(0.5, 0, 0, 0.5, 1, 1, id, data);
    model.addBox(0, 0, 0.5, 1, 1, 0.5, id, data);
    render.addEntry(model);
    BlockRenderer.setStaticICRender(id, data, render);
    BlockRenderer.setCustomCollisionShape(id, data, new ICRender.CollisionShape());
    Block.setShape(id, 0, 0, 0, 1, (height || 26) / 32, 1, data);
  },

  register: function(type, name, tier, material, trans){

    const seed = "seed_" + type;
    IDRegistry.genItemID(seed);
    Item.createItem(seed, name ? name + " Seeds" : "Magical Seeds", {name: seed});

    const ess = "ess_" + type;
    IDRegistry.genItemID(ess);
    Item.createItem(ess, name ? name + " Essence" : "Essence Dust", {name: ess});

    const crop = "crop_" + type;
    IDRegistry.genBlockID(crop);
    Block.createBlock(crop, [{name: "", texture: [[crop, 0]]}], BLOCK_TYPE_CROP);
    this.setModel(BlockID[crop], 0);
    
    Item.addCreativeGroup(type, name, [
    	ItemID[seed],
    	ItemID[ess],
    	BlockID[crop]
    ]);

    Item.registerUseFunction(seed, function(c, item, block, player){
      c.y++;
      let tile;
      let region = BlockSource.getDefaultForActor(player);
      block.id == 60 && c.side == 1 &&
        region.setBlock(c.x, c.y, c.z, BlockID.MagiCro) &
        TileEntity.addTileEntity(c.x, c.y, c.z, region) &
        (tile = World.getTileEntity(c.x, c.y, c.z, region)) &
        tile.container.setSlot("a", item.id, 1, 0) &
        Entity.setCarriedItem(player, item.id, item.count-1, item.data)&
        delete tile.liquidStorage &
        delete tile.data;
    });

    Block.registerDropFunction(crop, function(){
      return [
        [ItemID[ess], 1, 0],
        [ItemID[seed], (Math.random() * 2 + 1) | 0, 0]
      ];
    });
    
    TileEntity.registerPrototype(BlockID[crop], {
    	tick(){
    		let id = this.blockSource.getBlockId(this.x, this.y-1, this.z);
  			if(id != 60){
  				World.removeTileEntity(this.x, this.y, this.z, this.blockSource);
  				this.blockSource.destroyBlock(this.x, this.y, this.z, true);
  			}
    	}
    });

    tier && Recipes.addShaped({id: ItemID["magi_stone" + tier]},
      ["aaa", "aba", "aaa"],
      ["a", ItemID[ess], 0, "b", ItemID["magi_stone" + (tier - 1)], 0],
      function(api){
        for(let i = 9; i--;){
          i != 4 && api.decreaseFieldSlot(i);
        }
      }
    );

    material && Recipes.addShaped({id: ItemID[seed]},
      ["aba", "bcb", "aba"],
      ["a", material.id, material.data || 0, "b", ItemID["magi_ess" + (tier - 1)], 0, "c", ItemID.seed_base, 0],
      material.bucket ? RecFunc.bucket : undefined
    );

    trans && Recipes.addShaped({id: trans.id || material.id, data: trans.data || material.data || 0, count: trans.count},
      {4: ["oao", "aoa", "oao"], 6: ["oao", "aoa", "aaa"], 8: ["aaa", "aoa", "aaa"], 9: ["aaa", "aaa", "aaa"]}[trans.pattern || 8],
      ["a", ItemID[ess], 0],
    RecFunc.basic);

    this.data[ItemID[seed]] = BlockID[crop];
    this.crops[BlockID[crop]] = {
    	seed: ItemID[seed],
    	ess: ItemID[ess]
    };
    this.isCrop[BlockID[crop]] = true;

  },

  getType: function(x, y, z, region){
    if(region.getBlockId(x, y, z) == BlockID.MagiCro){
      const con = World.getContainer(x, y, z, region) || {};
      return con.getSlot("a").id;
    }
    return 0;
  },

  chance: function(x, y, z, region){
    if(!this.config.severe){
      return Math.random() < this.config.growth / 3;
    }
    let point = 0;
    if(this.config.farmland){
      const arr = [[], [], []];
      let block;
      for(let xx = -1; xx <= 1; xx++){
      for(let zz = -1; zz <= 1; zz++){
        block = region.getBlockId(x + xx, y - 1, z + zz);
        block.id  == 60 && (point += block.data == 7 ? !xx && !zz ? 4 : 0.75 : !xx && !zz ? 2 : 0.25);
        this.config.sametype && (arr[xx+1][zz+1] = this.getType(x + xx, y, z + zz, region));
      }
      }
    }
    else{
      point = 10;
    }
    this.config.sametype && (
      arr[1][1] == arr[0][0] ||
      arr[1][1] == arr[0][2] ||
      arr[1][1] == arr[2][0] ||
      arr[1][1] == arr[2][2] ||
      arr[1][1] == arr[0][1] && (arr[1][1] == arr[1][0] || arr[1][1] == arr[1][2]) ||
      arr[1][1] == arr[2][1] && (arr[1][1] == arr[1][0] || arr[1][1] == arr[1][2]) 
    ) && (point /= 2);
    return Math.random() < this.config.growth / -~(25 / point);
  }

};


const RecFunc = {

  stack: false,
  basic: function(api, field, result){
    let count = 1;
    let i = 0;
    if(RecFunc.stack){
      count = 64;
      for(i = 9; i--;){
        field[i].id && (count = Math.min(count, field[i].count));
      }
    }
    result.count *= count;
    for(i = 9; i--;){
      for(let j = count; j--;){
        api.decreaseFieldSlot(i);
      }
    }
  },

  stone: function(api, field, result){
    let count = RecFunc.stack ? Math.min(64, field[1].count, field[3].count, field[5].count, field[7].count) : 1;
    result.count *= count;
    for(; count--;){
      api.decreaseFieldSlot(1);
      api.decreaseFieldSlot(3);
      api.decreaseFieldSlot(5);
      api.decreaseFieldSlot(7);
    }
  },

  exp: function(api, field, result, player){
    let count = 1;
    let i = 0;
    if(RecFunc.stack){
      count = 64;
      for(i = 9; i--;){
        field[i].id && (count = Math.min(count, field[i].count));
      }
    }
    new PlayerActor(player).addExperience(40 * count);
    for(i = 9; i--;){
      for(let j = count; j--;){
        api.decreaseFieldSlot(i);
      }
    }
    result.id = result.count = 0;
  },

  bucket: function(api, field){
    for(let i = 9; i--;){
      !(i & 1) && i != 4 ? field[i].data = 0 : api.decreaseFieldSlot(i);
    }
  }

};

IDRegistry.genItemID("magi_stone0");
Item.createItem("magi_stone0", "Weak Infusion Stone", {name: "magi_stone"}, {stack: 1});
Recipes.addShaped({id: ItemID.magi_stone0}, ["aaa", "aba", "aaa"], ["a", ItemID.ess_base, 0, "b", 264, 0]);
Recipes.addShaped({id: ItemID.magi_stone0}, ["aaa", "aba", "aaa"], ["a", ItemID.ess_base, 0, "b", 388, 0]);

IDRegistry.genItemID("magi_stone1");
Item.createItem("magi_stone1", "§2Regular Infusion Stone", {name: "magi_stone", meta: 1}, {stack: 1});

IDRegistry.genItemID("magi_stone2");
Item.createItem("magi_stone2", "§1Strong Infusion Stone", {name: "magi_stone", meta: 2}, {stack: 1});

IDRegistry.genItemID("magi_stone3");
Item.createItem("magi_stone3", "§5Extreme Infusion Stone", {name: "magi_stone", meta: 3}, {stack: 1});

IDRegistry.genItemID("magi_stone4");
Item.createItem("magi_stone4", "§6Master Infusion Stone", {name: "magi_stone", meta: 4}, {stack: 1});

Callback.addCallback("ItemUse", function(c, item, block){
  let name = "";
  block.id == 58 && (name = IDRegistry.getNameByID(item.id) || "", RecFunc.stack = name.slice(0, -1) == "magi_stone");
});


IDRegistry.genBlockID("MagiCro"); 
Block.createBlock("MagiCro", [
  {name: "", texture: [["MagiCro", 0]]},
  {name: "", texture: [["MagiCro", 1]]},
  {name: "", texture: [["MagiCro", 2]]}
], BLOCK_TYPE_CROP);
Crop.setModel(BlockID.MagiCro, 0, 9);
Crop.setModel(BlockID.MagiCro, 1, 14);
Crop.setModel(BlockID.MagiCro, 2, 18);

TileEntity.registerPrototype(BlockID.MagiCro, {
  growth: function(){
    const data = this.blockSource.getBlockData(this.x, this.y, this.z);
    data == 2 ?
      this.blockSource.setBlock(this.x, this.y, this.z, Crop.data[this.container.getSlot("a").id]) &
      this.container.clearSlot("a") &
      World.removeTileEntity(this.x, this.y, this.z, this.blockSource) & TileEntity.addTileEntity(this.x, this.y, this.z, this.blockSource)  :
      this.blockSource.setBlock(this.x, this.y, this.z, BlockID.MagiCro, data + 1);
  },
  click: function(id, count, data, pos, player){
    id == ItemID.magi_fertilizer &&
      this.blockSource.setBlock(this.x, this.y, this.z, Crop.data[this.container.getSlot("a").id]) &
      this.container.clearSlot("a") &
      World.removeTileEntity(this.x, this.y, this.z, this.blockSource) &
      Entity.setCarriedItem(player, id, count-1, data);
  },
  tick(){
  	let id = this.blockSource.getBlockId(this.x, this.y-1, this.z);
  	if(id != 60){
  		World.removeTileEntity(this.x, this.y, this.z, this.blockSource);
  		this.blockSource.destroyBlock(this.x, this.y, this.z, true);
  	}
  }
});

Block.setRandomTickCallback(BlockID.MagiCro, function(x, y, z, id, data, region){
	const tile = World.getTileEntity(x, y, z, region);
  region.getBlockId(x, y - 1, z) == 60 ?
    tile && region.getLightLevel(x, y, z) > 8 && Crop.chance(x, y, z, region) && tile.growth() :
    World.removeTileEntity(x, y, z, region) & region.setBlock(x, y, z, 0);
});

Crop.config.particle && Block.setAnimateTickCallback(BlockID.MagiCro, function(x, y, z, id, data){
  for(let i = Math.random() * 4 + 1 | 0; i--;){
    Particles.addParticle(34, x + Math.random(), y + Math.random() + 0.5, z + Math.random(), 0, -0.05, 0);
  }
});

Block.registerDropFunction("MagiCro", function(c){
  World.removeTileEntity(c.x, c.y, c.z);
  return [];
});

Callback.addCallback("DestroyBlock", function(c, block, player){
	let region = BlockSource.getDefaultForActor(player);
	block.id == 31 && Math.random() < Crop.config.drop && region.spawnDroppedItem(c.x + 0.5, c.y, c.z + 0.5, Crop.crops[block.id].seed, 1);
  c.y++;
  const id = region.getBlockId(c.x, c.y, c.z);
  Crop.isCrop[id] && region.destroyBlock(c.x, c.y, c.z, true);
  id == BlockID.MagiCro && World.removeTileEntity(c.x, c.y, c.z, region) & region.setBlock(c.x, c.y, c.z, 0);
});


IDRegistry.genItemID("magi_ess0");
Item.createItem("magi_ess0", "§2Weak Essence", {name: "magi_ess"});

IDRegistry.genItemID("magi_ess1");
Item.createItem("magi_ess1", "§1Regular Essence", {name: "magi_ess", meta: 1});

IDRegistry.genItemID("magi_ess2");
Item.createItem("magi_ess2", "§5Strong Essence", {name: "magi_ess", meta: 2});

IDRegistry.genItemID("magi_ess3");
Item.createItem("magi_ess3", "§6Extreme Essence", {name: "magi_ess", meta: 3});

Crop.register("base");
Recipes.addShapeless({id: ItemID.ess_base}, [{id: ItemID.seed_base}]);
Recipes.addShaped({id: ItemID.seed_base}, ["aaa", "aba", "aaa"], ["a", ItemID.ess_base, 0, "b", 295, 0]);
Recipes.addShaped({id: 48, count: 8}, ["aaa", "aba", "aaa"], ["a", 4, 0, "b", ItemID.ess_base, 0]);
Recipes.addShaped({id: 98, data: 1, count: 8}, ["aaa", "aba", "aaa"], ["a", 98, -1, "b", ItemID.ess_base, 0]);
Recipes.addShaped({id: 81, count: 3}, ["aa", "aa", "aa"], ["a", ItemID.ess_base, 0]);
Recipes.addShaped({id: 106, count: 16}, ["aoa", "aaa", "aoa"], ["a", ItemID.ess_base, 0]);
Recipes.addShaped({id: 111, count: 8}, ["oao", "aaa", "aoa"], ["a", ItemID.ess_base, 0]);
Recipes.addShaped({id: 110, count: 8}, ["aba", "bcb", "aba"], ["a", 3, 0, "b", ItemID.ess_base, 0, "c", 39, 0]);
Recipes.addShaped({id: 110, count: 8}, ["aba", "bcb", "aba"], ["a", 3, 0, "b", ItemID.ess_base, 0, "c", 40, 0]);


(function(){
  let id = 0;
  for(let i = 4; i--;){
    id = ItemID[i ? "magi_ess" + (i - 1) : "ess_base"];
    Recipes.addShaped({id: ItemID["magi_ess" + i]}, ["oao", "aba", "oao"], ["a", id, 0, "b", ItemID["magi_stone" + i], 0], RecFunc.stone);
    Recipes.addShaped({id: ItemID["magi_ess" + i]}, ["oao", "aba", "oao"], ["a", id, 0, "b", ItemID["magi_stone4"], 0], RecFunc.stone);
    Recipes.addShapeless({id: id, count: 4}, [{id: ItemID["magi_ess" + i]}]);
  }
})();



Crop.register("water", "§1Water", 1, {id: 325, data: 8, bucket: true});
Recipes.addShaped({id: 325, data: 8}, ["oao", "aba", "oao"], ["a", ItemID.ess_water, 0, "b", 325, 0]);

Crop.register("fire", "§eFire", 1, {id: 325, data: 10, bucket: true});
Recipes.addShaped({id: 325, data: 10}, ["oao", "aba", "oao"], ["a", ItemID.ess_fire, 0, "b", 325, 0]);

Crop.register("earth", "§2Earth", 1, {id: 3});
Recipes.addShaped({id: 3, count: 32}, ["aa", "aa"], ["a", ItemID.ess_earth, 0]);

Crop.register("air", "§5Air", 1, {id: 374});
Recipes.addShaped({id: 288, count: 6}, ["aoo", "oao", "ooa"], ["a", ItemID.ess_earth, 0]);

Recipes.addShaped({id: 4, count: 32}, ["ab", "ba"], ["a", ItemID.ess_fire, 0, "b", ItemID.ess_earth, 0]);
Recipes.addShaped({id: 12, count: 16}, ["ab", "ba"], ["a", ItemID.ess_earth, 0, "b", ItemID.ess_fire, 0]);
Recipes.addShaped({id: 18, count: 8}, ["ab", "bb"], ["a", ItemID.ess_water, 0, "b", ItemID.ess_earth, 0]);
Recipes.addShaped({id: 18, data: 1, count: 8}, ["ba", "bb"], ["a", ItemID.ess_water, 0, "b", ItemID.ess_earth, 0]);
Recipes.addShaped({id: 18, data: 2, count: 8}, ["bb", "ab"], ["a", ItemID.ess_water, 0, "b", ItemID.ess_earth, 0]);
Recipes.addShaped({id: 18, data: 3, count: 8}, ["bb", "ba"], ["a", ItemID.ess_water, 0, "b", ItemID.ess_earth, 0]);
Recipes.addShaped({id: 332, count: 32}, ["ab", "ba"], ["a", ItemID.ess_water, 0, "b", ItemID.ess_air, 0]);
Recipes.addShaped({id: 337, count: 32}, ["ab", "ba"], ["a", ItemID.ess_water, 0, "b", ItemID.ess_earth, 0]);


Crop.register("coal", "§8Coal", 1, {id: 263}, {count: 12});

Crop.register("dye", "§4Dye§a", 1, {id: 351, data: -1});
Recipes.addShaped({id: 351, count: 6}, ["aaa", "ooo", "ooo"], ["a", ItemID.ess_dye, 0], RecFunc.basic);
Recipes.addShaped({id: 351, data: 1, count: 6}, ["ooo", "aaa", "ooo"], ["a", ItemID.ess_dye, 0], RecFunc.basic);
Recipes.addShaped({id: 351, data: 2, count: 6}, ["ooo", "ooo", "aaa"], ["a", ItemID.ess_dye, 0], RecFunc.basic);
Recipes.addShaped({id: 351, data: 5, count: 6}, ["oao", "oao", "oao"], ["a", ItemID.ess_dye, 0], RecFunc.basic);
Recipes.addShaped({id: 351, data: 6, count: 6}, ["aoo", "aoo", "aoo"], ["a", ItemID.ess_dye, 0], RecFunc.basic);
Recipes.addShaped({id: 351, data: 7, count: 6}, ["aoo", "oao", "ooa"], ["a", ItemID.ess_dye, 0], RecFunc.basic);
Recipes.addShaped({id: 351, data: 8, count: 6}, ["oao", "oao", "aoo"], ["a", ItemID.ess_dye, 0], RecFunc.basic);
Recipes.addShaped({id: 351, data: 9, count: 6}, ["aa", "ao"], ["a", ItemID.ess_dye, 0], RecFunc.basic);
Recipes.addShaped({id: 351, data: 10, count: 6}, ["oaa", "aoo"], ["a", ItemID.ess_dye, 0], RecFunc.basic);
Recipes.addShaped({id: 351, data: 11, count: 6}, ["oao", "aoa"], ["a", ItemID.ess_dye, 0], RecFunc.basic);
Recipes.addShaped({id: 351, data: 12, count: 6}, ["aa", "oa"], ["a", ItemID.ess_dye, 0], RecFunc.basic);
Recipes.addShaped({id: 351, data: 13, count: 6}, ["oa", "ao ", "oa"], ["a", ItemID.ess_dye, 0], RecFunc.basic);
Recipes.addShaped({id: 351, data: 14, count: 6}, ["oa", "ao", "ao"], ["a", ItemID.ess_dye, 0], RecFunc.basic);

Crop.register("obsidian", "§5Obsidian", 2, {id: 49}, {count: 8});

Crop.register("red", "§4Redstone", 2, {id: 331}, {count: 24});

Crop.register("glow", "§eGlowstone", 2, {id: 348}, {count: 12});

Crop.register("iron", "§8Iron", 3, {id: 265}, {id: 15, count: 8});

Crop.register("gold", "§6Gold", 3, {id: 266}, {id: 14, count: 5});

Crop.register("lapis", "§1Lapis Lazuli", 3, {id: 351, data: 4}, {count: 12});

Crop.register("nether", "§4Nether", 3, {id: 112});
Recipes.addShaped({id: 87, count: 16}, ["aa"], ["a", ItemID.ess_nether, 0], RecFunc.basic);
Recipes.addShaped({id: 88, count: 8}, ["aaa"], ["a", ItemID.ess_nether, 0], RecFunc.basic);
Recipes.addShaped({id: 112, count: 16}, ["aa", "aa"], ["a", ItemID.ess_nether, 0], RecFunc.basic);
Recipes.addShaped({id: 372, count: 12}, ["aoa", "oao", "aoa"], ["a", ItemID.ess_nether, 0], RecFunc.basic);
Recipes.addShaped({id: 406, count: 8}, ["oao", "aaa", "oao"], ["a", ItemID.ess_nether, 0], RecFunc.basic);

Crop.register("cow", "Cow", 3, {id: 334});
Recipes.addShaped({id: 334, count: 16}, ["aaa", "aaa", "aoa"], ["a", ItemID.ess_cow, 0], RecFunc.basic);
Recipes.addShaped({id: 363, count: 6}, ["a", "a"], ["a", ItemID.ess_cow, 0], RecFunc.basic);
Recipes.addShaped({id: 325, data: 1}, ["oao", "aba", "oao"], ["a", ItemID.ess_cow, 0, "b", 325, 0]);
Recipes.addShaped({id: 384, count: 3}, ["aaa", "aaa", "aaa"], ["a", ItemID.ess_cow, 0], RecFunc.basic);

Crop.register("spider", "Spider", 3, {id: 375}, {pattern: 4, count: 8});
Recipes.addShaped({id: 287, count: 8}, ["ooa", "oao", "aoo"], ["a", ItemID.ess_spider, 0], RecFunc.basic);
Recipes.addShaped({id: 384, count: 3}, ["aaa", "aaa", "aaa"], ["a", ItemID.ess_spider, 0], RecFunc.basic);


Crop.register("skeleton", "Skeleton", 3, {id: 352});
Recipes.addShaped({id: 262, count: 12}, ["a", "a", "a"], ["a", ItemID.ess_skeleton, 0], RecFunc.basic);
Recipes.addShaped({id: 352, count: 10}, ["ooa", "oao", "aoo"], ["a", ItemID.ess_skeleton, 0], RecFunc.basic);
Recipes.addShaped({id: 384, count: 3}, ["aaa", "aaa", "aaa"], ["a", ItemID.ess_skeleton, 0], RecFunc.basic);
Recipes.addShaped({id: 397}, ["aa", "aa"], ["a", ItemID.ess_skeleton, 0], RecFunc.basic);

Crop.register("creeper", "Creeper", 3, {id: 289});
Recipes.addShaped({id: 289, count: 12}, ["aoa", "ooo", "aoa"], ["a", ItemID.ess_creeper, 0], RecFunc.basic);
Recipes.addShaped({id: 384, count: 3}, ["aaa", "aaa", "aaa"], ["a", ItemID.ess_creeper, 0], RecFunc.basic);
Recipes.addShaped({id: 397, count: 4}, ["aa", "aa"], ["a", ItemID.ess_creeper, 0], RecFunc.basic);

Recipes.addShaped({id: 500}, ["aba", "bcb", "aba"], ["a", ItemID.ess_skeleton, 0, "b", ItemID.ess_creeper, 0, "c", 351, 11]);
Recipes.addShaped({id: 501}, ["aba", "bcb", "aba"], ["a", ItemID.ess_skeleton, 0, "b", ItemID.ess_creeper, 0, "c", 351, 2]);
Recipes.addShaped({id: 502}, ["aba", "bcb", "aba"], ["a", ItemID.ess_skeleton, 0, "b", ItemID.ess_creeper, 0, "c", 351, 14]);
Recipes.addShaped({id: 503}, ["aba", "bcb", "aba"], ["a", ItemID.ess_skeleton, 0, "b", ItemID.ess_creeper, 0, "c", 351, 1]);
Recipes.addShaped({id: 504}, ["aba", "bcb", "aba"], ["a", ItemID.ess_skeleton, 0, "b", ItemID.ess_creeper, 0, "c", 351, 10]);
Recipes.addShaped({id: 505}, ["aba", "bcb", "aba"], ["a", ItemID.ess_skeleton, 0, "b", ItemID.ess_creeper, 0, "c", 351, 5]);
Recipes.addShaped({id: 506}, ["aba", "bcb", "aba"], ["a", ItemID.ess_skeleton, 0, "b", ItemID.ess_creeper, 0, "c", 351, 9]);
Recipes.addShaped({id: 507}, ["aba", "bcb", "aba"], ["a", ItemID.ess_skeleton, 0, "b", ItemID.ess_creeper, 0, "c", 351, 0]);
Recipes.addShaped({id: 508}, ["aba", "bcb", "aba"], ["a", ItemID.ess_skeleton, 0, "b", ItemID.ess_creeper, 0, "c", 351, 15]);
Recipes.addShaped({id: 509}, ["aba", "bcb", "aba"], ["a", ItemID.ess_skeleton, 0, "b", ItemID.ess_creeper, 0, "c", 351, 6]);
Recipes.addShaped({id: 510}, ["aba", "bcb", "aba"], ["a", ItemID.ess_skeleton, 0, "b", ItemID.ess_creeper, 0, "c", 351, 8]);
Recipes.addShaped({id: 511}, ["aba", "bcb", "aba"], ["a", ItemID.ess_skeleton, 0, "b", ItemID.ess_creeper, 0, "c", 351, 4]);


Crop.register("ender", "§aEnder", 3, {id: 368}, {count: 4});
Recipes.addShaped({id: 120}, ["aaa", "aaa", "aaa"], ["a", ItemID.ess_ender, 0], RecFunc.basic);
Recipes.addShaped({id: 121, count: 8}, ["aa", "aa"], ["a", ItemID.ess_ender, 0], RecFunc.basic);

Crop.register("slime", "Slime", 3, {id: 341}, {pattern: 4, count: 12});
Recipes.addShaped({id: 384, count: 3}, ["aaa", "aaa", "aaa"], ["a", ItemID.ess_slime, 0], RecFunc.basic);

Crop.register("blaze", "§eBlaze", 3, {id: 369}, {count: 3});

Crop.register("magma", "Magma", 3, {id: 378}, {pattern: 4, count: 6});
Recipes.addShaped({id: 384, count: 3}, ["aaa", "aaa", "aaa"], ["a", ItemID.ess_magma, 0], RecFunc.basic);

Crop.register("ghast", "Ghast", 3, {id: 370}, {pattern: 6, count: 6});
Recipes.addShaped({id: 384, count: 3}, ["aaa", "aaa", "aaa"], ["a", ItemID.ess_ghast, 0], RecFunc.basic);

Crop.register("xp", "§2EXP", 3);
Recipes.addShaped({id: ItemID.seed_xp}, ["abc", "bdb", "ebf"], ["a", 370, 0, "b", ItemID.magi_ess3, 0, "c", 368, 0, "d", ItemID.seed_base, 0, "e", 378, 0, "f", 369, 0]);
Recipes.addShaped({id: 384}, ["aaa", "aoa", "aaa"], ["a", ItemID.ess_xp, 0], RecFunc.exp);

Crop.register("dia", "§9Diamond", 4, {id: 264}, {count: 1});

Crop.register("emerald", "§aEmerald", 4, {id: 388}, {count: 1});

Crop.register("wither", "Wither Skeleton", 4, {id: 397, data: 1}, {count: 1});
Recipes.addShaped({id: 384, count: 3}, ["aaa", "aaa", "aaa"], ["a", ItemID.ess_wither, 0], RecFunc.basic);


IDRegistry.genItemID("magi_fertilizer");
Item.createItem("magi_fertilizer", "Magical Fertilizer", {name: "magi_fertilizer"});
Item.setGlint(ItemID.magi_fertilizer, true);
Recipes.addShaped({id: ItemID.magi_fertilizer, count: 4}, ["aba", "bcb", "aba"], ["a", ItemID.ess_base, 0, "b", 351, 15, "c", 264, 0]);


IDRegistry.genItemID("magi_coal");
Item.createItem("magi_coal", "Infused Coal", {name: "magi_coal"}, {isTech: true});
Recipes.addFurnaceFuel(ItemID.magi_coal, 0, 6400);
Recipes.addFurnaceFuel(ItemID.magi_coal, 1, 12800);
Recipes.addFurnaceFuel(ItemID.magi_coal, 2, 25600);
Recipes.addFurnaceFuel(ItemID.magi_coal, 3, 51200);
Recipes.addFurnaceFuel(ItemID.magi_coal, 4, 102400);

Item.registerNameOverrideFunction(ItemID.magi_coal, function(item, name){
  return ["", "§2", "§1", "§5", "§6"][item.data] + name;
});

(function(){
  let id = 0;
  for(let i = 5; i--;){
    id = ItemID[i ? "magi_ess" + (i - 1) : "ess_base"];
    Recipes.addShapeless({id: ItemID.magi_coal, data: i}, [{id: 263}, {id: id}, {id: id}]);
  }
})();



ModAPI.addAPICallback("ICore", function(){
  Crop.register("copper", "Copper", 2, {id: ItemID.ingotCopper}, {id: BlockID.oreCopper, count: 6});
  Crop.register("tin", "Tin", 2, {id: ItemID.ingotTin}, {id: BlockID.oreTin, count: 6});
  Crop.register("rubber", "Rubber", 2, {id: ItemID.rubber}, {count: 8});
  Crop.register("sulfur", "Sulfur", 2, {id: ItemID.dustSulfur}, {count: 12});
  Crop.register("lead", "Lead", 3, {id: ItemID.ingotLead}, {id: BlockID.oreLead, count: 4});
//  Crop.register("silver", "Silver", 3, {id: ItemID.ingotSilver}, {count: 12});
  Crop.register("uran", "Uranium", 4, {id: BlockID.oreUranium}, {count: 2});
  Crop.register("iridium", "Iridium", 4, {id: ItemID.iridiumChunk}, {pattern: 9, count: 1});
});