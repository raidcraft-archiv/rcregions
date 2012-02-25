package com.raidcraft.rcregions.config;

import com.raidcraft.rcregions.exceptions.UnconfiguredConfigException;
import com.silthus.raidcraft.bukkit.BukkitBasePlugin;
import com.silthus.raidcraft.config.ConfigManager;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Set;

/**
 * User: Silthus
 */
public class DistrictConfig {

    private static final String FILENAME = "districts.yml";
    private static BukkitBasePlugin plugin;

    public static void init(BukkitBasePlugin plugin) {
        DistrictConfig.plugin = plugin;
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

    public static Set<String> getDistricts() {
        return getConfig().getConfigurationSection("districts").getKeys(false);
    }

    public static SingleDistrictConfig getDistrict(String district) {
        return new SingleDistrictConfig(district);
    }

    public static class SingleDistrictConfig {

        private final ConfigurationSection section;
        private final String name;

        public SingleDistrictConfig(String name) {
            this.section = getConfig().getConfigurationSection("districts." + name);
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public String getIdentifier() {
            return section.getString("identifier");
        }

        public double getMinPrice() {
            return section.getDouble("minPrice", 0.0);
        }

        public boolean isDropable() {
            return section.getBoolean("dropable", true);
        }

        public double getRefundPercentage() {
            return section.getDouble("refund-percentage", 0.20);
        }

        public boolean useVolume() {
            return section.getBoolean("taxes.useVolume", true);
        }

        public double getPricePerBlock() {
            return section.getDouble("taxes.pricePerBlock", 0.0);
        }

        public boolean dropRegionOnChange() {
            return section.getBoolean("drop-on-change", false);
        }

        public int getMaxRegions() {
            return section.getInt("maxRegions", -1);
        }

        public double getTaxes(int count) {
            double tax = section.getDouble("taxes." + count);
            if (tax == 0 && count > 0) {
                return getTaxes(--count);
            }
            return section.getDouble("taxes." + count, 0.0);
        }

        private ConfigurationSection getScheduledTaxes() throws UnconfiguredConfigException {
            ConfigurationSection taxes = section.getConfigurationSection("scheduledTaxes");
            if (taxes == null) {
                throw new UnconfiguredConfigException("The scheduled taxes for " + getName() + " are not configured.");
            }
            return taxes;
        }

        public int getScheduledRegionCount() throws UnconfiguredConfigException {
            return getScheduledTaxes().getInt("regionCount", 3);
        }

        public int getScheduledRegionInterval() throws UnconfiguredConfigException {
            return getScheduledTaxes().getInt("interval", 3600);
        }

        public double getScheduledTax() throws UnconfiguredConfigException {
            return getScheduledTaxes().getDouble("tax", 0.20);
        }

        public boolean getNeedsPermission() {
            return section.getBoolean("needs-permission", false);
        }
    }
}