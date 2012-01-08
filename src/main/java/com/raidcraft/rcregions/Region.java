package com.raidcraft.rcregions;

import com.silthus.raidcraft.util.RCLogger;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.raidcraft.rcregions.bukkit.RegionsPlugin;
import com.raidcraft.rcregions.config.MainConfig;
import com.raidcraft.rcregions.database.RegionDatabase;

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
    private double minPrice;
    private boolean forSale;
    
    public Region(ProtectedRegion region) {
        this.name = region.getId();
        this.region = region;
        load();
    }
    
    private void setDefaults() {
        this.owner = MainConfig.getDefaultOwner();
        this.price = MainConfig.getDefaultPrice();
        this.forSale = MainConfig.getDefaultForSale();
    }
    
    private void load() {
        RegionDatabase regionDatabase = RegionsPlugin.get().getDatabase().find(RegionDatabase.class).
                where().ieq("region", getName()).findUnique();
        if (regionDatabase == null) {
            setDefaults();
            regionDatabase = new RegionDatabase();
            regionDatabase.setOwner(getOwner());
            regionDatabase.setPrice(getPrice());
            regionDatabase.setMinPrice(getMinPrice());
            regionDatabase.setRegion(getName());
            regionDatabase.setForSale(isForSale());
            saveDatabase(regionDatabase);
            RCLogger.debug("New Region Database entry for " + getName() + " created.");
            return;
        }
        loadFromDatabase(regionDatabase);
        RCLogger.debug("Region loaded from database: " + getName());
    }
    
    private void loadFromDatabase(RegionDatabase database) {
        this.owner = database.getOwner();
        this.price = database.getPrice();
        this.minPrice = database.getMinPrice();
        this.forSale = database.isForSale();
    }

    private void saveDatabase(RegionDatabase database) {
        RegionsPlugin.get().getDatabase().save(database);
    }
    
    public void save() {
        // TODO: add more save functions
        RegionDatabase regionDatabase = RegionsPlugin.get().getDatabase().find(RegionDatabase.class).
                where().ieq("region", getName()).findUnique();
        regionDatabase.setOwner(getOwner());
        regionDatabase.setPrice(getPrice());
        regionDatabase.setMinPrice(getMinPrice());
        regionDatabase.setForSale(isForSale());
        saveDatabase(regionDatabase);
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
    
    public double getMinPrice() {
        return minPrice;
    }
    
    public boolean isForSale() {
        return forSale;
    }
    
    public ProtectedRegion getRegion() {
        return region;
    }

    public void setOwner(String player) {
        this.owner = player;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setMinPrice(double price) {
        this.minPrice = price;
    }

    public void setForSale(boolean forSale) {
        this.forSale = forSale;
    }
}
