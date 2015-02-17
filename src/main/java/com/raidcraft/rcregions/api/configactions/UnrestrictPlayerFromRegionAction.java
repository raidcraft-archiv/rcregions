package com.raidcraft.rcregions.api.configactions;

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
public class UnrestrictPlayerFromRegionAction implements Action<Player> {

    private RestrictionManager restrictionManager;

    public UnrestrictPlayerFromRegionAction(RegionsPlugin plugin) {

        this.restrictionManager = plugin.getRestrictionManager();
    }

    @Override
    public void accept(Player player, ConfigurationSection config) {

        try {
            String region = config.getString("region", null);
            restrictionManager.removePlayerToRegionRestriction(player, region);
        } catch (RegionException e) {
            e.printStackTrace();
            player.sendMessage(ChatColor.RED + e.getMessage());
        }
    }
}