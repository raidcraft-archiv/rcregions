package com.raidcraft.rcregions.database;

import com.raidcraft.rcregions.RegionWarning;
import com.raidcraft.rcregions.bukkit.RegionsPlugin;
import com.raidcraft.rcregions.exceptions.UnknownRegionException;
import com.silthus.raidcraft.database.RCDatabase;

import java.util.List;

/**
 * User: Silthus
 */
public class RegionsDatabase extends RCDatabase {

	private static final String PREFIX = "rcr_";
    private static RegionsDatabase _self;

    public static RegionsDatabase getInstance() {
        if (_self == null) {
            _self = new RegionsDatabase();
            _self.setupTables();
        }
        return _self;
    }

    private RegionsDatabase() {
        super(RegionsPlugin.get(), PREFIX);
        addTable(new LogTable(this));
    }

	public static List<RegionWarning> getRegionWarnings(String region) {
		return getInstance().getTable(WarningTable.class).getRegionWarning(region);
	}

	public static void saveWarning(RegionWarning warning) {
		getInstance().getTable(WarningTable.class).saveWarning(warning);
	}

	public static List<RegionWarning> getAllRegionWarnings() {
		return getInstance().getTable(WarningTable.class).getAllWarnings();
	}

	public static int getNextWarningId() {
		return getInstance().getTable(WarningTable.class).getNextWarningId();
	}

	public static int getWarningCount() {
		return getInstance().getTable(WarningTable.class).getWarningCount();
	}

	public static RegionWarning getRegionWarning(int id) throws UnknownRegionException {
		return getInstance().getTable(WarningTable.class).getRegionWarning(id);
	}

	public static void removeWarning(RegionWarning warning) {
		getInstance().getTable(WarningTable.class).removeWarning(warning);
	}
}
