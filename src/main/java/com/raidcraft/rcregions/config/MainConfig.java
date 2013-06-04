package com.raidcraft.rcregions.config;

import com.raidcraft.rcregions.RegionsPlugin;
import de.raidcraft.api.config.ConfigurationBase;
import de.raidcraft.api.config.Setting;

import java.util.Set;

/**
 * 17.12.11 - 11:27
 *
 * @author Silthus
 */
public class MainConfig extends ConfigurationBase<RegionsPlugin> {

    public MainConfig(RegionsPlugin plugin) {

        super(plugin, "config.yml");
    }

    @Setting("signIdentifier")
    public String sign_identitifer = "Region";
    @Setting("warn-interval")
    public int warn_interval = 300;
    @Setting("tool-id")
    public int tool_id = 340;

    public Set<String> getDistricts() {

        return getPlugin().getDistrictConfig().getDistricts();
    }

    public DistrictConfig.SingleDistrictConfig getDistrict(String district) {

        return getPlugin().getDistrictConfig().getDistrict(district);
    }


}
