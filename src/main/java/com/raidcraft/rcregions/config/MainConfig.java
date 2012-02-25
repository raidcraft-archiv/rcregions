package com.raidcraft.rcregions.config;

import com.raidcraft.rcregions.exceptions.UnconfiguredConfigException;
import com.silthus.raidcraft.bukkit.BukkitBasePlugin;
import com.silthus.raidcraft.config.ConfigManager;
import org.bukkit.configuration.ConfigurationSection;

import java.util.List;
import java.util.Set;

/**
 *
 * 17.12.11 - 11:27
 * @author Silthus
 */
public class MainConfig {

    private static final String FILENAME = "config.yml";
    private static BukkitBasePlugin plugin;
    
    public static void init(BukkitBasePlugin plugin) {
        MainConfig.plugin = plugin;
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

    public static ConfigurationSection getConfig() {
        return ConfigManager.getConfig(FILENAME, plugin);
    }

    public static String getSignIdentifier() {
        return getConfig().getString("signIdentifier");
    }

    public static long getWarnInterval() {
        return getConfig().getLong("warn-interval", 300);
    }
    
    public static int getToolId() {
        return getConfig().getInt("tool-id");
    }
    
    public static DatabaseConfig getDatabase() {
        return new DatabaseConfig();
    }
    
    public static class DatabaseConfig {
        
        private ConfigurationSection section;
        
        public DatabaseConfig() {
            this.section = getConfig().getConfigurationSection("database");
        }
        
        public String getType() {
            return section.getString("type");
        }
        
        public String getName() {
            return section.getString("database");
        }
        
        public String getUsername() {
            return section.getString("username");
        }
        
        public String getPassword() {
            return section.getString("password");
        }
        
        public String getUrl() {
            return section.getString("url");
        }
        
        public String getPrefix() {
            return section.getString("prefix");
        }
    }

    public static Set<String> getDistricts() {
        return DistrictConfig.getDistricts();
    }

    public static DistrictConfig.SingleDistrictConfig getDistrict(String district) {
        return DistrictConfig.getDistrict(district);
    }
}
