/*
 * Copyright (C) 2011 RaidCraft <http://www.raid-craft.de>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.silthus.rcregions;

import com.silthus.raidcraft.util.RCEconomy;
import com.silthus.rcregions.bukkit.RCRegionsPlugin;
import com.silthus.rcregions.config.RegionsConfig;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * 29.09.11 - 17:56
 * @author Silthus
 */
public class RCRegionManager {

    private static RCRegionsPlugin plugin;
    // Zest
    private static HashMap<String,Region> regions = new HashMap<String, Region>();

    public static void init(RCRegionsPlugin plugin) {
        RCRegionManager.plugin = plugin;
        loadRegions();
    }

    private static void loadRegions() {
        // TODO: implement region storage and saving
    }

    /**
     * Gets the region manager for the specified world
     * @param world player is in
     * @return RegionManager
     */
    private static RegionManager getRegionManager(World world) {
        return plugin.getWorldGuard().getRegionManager(world);
    }

    /**
     * Gets a list of all region managers
     * @return list of region managers for all worlds
     */
    private static List<RegionManager> getAllRegionManagers() {
        List<RegionManager> regionManagerList = new ArrayList<RegionManager>();
        for (World w : plugin.getServer().getWorlds()) {
            // ...and put every world and its RegionManager into the Map
            regionManagerList.add(plugin.getWorldGuard().getRegionManager(w));
        }
        return regionManagerList;
    }

    /**
     * Gets a WorldGuardRegion from all worlds defined by the id
     * @param id of the WorldGuard region
     * @return WorldGuard Region
     * @throws UnknownRegionException
     */
    public static ProtectedRegion getWorldGuardRegion(String id) throws UnknownRegionException {
        for (RegionManager regionManager : getAllRegionManagers()) {
            if (regionManager.hasRegion(id)) {
                return regionManager.getRegion(id);
            }
        }
        throw new UnknownRegionException("This WorldGuard Region does not exist!");
    }

    /**
     * Gets a region defined by its unique name and
     * adds it as a new region if none exists
     * @param name of the region
     * @return ProtectedRegion
     */
    public static Region getRegion(String name) throws UnknownRegionException {
        if (regions.containsKey(name)) {
            return regions.get(name);
        } else if (isWorldGuardRegion(name)) {
            return addRegion(name);
        } else {
            throw new UnknownRegionException("This region is not a WorldGuard Region!");
        }
    }

    /**
     * Gets all regions from all worlds
     * @return all regions
     */
    public static HashMap<String, ProtectedRegion> getAllRegions() {
        HashMap<String, ProtectedRegion> regionHashMap = new HashMap<String, ProtectedRegion>();
        for (RegionManager regionManager : getAllRegionManagers()) {
            regionHashMap.putAll(regionManager.getRegions());
        }
        return regionHashMap;
    }

    /**
     * Adds a new region to the regions HashMap
     * and returns the associated Region object
     * @param name of the region to add
     * @return Region
     */
    private static Region addRegion(String name) throws UnknownRegionException {
        Region region = new Region(name);
        regions.put(name, region);
        return region;
    }

    /**
     * Wraps a player into a WorldGuard player
     * @param player to wrap
     * @return LocalPlayer
     */
    private static LocalPlayer wrapPlayer(Player player) {
        return plugin.getWorldGuard().wrapPlayer(player);
    }

    /**
     * Checks if the given name is a WorldGuard region
     * @param name regionName
     * @return true if worldGuard region
     */
    private static boolean isWorldGuardRegion(String name) {
        // goes thru all worlds to check if the region exists
        for (RegionManager rm : getAllRegionManagers()) {
            // checks if the region exists
            if (rm.hasRegion(name))
                return true;
        }
        return false;
    }

    /**
     * Gets all regions the player is MainOwner from
     * @param player to get regions for
     * @return region count
     */
    public static int getRegionCount(Player player) throws UnknownRegionException {
        int cnt = 0;
        for (String region : getAllRegions().keySet()) {
            if (getRegion(region).getMainOwner().equalsIgnoreCase(player.getName())) {
                cnt ++;
            }
        }
        return cnt;
    }

    /**
     * Saves all Regions into a Database or Flatfile
     */
    public static void save() {
        // TODO: save all regions here
    }

    /**
     * Checks if the player has enough money to buy that region
     * @param player to check
     * @param region to check
     * @return true if player has enough money
     * @throws UnknownRegionException
     */
    public static boolean hasEnough(Player player, Region region) throws UnknownRegionException {
        return RCEconomy.hasEnough(player, getTotalRegionCost(player, region));
    }

    /**
     * Gets the required money to buy the region also
     * calculating the extra percentage from multi regions
     * @param player to get price for
     * @param region to get price from
     * @return region price
     * @throws UnknownRegionException
     */
    public static double getTotalRegionCost(Player player, Region region) throws UnknownRegionException {
        // calculates the price based on the plus price percentage for each region
        return ((getRegionCount(player) * RegionsConfig.getMultiPercentage())
                * region.getPrice()) + region.getPrice();
    }

    /**
     * Buys a region and adds it to the players region list.
     * Also removes all current owners and members from the
     * region.
     * @param player that buys the region
     * @param region to buy
     * @return false if player has not enough money
     * @throws UnknownRegionException
     */
    public static boolean buyRegion(Player player, Region region) throws UnknownRegionException {
        if (hasEnough(player, region)) {
            region.clearOwners();
            RCEconomy.substract(player, region.getPrice());
            region.setMainOwner(player.getName());
            region.setForSale(false);
            return true;
        }
        return false;
    }
}
