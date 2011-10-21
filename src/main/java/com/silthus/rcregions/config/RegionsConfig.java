/*
 * Copyright (C) 2011 RaidCraft <http://www.raid-craft.de>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.silthus.rcregions.config;

import com.silthus.raidcraft.bukkit.BukkitBasePlugin;
import com.silthus.raidcraft.config.RCConfig;
import com.silthus.raidcraft.util.RCLogger;
import org.bukkit.util.config.Configuration;

/**
 *
 * 27.09.11 - 11:06
 * @author Silthus
 */
public class RegionsConfig extends RCConfig {

    private static final String FILENAME = "regions.yml";
    private static final int VERSION = 1;

    private static Configuration file;

    /**
     * Loads and creates the config file. Fires setup()
     * when everything is created correctly.
     * @param plugin instance
     */
    public RegionsConfig(BukkitBasePlugin plugin) {
        super(plugin, FILENAME, VERSION);
    }

    /**
     * This is used to setup all variables used in the
     * config file.
     * @param file to load from
     */
    @Override
    public void setup(Configuration file) {
        RegionsConfig.file = file;
    }

    public static double getMinPrice() {
        RCLogger.debug("[Config Access] regions.minprice: " 
                + file.getDouble("regions.minPrice", 200.00));
        return file.getDouble("regions.minPrice", 200.00);
    }

    public static String getUniqueLine() {
        RCLogger.debug("[Config Access] regions.identifier: "
                + file.getString("regions.identifier", "RCRegion"));
        return file.getString("regions.identifier", "RCRegion");
    }

    public static double getMultiPercentage() {
        RCLogger.debug("[Config Access] regions.percentage: " 
                + file.getDouble("regions.percentage", 0.30));
        return file.getDouble("regions.percentage", 0.30);
    }
}
