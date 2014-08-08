package com.raidcraft.rcregions;

import com.raidcraft.rcregions.api.raidcraftevents.RcPlayerExitRegionEvent;
import com.raidcraft.rcregions.exceptions.RegionException;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * @author Dragonfire
 */
public class RestrictionManager implements Listener {

    private Map<UUID, Set<ProtectedRegion>> restrictedTo = new HashMap<>();
    private Map<UUID, Set<ProtectedRegion>> cannotEnter = new HashMap<>();
    private WorldGuardPlugin wg;
    private RegionsPlugin plugin;

    public RestrictionManager(RegionsPlugin plugin) {

        this.plugin = plugin;
        wg = WorldGuardManager.getWorldGuard();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void restrictPlayerToRegion(Player player, String region) throws RegionException {

        if (region == null) {
            throw new RegionException("region is null (restrictPlayerToRegion)");
        }
        ProtectedRegion wgRegion = wg.getRegionManager(player.getWorld()).getRegion(region);
        if (wgRegion == null) {
            throw new RegionException("Region (" + region + ") existiert nicht (restrictPlayerToRegion)");
        }
        Set<ProtectedRegion> alreadyRestricted = restrictedTo.get(player.getUniqueId());

        if (alreadyRestricted != null && alreadyRestricted.contains(wgRegion)) {
            throw new RegionException("Spieler (" + player.getName()
                    + ") ist bereits auf die Region (" + region + ") beschränkt");
        }
        if (!wgRegion.contains(player.getLocation().getBlockX(),
                player.getLocation().getBlockY(),
                player.getLocation().getBlockZ())) {
            throw new RegionException("Spieler (" + player.getName()
                    + ") steht nicht in Region (" + region + ") auf die er beschränkt wird");
        }
        if (alreadyRestricted == null) {
            alreadyRestricted = new HashSet<>();
            restrictedTo.put(player.getUniqueId(), alreadyRestricted);
        }
        alreadyRestricted.add(wgRegion);
    }

    public void removePlayerToRegionRestriction(Player player, String region) throws RegionException {

        if (region == null) {
            throw new RegionException("region is null (removePlayerToRegionRestriction)");
        }
        ProtectedRegion wgRegion = wg.getRegionManager(player.getWorld()).getRegion(region);
        if (wgRegion == null) {
            throw new RegionException("Region (" + region + ") existiert nicht (removePlayerToRegionRestriction)");
        }

        Set<ProtectedRegion> alreadyRestricted = restrictedTo.get(player.getUniqueId());
        if (alreadyRestricted == null || !alreadyRestricted.contains(wgRegion)) {
            throw new RegionException("Spieler (" + player.getName()
                    + ") steht nicht auf Region (" + region + ") beschränkt");
        }
        alreadyRestricted.remove(wgRegion);
        if (alreadyRestricted.size() <= 0) {
            restrictedTo.remove(player.getUniqueId());
        }
    }

    @EventHandler
    private void exit(RcPlayerExitRegionEvent event) {

        if (!restrictedTo.containsKey(event.getPlayer().getUniqueId())) return;

        // if player is not restricted to region
        if (!restrictedTo.get(event.getPlayer().getUniqueId()).contains(event.getRegion())) {
            return;
        }
        Location loc = event.getLastLocation();
        // if player is not in region
        if (event.getRegion().contains(loc.getBlockX(), loc.getBlockY(), loc.getBlockY())) {
            plugin.getLogger().warning("Player (" + event.getPlayer().getName() + ") beschränkt auf ("
                    + event.getRegion().getId() + ") aber alte Position ist auch in dieser Region");
            return;
        }
        event.getPlayer().sendMessage("Du darfst diesen Ort nicht verlassen");
        event.getPlayer().teleport(loc);
        event.setCancelled(true);
    }
}
