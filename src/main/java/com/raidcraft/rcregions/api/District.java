package com.raidcraft.rcregions.api;

import java.util.List;
import java.util.Set;

/**
 * @author Silthus
 */
public interface District {

    String getName();

    String getFriendlyName();

    String getIdentifier();

    String getDescription();

    Set<String> getApplicableWorlds();

    List<Region> getRegions();

    double getPricePerBlock();

    boolean isDefaultBuyable();

    int getMaxRegionCount();
}
