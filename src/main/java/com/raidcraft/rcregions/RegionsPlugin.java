package com.raidcraft.rcregions;

import com.raidcraft.rcregions.api.configactions.CA_RestrictPlayerToRegion;
import com.raidcraft.rcregions.api.configactions.CA_UnrestrictPlayerFromRegion;
import com.raidcraft.rcregions.api.trigger.RegionTrigger;
import com.raidcraft.rcregions.commands.RegionCommand;
import com.raidcraft.rcregions.config.DistrictConfig;
import com.raidcraft.rcregions.config.MainConfig;
import com.raidcraft.rcregions.listeners.RCBlockListener;
import com.raidcraft.rcregions.listeners.RCPlayerListener;
import com.raidcraft.rcregions.tables.TRegion;
import com.raidcraft.rcregions.tables.TRestrictRegion;
import de.raidcraft.api.BasePlugin;
import de.raidcraft.api.action.ActionAPI;
import lombok.Getter;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;


/**
 * 17.12.11 - 11:26
 *
 * @author Silthus
 */
public class RegionsPlugin extends BasePlugin {

    private RegionManager regionManager;
    private DistrictManager districtManager;
    private MainConfig mainConfig;
    private DistrictConfig districtConfig;
    @Getter
    private RestrictionManager restrictionManager;

    @Override
    public void enable() {

        // load all configs
        mainConfig = configure(new MainConfig(this), true);
        districtConfig = configure(new DistrictConfig(this), false);
        // the region manager needs to init first because the district manager needs it
        regionManager = new RegionManager(this);
        districtManager = new DistrictManager(this);

        registerEvents(new RCBlockListener(this));
        registerEvents(new RCPlayerListener(this));

        registerCommands(RegionCommand.class);
        Bukkit.getScheduler().runTaskTimer(this, new PlayerTracker(this), -1, 20);
        restrictionManager = new RestrictionManager(this);

        setupActionApi();
//        Bukkit.getPluginManager().registerEvents(new Listener() {
//            @EventHandler
//            public void entry(RcPlayerEntryRegionEvent event) {
//
//                Bukkit.broadcastMessage(event.getPlayer().getName()
//                        + " enter " + event.getRegion().getId());
//            }
//
//            @EventHandler
//            public void exit(RcPlayerExitRegionEvent event) {
//
//                Bukkit.broadcastMessage(event.getPlayer().getName()
//                        + " exit " + event.getRegion().getId());
//            }
//        }, this);
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

    public void setupActionApi() {

        ActionAPI.register(this)
                .action("restrict-to-region", new CA_RestrictPlayerToRegion(this))
                .action("unrestrict-to-region", new CA_UnrestrictPlayerFromRegion(this))
                .trigger(new RegionTrigger());
    }

    @Override
    public List<Class<?>> getDatabaseClasses() {

        ArrayList<Class<?>> tables = new ArrayList<>();
        tables.add(TRegion.class);
        tables.add(TRestrictRegion.class);
        return tables;
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
