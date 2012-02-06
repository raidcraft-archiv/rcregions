package com.raidcraft.rcregions;

import com.raidcraft.rcregions.config.MainConfig;
import com.raidcraft.rcregions.exceptions.UnconfiguredConfigException;
import com.silthus.raidcraft.util.RCLogger;
import com.silthus.raidcraft.util.RCMessaging;

import javax.annotation.concurrent.Immutable;

/**
 *
 * 21.01.12 - 10:16
 * @author Silthus
 */
@Immutable
public class District {

    private final String name;
    private String identifier;
    private double minPrice;
    private boolean dropable;
    private int maxRegions;
    private boolean scheduleTaxes = false;
    private boolean dropOnChange;
    private boolean useVolume;
    private int scheduledTaxesCount;
    private int scheduledTaxesInterval;
    private double scheduledTaxesAmount;
    
    public District(String name) {
        this.name = name;
        load();
    }
    
    private void load() {
        MainConfig.SingleDistrictConfig district = MainConfig.getDistrict(getName());
        this.identifier = district.getIdentifier();
        this.minPrice = district.getMinPrice();
        this.dropable = district.isDropable();
        this.dropOnChange = district.dropRegionOnChange();
        this.maxRegions = district.getMaxRegions();
        this.maxRegions = district.getMaxRegions();
        this.useVolume = district.useVolume();
        try {
            this.scheduledTaxesCount = district.getScheduledRegionCount();
            this.scheduledTaxesInterval = district.getScheduledRegionInterval();
            this.scheduledTaxesAmount = district.getScheduledTax();
            this.scheduleTaxes = true;
        } catch (UnconfiguredConfigException e) {
            scheduleTaxes = false;
            RCLogger.debug("Interval taxes for the District " + getName() + " not loaded.");
        }
    }
    
    public String getName() {
        return name;
    }

    public String getIdentifier() {
        return identifier;
    }

    public double getMinPrice() {
        return minPrice;
    }

    public boolean isDropable() {
        return dropable;
    }

    public boolean useVolume() {
        return useVolume;
    }
    
    public boolean dropOnChange() {
        return dropOnChange;
    }

    public int getMaxRegions() {
        return maxRegions;
    }
    
    public double getTaxes(int count) {
        return MainConfig.getDistrict(getName()).getTaxes(count);
    }
    
    public boolean scheduleTaxes() {
        return scheduleTaxes;
    }

    public int getScheduledTaxesCount() {
        return scheduledTaxesCount;
    }

    public int getScheduledTaxesInterval() {
        return scheduledTaxesInterval;
    }

    public double getScheduledTaxesAmount() {
        return scheduledTaxesAmount;
    }
    
    public boolean equals(Object district) {
        if (district instanceof District) {
            District d = (District) district;
            return d.getIdentifier().equalsIgnoreCase(getIdentifier());
        }
        return false;
    }
    
    public String toString() {
        return RCMessaging.green(getName()) + "[" + RCMessaging.yellow(getIdentifier()) + "]";
    }
}
