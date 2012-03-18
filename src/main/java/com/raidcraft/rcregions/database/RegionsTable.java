package com.raidcraft.rcregions.database;

import com.silthus.raidcraft.database.Database;
import com.silthus.raidcraft.database.RCTable;
import com.silthus.raidcraft.util.RCLogger;

import java.sql.SQLException;

/**
 * User: Silthus
 */
public class RegionsTable extends RCTable<RegionsTable> {
    

    public RegionsTable(Database database) {
        super(RegionsTable.class, database, "regions");
    }

    @Override
    public void createTable() {
	    try {
		    getConnection().prepareStatement(
				    "CREATE TABLE  `" + getName() + "`.`" + getName() + "` (\n" +
						    "`id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY ,\n" +
						    "`name` VARCHAR( 64 ) NOT NULL ,\n" +
						    "`owner` VARCHAR( 64 ) NULL ,\n" +
						    "`volume` DOUBLE NULL ,\n" +
						    "`buyable` INT( 1 ) NULL\n" +
						    ") ENGINE = InnoDB ;").executeUpdate();
	    } catch (SQLException e) {
		    RCLogger.warning(e.getMessage());
	    }
    }
}
