package com.raidcraft.rcregions.database;

import com.raidcraft.rcregions.bukkit.RegionsPlugin;
import com.raidcraft.rcregions.config.MainConfig;
import com.silthus.raidcraft.database.*;
import com.silthus.raidcraft.util.RCLogger;
import org.bukkit.Bukkit;

import java.sql.PreparedStatement;
import java.util.HashSet;
import java.util.Set;

/**
 * User: Silthus
 */
public class RegionsDatabase implements Database {
    
    private static RegionsDatabase _self;
    
    public static void init() {
        get();
    }
    
    public static RegionsDatabase get() {
        if (_self == null) {
            _self = new RegionsDatabase();
        }
        return _self;
    }
    
    private String username;
    private String password;
    private String hostname;
    private String databaseName;
    private String prefix;
    private DatabaseType type;
    private Set<String> tables = new HashSet<String>();
    
    private Connection connection;

    private RegionsDatabase() {
        MainConfig.DatabaseConfig config = MainConfig.getDatabase();
        this.username = config.getUsername();
        this.password = config.getPassword();
        this.hostname = config.getUrl();
        this.databaseName = config.getName();
        this.prefix = config.getPrefix();
        try {
            this.type = DatabaseType.getType(config.getType());
            if (type == DatabaseType.SQLITE) {
                this.databaseName = "regions.db";
            }
        } catch (UnknownDatabaseType e) {
            RCLogger.warning("Wrong database Type defined in Config! Fallback to Default: SQLite");
            this.type = DatabaseType.SQLITE;
            this.databaseName = "regions.db";
        }
        try {
            this.connection = DatabaseHandler.get().registerDatabase(this);
            tables.add(getPrefix() + "regions");
            tables.add(getPrefix() + "flags");
            tables.add(getPrefix() + "districts");
        } catch (DuplicateDatabaseException e) {
            RCLogger.warning("Error when opening Connection to the Database...");
            RCLogger.warning("...disabling RCRegions!");
            Bukkit.getServer().getPluginManager().disablePlugin(RegionsPlugin.get());
        }
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getHostname() {
        return hostname;
    }

    public Set<String> getTableNames() {
        return tables;
    }

    public String getDatabaseName() {
        return databaseName;
    }
    
    public DatabaseType getType() {
        return type;
    }
    
    private String getPrefix() {
        return prefix;
    }

    public void createTables(Connection connection) {
        PreparedStatement prepare = connection.prepare(
                "CREATE TABLE  `" + getDatabaseName() + "`.`" + getPrefix() + "regions` (\n" +
                "`id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY ,\n" +
                "`name` VARCHAR( 64 ) NOT NULL ,\n" +
                "`owner` VARCHAR( 64 ) NULL ,\n" +
                "`volume` DOUBLE NULL ,\n" +
                "`buyable` INT( 1 ) NULL\n" +
                ") ENGINE = InnoDB ;");
        connection.executeUpdate(prepare);
    }
}
