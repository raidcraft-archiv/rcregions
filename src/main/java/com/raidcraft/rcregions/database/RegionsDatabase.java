package com.raidcraft.rcregions.database;

import com.raidcraft.rcregions.bukkit.RegionsPlugin;
import com.raidcraft.rcregions.config.MainConfig;
import com.silthus.raidcraft.database.RCDatabase;

import java.sql.PreparedStatement;
import java.util.HashSet;
import java.util.Set;

/**
 * User: Silthus
 */
public class RegionsDatabase extends RCDatabase {
    
    private static RegionsDatabase _self;
    private static MainConfig.DatabaseConfig config;
    
    public static void init() {
        config = MainConfig.getDatabase();
        get();
    }
    
    public static RegionsDatabase get() {
        if (_self == null) {
            _self = new RegionsDatabase();
            _self.getConnection().setupTables();
        }
        return _self;
    }
    
    private String prefix;
    private Set<String> tables = new HashSet<String>();

    private RegionsDatabase() {
        super(RegionsPlugin.get(),
                config.getName(),
                config.getUrl(),
                config.getUsername(),
                config.getPassword(),
                config.getType());
        this.prefix = config.getPrefix();
        tables.add(getPrefix() + "regions");
    }

    public Set<String> getTableNames() {
        return tables;
    }
    
    private String getPrefix() {
        return prefix;
    }

    public void createTables() {
        PreparedStatement prepare = connection.prepare(
                "CREATE TABLE  `" + getName() + "`.`" + getPrefix() + "regions` (\n" +
                "`id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY ,\n" +
                "`name` VARCHAR( 64 ) NOT NULL ,\n" +
                "`owner` VARCHAR( 64 ) NULL ,\n" +
                "`volume` DOUBLE NULL ,\n" +
                "`buyable` INT( 1 ) NULL\n" +
                ") ENGINE = InnoDB ;");
        connection.executeUpdate(prepare);
    }
}
