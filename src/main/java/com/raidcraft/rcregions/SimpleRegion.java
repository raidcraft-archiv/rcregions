package com.raidcraft.rcregions;

import com.avaje.ebean.EbeanServer;
import com.raidcraft.rcregions.api.AbstractRegion;
import com.raidcraft.rcregions.exceptions.UnknownDistrictException;
import com.raidcraft.rcregions.tables.TRegion;
import de.raidcraft.RaidCraft;

/**
 * @author Silthus
 */
public class SimpleRegion extends AbstractRegion {

    protected SimpleRegion(TRegion region) throws UnknownDistrictException {

        super(region.getId(),
                WorldGuardManager.getRegion(region.getName()),
                RaidCraft.getComponent(DistrictManager.class).getDistrict(region.getDistrict())
        );
        setBuyable(region.isBuyable());
    }

    @Override
    public void save() {

        EbeanServer database = RaidCraft.getDatabase(RegionsPlugin.class);
        TRegion region = database.find(TRegion.class, getId());
        region.setOwner(getOwner());
        region.setDistrict(getDistrict().getName());
        region.setPrice(getPrice());
        region.setBuyable(isBuyable());
        database.update(region);
    }
}
