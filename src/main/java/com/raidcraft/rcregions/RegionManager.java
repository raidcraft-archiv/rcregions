package com.raidcraft.rcregions;

import com.raidcraft.rcregions.bukkit.RegionsPlugin;
import com.raidcraft.rcregions.config.MainConfig;
import com.raidcraft.rcregions.exceptions.PlayerException;
import com.raidcraft.rcregions.exceptions.RegionException;
import com.raidcraft.rcregions.exceptions.UnknownDistrictException;
import com.raidcraft.rcregions.exceptions.UnknownRegionException;
import com.silthus.raidcraft.bukkit.BukkitBasePlugin;
import com.silthus.raidcraft.util.RCEconomy;
import com.silthus.raidcraft.util.RCLogger;
import com.silthus.raidcraft.util.RCMessaging;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
    }
    
    public synchronized static void reload() {
        _self = null;
        _self = new RegionManager();
    }
    
    public static void init() {
        get();
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
                    Region rcRegion = new Region(region);
                    _regions.put(name, rcRegion);
                    return rcRegion;
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
            if (id.matches("^" + MainConfig.getDistrict(district).getIdentifier() + "\\d*")) {
                matches = true;
                break;
            }
        }
        return matches;
    }

    public boolean isBuyableRegion(Player player, Region region) throws PlayerException, RegionException {
        String owner = region.getOwner();
        if (!(owner == null) && owner.equalsIgnoreCase(player.getName())) {
            throw new PlayerException("Du bist bereits der Besitzer dieser Region.");
        }
        District district = region.getDistrict();
        if (!(district.getMaxRegions() == -1) && !(getPlayerRegionCount(player, district) < district.getMaxRegions())) {
            throw new RegionException("Du hast bereits zu viele Grundstücke in diesem Distrikt.");
        }
        if (!region.isBuyable()) {
            throw new RegionException("Diese Region steht nicht zum Verkauf bereit.");
        }
        if (district.dropOnChange() && getPlayerRegionCount(player) > 0) {
            throw new RegionException("Tut mir leid, du darfst diese Region nicht mehr kaufen.");
        }
        return true;
    }

    public void buyRegion(Player player, Region region) throws PlayerException, RegionException {
        if (isBuyableRegion(player, region)) {
            RCEconomy economy = RegionsPlugin.get().getEconomy();
            String owner = region.getOwner();
            double price = region.getPrice();
            if (!economy.has(player.getName(), price)) {
                throw new PlayerException("Nicht genug Geld für das Grundstück: " + price);
            }
            double tax = region.getBasePrice() * getTaxes(player, region);
            if (!economy.hasEnough(player, (price + tax))) {
                throw new PlayerException("Nicht genug Geld! Grundstück: " + price + " + Steuern: " + tax);
            }
            economy.substract(player, (price + tax));
            if (!(owner == null) && !(owner.equals(""))) {
                economy.add(region.getOwner(), price);
            }
            boolean droped = false;
            for (District d : DistrictManager.get().getDistricts().values()) {
                if (d.dropOnChange()) {
                    for (Region r : getPlayerRegions(player, d)) {
                        clearRegion(player, r);
                        RCMessaging.send(player, "Dein altes Grundstück " + r.getName() + " wurde aufgelöst.");
                        droped = true;
                    }
                }
            }
            if (droped)
                RCMessaging.send(player, "Du kannst weiterhin auf deine Kisten zugreifen, jedoch nicht bauen.");
            region.setOwner(player.getName());
            region.setBuyable(false);
            region.setAccessFlags(false);
        }
    }

    public double getTaxes(Player player, Region region) {
        return getTaxes(player, region.getDistrict());
    }

    public double getTaxes(Player player, District district) {
        return district.getTaxes(getPlayerRegionCount(player, district));
    }

    public double getFullPrice(Player player, Region region) {
        return Math.round(region.getBasePrice() * getTaxes(player, region)) + region.getPrice();
    }

    public int getPlayerRegionCount(Player player, District district) {
        return getPlayerRegions(player, district).size();
    }

    public int getPlayerRegionCount(Player player) {
        return getPlayerRegions(player).size();
    }

    public List<Region> getPlayerRegions(Player player) {
        List<Region> playerRegions = new ArrayList<Region>();
        for (String region : WorldGuardManager.getPlayerRegions(player).keySet()) {
            try {
                playerRegions.add(getRegion(region));
            } catch (UnknownRegionException e) {
                RCLogger.warning("Player " + player.getName() + " owns a region of a undefined district.");
            }
        }
        return playerRegions;
    }

    public List<Region> getPlayerRegions(Player player, District district) {
        List<Region> playerRegions = new ArrayList<Region>();
        for (Region region : getPlayerRegions(player)) {
            if (region.getDistrict().equals(district)) {
                playerRegions.add(region);
            }
        }
        return playerRegions;
    }

    public void updateSign(Sign sign, Region region) {
        double price = region.getPrice();
        if (price > 0.0) {
            sign.setLine(0, "" + ChatColor.GREEN + price + ChatColor.YELLOW + "c");
        } else {
            sign.setLine(0, ChatColor.GREEN + "Kostenlos");
        }
        sign.setLine(1, "Region: " + ChatColor.DARK_RED + region.getName());
        String owner = region.getOwner();
        if (owner == null || owner.equalsIgnoreCase("")) {
            owner = "Staff";
        }
        sign.setLine(2, ChatColor.WHITE + owner);
        if (region.isBuyable()) {
            sign.setLine(3, "[" + ChatColor.GREEN + MainConfig.getSignIdentifier().toUpperCase() + ChatColor.BLACK + "]");
        } else {
            sign.setLine(3, "[" + ChatColor.DARK_RED + MainConfig.getSignIdentifier().toUpperCase() + ChatColor.BLACK + "]");
        }
        sign.update();
    }

    public void updateSign(SignChangeEvent sign, Region region) {
        double price = region.getPrice();
        if (price > 0.0) {
            sign.setLine(0, "" + ChatColor.GREEN + price + ChatColor.YELLOW + "c");
        } else {
            sign.setLine(0, ChatColor.GREEN + "Kostenlos");
        }
        sign.setLine(1, "Region: " + ChatColor.DARK_RED + region.getName());
        String owner = region.getOwner();
        if (owner == null || owner.equalsIgnoreCase("")) {
            owner = "Staff";
        }
        sign.setLine(2, ChatColor.WHITE + owner);
        if (region.isBuyable()) {
            sign.setLine(3, "[" + ChatColor.GREEN + MainConfig.getSignIdentifier().toUpperCase() + ChatColor.BLACK + "]");
        } else {
            sign.setLine(3, "[" + ChatColor.DARK_RED + MainConfig.getSignIdentifier().toUpperCase() + ChatColor.BLACK + "]");
        }
    }

    public void dropRegion(Player player, Region region) throws RegionException {
        if (!region.getDistrict().isDropable()) {
            throw new RegionException("Du kannst diese Region nicht an den Server abgeben.");
        }
        if (!player.hasPermission("rcregions.admin") && !region.getOwner().equalsIgnoreCase(player.getName())) {
            throw new RegionException("Du bist nicht der Besitzer dieser Region.");
        }
        clearRegion(player, region);
    }

    public void clearRegion(Player player, Region region) {
        region.setOwner(null);
        region.setBuyable(true);
        region.setAccessFlags(true);
        RegionsPlugin.get().getEconomy().add(player, (region.getBasePrice() *
                MainConfig.getDistrict(region.getDistrict().getName()).getRefundPercentage()));
    }

    public boolean hasWarnedRegions(Player player) {
        List<Region> regions = getWarnedRegions(player);
        return regions.size() > 0;
    }

    public List<Region> getWarnedRegions(Player player) {
        List<Region> regions = getPlayerRegions(player);
        for (Region region : getPlayerRegions(player)) {
            if (!region.isWarned()) {
                regions.remove(region);
            }
        }
        return regions;
    }
}
