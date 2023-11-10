package com.reider745.network.transaction;

import cn.nukkit.Player;
import cn.nukkit.event.inventory.InventoryTransactionEvent;
import cn.nukkit.inventory.Inventory;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author geNAZt
 * @version 1.0
 */
public class TransactionGroup {

    private static final Logger LOGGER = LoggerFactory.getLogger( TransactionGroup.class );

    private final Player player;
    private final List<Transaction<?, ?, ?>> transactions = new ArrayList<>();

    // Need / have for this transactions
    private List<Item> haveItems = new ArrayList<>();
    private List<Item> needItems = new ArrayList<>();

    // Matched
    private boolean matchItems;

    public TransactionGroup(Player player) {
        this.player = player;
    }

    public List<Transaction<?, ?, ?>> getTransactions() {
        return this.transactions;
    }

    /**
     * Add a new transaction to this group
     *
     * @param transaction The transaction which should be added
     */
    public void addTransaction( Transaction<?, ?, ?> transaction ) {
        // Check if not already added
        if ( this.transactions.contains( transaction ) ) {
            return;
        }

        // Add this transaction and also the inventory
        this.transactions.add( transaction );
    }

    private void calcMatchItems() {
        // Clear both sides for a fresh compare
        this.haveItems.clear();
        this.needItems.clear();


        // Check all transactions for needed and having items
        for ( Transaction<?, ?, ?> ts : this.transactions ) {
            if ( !( ts.targetItem().getId() == 0) ) {
                this.needItems.add( ( (Item) ts.targetItem() ).clone() );
            }

            Item sourceItem = ts.sourceItem() != null ? ( (Item) ts.sourceItem() ).clone() : null;
            if ( ts.hasInventory() && sourceItem != null ) {
                Item checkSourceItem = ts.inventory().getItem( ts.slot() );

                // Check if source inventory changed during transaction
                if ( !checkSourceItem.equals( sourceItem ) || sourceItem.getCount() != checkSourceItem.getCount()) {
                    this.matchItems = false;
                    return;
                }
            }

            if ( sourceItem != null && !( sourceItem.getId() == 0) ) {
                this.haveItems.add( sourceItem );
            }
        }

        // Now check if we have items left which are needed
        for ( Item needItem : new ArrayList<>( this.needItems ) ) {
            for ( Item haveItem : new ArrayList<>( this.haveItems ) ) {
                if ( needItem.equals( haveItem ) ) {
                    int amount = Math.min( haveItem.getCount(), needItem.getCount() );
                    needItem.setCount( needItem.getCount() - amount );
                    haveItem.setCount( haveItem.getCount() - amount );

                    if ( haveItem.getCount() == 0 ) {
                        this.haveItems.remove( haveItem );
                    }

                    if ( needItem.getCount() == 0 ) {
                        this.needItems.remove( needItem );
                        break;
                    }
                }
            }
        }

        this.matchItems = true;
    }

