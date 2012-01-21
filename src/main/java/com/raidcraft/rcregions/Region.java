package com.raidcraft.rcregions;

import com.raidcraft.rcregions.config.MainConfig;
import com.raidcraft.rcregions.exceptions.UnknownDistrictException;
import com.silthus.raidcraft.util.RCLogger;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

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
        RCLogger.warning("Region " + region.getId() + " is buyable? " + region.getFlag(DefaultFlag.BUYABLE));
        // check if it is for sale
        if (region.getFlag(DefaultFlag.BUYABLE) == null) {
            setBuyable(MainConfig.getDefaultBuyable());
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
        domain.addPlayer(player);
        region.setOwners(domain);
        save();
    }

    public void setPrice(double price) {
        if (price < district.getMinPrice()) {
            this.price = district.getMinPrice();
        } else {
            this.price = price;
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
}
