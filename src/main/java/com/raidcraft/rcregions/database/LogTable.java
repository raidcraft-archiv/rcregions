package com.raidcraft.rcregions.database;

import com.raidcraft.rcregions.RegionLog;
import com.silthus.raidcraft.database.Database;
import com.silthus.raidcraft.database.RCTable;
import com.silthus.raidcraft.util.RCLogger;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class LogTable extends RCTable<LogTable> {
    

    public LogTable(Database database) {
        super(LogTable.class, database, "logs");
    }

    @Override
    public void createTable() {
        PreparedStatement prepare = getDatabase().prepare(
                "CREATE TABLE  `" + getDatabase().getName() + "`.`" + getName() + "` (\n" +
                        "`id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY ," +
                        "`player` VARCHAR( 32 ) NULL ," +
                        "`region` VARCHAR( 32 ) NULL ," +
                        "`action` VARCHAR( 32 ) NULL ," +
                        "`price` DOUBLE NULL ," +
                        "`tax` DOUBLE NULL ," +
                        "`time` VARCHAR( 32 ) NULL " +
                        ") ENGINE = InnoDB;"
        );
        getDatabase().executeUpdate(prepare);
    }

    public void logAction(RegionLog log) {
        
        PreparedStatement statement = getDatabase().prepare(
                "INSERT INTO " + getName() + "(player, region, action, price, tax, time) VALUES (" +
                        "'" + log.getPlayer() + "'," +
                        "'" + log.getRegion() + "'," +
                        "'" + log.getAction() + "'," +
                        "'" + log.getPrice() + "'," +
                        "'" + log.getTax() + "'," +
                        "'" + log.getTime() + "'" +
                        ");"
        );
        getDatabase().executeUpdate(statement);
    }

    public List<RegionLog> getHistory(String region) {
        PreparedStatement statement = getDatabase().prepare(
                "SELECT * FROM " + getName() + " WHERE region = '" + region + "' ORDER BY id DESC LIMIT 0, 3;"
        );
        ResultSet resultSet = getDatabase().executeQuery(statement);
        List<RegionLog> regionHistory = new ArrayList<RegionLog>();
        try {
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
            RCLogger.warning(e.getMessage());
        }
        return regionHistory;
    }
}
