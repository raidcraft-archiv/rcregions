package com.raidcraft.rcregions.listeners;

import com.raidcraft.rcregions.RegionsPlugin;
import com.raidcraft.rcregions.api.Region;
import com.raidcraft.rcregions.exceptions.UnknownDistrictException;
import com.raidcraft.rcregions.exceptions.UnknownRegionException;
import com.raidcraft.rcregions.exceptions.WrongSignFormatException;
import com.raidcraft.rcregions.util.RegionUtil;
import de.raidcraft.RaidCraft;
import de.raidcraft.util.SignUtil;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

/**
 * 31.12.11 - 11:15
 *
 * @author Silthus
 */
public class RCBlockListener implements Listener {

    private final RegionsPlugin plugin;

    public RCBlockListener(RegionsPlugin plugin) {

        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onSignChange(SignChangeEvent event) {

        // lets check the first and last line for the region identifier
        if (!SignUtil.isLineEqual(event.getLine(0), RaidCraft.getComponent(RegionsPlugin.class).getMainConfig().sign_identitifer)
                && !SignUtil.isLineEqual(event.getLine(3), RaidCraft.getComponent(RegionsPlugin.class).getMainConfig().sign_identitifer)) {
            return;
        }
        // lets check permissions
        if (!event.getPlayer().hasPermission("rcregions.sign.create")) {
            event.getPlayer().sendMessage(ChatColor.RED + "Du hast keine Rechte Regions Schilder aufzustellen.");
            event.setCancelled(true);
            return;
        }
        try {
            String regionName = RegionUtil.parseRegionName(event.getLines());
            Region region = plugin.getRegionManager().getRegion(regionName);
            String[] lines = RegionUtil.formatSign(region);
            // update the lines with the region information
            for (int i = 0; i < 4; i++) {
                event.setLine(i, lines[i]);
            }
        } catch (WrongSignFormatException | UnknownDistrictException | UnknownRegionException e) {
            event.getPlayer().sendMessage(ChatColor.RED + e.getMessage());
            event.setCancelled(true);
        }
    }
}
