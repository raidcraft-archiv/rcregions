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

	public int getNextWarningId() {

		try {
			ResultSet resultSet = getDatabase().executeQuery("SELECT MAX(id) as nextId FROM `" + getName() + "` LIMIT 1");
			while (resultSet.next()) {
				return (resultSet.getInt("nextId") + 1);
			}
		} catch (SQLException e) {
			RCLogger.error(e);
		}
		return 1;
	}

	public int getWarningCount() {

		try {
			ResultSet resultSet = getDatabase().executeQuery("SELECT COUNT(*) as count FROM `" + getName() + "`");
			while (resultSet.next()) {
				return resultSet.getInt("count");
			}
		} catch (SQLException e) {
			RCLogger.error(e);
		}
		return 0;
	}

	public List<RegionWarning> getRegionWarning(String region) {

		List<RegionWarning> warnings = new ArrayList<RegionWarning>();
		try {
			ResultSet resultSet = getDatabase().executeQuery("SELECT * FROM `" + getName() + "` WHERE region='" + region + "' " +
					"ORDER BY time asc");
			while (resultSet.next()) {
				warnings.add(new RegionWarning(new Data(resultSet)));
			}
		} catch (SQLException e) {
			RCLogger.error(e);
		} catch (UnknownRegionException e) {
			RCLogger.error(e);
		}
		return warnings;
	}

	public List<RegionWarning> getAllWarnings() {

		List<RegionWarning> warnings = new ArrayList<RegionWarning>();
		try {
			ResultSet resultSet = getDatabase().executeQuery("SELECT * FROM `" + getName() + "` ORDER BY time asc");
			while (resultSet.next()) {
				warnings.add(new RegionWarning(new Data(resultSet)));
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
				"VALUES ('" + warning.getRegion().getName() + "', '" + warning.getMessage() + "', " + warning.getTime() + ")");
	}

	public void removeWarning(RegionWarning warning) {

		getDatabase().executeUpdate("DELETE FROM `" + getName() + "` WHERE id=" + warning.getId());
	}

	public RegionWarning getRegionWarning(int id) throws UnknownRegionException {
		try {
			ResultSet resultSet = getDatabase().executeQuery("SELECT * FROM `" + getName() + "` WHERE id=" + id);
			while (resultSet.next()) {
				return new RegionWarning(new Data(resultSet));
			}
		} catch (SQLException e) {
			RCLogger.error(e);
		} catch (UnknownRegionException e) {
			RCLogger.error(e);
		}
		throw new UnknownRegionException("Es gibt keine Verwarnung mit der ID: " + id);
	}

	public static class Data {

		public final int id;
		public final String message;
		public final String region;
		public final long time;

		public Data(ResultSet resultSet) throws SQLException, UnknownRegionException {

			this.id = resultSet.getInt("id");
			this.message = resultSet.getString("message");
			this.region = resultSet.getString("region");
			this.time = resultSet.getLong("time");
		}
	}
}
