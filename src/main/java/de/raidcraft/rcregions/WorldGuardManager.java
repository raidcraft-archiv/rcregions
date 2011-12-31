package de.raidcraft.rcregions;

import com.silthus.raidcraft.util.RCLogger;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

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
            RCLogger.warning("WorldGuard not found! Disabling RCRegions...");
            Plugin rcRegions = Bukkit.getServer().getPluginManager().getPlugin("RCRegions");
            Bukkit.getServer().getPluginManager().disablePlugin(rcRegions);
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

    public static ApplicableRegionSet getLocalRegions(Location location) {
        return getWorldGuard().getRegionManager(location.getWorld()).getApplicableRegions(location);
    }

    public static boolean isOwner(String player, String id, World world) {
        return getRegion(id, world).isOwner(getWorldGuard().wrapPlayer(Bukkit.getServer().getPlayer(player)));
    }

    public static boolean isOwner(String player, ProtectedRegion region) {
        return region.isOwner(getWorldGuard().wrapPlayer(Bukkit.getServer().getPlayer(player)));
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
}
