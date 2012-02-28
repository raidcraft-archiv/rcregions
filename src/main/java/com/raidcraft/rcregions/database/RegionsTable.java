package com.raidcraft.rcregions.database;

import com.silthus.raidcraft.database.Connection;
import com.silthus.raidcraft.database.Database;
import com.silthus.raidcraft.database.RCTable;

import java.sql.PreparedStatement;

/**
 * User: Silthus
 */
public class RegionsTable extends RCTable {
    

    public RegionsTable(Database database) {
        super(database, "regions");
    }

    @Override
    public void createTable() {
        Connection connection = getDatabase().getConnection();
        PreparedStatement prepare = connection.prepare(
                "CREATE TABLE  `" + getName() + "`.`" + getName() + "` (\n" +
                        "`id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY ,\n" +
                        "`name` VARCHAR( 64 ) NOT NULL ,\n" +
                        "`owner` VARCHAR( 64 ) NULL ,\n" +
                        "`volume` DOUBLE NULL ,\n" +
                        "`buyable` INT( 1 ) NULL\n" +
                        ") ENGINE = InnoDB ;");
        connection.executeUpdate(prepare);
    }
}
