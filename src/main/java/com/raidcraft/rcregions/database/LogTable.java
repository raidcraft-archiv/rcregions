package com.raidcraft.rcregions.database;

import com.raidcraft.rcregions.RegionLog;
import de.raidcraft.RaidCraft;
import de.raidcraft.api.database.Table;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class LogTable extends Table {

    public LogTable() {

        super("logs", "rcr_");
    }

    @Override
    public void createTable() {

        try {
            getConnection().prepareStatement(
                    "CREATE TABLE  `" + getTableName() + "` (\n" +
                            "`id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY ," +
                            "`player` VARCHAR( 32 ) NULL ," +
                            "`region` VARCHAR( 32 ) NULL ," +
                            "`action` VARCHAR( 32 ) NULL ," +
                            "`price` DOUBLE NULL ," +
                            "`tax` DOUBLE NULL ," +
                            "`time` VARCHAR( 32 ) NULL " +
                            ") ENGINE = InnoDB;"
            ).executeUpdate();
        } catch (SQLException e) {
            RaidCraft.LOGGER.warning(e.getMessage());
            e.printStackTrace();
        }
    }

    public void logAction(RegionLog log) {

        try {
            getConnection().prepareStatement(
                    "INSERT INTO " + getTableName() + "(player, region, action, price, tax, time) VALUES (" +
                            "'" + log.getPlayer() + "'," +
                            "'" + log.getRegion() + "'," +
                            "'" + log.getAction() + "'," +
                            "'" + log.getPrice() + "'," +
                            "'" + log.getTax() + "'," +
                            "'" + log.getTime() + "'" +
                            ");"
            ).executeUpdate();
        } catch (SQLException e) {
            RaidCraft.LOGGER.warning(e.getMessage());
            e.printStackTrace();
        }
    }

    public List<RegionLog> getHistory(String region) {
        List<RegionLog> regionHistory = new ArrayList<>();
        try {
            ResultSet resultSet = getConnection().prepareStatement(
                    "SELECT * FROM " + getTableName() + " WHERE region = '" + region + "' ORDER BY id DESC LIMIT 0, 3;"
            ).executeQuery();
            RegionLog regionLog;
            if (resultSet.next()) {
                do {
                    regionLog = new RegionLog(
                            resultSet.getString("player"),
                            resultSet.getString("region"),
                            resultSet.getString("action"),
                            resultSet.getDouble("price"),
                            resultSet.getDouble("tax"),
                            resultSet.getString("time")
                    );
                    regionHistory.add(regionLog);
                } while (resultSet.next());
            }
        } catch (SQLException e) {
            RaidCraft.LOGGER.warning(e.getMessage());
            e.printStackTrace();
        }
        return regionHistory;
    }
}
