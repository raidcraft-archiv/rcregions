package de.raidcraft.rcregions.config;

import com.silthus.raidcraft.bukkit.BukkitBasePlugin;
import com.silthus.raidcraft.config.ConfigManager;
import org.bukkit.configuration.ConfigurationSection;

import java.util.List;

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
    
    public static void load() {
        ConfigManager.loadConfig(FILENAME, plugin);
    }

    public static ConfigurationSection getConfig() {
        return ConfigManager.getConfig(FILENAME, plugin);
    }

    public static ConfigurationSection getDefault() {
        return getConfig().getConfigurationSection("defaults");
    }
    
    public static List<String> getIgnoredRegions() {
        return getConfig().getStringList("ignoredRegions");
    }
    
    public static String getDefaultOwner() {
        return getDefault().getString("owner");
    } 
    
    public static double getDefaultPrice() {
        return getDefault().getDouble("price");
    }
    
    public static boolean getDefaultForSale() {
        return getDefault().getBoolean("forSale");
    }

    public static String getSignIdentifier() {
        return getConfig().getString("signIdentifier");
    }
}
