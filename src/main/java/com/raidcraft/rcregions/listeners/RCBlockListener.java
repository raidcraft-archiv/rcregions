package com.raidcraft.rcregions.listeners;

import com.raidcraft.rcregions.Region;
import com.raidcraft.rcregions.RegionsPlugin;
import com.raidcraft.rcregions.exceptions.UnknownRegionException;
import com.raidcraft.rcregions.exceptions.WrongSignFormat;
import de.raidcraft.RaidCraft;
import de.raidcraft.util.BlockUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

/**
 * 31.12.11 - 11:15
 *
 * @author Silthus
 */
public class RCBlockListener implements Listener {

    @EventHandler
    public void onSignChange(SignChangeEvent event) {

        if (event.isCancelled()) {
            return;
        }
        RegionsPlugin plugin = RaidCraft.getComponent(RegionsPlugin.class);
        Player player = event.getPlayer();
        if (event.getLine(3).equalsIgnoreCase(ChatColor.stripColor("[" + plugin.getMainConfig().sign_identitifer + "]"))) {
            player.sendMessage(ChatColor.RED + "Nene Freundchen, erstell mal lieber nen richtiges Schild!");
            event.setCancelled(true);
        } else if (event.getLine(0).equalsIgnoreCase("[" + plugin.getMainConfig().sign_identitifer + "]")) {
            if (player.hasPermission("rcregions.sign.place")) {
                try {
                    Region region;
                    if (!(event.getLine(1) == null) && !(event.getLine(1).equals(""))) {
                        region = plugin.getRegionManager().getRegion(event.getLine(1));
                    } else {
                        region = plugin.getRegionManager().getRegion(event.getBlock().getLocation());
                    }
                    if (!(player.getName().equalsIgnoreCase(region.getOwner()))) {
                        player.sendMessage(ChatColor.RED + "Du bist nicht der Besitzer dieser Region.");
                    }
                    if (!(event.getLine(2) == null) && !(event.getLine(2).equals(""))) {
                        double price = Double.parseDouble(event.getLine(2));
                        if (price < region.getDistrict().getMinPrice()) {
                            event.setCancelled(true);
                            throw new WrongSignFormat("Preis ist unter dem Mindestpreis der Region!");
                        }
                        region.setPrice(price);
                    }
                    plugin.getRegionManager().updateSign(event, region);
                } catch (WrongSignFormat | UnknownRegionException e) {
                    player.sendMessage(ChatColor.RED + e.getMessage());
                    event.setCancelled(true);
                } catch (NumberFormatException e) {
                    player.sendMessage(ChatColor.RED + "Bitte in Zeile 3 einen Preis oder nichts angeben.");
                    event.setCancelled(true);
                }
            } else {
                player.sendMessage(ChatColor.RED + "Du hast dafür keine Rechte.");
                event.setCancelled(true);
            }
        }
        if (event.isCancelled()) {
            BlockUtil.destroyBlock(event.getBlock());
        }
    }
}
