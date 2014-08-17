package com.raidcraft.rcregions.api.configactions;

import com.raidcraft.rcregions.RegionsPlugin;
import com.raidcraft.rcregions.RestrictionManager;
import com.raidcraft.rcregions.exceptions.RegionException;
import de.raidcraft.api.action.action.Action;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

/**
 * @author Dragonfire
 */
public class CA_RestrictPlayerToRegion implements Action<Player> {

    private RestrictionManager restrictionManager;

    public CA_RestrictPlayerToRegion(RegionsPlugin plugin) {

        this.restrictionManager = plugin.getRestrictionManager();
    }

    @Override
    public void accept(Player player, ConfigurationSection config) {

        try {
            String region = config.getString("region", null);
            String msg = config.getString("msg", null);
            restrictionManager.restrictPlayerToRegion(player, region, msg);
        } catch (RegionException e) {
            e.printStackTrace();
        }
    }
}