    private void mergeTransactions() {
        Map<Inventory, Map<Integer, List<Transaction<?, ?, ?>>>> mergedTransactions = new HashMap<>();

        for ( Transaction<?, ?, ?> transaction : this.transactions ) {
            if ( transaction.hasInventory() ) {
                Map<Integer, List<Transaction<?, ?, ?>>> slotTransactions = mergedTransactions.computeIfAbsent( transaction.inventory(), inventory -> new HashMap<>() );
                slotTransactions.computeIfAbsent( transaction.slot(), integer -> new ArrayList<>() ).add( transaction );
            }
        }

        for ( Map.Entry<Inventory, Map<Integer, List<Transaction<?, ?, ?>>>> inventoryMapEntry : mergedTransactions.entrySet() ) {
            for ( Map.Entry<Integer, List<Transaction<?, ?, ?>>> slotEntry : inventoryMapEntry.getValue().entrySet() ) {
                if ( slotEntry.getValue().size() > 1 ) {
                    LOGGER.debug( "Merging slot {} for inventory {}", slotEntry.getKey(), inventoryMapEntry.getKey() );

                    List<Transaction<?, ?, ?>> transactions = slotEntry.getValue();
                    List<Transaction<?, ?, ?>> original = new ArrayList<>( transactions );
                    Item lastTargetItem = null;
                    InventoryTransaction<?, ?, ?> startTransaction = null;

                    for ( int i = 0; i < transactions.size(); i++ ) {
                        Transaction<?, ?, ?> ts = transactions.get( i );

                        Item sourceItem = ts.sourceItem() != null ? (  ts.sourceItem() ).clone() : null;
                        if ( ts.hasInventory() && sourceItem != null ) {
                            Item checkSourceItem = ts.inventory().getItem( ts.slot() );

                            // Check if source inventory changed during transaction
                            if ( checkSourceItem.equals( sourceItem ) && sourceItem.getCount() == checkSourceItem.getCount() ) {
                                transactions.remove( i );
                                startTransaction = (InventoryTransaction<?, ?, ?>) ts;
                                lastTargetItem = ts.targetItem();
                                break;
                            }
                        }
                    }

                    if ( startTransaction == null ) {
                        return;
                    }

                    int sortedThisLoop;

                    do {
                        sortedThisLoop = 0;
                        for ( int i = 0; i < transactions.size(); i++ ) {
                            Transaction<?, ?, ?> ts = transactions.get( i );

                            Item actionSource = ts.sourceItem();
                            if ( actionSource.equals( lastTargetItem ) && actionSource.getCount() == lastTargetItem.getCount() ) {
                                lastTargetItem = ts.targetItem();
                                transactions.remove( i );
                                sortedThisLoop++;
                            } else if ( actionSource.equals( lastTargetItem ) ) {
                                lastTargetItem.setCount( lastTargetItem.getCount() - actionSource.getCount() );
                                transactions.remove( i );
                                if ( lastTargetItem.getCount() == 0 ) {
                                    sortedThisLoop++;
                                }
                            }
                        }
                    } while ( sortedThisLoop > 0 );

                    if ( !transactions.isEmpty() ) {
                        LOGGER.debug( "Failed to compact {} actions", original.size() );
                        return;
                    }

                    for ( Transaction<?, ?, ?> transaction : original ) {
                        this.transactions.remove( transaction );
                    }

                    this.transactions.add( new InventoryTransaction<>( startTransaction.getOwner(), startTransaction.inventory(),
                            startTransaction.slot(), startTransaction.sourceItem(), lastTargetItem, startTransaction.getInventoryWindowId() ) );
                    LOGGER.debug( "Successfully compacted {} actions", original.size() );
                }
            }
        }
    }

    /**
     * Check if transaction is complete and can be executed
     *
     * @return true if the transaction is complete and can be executed
     */
    private boolean canExecute() {
        this.mergeTransactions();
        this.calcMatchItems();

        boolean matched = this.matchItems && this.haveItems.isEmpty() && this.needItems.isEmpty() && !this.transactions.isEmpty();
        if ( matched ) {
            List<Transaction<?, ?, ?>> transactionList = new ArrayList<>( this.transactions );
            InventoryTransactionEvent transactionEvent = new InventoryTransactionEvent( null);
            this.player.getLevel().getServer().getPluginManager().callEvent( transactionEvent );
            return !transactionEvent.isCancelled();
        }

        return false;
    }

    /**
     * Try to execute the transaction
     *
     * @param forceExecute to force execution (like creative mode does)
     */
    public boolean execute( boolean forceExecute ) {
        if ( this.canExecute() || forceExecute ) {
            for ( Transaction<?, ?, ?> transaction : this.transactions ) {
                transaction.commit();
            }

            return true;
        } else {
            for ( Transaction<?, ?, ?> transaction : this.transactions ) {
                transaction.revert();
            }
        }

        return false;
    }

    @Override
    public String toString() {
        return "{\"_class\":\"TransactionGroup\", " +
                "\"player\":" + (this.player == null ? "null" : this.player) + ", " +
                "\"transactions\":" + (this.transactions == null ? "null" : Arrays.toString(this.transactions.toArray())) + ", " +
                "\"haveItems\":" + (this.haveItems == null ? "null" : Arrays.toString(this.transactions.toArray())) + ", " +
                "\"needItems\":" + (this.needItems == null ? "null" : Arrays.toString(this.transactions.toArray())) + ", " +
                "\"matchItems\":\"" + this.matchItems + "\"" +
                "}";
    }
}
