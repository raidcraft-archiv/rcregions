/*
 * Copyright (C) 2011 RaidCraft <http://www.raid-craft.de>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.silthus.rcregions.bukkit;

import com.nijikokun.register.payment.Methods;
import com.silthus.raidcraft.bukkit.BukkitBasePlugin;
import com.silthus.raidcraft.exceptions.UnknownPluginException;
import com.silthus.raidcraft.util.RCEconomy;
import com.silthus.raidcraft.util.RCLogger;
import com.silthus.rcregions.RCRegionManager;
import com.silthus.rcregions.commands.RCRCommand;
import com.silthus.rcregions.config.RegionsConfig;
import com.silthus.rcregions.listeners.BlockListener;
import com.silthus.rcregions.listeners.PlayerListener;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;

import static com.nijikokun.register.payment.Methods.getMethod;

/**
 *
 * 27.09.11 - 11:01
 * @author Silthus
 */
public class RCRegionsPlugin extends BukkitBasePlugin {
    
    // our event listeners
    private final org.bukkit.event.block.BlockListener blockListener = new BlockListener(this);
    private final org.bukkit.event.player.PlayerListener playerListener = new PlayerListener(this);
    // private final org.bukkit.event.server.ServerListener serverListener = new ServerListener(this);
    // our dependencies
    private WorldGuardPlugin worldGuard;

    /**
     * Enables this RaidCraft Plugin module
     */
    public void onEnable() {
        // load our plugin thru the core module
        super.onEnable();
    }

    public void onDisable() {
        // unload our plugin thru the core module
        super.onDisable();
        // save all regions
        // TODO: need to save regions when WorldGuard is disabled
        RCRegionManager.save();
    }

    /**
     * This is fired when onEnable() in the BukkitBasePlugin
     * is called.
     */
    @Override
    public void registerEvents() {
        // create a new config file and load
        new RegionsConfig(this);
        // register our command label
        registerCommand("rcd", new RCRCommand());
        // enable dependencies
        // if this fails the plugin will be terminated
        loadDependencies();
        // initialize our regionManager
        RCRegionManager.init(this);
        // register our event listeners
        registerEvent(Event.Type.SIGN_CHANGE, blockListener);
        registerEvent(Event.Type.BLOCK_BREAK, blockListener);
        registerEvent(Event.Type.PLAYER_INTERACT, playerListener);
    }

    /**
     * Loads all critical plugin dependencies
     */
    private void loadDependencies() {
        loadWorldGuard();
        loadRegister();
    }

    /**
     * Loads Register
     */
    private void loadRegister() {
        Methods.setMethod(getServer().getPluginManager());
        if (Methods.getMethod() != null) {
            RCEconomy.init(this);
            RCLogger.warning("Economy method found: " + getMethod().getName() + getMethod().getVersion());
        }
    }

    /**
     * Loads WorldGuard or stops the plugin if
     * WorldGuard was not found
     */
    private void loadWorldGuard() {
        try {
            // try to get WorldGuard
            worldGuard = enableWorldGuard();
        } catch (UnknownPluginException e) {
            // print fail message and ...
            RCLogger.warning(e.getMessage());
            // ... stop our plugin
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    /**
     * Gets the refrence to the WorldGuardPlugin
     * @return WorldGuard
     * @throws UnknownPluginException WorldGuard not enabled
     */
    private WorldGuardPlugin enableWorldGuard() throws UnknownPluginException {
        Plugin plugin = getServer().getPluginManager().getPlugin("WorldGuard");

        // WorldGuard may not be loaded
        if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
            throw new UnknownPluginException("WorldGuard not found! Disabling RCRegions...");
        }
    return (WorldGuardPlugin) plugin;
    }

    /**
     * Gets the WorldGuard plugin refrence
     * @return WorldGuard
     */
    public WorldGuardPlugin getWorldGuard() {
        return worldGuard;
    }
}
