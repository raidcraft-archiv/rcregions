package com.raidcraft.rcregions;

import com.raidcraft.rcregions.api.raidcraftevents.RcPlayerExitRegionEvent;
import com.raidcraft.rcregions.exceptions.RegionException;
import com.raidcraft.rcregions.tables.RestrictRegionType;
import com.raidcraft.rcregions.tables.TRestrictRegion;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author Dragonfire
 */
public class RestrictionManager implements Listener {

    private Map<UUID, Map<ProtectedRegion, TRestrictRegion>> restrictedTo = new HashMap<>();
    private WorldGuardPlugin wg;
    private RegionsPlugin plugin;

    public RestrictionManager(RegionsPlugin plugin) {

        this.plugin = plugin;
        wg = WorldGuardManager.getWorldGuard();
        load();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    private void load() {

        List<TRestrictRegion> regions = plugin.getDatabase().find(TRestrictRegion.class).findList();
        HashMap<String, RegionManager> worlds = new HashMap<>();
        for (TRestrictRegion region : regions) {
            // get world regionmanager
            RegionManager regionManager = worlds.get(region.getWorldName());
            if (regionManager == null) {
                regionManager = wg.getRegionManager(Bukkit.getWorld(region.getWorldName()));
                worlds.put(region.getWorldName(), regionManager);
            }
            // load region
            ProtectedRegion wgRegion = regionManager.getRegion(region.getRegionName());
            if (wgRegion == null) {
                plugin.getLogger().warning("Region (" + region.getRegionName()
                        + ") not exist in world (" + region.getWorldName() + ")");
                continue;
            }
            region.setRegion(wgRegion);
            Map<ProtectedRegion, TRestrictRegion> alreadyRestricted =
                    restrictedTo.get(region.getPlayer());
            if (alreadyRestricted == null) {
                alreadyRestricted = new HashMap<>();
                restrictedTo.put(region.getPlayer(), alreadyRestricted);
            }
            alreadyRestricted.put(wgRegion, region);
        }
    }

    public void restrictPlayerToRegion(Player player, String region, String msg) throws RegionException {

        if (region == null) {
            throw new RegionException("region is null (restrictPlayerToRegion)");
        }
        ProtectedRegion wgRegion = wg.getRegionManager(player.getWorld()).getRegion(region);
        // exists region?
        if (wgRegion == null) {
            throw new RegionException("Region (" + region + ") existiert nicht (restrictPlayerToRegion)");
        }
        // check if player is in region
        if (!wgRegion.contains(player.getLocation().getBlockX(),
                player.getLocation().getBlockY(),
                player.getLocation().getBlockZ())) {
            throw new RegionException("Spieler (" + player.getName()
                    + ") steht nicht in Region (" + region + ") auf die er beschränkt wird");
        }

        Map<ProtectedRegion, TRestrictRegion> alreadyRestricted = restrictedTo.get(player.getUniqueId());
        if (alreadyRestricted != null && alreadyRestricted.containsKey(wgRegion)) {
            throw new RegionException("Spieler (" + player.getName()
                    + ") ist bereits auf die Region (" + region + ") beschränkt");
        }
        // save all
        TRestrictRegion restrictedRegion = new TRestrictRegion();
        restrictedRegion.setRegion(wgRegion);
        restrictedRegion.setMsg(msg);
        restrictedRegion.setPlayer(player.getUniqueId());
        restrictedRegion.setRegionName(wgRegion.getId());
        restrictedRegion.setWorldName(player.getWorld().getName());
        restrictedRegion.setType(RestrictRegionType.EXIT);
        plugin.getDatabase().save(restrictedRegion);

        if (alreadyRestricted == null) {
            alreadyRestricted = new HashMap<>();
            restrictedTo.put(player.getUniqueId(), alreadyRestricted);
        }
        alreadyRestricted.put(wgRegion, restrictedRegion);
    }

    public void removePlayerToRegionRestriction(Player player, String region) throws RegionException {

        if (region == null) {
            throw new RegionException("region is null (removePlayerToRegionRestriction)");
        }
        ProtectedRegion wgRegion = wg.getRegionManager(player.getWorld()).getRegion(region);
        if (wgRegion == null) {
            throw new RegionException("Region (" + region + ") existiert nicht (removePlayerToRegionRestriction)");
        }

        Map<ProtectedRegion, TRestrictRegion> alreadyRestricted = restrictedTo.get(player.getUniqueId());
        if (alreadyRestricted == null || !alreadyRestricted.containsKey(wgRegion)) {
            throw new RegionException("Spieler (" + player.getName()
                    + ") steht nicht auf Region (" + region + ") beschränkt");
        }
        // delete db entry
        TRestrictRegion restrictedRegion = alreadyRestricted.get(wgRegion);
        plugin.getDatabase().delete(restrictedRegion);
        alreadyRestricted.remove(wgRegion);
        if (alreadyRestricted.size() <= 0) {
            restrictedTo.remove(player.getUniqueId());
        }
    }

    @EventHandler
    private void exit(RcPlayerExitRegionEvent event) {

        if (!restrictedTo.containsKey(event.getPlayer().getUniqueId())) return;

        // if player is not restricted to region
        TRestrictRegion region = restrictedTo.get(event.getPlayer().getUniqueId()).get(event.getRegion());
        if (region == null) {
            return;
        }
        Location loc = event.getLastLocation();
        // if player is not in region
        if (event.getRegion().contains(loc.getBlockX(), loc.getBlockY(), loc.getBlockY())) {
            plugin.getLogger().warning("Player (" + event.getPlayer().getName() + ") beschränkt auf ("
                    + event.getRegion().getId() + ") aber alte Position ist auch in dieser Region");
            return;
        }
        String msg = region.getMsg();
        if (msg == null) {
            msg = "Du darfst die Region nicht verlassen";
        }
        event.getPlayer().sendMessage(msg);
        event.getPlayer().teleport(loc);
        event.setCancelled(true);
    }
}
