package com.raidcraft.rcregions.database;

import com.raidcraft.rcregions.bukkit.RegionsPlugin;
import com.raidcraft.rcregions.config.MainConfig;
import com.silthus.raidcraft.database.RCDatabase;

/**
 * User: Silthus
 */
public class RegionsDatabase extends RCDatabase {

	private static final String PREFIX = "rcr";
    private static RegionsDatabase _self;
    
    public static void init() {
        get();
    }
    
    public static RegionsDatabase get() {
        if (_self == null) {
            _self = new RegionsDatabase();
            _self.setupTables();
        }
        return _self;
    }

    private RegionsDatabase() {
        super(RegionsPlugin.get(), "rcr");
        addTable(new RegionsTable(this));
    }
}
