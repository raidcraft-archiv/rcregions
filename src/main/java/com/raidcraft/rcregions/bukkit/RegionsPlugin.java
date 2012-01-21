package com.raidcraft.rcregions.bukkit;

import com.raidcraft.rcregions.DistrictManager;
import com.raidcraft.rcregions.RegionManager;
import com.raidcraft.rcregions.commands.RegionCommand;
import com.raidcraft.rcregions.config.MainConfig;
import com.raidcraft.rcregions.listeners.RCBlockListener;
import com.silthus.raidcraft.bukkit.BukkitBasePlugin;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockListener;

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
    }
    
    public void reload() {
        DistrictManager.reload();
        RegionManager.reload();
    }
}
