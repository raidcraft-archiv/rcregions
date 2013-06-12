package com.raidcraft.rcregions.config;

import com.raidcraft.rcregions.RegionsPlugin;
import de.raidcraft.api.config.ConfigurationBase;

/**
 * User: Silthus
 */
public class DistrictConfig extends ConfigurationBase<RegionsPlugin> {

    public DistrictConfig(RegionsPlugin plugin) {

        super(plugin, "districts.yml");
    }
}
