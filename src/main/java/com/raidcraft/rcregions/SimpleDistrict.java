package com.raidcraft.rcregions;

import com.raidcraft.rcregions.api.AbstractDistrict;
import com.raidcraft.rcregions.api.Region;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Silthus
 */
public class SimpleDistrict extends AbstractDistrict {

    private final List<Region> regions = new ArrayList<>();

    protected SimpleDistrict(String name, ConfigurationSection config) {

        super(name, config);
    }

    @Override
    public List<Region> getRegions() {

        return regions;
    }

    public void addRegion(Region region) {

        this.regions.add(region);
    }
}
