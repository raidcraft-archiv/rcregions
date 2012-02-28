package com.raidcraft.rcregions.database;

import com.raidcraft.rcregions.bukkit.RegionsPlugin;
import com.raidcraft.rcregions.config.MainConfig;
import com.silthus.raidcraft.database.RCDatabase;

import java.util.HashSet;
import java.util.Set;

/**
 * User: Silthus
 */
public class RegionsDatabase extends RCDatabase {
    
    private static RegionsDatabase _self;
    private static MainConfig.DatabaseConfig config;
    
    public static void init() {
        config = MainConfig.getDatabase();
        get();
    }
    
    public static RegionsDatabase get() {
        if (_self == null) {
            _self = new RegionsDatabase();
            _self.setupTables();
        }
        return _self;
    }
    
    private String prefix;
    private Set<String> tables = new HashSet<String>();

    private RegionsDatabase() {
        super(RegionsPlugin.get(),
                config.getName(),
                config.getUrl(),
                config.getUsername(),
                config.getPassword(),
                config.getType(),
                config.getPrefix());
    }
}
