package com.raidcraft.rcregions.listeners;

import com.raidcraft.rcregions.Region;
import com.raidcraft.rcregions.RegionManager;
import com.raidcraft.rcregions.config.MainConfig;
import com.raidcraft.rcregions.exceptions.UnknownRegionException;
import com.raidcraft.rcregions.exceptions.WrongSignFormat;
import com.silthus.raidcraft.util.RCMessaging;
import com.silthus.raidcraft.util.RCUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.SignChangeEvent;

/**
 *
 * 31.12.11 - 11:15
 * @author Silthus
 */
public class RCBlockListener implements Listener {

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        if (event.isCancelled()) {
            return;
        }
        if (event.getLine(3).equalsIgnoreCase(ChatColor.stripColor("[" + MainConfig.getSignIdentifier() + "]"))) {
            RCMessaging.warn(event.getPlayer(), "Nene Freundchen, erstell mal lieber nen richtiges Schild!");
            event.setCancelled(true);
        } else if (event.getLine(0).equalsIgnoreCase("[" + MainConfig.getSignIdentifier() + "]")) {
            Player player = event.getPlayer();
            if (player.hasPermission("rcregions.sign.place")) {
                try {
                    Region region;
                    if (!(event.getLine(1) == null) && !(event.getLine(1).equals(""))) {
                        region = RegionManager.get().getRegion(event.getLine(1));
                    } else {
                        region = RegionManager.get().getRegion(event.getBlock().getLocation());
                    }
                    if (!(player.getName().equalsIgnoreCase(region.getOwner()))) {
                        RCMessaging.send(player, "Du bist nicht der Besitzer dieser Region.");
                    }
                    if (!(event.getLine(2) == null) && !(event.getLine(2).equals(""))) {
                        double price = Double.parseDouble(event.getLine(2));
                        if (price < region.getDistrict().getMinPrice()) {
                            event.setCancelled(true);
                            throw new WrongSignFormat("Preis ist unter dem Mindestpreis der Region!");
                        }
                        region.setPrice(price);
                    }
                    RegionManager.get().updateSign(event, region);
                } catch (WrongSignFormat e) {
                    RCMessaging.warn(player, e.getMessage());
                    event.setCancelled(true);
                } catch (UnknownRegionException e) {
                    RCMessaging.warn(player, e.getMessage());
                    event.setCancelled(true);
                } catch (NumberFormatException e) {
                    RCMessaging.warn(player, "Bitte in Zeile 3 einen Preis oder nichts angeben.");
                    event.setCancelled(true);
                }
            } else {
                RCMessaging.noPermission(player);
                event.setCancelled(true);
            }
        }
        if (event.isCancelled()) {
            RCUtils.destroyBlock(event.getBlock());
        }
    }
}
