package com.raidcraft.rcregions;

import com.raidcraft.rcregions.config.DistrictConfig;
import com.raidcraft.rcregions.exceptions.UnconfiguredConfigException;
import de.raidcraft.RaidCraft;

import javax.annotation.concurrent.Immutable;

/**
 * 21.01.12 - 10:16
 *
 * @author Silthus
 */
@Immutable
public class District {

    private final String name;
    private String identifier;
    private String claimCommand;
    private boolean needsPermission;
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

        DistrictConfig.SingleDistrictConfig district = RaidCraft.getComponent(RegionsPlugin.class).getMainConfig().getDistrict(getName());
        this.identifier = district.getIdentifier();
        this.claimCommand = district.getClaimCommand();
        this.minPrice = district.getMinPrice();
        this.dropable = district.isDropable();
        this.dropOnChange = district.dropRegionOnChange();
        this.maxRegions = district.getMaxRegions();
        this.maxRegions = district.getMaxRegions();
        this.useVolume = district.useVolume();
        this.needsPermission = district.getNeedsPermission();
        try {
            this.scheduledTaxesCount = district.getScheduledRegionCount();
            this.scheduledTaxesInterval = district.getScheduledRegionInterval();
            this.scheduledTaxesAmount = district.getScheduledTax();
            this.scheduleTaxes = true;
        } catch (UnconfiguredConfigException e) {
            scheduleTaxes = false;
            RaidCraft.LOGGER.info("Interval taxes for the District " + getName() + " not loaded.");
        }
    }

    public String getName() {

        return name;
    }

    public String getIdentifier() {

        return identifier;
    }

    public String getClaimCommand() {

        return claimCommand;
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

    public boolean getNeedsPermission() {

        return needsPermission;
    }

    public double getTaxes(int count) {

        return RaidCraft.getComponent(RegionsPlugin.class).getMainConfig().getDistrict(getName()).getTaxes(count);
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

        return getName() + "[" + getIdentifier() + "]";
    }
}
