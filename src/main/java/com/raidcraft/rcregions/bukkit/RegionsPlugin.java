package com.raidcraft.rcregions.bukkit;

import com.raidcraft.rcregions.DistrictManager;
import com.raidcraft.rcregions.RegionManager;
import com.raidcraft.rcregions.commands.RegionCommand;
import com.raidcraft.rcregions.config.MainConfig;
import com.raidcraft.rcregions.listeners.RCBlockListener;
import com.raidcraft.rcregions.listeners.RCPlayerListener;
import com.silthus.raidcraft.bukkit.BukkitBasePlugin;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.player.PlayerListener;

/**
 *
 * 17.12.11 - 11:26
 * @author Silthus
 */
public class RegionsPlugin extends BukkitBasePlugin {

    private static RegionsPlugin _self;

    public void onEnable() {
        super.onEnable();
        _self = this;
        getServer().getPluginManager().registerEvents(new RCBlockListener(), this);
        getServer().getPluginManager().registerEvents(new RCPlayerListener(), this);
    }
    
    public static RegionsPlugin get() {
        return _self;
    }
    
    @Override
    public void registerEvents() {
        MainConfig.init(this);
        initializeManagers();
        registerCommand("rcr", new RegionCommand());
    }

    private void initializeManagers() {
        DistrictManager.init();
        RegionManager.init();
    }
    
    public void reload() {
        MainConfig.reload();
        DistrictManager.reload();
        RegionManager.reload();
    }
}
