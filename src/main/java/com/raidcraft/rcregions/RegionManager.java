package com.raidcraft.rcregions;

import com.raidcraft.rcregions.exceptions.PlayerException;
import com.raidcraft.rcregions.exceptions.RegionException;
import com.raidcraft.rcregions.exceptions.UnknownDistrictException;
import com.raidcraft.rcregions.exceptions.UnknownRegionException;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import de.raidcraft.RaidCraft;
import de.raidcraft.api.Component;
import de.raidcraft.api.economy.BalanceSource;
import de.raidcraft.api.economy.Economy;
import org.bukkit.Bukkit;
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
public final class RegionManager implements Component {

    private final RegionsPlugin plugin;
    private final HashMap<String, Region> regions = new HashMap<>();

    protected RegionManager(RegionsPlugin plugin) {

        this.plugin = plugin;
        RaidCraft.registerComponent(RegionManager.class, this);
    }

    public void reload() {

        regions.clear();
    }

    public Region getRegion(String name) throws UnknownRegionException {

        if (regions.containsKey(name)) {
            return regions.get(name);
        } else {
            ProtectedRegion region = WorldGuardManager.getRegion(name);
            if (region != null && isAllowedRegion(region)) {
                try {
                    Region rcRegion = new Region(region);
                    regions.put(name, rcRegion);
                    return rcRegion;
                } catch (UnknownDistrictException e) {
                    plugin.getLogger().warning(e.getMessage());
                }
            }
        }
        throw new UnknownRegionException("The region " + name + " does not exist. Did you name it correctly?");
    }

    public Region getRegion(Location location) throws UnknownRegionException {

        ApplicableRegionSet localRegions = WorldGuardManager.getLocalRegions(location);
        for (ProtectedRegion region : localRegions) {
            String name = region.getId();
            if (regions.containsKey(name)) {
                return regions.get(name);
            } else if (isAllowedRegion(region)) {
                try {
                    regions.put(name, new Region(region));
                    return getRegion(name);
                } catch (UnknownDistrictException e) {
                    plugin.getLogger().warning(e.getMessage());
                }
            }
        }
        throw new UnknownRegionException("An dieser Stelle befindet sich keine RCRegions Region!");
    }

    public boolean isAllowedRegion(ProtectedRegion region) {

        String id = region.getId();
        boolean matches = false;
        for (String district : plugin.getDistrictConfig().getDistricts()) {
            if (id.matches("^" + plugin.getDistrictConfig().getDistrict(district).getIdentifier() + "\\d*")) {
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
        if (district.getNeedsPermission() && !player.hasPermission("rcregions.district." + district.getName())) {
            throw new PlayerException("Du darfst kein Grundstück in diesem Distrikt kaufen!");
        }
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
            Economy economy = RaidCraft.getEconomy();
            String owner = region.getOwner();
            double price = region.getPrice();

            if (!economy.hasEnough(player.getName(), price)) {
                throw new PlayerException("Nicht genug Geld für das Grundstück: " + price);
            }

            double tax = region.getBasePrice() * getTaxes(player, region);
            if (!economy.hasEnough(player.getName(), (price + tax))) {
                throw new PlayerException("Nicht genug Geld! Grundstück: " + price + " + Steuern: " + tax);
            }
            economy.modify(player.getName(), -(price + tax), BalanceSource.BUY_REGION, "Kauf von " + region.getName());

            if (!(owner == null) && !(owner.equals(""))) {
                economy.modify(region.getOwner(), price, BalanceSource.SELL_REGION, "Verkauf von " + region.getName());
                Player playerOwner = Bukkit.getPlayer(region.getOwner());
                if (playerOwner != null) {
                    playerOwner.sendMessage(ChatColor.YELLOW + "Dein Grundstück " + region.getName() + " wurde von " + player.getName() + " für " + economy.getFormattedAmount(price) + " abgekauft!");
                }
            }
            boolean droped = false;
            for (District d : plugin.getDistrictManager().getDistricts().values()) {
                if (d.dropOnChange()) {
                    for (Region r : getPlayerRegions(player, d)) {
                        clearRegion(player, r);
                        player.sendMessage(ChatColor.RED + "Dein altes Grundstück " + r.getName() + " wurde aufgelöst.");
                        droped = true;
                    }
                }
            }
            if (droped)
                player.sendMessage(ChatColor.GRAY + "Du kannst weiterhin auf deine Kisten zugreifen, jedoch nicht bauen.");
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
                // do nothing because when this happens it means there are other worldguard regions with ownser
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
            sign.setLine(0, RaidCraft.getEconomy().getFormattedAmount(price));
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
            sign.setLine(3, "[" + ChatColor.GREEN + plugin.getMainConfig().sign_identitifer.toUpperCase() + ChatColor.BLACK + "]");
        } else {
            sign.setLine(3, "[" + ChatColor.DARK_RED + plugin.getMainConfig().sign_identitifer.toUpperCase() + ChatColor.BLACK + "]");
        }
        sign.update();
    }

    public void updateSign(SignChangeEvent sign, Region region) {

        double price = region.getPrice();
        if (price > 0.0) {
            sign.setLine(0, RaidCraft.getEconomy().getFormattedAmount(price));
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
            sign.setLine(3, "[" + ChatColor.GREEN + plugin.getMainConfig().sign_identitifer.toUpperCase() + ChatColor.BLACK + "]");
        } else {
            sign.setLine(3, "[" + ChatColor.DARK_RED + plugin.getMainConfig().sign_identitifer.toUpperCase() + ChatColor.BLACK + "]");
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
        RaidCraft.getEconomy().modify(player.getName(), getRefundValue(region), BalanceSource.SELL_REGION, "Verkauf von " + region.getName() + " an Server");
    }

    public double getRefundValue(Region region) {

        return region.getBasePrice() * getRefundPercentage(region);
    }

    public double getRefundPercentage(Region region) {

        return plugin.getMainConfig().getDistrict(region.getDistrict().getName()).getRefundPercentage();
    }
}
