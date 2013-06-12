package com.raidcraft.rcregions;

import com.raidcraft.rcregions.api.AbstractDistrict;
import com.raidcraft.rcregions.api.Region;
import com.raidcraft.rcregions.exceptions.UnknownDistrictException;
import com.raidcraft.rcregions.tables.TRegion;
import de.raidcraft.RaidCraft;
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
        // load all of the regions defined in the database
        List<TRegion> regions = RaidCraft.getDatabase(RegionsPlugin.class).find(TRegion.class).where().eq("district", getName()).findList();
        RegionManager regionManager = RaidCraft.getComponent(RegionManager.class);
        for (TRegion tRegion : regions) {
            try {
                this.regions.add(regionManager.createRegion(tRegion));
            } catch (UnknownDistrictException e) {
                // this should never ever occur
                e.printStackTrace();
            }
        }
    }

    @Override
    public List<Region> getRegions() {

        return regions;
    }
}
