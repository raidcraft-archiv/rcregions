package com.raidcraft.rcregions;

import com.silthus.raidcraft.util.RCEconomy;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.raidcraft.rcregions.config.MainConfig;
import com.raidcraft.rcregions.exceptions.PlayerException;
import com.raidcraft.rcregions.exceptions.RegionException;
import com.raidcraft.rcregions.exceptions.UnknownRegionException;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * 17.12.11 - 11:49
 *
 * @author Silthus
 */
public class RegionManager {

    private static RegionManager self;
    private final HashMap<String, Region> _regions;

    private RegionManager() {
        _regions = new HashMap<String, Region>();
    }

    public static RegionManager get() {
        if (self == null) {
            self = new RegionManager();
        }
        return self;
    }

    public Region getRegion(String name) throws UnknownRegionException {
        if (_regions.containsKey(name)) {
            return _regions.get(name);
        } else {
            ProtectedRegion region = WorldGuardManager.getRegion(name);
            if (region != null && isAllowedRegion(region)) {
                _regions.put(name, new Region(region));
                return getRegion(name);
            }
        }
        throw new UnknownRegionException("Die Region " + name + " existiert nicht!");
    }

    public Region getRegion(Location location) throws UnknownRegionException {
        ApplicableRegionSet localRegions = WorldGuardManager.getLocalRegions(location);
        for (ProtectedRegion region : localRegions) {
            String name = region.getId();
            if (_regions.containsKey(name)) {
                return _regions.get(name);
            } else if (isAllowedRegion(region)) {
                _regions.put(name, new Region(region));
                return getRegion(name);
            }
        }
        throw new UnknownRegionException("Die Region existiert nicht oder steht nicht zum Verkauf bereit.");
    }

    public boolean isAllowedRegion(ProtectedRegion region) {
        return !(MainConfig.getIgnoredRegions().contains(region.getId()));
    }

    public void buyRegion(Player player, Region region) throws PlayerException, RegionException {
        String owner = region.getOwner();
        if ( !(owner == null || owner.equals("") || owner.equalsIgnoreCase("Staff"))) {
            throw new RegionException("Die Region gehört bereits jemandem.\nBesitzer: " + owner);
        }
        double price = region.getPrice();
        if (!RCEconomy.hasEnough(player, price)) {
            throw new PlayerException("Nicht genug Geld um dieses Grundstück zu kaufen.\nPreis: " + price + " Coins.");
        }
        price = price * getTaxes(player, region);
        if (!RCEconomy.hasEnough(player, price)) {
            throw new PlayerException("Du hast nicht genug Geld um die Steuern zu bezahlen.\nPreis: " + price + " Coins.");
        }
    }

    public double getTaxes(Player player, Region region) {
        Map<String, ProtectedRegion> playerRegions = getPlayerRegions(player);
        // the identifier of the regions district, e.g. z for zentrum
        // the regex removes all numbers at the end of the string
        String district = region.getName().replaceAll("\\d*$", "");

    }

    private Map<String, ProtectedRegion> getPlayerRegions(Player player) {
        return WorldGuardManager.getPlayerRegions(player);
    }
}
