package com.raidcraft.rcregions.bukkit;

import com.raidcraft.rcregions.commands.RegionCommand;
import com.raidcraft.rcregions.config.MainConfig;
import com.raidcraft.rcregions.database.RegionDatabase;
import com.raidcraft.rcregions.listeners.RCBlockListener;
import com.silthus.raidcraft.bukkit.BukkitBasePlugin;
import com.silthus.raidcraft.util.RCLogger;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockListener;

import javax.persistence.PersistenceException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * 17.12.11 - 11:26
 * @author Silthus
 */
public class RegionsPlugin extends BukkitBasePlugin {

    private static BukkitBasePlugin _self;
    private static final BlockListener blockListener = new RCBlockListener();

    public void onEnable() {
        super.onEnable();
        _self = this;
    }
    
    public static BukkitBasePlugin get() {
        return _self;
    }
    
    @Override
    public void registerEvents() {
        MainConfig.init(this);
        registerCommand("rcr", new RegionCommand());
        registerEvent(Event.Type.SIGN_CHANGE, blockListener);
        setupDatabase();
    }

    private void setupDatabase() {
        try {
            getDatabase().find(RegionDatabase.class).findRowCount();
        } catch (PersistenceException ex) {
            RCLogger.info("Installing database for " + getDescription().getName() + " due to first time usage.");
            installDDL();
        }
    }

    @Override
    public List<Class<?>> getDatabaseClasses() {
        List<Class<?>> list = new ArrayList<Class<?>>();
        list.add(RegionDatabase.class);
        return list;
    }
}
