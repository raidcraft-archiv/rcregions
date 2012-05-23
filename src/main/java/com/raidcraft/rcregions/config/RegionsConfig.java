package com.raidcraft.rcregions.config;

import com.raidcraft.rcregions.bukkit.RegionsPlugin;
import com.silthus.raidcraft.bukkit.BukkitBasePlugin;
import com.silthus.raidcraft.config.RCConfig;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashSet;
import java.util.Set;

/**
 * User: Silthus
 */
public class RegionsConfig extends RCConfig {

    private static final String FILENAME = "regions.yml";
    private static RegionsConfig self;

    public RegionsConfig(BukkitBasePlugin plugin) {
        super(plugin, FILENAME);
    }

	public static void init(BukkitBasePlugin plugin) {
		self = new RegionsConfig(plugin);
	}

    public static RegionsConfig get() {
        if (self == null) {
            self = new RegionsConfig(RegionsPlugin.get());
        }
        return self;
    }

    private ConfigurationSection getRegionsSection() {
        return get().getConfig().getConfigurationSection("regions");
    }

    public Set<String> getRegions() {
        Set<String> keys = getRegionsSection().getKeys(false);
        if (keys == null) {
            return new HashSet<String>();
        }
        return keys;
    }
    
    public SingleRegionConfig getRegion(String id) {
        return new SingleRegionConfig(id);
    }

    public class SingleRegionConfig {

        private ConfigurationSection section;
        
        public SingleRegionConfig(String id) {
            this.section = getRegionsSection().getConfigurationSection(id);
            if (section == null) {
                getRegionsSection().createSection(id);
                this.section = getRegionsSection().getConfigurationSection(id);
            }
        }

        public Object getFlag(String flag) {
            return section.get(flag);
        }

        public void setFlag(String flag, Object value) {
            section.set(flag, value);
        }
    }
    
}
