/*
 * Copyright (c) 2020 Gomint team
 *
 * This code is licensed under the BSD license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.reider745.network.session;

import cn.nukkit.Player;
import cn.nukkit.inventory.CraftingRecipe;
import cn.nukkit.inventory.Inventory;
import cn.nukkit.inventory.Recipe;
import cn.nukkit.item.Item;
import com.reider745.InnerCoreServer;
import com.reider745.network.request.InventoryCraftingResultAction;

import java.util.ArrayList;
import java.util.Collection;

public class CraftingSession implements Session {

    private final Player connection;
    private final Inventory inputInventory;
    private final Inventory outputInventory;

    private CraftingRecipe recipe;
    private byte amount;
    private InventoryCraftingResultAction crafting;

    public CraftingSession(Player connection) {
        this.connection = connection;

        // Check which input size we currently have
        this.inputInventory = new SessionInventory(connection, connection.getCraftingGrid().getSize());
        this.outputInventory = connection.getInventory();
    }

    public CraftingSession setCraftingResult(InventoryCraftingResultAction crafting) {
        this.crafting = crafting;
        return this;
    }

    public CraftingSession findRecipe(int recipeId) {
        Collection<Recipe> collection = this.connection.getServer().getCraftingManager().getRecipes(InnerCoreServer.PROTOCOL);
        var it = collection.iterator();
        int i = 0;
        while (it.hasNext()){
            Recipe recipe = it.next();
            if(i == recipeId){
                if(!(recipe instanceof CraftingRecipe))
                    return this;
                this.recipe = (CraftingRecipe) recipe;
            }
            i++;
        }
        return this;
    }

    public void setAmountOfCrafts(byte amount) {
        this.amount = amount;
    }

    @Override
    public void addInput(Item item, int slot) {
        this.inputInventory.addItem(item);
    }

    @Override
    public void postProcess() {

    }

    @Override
    public boolean process() {
        /*// Generate a output stack for compare
        Collection<Item> output = this.recipe.getAllResults();

        // Craft the amount wanted
        for (byte i = 0; i < this.amount; i++) {
            // Let the recipe check if it can complete
            int[] consumeSlots = this.recipe.(this.inputInventory);
            boolean craftable = consumeSlots != null;
            if (!craftable) {
                return false;
            }

            PlayerCraftingEvent event = new PlayerCraftingEvent(this.connection.entity(), this.recipe);
            this.connection.entity().world().server().pluginManager().callEvent(event);

            if (event.cancelled()) {
                return false;
            }

            // We can craft this
            for (io.gomint.inventory.item.ItemStack<?> itemStack : output) {
                if (!this.outputInventory.hasPlaceFor(itemStack)) {
                    return false;
                }
            }

            // Consume items
            for (int slot : consumeSlots) {
                io.gomint.server.inventory.item.ItemStack<?> itemStack = (io.gomint.server.inventory.item.ItemStack<?>) this.inputInventory.item(slot);
                itemStack.afterPlacement();
            }

            // We can craft this
            for (io.gomint.inventory.item.ItemStack<?> itemStack : output) {
                this.outputInventory.addItem(itemStack);
            }
        }*/

        ArrayList<Item> resultCrafting = crafting.getResultItems();
        for(Item item : resultCrafting){
            item.setCount(crafting.getAmount());

            Item[] drops = this.outputInventory.addItem(item);
            for (Item drop : drops)
                connection.dropItem(drop);
        }

        return true;
    }

    @Override
    public Inventory getOutput() {
        return this.outputInventory;
    }

}
