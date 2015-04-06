package com.raidcraft.rcregions.trigger;

import com.raidcraft.rcregions.api.events.RCBuyRegionEvent;
import com.raidcraft.rcregions.api.events.RCClaimRegionEvent;
import com.raidcraft.rcregions.api.events.RCDropRegionEvent;
import com.raidcraft.rcregions.api.events.RcPlayerEntryRegionEvent;
import com.raidcraft.rcregions.api.events.RcPlayerExitRegionEvent;
import de.raidcraft.api.action.trigger.Trigger;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * @author Silthus
 */
public class RegionTrigger extends Trigger implements Listener {

    public RegionTrigger() {

        super("region", "buy", "sell", "drop", "claim", "enter", "exit");
    }

    @Information(value = "region.enter",
            desc = "If the player entry a region")
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void entry(RcPlayerEntryRegionEvent event) {

        Player player = event.getPlayer();
        if (player == null) {
            return;
        }
        informListeners("entry", player, config ->
                !config.isSet("region")
                        || config.getString("region").equalsIgnoreCase(event.getRegion().getId()));
    }

    @Information(value = "region.exit",
            desc = "If the player exit a region")
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void create(RcPlayerExitRegionEvent event) {

        Player player = event.getPlayer();
        if (player == null) {
            return;
        }
        informListeners("exit", player, config ->
                !config.isSet("region")
                        || config.getString("region").equalsIgnoreCase(event.getRegion().getId()));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onRegionClaim(RCClaimRegionEvent event) {

        if (!event.getPlayer().isOnline()) return;
        informListeners("claim", event.getPlayer().getPlayer(), config -> {
            if (config.isSet("region")) {
                return event.getRegion().getName().equalsIgnoreCase(config.getString("region"));
            }
            if (config.isSet("district")) {
                return event.getRegion().getDistrict().getName().equalsIgnoreCase(config.getString("district"));
            }
            return true;
        });
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onRegionDrop(RCDropRegionEvent event) {

        if (!event.getPlayer().isOnline()) return;
        informListeners("drop", event.getPlayer().getPlayer(), config -> {
            if (config.isSet("region")) {
                return event.getRegion().getName().equalsIgnoreCase(config.getString("region"));
            }
            if (config.isSet("district")) {
                return event.getRegion().getDistrict().getName().equalsIgnoreCase(config.getString("district"));
            }
            return true;
        });
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onRegionBuy(RCBuyRegionEvent event) {

        if (!event.getPlayer().isOnline()) return;
        informListeners("drop", event.getPlayer().getPlayer(), config -> {
            if (config.isSet("min-price")) {
                if (event.getPrice() < config.getDouble("min-price")) return false;
            }
            if (config.isSet("region")) {
                return event.getRegion().getName().equalsIgnoreCase(config.getString("region"));
            }
            if (config.isSet("district")) {
                return event.getRegion().getDistrict().getName().equalsIgnoreCase(config.getString("district"));
            }
            return true;
        });
    }
}
