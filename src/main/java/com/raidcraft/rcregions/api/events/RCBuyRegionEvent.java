package com.raidcraft.rcregions.api.events;

import com.raidcraft.rcregions.api.Region;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * @author Silthus
 */
@Getter
@Setter
public class RCBuyRegionEvent extends Event implements Cancellable {

    private final Player player;
    private final Region region;
    private double price;
    private boolean cancelled;

    public RCBuyRegionEvent(Player player, Region region, double price) {

        this.player = player;
        this.region = region;
        this.price = price;
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
