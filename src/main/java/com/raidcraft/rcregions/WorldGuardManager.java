package com.raidcraft.rcregions;

import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.databases.ProtectionDatabaseException;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import de.raidcraft.RaidCraft;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * 17.12.11 - 11:30
 * @author Silthus
 */
public class WorldGuardManager {

    private static WorldGuardPlugin worldGuard;

    public static void load() {
        Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("WorldGuard");
        if ((plugin == null) || !(plugin instanceof WorldGuardPlugin)) {
            RaidCraft.LOGGER.warning("WorldGuard not found! Disabling RCRegions...");
            Plugin rcRegions = Bukkit.getServer().getPluginManager().getPlugin("RCRegions");
            Bukkit.getServer().getPluginManager().disablePlugin(rcRegions);
            return;
        }
        worldGuard = (WorldGuardPlugin) plugin;
    }
    
    public static WorldGuardPlugin getWorldGuard() {
        if (worldGuard == null) {
            load();
        }
        return worldGuard;
    }

    public static ProtectedRegion getRegion(String id, World world) {
        return getWorldGuard().getRegionManager(world).getRegion(id);
    }

    public static ProtectedRegion getRegion(String id) {
        ProtectedRegion region = null;
        for (World world : Bukkit.getServer().getWorlds()) {
            region = getWorldGuard().getRegionManager(world).getRegion(id);
            if (region != null) {
                return region;
            }
        }
        return region;
    }

    private static LocalPlayer wrapPlayer(Player player) {
        return getWorldGuard().wrapPlayer(player);
    }

    public static Map<String, ProtectedRegion> getPlayerRegions(Player player) {
        Map<String, ProtectedRegion> regionMap = new HashMap<String, ProtectedRegion>();
        RegionManager regionManager = getWorldGuard().getRegionManager(player.getWorld());
        Map<String, ProtectedRegion> regions = regionManager.getRegions();
        int count = regionManager.getRegionCountOfPlayer(wrapPlayer(player));
        int i = 0;
        for (ProtectedRegion region : regions.values()) {
            if (isOwner(player.getName(), region)) {
                i++;
                regionMap.put(region.getId(), region);
            }
            if (i == count) {
                break;
            }
        }
        return regionMap;
    }

    public static ApplicableRegionSet getLocalRegions(Location location) {
        return getWorldGuard().getRegionManager(location.getWorld()).getApplicableRegions(location);
    }

    public static boolean isOwner(String player, String id, World world) {
        return getRegion(id, world).isOwner(wrapPlayer(Bukkit.getServer().getPlayer(player)));
    }

    public static boolean isOwner(String player, ProtectedRegion region) {
        return region.isOwner(wrapPlayer(Bukkit.getServer().getPlayer(player)));
    }

    public static Set<String> getOwners(String id, World world) {
        return getRegion(id, world).getOwners().getPlayers();
    }

    public static Set<String> getMembers(String id, World world) {
        return getRegion(id, world).getMembers().getPlayers();
    }

    public static Set<String> getOwners(ProtectedRegion region) {
        return region.getOwners().getPlayers();
    }

    public static Set<String> getMembers(ProtectedRegion region) {
        return region.getMembers().getPlayers();
    }
    
    public static void save() {
        for (World world : Bukkit.getServer().getWorlds()) {
            try {
                getWorldGuard().getRegionManager(world).save();
            } catch (ProtectionDatabaseException e) {
				RaidCraft.LOGGER.warning(e.getMessage());
            }
        }
    }
}
