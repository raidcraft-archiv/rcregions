package com.raidcraft.rcregions.config;

import com.raidcraft.rcregions.RegionsPlugin;
import de.raidcraft.api.config.ConfigurationBase;
import de.raidcraft.api.config.Setting;

/**
 * 17.12.11 - 11:27
 *
 * @author Silthus
 */
public class MainConfig extends ConfigurationBase<RegionsPlugin> {

    public MainConfig(RegionsPlugin plugin) {

        super(plugin, "config.yml");
    }

    @Setting("sign-identifier")
    public String sign_identitifer = "Region";
    @Setting("tool-id")
    public int tool_id = 340;
}
