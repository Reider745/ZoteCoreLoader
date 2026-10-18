package com.reider745.behavior.entities.runtime;

import cn.nukkit.command.CommandSender;
import cn.nukkit.entity.Entity;
import cn.nukkit.lang.CommandOutputContainer;
import cn.nukkit.lang.TextContainer;
import cn.nukkit.permission.*;
import cn.nukkit.plugin.Plugin;
import cn.nukkit.Server;

import java.util.Map;

/** Commands from an installed pack run with that actor's position and selector context. */
public final class MobCommandSender implements CommandSender {
    private final Entity entity;
    public MobCommandSender(Entity entity) { this.entity = entity; }
    public void sendMessage(String message) { getServer().getLogger().debug("[Behavior " + entity.getName() + "] " + message); }
    public void sendMessage(TextContainer message) { sendMessage(message.getText()); }
    public void sendCommandOutput(CommandOutputContainer output) {}
    public Server getServer() { return entity.getServer(); }
    public String getName() { return entity.getName(); }
    public boolean isEntity() { return true; }
    public Entity asEntity() { return entity; }
    public boolean isPlayer() { return false; }
    public cn.nukkit.level.Position getPosition() { return entity.getPosition(); }
    public cn.nukkit.level.Location getLocation() { return entity.getLocation(); }
    public boolean isOp() { return true; }
    public void setOp(boolean value) {}
    public boolean hasPermission(String name) { return true; }
    public boolean hasPermission(Permission permission) { return true; }
    public boolean isPermissionSet(String name) { return true; }
    public boolean isPermissionSet(Permission permission) { return true; }
    public PermissionAttachment addAttachment(Plugin plugin) { throw new UnsupportedOperationException(); }
    public PermissionAttachment addAttachment(Plugin plugin, String name) { throw new UnsupportedOperationException(); }
    public PermissionAttachment addAttachment(Plugin plugin, String name, Boolean value) { throw new UnsupportedOperationException(); }
    public void removeAttachment(PermissionAttachment attachment) {}
    public void recalculatePermissions() {}
    public Map<String, PermissionAttachmentInfo> getEffectivePermissions() { return Map.of(); }
}
