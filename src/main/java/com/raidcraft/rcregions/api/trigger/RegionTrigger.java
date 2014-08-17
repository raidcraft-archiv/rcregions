package com.raidcraft.rcregions.api.trigger;

import com.raidcraft.rcregions.api.raidcraftevents.RcPlayerEntryRegionEvent;
import com.raidcraft.rcregions.api.raidcraftevents.RcPlayerExitRegionEvent;
import de.raidcraft.api.action.trigger.Trigger;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * @author Dragonfire
 */
public class RegionTrigger extends Trigger implements Listener {

    public RegionTrigger() {

        super("region", "entry", "exit");
    }

    @Information(value = "region.entry",
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
}