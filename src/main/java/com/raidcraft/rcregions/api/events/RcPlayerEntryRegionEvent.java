package com.raidcraft.rcregions.api.events;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * @author Dragonfire
 */
@Setter
@Getter
public class RcPlayerEntryRegionEvent extends Event implements Cancellable {

    boolean cancelled = false;
    private Player player;
    private ProtectedRegion region;
    private Location lastLocation;

    public RcPlayerEntryRegionEvent(Player player, ProtectedRegion region, Location lastLocation) {

        this.player = player;
        this.region = region;
        this.lastLocation = lastLocation;
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
