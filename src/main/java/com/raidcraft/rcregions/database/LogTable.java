package com.raidcraft.rcregions.database;

import com.raidcraft.rcregions.util.Enums;
import com.silthus.raidcraft.database.Database;
import com.silthus.raidcraft.database.RCTable;

import java.sql.PreparedStatement;
import java.text.SimpleDateFormat;
import java.util.Date;

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
                        "`time` VARCHAR( 32 ) NULL ," +
                        ") ENGINE = InnoDB;"
        );
        getDatabase().executeUpdate(prepare);
    }

    public void logAction(String player, String region, Enums.Action action, double price, double tax) {
        SimpleDateFormat df = new SimpleDateFormat( "dd-MM-yyy HH:mm:ss" );
        PreparedStatement statement = getDatabase().prepare(
                "INSERT INTO " + getName() + "(player, region, action, price, tax, time) VALUES (" +
                        "'" + player + "'," +
                        "'" + region + "'," +
                        "'" + action.name() + "'" +
                        "'" + price + "'" +
                        "'" + tax + "'" +
                        "'" + df.format(new Date()) + "'" +
                        ");"
        );
        getDatabase().executeUpdate(statement);
    }
}
