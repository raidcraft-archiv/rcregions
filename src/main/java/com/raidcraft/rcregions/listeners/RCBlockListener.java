package com.raidcraft.rcregions.listeners;

import com.raidcraft.rcregions.Region;
import com.raidcraft.rcregions.RegionManager;
import com.silthus.raidcraft.util.RCMessaging;
import com.raidcraft.rcregions.config.MainConfig;
import com.raidcraft.rcregions.exceptions.UnknownRegionException;
import com.raidcraft.rcregions.exceptions.WrongSignFormat;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.SignChangeEvent;

/**
 *
 * 31.12.11 - 11:15
 * @author Silthus
 */
public class RCBlockListener extends BlockListener {

    @Override
    public void onSignChange(SignChangeEvent event) {
        if (event.isCancelled()) {
            return;
        }
        if (event.getLine(0).equalsIgnoreCase("[" + MainConfig.getSignIdentifier() + "]")) {
            Player player = event.getPlayer();
            // TODO: replace debug "true" with: player.hasPermission("rcregions.sign.place")
            if (true) {
                try {
                    if (!(event.getLine(1) == null) && !(event.getLine(1).equals(""))) {
                        Region region = RegionManager.get().getRegion(event.getLine(1));
                        event.setLine(1, "Nummer: " + ChatColor.RED + region.getName());
                        if (!(event.getLine(2) == null) && !(event.getLine(2).equals(""))) {
                            double price = Double.parseDouble(event.getLine(2));
                            if (price < region.getMinPrice()) {
                                throw new WrongSignFormat("Preis ist unter dem Mindestpreis der Region!");
                            }
                            region.setPrice(price);
                            event.setLine(0, "Preis: " + ChatColor.GREEN + price);
                        }
                        if ((event.getLine(3) == null) && (event.getLine(3).equals(""))) {
                            String owner = region.getOwner();
                            if (owner == null || owner.equals("") || owner.equals("Staff")) {
                                owner = "Staff";
                            }
                            if (owner.length() < 6) {
                                event.setLine(2, "Makler: " + ChatColor.WHITE + owner);
                            } else if (owner.length() < 14) {
                                event.setLine(2, "Makler: ");
                                event.setLine(3, ChatColor.WHITE + owner);
                            } else if (owner.length() < 18) {
                                event.setLine(2, "Makler: " + ChatColor.WHITE + owner.substring(0, 4) + "-");
                                event.setLine(3, ChatColor.WHITE + owner.substring(4));
                            } else {
                                event.setLine(2, "Makler: " + ChatColor.WHITE + "Owner");
                            }
                        } else {
                            throw new WrongSignFormat("In Zeile 4 darf nichts stehen.");
                        } 
                    } else {
                        throw new WrongSignFormat("In Zeile 2 muss der Name der Region stehen.");
                    }
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
    }
}
