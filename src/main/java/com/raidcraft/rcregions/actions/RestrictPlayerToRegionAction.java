package com.raidcraft.rcregions.actions;

import com.raidcraft.rcregions.RegionsPlugin;
import com.raidcraft.rcregions.RestrictionManager;
import com.raidcraft.rcregions.exceptions.RegionException;
import de.raidcraft.api.action.action.Action;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

/**
 * @author Dragonfire
 */
public class RestrictPlayerToRegionAction implements Action<Player> {

    private RestrictionManager restrictionManager;

    public RestrictPlayerToRegionAction(RegionsPlugin plugin) {

        this.restrictionManager = plugin.getRestrictionManager();
    }

    @Override
    public void accept(Player player, ConfigurationSection config) {

        try {
            String region = config.getString("region", null);
            String message = config.getString("message", null);
            restrictionManager.restrictPlayerToRegion(player, region, message);
        } catch (RegionException e) {
            e.printStackTrace();
            player.sendMessage(ChatColor.RED + e.getMessage());
        }
    }
}