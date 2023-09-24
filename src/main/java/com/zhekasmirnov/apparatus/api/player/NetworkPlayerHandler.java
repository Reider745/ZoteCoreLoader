package com.zhekasmirnov.apparatus.api.player;

//import com.zhekasmirnov.apparatus.adapter.innercore.UserDialog;
import com.zhekasmirnov.apparatus.adapter.innercore.game.entity.EntityActor;
import com.zhekasmirnov.apparatus.api.player.armor.ActorArmorHandler;
import com.zhekasmirnov.apparatus.job.JobExecutor;
import com.zhekasmirnov.apparatus.multiplayer.Network;
import com.zhekasmirnov.apparatus.multiplayer.server.ConnectedClient;
import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.api.runtime.Callback;

public class NetworkPlayerHandler {
    private final boolean isLocal;
    private final long playerUid;
    private final EntityActor actor;
    private final ActorArmorHandler armorHandler;

    private final JobExecutor instantExecutor = Network.getSingleton().getInstantJobExecutor();

    private boolean isInitialized = false;
    private boolean isTickCallbackDisabled = false;
    private int dimensionId = 0;


    public NetworkPlayerHandler(long playerUid, boolean isLocal) {
        this.isLocal = isLocal;
        this.playerUid = playerUid;
        this.actor = new EntityActor(playerUid);
        armorHandler = new ActorArmorHandler(actor, isLocal);
    }


    // callbacks

    public boolean isPlayerDead() {
        return actor.getHealth() <= 0;
    }

    public ConnectedClient getAssociatedClient() {
        return Network.getSingleton().getServer().getConnectedClientForPlayer(playerUid);
    }

    public EntityActor getActor() {
        return actor;
    }

    public void onTick() {
        boolean isDead = isPlayerDead();
        if (!isDead) {
            if (!isInitialized) {
                initialize();
                isInitialized = true;
            }
            int dimension = actor.getDimension();
            if (dimension != dimensionId) {
                onChangeDimension(dimension, dimensionId);
            }
        }

        if (!isTickCallbackDisabled) {
            try {
                Callback.invokeCallback(isLocal ? "LocalPlayerTick" : "ServerPlayerTick", actor.getUid(), isDead);
            } catch (Throwable err) {
                isTickCallbackDisabled = true;
                ICLog.e("FATAL ERROR", "Fatal error occurred in ticking callback for player entity " + actor.getUid() + ", ticking callback will be disabled for this player, until re-entering the world.", err);
            }
        }

        armorHandler.onTick(); // tick even if dead to reset armor
    }

    private void initialize() {
        Callback.invokeAPICallback(isLocal ? "LocalPlayerLoaded" : "ServerPlayerLoaded", actor.getUid());
        dimensionId = actor.getDimension();
        onChangeDimension(dimensionId, dimensionId);
    }

    public void onChangeDimension(int currentId, int lastId) {
        dimensionId = currentId;
        Callback.invokeAPICallback(isLocal ? "LocalPlayerChangedDimension" : "PlayerChangedDimension", actor.getUid(), currentId, lastId);
    }

    public void onHurt(long attacker, int damageValue, int damageType, boolean bool1, boolean bool2) {
        armorHandler.onHurt(attacker, damageValue, damageType, bool1, bool2);
    }

    public void onEat(int food, float ratio) {
        Callback.invokeAPICallback(isLocal ? "LocalPlayerEat" : "ServerPlayerEat", actor.getUid(), food, ratio);
    }
}
