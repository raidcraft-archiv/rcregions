package com.raidcraft.rcregions;

import com.raidcraft.rcregions.api.raidcraftevents.RcPlayerEntryRegionEvent;
import com.raidcraft.rcregions.api.raidcraftevents.RcPlayerExitRegionEvent;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import de.raidcraft.RaidCraft;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * @author Dragonfire
 */
public class PlayerTracker implements Runnable, Listener {

    private Map<UUID, Location> lastLocation = new HashMap<>();
    private Map<UUID, Set<ProtectedRegion>> lastRegion = new HashMap<>();
    private WorldGuardPlugin wg;

    // tmp variables for performance
    private Collection<? extends Player> playerCache;
    private ApplicableRegionSet regionSet;
    private Set<ProtectedRegion> newSet;
    private Set<ProtectedRegion> lastSet;
    private RcPlayerEntryRegionEvent entryEvent;
    private RcPlayerExitRegionEvent exitEvent;


    public PlayerTracker(Plugin plugin) {

        wg = WorldGuardManager.getWorldGuard();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void run() {

        playerCache = Bukkit.getOnlinePlayers();
        for (Player player : playerCache) {
            if (trackPlayer(player)) {
                lastLocation.put(player.getUniqueId(), player.getLocation());
            }
        }
    }

    public boolean trackPlayer(Player player) {

        regionSet = wg.getRegionManager(player.getWorld())
                .getApplicableRegions(player.getLocation());
        newSet = new HashSet<>();
        lastSet = lastRegion.get(player.getUniqueId());
        for (ProtectedRegion regio : regionSet) {
            // check if player was in this regio
            if (lastSet.contains(regio)) {
                // remove it
                lastSet.remove(regio);
            } else {
                // player entry region
                entryEvent = new RcPlayerEntryRegionEvent(player, regio);
                RaidCraft.callEvent(entryEvent);
                if (entryEvent.isCancelled()) {
                    teleportBack(player);
                    return false;
                }
            }
            newSet.add(regio);

        }
        for (ProtectedRegion oldRegion : lastSet) {
            // player exit region
            exitEvent = new RcPlayerExitRegionEvent(player, oldRegion);
            RaidCraft.callEvent(exitEvent);
            if (exitEvent.isCancelled()) {
                teleportBack(player);
                return false;
            }
        }
        lastRegion.put(player.getUniqueId(), newSet);
        return true;
    }

    public void teleportBack(Player player) {

        player.teleport(lastLocation.get(player.getUniqueId()));
    }


    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void join(PlayerJoinEvent event) {

        lastRegion.put(event.getPlayer().getUniqueId(), new HashSet<>());
        lastLocation.put(event.getPlayer().getUniqueId(), event.getPlayer().getLocation());
    }
}
