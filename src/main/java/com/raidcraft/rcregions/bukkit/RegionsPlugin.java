package com.raidcraft.rcregions.bukkit;

import com.raidcraft.rcregions.DistrictManager;
import com.raidcraft.rcregions.RegionManager;
import com.raidcraft.rcregions.achievements.RegionAchievements;
import com.raidcraft.rcregions.commands.RegionCommand;
import com.raidcraft.rcregions.config.DistrictConfig;
import com.raidcraft.rcregions.config.MainConfig;
import com.raidcraft.rcregions.config.RegionsConfig;
import com.raidcraft.rcregions.database.RegionsDatabase;
import com.raidcraft.rcregions.listeners.RCBlockListener;
import com.raidcraft.rcregions.listeners.RCPlayerListener;
import com.silthus.raidcraft.bukkit.BukkitBasePlugin;
import de.raidcraft.rcachievements.api.AchievementHandler;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;


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
    }
    
    public static RegionsPlugin get() {
        return _self;
    }

    @Override
    public void registerEvents() {
	    loadConfigs();
	    getServer().getPluginManager().registerEvents(new RCBlockListener(), this);
	    getServer().getPluginManager().registerEvents(new RCPlayerListener(), this);
        initializeManagers();
        registerCommand("rcr", new RegionCommand());
        RegionsDatabase.init();
	    // register achievements
	    registerAchievements();
    }

	private void registerAchievements() {
		// register a class for the achievement plugin
		Plugin rcAchievements = Bukkit.getPluginManager().getPlugin("RCAchievements");
		if (rcAchievements != null && rcAchievements.isEnabled()) {
			AchievementHandler.registerPlugin(this, RegionAchievements.class);
		}
	}

	private void loadConfigs() {
		MainConfig.init(this);
		DistrictConfig.init(this);
		RegionsConfig.init(this);
	}

    private void initializeManagers() {
        DistrictManager.init();
        RegionManager.init();
    }
    
    public void reload() {
        MainConfig.get().reload();
        RegionsConfig.get().reload();
        DistrictConfig.get().reload();
        DistrictManager.reload();
        RegionManager.reload();
    }
}
