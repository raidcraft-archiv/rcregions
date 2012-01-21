package com.raidcraft.rcregions;

import com.raidcraft.rcregions.config.MainConfig;
import com.raidcraft.rcregions.exceptions.PlayerException;
import com.raidcraft.rcregions.exceptions.RegionException;
import com.raidcraft.rcregions.exceptions.UnknownDistrictException;
import com.raidcraft.rcregions.exceptions.UnknownRegionException;
import com.silthus.raidcraft.util.RCEconomy;
import com.silthus.raidcraft.util.RCLogger;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * 17.12.11 - 11:49
 *
 * @author Silthus
 */
public final class RegionManager {

    private static RegionManager _self;
    private final HashMap<String, Region> _regions;

    private RegionManager() {
        _regions = new HashMap<String, Region>();
        load();
    }

    private void load() {
        WorldGuardPlugin worldGuard = WorldGuardManager.getWorldGuard();
        for (World world : Bukkit.getServer().getWorlds()) {
            for (ProtectedRegion region : worldGuard.getRegionManager(world).getRegions().values()) {
                if (isAllowedRegion(region)) {
                    try {
                        _regions.put(region.getId(), new Region(region));
                    } catch (UnknownDistrictException e) {
                        RCLogger.warning(e.getMessage());
                    }
                }
            }
        }
    }
    
    public synchronized static void reload() {
        _self = null;
        _self = new RegionManager();
    }

    public static RegionManager get() {
        if (_self == null) {
            _self = new RegionManager();
        }
        return _self;
    }

    public Region getRegion(String name) throws UnknownRegionException {
        if (_regions.containsKey(name)) {
            return _regions.get(name);
        } else {
            ProtectedRegion region = WorldGuardManager.getRegion(name);
            if (region != null && isAllowedRegion(region)) {
                try {
                    Region regio = new Region(region);
                    _regions.put(name, regio);
                    return regio;
                } catch (UnknownDistrictException e) {
                    RCLogger.warning(e.getMessage());
                }
            }
        }
        throw new UnknownRegionException("The region " + name + " does not exist. Did you name it correctly?");
    }

    public Region getRegion(Location location) throws UnknownRegionException {
        ApplicableRegionSet localRegions = WorldGuardManager.getLocalRegions(location);
        for (ProtectedRegion region : localRegions) {
            String name = region.getId();
            if (_regions.containsKey(name)) {
                return _regions.get(name);
            } else if (isAllowedRegion(region)) {
                try {
                    _regions.put(name, new Region(region));
                    return getRegion(name);
                } catch (UnknownDistrictException e) {
                    RCLogger.warning(e.getMessage());
                }
            }
        }
        throw new UnknownRegionException("Die Region existiert nicht oder steht nicht zum Verkauf bereit.");
    }

    public boolean isAllowedRegion(ProtectedRegion region) {
        String id = region.getId();
        boolean matches = false;
        for (String district : MainConfig.getDistricts()) {
            if (id.matches("^" + MainConfig.getDistrict(district).getIdentifier() + ".*")) {
                matches = true;
                break;
            }
        }
        return matches;
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
       District district = region.getDistrict();
        int taxCnt = 0;
        for (String r :getPlayerRegions(player).keySet()) {
            try {
                if (getRegion(r).getDistrict() == district) {
                    taxCnt++;
                }
            } catch (UnknownRegionException e) {
                RCLogger.debug(e.getMessage());
            }
        }
        return district.getTaxes(taxCnt);
    }

    private Map<String, ProtectedRegion> getPlayerRegions(Player player) {
        return WorldGuardManager.getPlayerRegions(player);
    }
}
