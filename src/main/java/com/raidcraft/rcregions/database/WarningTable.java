package com.raidcraft.rcregions.database;

import com.raidcraft.rcregions.Region;
import com.raidcraft.rcregions.RegionManager;
import com.raidcraft.rcregions.RegionWarning;
import com.raidcraft.rcregions.exceptions.UnknownRegionException;
import com.silthus.raidcraft.database.Database;
import com.silthus.raidcraft.database.RCTable;
import com.silthus.raidcraft.util.RCLogger;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Silthus
 */
public class WarningTable extends RCTable<WarningTable> {

	public WarningTable(Database database) {
		super(WarningTable.class, database, "warnings");
	}

	@Override
	public void createTable() {
		PreparedStatement prepare = getDatabase().prepare(
				"CREATE TABLE  `" + getDatabase().getName() + "`.`" + getName() + "` (\n" +
						"`id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY ," +
						"`region` VARCHAR( 64 ) NOT NULL ," +
						"`message` VARCHAR( 256 ) NOT NULL ," +
						"`time` LONG NOT NULL " +
						") ENGINE = InnoDB;"
		);
		getDatabase().executeUpdate(prepare);
	}

	public List<RegionWarning> getRegionWarning(String region) {

		List<RegionWarning> warnings = new ArrayList<RegionWarning>();
		try {
			ResultSet resultSet = getDatabase().executeQuery("SELECT * FROM `" + getName() + "` WHERE region='" + region + "'");
			while (resultSet.next()) {
				warnings.add(new RegionWarning(new Data(region, resultSet)));
			}
		} catch (SQLException e) {
			RCLogger.error(e);
		} catch (UnknownRegionException e) {
			RCLogger.error(e);
		}
		return warnings;
	}

	public void saveWarning(RegionWarning warning) {

		getDatabase().executeUpdate("INSERT INTO `" + getName() + "` (region, message, time) " +
				"VALUES ('" + warning.getRegion().getName() + "', '" + warning.getMessage() + "', " + warning.getTime());
	}

	public static class Data {

		public final String message;
		public final Region region;
		public final long time;

		public Data(String region, ResultSet resultSet) throws SQLException, UnknownRegionException {

			this.message = resultSet.getString("message");
			this.region = RegionManager.getInstance().getRegion(region);
			this.time = resultSet.getLong("time");
		}
	}
}
