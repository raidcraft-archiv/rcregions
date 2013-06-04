package com.raidcraft.rcregions;

import com.raidcraft.rcregions.commands.RegionCommand;
import com.raidcraft.rcregions.config.DistrictConfig;
import com.raidcraft.rcregions.config.MainConfig;
import com.raidcraft.rcregions.listeners.RCBlockListener;
import com.raidcraft.rcregions.listeners.RCPlayerListener;
import de.raidcraft.api.BasePlugin;


/**
 *
 * 17.12.11 - 11:26
 * @author Silthus
 */
public class RegionsPlugin extends BasePlugin {

    private RegionManager regionManager;
    private DistrictManager districtManager;
    private MainConfig mainConfig;
    private DistrictConfig districtConfig;

    @Override
    public void enable() {

        // load all configs
        mainConfig = configure(new MainConfig(this), true);
        districtConfig = configure(new DistrictConfig(this), false);
        // init our manager
        regionManager = new RegionManager(this);
        districtManager = new DistrictManager(this);

        registerEvents(new RCBlockListener());
        registerEvents(new RCPlayerListener());
        registerCommands(RegionCommand.class);
        RegionsDatabase.getInstance();
        // start the warning task
        RCPlayerListener.startWarningTask();
    }

    @Override
    public void disable() {

    }

    public void reload() {

        getMainConfig().reload();
        getDistrictConfig().reload();
        getRegionManager().reload();
        getDistrictManager().reload();
    }

    public RegionManager getRegionManager() {

        return regionManager;
    }

    public DistrictManager getDistrictManager() {

        return districtManager;
    }

    public MainConfig getMainConfig() {

        return mainConfig;
    }

    public DistrictConfig getDistrictConfig() {

        return districtConfig;
    }
}
