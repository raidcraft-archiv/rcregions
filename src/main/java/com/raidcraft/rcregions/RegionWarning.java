package com.raidcraft.rcregions;

import com.raidcraft.rcregions.database.RegionsDatabase;
import com.raidcraft.rcregions.database.WarningTable;

/**
 * @author Silthus
 */
public class RegionWarning {

	private final Region region;
	private final String message;
	private final long time;

	protected RegionWarning(Region region, String message) {

		this.region = region;
		this.message = message;
		this.time = System.currentTimeMillis();
		save();
	}

	public RegionWarning(WarningTable.Data data) {

		this.region = data.region;
		this.message = data.message;
		this.time = data.time;
	}

	private void save() {
		RegionsDatabase.saveWarning(this);
	}

	public Region getRegion() {
		return region;
	}

	public String getMessage() {
		return message;
	}

	public long getTime() {
		return time;
	}
}
