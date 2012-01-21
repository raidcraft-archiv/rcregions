package com.raidcraft.rcregions;

import com.raidcraft.rcregions.config.MainConfig;
import com.raidcraft.rcregions.exceptions.UnconfiguredConfigException;
import com.silthus.raidcraft.util.RCLogger;

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
    private boolean free;
    private int maxRegions;
    private boolean scheduleTaxes = false;
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
        this.free = district.isFreeDistrict();
        this.maxRegions = district.getMaxRegions();
        this.maxRegions = district.getMaxRegions();
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

    public boolean isFree() {
        return free;
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
}
