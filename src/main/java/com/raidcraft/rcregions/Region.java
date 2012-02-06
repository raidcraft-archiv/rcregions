package com.raidcraft.rcregions;

import com.raidcraft.rcregions.config.MainConfig;
import com.raidcraft.rcregions.exceptions.RegionException;
import com.raidcraft.rcregions.exceptions.UnknownDistrictException;
import com.silthus.raidcraft.util.RCMessaging;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 *
 * 17.12.11 - 11:54
 * @author Silthus
 */
public class Region {

    private final String name;
    private final ProtectedRegion region;
    
    private String owner;
    private double price;
    private District district;
    private boolean buyable;
    
    public Region(ProtectedRegion region) throws UnknownDistrictException {
        this.name = region.getId();
        this.region = region;
        load();
    }
    
    private void load() throws UnknownDistrictException {
        // get the district
        String district = getName().replaceAll("\\d*$", "");
        this.district = DistrictManager.get().getDistrictFromIdentifier(district);
        // check for null
        if (district == null) {
            throw new UnknownDistrictException("The district of this region is not configured!");
        }
        // set the first owner as main owner
        for (String player : region.getOwners().getPlayers()) {
            this.owner = player;
            break;
        }
        // check if it is for sale
        if (region.getFlag(DefaultFlag.BUYABLE) == null) {
            if (owner == null || owner.equals("")) {
                setBuyable(true);
            } else {
                setBuyable(false);
            }
        } else {
            this.buyable = region.getFlag(DefaultFlag.BUYABLE);
        }
        // set the price
        if (region.getFlag(DefaultFlag.PRICE) == null) {
            setPrice(getDistrict().getMinPrice());
        } else {
            this.price = region.getFlag(DefaultFlag.PRICE);
        }
    }

    /* All Getters and Setters go here */
    public String getName() {
        return name;
    }
    
    public String getOwner() {
        return owner;
    } 
    
    public double getPrice() {
        if (district.useVolume() && price < getBasePrice()) {
            setPrice(getBasePrice());
        }
        return price;
    }
    
    public boolean isBuyable() {
        return buyable;
    }
    
    public ProtectedRegion getRegion() {
        return region;
    }

    public void setOwner(String player) {
        this.owner = player;
        DefaultDomain domain = new DefaultDomain();
        if (!(player == null) && !player.equals("")) {
            domain.addPlayer(player);
        }
        region.setOwners(domain);
        save();
    }

    public void setPrice(double price) {
        if (district.useVolume()) {
            if (price < getBasePrice()) {
                this.price = getBasePrice();
            } else {
                this.price = price;
            }
        } else {
            if (price < district.getMinPrice()) {
                this.price = district.getMinPrice();
            } else {
                this.price = price;
            }
        }
        this.region.setFlag(DefaultFlag.PRICE, price);
        save();
    }
    
    public void setBuyable(boolean sell) {
        this.buyable = sell;
        region.setFlag(DefaultFlag.BUYABLE, sell);
        save();
    }
    
    public District getDistrict() {
        return district;
    }
    
    private void save() {
        WorldGuardManager.save();
    }

    public void setAccessFlags(boolean denyAccess) {
        getRegion().setFlag(DefaultFlag.CHEST_ACCESS, (denyAccess ? StateFlag.State.ALLOW : null));
        getRegion().setFlag(DefaultFlag.USE, (denyAccess ? StateFlag.State.ALLOW : null));
        getRegion().setFlag(DefaultFlag.BUILD, (denyAccess ? StateFlag.State.DENY : null));
        save();
    }
    
    public double getBasePrice() {
        if (region instanceof ProtectedCuboidRegion) {
            MainConfig.SingleDistrictConfig district = MainConfig.getDistrict(this.district.getName());
            BlockVector max = region.getMaximumPoint();
            BlockVector min = region.getMinimumPoint();
            int xLength = max.getBlockX() - min.getBlockX();
            int zWidth = max.getBlockZ() - min.getBlockZ();
            int volume;
            if (district.useVolume()) {
                volume = xLength * zWidth * (max.getBlockY() - min.getBlockY());
            } else {
                volume = xLength * zWidth;
            }
            return Math.round(volume * district.getPricePerBlock());
        }
        Player player;
        for (String name : MainConfig.getAdmins()) {
            player = Bukkit.getPlayer(name);
            if (!(player == null)) {
                RCMessaging.warn(player, "Region " + region.getId() + " is a polygon! Please change it to a cuboid...");
            }
        }
        return MainConfig.getDistrict(district.getName()).getMinPrice();
    }
    
    public String toString() {
        return getName();
    }
}
