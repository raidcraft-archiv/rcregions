package com.raidcraft.rcregions.database;

import com.raidcraft.rcregions.bukkit.RegionsPlugin;
import com.silthus.raidcraft.database.RCDatabase;

/**
 * User: Silthus
 */
public class RegionsDatabase extends RCDatabase {

	private static final String PREFIX = "rcr_";
    private static RegionsDatabase _self;
    
    public static RegionsDatabase get() {
        if (_self == null) {
            _self = new RegionsDatabase();
            _self.setupTables();
        }
        return _self;
    }

    private RegionsDatabase() {
        super(RegionsPlugin.get(), PREFIX);
        addTable(new RegionsTable(this));
    }
}
