package com.raidcraft.rcregions.api.raidcraftevents;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * @author Dragonfire
 */
public class RcPlayerEntryRegionEvent extends Event implements Cancellable {

    @Setter
    @Getter
    boolean cancelled = false;
    @Setter
    @Getter
    private Player player;
    @Setter
    @Getter
    private ProtectedRegion region;

    public RcPlayerEntryRegionEvent(Player player, ProtectedRegion region) {

        this.player = player;
        this.region = region;
    }

    // Bukkit stuff
    private static final HandlerList handlers = new HandlerList();

    public HandlerList getHandlers() {

        return handlers;
    }

    public static HandlerList getHandlerList() {

        return handlers;
    }
}
