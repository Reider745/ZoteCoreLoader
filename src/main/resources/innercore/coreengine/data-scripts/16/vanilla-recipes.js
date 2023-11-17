(function addVanillaRecipes() {
    Logger.debug("CoreEngine", "Adding vanilla recipes");

    // workbench
    Recipes.addShapedVanilla({id: 58, count: 1, data: 0}, [
        "##",
        "##",
    ], ['#', 5, -1]);

    // chest
    Recipes.addShapedVanilla({id: 54, count: 1, data: 0}, [
        "###",
        "# #",
        "###"
    ], ['#', 5, -1]);

    // bed
    for (let data = 0; data < 16; data++) {
        Recipes.addShapedVanilla({id: 355, count: 1, data: data}, [
            "AAA",
            "###"
        ], ['A', 35, data, '#', 5, -1]);
    }

    // piston
    Recipes.addShapedVanilla({id: 33, count: 1, data: 0}, [
        "###",
        "CAC",
        "CBC"
    ], ['#', 5, -1, "A", 265, 0, "B", 331, 0, "C", 4, 0]);

    // stick
    Recipes.addShapedVanilla({id: 280, count: 4, data: 0}, [
        "#",
        "#"
    ], ['#', 5, -1]);

    // wooden axe
    Recipes.addShapedVanilla({id: 271, count: 1, data: 0}, [
        " AA",
        " #A",
        " # "
    ], ['#', 280, 0, 'A', 5, -1]);

    // wooden hoe
    Recipes.addShapedVanilla({id: 290, count: 1, data: 0}, [
        " AA",
        " # ",
        " # "
    ], ['#', 280, 0, 'A', 5, -1]);

    // wooden pickaxe
    Recipes.addShapedVanilla({id: 270, count: 1, data: 0}, [
        "AAA",
        " # ",
        " # "
    ], ['#', 280, 0, 'A', 5, -1]);

    // wooden shovel
    Recipes.addShapedVanilla({id: 269, count: 1, data: 0}, [
        " A",
        " #",
        " #"
    ], ['#', 280, 0, 'A', 5, -1]);

    // wooden sword
    Recipes.addShapedVanilla({id: 268, count: 1, data: 0}, [
        " A",
        " A",
        " #"
    ], ['#', 280, 0, 'A', 5, -1]);

    // oak trapdoor
    Recipes.addShapedVanilla({id: 96, count: 1, data: 0}, [
        "###",
        "###"
    ], ['#', 5, 0]);

    // spruce trapdoor
    Recipes.addShapedVanilla({id: -149, count: 1, data: 0}, [
        "###",
        "###"
    ], ['#', 5, 1]);

    // birch trapdoor
    Recipes.addShapedVanilla({id: -146, count: 1, data: 0}, [
        "###",
        "###"
    ], ['#', 5, 2]);

    // jungle trapdoor
    Recipes.addShapedVanilla({id: -148, count: 1, data: 0}, [
        "###",
        "###"
    ], ['#', 5, 3]);

    // acacia trapdoor
    Recipes.addShapedVanilla({id: -145, count: 1, data: 0}, [
        "###",
        "###"
    ], ['#', 5, 4]);

    // dark_oak trapdoor
    Recipes.addShapedVanilla({id: -147, count: 1, data: 0}, [
        "###",
        "###"
    ], ['#', 5, 5]);

    // bowl
    Recipes.addShapedVanilla({id: 281, count: 4, data: 0}, [
        "# #",
        " # "
    ], ['#', 5, -1]);

    // cobblestone slab
    Recipes.addShapedVanilla({id: 44, count: 6, data: 3}, [
        "###",
    ], ['#', 4, 0]);

    // torch
    Recipes.addShapedVanilla({id: 50, count: 4, data: 0}, [
        "A",
        "B"
    ], ['A', 263, -1, 'B', 280, -1]); // coal

    Recipes.addShapedVanilla({id: 50, count: 4, data: 0}, [
        "A",
        "B"
    ], ['A', 866, -1, 'B', 280, -1]); // charcoal


    // ----------------------------------

}) ()