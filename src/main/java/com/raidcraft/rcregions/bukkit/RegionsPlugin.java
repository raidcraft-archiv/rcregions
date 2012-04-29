package com.raidcraft.rcregions.bukkit;

import com.raidcraft.rcregions.DistrictManager;
import com.raidcraft.rcregions.RegionManager;
import com.raidcraft.rcregions.commands.RegionCommand;
import com.raidcraft.rcregions.config.DistrictConfig;
import com.raidcraft.rcregions.config.MainConfig;
import com.raidcraft.rcregions.config.RegionsConfig;
import com.raidcraft.rcregions.database.RegionsDatabase;
import com.raidcraft.rcregions.listeners.RCBlockListener;
import com.raidcraft.rcregions.listeners.RCPlayerListener;
import com.silthus.raidcraft.bukkit.BukkitBasePlugin;
import com.silthus.raidcraft.util.RCLogger;
import org.bukkit.plugin.Plugin;
import org.getspout.spoutapi.Spout;


/**
 *
 * 17.12.11 - 11:26
 * @author Silthus
 */
public class RegionsPlugin extends BukkitBasePlugin {

    private static RegionsPlugin _self;
    private boolean spoutEnabled = false;

    public void onEnable() {
        super.onEnable();
        _self = this;
        getServer().getPluginManager().registerEvents(new RCBlockListener(), this);
        getServer().getPluginManager().registerEvents(new RCPlayerListener(), this);
        Plugin plugin = getServer().getPluginManager().getPlugin("Spout");
        if (plugin != null) {
            spoutEnabled = true;
        }
    }
    
    public static RegionsPlugin get() {
        return _self;
    }

    public boolean isSpoutEnabled() {
        return spoutEnabled;
    }

    @Override
    public void registerEvents() {
        MainConfig.init(this);
        RegionsConfig.init(this);
        DistrictConfig.init(this);
        initializeManagers();
        registerCommand("rcr", new RegionCommand());
        RegionsDatabase.init();
    }

    private void initializeManagers() {
        DistrictManager.init();
        RegionManager.init();
    }
    
    public void reload() {
        MainConfig.reload();
        RegionsConfig.reload();
        DistrictConfig.reload();
        DistrictManager.reload();
        RegionManager.reload();
    }
}
