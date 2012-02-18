package com.raidcraft.rcregions.config;

import com.raidcraft.rcregions.exceptions.UnknownRegionException;
import com.silthus.raidcraft.bukkit.BukkitBasePlugin;
import com.silthus.raidcraft.config.ConfigManager;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashSet;
import java.util.Set;

/**
 * User: Silthus
 */
public class RegionsConfig {

    private static final String FILENAME = "regions.yml";
    private static BukkitBasePlugin plugin;

    public static void init(BukkitBasePlugin plugin) {
        RegionsConfig.plugin = plugin;
        load();
    }

    public static void save() {
        ConfigManager.save(FILENAME, plugin);
    }

    public static void reload() {
        ConfigManager.reload(FILENAME, plugin);
    }

    public static void load() {
        ConfigManager.loadConfig(FILENAME, plugin);
    }
    
    private static ConfigurationSection getConfig() {
        return ConfigManager.getConfig(FILENAME, plugin).getConfigurationSection("regions");
    }

    public static Set<String> getRegions() {
        Set<String> keys = getConfig().getKeys(false);
        if (keys == null) {
            return new HashSet<String>();
        }
        return keys;
    }
    
    public static SingleRegionConfig getRegion(String id) {
        return new SingleRegionConfig(id);
    }

    public static class SingleRegionConfig {

        private final ConfigurationSection section;
        
        public SingleRegionConfig(String id) {
            this.section = getConfig().getConfigurationSection(id);
            if (section == null) {
                getConfig().createSection(id);
                save();
            }
        }

        public Object getFlag(String flag) {
            return section.get(flag);
        }

        public void setFlag(String flag, Object value) {
            section.createSection(flag);
            section.set(flag, value);
            save();
        }
    }
    
}
