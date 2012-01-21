package com.raidcraft.rcregions.listeners;

import com.raidcraft.rcregions.Region;
import com.raidcraft.rcregions.RegionManager;
import com.raidcraft.rcregions.config.MainConfig;
import com.raidcraft.rcregions.exceptions.UnknownRegionException;
import com.silthus.raidcraft.util.RCMessaging;
import com.silthus.raidcraft.util.SignUtils;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;

/**
 * 21.01.12 - 19:09
 *
 * @author Silthus
 */
public class RCPlayerListener extends PlayerListener {

    @Override
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Player player = event.getPlayer();
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && SignUtils.isSign(event.getClickedBlock())) {
            Sign sign = SignUtils.getSign(event.getClickedBlock());
            if (ChatColor.stripColor(sign.getLine(3)).equalsIgnoreCase("[" + MainConfig.getSignIdentifier() + "]")) {
                try {
                    Region region = RegionManager.get().getRegion(ChatColor.stripColor(sign.getLine(1).replaceAll("Region: ", "")));
                    String owner = region.getOwner();
                    if (owner == null) owner = "";
                    if ( !(player.hasPermission("rcregions.admin")) && owner.equalsIgnoreCase("")) {
                        RCMessaging.send(player, "Dieses Grundstück wird vom Server verwaltet.");
                        event.setCancelled(true);
                        return;
                    }
                    if ((player.hasPermission("rcregions.admin")) ||
                            (player.hasPermission("rcregions.region.sell") && owner.equalsIgnoreCase(player.getName()))) {
                        if (region.isBuyable()) {
                            region.setBuyable(false);
                            RCMessaging.send(player, "Die Region kann nun nicht mehr gekauft werden.");
                        } else {
                            region.setBuyable(true);
                            RCMessaging.send(player, "Die Region kann nun gekauft werden.");
                        }
                        RegionManager.get().updateSign(sign, region);
                    } else {
                        RCMessaging.noPermission(player);
                    }
                } catch (UnknownRegionException e) {
                    RCMessaging.warn(player, e.getMessage());
                }
            }
        } else if (event.getAction() == Action.LEFT_CLICK_BLOCK && SignUtils.isSign(event.getClickedBlock())) {
            Sign sign = SignUtils.getSign(event.getClickedBlock());
            if (ChatColor.stripColor(sign.getLine(3)).equalsIgnoreCase("[" + MainConfig.getSignIdentifier() + "]")) {
                if (player.getGameMode() == GameMode.CREATIVE) {
                    event.setCancelled(true);
                }
                try {
                    Region region = RegionManager.get().getRegion(ChatColor.stripColor(sign.getLine(1).replaceAll("Region: ", "")));
                    RegionManager.get().updateSign(sign, region);
                    if (region.isBuyable()) {
                        double price = RegionManager.get().getFullPrice(player, region);
                        RCMessaging.send(player, "Gebe \"/rcr -b " + region.getName() + "\" ein um die Region zu kaufen.");
                        RCMessaging.send(player, "Preis inkl. Steuern: " + price);
                    }
                } catch (UnknownRegionException e) {
                    RCMessaging.warn(player, e.getMessage());
                }
            }
        }
    }
}
