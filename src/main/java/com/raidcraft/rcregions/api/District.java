package com.raidcraft.rcregions.api;

import java.util.List;
import java.util.Set;

/**
 * @author Silthus
 */
public interface District {

    public String getName();

    public String getFriendlyName();

    public String getIdentifier();

    public String getDescription();

    public Set<String> getApplicableWorlds();

    public List<Region> getRegions();

    public double getPricePerBlock();

    public boolean isDefaultBuyable();

    public int getMaxRegionCount();
}
