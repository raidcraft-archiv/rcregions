package com.raidcraft.rcregions.config;

import com.raidcraft.rcregions.bukkit.RegionsPlugin;
import com.silthus.raidcraft.bukkit.BukkitBasePlugin;
import com.silthus.raidcraft.config.RCConfig;

import java.util.Set;

/**
 *
 * 17.12.11 - 11:27
 * @author Silthus
 */
public class MainConfig extends RCConfig {

    private static final String FILENAME = "config.yml";
    private static MainConfig self;

    public MainConfig(BukkitBasePlugin plugin) {
        super(plugin, FILENAME);
    }

	public static void init(BukkitBasePlugin plugin) {
		self = new MainConfig(plugin);
	}

    public static MainConfig get() {
        if (self == null) {
            self = new MainConfig(RegionsPlugin.get());
        }
        return self;
    }

    public String getSignIdentifier() {
        return get().getConfig().getString("signIdentifier");
    }

    public long getWarnInterval() {
        return get().getConfig().getLong("warn-interval", 300);
    }
    
    public int getToolId() {
        return get().getConfig().getInt("tool-id");
    }

    public Set<String> getDistricts() {
        return DistrictConfig.get().getDistricts();
    }

    public DistrictConfig.SingleDistrictConfig getDistrict(String district) {
        return DistrictConfig.get().getDistrict(district);
    }
}
