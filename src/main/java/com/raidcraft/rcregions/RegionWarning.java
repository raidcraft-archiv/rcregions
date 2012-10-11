package com.raidcraft.rcregions;

import com.raidcraft.rcregions.database.RegionsDatabase;
import com.raidcraft.rcregions.database.WarningTable;
import com.raidcraft.rcregions.listeners.RCPlayerListener;

import java.text.SimpleDateFormat;

/**
 * @author Silthus
 */
public class RegionWarning {

	public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy HH:mm");

	private final int id;
	private final Region region;
	private final String message;
	private final long time;

	protected RegionWarning(Region region, String message) {

		this.id = RegionsDatabase.getNextWarningId();
		this.region = region;
		this.message = message;
		this.time = System.currentTimeMillis();
		save();
	}

	public RegionWarning(WarningTable.Data data) {

		this.id = data.id;
		this.region = data.region;
		this.message = data.message;
		this.time = data.time;
	}

	private void save() {
		RegionsDatabase.saveWarning(this);
	}

	public void remove() {
		RegionsDatabase.removeWarning(this);
		RCPlayerListener.removeWarning(this);
	}

	public int getId() {
		return id;
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
